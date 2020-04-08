package algorithm;

import abstraction.MatchFieldEnum;
import abstraction.AuxLogic;
import abstraction.NBSConnection;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeduplicationTest {
    private AuxLogic db;

    @Before
    public void setupDatabaseConnection() throws SQLException, IOException {
        db = new AuxLogic(NBSConnection.getNBSConnection("localhost", 1433, "ODS_PRIMARY_DATA01",
                "SA", "saYyWbfZT5ni7t"));
    }

    @Test
    public void testGetMatches() throws SQLException {

        List<Set<MatchFieldEnum>> config = new ArrayList<>();
        config.add(Sets.newHashSet(
                MatchFieldEnum.FIRST_NAME,
                MatchFieldEnum.LAST_NAME
        ));
        List<Set<Set<Long>>> res = Deduplication.getMatching(db, config);
    }

    @Test
    public void testGetMatchesMerge() throws SQLException {

        List<Set<MatchFieldEnum>> config = new ArrayList<>();
        config.add(Sets.newHashSet(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.LAST_NAME));

        Set<Set<Long>> res = Deduplication.getMatchingMerged(db, config);
        System.out.println("merged results " + res.toString());

    }

}
