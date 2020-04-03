package utils;

import java.util.Set;

public class ResultType {
    public Set<Object> values;
    public boolean unknown;

    public ResultType(Set<Object> result, boolean unknownValue) {
        this.values = result;
        this.unknown = unknownValue;
    }
}
