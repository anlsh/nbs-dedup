package abstraction;

import algorithm.DummyDeduplicationTest;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import org.junit.Test;

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
        // And injective (there's a very, *very* small chance of collision here: if this test ever fails then
        // you're having a very unlucky day :D
        assert !auxFnameFirst.equals(auxFnameLast);
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

        AuxMap loadedMap = AuxMapManager.loadAuxMapFromAttrs(empty);
        assert loadedMap.getAttrs().equals(empty);
        assert loadedMap.getIdToHashes().size() == 1;
        assert loadedMap.getHashToIds().isEmpty();

        AuxMapManager.removeFromAuxManager(empty);
        assert !AuxMapManager.auxMapExists(empty);
    }

    @Test
    public void testAuxMapManagerGetAuxMap() throws SQLException {
        Set<MatchFieldEnum> attrs = Sets.newHashSet(MatchFieldEnum.FIRST_NAME);
        AuxMap auxMap = AuxMapManager.getAuxMap(al, attrs);
        dummy_conn.insertRow("Person", personColumns, "555, '800-98-8230'");
        dummy_conn.insertRow("Person_name", personNameColumns, "555, 'Zalgo', 'Tron'");
        AuxMap auxMap2 = AuxMapManager.getAuxMap(al, attrs);
        assert(auxMap.equals(auxMap2));
        AuxMapManager.hookAddRecord(al, 555);
        AuxMap auxMap3 = AuxMapManager.getAuxMap(al, attrs);
        assert(!auxMap.equals(auxMap3));
    }
}
