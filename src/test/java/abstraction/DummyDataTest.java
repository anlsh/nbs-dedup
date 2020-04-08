package abstraction;

import org.h2.tools.Server;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DummyDataTest {
    private class DummyDataDB extends NBS_DB {
        private Server server;
        public DummyDataDB(String name) throws SQLException, ClassNotFoundException {
            server = Server.createTcpServer("-tcpAllowOthers").start();
            Class.forName("org.h2.Driver"); //I'm not sure why this needs to be here
            conn = DriverManager.getConnection("jdbc:h2:tcp://localhost/~/" + name, "sa", "");
        }

        //Only for test DBs
        public void close() {
            server.stop();
        }

        public void createTempTable(String tableName, String columnDefinitions) throws SQLException {
            //https://www.tutorialspoint.com/h2_database/h2_database_create.htm
            String createString = "CREATE TEMPORARY TABLE " + tableName + "(" + columnDefinitions + ") NOT PERSISTENT;";
            conn.createStatement().executeUpdate(createString);
        }

        //https://www.w3schools.com/sql/sql_insert.asp
        public void insertRow(String tableName, String columns, String values) throws SQLException {
            String insertString = "INSERT INTO " + tableName + "(" + columns + ") VALUES (" + values + ");";
            conn.createStatement().executeUpdate(insertString);
        }
    }
}
