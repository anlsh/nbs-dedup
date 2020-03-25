package abstraction;

import java.lang.reflect.Array;
import java.util.*;

public class MatchFieldUtils {

    private static List<MatchFieldEnum> sortedMfields = null;
    public static List<MatchFieldEnum> getSortedMfields() {
        if (sortedMfields == null) {
            List<MatchFieldEnum> thingy = Arrays.asList(MatchFieldEnum.values());
            Collections.sort(thingy);
            sortedMfields = thingy;
        }
        return sortedMfields;
    }

    /**
     * Given the name of a table and a column within that table, return a fully-qualified name suitable for use in
     * an SQL query
     * @param tableName Name of a table in the SQL schema
     * @param columnName The name of a column within the aforementioned table
     * @return
     */
    public static String getSQLQualifiedColName(String tableName, String columnName) {
        return tableName + "." + columnName;
    }

    /**
     * Given the name of a table and a column within that table, return an alias suitable for the deduplication API's
     * internal use. This function is necessary because the SQL query cannot alias to aliases containing a period,
     * perhaps because the syntax is simply disallowed or simply because of a name conflict in the specifically-tested
     * queries (more likely).
     * @param tableName Name of a table in the SQL schema
     * @param columnName The name of a column within the aforementioned table
     * @return
     */
    public static String getAliasedColName(String tableName, String columnName) {
        return tableName + "__" + columnName;
    }

    /** Given a set of matchfieldenums, generate a map from required tables in the database to attributes depending
     * on them
     * @param attrs
     * @return
     */
    public static Map<String, Set<MatchFieldEnum>> getTableNameMap(Set<MatchFieldEnum> attrs) {
        Map<String, Set<MatchFieldEnum>> ret = new HashMap<>();
        for(MatchFieldEnum e : attrs) {
            Set<MatchFieldEnum> entry = ret.getOrDefault(e.getTableName(), new HashSet<>());
            entry.add(e);
            ret.put(e.getTableName(), entry);
        }
        return ret;
    }

    //TODO clean this up and comment it, it feels bad and arcane
    //TODO rewrite this to be efficient/not recursive
    public static Set<Map<MatchFieldEnum, Object>> explodeAttrMap(Map<MatchFieldEnum, Object> attr_map) {
        Set<MatchFieldEnum> keySet = attr_map.keySet();
        Set<Map<MatchFieldEnum, Object>> ret = new HashSet<>();
        for(MatchFieldEnum mf : keySet) {
            if(mf.isMultiField()) {
                if(Iterable.class.isAssignableFrom(mf.getFieldType())) {
                    Iterable c = (Iterable) attr_map.get(mf);
                    for(Object o : c) {
                        Map<MatchFieldEnum, Object> m = new HashMap<>(attr_map);
                        m.remove(mf);
                        m.put(mf.explodedFieldName(), o);
                        ret.add(m);
                    }
                } else if(Array.class.isAssignableFrom(mf.getFieldType())) {
                    //TODO
                } else {
                    throw new RuntimeException("Field " + mf + " is multifield but specified type " + mf.getFieldType() + " does not implement Collection or Array");
                }
                //TODO this recursion is really REALLY inefficient but it'll work
                Set<Map<MatchFieldEnum, Object>> realRet = new HashSet<>();
                for(Map<MatchFieldEnum, Object> map : ret) realRet.addAll(explodeAttrMap(map));
                return realRet;
            }
        }
        ret.add(attr_map);
        return ret;
    }
}
