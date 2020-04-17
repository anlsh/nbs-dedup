package Constants;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * A wrapper class for constants which API users might want to configure (though we anticipate that such configuration
 * should be rare). Reasonable defaults are provided EXCEPT for the auxmap storage directory, which should be placed
 * somewhere other than /tmp/
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

    // Hash function to be used for hashing records. Hash functions producing more bits may reduce collisions (we do
    // check for those), but will also consume correspondingly more space on disk: ie a 64-bit hashmap will consume
    // roughly twice as much space as a 32-bit hashmap, etc.
    // 64-bits should ensure a low number of collisions even on the largest databases while consuming a reasonable
    // amount of space. Therefore, the default hash function is the SipHash-2-4 algorithm
    // Other choices for hashing algorithms, with more or fewer bits, are available at
    // https://guava.dev/releases/21.0/api/docs/com/google/common/hash/Hashing.html
    public static final HashFunction HASH_FUNCTION = Hashing.sipHash24();
}
