package utils;

import abstraction.MatchFieldEnum;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TODO Document
 */
public class AggregateResultType {
    private Map<MatchFieldEnum, Object> values;
    private boolean isUnknown;

    public AggregateResultType(Set<MatchFieldEnum> attrs, ResultSet rs) {

        values = new HashMap<>();
        isUnknown = false;

        try {
            for (MatchFieldEnum mfield : new ArrayList<>(attrs)) {
                ResultType result = mfield.getFieldValue(rs);
                if (result.isUnknown()) {
                    isUnknown = true;
                    break;
                } else {
                    values.put(mfield, result.getValue());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<MatchFieldEnum, Object> getValues() { return values; }
    public boolean isUnknown() { return isUnknown; }
}
