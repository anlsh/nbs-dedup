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
        String connectionUrl = "jdbc:sqlserver://" + server + ":" + port + ";databaseName=" + dbName
                + ";user=" + username +  ";password=" + password;

        conn = DriverManager.getConnection(connectionUrl);
        // For safety
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

    private String getSQLQueryForAllEntries(Set<MatchFieldEnum> attrs, Long specific_id) {
        Map<String, Set<MatchFieldEnum>> tableNameMap = MatchFieldEnum.getTableNameMap(attrs);
        List<String> tableColumns = new ArrayList<>();
        String queryString = "SELECT ";
        for(String tableName : tableNameMap.keySet()) {
            List<String> currTableColumns = new ArrayList<>();
            currTableColumns.add((tableName + "." + Constants.COL_PERSON_UID) + " as " + (tableName + "__" + Constants.COL_PERSON_UID));
            for (MatchFieldEnum mfield : tableNameMap.get(tableName)) {
                for(String reqiredColumn : mfield.getRequiredColumnsArray()) {
                    currTableColumns.add((tableName + "." + reqiredColumn) + " as " + (tableName + "__" + reqiredColumn));
                }
            }
            tableColumns.add(String.join(", ", currTableColumns));
        }
        queryString += String.join(", ", tableColumns);
        queryString += " from " + String.join(", ", Lists.newArrayList(tableNameMap.keySet()));

        // TODO Don't hardcode the primary table name!
        List<String> where_clauses = new ArrayList<>();
        if (specific_id != null) {
            where_clauses.add("Person." + Constants.COL_PERSON_UID + " = " + specific_id);
        }
        if(tableNameMap.keySet().size() > 1) {
            //This kind of joining found at https://www.geeksforgeeks.org/joining-three-tables-sql/
            // queryString += " where ";
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

    public AuxMap constructAuxMap(final Set<MatchFieldEnum> attrs) {

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

        // TODO This methodology is sourced from https://stackoverflow.com/questions/7507121/efficient-way-to-handle-resultset-in-java
        // But should be abstracted using a standard library like DBUtils or MapListHandler

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
}
