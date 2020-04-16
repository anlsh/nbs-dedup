package utils;

import abstraction.MatchFieldEnum;

/**
 * Thrown whenever a MatchFieldEnum cannot retrieve its value from a ResultSet due to a typing error in columns.
 *
 * In practice this should never be thrown and as such is an unchecked exception.
 */
public class BadTableObjectException extends Error {
    public BadTableObjectException(Object o, MatchFieldEnum mfield) {
        super(mfield + " cannot interpret the following object as " + mfield.getFieldType() + ": " + o);
    }
}
