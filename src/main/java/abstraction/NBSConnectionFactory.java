package abstraction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for making Connection objects for NBS databases
 */
public class NBSConnectionFactory {
    /**
     * @param serverURL         URL for the machine running the SQL server
     * @param port              Port on which the SQL server is exposed
     * @param dbName            Name of the database to be accessed on the SQL server
     * @param username          Database username
     * @param password          Database password
     * @return                  A Connection object if connection is successful
     * @throws SQLException     When connection fails
     */
    public static Connection make(String serverURL, int port, String dbName, String username, String password)
            throws SQLException {
        Connection conn = DriverManager.getConnection(
                "jdbc:sqlserver://" + serverURL + ":" + port
                        + ";databaseName=" + dbName
                        + ";user=" + username
                        +  ";password=" + password
        );
        conn.setReadOnly(true);
        return conn;
    }
}
