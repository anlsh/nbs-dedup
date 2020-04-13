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

    /**
     * Given a set of match fields, traverse the database and create a fresh AuxMap object.
     *
     * @param attrs         The relevant set of MatchFieldEnums
     * @param num_threads   The number of worker threads used to hash database entries.
     * @return              An AuxMap object for the fields specified by "attrs"
     */
    public AuxMap constructAuxMap(final Set<MatchFieldEnum> attrs, int num_threads) {

        // When num_threads == 1, we can avoid some overhead by not using thread-safe objects. This unfortunately
        // complicates the code somewhat, but the increase in performance is probably worth it.

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
            throw new RuntimeException(e);
        }

        Map<Long, Set<HashCode>> idToHash;
        Map<HashCode, Set<Long>> hashToIDs;

        if (num_threads == 1) {
            idToHash = new HashMap<>();
            hashToIDs = new HashMap<>();
        } else {
            idToHash = new ConcurrentHashMap<>();
            hashToIDs = new ConcurrentHashMap<>();
        }
        // Is unused when num_threads == 1, but must be initialized anyways
        ExecutorService executor = new BlockingThreadPool(num_threads, Constants.blocking_q_size);


        // Loop over the items in the ResultSet, hashing them immediately or concurrently
        try {
            while (rs.next()) {
                Map<MatchFieldEnum, Object> attr_map = new HashMap<>();
                boolean include_entry = true;
                long uid = (long) MatchFieldEnum.UID.getFieldValue(rs).getValue();

                // Determine whether the current record should be included in the AuxMap
                for (MatchFieldEnum mfield : new ArrayList<>(attrs)) {
                    ResultType result = mfield.getFieldValue(rs);
                    if (result.isUnknown()) {
                        include_entry = false;
                        break;
                    } else {
                        attr_map.put(mfield, result.getValue());
                    }
                }

                // Hash the entry and update the maps accordingly
                if (include_entry) {
                    Runnable hashSubmissionJob = () -> {
                        HashCode hash = HashUtils.hashFields(attr_map);

                        idToHash.putIfAbsent(uid, ConcurrentSetFactory.newSet());
                        idToHash.get(uid).add(hash);

                        hashToIDs.putIfAbsent(hash, ConcurrentSetFactory.newSet());
                        hashToIDs.get(hash).add(uid);
                    };

                    if (num_threads == 1) {
                        hashSubmissionJob.run();
                    } else {
                        executor.execute(hashSubmissionJob);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while trying to scan database entries");
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Constants.HASHING_TIME_LIMIT_VAL, Constants.HASHING_TIME_LIMIT_UNITS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted before could add all entries to AuxMap");
        }
        return new AuxMap(attrs, idToHash, hashToIDs);
    }

    public AuxMap constructAuxMap(final Set<MatchFieldEnum> attrs) {
        return constructAuxMap(attrs, Constants.NUM_AUXMAP_THREADS);
    }
}
