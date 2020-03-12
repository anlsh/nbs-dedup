package abstraction;

import java.util.*;

public class MatchFieldUtils {
    public static Map<String, Set<String>> getRequiredColumnsForEachTable(Set<MatchFieldEnum> attrs) {
        Map<String, Set<String>> ret = new HashMap<>();
        for(MatchFieldEnum mfield : attrs) {
            Set<String> entry = ret.getOrDefault(mfield.getTableName(), new HashSet<>());
            entry.addAll(Arrays.asList(mfield.getRequiredColumnsArray()));
            ret.put(mfield.getTableName(), entry);
        }
        return ret;
    }
}