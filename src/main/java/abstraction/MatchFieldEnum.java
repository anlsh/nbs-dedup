package abstraction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public enum MatchFieldEnum {
    UID {
        @Override public MatchFieldEnum getParent() { return null; }

        @Override public boolean isDeduplicableField() { return false; }
        @Override public String getHumanReadableName() { return "Unique ID"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_PERSON_UID}; }
        @Override public Class getFieldType() { return Long.class; }
        @Override public boolean isUnknownValue(Object o) { return o == null; }
        @Override public String getTableName() {return "Person";}
    },
    FIRST_NAME {
        @Override public MatchFieldEnum getParent() { return null; }
        @Override public String getHumanReadableName() { return "First Name"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_FIRST_NAME}; }
        @Override public Class getFieldType() { return String.class; }
        @Override public boolean isUnknownValue(Object o) { return o == null; }
        @Override public String getTableName() {return "Person";}
    },
    LAST_NAME {
        @Override public MatchFieldEnum getParent() { return null; }
        @Override public String getHumanReadableName() { return "Last Name"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_LAST_NAME}; }
        @Override public Class getFieldType() { return String.class; }
        @Override public boolean isUnknownValue(Object o) { return o == null; }
        @Override public String getTableName() {return "Person";}
    },
    SSN {
        @Override public MatchFieldEnum getParent() { return null; }
        @Override public String getHumanReadableName() { return "Social Security Number"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{Constants.COL_SSN}; }
        @Override public Class getFieldType() { return String.class; }
        @Override public boolean isUnknownValue(Object o) { return o == null; }
        @Override public String getTableName() {return "Person";}
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
        @Override public boolean isUnknownValue(Object o) { return o == null; }
        @Override public String getTableName() {return "Person";}
    },

    OTHER_TABLE_NAME {
        @Override public MatchFieldEnum getParent() { return null; }
        @Override public String getHumanReadableName() { return "Name but from the person table"; }
        @Override public String[] getRequiredColumnsArray() { return new String[]{"first_nm"}; }
        @Override public Class getFieldType() { return String.class; }
        @Override public boolean isUnknownValue(Object o) { return o == null; }
        @Override public String getTableName() { return "Person_name"; }
    };

    public boolean isDeduplicableField() {
        // Should return true for fields which it makes sense to deduplicate on (almost all of them) and false
        // for others: at the moment the only field which it doesn't make sense to deduplicate on is UID
        return true;
    }
    public abstract MatchFieldEnum getParent();
    public abstract String getHumanReadableName();
    public abstract String[] getRequiredColumnsArray();
    public Object getFieldValue(ResultSet rs) throws SQLException {
        if (getRequiredColumnsArray().length != 1) {
            throw new RuntimeException("Using default getFieldValue to retrieve information " +
                    "depending on multiple fields");
        }
        return rs.getObject(getTableName() + "__" + getRequiredColumnsArray()[0]);
    };
    public abstract Class getFieldType();
    public abstract boolean isUnknownValue(Object o);
    public abstract String getTableName();

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
