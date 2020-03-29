package abstraction;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public class NBS_DBTest {

    private NBS_DB db;
    Stopwatch timer;

    @Before
    public void setupDatabaseConnection() throws SQLException, ClassNotFoundException {
//        db = new NBS_DB(Constants.DB_SERVER, Constants.DB_PORT, Constants.DB_NAME,
//                Constants.DB_USERNAME, Constants.DB_PASSWORD);
        db = new NBS_DB("test_db");
        timer = Stopwatch.createUnstarted();
    }

    @Test
    public void testCreateAuxMap() throws SQLException {
        timer.start();
        AuxMap aux = db.constructAuxMap(Sets.newHashSet(Lists.newArrayList(
                MatchFieldEnum.FIRST_NAME,
                MatchFieldEnum.SSN,
                MatchFieldEnum.OTHER_TABLE_NAME
        )));
        timer.stop();

        System.out.println("Test finished in " + timer);

        assert (aux.hashToIdMap != null);
        assert (aux.idToHashMap != null);

        assert aux.hashToIdMap.size() > 0;
        assert aux.idToHashMap.size() > 0;
    }

    @Test
    public void testCreateTestDB() throws SQLException, ClassNotFoundException {
        db.createTempTable("test_table_1", "id INT NOT NULL, name VARCHAR(50)");
        db.insertRow("test_table_1", "id, name", "1, 'test_name'");
    }

//    @Test
//    public void testAddHook() throws Exception{
//        AuxMapManager.hookAddRecord(db, Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.SSN)), 100L, HashUtils.hash(0));
//    }
//
//    @Test
//    public void testRemoveHook() throws Exception{
//        AuxMapManager.hookRemoveRecord(db, Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.SSN)), 100L, HashUtils.hash(0));
//    }
}
