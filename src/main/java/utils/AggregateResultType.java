package utils;

import abstraction.MatchFieldEnum;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulating object for
 *      (a) values of a set of MatchFieldEnum on a specific record and
 *      (b) whether there are any unknown values in (a)
 */
public class AggregateResultType {
    private Map<MatchFieldEnum, Object> values;
    private boolean isUnknown;

    /** Given a set of MatchFieldEnums and a record, extract values for the match fields.
     *
     * The set of extracted values is guaranteed to include all requested values only when all of them are known.
     * Otherwise, all extracted values are discarded.
     * @param attrs     A set of MatchFieldEnum whose values are to be extracted
     * @param rs        A ResultSet whose current active row is the record from which to extract *attrs*
     */
    public AggregateResultType(Set<MatchFieldEnum> attrs, ResultSet rs) {

        Map<MatchFieldEnum, Object> vmap = new HashMap<>();
        isUnknown = true;

        try {
            for (MatchFieldEnum mfield : new ArrayList<>(attrs)) {
                ResultType result = mfield.getFieldValue(rs);
                if (result.isUnknown()) {
                    return;
                } else {
                    vmap.put(mfield, result.getValue());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        isUnknown = false;
        values = vmap;
    }

    //Make sure that column names are of the form TABLE_NAME__COLUMN_NAME
    public AggregateResultType(Set<MatchFieldEnum> attrs, String[] columnArr, String[] valueArr) {
        Map<MatchFieldEnum, Object> vmap = new HashMap<>();
        isUnknown = true;
        for(MatchFieldEnum mfield : new ArrayList<>(attrs)) {
            ResultType result = mfield.getFieldValue(columnArr, valueArr);
            if(result.isUnknown()) return;
            else vmap.put(mfield, result.getValue());
        }
        isUnknown = false;
        values = vmap;
    }

    public Map<MatchFieldEnum, Object> getValues() { return values; }
    public boolean isUnknown() { return isUnknown; }
}
