package exceptions;

import abstraction.MatchFieldEnum;

public class UnknownValueException extends Exception {
    public UnknownValueException(Object o, MatchFieldEnum m) {
        super("Object " + o + " is unknown value for field " + m);
    }
}
