package abstraction;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashSet;

public class NBS_DBTest {

    private NBS_DB db;

    @Before
    public void setupDatabaseConnection() throws SQLException {
        db = new NBS_DB("localhost", 1433, "ODS_PRIMARY_DATA01",
                "SA", "saYyWbfZT5ni7t");
    }

    @Test
    public void testCreateAuxTable() throws SQLException {
        db.constructAuxTable(Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.SSN)));
    }
}