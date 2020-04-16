package Constants;

/**
 * A wrapper class for constants which API users might want to configure (though we anticipate that such configuration
 * should be rare). Reasonable defaults are provided EXCEPT for the auxmap storage directory, which should be placed
 * somewhere other than /tmp/
 *
 * TODO As these constants are meant to be configurable, they should ideally be loaded from a .conf file or something
 * similar. Otherwise the project code has to be recompiled before changes are reflected.
 *
 * TODO Move AUX_DATA_ROOT to a preferred location not in /tmp/
 */
public class Config {

    // The "data directory" for the deduplication API. Mostly used for caching AuxMaps
    public static final String AUX_DATA_ROOT = "/tmp/aux-maps/";

    // Number of threads with which to construct AuxMaps by default. In our testing we have determined that
    // single-threaded performance is best due to the overhead associated with concurrent objects.
    // This might change (but probably wont) as larger and therefore slower hash functions are used, in which case
    // increasing the number of threads would be helpful.
    public static final int NUM_AUXMAP_THREADS = 1;

    // The number of rows to fetch at a time when hashing database entries. Larger values will reduce the number of
    // calls made to the database, but will make the program more memory-intensive. This parameter can supposedly
    // have a large effect on performance, but our testing hasn't indicated that. A reasonable-enough default is
    // provided.
    public static final int fetch_size = 100;
}
