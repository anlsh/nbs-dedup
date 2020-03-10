package abstraction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MatchFieldUtils {
    public static Map<String, Set<MatchFieldEnum>> getTableNameMap(Set<MatchFieldEnum> attrs) {
        Map<String, Set<MatchFieldEnum>> ret = new HashMap<>();
        for(MatchFieldEnum e : attrs) {
            Set<MatchFieldEnum> entry = ret.getOrDefault(e.getTableName(), new HashSet<>());
            entry.add(e);
            ret.put(e.getTableName(), entry);
        }
        return ret;
    }
}