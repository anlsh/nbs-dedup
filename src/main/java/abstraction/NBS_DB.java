package abstraction;

import java.sql.*;
import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
        Map<String, Set<MatchFieldEnum>> tableNameMap = MatchFieldEnum.getTableNameMap(attrs);
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
                            + MatchFieldUtils.getAliasedColName(tableName, reqiredColumn)
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
     * @return
     */
    public AuxMap constructAuxMap(final Set<MatchFieldEnum> attrs, int num_threads) {

        ResultSet rs;
        String queryString = getSQLQueryForAllEntries(attrs, null);

        try {
            Statement query = conn.createStatement();
            rs = query.executeQuery(queryString);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not connect to and query SQL database");
        }

        Map<Long, HashCode> idToHash = new HashMap<>();
        Map<HashCode, Set<Long>> hashToIDs = new HashMap<>();

        List<Map<MatchFieldEnum, Object>> dbD= new LinkedList<>();

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
                    HashCode hash = HashUtils.hashFields(attr_map);

                    idToHash.put(record_id, hash);

                    Set<Long> idsWithSameHash = hashToIDs.getOrDefault(hash, null);
                    if (idsWithSameHash != null) {
                        idsWithSameHash.add(record_id);
                    } else {
                        hashToIDs.put(hash, Sets.newHashSet(record_id));
                    }
                }
            }
        } catch (SQLException e) {
            // TODO Exception Handling
            e.printStackTrace();
            throw new RuntimeException("Error while trying to scan database entries");
        }

        return new AuxMap(attrs, idToHash, hashToIDs);
    }
    public AuxMap constructAuxMap(final Set<MatchFieldEnum> attrs) {
        return constructAuxMap(attrs, 1);
    }
}
