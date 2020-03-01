package abstraction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum MatchFieldEnum {
    UID {
        @Override public MatchFieldEnum getParent() { return null; }
        @Override public String getHumanReadableName() { return "Unique ID"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_PERSON_UID}; }
        @Override public Object getFieldValue(ResultSet rs) throws SQLException { return rs.getObject(Constants.COL_PERSON_UID); }
        @Override public Class getFieldType() { return Long.class; }
    },
    FIRST_NAME {
        @Override public MatchFieldEnum getParent() { return null; }
        @Override public String getHumanReadableName() { return "First Name"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_FIRST_NAME}; }
        @Override public Object getFieldValue(ResultSet rs) throws SQLException { return rs.getObject(Constants.COL_FIRST_NAME); }
        @Override public Class getFieldType() { return String.class; }
    },
    LAST_NAME {
        @Override public MatchFieldEnum getParent() { return null; }
        @Override public String getHumanReadableName() { return "Last Name"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_LAST_NAME}; }
        @Override public Object getFieldValue(ResultSet rs) throws SQLException { return rs.getObject(Constants.COL_LAST_NAME); }
        @Override public Class getFieldType() { return String.class; }
    },
    SSN {
        @Override public MatchFieldEnum getParent() { return null; }
        @Override public String getHumanReadableName() { return "Social Security Number"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_SSN}; }
        @Override public Object getFieldValue(ResultSet rs) throws SQLException { return rs.getObject(Constants.COL_SSN); }
        @Override public Class getFieldType() { return String.class; }
    },
    SSN_LAST_FOUR {
        @Override public MatchFieldEnum getParent() { return SSN; }
        @Override public String getHumanReadableName() { return "SSN (last four digits)"; }
        @Override public String[] getRequiredColumnsArray() { return SSN.getRequiredColumnsArray(); }
        @Override public Object getFieldValue(ResultSet rs) throws SQLException {
            // TODO The manual cast to String rather than SSN.getFieldType is a code smell
            String ssn = (String) SSN.getFieldValue(rs);
            // TODO This doesn't actually give SSN's last four digits
            return ssn == null ? null : ssn.substring(0, 1);
        }
        @Override public Class getFieldType() { return String.class; }
    };

    public abstract MatchFieldEnum getParent();
    public abstract String getHumanReadableName();
    public abstract String[] getRequiredColumnsArray();
    public abstract Object getFieldValue(ResultSet rs) throws SQLException;
    public abstract Class getFieldType();
}
