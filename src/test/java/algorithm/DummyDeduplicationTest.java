package algorithm;

import abstraction.*;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

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
        //This isn't super necessary since the db is set up to be temporary and delete itself when the program
        //is done running. However, in case we add other extensions, we should do this.
    }

    @Before
    public void populateTables() throws SQLException {
        System.out.println("Populating tables");
        dummy_conn.insertRow("Person", personColumns, "123, '999-99-9999'");
        dummy_conn.insertRow("Person", personColumns, "124, '999-99-9990'");
        dummy_conn.insertRow("Person", personColumns, "125, '999-99-9999'");
        dummy_conn.insertRow("Person", personColumns, "126, '999-99-9990'");

        dummy_conn.insertRow("Person_name", personNameColumns, "123, 'Joe', 'Schmoe'"); //TODO add more records to both tables
        dummy_conn.insertRow("Person_name", personNameColumns, "124, 'Jane', 'Schmoe'"); //TODO add more records to both tables
    }

    @Before
    public void deleteAuxMapDir() throws IOException {
        System.out.println("Deleting auxmap dir");
        FileUtils.deleteDirectory(new File(AuxMapManager.getDataRoot()));
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
    @Test
    public void testDedupSingleField() throws SQLException {
        Set<MatchFieldEnum> attrs = new HashSet<>();
        attrs.add(MatchFieldEnum.SSN);
        ArrayList<Set<MatchFieldEnum> > config = new ArrayList<>();
        config.add(attrs);
        List<Set<Set<Long> >> matchingIDs = Deduplication.getMatching(al, config);
        System.out.println("Matching IDs:");
        for(Set<Set<Long> > matchingIDSet : matchingIDs) {
            for(Set<Long> s : matchingIDSet) {
                System.out.print("\t[");
                for (Long l : s) {
                    System.out.print(l + ", ");
                }
                System.out.println("]");
            }
        }
        Set<Set<Long> > expectedMatchingIDs = new HashSet<>();
        Set<Long> temp = new HashSet<>();
        temp.add(123l);
        temp.add(125l);
        expectedMatchingIDs.add(temp);
        temp = new HashSet<>();
        temp.add(124l);
        temp.add(126l);
//        expectedMatchingIDs.add(temp);
        //assert(expectedMatchingIDs.equals(matchingIDs));
    }
}
