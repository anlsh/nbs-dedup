package abstraction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.channels.FileLock;

import hashing.HashUtils;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import utils.AggregateResultType;

import java.io.*;
import java.util.*;

public class AuxMapManager {

    private static String AUXMAP_MANAGER = Constants.AUX_DATA_ROOT + "manager.json";
    public static String getDataRoot() { return Constants.AUX_DATA_ROOT; }

    /**
     * Condenses a set of MatchFieldEnum into a number which will be used as the AuxMap's filename
     * @param attrs     A set of MatchFieldEnum
     * @return          The filename for attrs
     */
    static String mfieldSetToFilename(final Set<MatchFieldEnum> attrs) {
        List<MatchFieldEnum> attrList = new ArrayList<>(attrs);
        Collections.sort(attrList);
        List<String> attrNames = new ArrayList<>();
        for (MatchFieldEnum mfield : attrList) {
            attrNames.add(mfield.toString());
        }
        String attrHash = Integer.toString(Math.abs(String.join("__", attrNames).hashCode()));
        return Constants.AUX_DATA_ROOT + attrHash + ".auxmap";
    }

    private static JSONObject getOrCreateMapManager() {

        File managerFile = new File(AUXMAP_MANAGER);
        if (managerFile.exists()) {
            return loadManagerFromFile();
        } else {
            return new JSONObject();
        }
    }

    /**
     * Check whether an auxmap file exists for the given set of MatchFieldEnums
     *
     * @param attrs     A set of MatchFieldEnum
     * @return          Whether the file exists
     */
    public static boolean auxMapExists(final Set<MatchFieldEnum> attrs) {
        File auxMapFile = new File(mfieldSetToFilename(attrs));
        return auxMapFile.exists();
    }

    /**
     * Save an AuxMap object to the filesystem
     * @param aux   An AuxMap object
     */
    public static void saveAuxMapToFile(AuxMap aux) {

        deleteAuxMap(aux.getAttrs());

        try {
            File auxMapFile = new File(mfieldSetToFilename(aux.getAttrs()));
            auxMapFile.getParentFile().mkdirs();
            if (auxMapFile.getParentFile() != null) {
                auxMapFile.getParentFile().mkdirs();
            }

            FileOutputStream fos = new FileOutputStream(auxMapFile);
            FileLock lock = fos.getChannel().lock();   //blocks until obtained
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(aux);
            oos.close();


            hookManagerAddMap(aux);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static void saveManagerToFile(JSONObject manager){
        deleteAuxMapManager();

        try {
            File managerFile = new File(AUXMAP_MANAGER);
            managerFile.getParentFile().mkdirs();
            FileWriter managerFileWriter = new FileWriter(managerFile);
            if (managerFile.getParentFile() != null){
                managerFile.getParentFile().mkdirs();
            }

            managerFileWriter.write(manager.toJSONString());
            managerFileWriter.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void hookManagerAddMap(AuxMap auxMap){
        JSONObject manager = getOrCreateMapManager();
        String fileName = mfieldSetToFilename(auxMap.getAttrs());
        JSONArray attrString = new JSONArray();
        attrString.addAll(auxMap.getAttrs());

        manager.put(fileName, attrString);
        saveManagerToFile(manager);
    }

    private static JSONObject loadManagerFromFile(){
        try (FileReader reader = new FileReader(AUXMAP_MANAGER)) {
            JSONParser jsonParser = new JSONParser();
            return (JSONObject) jsonParser.parse(reader);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static AuxMap loadAuxMapFromFilename(final String filename){
        try {
            FileInputStream fin = new FileInputStream(filename);
            FileLock lock = fin.getChannel().lock(0L, Long.MAX_VALUE, true);
            ObjectInputStream ois = new ObjectInputStream(fin);
            AuxMap aux = (AuxMap) ois.readObject();
            ois.close();
            return aux;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static AuxMap loadAuxMapFromFile(final Set<MatchFieldEnum> attrs) {

        try {
            FileInputStream fin = new FileInputStream(mfieldSetToFilename(attrs));
            FileLock lock = fin.getChannel().lock(0L, Long.MAX_VALUE, true);
            ObjectInputStream ois = new ObjectInputStream(fin);
            AuxMap aux = (AuxMap) ois.readObject();
            ois.close();

            aux.ensureThreadSafe();
            return aux;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteAuxMap(final Set<MatchFieldEnum> attrs) {

        File auxMapfile = new File(mfieldSetToFilename(attrs));
        auxMapfile.delete();

        JSONObject manager = getOrCreateMapManager();
        String fileName = mfieldSetToFilename(attrs);

        manager.remove(fileName);
        saveManagerToFile(manager);
    }

    public static void deleteAuxMapManager(){
        File auxMapFile = new File(AUXMAP_MANAGER);
        auxMapFile.delete();
    }

    public static AuxMap getAuxMap(AuxLogic db, Set<MatchFieldEnum> attrs, boolean delete_existing) {
        if (delete_existing) {
            deleteAuxMap(attrs);
        }
        if (auxMapExists(attrs)) {
            return loadAuxMapFromFile(attrs);
        } else {

            if (delete_existing && auxMapExists(attrs)) {
                throw new RuntimeException("Auxmap exists after deletion?");
            }

            AuxMap aux = db.constructAuxMap(attrs);

            // Whenever an auxmap is newly created update the manager
            saveAuxMapToFile(aux);
            return aux;
        }
    }
    public static AuxMap getAuxMap(AuxLogic db, Set<MatchFieldEnum> attrs) {
        return getAuxMap(db, attrs, false);
    }


    /**
     * Updates all auxmaps to reflect the addition of a record to the database
     * @param rs    A ResultSet of length 1 and which includes the necessary columns (not all necessarily populated with
     *              "known" values) for every value of MatchField.
     */
    public static void hookAddRecord(ResultSet rs) {

        JSONObject auxmapManager = getOrCreateMapManager();

        try {
            while (rs.next()) {
                long uid = (long) MatchFieldEnum.UID.getFieldValue(rs).getValue();

                for (Object filename : auxmapManager.keySet()) {
                    AuxMap auxmap = loadAuxMapFromFilename((String) filename);
                    AggregateResultType attrResults = new AggregateResultType(auxmap.getAttrs(), rs);

                    if (!attrResults.isUnknown()) {
                        auxmap.addPair(uid, HashUtils.hashFields(attrResults.getValues()));
                    }
                    saveAuxMapToFile(auxmap);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes the record associated with uid from all aux maps.
     *
     * @param uid   The uid of record to be removed
     */
    public static void hookRemoveRecord(long uid) {
        JSONObject auxmapManager = getOrCreateMapManager();
        for (Object filename : auxmapManager.keySet()) {
            AuxMap auxmap = loadAuxMapFromFilename((String) filename);
            auxmap.removeByID(uid);
            saveAuxMapToFile(auxmap);
        }
    }
}
