package hashing;

import Constants.Config;

import abstraction.MatchFieldEnum;
import com.google.common.base.Charsets;
import com.google.common.hash.*;

import javax.naming.OperationNotSupportedException;
import java.nio.charset.Charset;
import java.util.*;

public class HashUtils {
    private static HashFunction hashFunction = Config.HASH_FUNCTION;
    private static Charset charset = Charsets.UTF_8; //Whatever NBS uses, change this to that.
    private static Charset schemaCharset = Charsets.UTF_8;
    //Note that all HashCode objects have a asLong() function, as well as asBytes()
    //https://guava.dev/releases/19.0/api/docs/com/google/common/hash/HashCode.html

    public static HashCode hash(boolean b) {
        return hashFunction.newHasher().putBoolean(b).hash();
    }

    public static HashCode hash(byte b) {
        return hashFunction.newHasher().putByte(b).hash();
    }

    public static HashCode hash(byte[] b) {
        return hashFunction.newHasher().putBytes(b).hash();
    }

    public static HashCode hash(char c) {
        return hashFunction.newHasher().putChar(c).hash();
    }

    public static HashCode hash(double d) {
        return hashFunction.newHasher().putDouble(d).hash();
    }

    public static HashCode hash(float f) {
        return hashFunction.newHasher().putFloat(f).hash();
    }

    public static HashCode hash(int i) {
        return hashFunction.newHasher().putInt(i).hash();
    }

    public static HashCode hash(long l) {
        return hashFunction.newHasher().putLong(l).hash();
    }

    //Note that String implements CharSequence, so you can put strings into this.
    public static HashCode hash(CharSequence s) {
        return hashFunction.newHasher().putString(s, charset).hash();
    }

    public static <T> HashCode hash(T t, Funnel<T> funnel) {
        return hashFunction.newHasher().putObject(t, funnel).hash();
    }

    public static void putIntoHasher(Hasher hasher, Class type, Object object) throws OperationNotSupportedException {
        if(type == String.class) {
            if(object == null) hasher.putString("", charset);
            else hasher.putString((String) object, charset);
        } else if(type == Integer.class) {
            if(object == null) hasher.putInt(0);
            else hasher.putInt((Integer) object);
        } else if(type == Long.class) {
            if(object == null) hasher.putInt(0);
            else hasher.putLong((Long) object);
        } else {
            throw new OperationNotSupportedException("Putting an object of type " + type.toString() + " in a hasher is unsupported");
        }
        //TODO expand this to all reasonable types
    }

    public static HashCode hashFields(Map<MatchFieldEnum, Object> record) {

        for (MatchFieldEnum mfield : record.keySet()) {
            if (!mfield.isDeduplicableField()) {
                throw new RuntimeException("Attempting to include non-deduplicable field " + mfield
                        + " in deduplication attributes");
            }
        }

        Set<MatchFieldEnum> keySet = record.keySet();
        Hasher hasher = hashFunction.newHasher();

        for (MatchFieldEnum mfield : MatchFieldEnum.values()) {
            if (keySet.contains(mfield)) {
                try {
                    putIntoHasher(hasher,  mfield.getFieldType(), record.get(mfield));
                } catch(OperationNotSupportedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return hasher.hash();
    }
}
