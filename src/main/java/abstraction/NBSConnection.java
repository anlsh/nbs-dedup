package abstraction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class NBSConnection {
    public static Connection getNBSConnection(String server, int port, String dbName, String username, String password)
            throws SQLException {
        Connection conn = DriverManager.getConnection(
                "jdbc:sqlserver://" + server + ":" + port
                        + ";databaseName=" + dbName
                        + ";user=" + username
                        +  ";password=" + password
        );
        conn.setReadOnly(true);
        return conn;
    }
}
