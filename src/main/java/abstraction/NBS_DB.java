package abstraction;

import java.sql.*;
import java.util.*;

import com.google.common.collect.Lists;
import hashing.HashUtils;

public class NBS_DB {

    public Connection conn;

    public NBS_DB(String server, int port, String dbName, String username, String password) throws SQLException {
        String connectionUrl = "jdbc:sqlserver://" + server + ":" + port + ";databaseName=" + dbName
                + ";user=" + username +  ";password=" + password;

        conn = DriverManager.getConnection(connectionUrl);
    }

    public Map<Long, Long> constructAuxTable(final Set<MatchFieldEnum> attrs) throws SQLException {

        Set<String> requiredColumns = new HashSet<String>();
        for (MatchFieldEnum mfield : attrs) {
            requiredColumns.addAll(MatchFieldUtils.getRequiredColumns(mfield));
        }
        requiredColumns.add(Constants.COL_PERSON_UID);

        Statement query = conn.createStatement();
        ResultSet rs = query.executeQuery(
                "SELECT " + String.join(",", Lists.newArrayList(requiredColumns)) +
                        " from Person"
        );

        HashMap<Long, Long> auxTable = new HashMap<Long, Long>();

        // TODO This methodology is sourced from https://stackoverflow.com/questions/7507121/efficient-way-to-handle-resultset-in-java
        // But should be abstracted using a standard library like DBUtils or MapListHandler
        while (rs.next()) {
            HashMap attr_map = new HashMap<MatchFieldEnum, Object>();

            for (MatchFieldEnum mfield : attrs) {
                attr_map.put(mfield, MatchFieldUtils.getFieldValue(rs, mfield));
            }

            auxTable.put(
                    (long) MatchFieldUtils.getFieldValue(rs, MatchFieldEnum.UID),
                    HashUtils.hashFields(attr_map)
            );
        }

        return auxTable;
    }
}
