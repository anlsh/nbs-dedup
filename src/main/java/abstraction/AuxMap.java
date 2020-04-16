package abstraction;

import com.google.common.hash.HashCode;
import utils.ConcurrentSetFactory;

import java.io.*;
import java.util.*;

/**
 * The fundamental data structure of the deduplication algorithm.
 *
 * An AuxMap for a given set of MatchFieldEnums consists of two maps. The first maps the patient_uid value of each
 * record to a combined hash of the respective set of MatchFieldEnums (See note 1). The second maps hashes to the
 * set of UIDs hashing to that value, and is used in detecting duplicates.
 *
 * Note 1: Any given record is included in the AuxMap only if all the values specified in "attrs" are known for that
 * record. This is a better solution than treating unknown values as special constants (null, for instance), as in that
 * case any two records with unknown SSNs would match in an AuxMap(SSN), which is undesirable behavior given how
 * AuxMap results are aggregated together in Configurations. This constraint is enforced when AuxMaps are constructed
 * in AuxLogic and modified via the hooks in AuxMapManager
 */
public class AuxMap implements Serializable {

    private Set<MatchFieldEnum> attrs;
    private Map<Long, Set<HashCode>> idToHashes;
    private Map<HashCode, Set<Long>> hashToIds;

    /**
     * Create an AuxMap object for a given set of attributes and maps. As this class is mostly a wrapper object, the
     * actual construction of the idToHashes and hashToIds maps is handled elsewhere
     * @param attrs         Set of MatchFields which this AuxMap describes
     * @param idToHashes    Map from patient_uid to hashes
     * @param hashToIds     Map from hash to patient_uids
     */
    public AuxMap(Set<MatchFieldEnum> attrs,
                  Map<Long, Set<HashCode>> idToHashes, Map<HashCode, Set<Long>> hashToIds) {
        this.attrs = attrs;
        this.idToHashes = idToHashes;
        this.hashToIds = hashToIds;
    }

    /**
     * Updates the AuxMap object with information that record corresponding to "uid" hashes to "hash"
     *
     * @param uid       A value for person_uid
     * @param hash      A hash code to be associated with uid
     */
    public void addPair(long uid, HashCode hash) {
        idToHashes.putIfAbsent(uid, ConcurrentSetFactory.newSet());
        idToHashes.get(uid).add(hash);

        hashToIds.putIfAbsent(hash, ConcurrentSetFactory.newSet());
        hashToIds.get(hash).add(uid);
    }

    /**
     * Removes the hashes associated with uid from the AuxMap
     *
     * @param uid       A value for person_uid
     */
    public void removeByID(long uid) {
        if (idToHashes.containsKey(uid)) {
            Set<HashCode> hashes = idToHashes.get(uid);
            idToHashes.remove(uid);

            for (HashCode hash : hashes) {
                Set<Long> idsWithThisHash = hashToIds.get(hash);
                idsWithThisHash.remove(uid);
                if (idsWithThisHash.isEmpty()) { hashToIds.remove(hash); }
            }
        }
    }

    public Set<MatchFieldEnum> getAttrs() { return attrs; }
    public Map<HashCode, Set<Long>> getHashToIds() { return hashToIds; }
    public Map<Long, Set<HashCode>> getIdToHashes() { return idToHashes; }

    @Override
    public int hashCode() {
        return attrs.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || (hashCode() != o.hashCode())) return false;
        else if (o instanceof AuxMap) {
            AuxMap other = (AuxMap) o;
            return attrs.equals(other.attrs) && hashToIds.equals(other.hashToIds) && idToHashes.equals(other.idToHashes);
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
        for(Long l : idToHashes.keySet()) {
            ret.append("\t");
            ret.append(l);
            ret.append(" : [");
            for(HashCode hc : idToHashes.get(l)) {
                ret.append(hc.toString());
                ret.append(", ");
            }
            ret.append("]\n");
        }
        ret.append("}\nhash_to_id: {\n");
        for(HashCode hc : hashToIds.keySet()) {
            ret.append("\t");
            ret.append(hc.toString());
            ret.append(" : [");
            for(Long l : hashToIds.get(hc)) {
                ret.append(l);
                ret.append(", ");
            }
            ret.append("]\n");
        }
        ret.append("}");
        return ret.toString();
    }
}
