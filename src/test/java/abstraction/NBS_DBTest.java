package abstraction;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public class NBS_DBTest {

    private NBS_DB db;

    @Before
    public void setupDatabaseConnection() throws SQLException {
        db = new NBS_DB(Constants.DB_SERVER, Constants.DB_PORT, Constants.DB_NAME,
                Constants.DB_USERNAME, Constants.DB_PASSWORD);
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
}
