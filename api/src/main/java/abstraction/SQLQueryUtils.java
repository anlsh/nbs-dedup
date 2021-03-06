package abstraction;

import Constants.InternalConstants;

import com.google.common.collect.Lists;

import java.util.*;

public class SQLQueryUtils {

    /**
     * Given the name of a table and a column within that table, return a fully-qualified usable as an identifier in an
     * SQL query.
     *
     * @param tableName     Name of a table in the SQL schema
     * @param columnName    The name of a column within the aforementioned table
     * @return
     */
    public static String getSQLQualifiedColName(String tableName, String columnName) {
        return tableName + "." + columnName;
    }

    /**
     * Given the name of a table and a column within that table, return an alias suitable for the deduplication API's
     * internal use. This function is necessary because the SQL query cannot alias to aliases containing a period,
     * perhaps because the syntax is simply disallowed or simply because of a name conflict in the tested
     * queries.
     *
     * The latter is perhaps more likely, but regardless this function permanently solves the problem
     *
     * @param tableName     Name of a table in the SQL schema
     * @param columnName    The name of a column within the aforementioned table
     * @return
     */
    public static String getAliasedColName(String tableName, String columnName) {
        return tableName + "__" + columnName;
    }

    /**
     * Given a set of MatchFieldEnums, generate a map from required database tables to the columns which are required
     * from each table.
     *
     * @param attrs     A set of MatchFieldEnums
     * @return          A map from database table names to dependent columns required for "attrs"
     */
    private static Map<String, Set<String>> getAttrSetDbDeps(Set<MatchFieldEnum> attrs) {
        Map<String, Set<String>> ret = new HashMap<>();

        for(MatchFieldEnum mfield : attrs) {
            Map<String, Set<String>> attrDeps = mfield.getDbDeps();
            for (String tableName : attrDeps.keySet()) {
                ret.putIfAbsent(tableName, new HashSet<>());
                ret.get(tableName).addAll(attrDeps.get(tableName));
            }
        }
        return ret;
    }

    /**
     * Generates an SQL query to retrieve the columns for any given set of match fields and (potentially) any specific
     * id.
     *
     * If uid is null, then returns a ResultSet containing the needed columns for every single entry in the
     * database. If it is non-null, then the ResultSet only contains the information concerning the given uid.
     *
     * Unfortunately Java lacks list comprehensions, so the implementation is rather long (though not complicated). The
     * function is probably best understood by example.
     *
     * For example, getSQLQueryForEntries({first_nm, ssn}, 123) returns
     *   "SELECT    Person_name.person_uid as Person_name__person_uid,
     *              Person_name.first_nm as Person_name__first_nm,
     *              Person.person_uid as Person__person_uid,
     *              Person.SSN as Person__SSN,
     *              Person.person_uid as Person__person_uid
     *    FROM Person_name, Person
     *    WHERE     Person.person_uid = 123
     *              and Person.person_uid = Person_name.person_uid"
     *
     * Calling with "null" in place of "123" would remove the WHERE clause specifying the value of 123 for
     * Person.person_uid.
     *
     * @param attrs     The set of MatchFields to deduplicate on
     * @param uid       The specific person ID to retrieve information for, or null if information for all IDs should be
     *                  retrieved
     * @return          A string representing the appropriate SQL query.
     */
    public static String getSQLQueryForEntries(Set<MatchFieldEnum> attrs, Long uid) {

        attrs = new HashSet<>(attrs);
        attrs.add(MatchFieldEnum.UID);

        Map<String, Set<String>> dbDeps = getAttrSetDbDeps(attrs);
        List<String> qualifiedTableColumns = new ArrayList<>();
        String queryString = "SELECT ";
        for(String tableName : dbDeps.keySet()) {
            List<String> currTableColumns = new ArrayList<>();

            // Always align based on the person_uid column
            currTableColumns.add(
                    SQLQueryUtils.getSQLQualifiedColName(tableName, InternalConstants.COL_PERSON_UID)
                            + " as " + SQLQueryUtils.getAliasedColName(tableName, InternalConstants.COL_PERSON_UID)
            );
            for (String reqCol : dbDeps.get(tableName)) {
                currTableColumns.add(
                        SQLQueryUtils.getSQLQualifiedColName(tableName, reqCol)
                                + " as " + SQLQueryUtils.getAliasedColName(tableName, reqCol)
                );
            }
            qualifiedTableColumns.add(String.join(", ", currTableColumns));
        }
        queryString += String.join(", ", qualifiedTableColumns);
        queryString += " from " + String.join(", ", Lists.newArrayList(dbDeps.keySet()));

        // If only fetching for a single id, add that constraint to the query
        List<String> where_clauses = new ArrayList<>();
        if (uid != null) {
            where_clauses.add(
                    SQLQueryUtils.getSQLQualifiedColName(
                            InternalConstants.PRIMARY_TABLE_NAME, InternalConstants.COL_PERSON_UID
                    )
                    + " = " + uid);
        }
        // Align the columns from each table by the person_uid column.
        if (dbDeps.keySet().size() > 1) {
            Iterator<String> iter = dbDeps.keySet().iterator();
            String primaryTableUID = SQLQueryUtils.getSQLQualifiedColName(
                    InternalConstants.PRIMARY_TABLE_NAME,
                    InternalConstants.COL_PERSON_UID
            );
            while (iter.hasNext()) {
                String currTableUID  = SQLQueryUtils.getSQLQualifiedColName(
                        iter.next(), InternalConstants.COL_PERSON_UID
                );
                if (!primaryTableUID.equals(currTableUID)) {
                    where_clauses.add(primaryTableUID + " = " + currTableUID);
                }
            }
        }
        if (where_clauses.size() > 0) {
            queryString += " where " + String.join(" and ", where_clauses);
        }

        return queryString;
    }
}
