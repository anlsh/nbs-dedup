package abstraction;

import java.util.List;
import java.util.Set;

public class AuxDatabase {
    // This is basically going to be some sort of bi-directional hash map.
    public AuxDatabase(String load_path) {
        // TODO Implement...
    }

    public List<DatabaseRecord> ids_mapping_to_hash(long hashcode) {
        // TODO Implement. Returns a list of record ids which map to the given hash code
        return null;
    }

    public Long get_hash_for_id(DatabaseRecord id) {
        // TODO Implement.
        return null;
    }

//    public static AuxDatabase create_aux_table(NBS_DB db, Set<RecordFields> fieldSet) {
//
//    }
}
