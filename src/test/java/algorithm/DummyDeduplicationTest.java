package algorithm;

import abstraction.Constants;
import abstraction.DummyDataTest;
import abstraction.MatchFieldEnum;
import org.junit.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DummyDeduplicationTest extends DummyDataTest {
    protected static String personSchema;
    protected static String personColumns;
    protected static String personNameSchema;
    protected static String personNameColumns;

    @BeforeClass
    public static void createTables() throws SQLException {
        StringBuilder schema = new StringBuilder();
        StringBuilder cols = new StringBuilder();
        schema.append(Constants.COL_PERSON_UID);
        cols.append(Constants.COL_PERSON_UID);
        schema.append(" BIGINT NOT NULL, "); //TODO write a member of MatchField that gives the schema for each field
        cols.append(", ");
        schema.append(Constants.COL_SSN);
        cols.append(Constants.COL_SSN);
        schema.append(" VARCHAR(12) ");
        personSchema = schema.toString();
        personColumns = cols.toString();
        dummy_conn.createTempTable("Person", personSchema);
        //TODO add other tables like the one referenced in MatchFieldEnum.OTHER_TABLE_NAME
        schema = new StringBuilder();
        cols = new StringBuilder();
        schema.append(Constants.COL_PERSON_UID);
        cols.append(Constants.COL_PERSON_UID);
        schema.append(" BIGINT NOT NULL, ");
        cols.append(", ");
        schema.append(Constants.COL_FIRST_NAME);
        cols.append(Constants.COL_FIRST_NAME);
        schema.append(" VARCHAR(50), ");
        cols.append(", ");
        schema.append(Constants.COL_LAST_NAME);
        cols.append(Constants.COL_LAST_NAME);
        schema.append(" VARCHAR(50) ");
        personNameSchema = schema.toString();
        personNameColumns = cols.toString();
        dummy_conn.createTempTable("Person_name", personNameSchema);
    }
    @AfterClass
    public static void dropTables() throws SQLException {
        dummy_conn.dropTable("Person");
        dummy_conn.dropTable("Person_name");
    }

    @Before
    public void populateTables() throws SQLException {
        dummy_conn.insertRow("Person", personColumns, "123, '999-99-9999'");
        dummy_conn.insertRow("Person", personColumns, "124, '999-99-9990'");
        dummy_conn.insertRow("Person", personColumns, "125, '999-99-9999'");
        dummy_conn.insertRow("Person", personColumns, "126, '999-99-9990'");

        dummy_conn.insertRow("Person_name", personNameColumns, "123, 'Joe', 'Schmoe'"); //TODO add more records to both tables
        dummy_conn.insertRow("Person_name", personNameColumns, "124, 'Jane', 'Schmoe'"); //TODO add more records to both tables
    }

    @After
    public void clearTables() throws SQLException {
        dummy_conn.clearTable("Person");
        dummy_conn.clearTable("Person_name");
    }
    @Test
    public void testRead() throws SQLException {
        Set<MatchFieldEnum> attrs = new HashSet<>();
        attrs.add(MatchFieldEnum.FIRST_NAME);
        attrs.add(MatchFieldEnum.SSN);
        Map<MatchFieldEnum, Object> results = al.getFieldsById(123l, attrs);
        for(MatchFieldEnum key : results.keySet()) {
            System.out.println(key + " : " + results.get(key));
        }
        Map<MatchFieldEnum, Object> expectedResults = new HashMap<>();
        expectedResults.put(MatchFieldEnum.FIRST_NAME, "Joe");
        expectedResults.put(MatchFieldEnum.SSN, "999-99-9999");
        assert(expectedResults.equals(results));
    }

}
