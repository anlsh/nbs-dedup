//package abstraction;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;
//
//public class MatchFieldUtils {
//    public static Set<String> getRequiredColumns(MatchFieldEnum mfield) {
//        return new HashSet<>(Arrays.asList(getRequiredColumnsArray(mfield)));
//    }
//    private static String[] getRequiredColumnsArray(MatchFieldEnum mfield) {
//        // TODO Fill this out with all subfields which can be matched on.
//        if (mfield == MatchFieldEnum.UID) {
//            return new String[]{Constants.COL_PERSON_UID};
//        }
//        else if (mfield == MatchFieldEnum.FIRST_NAME) {
//            return new String[]{Constants.COL_FIRST_NAME};
//        } else if (mfield == MatchFieldEnum.LAST_NAME) {
//            return new String[]{Constants.COL_LAST_NAME};
//        } else if (mfield == MatchFieldEnum.SSN) {
//            return new String[]{Constants.COL_SSN};
//        } else {
//            throw new RuntimeException("Attempted to get columns for unknown MatchField " + mfield);
//        }
//    }
//
//    // TODO: We probably need to get more granular than Object for hashing purposes
//    public static Object getFieldValue(ResultSet rs, MatchFieldEnum mfield) throws SQLException {
//        // TODO Fill this out with all subfields which can be matched on.
//        if (mfield == MatchFieldEnum.UID) {
//            return rs.getObject(Constants.COL_PERSON_UID);
//        }
//        else if (mfield == MatchFieldEnum.FIRST_NAME) {
//            return rs.getObject(Constants.COL_FIRST_NAME);
//        } else if (mfield == MatchFieldEnum.LAST_NAME) {
//            return rs.getObject(Constants.COL_LAST_NAME);
//        } else if (mfield == MatchFieldEnum.SSN) {
//            return rs.getObject(Constants.COL_SSN);
//        } else {
//            throw new RuntimeException("Attempted to get columns for unknown MatchField " + mfield);
//        }
//    }
//
//    public static Class getFieldType(MatchFieldEnum mfield) {
//        //TODO check these against actual types returned by rs.getObject on the relevant fields
//        if (mfield == MatchFieldEnum.UID) {
//            return Long.class;
//        } else if (mfield == MatchFieldEnum.FIRST_NAME) {
//            return String.class;
//        } else if (mfield == MatchFieldEnum.LAST_NAME) {
//            return String.class;
//        } else if (mfield == MatchFieldEnum.SSN) {
//            return String.class;
//        } else {
//            throw new RuntimeException("Attempted to get type for unknown MatchField " + mfield);
//        }
//    }
//}
