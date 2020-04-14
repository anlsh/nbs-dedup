package abstraction;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import com.google.common.hash.HashCode;
import hashing.HashUtils;
import utils.AggregateResultType;
import utils.BlockingThreadPool;
import utils.ResultType;

public class AuxLogic {

    private Connection conn;

    public AuxLogic(Connection conn) {
        this.conn = conn;
    }

    /** When a hash collision (potential match) is detected, we need to retrieve the original information to ensure
     * that the match is in fact genuine. This function calculates the MatchField values for the given person_uid and
     * returns them in a dictionary
     * @param id        Specific value for patient_uid
     * @param attrs     Set of MatchFieldEnums to retrieve values for
     * @return          A map from MatchFieldEnum to values
     * @throws SQLException
     */
    public Map<MatchFieldEnum, Object> getFieldsById(long id, final Set<MatchFieldEnum> attrs) throws SQLException {
        Map<MatchFieldEnum, Object> ret = new HashMap<>();
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
        AuxMap aux = new AuxMap(attrs, idToHash, hashToIDs);

        // Is unused when num_threads == 1, but must be initialized anyways
        ExecutorService executor = new BlockingThreadPool(num_threads, Constants.blocking_q_size);


        // Loop over the items in the ResultSet, hashing them immediately or concurrently
        try {
            while (rs.next()) {

                long uid = (long) MatchFieldEnum.UID.getFieldValue(rs).getValue();
                AggregateResultType result = new AggregateResultType(attrs, rs);

                // Hash the entry and update the maps accordingly
                if (!result.isUnknown()) {
                    Runnable hashSubmissionJob = () -> {
                        aux.addPair(uid, HashUtils.hashFields(result.getValues()));
                    };

                    if (num_threads == 1) {
                        hashSubmissionJob.run();
                    } else {
                        executor.execute(hashSubmissionJob);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Constants.HASHING_TIME_LIMIT_VAL, Constants.HASHING_TIME_LIMIT_UNITS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new AuxMap(attrs, idToHash, hashToIDs);
    }

    public AuxMap constructAuxMap(final Set<MatchFieldEnum> attrs) {
        return constructAuxMap(attrs, Constants.NUM_AUXMAP_THREADS);
    }
}
