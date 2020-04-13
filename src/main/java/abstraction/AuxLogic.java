package abstraction;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import com.google.common.hash.HashCode;
import hashing.HashUtils;
import utils.BlockingThreadPool;
import utils.ConcurrentSetFactory;
import utils.ResultType;

public class AuxLogic {

    Connection conn;

    public AuxLogic(Connection conn) {
        this.conn = conn;
    }

    /** When a hash collision (potential match) is detected, we need to retrieve the original information to ensure
     * that the match is in fact genuine. This function calculates the MatchField values for the given person_uid and
     * returns them in a dictionary
     * @param id
     * @param attrs
     * @return
     * @throws SQLException
     */
    public Map<MatchFieldEnum, Object> getFieldsById(long id, final Set<MatchFieldEnum> attrs) throws SQLException {
        ConcurrentMap<MatchFieldEnum, Object> ret = new ConcurrentHashMap<>();
        String queryString = SQLQueryUtils.getSQLQueryForEntries(attrs, id);
        ResultSet rs = conn.createStatement().executeQuery(queryString);
        rs.next();
        for (MatchFieldEnum mf : attrs) {
            ResultType result = mf.getFieldValue(rs);
            ret.put(mf, result.getValue());
        }
        return ret;
    }

    /** Given a set of match fields, traverse the database and create a fresh AuxMap object.
     * @param attrs
     * @param num_threads The number of worker threads used to hash database entries.
     * @return
     */
    public AuxMap constructAuxMap(final Set<MatchFieldEnum> attrs, int num_threads) {

        // Obtain a ResultSet object through which to access the database
        ResultSet rs;
        String queryString = SQLQueryUtils.getSQLQueryForEntries(attrs, null);
        try {
            Statement query = conn.createStatement();
            query.setFetchSize(Constants.fetch_size);
            rs = query.executeQuery(queryString);
            rs.setFetchSize(Constants.fetch_size);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not connect to and query SQL database");
        }

        ExecutorService executor = new BlockingThreadPool(num_threads, Constants.blocking_q_size);
        // The sets referred to in the type signature below are in fact synchronized.
        ConcurrentMap<Long, Set<HashCode>> idToHash = new ConcurrentHashMap<>();
        ConcurrentMap<HashCode, Set<Long>> hashToIDs = new ConcurrentHashMap<>();

        // Loop over the items in the ResultSet, submitting them to a thread pool to be hashed in a concurrent fashion.

        ArrayList<MatchFieldEnum> attrsAsList = new ArrayList<>(attrs);

        try {
            while (rs.next()) {
                Map<MatchFieldEnum, Object> attr_map = new HashMap<>();
                boolean include_entry = true;

                long uid = (long) MatchFieldEnum.UID.getFieldValue(rs).getValue();

                for (MatchFieldEnum mfield : attrsAsList) {
                    ResultType result = mfield.getFieldValue(rs);
                    if (result.isUnknown()) {
                        include_entry = false;
                        break;
                    } else {
                        attr_map.put(mfield, result.getValue());
                    }
                }

                if (include_entry) {
                    executor.execute(
                            () -> {
                                HashCode hash = HashUtils.hashFields(attr_map);

                                idToHash.putIfAbsent(uid, ConcurrentSetFactory.newSet());
                                idToHash.get(uid).add(hash);

                                hashToIDs.putIfAbsent(hash, ConcurrentSetFactory.newSet());
                                hashToIDs.get(hash).add(uid);
                            }
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while trying to scan database entries");
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Constants.hashing_time_limit_minutes, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted before could add all entries to AuxMap");
        }
        return new AuxMap(attrs, idToHash, hashToIDs);
    }

    public AuxMap constructAuxMap(final Set<MatchFieldEnum> attrs) {
        return constructAuxMap(attrs, Constants.NUM_AUXMAP_THREADS);
    }
}
