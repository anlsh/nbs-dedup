package abstraction;

import exceptions.BadTableObjectException;
import utils.ResultType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * An enum where each value corresponds to a flag which can be matched on: for instance first name, or last name,
 * or SSN, or the last four digits of an SSN, or anything else.
 */
public enum MatchFieldEnum {
    // Match flags. Each match flag must implement a certain set of functions, which are declared at the end of this
    // class. Implementing the functions here unfortunately leads to a very long file, but gives a compile-time
    // guarantee that every flag implements everything that it needs to.
    UID {
        @Override public boolean isDeduplicableField() { return false; }
        @Override public String getHumanReadableName() { return "Unique ID"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_PERSON_UID}; }
        @Override public Class getFieldType() { return Long.class; }
//        @Override public boolean isUnknownValue(Object o) { return o == null; }
        @Override public String getTableName() {return "Person";}
    },
    FIRST_NAME {
        @Override public String getHumanReadableName() { return "First Name"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_FIRST_NAME}; }
        @Override public Class getFieldType() { return String.class; }
//        @Override public boolean isUnknownValue(Object o) { return o == null; }
        @Override public String getTableName() {return "Person";}
    },
    LAST_NAME {
        @Override public String getHumanReadableName() { return "Last Name"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_LAST_NAME}; }
        @Override public Class getFieldType() { return String.class; }
//        @Override public boolean isUnknownValue(Object o) { return o == null; }
        @Override public String getTableName() {return "Person";}
    },
    SSN {
        @Override public String getHumanReadableName() { return "Social Security Number"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_SSN}; }
        @Override public Class getFieldType() { return String.class; }
//        @Override public boolean isUnknownValue(Object o) { return o == null; }
        @Override public String getTableName() {return "Person";}
    },
    SSN_LAST_FOUR {
        @Override public MatchFieldEnum getParent() { return SSN; }
        @Override public String getHumanReadableName() { return "SSN (last four digits)"; }
        @Override public String[] getRequiredColumnsArray() { return SSN.getRequiredColumnsArray(); }
//        @Override public Set<Object> getFieldValues(ResultSet rs) throws SQLException, UnknownValueException {
//            Set<Object> lastFours = new HashSet<>();
//            for (Object ssn : SSN.getFieldValues(rs)) {
//                String ssnStr = (String) ssn;
//                lastFours.add(ssnStr == null ? null : ssnStr.substring(ssnStr.length() - 4, ssnStr.length()));
//            }
//            return lastFours;
//        }
        @Override public Class getFieldType() { return String.class; }
        @Override public String getTableName() {return "Person";}
    },

    OTHER_TABLE_NAME {
        @Override public MatchFieldEnum getParent() { return null; }
        @Override public String getHumanReadableName() { return "Name but from the person table"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{"first_nm"}; }
        @Override public Class getFieldType() { return String.class; }
        @Override public String getTableName() { return "Person_name"; }
    };

    /** Should return true for fields which it makes sense to deduplicate on (almost all of them) and false
     *  for others: at the moment the only field which it doesn't make sense to deduplicate on is UID
     * @return
     */
    public boolean isDeduplicableField() {
        return true;
    }

    /** Return null if the attribute in question is "top-level," (eg SSN), or the parent MatchFieldEnum if not
     *  (eg last four digits of an SSN
     * @return
     */
    public MatchFieldEnum getParent() {
        return null;
    };

    /** Returns the name to be displayed for this MatchFieldEnum in the user-interface
     * @return
     */
    public abstract String getHumanReadableName();

    /** Returns the name of the table on which this attribute depends. Each attribute can only depend on a single table
     * at the moment, as we see no reason to augment this functionality.
     * @return
     */
    public abstract String getTableName();

    /** Returns a list of columns which the attribute depends on *within its table*. So for example the FIRST_NM
     * flag which depends on the "first_nm" column from the "Person_name" table would return ["first_nm"]
     * @return
     */
    public abstract String[] getRequiredColumnsArray();

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
    public ResultType getFieldValues(ResultSet rs) throws SQLException, BadTableObjectException {
        if (getRequiredColumnsArray().length != 1) {
            throw new RuntimeException("Using default getFieldValue to retrieve information " +
                    "depending on multiple fields");
        }
        Object tableObj = rs.getObject(
                MatchFieldUtils.getAliasedColName(getTableName(), getRequiredColumnsArray()[0])
        );

        if (tableObj == null) {
            return new ResultType(null, true);
        }
        else if (Collection.class.isInstance(tableObj)) {
            for (Object o : (Collection) tableObj) {
                if (!this.getFieldType().isInstance(o)) {
                    throw new BadTableObjectException(o, this);
                }
            }
            return new ResultType(new HashSet<>((Collection) tableObj), false);
        }
        // Check to se if this is a single object of the specified type: if so, then promote to set & return
        else if (this.getFieldType().isInstance(tableObj)) {
            HashSet ret = new HashSet();
            ret.add(tableObj);
            return new ResultType(ret, false);
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
