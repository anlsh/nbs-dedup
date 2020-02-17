package algorithm;

import abstraction.MatchFieldEnum;
import abstraction.NBS_DB;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class DeduplicationTest {
    private NBS_DB db;
    private Deduplication dService;
    Stopwatch timer;

    @Before
    public void setupDatabaseConnection() throws SQLException {
        db = new NBS_DB("localhost", 1433, "ODS_PRIMARY_DATA01",
                "SA", "saYyWbfZT5ni7t");
        dService = new Deduplication();
        timer = Stopwatch.createUnstarted();
    }

    @Test
    public void testGetMatches() throws Exception {

        List<Set<MatchFieldEnum>> config = new ArrayList<>();
        config.add(Sets.newHashSet(
                MatchFieldEnum.FIRST_NAME,
                MatchFieldEnum.LAST_NAME
        ));


        timer.start();
        List<Set<Set<Long>>> res = dService.getMatching(db, config);
        timer.stop();

        System.out.println("Test completed in " + timer);
        System.out.println(res);
    }

    @Test
    public void testGetMatchesMerge() throws Exception{

        List<Set<MatchFieldEnum>> config = new ArrayList<>();
        config.add(Sets.newHashSet(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.LAST_NAME));

//        List<Set<Long>> res = dService.getMatchingMerged(db, config);
        Set<Set<Long>> res = dService.getMatchingMerged(db, config);
        System.out.println("merged results " + res.toString());

    }

}