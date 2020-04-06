package abstraction;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Set;

public class NBS_DBTest {

    private NBS_DB db;
    private String personSchema;
    private String personColumns;

    @Before
    public void setupDatabaseConnection() throws SQLException, ClassNotFoundException {
//        db = new NBS_DB(Constants.DB_SERVER, Constants.DB_PORT, Constants.DB_NAME,
//                Constants.DB_USERNAME, Constants.DB_PASSWORD);
        db = new NBS_DB("test_db");
        createTables();
        populateTables();
    }

    private void createTables() throws SQLException {
        StringBuilder schema = new StringBuilder();
        StringBuilder cols = new StringBuilder();
        schema.append(Constants.COL_PERSON_UID);
        cols.append(Constants.COL_PERSON_UID);
        schema.append(" INT NOT NULL, "); //TODO write a member of MatchField that gives the schema for each field
        cols.append(", ");
        schema.append(Constants.COL_FIRST_NAME);
        cols.append(Constants.COL_FIRST_NAME);
        schema.append(" VARCHAR(50), ");
        cols.append(", ");
        schema.append(Constants.COL_LAST_NAME);
        cols.append(Constants.COL_LAST_NAME);
        schema.append(" VARCHAR(50), ");
        cols.append(", ");
        schema.append(Constants.COL_SSN);
        cols.append(Constants.COL_SSN);
        schema.append(" VARCHAR(12) ");
        personSchema = schema.toString();
        personColumns = cols.toString();
        db.createTempTable("Person", personSchema);
        //TODO add other tables like the one referenced in MatchFieldEnum.OTHER_TABLE_NAME
    }

    private void populateTables() throws SQLException {
        db.insertRow("Person", personColumns, "123, 'Joe', 'Schmoe', '999-99-9999'");
        db.insertRow("Person", personColumns, "124, 'Jane', 'Schmoe', '999-99-9990'");
        db.insertRow("Person", personColumns, "125, 'Joe', 'Pesci', '999-99-9999'");
        db.insertRow("Person", personColumns, "126, 'Jane', 'Doe', '999-99-9990'");
    }

    @Test
    public void testCreateAuxMap() {
        AuxMap aux = db.constructAuxMap(Sets.newHashSet(Lists.newArrayList(
                MatchFieldEnum.FIRST_NAME,
                MatchFieldEnum.SSN,
                MatchFieldEnum.OTHER_TABLE_NAME
        )));

        assert (aux.hashToIdMap != null);
        assert (aux.idToHashMap != null);

        assert aux.hashToIdMap.size() > 0;
        assert aux.idToHashMap.size() > 0;
    }

    @Test
    public void testParalellizationSpeedsUpAuxMapCreation() {
        Set<MatchFieldEnum> attrSet = Sets.newHashSet(Lists.newArrayList(
                MatchFieldEnum.FIRST_NAME
        ));

        int NUM_TRIALS = 10000;
        int SLOW_RUN_THREADS = 1;
        int FAST_RUN_THREADS = 4;

        // Time the fast section
        long fast_start_time = System.currentTimeMillis();
        for (int i = 0; i < NUM_TRIALS; ++i) {
            db.constructAuxMap(attrSet, FAST_RUN_THREADS);
        }
        System.out.println(NUM_TRIALS + " auxmap constructions using " + FAST_RUN_THREADS + " took "
                + (((double)((System.currentTimeMillis() - fast_start_time))) / 1000) + " seconds");

        // Time the slow section
        long slow_start_time = System.currentTimeMillis();
        for (int i = 0; i < NUM_TRIALS; ++i) {
            db.constructAuxMap(attrSet, SLOW_RUN_THREADS);
        }
        System.out.println(NUM_TRIALS + " auxmap constructions using " + SLOW_RUN_THREADS + " took "
                + (((double)(System.currentTimeMillis() - slow_start_time)) / 1000) + " seconds");
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
