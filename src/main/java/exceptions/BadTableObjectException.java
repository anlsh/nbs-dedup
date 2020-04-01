package exceptions;

import abstraction.MatchFieldEnum;

public class BadTableObjectException extends Error {
    public BadTableObjectException(Object o, MatchFieldEnum mfield) {
        super("The object " + o + " is neither a " + mfield.getFieldType()
                + " nor a Collection<" + mfield.getFieldType() + ">");
    }
}
