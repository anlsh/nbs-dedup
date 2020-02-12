package abstraction;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AuxMapManager {

    private static String DATA_ROOT = "/tmp/aux-maps/";
    public static String getDataRoot() { return DATA_ROOT; }
    // TODO Make the setter for this do proper validation on directory name
    public static void setDataRoot(String curr) { DATA_ROOT = curr; }

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
            File auxMapFile = new File(mfieldSetToFilename(aux.attrs));
            if (auxMapFile.getParentFile() != null) {
                auxMapFile.getParentFile().mkdirs();
            }

            FileOutputStream fos = new FileOutputStream(auxMapFile);
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
}
