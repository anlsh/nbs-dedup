package algorithm;

import abstraction.AuxMap;
import abstraction.MatchFieldEnum;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DummyAuxLogicTest extends DummyDeduplicationTest {

    private boolean auxMapIsEmpty(AuxMap aux) {
        // For debug to see if its broken
        return aux.getAttrs().isEmpty() || aux.getHashToIdMap().isEmpty() || aux.getIdToHashMap().isEmpty();
    }

    @Test
    public void testConstructAuxMapThreading() {
        Set<MatchFieldEnum> attrs = new HashSet<>();
        attrs.add(MatchFieldEnum.SSN);
        ArrayList<Set<MatchFieldEnum> > config = new ArrayList<>();
        config.add(attrs);
        AuxMap stAuxMap = al.constructAuxMap(attrs, 1);
        System.out.println("ST AUXMAP: " + stAuxMap);
        AuxMap mtAuxMap = al.constructAuxMap(attrs, 4);
        System.out.println("MT AUXMAP: " + mtAuxMap);
        assert(stAuxMap.equals(mtAuxMap));
        assert(!auxMapIsEmpty(stAuxMap));
    }
}
