package algorithm;

import abstraction.AuxMap;
import abstraction.MatchFieldEnum;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DummyAuxLogicTest extends DummyDeduplicationTest {

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
        assert(!stAuxMap.isEmpty());
        //TODO figure out why these are empty
    }
}
