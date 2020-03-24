package abstraction;

public class MatchFieldUtils {

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
}
