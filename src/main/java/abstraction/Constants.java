package abstraction;

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
    public static final String WEB_SERVER = "localhost";
    public static final String WEB_PORT = "8080";
    public static final String GET_CONFIGS_REQUEST = "/config/";
    public static final String PRIMARY_TABLE_NAME = "Person";

    // Paralellization Things

    public static final int NUM_AUXMAP_THREADS = 4;
//    public static final int NUM_AUXMAP_THREADS = 1; //TODO debug to see if AuxMap construction parallelism is bugged
    // TODO I don't think this variable actually affects the time even though it apparently really should
    // See https://stackoverflow.com/questions/17744090/iterating-a-resultset-using-the-jdbc-for-oracle-takes-a-lot-of-time-about-16s
    public static final int fetch_size = 10;

    public static final int blocking_q_size = 10;
    public static final int hashing_time_limit_minutes = 60;
}
