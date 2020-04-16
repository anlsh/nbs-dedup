package utils;

/**
 * Provides something akin to an option type for sql query results
 *
 * When values are retrieved from SQL queries, it is necessary to know (a) what the value is and (b) whether the value
 * constitutes an "unknown value" for the given query. Signaling unknown values via exceptions is prohibitively
 * expensive, and this is the next best result
 */
public class ResultType {
    private Object value;
    private boolean isUnknown;

    public ResultType(Object result, boolean isUnknown) {
        this.value = result;
        this.isUnknown = isUnknown;
    }

    public Object getValue() { return value; }
    public boolean isUnknown() { return isUnknown; }
}
