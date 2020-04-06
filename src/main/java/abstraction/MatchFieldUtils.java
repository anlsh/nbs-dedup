package abstraction;

import java.util.*;

public class MatchFieldUtils {


    private static List<MatchFieldEnum> sortedMfields = null;

    /** For utility purposes, it's often useful throughout the codebase to have MatchFieldEnum's values in some
     *  arbitrary but consistent order. We use the sorted order, and store it in a variable to avoid having to
     *  unnecessarily re-sort
     * @return
     */
    public static List<MatchFieldEnum> getSortedMfields() {
        if (sortedMfields == null) {
            List<MatchFieldEnum> thingy = Arrays.asList(MatchFieldEnum.values());
            Collections.sort(thingy);
            sortedMfields = thingy;
        }
        return sortedMfields;
    }

    /**
     * Given the name of a table and a column within that table, return a fully-qualified name suitable for use in
     * an SQL query
     * @param tableName Name of a table in the SQL schema
     * @param columnName The name of a column within the aforementioned table
     * @return
     */
    public static String getSQLQualifiedColName(String tableName, String columnName) {
        return tableName + "." + columnName;
    }

    /**
     * Given the name of a table and a column within that table, return an alias suitable for the deduplication API's
     * internal use. This function is necessary because the SQL query cannot alias to aliases containing a period,
     * perhaps because the syntax is simply disallowed or simply because of a name conflict in the specifically-tested
     * queries (more likely).
     * @param tableName Name of a table in the SQL schema
     * @param columnName The name of a column within the aforementioned table
     * @return
     */
    public static String getAliasedColName(String tableName, String columnName) {
        return tableName + "__" + columnName;
    }

    /** Given a set of matchfieldenums, generate a map from required tables in the database to attributes depending
     * on them
     * @param attrs
     * @return
     */
    public static Map<String, Set<MatchFieldEnum>> getTableNameMap(Set<MatchFieldEnum> attrs) {
        Map<String, Set<MatchFieldEnum>> ret = new HashMap<>();
        for(MatchFieldEnum e : attrs) {
            Set<MatchFieldEnum> entry = ret.getOrDefault(e.getTableName(), new HashSet<>());
            entry.add(e);
            ret.put(e.getTableName(), entry);
        }
        return ret;
    }
}
