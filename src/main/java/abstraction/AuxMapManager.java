package abstraction;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AuxMapManager {

    public static final String DATA_ROOT = "/tmp/aux-maps/";

    public static String mfieldSetToString(final Set<MatchFieldEnum> attrs) {
        List<MatchFieldEnum> attrList = new ArrayList<MatchFieldEnum>(attrs);
        Collections.sort(attrList);
        String attributes_str = "";
        for (MatchFieldEnum mfield : attrList) {
            attributes_str += mfield.toString() + "_";
        }
        return attributes_str;
    }

    public static boolean auxMapExists(final Set<MatchFieldEnum> attrs) {

        String attributes_str = mfieldSetToString(attrs);
        String auxMapFileName = DATA_ROOT + attributes_str + ".auxmap";
        File auxMapFile = new File(auxMapFileName);

        return auxMapFile.exists();
    }
}
