package abstraction;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import hashing.HashUtils;
import utils.BlockingThreadPool;
import utils.ConcurrentSet;
import utils.ResultType;

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

    /** When a hash collision (potential match) is detected, we need to retrieve the original information to ensure
     * that the match is in fact genuine. This function calculates the MatchField values for the given person_uid and
     * returns them in a dictionary
     * @param id
     * @param attrs
     * @return
     * @throws SQLException
     */
    public Map<MatchFieldEnum, Object> getFieldsById(long id, final Set<MatchFieldEnum> attrs) throws SQLException {
        ConcurrentMap<MatchFieldEnum, Object> ret = new ConcurrentHashMap<>();
        String queryString = getSQLQueryForAllEntries(attrs, id);
        ResultSet rs = conn.createStatement().executeQuery(queryString);
        rs.next();
        for (MatchFieldEnum mf : attrs) {
            ResultType result = mf.getFieldValue(rs);
            ret.put(mf, result.value);
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

        // If only fetching for a single id, add that constraint to the query
        List<String> where_clauses = new ArrayList<>();
        if (uid != null) {
            where_clauses.add(MatchFieldUtils.getSQLQualifiedColName(Constants.PRIMARY_TABLE_NAME, Constants.COL_PERSON_UID)
                    + " = " + uid);
        }
        // Align the columns from each table by the person_uid column.
        if(tableNameMap.keySet().size() > 1) {
            Iterator<String> iter = tableNameMap.keySet().iterator();
            while (iter.hasNext()) {
                where_clauses.add(
                        MatchFieldUtils.getSQLQualifiedColName(Constants.PRIMARY_TABLE_NAME, Constants.COL_PERSON_UID)
                        + " = "
                        + MatchFieldUtils.getSQLQualifiedColName(iter.next(), Constants.COL_PERSON_UID));
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
            query.setFetchSize(Constants.fetch_size);
            rs = query.executeQuery(queryString);
            rs.setFetchSize(Constants.fetch_size);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not connect to and query SQL database");
        }

        ExecutorService executor = new BlockingThreadPool(num_threads, Constants.blocking_q_size);

        // The sets referred to in the type signature below are in fact synchronized.
        ConcurrentMap<Long, Set<HashCode>> idToHash = new ConcurrentHashMap<>();
        ConcurrentMap<HashCode, Set<Long>> hashToIDs = new ConcurrentHashMap<>();

        // Loop over the items in the ResultSet, submitting them to a thread pool to be hashed in a concurrent fashion.

        ArrayList<MatchFieldEnum> attrsAsList = new ArrayList<>(attrs);

        try {
            while (rs.next()) {
                Map<MatchFieldEnum, Object> attr_map = new HashMap<>();
                boolean include_entry = true;

                long uid = (long) MatchFieldEnum.UID.getFieldValue(rs).value;

                for (MatchFieldEnum mfield : attrsAsList) {
                    ResultType result = mfield.getFieldValue(rs);
                    if (result.unknown) {
                        include_entry = false;
                        break;
                    } else {
                        attr_map.put(mfield, result.value);
                    }
                }

                if (include_entry) {
                    executor.execute(
                            () -> {
                                HashCode hash = HashUtils.hashFields(attr_map);
                                Set<HashCode> currentIdToHashes = idToHash.getOrDefault(uid, null);
                                if (currentIdToHashes != null) {
                                    currentIdToHashes.add(hash);
                                } else {
                                    idToHash.put(uid, ConcurrentSet.newSingletonSet(hash));
                                }

                                Set<Long> idsWithSameHash = hashToIDs.getOrDefault(hash, null);
                                if (idsWithSameHash != null) {
                                    idsWithSameHash.add(uid);
                                } else {
                                    hashToIDs.put(hash, ConcurrentSet.newSingletonSet(uid));
                                }
                            }
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while trying to scan database entries");
        }

        executor.shutdown();
        return new AuxMap(attrs, idToHash, hashToIDs);
    }

    public AuxMap constructAuxMap(final Set<MatchFieldEnum> attrs) {
        return constructAuxMap(attrs, Constants.NUM_AUXMAP_THREADS);
    }
}
