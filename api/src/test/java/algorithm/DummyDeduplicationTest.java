package algorithm;

import Constants.InternalConstants;

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
        schema.append(InternalConstants.COL_PERSON_UID);
        cols.append(InternalConstants.COL_PERSON_UID);
        schema.append(" BIGINT NOT NULL, ");
        cols.append(", ");
        schema.append(InternalConstants.COL_SSN);
        cols.append(InternalConstants.COL_SSN);
        schema.append(" VARCHAR(12) ");
        personSchema = schema.toString();
        personColumns = cols.toString();
        dummy_conn.createTempTable("Person", personSchema);
        schema = new StringBuilder();
        cols = new StringBuilder();
        schema.append(InternalConstants.COL_PERSON_UID);
        cols.append(InternalConstants.COL_PERSON_UID);
        schema.append(" BIGINT NOT NULL, ");
        cols.append(", ");
        schema.append(InternalConstants.COL_FIRST_NAME);
        cols.append(InternalConstants.COL_FIRST_NAME);
        schema.append(" VARCHAR(50), ");
        cols.append(", ");
        schema.append(InternalConstants.COL_LAST_NAME);
        cols.append(InternalConstants.COL_LAST_NAME);
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
        dummy_conn.insertRow("Person", personColumns, "123, '999-99-9999'");
        dummy_conn.insertRow("Person", personColumns, "124, '999-99-9990'");
        dummy_conn.insertRow("Person", personColumns, "125, '999-99-9999'");
        dummy_conn.insertRow("Person", personColumns, "126, '999-99-9990'");

        dummy_conn.insertRow("Person_name", personNameColumns, "123, 'Joe', 'Schmoe'"); //TODO add more records to both tables
        dummy_conn.insertRow("Person_name", personNameColumns, "124, 'Jane', 'Schmoe'");
        dummy_conn.insertRow("Person_name", personNameColumns, "125, 'Jeff', 'Schmoe'");
        dummy_conn.insertRow("Person_name", personNameColumns, "126, 'JJ', 'S'");
    }

    @Before
    public void deleteAuxMapDir() throws IOException {
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
        Set<Set<Long> > matchingIDs = Deduplication.getMatchingMerged(al, config);
        Set<Set<Long> > expectedMatchingIDs = new HashSet<>();
        Set<Long> temp = new HashSet<>();
        temp.add(123l);
        temp.add(125l);
        expectedMatchingIDs.add(temp);
        temp = new HashSet<>();
        temp.add(124l);
        temp.add(126l);
        expectedMatchingIDs.add(temp);
        assert(expectedMatchingIDs.equals(matchingIDs));
    }
    @Test
    public void testDedupMultiField() throws SQLException {
        Set<MatchFieldEnum> attrs = new HashSet<>();
        attrs.add(MatchFieldEnum.SSN);
        attrs.add(MatchFieldEnum.LAST_NAME);
        ArrayList<Set<MatchFieldEnum> > config = new ArrayList<>();
        config.add(attrs);
        Set<Set<Long> > matchingIDs = Deduplication.getMatchingMerged(al, config);
        Set<Set<Long> > expectedMatchingIDs = new HashSet<>();
        Set<Long> temp = new HashSet<>();
        temp.add(123l);
        temp.add(125l);
        expectedMatchingIDs.add(temp);
        assert(expectedMatchingIDs.equals(matchingIDs));
    }
}
