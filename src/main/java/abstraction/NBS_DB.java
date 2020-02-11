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
                if (idsWithSameHash == null) {
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
