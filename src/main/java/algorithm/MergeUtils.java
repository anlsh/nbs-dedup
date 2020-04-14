package algorithm;

import java.util.*;

/**
 * A set of utilities to merge groups, currently with one public member:
 * @see MergeUtils#merge(List)
 */
public class MergeUtils {
    /**
     * Merges a list of sets of groups of IDs so that all grouping is
     * transitive.
     *
     * If any two elements A and B are in a group together, and B and C are in
     * a group together, then A, B, and C will be in a group together in the
     * resulting set of groups.
     *
     * @param unionOfGroups a list of sets of groups to be merged
     * @return              the merged set of groups
     */
    public static Set<Set<Long>> merge(List<Set<Set<Long>>> unionOfGroups) {

        //Map from each id to the set of all groupings it is explicitly put into
        Map<Long, Set<Set<Long>>> idsToSetsOfGroups = new HashMap<>();
        for(Set<Set<Long>> groups : unionOfGroups) {
            for(Set<Long> group : groups) {
                for(Long id : group) {
                    Set<Set<Long>> setOfGroups = idsToSetsOfGroups.getOrDefault(id, new HashSet<>());
                    setOfGroups.add(group);
                    idsToSetsOfGroups.put(id, setOfGroups);
                }
            }
        }

        //TODO speed this up with the following:
        //I think this could be modelled as a Connected Components graph problem and solved in linear time, but w/e

        //Merge all groupings until there is no more merging to do.
        boolean changed;
        do {
            changed = false;
            for (Set<Set<Long>> setOfGroups : idsToSetsOfGroups.values()) {
                if (setOfGroups.size() > 1) {
                    Set<Long> merged = merge(setOfGroups);
                    setOfGroups.clear();
                    for (Long id : merged) {
                        idsToSetsOfGroups.get(id).add(merged);
                    }
                    changed = true;
                }
            }
        } while(changed);

        //Now each id maps to a set containing a single set containing all other ids it is equivalent to.

        Set<Set<Long>> ret = new HashSet<>();
        for(Set<Set<Long>> setOfSingleGroup : idsToSetsOfGroups.values()) {
            for(Set<Long> s : setOfSingleGroup) ret.add(s);
        }

        return ret;
    }

    /**
     * Takes a set of groups and puts all elements in the same group
     * @param setOfGroups   the set of groups to merge
     * @return              the merged group
     */
    private static Set<Long> merge(Set<Set<Long>> setOfGroups) {
        Set<Long> ret = new HashSet<>();
        for(Set<Long> s : setOfGroups) ret.addAll(s);
        return ret;
    }
}
