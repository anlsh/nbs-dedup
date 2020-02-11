package abstraction;

import java.io.*;
import java.util.*;

public class AuxMap implements Serializable {

    Set<MatchFieldEnum> attrs;
    Map<Long, Long> idToHashMap;
    Map<Long, Set<Long>> hashToIdMap;

    public AuxMap(Set<MatchFieldEnum> attrs, Map<Long, Long> idToHash, Map<Long, Set<Long>> hashToId) {
        this.attrs = attrs;
        this.idToHashMap = idToHash;
        this.hashToIdMap = hashToId;
    }
}
