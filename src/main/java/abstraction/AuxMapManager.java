package abstraction;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

import java.io.*;
import java.util.*;

public class AuxMapManager {

    private static String DATA_ROOT = "/tmp/aux-maps/";
    private static String AUXMAP_MANAGER = "/tmp/aux-maps/manager.map";

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

    private static Map<String, Set<MatchFieldEnum>> getOrCreateMapManager(){
        File managerFile = new File(AUXMAP_MANAGER);
        if (managerFile.exists()){
            return loadManagerFromFile();
        } else {
            HashMap<String, Set<MatchFieldEnum>> manager = new HashMap<String, Set<MatchFieldEnum>>();
            return manager;
        }
    }

    public static String mfieldSetToFilename(final Set<MatchFieldEnum> attrs) {
        return DATA_ROOT + mfieldSetToString(attrs) + ".auxmap";
    }

    public static boolean auxMapExists(final Set<MatchFieldEnum> attrs) {

        File auxMapFile = new File(mfieldSetToFilename(attrs));
        return auxMapFile.exists();
    }

    public synchronized static void saveAuxMapToFile(AuxMap aux) {

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

    public synchronized static void saveManagerToFile(Map<String, Set<MatchFieldEnum>> manager){
        deleteAuxMapManager();

        try {
            File managerFile = new File(AUXMAP_MANAGER);
            if (managerFile.getParentFile() != null){
                managerFile.getParentFile().mkdirs();
            }

            FileOutputStream fos = new FileOutputStream(managerFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(manager);
            oos.close();

        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static Map<String, Set<MatchFieldEnum>> loadManagerFromFile(){
        try {
            FileInputStream fin = new FileInputStream(AUXMAP_MANAGER);
            ObjectInputStream ois = new ObjectInputStream(fin);
            HashMap<String, Set<MatchFieldEnum>> manager = (HashMap<String, Set<MatchFieldEnum>>) ois.readObject();
            ois.close();
            return manager;
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

    public static void deleteAuxMapManager(){
        File auxMapFile = new File(AUXMAP_MANAGER);
        auxMapFile.delete();
    }

    public static AuxMap getAuxMap(NBS_DB db, Set<MatchFieldEnum> attrs, boolean delete_existing) {
        if (delete_existing) {
            deleteAuxMap(attrs);
        }
        if (auxMapExists(attrs)) {
            return loadAuxMapFromFile(attrs);
        } else {
            AuxMap aux = db.constructAuxMap(attrs);
            Map<String, Set<MatchFieldEnum>> manager = getOrCreateMapManager();
            saveAuxMapToFile(aux);
            manager.put(mfieldSetToFilename(attrs), attrs);
            saveManagerToFile(manager);
            return aux;
        }
    }
    public static AuxMap getAuxMap(NBS_DB db, Set<MatchFieldEnum> attrs) {
        return getAuxMap(db, attrs, false);
    }

    public static void hookAddRecord(NBS_DB db, Set<MatchFieldEnum> attrs, Long id, HashCode hash) {

        AuxMap map = getAuxMap(db, attrs);

        map.getIdToHashMap().put(id, hash);
        if (map.getHashToIdMap().containsKey(hash)){
            map.getHashToIdMap().get(hash).add(id);
        }
        else {
            map.getHashToIdMap().put(hash, Sets.newHashSet(id));
        }

        saveAuxMapToFile(map);
    }

    public static void hookRemoveRecord(NBS_DB db, Set<MatchFieldEnum> attrs, Long id, HashCode hash) {

        AuxMap map = getAuxMap(db, attrs);

        map.getIdToHashMap().remove(id);
        if (map.getHashToIdMap().containsKey(hash)){
            map.getHashToIdMap().get(hash).remove(id);
        }
        saveAuxMapToFile(map);
    }
}
