package abstraction;

import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import static org.junit.Assert.*;

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
}