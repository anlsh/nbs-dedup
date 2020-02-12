package abstraction;

import algorithm.Deduplication;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public class NBS_DBTest {

    private NBS_DB db;
    private Deduplication dedupService;

    @Before
    public void setupDatabaseConnection() throws SQLException {
        db = new NBS_DB("localhost", 1433, "ODS_PRIMARY_DATA01",
                "SA", "saYyWbfZT5ni7t");
        dedupService = new Deduplication(db);
    }

    @Test
    public void testCreateAuxTable() throws SQLException {
        db.constructAuxMap(Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.SSN)));
    }

    @Test
    public void testGetOrCreateAuxTable() throws SQLException {
        dedupService.create_or_get_aux_db(this.db, Lists.newArrayList(Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.SSN)),
                Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.LAST_NAME))));
    }

    @Test
    public void testGetDuplicates() throws Exception{
        dedupService.getMatching(db, Lists.newArrayList(Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.SSN)),
                Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.LAST_NAME))));
    }

    @Test
    public void testAddHook() throws Exception{
        dedupService.hookAddRecord(db, Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.SSN)), 100L, 100L);
    }

    @Test
    public void testRemoveHook() throws Exception{
        dedupService.hookRemoveRecord(db, Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.SSN)), 100L, 100L);
    }

}
