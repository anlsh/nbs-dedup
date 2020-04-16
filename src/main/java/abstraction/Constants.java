package abstraction;

import java.util.concurrent.TimeUnit;

public class Constants {
    public static final String COL_PERSON_UID = "person_uid";
    public static final String COL_FIRST_NAME = "first_nm";
    public static final String COL_LAST_NAME = "last_nm";
    public static final String COL_SSN = "SSN";
    public static final String DB_SERVER = "localhost";
    public static final int DB_PORT = 1433;
    public static final String DB_NAME = "ODS_PRIMARY_DATA01";
    public static final String DB_USERNAME = "SA";
    public static final String DB_PASSWORD = "saYyWbfZT5ni7t";
    public static final String PRIMARY_TABLE_NAME = "Person";

    public static final String AUX_DATA_ROOT = "/tmp/aux-maps/";

    // Paralellization Things

    public static final int NUM_AUXMAP_THREADS = 4;
    // TODO I don't think this variable actually affects the time even though it apparently really should
    // See https://stackoverflow.com/questions/17744090/iterating-a-resultset-using-the-jdbc-for-oracle-takes-a-lot-of-time-about-16s
    public static final int fetch_size = 10;

    public static final int blocking_q_size = 10;
    public static final int HASHING_TIME_LIMIT_VAL = 60;
    public static final TimeUnit HASHING_TIME_LIMIT_UNITS = TimeUnit.MINUTES;

    public static final int THREAD_TIMEOUT_MILLIS = 2000;
    public static final TimeUnit THREAD_TIMEOUT_UNITS = TimeUnit.MILLISECONDS;

    public static final String AUX_FILE_EXTENSION = ".aux";
    public static final String MANAGER_FILE_NAME = "manager.json";
}
