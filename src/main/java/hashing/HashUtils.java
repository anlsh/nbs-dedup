package hashing;

import com.google.common.base.Charsets;
import com.google.common.hash.*;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Map;

public class HashUtils {
    public static HashFunction hashFunction = Hashing.sipHash24();
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

    /*
    Disabled because I can't guarantee that a map with the same contents will be looped through in the same
    order, so it might not get the same hash, which is bad.
    enum FieldsFunnel implements Funnel<Map<String, Object>> {
        INSTANCE;
        public void funnel(Map<String, Object> fields, PrimitiveSink into) {
            for(String s : fields.keySet()) {
                into.putString(s, schemaCharset);
                String serial = fields.get(s).toString();
                into.putInt(serial.length());
                into.putUnencodedChars(serial);
            }
        }
    }

    public static HashCode hash(Map<String, Object> fields, Map<String, Class<?>> typeMap) {

    }

     */
}
