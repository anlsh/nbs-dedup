package abstraction;

import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public class NBS_DBTest {

    private NBS_DB db;

    @Before
    public void setupDatabaseConnection() throws SQLException {
        db = new NBS_DB("localhost", 1433, "ODS_PRIMARY_DATA01",
                "SA", "saYyWbfZT5ni7t");
    }

    @Test
    public void testGetPersonIDs() throws SQLException {
        db.getDatabaseAsMap();
    }
}