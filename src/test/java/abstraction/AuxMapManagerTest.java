package abstraction;

import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AuxMapManagerTest {

    private NBS_DB db;
    private final String testDataDirectory = "/tmp/nbs-data/";

    @Before
    public void setupDatabaseConnection() throws IOException, SQLException {
        db = new NBS_DB("localhost", 1433, "ODS_PRIMARY_DATA01",
                "SA", "saYyWbfZT5ni7t");
        AuxMapManager.setDataRoot(testDataDirectory);
        FileUtils.deleteDirectory(new File(testDataDirectory));
    }

    @Test
    public void testAuxMapFilenameConstruction() {
        String auxFileName = AuxMapManager.mfieldSetToFilename(Sets.newHashSet(MatchFieldEnum.FIRST_NAME));
        System.out.println(auxFileName);
        assert auxFileName.equals(testDataDirectory + "FIRST_NAME_.auxmap");
    }

    @Test
    public void testGetAuxMap() {
        Set<MatchFieldEnum> mfields = Sets.newHashSet(MatchFieldEnum.FIRST_NAME);

        assert !AuxMapManager.auxMapExists(mfields);
        AuxMap fnameAux = AuxMapManager.getAuxMap(db, mfields, false);
        assert AuxMapManager.auxMapExists(mfields);
    }

    @Test
    public void testAuxMapManualLoadAndSave() {

        int MAGIC_KEY = 7;

        Map<Long, HashCode> dummyData = new HashMap<Long, HashCode>();
        dummyData.put(new Long(MAGIC_KEY), null);
        Set<MatchFieldEnum> empty = new HashSet();
        AuxMap emptyAux = new AuxMap(empty, dummyData, null);

        assert !AuxMapManager.auxMapExists(empty);
        AuxMapManager.saveAuxMapToFile(emptyAux);
        assert AuxMapManager.auxMapExists(empty);

        AuxMap loadedMap = AuxMapManager.loadAuxMapFromFile(empty);
        assert loadedMap.attrs.equals(empty);
        assert loadedMap.idToHashMap.size() == 1;
        assert loadedMap.idToHashMap.get(MAGIC_KEY) == null;

        AuxMapManager.deleteAuxMap(empty);
        assert !AuxMapManager.auxMapExists(empty);
    }
}