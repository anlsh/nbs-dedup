package algorithm;

import abstraction.AuxMapManager;
import abstraction.MatchFieldEnum;
import abstraction.DbAuxConstructor;
import abstraction.NBSConnectionFactory;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeduplicationTest {
    private DbAuxConstructor db;

    @Before
    public void setupDatabaseConnection() throws SQLException, IOException {
        db = new DbAuxConstructor(NBSConnectionFactory.make("localhost", 1433, "ODS_PRIMARY_DATA01",
                "SA", "saYyWbfZT5ni7t"));
        FileUtils.deleteDirectory(new File(AuxMapManager.getDataRoot()));
    }

    @Test
    public void testGetMatches() throws SQLException {

        List<Set<MatchFieldEnum>> config = new ArrayList<>();
        config.add(Sets.newHashSet(
                MatchFieldEnum.FIRST_NAME,
                MatchFieldEnum.RACE
        ));
        List<Set<Set<Long>>> res = Deduplication.getMatchingHelper(db, config);
    }

    @Test
    public void testGetMatchesMerge() throws SQLException {

        List<Set<MatchFieldEnum>> config = new ArrayList<>();
        config.add(Sets.newHashSet(MatchFieldEnum.SSN, MatchFieldEnum.LAST_NAME));

        Set<Set<Long>> res = Deduplication.getMatchingMerged(db, config);
    }
}
