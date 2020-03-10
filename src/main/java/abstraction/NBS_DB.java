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

    public Map<MatchFieldEnum, Object> getFieldsById(long id, final Set<MatchFieldEnum> attrs) {
        Set<String> requiredColumns;
        Map<String, Set<MatchFieldEnum>> tableNameMap = MatchFieldUtils.getTableNameMap(attrs);
        Map<MatchFieldEnum, Object> ret = new HashMap<>();
        ResultSet rs;
        for(String tableName : tableNameMap.keySet()) {
            requiredColumns = new HashSet<>();
            for (MatchFieldEnum mfield : tableNameMap.get(tableName)) {
                requiredColumns.addAll(Arrays.asList(mfield.getRequiredColumnsArray()));
            }
            requiredColumns.add(Constants.COL_PERSON_UID);
            try {
                Statement query = conn.createStatement();
                String q = "SELECT " + String.join(",", Lists.newArrayList(requiredColumns)) +
                        " from " + tableName + " where " + Constants.COL_PERSON_UID + " = " + id;
                rs = query.executeQuery(q);
                rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not query SQL DB to find Person with id "
                        + id + " and rows " + String.join(",", Lists.newArrayList(requiredColumns)));
            }
            for (MatchFieldEnum mf : tableNameMap.get(tableName)) {
                try {
                    ret.put(mf, mf.getFieldValue(rs));
                } catch (SQLException e) {
                    e.printStackTrace();
                    ;
                    throw new RuntimeException("Couldn't get value for field " + mf + " in resultset");
                }
            }
        }
        return ret;
    }

    public AuxMap constructAuxMap(final Set<MatchFieldEnum> attrs) {

        Map<String, Set<MatchFieldEnum>> tableNameMap = MatchFieldUtils.getTableNameMap(attrs);
        ResultSet rs;
        List<String> colsForEachTableList = new ArrayList<>();
        String queryString = "SELECT ";
        for(String tableName : tableNameMap.keySet()) {
            Set<String> requiredColumns = new HashSet<>();
            for (MatchFieldEnum mfield : tableNameMap.get(tableName)) {
                for(String reqiredColumn : mfield.getRequiredColumnsArray()) {
                    requiredColumns.add(tableName + "." + reqiredColumn);
                }
            }
            requiredColumns.add(tableName + "." + Constants.COL_PERSON_UID);
            colsForEachTableList.add(String.join(",", Lists.newArrayList(requiredColumns)));
        }
        queryString += String.join(",", colsForEachTableList);
        queryString += " from " + String.join(",", Lists.newArrayList(tableNameMap.keySet()));
        if(tableNameMap.keySet().size() > 1) {
            //This kind of joining found at https://www.geeksforgeeks.org/joining-three-tables-sql/
            queryString += " where ";
            Iterator<String> iter = tableNameMap.keySet().iterator();
            String primaryTableName = iter.next();
            while (iter.hasNext()) {
                //TODO make a map from each table to the name of its Person ID column, use that instead of Constants.COL_PERSON_UID all the time
                queryString += primaryTableName + "." + Constants.COL_PERSON_UID + " = " + iter.next() + "." + Constants.COL_PERSON_UID;
                if(iter.hasNext()) queryString += " and ";
            }
        }
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

                if (!include_entry) {
                    continue;
                } else {
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
