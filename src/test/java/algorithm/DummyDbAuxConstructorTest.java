package algorithm;

import abstraction.AuxMap;
import abstraction.MatchFieldEnum;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DummyDbAuxConstructorTest extends DummyDeduplicationTest {

    private boolean auxMapIsEmpty(AuxMap aux) {
        // For debug to see if its broken
        return aux.getAttrs().isEmpty() || aux.getHashToIds().isEmpty() || aux.getIdToHashes().isEmpty();
    }

    /**
     * This test ensures that parallellization does not affect the contents of an Aux Map
     */
    @Test
    public void testConstructAuxMapThreading() {
        Set<MatchFieldEnum> attrs = new HashSet<>();
        attrs.add(MatchFieldEnum.SSN);
        ArrayList<Set<MatchFieldEnum>> config = new ArrayList<>();
        config.add(attrs);
        AuxMap stAuxMap = al.constructAuxMap(attrs, 1);
        AuxMap mtAuxMap = al.constructAuxMap(attrs, 4);
        assert(stAuxMap.equals(mtAuxMap));
        assert(!auxMapIsEmpty(stAuxMap));
    }
}
