package abstraction;

import java.sql.SQLException;
import java.nio.channels.FileLock;

import hashing.HashUtils;
import com.google.gson.*;

import java.io.*;
import java.util.*;

public class AuxMapManager {
    //TODO make a static lock for AuxMapManager instead of making everyting synchronized

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
        //TODO this might conflict because its a hashcode. Change to either use full names,
        //or the enum index separated by underscores or something. The first way is more robust
        //and human readable but could make really long file names. The second way could get broken
        //when a new field is added in the enum definition.
        String attrHash = Integer.toString(Math.abs(String.join("__", attrNames).hashCode()));
        return Constants.AUX_DATA_ROOT + attrHash + ".auxmap";
    }

    private static JsonObject getOrCreateMapManager() {

        File managerFile = new File(AUXMAP_MANAGER);
        if (managerFile.exists()) {
            return loadManagerFromFile();
        } else {
            return new JsonObject();
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

        hookManagerDeleteMap(aux.getAttrs()); //TODO this isn't the safe way to do this
        //We should be creating it under a temp name, then deleting the old one
        //and changing the new one's name to the old one.

        try {
            File auxMapFile = new File(mfieldSetToFilename(aux.getAttrs()));
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

    public synchronized static void saveManagerToFile(JsonObject manager){
        deleteAuxMapManager(); //TODO see comment from saveAuxMapToFile

        try {
            File managerFile = new File(AUXMAP_MANAGER);
            if (managerFile.getParentFile() != null){
                managerFile.getParentFile().mkdirs();
            }
            FileWriter managerFileWriter = new FileWriter(managerFile);
            //No lock for this one because it's synchronized
            (new Gson()).toJson(manager, managerFileWriter);
            managerFileWriter.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void hookManagerAddMap(AuxMap auxMap){
        JsonObject manager = getOrCreateMapManager();
        String fileName = mfieldSetToFilename(auxMap.getAttrs());

        JsonArray attrNames = new JsonArray();

        for (MatchFieldEnum mfield : auxMap.getAttrs()) {
            attrNames.add(mfield.toString());
        }

        manager.add(fileName, attrNames);
        saveManagerToFile(manager);
    }

    private static JsonObject loadManagerFromFile(){
        try (FileReader reader = new FileReader(AUXMAP_MANAGER)) {
            return (new Gson()).fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static AuxMap loadAuxMapFromFilename(final String filename){
        try {
            FileInputStream fin = new FileInputStream(filename);
            FileLock lock = fin.getChannel().lock(0L, Long.MAX_VALUE, true); //TODO why are there arguments here but not for the other auxmap lock?
            ObjectInputStream ois = new ObjectInputStream(fin);
            AuxMap aux = (AuxMap) ois.readObject();
            ois.close();
            return aux;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static AuxMap loadAuxMapFromFile(final Set<MatchFieldEnum> attrs) {
        return loadAuxMapFromFilename(mfieldSetToFilename(attrs));
//        try {
//            FileInputStream fin = new FileInputStream(mfieldSetToFilename(attrs));
//            FileLock lock = fin.getChannel().lock(0L, Long.MAX_VALUE, true);
//            ObjectInputStream ois = new ObjectInputStream(fin);
//            AuxMap aux = (AuxMap) ois.readObject();
//            ois.close();
//
//            aux.ensureThreadSafe();
//            return aux;
//        } catch (IOException | ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
    }


    public static synchronized void hookManagerDeleteMap(final Set<MatchFieldEnum> attrs) {

        File auxMapfile = new File(mfieldSetToFilename(attrs));
        auxMapfile.delete();

        JsonObject manager = getOrCreateMapManager();
        String fileName = mfieldSetToFilename(attrs);

        manager.remove(fileName);
        saveManagerToFile(manager);
    }

    private static synchronized void deleteAuxMapManager(){
        File auxMapFile = new File(AUXMAP_MANAGER);
        auxMapFile.delete();
    }

    public static AuxMap getAuxMap(AuxLogic db, Set<MatchFieldEnum> attrs, boolean delete_existing) {
        if (delete_existing) {
            hookManagerDeleteMap(attrs);
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


    /*
    public static synchronized void hookAddRecord(ResultSet rs) {

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

    public static synchronized void hookAddRecord(String columns, String values) {
        JSONObject auxmapManager = getOrCreateMapManager();
        String[] columnArr = columns.split(",");
        String[] valueArr = values.split(",");
        assert(columnArr.length == valueArr.length);
        for(int i = 0; i < columnArr.length; i++) {
            columnArr[i] = columnArr[i].trim();
            valueArr[i] = valueArr[i].trim();
        }
        long uid = (long) MatchFieldEnum.UID.getFieldValue(columnArr, valueArr).getValue();
        for (Object filename : auxmapManager.keySet()) {
            AuxMap auxmap = loadAuxMapFromFilename((String) filename);
            AggregateResultType attrResults = new AggregateResultType(auxmap.getAttrs(), columnArr, valueArr);

            if (!attrResults.isUnknown()) {
                auxmap.addPair(uid, HashUtils.hashFields(attrResults.getValues()));
            }
            saveAuxMapToFile(auxmap);
        }
    }
     */

    /**
     * Updates all auxmaps to reflect the addition of a record to the database
     * @param al    the database
     * @param uid   the uid added
     * @throws SQLException
     */
    public static synchronized void hookAddRecord(AuxLogic al, long uid) throws SQLException {
        JsonObject auxmapManager = getOrCreateMapManager();
        for(Object filename : auxmapManager.keySet()) {
            AuxMap auxmap = loadAuxMapFromFilename((String) filename);
            auxmap.addPair(uid, HashUtils.hashFields(al.getFieldsById(uid, auxmap.getAttrs()))); //Runs one get per auxmap
            saveAuxMapToFile(auxmap);
        }
    }

    /**
     * Removes the record associated with uid from all aux maps.
     *
     * @param uid   The uid of record to be removed
     */
    public static synchronized void hookRemoveRecord(long uid) {
        JsonObject auxmapManager = getOrCreateMapManager();
        for (Object filename : auxmapManager.keySet()) {
            AuxMap auxmap = loadAuxMapFromFilename((String) filename);
            auxmap.removeByID(uid);
            saveAuxMapToFile(auxmap);
        }
    }
}
