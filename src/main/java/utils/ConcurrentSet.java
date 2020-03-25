package utils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentSet {
    //  Java does not seem to have a "concurrent set" type, although they can in fact be created by "deriving" from a
    //  HashMap. We take that approach below
    //  See https://better-coding.com/solved-how-to-create-concurrenthashset-before-and-after-java-8/
    public static <T> Set<T> newSet() {
        Map<T, Boolean> myMap = new ConcurrentHashMap<>();
        return Collections.newSetFromMap(myMap);
    }
    public static <T> Set<T> newSingletonSet(T el) {
        Set<T> set = ConcurrentSet.newSet();
        set.add(el);
        return set;
    }
}
