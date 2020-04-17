package abstraction;

import Constants.InternalConstants;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import utils.BadTableObjectException;
import utils.ResultType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 * An enum where each value corresponds to a flag which can be matched on: for instance first name, or last name,
 * or SSN, or the last four digits of an SSN, or anything else.
 */
public enum MatchFieldEnum {
    // Match flags. Each match flag must implement a certain set of functions, which are declared at the end of this
    // class. Implementing the functions here unfortunately leads to a very long file, but gives a compile-time
    // guarantee that every flag implements everything that it needs to.
    UID {
        public boolean isDeduplicableField() { return false; }
        public String getHumanReadableName() { return "Unique ID"; }
        public Map<String, Set<String>> getDbDeps() {
            return ImmutableMap.<String, Set<String>>builder()
                    .put("Person", Sets.newHashSet("person_uid"))
                    .build();
        }
        public Class getFieldType() { return Long.class; }
    },
    FIRST_NAME {
        public String getHumanReadableName() { return "First Name"; }
        public Class getFieldType() { return String.class; }
        public Map<String, Set<String>> getDbDeps() {
            return ImmutableMap.<String, Set<String>>builder()
                    .put("Person_name", Sets.newHashSet(InternalConstants.COL_FIRST_NAME))
                    //.put("Person", Sets.newHashSet(InternalConstants.COL_FIRST_NAME))
                    .build();
        }
    },
    LAST_NAME {
        public String getHumanReadableName() { return "Last Name"; }
        public Class getFieldType() { return String.class; }
        public Map<String, Set<String>> getDbDeps() {
            return ImmutableMap.<String, Set<String>>builder()
                    .put("Person_name", Sets.newHashSet(InternalConstants.COL_LAST_NAME))
                    //.put("Person", Sets.newHashSet(InternalConstants.COL_LAST_NAME))
                    .build();
        }
    },
    SSN {
        public String getHumanReadableName() { return "Social Security Number"; }
        public Class getFieldType() { return String.class; }
        public Map<String, Set<String>> getDbDeps() {
            return ImmutableMap.<String, Set<String>>builder()
                    .put("Person", Sets.newHashSet(InternalConstants.COL_SSN))
                    .build();
        }
    },
    SSN_LAST_FOUR {
        public MatchFieldEnum getParent() { return SSN; }
        public String getHumanReadableName() { return "SSN (last four digits)"; }
        public ResultType getFieldValue(ResultSet rs) throws SQLException {
            ResultType ssn = SSN.getFieldValue(rs);
            if (ssn.isUnknown()) { return new ResultType(null, true); }
            else {
                String ssnStr = ((String) ssn.getValue());
                return new ResultType(ssnStr.substring(ssnStr.length() - 4), false);
            }
        }
        public Class getFieldType() { return String.class; }
        public Map<String, Set<String>> getDbDeps() {
            return SSN.getDbDeps();
        }
    },
    RACE {
        public String getHumanReadableName() { return "Race"; }
        public Class getFieldType() { return String.class; }
        public Map<String, Set<String>> getDbDeps() {
            return ImmutableMap.<String, Set<String>>builder()
                    .put("Person_race", Sets.newHashSet("race_cd"))
                    .build();
        }
    };

    /** Should return true for fields which it makes sense to deduplicate on (almost all of them) and false
     *  for others: at the moment the only field which it doesn't make sense to deduplicate on is UID
     *
     * @return  A boolean
     */
    public boolean isDeduplicableField() {
        return true;
    }

    /** Return null if the attribute in question is "top-level," (eg SSN), or the parent MatchFieldEnum if not
     *  (eg last four digits of an SSN
     *
     * @return  A MatchFieldEnum or null
     */
    public MatchFieldEnum getParent() {
        return null;
    };

    /** Returns the name to be displayed for this MatchFieldEnum in the user-interface
     *
     * @return  A string to be displayed in the UI
     */
    public abstract String getHumanReadableName();

    /**
     * An attribute can depend on multiple columns in a table, or even columns across different tables. This function
     * returns a map where the keys are the tables depends on, and the values are requested sets of column names in
     * each table.
     *
     * @return  The dependent table name
     */
    // Curses on Java, what would be simple to encode in Python involves some pretty tedious code in this language.
    public abstract Map<String, Set<String>> getDbDeps();

    /** Given a ResultSet consisting of several columns, perform whatever logic is necessary to extract the attribute's
     * value.
     *
     * To account for attributes which can have multiple values, this function must always return a list consisting
     * of all possible values for the attribute- even if the attribute happens to be singly-valued for the given row
     * in the ResultSet.
     *
     * As attributes which essentially act as pass-throughs for a single column are very common, the default
     * implementation performs this operation.
     * @param rs
     * @return
     * @throws SQLException
     */
    public ResultType getFieldValue(ResultSet rs) throws SQLException, BadTableObjectException {

        Map<String, Set<String>> deps = getDbDeps();

        String aTable = (String) deps.keySet().toArray()[0];
        String aColumn = (String) deps.get(aTable).toArray()[0];

        if (deps.keySet().size() != 1 || deps.get(aTable).size() != 1) {
            throw new RuntimeException("Using default getFieldValue to retrieve information " +
                    "depending on multiple tables or columns");
        }
        Object tableObj = rs.getObject(
                SQLQueryUtils.getAliasedColName(aTable, aColumn)
        );

        if (tableObj == null) {
            return new ResultType(null, true);
        }
        else if (this.getFieldType().isInstance(tableObj)) {
            return new ResultType(tableObj, false);
        } else {
            throw new BadTableObjectException(tableObj, this);
        }
    };

    /** Returns the underlying type of the attribute: should be suitable as a cast target for the corresponding
     * value of getFieldValue()
     *
     * Possibly multiple-valued attributes should be treated the same as single-valued ones. For instance, the return
     * value for the FIRST_NAME attribute is String, even though it should be more like List<String> since people can
     * have multiple names
     * @return
     */
    public abstract Class getFieldType();
}
