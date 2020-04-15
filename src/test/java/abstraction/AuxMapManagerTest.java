package abstraction;

import algorithm.DummyDeduplicationTest;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AuxMapManagerTest extends DummyDeduplicationTest {

    @Test
    public void testAuxMapFilenameConstruction() {
        String auxFnameFirst = AuxMapManager.mfieldSetToFilename(Sets.newHashSet(MatchFieldEnum.FIRST_NAME));
        String auxFnameLast = AuxMapManager.mfieldSetToFilename(Sets.newHashSet(MatchFieldEnum.LAST_NAME));

        // Make sure the construction is deterministic
        assert auxFnameFirst.equals(AuxMapManager.mfieldSetToFilename(Sets.newHashSet(MatchFieldEnum.FIRST_NAME)));
        // And injective
        assert !auxFnameFirst.equals(auxFnameLast); //TODO there is actually a small chance of collision here!
    }

    @Test
    public void testGetAuxMap() {
        Set<MatchFieldEnum> mfields = Sets.newHashSet(MatchFieldEnum.FIRST_NAME);

        assert !AuxMapManager.auxMapExists(mfields);
        AuxMapManager.getAuxMap(al, mfields, false);
        assert AuxMapManager.auxMapExists(mfields);
        AuxMapManager.getAuxMap(al, mfields, true);
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
        assert loadedMap.getAttrs().equals(empty);
        assert loadedMap.getIdToHashes().size() == 1;
        assert loadedMap.getHashToIds().isEmpty();

        AuxMapManager.hookManagerDeleteMap(empty);
        assert !AuxMapManager.auxMapExists(empty);
    }
}
