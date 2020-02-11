package algorithm;

import abstraction.MatchFieldEnum;
import abstraction.NBS_DB;
import abstraction.AuxMap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dedup {

    private NBS_DB db;

    //track which tables currently loaded into memory
    private Map<String, List<Map<Long, Long>>> auxTables = new HashMap<>();

    public dedup(NBS_DB db) {
        this.db = db;


    }


    public void create_or_get_aux_db(NBS_DB db, List<Set<MatchFieldEnum>> field_subset) throws SQLException {
        create_or_get_aux_db(db, field_subset, true);
    }

    public void create_or_get_aux_db(NBS_DB db, List<Set<MatchFieldEnum>> field_subset, boolean forceReplace) throws SQLException {
        // TODO implement
        //check if table is in database
        List<List<Map<Long, Long>>> tables = new ArrayList<>();
        //tables is a list of pairs represented as a list of length 2
        //each pair is an id->field and field->id map
        for (Set<MatchFieldEnum> matchSet : field_subset) {

            if (forceReplace == false && auxTables.containsKey(AuxMap.calculateAttrStr(matchSet))){
                continue;
            }

            if (AuxMap.auxTableExists(matchSet)) {
                List pair = AuxMap.deserializeTables(matchSet);
                tables.add(pair);
                this.auxTables.put(AuxMap.calculateAttrStr(matchSet), pair);
            } else {
                List pair = db.constructAuxTable(matchSet);
                tables.add(pair);
                this.auxTables.put(AuxMap.calculateAttrStr(matchSet), pair);

            }
        }
    }




    //Doesn't actually work
    public void get_duplicates(NBS_DB db, List<Set<MatchFieldEnum>> config) throws SQLException {
        // Given a database and a list of sets of recordfields, return a list of duplicates
        // God this documentation SUCKS I'm really sorry
        //^no ur fukin not

        // Create the auxiliary databases if they don't already exist.
        create_or_get_aux_db(db, config);
        /*
        List<Set<DatabaseRecord>> dup_list = new ArrayList<>();

        int totalRecords = 0;
        // Structure loop so that we only load each auxiliary database into memory once...
        for (Set<RecordFields> field_subset : config) {
            AuxDatabase aux_db = create_or_get_aux_db(db, field_subset);

           for (DatabaseRecord r : db.get_id_list()) {
                long aux_hash = aux_db.get_hash_for_id(r);
                for (DatabaseRecord dup_record : aux_db.ids_mapping_to_hash(aux_hash)) {
                    dup_list.add(Set.of(r, dup_record));
                }
            }
        }

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
}