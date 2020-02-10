package algorithm;

import abstraction.AuxDatabase;
import abstraction.NBS_DB;
import abstraction.DatabaseRecord;
import abstraction.RecordFields;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dedup {

    public dedup (String host, String username, String password, String auxDatabasePath){
        //create connections to sql database
        try {
            Connection con = DriverManager.getConnection(host, username, password);
        }
        catch (SQLException err){
            System.out.println((err.getMessage()));
        }

        AuxDatabase aux = new AuxDatabase(auxDatabasePath);
    }

//    public static AuxDatabase create_or_get_aux_db(NBS_DB db, Set<RecordFields> field_subset){
//        // TODO implement
//        //check if table is in database
//
//        //otherwise
//        newAux = AuxDatabase.create_aux_table(db, field_subset);
//    }
//
//    public static List<Set<DatabaseRecord>> get_duplicates(NBS_DB db, List<Set<RecordFields>> config) {
//        // Given a database and a list of sets of recordfields, return a list of duplicates
//        // God this documentation SUCKS I'm really sorry
//
//        // Create the auxiliary databases if they don't already exist.
//        for (Set<RecordFields> field_subset : config) {
//            create_or_get_aux_db(db, field_subset);
//        }
//
//        List<Set<DatabaseRecord>> dup_list = new ArrayList<>();
//
//        int totalRecords = 0;
//        // Structure loop so that we only load each auxiliary database into memory once...
//        for (Set<RecordFields> field_subset : config) {
//            AuxDatabase aux_db = create_or_get_aux_db(db, field_subset);
//
//            for (DatabaseRecord r : db.get_id_list()) {
//                long aux_hash = aux_db.get_hash_for_id(r);
//                for (DatabaseRecord dup_record : aux_db.ids_mapping_to_hash(aux_hash)) {
//                    dup_list.add(Set.of(r, dup_record));
//                }
//            }
//        }
//
//        List<Set<DatabaseRecord>> finalDups = new ArrayList<>();
//        // TODO Merge sequences of duplicates like [r1, r2], [r2, r3], ... into [r1, r2, r3], ...
//        while(dup_list.size() > 0){
//            Set<DatabaseRecord> firstSet = dup_list.get(0);
//            int i = 0;
//            while (i < dup_list.size()){
//                Set<DatabaseRecord> setToCompare = dup_list.get(i);
//                Set<DatabaseRecord> intersection = new HashSet<DatabaseRecord>(setToCompare);
//
//                //calc intersection
//                intersection.retainAll(firstSet);
//                if (intersection.size() != 0){  //if there is overlap merge
//                    firstSet.addAll(setToCompare);
//                    dup_list.remove(i);
//
//                }
//                else {
//                    i++;
//                }
//            }
//
//            finalDups.add(firstSet);
//            dup_list.remove(0);
//
//        }
//
//        return finalDups;
//    }
}
