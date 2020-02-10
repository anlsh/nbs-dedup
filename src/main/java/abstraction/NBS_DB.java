package abstraction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;

public class NBS_DB {

    public Connection conn;

    public NBS_DB(String server, int port, String dbName, String username, String password) throws SQLException {
        String connectionUrl = "jdbc:sqlserver://" + server + ":" + port + ";databaseName=" + dbName
                + ";user=" + username +  ";password=" + password;

        conn = DriverManager.getConnection(connectionUrl);
    }
//    public List<String> get_id_list() throws SQLException {
//
//        Statement query = conn.createStatement();
//        String queryText = "SELECT TOP 10 * FROM Person.Contact"
//    }
//    public Object get_id_attr(DatabaseRecord id, RecordFields field) {
//        // TODO Implement
//        // Get database[id][field]
//        return null;
//    }
}
