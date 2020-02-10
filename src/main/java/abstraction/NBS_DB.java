package abstraction;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NBS_DB {

    public Connection conn;

    public NBS_DB(String server, int port, String dbName, String username, String password) throws SQLException {
        String connectionUrl = "jdbc:sqlserver://" + server + ":" + port + ";databaseName=" + dbName
                + ";user=" + username +  ";password=" + password;

        conn = DriverManager.getConnection(connectionUrl);
    }
    public List<Map<String, Object>> getDatabaseAsMap() throws SQLException {

        // TODO might want to convert this to an array later on... fine for now though
        List ll = new LinkedList<Map<String, Object>>();

        Statement query = conn.createStatement();
        ResultSet rs = query.executeQuery("SELECT * from Person");
        ResultSetMetaData rsMeta = rs.getMetaData();

        int num_columns = rsMeta.getColumnCount();

        // TODO This methodology is sourced from https://stackoverflow.com/questions/7507121/efficient-way-to-handle-resultset-in-java
        // But should be abstracted using a standard library like DBUtils or MapListHandler
        while (rs.next()) {
            HashMap row = new HashMap<String, Object>(num_columns);

            for (int c = 1; c <= num_columns; c++) {
                row.put(rsMeta.getColumnName(c), rs.getObject(c));
            }
            ll.add(row);
        }

        return ll;
    }
//    public Object get_id_attr(DatabaseRecord id, RecordFields field) {
//        // TODO Implement
//        // Get database[id][field]
//        return null;
//    }
}
