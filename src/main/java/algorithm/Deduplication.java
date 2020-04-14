package algorithm;

import abstraction.*;

import java.util.*;

import java.sql.SQLException;

/**
 * Utility functions for determining which records in a database are duplicates
 * of eachother, according to given criteria.
 */
public class Deduplication {

    /**
     * Takes config to match on, and returns a list of sets of groups of IDs
     * that match under the specified config.
     *
     * The config is a list of sets of MatchFieldEnums, such that records that
     * have the same values for all fields specified in a set are counted as
     * "matching". Each set of fields is evaluated separately, and the
     * resulting sets of sets of matching IDs are returned in a list, in the
     * same order as the input list of sets of fields that they match under.
     *
     * The matching sets are not merged--some IDs may be listed in multiple
     * sets, and so some "chains" of matches (A = B = C) may not be
     * consolidated into the same set. See getMatchingMerged() if you want this
     * behavior.
     *
     * @param db        the database (AuxLogic) to look in for records/ids
     * @param config    the configuration specifying what counts as a "match"
     * @return          the list of sets of matching groups
     * @throws SQLException
     * @see             Deduplication#getMatchingMerged(AuxLogic, List)
     */
    public static List<Set<Set<Long>>> getMatching(AuxLogic db, List<Set<MatchFieldEnum>> config) throws SQLException {
        //each set in all matches corresponds to a configuration
        List<Set<Set<Long>>> allMatches = new ArrayList<>();

        for(Set<MatchFieldEnum> fieldsToMatchOn : config) {
            AuxMap auxMap = AuxMapManager.getAuxMap(db, fieldsToMatchOn);
            Set<Set<Long>> matchesForCurrentConfig = new HashSet<>();
            for(Set<Long> idsWithMatchingHashes : auxMap.getHashToIds().values()) {
                while(idsWithMatchingHashes.size() > 1) {
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

    /**
     * Takes a config to match on, and returns sets of groups of IDs that match
     * under the specified config.
     *
     * Similar to getMatching(), but all groups with any intersection are
     * merged together, guaranteeing that in any returned set of IDs, all IDs
     * in that set match under some criteria given by the config, no ID is in
     * more than one group, and any ID that matches under any criteria with any
     * other ID will be in a group with that matching ID.
     *
     * @param db        the database (AuxLogic) to look in for records/ids
     * @param config    the configuration specifying what counts as a "match"
     * @return          the set of matching groups
     * @throws SQLException
     * @see             Deduplication#getMatching(AuxLogic, List)
     */
    public static Set<Set<Long>> getMatchingMerged(AuxLogic db, List<Set<MatchFieldEnum>> config) throws SQLException {
        return MergeUtils.merge(getMatching(db, config));
    }
}
