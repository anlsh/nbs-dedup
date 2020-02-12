package abstraction;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AuxMapManager {

    public static final String DATA_ROOT = "/tmp/aux-maps/";

    public static String mfieldSetToString(final Set<MatchFieldEnum> attrs) {
        // TODO mfieldSetToString is called in a bunch of places, so there's some duplicate work there...
        // Maybe reduce duplicate calls to it later
        List<MatchFieldEnum> attrList = new ArrayList<MatchFieldEnum>(attrs);
        Collections.sort(attrList);
        String attributes_str = "";
        for (MatchFieldEnum mfield : attrList) {
            attributes_str += mfield.toString() + "_";
        }
        return attributes_str;
    }

    public static String mfieldSetToFilename(final Set<MatchFieldEnum> attrs) {
        return DATA_ROOT + mfieldSetToString(attrs) + ".auxmap";
    }

    public static boolean auxMapExists(final Set<MatchFieldEnum> attrs) {

        File auxMapFile = new File(mfieldSetToFilename(attrs));
        return auxMapFile.exists();
    }

    public static void saveAuxMapToFile(AuxMap aux) {

        deleteAuxMap(aux.attrs);

        try {
            FileOutputStream fos = new FileOutputStream(mfieldSetToFilename(aux.attrs));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(aux);
            oos.close();
        } catch (Exception e) {
            // TODO Exception handling
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static AuxMap loadAuxMapFromFile(final Set<MatchFieldEnum> attrs) {

        try {
            FileInputStream fin = new FileInputStream(mfieldSetToFilename(attrs));
            ObjectInputStream ois = new ObjectInputStream(fin);
            AuxMap aux = (AuxMap) ois.readObject();
            ois.close();
            return aux;
        } catch (Exception e) {
            // TODO Exception handling
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static void deleteAuxMap(final Set<MatchFieldEnum> attrs) {
        File auxMapfile = new File(mfieldSetToFilename(attrs));
        auxMapfile.delete();
    }
    public static AuxMap getAuxMap(NBS_DB db, Set<MatchFieldEnum> attrs, boolean delete_existing) {
        if (delete_existing) {
            deleteAuxMap(attrs);
        }
        if (auxMapExists(attrs)) {
            return loadAuxMapFromFile(attrs);
        } else {
            AuxMap aux = db.constructAuxMap(attrs);
            saveAuxMapToFile(aux);
            return aux;
        }
    }
    public static AuxMap getAuxMap(NBS_DB db, Set<MatchFieldEnum> attrs) {
        return getAuxMap(db, attrs, false);
    }
}
