package Constants;

import java.util.concurrent.TimeUnit;

/**
 * A class strictly for constants employed throughout the API, but which are purely internal: ie, end-users will never
 * know or care about them.
 */
public class InternalConstants {

    // Name of the primary table
    public static final String PRIMARY_TABLE_NAME = "Person";

    // Constants describing column names in the database. This list isn't comprehensive because column names only ever
    // show up in MatchFieldEnum (usually only once). However these are useful for setting up the database schema for
    // testing purposes
    public static final String COL_PERSON_UID = "person_uid";
    public static final String COL_FIRST_NAME = "first_nm";
    public static final String COL_LAST_NAME = "last_nm";
    public static final String COL_SSN = "SSN";

    // The file extensions to be used for cached AuxMaps
    public static final String AUX_FILE_EXTENSION = "aux";
    public static final String MANAGER_FILE_NAME = "manager.json";

    // Time-outs for various operations (hashing entries and maximum life of a thread in the paralellized hashing
    // thread pool). The timeouts are large enough that they actually don't matter (by design): they mainly just need
    // to exist for certain function calls
    public static final int HASHING_TIME_LIMIT_VAL = 60;
    public static final TimeUnit HASHING_TIME_LIMIT_UNITS = TimeUnit.MINUTES;
    public static final int THREAD_TIMEOUT_MILLIS = 60;
    public static final TimeUnit THREAD_TIMEOUT_UNITS = TimeUnit.MINUTES;
}
