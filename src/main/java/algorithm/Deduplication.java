package algorithm;

import abstraction.*;

import java.util.*;

import java.sql.SQLException;

public class Deduplication {


    // Gets duplicates given a list of various configurations
    public static List<Set<Set<Long>>> getMatching(AuxLogic db, List<Set<MatchFieldEnum>> config) throws SQLException {
        //TODO fix this, it returns a random subset of what's correct.
        //each set in all matches corresponds to a configuration
        List<Set<Set<Long>>> allMatches = new ArrayList<>();

        for(Set<MatchFieldEnum> fieldsToMatchOn : config) {
            AuxMap auxMap = AuxMapManager.getAuxMap(db, fieldsToMatchOn);
            //TODO remove debug printlns
            System.out.println("Auxmap in getMatching: ");
            System.out.println(auxMap);
            Set<Set<Long>> matchesForCurrentConfig = new HashSet<>();
            for(Set<Long> idsWithMatchingHashes : auxMap.getHashToIdMap().values()) {
                while(idsWithMatchingHashes.size() > 1) {
                    //TODO
                    //Hashes match, check for actual matches within set using fieldsToMatchOn and read db
                    //For actual matches, add to a new set, remove from idsWithMatchingHashes, and continue
                    //The while loop is to take care of the pathological case where we have two different sets
                    //of records with matching hashes but different actual values (but still more than one record
                    //for each set of matching fields)
                    long primary_id = idsWithMatchingHashes.iterator().next();
                    Map<MatchFieldEnum, Object> primary_row = db.getFieldsById(primary_id, fieldsToMatchOn);
                    Set<Long> group = new HashSet<>();
                    for(Long id : idsWithMatchingHashes) {
                        if(primary_row.equals(db.getFieldsById(id, fieldsToMatchOn))) group.add(id);
                    }
                    idsWithMatchingHashes.removeAll(group);
                    matchesForCurrentConfig.add(group);
                }
            }
            allMatches.add(matchesForCurrentConfig);
        }
        return allMatches;
    }

    public static Set<Set<Long>> getMatchingMerged(AuxLogic db, List<Set<MatchFieldEnum>> config) throws SQLException {
        return MergeUtils.merge(getMatching(db, config));
    }
}
