package utils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class providing a method to construct concurrent (thread-safe) sets.
 *
 * Java 8 seems to have no ConcurrentSet type, though oddly enough it has a ConcurrentMap type from which concurrent
 * sets can be created- this class simply provides the utility to do so.
 */
public class ConcurrentSetFactory {
    /**
     * @param <T>   The type of object which this set will hold
     * @return      A new Set object which supports concurrent modification
     */
    public static <T> Set<T> newSet() {
        //  Java does not seem to have a "concurrent set" type, although they can in fact be created by "deriving" from
        //  a HashMap. We take that approach below
        //  See https://better-coding.com/solved-how-to-create-concurrenthashset-before-and-after-java-8/
        Map<T, Boolean> myMap = new ConcurrentHashMap<>();
        return Collections.newSetFromMap(myMap);
    }
}
