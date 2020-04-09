package abstraction;

import com.google.common.hash.HashCode;
import utils.ConcurrentSet;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class AuxMap implements Serializable {

    Set<MatchFieldEnum> attrs;
    ConcurrentMap<Long, Set<HashCode>> idToHashMap;
    ConcurrentMap<HashCode, Set<Long>> hashToIdMap;

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("attrs: [");
        for(MatchFieldEnum mfe : attrs) {
            ret.append(mfe);
            ret.append(", ");
        }
        ret.append("]\n");
        ret.append("id_to_hash: {\n");
        for(Long l : idToHashMap.keySet()) {
            ret.append("\t");
            ret.append(l);
            ret.append(" : [");
            for(HashCode hc : idToHashMap.get(l)) {
                ret.append(hc.toString());
                ret.append(", ");
            }
            ret.append("]\n");
        }
        ret.append("}");
        return ret.toString();
    }

    public AuxMap(Set<MatchFieldEnum> attrs,
                  ConcurrentMap<Long, Set<HashCode>> idToHash, ConcurrentMap<HashCode, Set<Long>> hashToId) {
        this.attrs = attrs;
        this.idToHashMap = idToHash;
        this.hashToIdMap = hashToId;

        ensureThreadSafe();
    }

    public Map<HashCode, Set<Long>> getHashToIdMap() {
        return hashToIdMap;
    }
    public Map<Long, Set<HashCode>> getIdToHashMap() {
        return idToHashMap;
    }

    /**
     * Because Java jas no built-in concurrent set type, we must enforce that the sets are concurrent by manually
     * copying them into a thread-safe set.
     */
    public void ensureThreadSafe() {
        for (Long key : idToHashMap.keySet()) {
            Set<HashCode> concurrentIdToHashMapElement = ConcurrentSet.newSet();
            concurrentIdToHashMapElement.addAll(idToHashMap.get(key));
            idToHashMap.put(key, concurrentIdToHashMapElement);
        }
        for (HashCode key : hashToIdMap.keySet()) {
            Set<Long> concurrentHashToIdMapElement = ConcurrentSet.newSet();
            concurrentHashToIdMapElement.addAll(hashToIdMap.get(key));
            hashToIdMap.put(key, concurrentHashToIdMapElement);
        }
    }
}
