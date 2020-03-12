package abstraction;

import java.sql.*;
import java.util.*;

import com.google.common.hash.HashCode;
import hashing.HashUtils;

public class NBS_DB {

    public Connection conn;

    public NBS_DB(String server, int port, String dbName, String username, String password) throws SQLException {
        String connectionUrl = "jdbc:sqlserver://" + server + ":" + port + ";databaseName=" + dbName
                + ";user=" + username +  ";password=" + password;

        conn = DriverManager.getConnection(connectionUrl);
        // For safety
        conn.setReadOnly(true);
    }

    private static List<String> getQueryColumnStrings(String prefix, Set<String> NeededColumns) {
        List<String> neededCols = new ArrayList<>();
        for (String colName : NeededColumns) {
            neededCols.add(colName + " as " + (prefix + "_" + colName));
        }
        return neededCols;
    }

    public String sqlFetchColumnsForEachTable(Map<String, Set<String>> tableToNeededColumns) {

        String queryString = "SELECT "; // + String.join(", ", neededCols);
        List<String> colsFromEachTableString = new ArrayList<>();
        for (String tableName : tableToNeededColumns.keySet()) {
            colsFromEachTableString.add(String.join(", ",
                    getQueryColumnStrings(tableName, tableToNeededColumns.get(tableName))) + " from " + tableName);
        }
        queryString += String.join(", ", colsFromEachTableString);

        if(tableToNeededColumns.keySet().size() > 1) {
            List<String> alignTables = new ArrayList<>();
            //This kind of joining found at https://www.geeksforgeeks.org/joining-three-tables-sql/
            Iterator<String> iter = tableToNeededColumns.keySet().iterator();
            String primaryTableName = iter.next();
            while (iter.hasNext()) {
                //TODO make a map from each table to the name of its Person ID column, use that instead of Constants.COL_PERSON_UID all the time
                alignTables.add(primaryTableName + "." + Constants.COL_PERSON_UID + " = " + iter.next() + "." + Constants.COL_PERSON_UID);
            }

            queryString += " where " + String.join(" and ", alignTables);
        }
        return queryString;
    }

    public String sqlFetchColumnsForUID(long uid, Map<String, Set<String>> tableToNeededColumns) {
        return sqlFetchColumnsForEachTable(tableToNeededColumns) + " and " + Constants.COL_PERSON_UID + " = " + uid;
    }

    public Map<MatchFieldEnum, Object> getFieldsById(long id, final Set<MatchFieldEnum> attrs) {
        Map<String, Set<String>> tableToColsMap = MatchFieldUtils.getRequiredColumnsForEachTable(attrs);
        ResultSet rs;
        try {
            rs = conn.createStatement().executeQuery(
                    sqlFetchColumnsForUID(id, tableToColsMap)
            );
            rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not query SQL DB to find Person with id "
                    + id + " and attrs" + attrs);
        }
        Map<MatchFieldEnum, Object> ret = new HashMap<>();
        for (MatchFieldEnum mf : attrs) {
            try {
                ret.put(mf, mf.getFieldValue(rs));
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Couldn't get value for field " + mf + " in resultset");
            }
        }
        return ret;
    }

    public AuxMap constructAuxMap(final Set<MatchFieldEnum> attrs) throws SQLException {

        String queryString = sqlFetchColumnsForEachTable(
                MatchFieldUtils.getRequiredColumnsForEachTable(attrs)
        );
        ResultSet rs = conn.createStatement().executeQuery(queryString);

        Map<Long, HashCode> idToHash = new HashMap<>();
        Map<HashCode, Set<Long>> hashToIDs = new HashMap<>();

        while (rs.next()) {
            Map<MatchFieldEnum, Object> attr_map = new HashMap<>();

            // If a required mfield in the result set is unknown, then we prevent it from being inserted into the
            // auxiliary map at all. Hashing unknown values to a constant will cause the algorithm to mark two entries
            // with unknown SSNs, for example as duplicates
            boolean include_entry = true;
            for (MatchFieldEnum mfield : attrs) {
                Object mfield_val = mfield.getFieldValue(rs);
                if (mfield.isUnknownValue(mfield_val)) {
                    include_entry = false;
                    break;
                }
                attr_map.put(mfield, mfield.getFieldValue(rs));
            }
            if (!include_entry) {
                continue;
            }

            // All the relevant fields are known, insert the entry into the Auxiliary Map.
            long record_id = (long) MatchFieldEnum.UID.getFieldValue(rs);
            HashCode hash = HashUtils.hashFields(attr_map);
            idToHash.put(record_id, hash);

            Set<Long> idsWithSameHash = hashToIDs.getOrDefault(hash, new HashSet<>());
            idsWithSameHash.add(record_id);
            hashToIDs.put(hash, idsWithSameHash);
        }

        return new AuxMap(attrs, idToHash, hashToIDs);
    }
}
