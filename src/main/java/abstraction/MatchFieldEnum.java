package abstraction;

import exceptions.BadTableObjectException;
import utils.ResultType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

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
        @Override public String getTableName() {return "Person";}
        @Override public Object getFromString(String s) {return Long.parseLong(s);}
    },
    FIRST_NAME {
        @Override public String getHumanReadableName() { return "First Name"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_FIRST_NAME}; }
        @Override public Class getFieldType() { return String.class; }
        @Override public String getTableName() {return "Person_name";}
        @Override public Object getFromString(String s) {return s;}
    },
    LAST_NAME {
        @Override public String getHumanReadableName() { return "Last Name"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_LAST_NAME}; }
        @Override public Class getFieldType() { return String.class; }
        @Override public String getTableName() {return "Person_name";}
        @Override public Object getFromString(String s) {return s;}
    },
    SSN {
        @Override public String getHumanReadableName() { return "Social Security Number"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_SSN}; }
        @Override public Class getFieldType() { return String.class; }
        @Override public String getTableName() {return "Person";}
        @Override public Object getFromString(String s) {return s;}
    },
    SSN_LAST_FOUR {
        @Override public MatchFieldEnum getParent() { return SSN; }
        @Override public String getHumanReadableName() { return "SSN (last four digits)"; }
        @Override public String[] getRequiredColumnsArray() { return SSN.getRequiredColumnsArray(); }
        @Override public ResultType getFieldValue(ResultSet rs) throws SQLException {
            ResultType ssn = SSN.getFieldValue(rs);
            if (ssn.isUnknown()) { return new ResultType(null, true); }
            else {
                String ssnStr = ((String) ssn.getValue());
                return new ResultType(ssnStr.substring(ssnStr.length() - 4), false);
            }
        }
        @Override public Class getFieldType() { return String.class; }
        @Override public String getTableName() {return "Person";}
        @Override public Object getFromString(String s) {return s;}
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

    /**
     * Returns an object of the correct type from a string representing a value of this field
     * @param s the string
     * @return
     */
    public abstract Object getFromString(String s);

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
        if (getRequiredColumnsArray().length != 1) {
            throw new RuntimeException("Using default getFieldValue to retrieve information " +
                    "depending on multiple fields");
        }
        Object tableObj = rs.getObject(
                SQLQueryUtils.getAliasedColName(getTableName(), getRequiredColumnsArray()[0])
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

    public ResultType getFieldValue(String[] cols, String[] vals) {
        if(getRequiredColumnsArray().length != 1) {
            throw new RuntimeException("Using default getFieldValue to retrieve information " +
                    "depending on multiple fields");
        }
        String colname = SQLQueryUtils.getAliasedColName(getTableName(), getRequiredColumnsArray()[0]);
        int index = Arrays.asList(cols).indexOf(colname);
        if(index < 0) return new ResultType(null, true);
        return new ResultType(getFromString(vals[index]), false);
    }

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
