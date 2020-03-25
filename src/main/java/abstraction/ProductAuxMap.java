package abstraction;

import com.google.common.hash.HashCode;

import java.util.Map;
import java.util.Set;

public class ProductAuxMap {
    Set<MatchFieldEnum> attrs;
    Map<Long, Set<HashCode>> idToHashesMap;
    Map<HashCode, Set<Long>> hashToIdsMap;

    public ProductAuxMap(Set<MatchFieldEnum> attrs, Map<Long, Set<HashCode>> idToHashes, Map<HashCode, Set<Long>> hashToIds) {
        this.attrs = attrs;
        this.idToHashesMap = idToHashes;
        this.hashToIdsMap = hashToIds;
    }

    public Map<HashCode, Set<Long>> getHashToIdsMap() {
        return hashToIdsMap;
    }

    public Map<Long, Set<HashCode>> getIdToHashesMap() {
        return idToHashesMap;
    }
}
