package abstraction;

import java.util.*;

public class MergeUtils {
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

    private static Set<Long> merge(Set<Set<Long>> setOfGroups) {
        Set<Long> ret = new HashSet<>();
        for(Set<Long> s : setOfGroups) ret.addAll(s);
        return ret;
    }
}
