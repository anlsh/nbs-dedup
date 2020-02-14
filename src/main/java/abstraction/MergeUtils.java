package abstraction;

import java.util.*;

public class MergeUtils {
    public static Set<Set<Long>> merge(List<Set<Set<Long>>> unionOfGroups) {
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

        Set<Set<Long>> toRemove = new HashSet<>();
        Set<Set<Long>> mergedSets = new HashSet<>();
        Set<Set<Long>> returnSets = new HashSet<>();
        for(Set<Set<Long>> setOfGroups: idsToSetsOfGroups.values()) {
            if(setOfGroups.size() > 1) {
                Set<Long> mergedGroup = new HashSet<>();
                for(Set<Long> group : setOfGroups) {
                    mergedGroup.addAll(group);
                    toRemove.add(group);
                }
                mergedSets.add(mergedGroup);
            } else {
                for(Set<Long> group : setOfGroups) returnSets.add(group);
            }
        }
        returnSets.removeAll(toRemove);
        returnSets.addAll(mergedSets);
        return returnSets;
    }
}
