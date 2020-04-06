package utils;

public class ResultType {
    public Object value;
    public boolean unknown;

    public ResultType(Object result, boolean unknownValue) {
        this.value = result;
        this.unknown = unknownValue;
    }
}
