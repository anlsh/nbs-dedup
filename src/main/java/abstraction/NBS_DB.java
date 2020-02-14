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
        conn.setReadOnly(true); //For safety
    }

    public ResultSet getResultSetById(long id) {
        ResultSet ret;
        try {
            Statement query = conn.createStatement();
            ret = query.executeQuery("SELECT * from Person where " + Constants.COL_PERSON_UID + " = " + id);
        } catch(SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not query SQL DB to find Person with id " + id);
        }
        return ret;
    }

    public Map<MatchFieldEnum, Object> getFieldsById(long id, final Set<MatchFieldEnum> attrs) {
        Set<String> requiredColumns = new HashSet<>();
        for (MatchFieldEnum mfield : attrs) {
            requiredColumns.addAll(MatchFieldUtils.getRequiredColumns(mfield));
        }
        requiredColumns.add(Constants.COL_PERSON_UID);

        ResultSet rs;
        try {
            Statement query = conn.createStatement();
            String q = "SELECT " + String.join(",", Lists.newArrayList(requiredColumns)) +
                    " from Person where " + Constants.COL_PERSON_UID + " = " + id;
            rs = query.executeQuery(q);
            rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not query SQL DB to find Person with id "
                    + id + " and rows " + String.join(",", Lists.newArrayList(requiredColumns)));
        }
        Map<MatchFieldEnum, Object> ret = new HashMap<>();
        for(MatchFieldEnum mf : attrs) {
            try {
                ret.put(mf, MatchFieldUtils.getFieldValue(rs, mf));
            } catch(SQLException e) {
                e.printStackTrace();;
                throw new RuntimeException("Couldn't get value for field " + mf + " in resultset");
            }
        }
        return ret;
    }

    public AuxMap constructAuxMap(final Set<MatchFieldEnum> attrs) {

        Set<String> requiredColumns = new HashSet<>();
        for (MatchFieldEnum mfield : attrs) {
            requiredColumns.addAll(MatchFieldUtils.getRequiredColumns(mfield));
        }
        requiredColumns.add(Constants.COL_PERSON_UID);

        ResultSet rs;
        try {
            Statement query = conn.createStatement();
            rs = query.executeQuery(
                    "SELECT " + String.join(",", Lists.newArrayList(requiredColumns)) +
                            " from Person"
            );
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

                for (MatchFieldEnum mfield : attrs) {
                    attr_map.put(mfield, MatchFieldUtils.getFieldValue(rs, mfield));
                }

                long record_id = (long) MatchFieldUtils.getFieldValue(rs, MatchFieldEnum.UID);
                HashCode hash = HashUtils.hashFields(attr_map);

                idToHash.put(record_id, hash);

                Set<Long> idsWithSameHash = hashToIDs.getOrDefault(hash, null);
                if (idsWithSameHash != null) {
                    idsWithSameHash.add(record_id);
                } else {
                    hashToIDs.put(hash, Sets.newHashSet(record_id));
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
