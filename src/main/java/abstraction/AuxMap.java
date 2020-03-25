package abstraction;

import com.google.common.hash.HashCode;

import java.io.*;
import java.util.*;

public class AuxMap implements Serializable {

    Set<MatchFieldEnum> attrs;
    Map<Long, Set<HashCode>> idToHashMap;
    Map<HashCode, Set<Long>> hashToIdMap;

    public AuxMap(Set<MatchFieldEnum> attrs, Map<Long, Set<HashCode>> idToHash, Map<HashCode, Set<Long>> hashToId) {
        this.attrs = attrs;
        this.idToHashMap = idToHash;
        this.hashToIdMap = hashToId;
    }

    public Map<HashCode, Set<Long>> getHashToIdMap() {
        return hashToIdMap;
    }

    public Map<Long, Set<HashCode>> getIdToHashMap() {
        return idToHashMap;
    }
}
