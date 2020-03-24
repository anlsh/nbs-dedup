package abstraction;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import hashing.HashUtils;

public class NBS_DB {

    public Connection conn;

    public NBS_DB(String server, int port, String dbName, String username, String password) throws SQLException {
        conn = DriverManager.getConnection(
                "jdbc:sqlserver://" + server + ":" + port
                        + ";databaseName=" + dbName
                        + ";user=" + username
                        +  ";password=" + password
        );
        conn.setReadOnly(true);
    }

    public Map<MatchFieldEnum, Object> getFieldsById(long id, final Set<MatchFieldEnum> attrs) throws SQLException {
        // When a hash collision (potential match) is detected, we need to retrieve the original information to ensure
        // that the original information matches.
        Map<MatchFieldEnum, Object> ret = new HashMap<>();
        String queryString = getSQLQueryForAllEntries(attrs, id);
        ResultSet rs = conn.createStatement().executeQuery(queryString);
        rs.next();
        for (MatchFieldEnum mf : attrs) {
            ret.put(mf, mf.getFieldValue(rs));
        }
        return ret;
    }

    /** Generates an SQL query to retrieve the columns for any given set of match fields and (potentially) any specific
     * id.
     *
     * If uid is null, then returns a ResultSet containing the needed columns for every single entry in the
     * database. If it is non-null, then the ResultSet only contains the information concerning the given uid
     * @param attrs The set of MatchFields to deduplicate on
     * @param uid The specific person ID to retrieve information for, or null if information for all IDs should be
     *            retrieved
     * @return
     */
    private String getSQLQueryForAllEntries(Set<MatchFieldEnum> attrs, Long uid) {
        Map<String, Set<MatchFieldEnum>> tableNameMap = MatchFieldUtils.getTableNameMap(attrs);
        List<String> tableColumns = new ArrayList<>();
        String queryString = "SELECT ";
        for(String tableName : tableNameMap.keySet()) {
            List<String> currTableColumns = new ArrayList<>();
            currTableColumns.add(
                    MatchFieldUtils.getSQLQualifiedColName(tableName, Constants.COL_PERSON_UID)
                    + " as " + MatchFieldUtils.getAliasedColName(tableName, Constants.COL_PERSON_UID)
            );
            for (MatchFieldEnum mfield : tableNameMap.get(tableName)) {
                for(String reqiredColumn : mfield.getRequiredColumnsArray()) {
                    currTableColumns.add(
                            MatchFieldUtils.getSQLQualifiedColName(tableName, reqiredColumn)
                            + " as " + MatchFieldUtils.getAliasedColName(tableName, reqiredColumn)
                    );
                }
            }
            tableColumns.add(String.join(", ", currTableColumns));
        }
        queryString += String.join(", ", tableColumns);
        queryString += " from " + String.join(", ", Lists.newArrayList(tableNameMap.keySet()));

        // TODO Don't hardcode the primary table name!
        // If only fetching for a single id, add that constraint to the query
        List<String> where_clauses = new ArrayList<>();
        if (uid != null) {
            where_clauses.add("Person." + Constants.COL_PERSON_UID + " = " + uid);
        }
        // Align the columns from each table by the person_uid column.
        if(tableNameMap.keySet().size() > 1) {
            Iterator<String> iter = tableNameMap.keySet().iterator();
            String primaryTableName = iter.next();
            while (iter.hasNext()) {
                //TODO make a map from each table to the name of its Person ID column, use that instead of Constants.COL_PERSON_UID all the time
                where_clauses.add(primaryTableName + "." + Constants.COL_PERSON_UID + " = " + iter.next() + "." + Constants.COL_PERSON_UID);
            }
        }
        if (where_clauses.size() > 0) {
            queryString += " where " + String.join(" and ", where_clauses);
        }

        return queryString;
    }

    /** Given a set of match fields, traverse the database and create a fresh AuxMap object.
     * @param attrs
     * @param num_threads The number of worker threads used to hash database entries.
     * @return
     */
    public AuxMap constructAuxMap(final Set<MatchFieldEnum> attrs, int num_threads) {

        // Obtain a ResultSet object through which to access the database
        ResultSet rs;
        String queryString = getSQLQueryForAllEntries(attrs, null);

        try {
            Statement query = conn.createStatement();
            rs = query.executeQuery(queryString);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not connect to and query SQL database");
        }

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(num_threads);
        Map<Long, HashCode> idToHash = new ConcurrentHashMap<>();
        // The sets referred to in the type signature below must of course be synchronized. However, Java does not seem
        // to have a "concurrent set" type, although they can in fact be created by "deriving" from a Hash Map.
        // We take that approach below https://better-coding.com/solved-how-to-create-concurrenthashset-before-and-after-java-8/
        Map<HashCode, Set<Long>> hashToIDs = new ConcurrentHashMap<>();

        class HashDatabaseEntry implements Runnable {

            /**
             * Represents a job which will calculate the hash of the given attribute map and update the relevant
             * maps for the final AuxMap
             */

            Map<MatchFieldEnum, Object> attr_map;
            long uid;
            HashDatabaseEntry(long uid, Map<MatchFieldEnum, Object> attr_map) {
                this.uid = uid;
                this.attr_map = attr_map;
            }

            @Override
            public void run() {
                HashCode hash = HashUtils.hashFields(attr_map);
                idToHash.put(uid, hash);

                Set<Long> idsWithSameHash = hashToIDs.getOrDefault(hash, null);
                if (idsWithSameHash != null) {
                    idsWithSameHash.add(uid);
                } else {
                    Map<Long, Boolean> myMap = new ConcurrentHashMap<>();
                    Set<Long> concurrentSet = Collections.newSetFromMap(myMap);
                    concurrentSet.add(uid);
                    hashToIDs.put(hash, concurrentSet);
                }
            }
        }

        // Loop over the items in the ResultSet, submitting them to a thread pool to be hashed in a concurrent fashion.
        // TODO In the case that jobs are added to the queue faster than they can be processed, it is possible that
        // calls to the ThreadPoolExecutor's execute function should block until there are less than <n> items in
        // its job queue. Otherwise, this function may essentially load the entire database into RAM
        try {
            while (rs.next()) {
                Map<MatchFieldEnum, Object> attr_map = new HashMap<>();

                boolean include_entry = true;

                for (MatchFieldEnum mfield : attrs) {
                    Object mfield_val = mfield.getFieldValue(rs);
                    if (mfield.isUnknownValue(mfield_val)) {
                        include_entry = false;
                        break;
                    }
                    attr_map.put(mfield, mfield.getFieldValue(rs));
                }

                if (include_entry) {
                    long record_id = (long) MatchFieldEnum.UID.getFieldValue(rs);
                    executor.execute(new HashDatabaseEntry(record_id, attr_map));
                }
            }
            executor.shutdown();
        } catch (SQLException e) {
            // TODO Exception Handling
            e.printStackTrace();
            throw new RuntimeException("Error while trying to scan database entries");
        }

        return new AuxMap(attrs, idToHash, hashToIDs);
    }
    public AuxMap constructAuxMap(final Set<MatchFieldEnum> attrs) {
        return constructAuxMap(attrs, Constants.NUM_AUXMAP_THREADS);
    }
}
