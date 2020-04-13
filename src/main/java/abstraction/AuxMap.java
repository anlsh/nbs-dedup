package abstraction;

import com.google.common.hash.HashCode;
import utils.ConcurrentSetFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class AuxMap implements Serializable {

    private Set<MatchFieldEnum> attrs;
    private ConcurrentMap<Long, Set<HashCode>> idToHashMap;
    private ConcurrentMap<HashCode, Set<Long>> hashToIdMap;

    public AuxMap(Set<MatchFieldEnum> attrs,
                  ConcurrentMap<Long, Set<HashCode>> idToHash, ConcurrentMap<HashCode, Set<Long>> hashToId) {
        this.attrs = attrs;
        this.idToHashMap = idToHash;
        this.hashToIdMap = hashToId;

        ensureThreadSafe();
    }

    public Set<MatchFieldEnum> getAttrs() { return attrs; }
    public Map<HashCode, Set<Long>> getHashToIdMap() {
        return hashToIdMap;
    }
    public Map<Long, Set<HashCode>> getIdToHashMap() {
        return idToHashMap;
    }

    /**
     * Because Java jas no built-in concurrent set type, we enforce thread-safety of sets by manually creating
     * concurrent copies.
     */
    public void ensureThreadSafe() {
        for (Long key : idToHashMap.keySet()) {
            Set<HashCode> concurrentIdToHashMapElement = ConcurrentSetFactory.newSet();
            concurrentIdToHashMapElement.addAll(idToHashMap.get(key));
            idToHashMap.put(key, concurrentIdToHashMapElement);
        }
        for (HashCode key : hashToIdMap.keySet()) {
            Set<Long> concurrentHashToIdMapElement = ConcurrentSetFactory.newSet();
            concurrentHashToIdMapElement.addAll(hashToIdMap.get(key));
            hashToIdMap.put(key, concurrentHashToIdMapElement);
        }
    }

    @Override
    public int hashCode() {
        return attrs.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || (hashCode() != o.hashCode())) return false;
        else if(o instanceof AuxMap) {
            AuxMap other = (AuxMap) o;
            return attrs.equals(other.attrs) && hashToIdMap.equals(other.hashToIdMap) && idToHashMap.equals(other.idToHashMap);
        }
        return false;
    }

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
        ret.append("}\nhash_to_id: {\n");
        for(HashCode hc : hashToIdMap.keySet()) {
            ret.append("\t");
            ret.append(hc.toString());
            ret.append(" : [");
            for(Long l : hashToIdMap.get(hc)) {
                ret.append(l);
                ret.append(", ");
            }
            ret.append("]\n");
        }
        ret.append("}");
        return ret.toString();
    }
}
