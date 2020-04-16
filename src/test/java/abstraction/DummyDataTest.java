package abstraction;

import org.h2.tools.Server;
import org.junit.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DummyDataTest {
    protected static DbAuxConstructor al;
    protected static DummyDataConnAux dummy_conn;

    @BeforeClass
    public static void setupDb() throws SQLException, ClassNotFoundException {
        dummy_conn = new DummyDataConnAux("test_db");
        al = new DbAuxConstructor(dummy_conn.conn);
    }
    @AfterClass
    public static void closeDbConnection() {
        dummy_conn.close();
    }
    protected static class DummyDataConnAux {
        Server server;
        Connection conn;
        public DummyDataConnAux(String name) throws SQLException, ClassNotFoundException {
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

        public void clearTable(String tableName) throws SQLException {
            //https://stackoverflow.com/questions/3000917/delete-all-from-table
            String clearString = "TRUNCATE TABLE " + tableName + ";";
            conn.createStatement().execute(clearString);
        }

        public void dropTable(String tableName) throws SQLException {
            String dropString = "DROP TABLE " + tableName + ";";
            conn.createStatement().execute(dropString);
        }
    }

    @Test
    public void testCreateTestDb() throws SQLException, ClassNotFoundException {
        dummy_conn.createTempTable("test_table_1", "id INT NOT NULL, name VARCHAR(50)");
        dummy_conn.insertRow("test_table_1", "id, name", "1, 'test_name'");
        dummy_conn.clearTable("test_table_1");
        dummy_conn.dropTable("test_table_1");
    }
}
