package abstraction;

import algorithm.Deduplication;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import hashing.HashUtils;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NBS_DBTest {

    private NBS_DB db;
    private Deduplication dService;

    @Before
    public void setupDatabaseConnection() throws SQLException {
        db = new NBS_DB("localhost", 1433, "ODS_PRIMARY_DATA01",
                "SA", "saYyWbfZT5ni7t");
        dService = new Deduplication();
    }

    @Test
    public void testCreateAuxMap() {
        AuxMap aux = db.constructAuxMap(Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME,
                MatchFieldEnum.SSN)));

        assert (aux.hashToIdMap != null);
        assert (aux.idToHashMap != null);

        assert aux.hashToIdMap.size() > 0;
        assert aux.idToHashMap.size() > 0;
    }

    @Test
    public void testGetMatches() throws Exception{

        List<Set<MatchFieldEnum>> config = new ArrayList<>();
        config.add(Sets.newHashSet(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.LAST_NAME));

        List<Set<Set<Long>>> res = dService.getMatching(db, config);
        System.out.println(res);
    }

    @Test
    public void testGetMatchesMerge() throws Exception{

        List<Set<MatchFieldEnum>> config = new ArrayList<>();
        config.add(Sets.newHashSet(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.LAST_NAME));

        List<Set<Long>> res = dService.getMatchingMerged(db, config);
        System.out.println("merged results " + res.toString());

    }

//    @Test
//    public void testGetOrCreateAuxTable() throws SQLException {
//        dedupService.create_or_get_aux_db(this.db, Lists.newArrayList(Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.SSN)),
//                Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.LAST_NAME))));
//    }
//
//    @Test
//    public void testGetDuplicates() throws Exception{
//        dedupService.get_duplicates(db, Lists.newArrayList(Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.SSN)),
//                Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.LAST_NAME))));
//    }
//
    @Test
    public void testAddHook() throws Exception{
        AuxMapManager.hookAddRecord(db, Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.SSN)), 100L, HashUtils.hash(0));
    }

    @Test
    public void testRemoveHook() throws Exception{
        AuxMapManager.hookRemoveRecord(db, Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.SSN)), 100L, HashUtils.hash(0));
    }
}
