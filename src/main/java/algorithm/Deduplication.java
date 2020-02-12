package algorithm;

import abstraction.AuxMapManager;
import abstraction.MatchFieldEnum;
import abstraction.NBS_DB;
import abstraction.AuxMap;

import java.util.*;

import java.sql.SQLException;

public class Deduplication {

//    private NBS_DB db;
//
//    //track which tables currently loaded into memory
//    private Map<String, List<Map<Long, Long>>> auxTables = new HashMap<>();
//
//    public dedup(NBS_DB db) {
//        this.db = db;
//    }
//
//    public void hookAddRecord(NBS_DB db, Set<MatchFieldEnum> attr, Long id, Long hash) throws SQLException {
//
//        create_or_get_aux_db(db, List.of(attr));
//        List<Map<Long, Long>> auxPair = this.auxTables.getOrDefault(attr, null);
//
//        if (auxPair == null){
//            return;
//        }
//
//        auxPair.get(0).put(id, hash);
//        auxPair.get(1).put(hash, id);
//
//        String attrStr = AuxMap.calculateAttrStr(attr);
//        AuxMap.serializeTable(attr, auxPair);
//    }
//
//    public void hookRemoveRecord(NBS_DB db, Set<MatchFieldEnum> attr, Long id, Long hash) throws SQLException {
//        create_or_get_aux_db(db, List.of(attr));
//        List<Map<Long, Long>> auxPair = this.auxTables.getOrDefault(attr, null);
//
//        if (auxPair == null) {
//            return;
//        }
//
//        auxPair.get(0).remove(id);
//        auxPair.get(1).remove(hash);
//
//        AuxMap.serializeTable(attr, auxPair);
//
//    }


    public Set<Set<Long>> getMatching(NBS_DB db, List<Set<MatchFieldEnum>> config) {
        List<AuxMap> auxMaps = new ArrayList<>(config.size());
        for(int i = 0; i < config.size(); i++) {
            auxMaps.set(i, AuxMapManager.getAuxMap(db, config.get(i)));
        }
        //TODO come back to this
    }

//    //Doesn't actually work
//    public void get_duplicates(NBS_DB db, List<Set<MatchFieldEnum>> config) throws SQLException {
//        // Given a database and a list of sets of recordfields, return a list of duplicates
//        // God this documentation SUCKS I'm really sorry
//        //^no ur fukin not
//
//        // Create the auxiliary databases if they don't already exist.
//        create_or_get_aux_db(db, config);
//
//        List<List<Long>> dup_list = new ArrayList<>();
//
//        int totalRecords = 0;
//
//        for (Set<MatchFieldEnum> field_subset : config) {
//            List<Map<Long, Long>> auxTablePair = this.auxTables.get(AuxMap.calculateAttrStr(field_subset));
//            for (Map.Entry<Long, Long> entry : auxTablePair.get(0).entrySet()) {
//                //for each id, get its attr hash and check if attr2id points back correctly
//                long attrHash = entry.getValue();
//
//                //if we find a pair add it here
//                if (auxTablePair.get(1).get(attrHash) != entry.getKey()) {
//                    dup_list.add(Arrays.asList(entry.getKey(), auxTablePair.get(1).get(attrHash)));
//                }
//            }
//        }

/*
       List<Set<DatabaseRecord>> finalDups = new ArrayList<>();
        // TODO Merge sequences of duplicates like [r1, r2], [r2, r3], ... into [r1, r2, r3], ...
        while(dup_list.size() > 0){
            Set<DatabaseRecord> firstSet = dup_list.get(0);
            int i = 0;
            while (i < dup_list.size()){
                Set<DatabaseRecord> setToCompare = dup_list.get(i);
                Set<DatabaseRecord> intersection = new HashSet<DatabaseRecord>(setToCompare);

               //calc intersection
                intersection.retainAll(firstSet);
                if (intersection.size() != 0){  //if there is overlap merge
                    firstSet.addAll(setToCompare);
                    dup_list.remove(i);

                }
                else {
                    i++;
                }
            }

            finalDups.add(firstSet);
            dup_list.remove(0);

        }

        return finalDups;*/
}
