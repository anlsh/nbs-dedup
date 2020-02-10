package abstraction;

import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class NBS_DBTest {
    @Test
    public void databaseConnectedSucceeds() throws SQLException {
        NBS_DB db = new NBS_DB("localhost", 1433, "ODS_PRIMARY_DATA01",
                "SA", "saYyWbfZT5ni7t");
    }
}