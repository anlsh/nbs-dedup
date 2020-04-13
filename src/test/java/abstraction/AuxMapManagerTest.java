package abstraction;

import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AuxMapManagerTest {

    private AuxLogic db;

    @Before
    public void setupDatabaseConnection() throws IOException, SQLException {
        Connection nbsConn = NBSConnectionFactory.make(Constants.DB_SERVER, Constants.DB_PORT, Constants.DB_NAME,
                Constants.DB_USERNAME, Constants.DB_PASSWORD);
        db = new AuxLogic(nbsConn);
        FileUtils.deleteDirectory(new File(AuxMapManager.getDataRoot()));
    }

    @Test
    public void testAuxMapFilenameConstruction() {
        String auxFileName = AuxMapManager.mfieldSetToFilename(Sets.newHashSet(MatchFieldEnum.FIRST_NAME));
        System.out.println(auxFileName);
        assert auxFileName.equals(AuxMapManager.getDataRoot() + "-1921453883.auxmap");
    }

    @Test


    public void testGetAuxMap() throws SQLException {
        Set<MatchFieldEnum> mfields = Sets.newHashSet(MatchFieldEnum.FIRST_NAME);

        assert !AuxMapManager.auxMapExists(mfields);
        AuxMapManager.getAuxMap(db, mfields, false);
        assert AuxMapManager.auxMapExists(mfields);
    }

    @Test
    public void testAuxMapManualLoadAndSave() {

        long MAGIC_KEY = 7;

        ConcurrentMap<Long, Set<HashCode>> dummyData = new ConcurrentHashMap<>();
        dummyData.put(MAGIC_KEY, new HashSet<>());
        Set<MatchFieldEnum> empty = new HashSet<>();

        ConcurrentMap<HashCode, Set<Long>> dummyHashToId = new ConcurrentHashMap<>();

        AuxMap emptyAux = new AuxMap(empty, dummyData, dummyHashToId);

        assert !AuxMapManager.auxMapExists(empty);
        AuxMapManager.saveAuxMapToFile(emptyAux);
        assert AuxMapManager.auxMapExists(empty);

        AuxMap loadedMap = AuxMapManager.loadAuxMapFromFile(empty);
        assert loadedMap.attrs.equals(empty);
        assert loadedMap.idToHashMap.size() == 1;
        assert loadedMap.idToHashMap.get(MAGIC_KEY).isEmpty();

        AuxMapManager.deleteAuxMap(empty);
        assert !AuxMapManager.auxMapExists(empty);
    }
}
