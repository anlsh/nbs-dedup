package abstraction;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ensure that AuxMap construction logic works on a real NBS database (such as dummy db which team 9345 was provided)
 * Since we don't know the data in the database necessarily, the tests in this class ensure that none of the auxmap
 * constructions fail. For tests which verify the actual logic of our algorithms, see <></>
 * TODO Update the above line with the "actual" test class, once it exists
 */
public class connAuxLogicRealDbTest {

    private AuxLogic al;

    @Before
    public void setupDatabaseConnection() throws SQLException {
        Connection nbsConn = NBSConnectionFactory.make(
                Constants.DB_SERVER, Constants.DB_PORT, Constants.DB_NAME,
                Constants.DB_USERNAME, Constants.DB_PASSWORD
        );
        al = new AuxLogic(nbsConn);
    }

    /**
     * Create a dummy auxiliary map and run some basic checks which should pass on any reasonable database
     */
    @Test
    public void testCreateAuxMap() {
        AuxMap aux = al.constructAuxMap(
                Sets.newHashSet(Lists.newArrayList(MatchFieldEnum.FIRST_NAME, MatchFieldEnum.LAST_NAME)),
                1
        );

        assert (aux.getIdToHashes() != null);
        assert (aux.getIdToHashes() != null);

        assert !aux.getIdToHashes().isEmpty();
        assert !aux.getHashToIds().isEmpty();
    }
}