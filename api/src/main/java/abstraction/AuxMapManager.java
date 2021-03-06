package abstraction;

import Constants.InternalConstants;
import Constants.Config;

import java.sql.SQLException;
import java.nio.channels.FileLock;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.UnsignedLongs;
import hashing.HashUtils;
import com.google.gson.*;

import java.io.*;
import java.util.*;

/**
 * In order for the deduplication API to be performant, it is efficient to store AuxMaps on disk load/modify them when
 * needed instead of continually traversing and re-hashing every entry in the database. This class handles the logic
 * associated with managing local AuxMap files.
 *
 * This class contains many functions which are public for testing purposes, but shouldn't need to be used by an API
 * user. These functions are marked with the VisibleForTesting tag.
 *
 * It would actually be appropriate (and probably simpler) to implement this via a database. However our team was unsure
 * of how to do this, and so has left this task undone.
 */
public class AuxMapManager {

    private static String AUXMAP_MANAGER = Config.AUX_DATA_ROOT + InternalConstants.MANAGER_FILE_NAME;
    public static String getDataRoot() { return Config.AUX_DATA_ROOT; }

    /////////////////////////////////////////////////////////////////////////////
    // Section 1                                                               //
    // ----------------------------------------------------------------------- //
    // Functions which are indeed meant to be exposed to API users. These      //
    // include the addition & deletion hooks and AuxMap loading functions      //
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Same as the two-parameter version of getAuxMap, but with the option to overwrite the existing AuxMap
     * (if it exists) with a freshly-created one.
     *
     * @param db                    An active database connection
     * @param attrs                 The set of MatchFields for which to create an AuxMap
     * @param delete_existing       Whether to overwrite the existing AuxMap, if it exists, with a freshly-created one
     * @return                      A populated AuxMap for "attrs"
     */
    public static AuxMap getAuxMap(DbAuxConstructor db, Set<MatchFieldEnum> attrs, boolean delete_existing) {
        if (delete_existing) {
            removeFromAuxManager(attrs);
        }
        if (auxMapExists(attrs)) {
            return loadAuxMapFromAttrs(attrs);
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

    /**
     * Get a populated AuxMap for the given database and set of attributes, loading it from disk if it exists there or
     * creating and saving it if it does not.
     *
     * @param db        An active database connection
     * @param attrs     The set of MatchFields for which to create an AuxMap
     * @return          A populated AuxMap for "attrs"
     */
    public static AuxMap getAuxMap(DbAuxConstructor db, Set<MatchFieldEnum> attrs) {
        return getAuxMap(db, attrs, false);
    }

    /**
     * Updates all auxmaps to reflect the addition of a record to the database
     *
     * TODO The current model of loading/saving the files every single time a record is added or deleted is slow
     * and inefficient. A better solution would be to insert addition/deletion operations into a job queue, and "flush"
     * (ie actually perform the operations) in said queue every time getAuxMap is called or the server is shut down.
     * This would ensure that the AuxMap files are loaded into memory only as often as needed.
     *
     * Unfortunately our team ran out of time to implement this specific feature, but it should be done before this
     * API is used in production.
     *
     * @param al                the database
     * @param uid               the uid added
     * @throws SQLException     When retrieving attribute values from the database fails
     */
    public static synchronized void hookAddRecord(DbAuxConstructor al, long uid) throws SQLException {
        JsonObject auxmapManager = loadAuxManager();
        for(Object filename : auxmapManager.keySet()) {
            AuxMap auxmap = loadAuxMapFromFile((String) filename);
            auxmap.addPair(uid, HashUtils.hashFields(al.getFieldsById(uid, auxmap.getAttrs()))); //Runs one get per auxmap
            saveAuxMapToFile(auxmap);
        }
    }

    /**
     * Removes the record associated with uid from all aux maps.
     *
     * TODO See to-do item in "hookAddRecord"
     *
     * @param uid   The uid of record to be removed
     */
    public static synchronized void hookRemoveRecord(long uid) {
        JsonObject auxmapManager = loadAuxManager();
        for (Object filename : auxmapManager.keySet()) {
            AuxMap auxmap = loadAuxMapFromFile((String) filename);
            auxmap.removeByID(uid);
            saveAuxMapToFile(auxmap);
        }
    }

    /**
     * Perform some basic cleanup operations on the storage directory: ie deletion of orphan .auxmap files which are
     * not recorded in manager.json and removing entries from manager.json which describe .auxmap files which no longer
     * exist
     */
    public static synchronized void cleanup() {
        JsonObject manager = loadAuxManagerFromFile();

        // Ensure that all AuxMaps described by the manager still exist
        for (String fnameAux : manager.keySet()) {
            File auxFile = new File(Config.AUX_DATA_ROOT + fnameAux);
            if (!auxFile.exists()) {
                manager.remove(fnameAux);
            }
        }

        // Ensure that there are no orphan AuxMap files cluttering the directory and taking up space, but
        // make sure not to delete the manager file.
        File directory = new File(Config.AUX_DATA_ROOT);
        for (File file : directory.listFiles()) {
            if (file.isFile() && file.getName().endsWith(InternalConstants.AUX_FILE_EXTENSION)) {
                if (!manager.keySet().contains(file.getName())) {
                    file.delete();
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    // Section 2                                                               //
    // ----------------------------------------------------------------------- //
    // Functions which are not meant to be exposed to API users, and which     //
    // mostly deal with loading AuxMaps from and storing them into files       //
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Condenses a set of MatchFieldEnum into a the filename for the corresponding AuxMap
     *
     * Since the underlying mechanism is a hash, it is theoretically possible that names could conflict.
     * The chances of this happening are very low, about one in a hundred thousand for any "reasonable" number of
     * AuxMaps. In all likelihood, the server would run out of space for AuxMaps before encountering a collision.
     *
     * Regardless, if absolute safety is preferred then this function could always simply return something like
     * attrs.toString() instead (at the cost of long file names for the AuxMaps, of course).
     *
     * @param attrs     A set of MatchFieldEnum
     * @return          The filename for attrs
     */
    @VisibleForTesting
    public static String mfieldSetToFilename(final Set<MatchFieldEnum> attrs) {

        List<MatchFieldEnum> attrList = new ArrayList<>(attrs);
        Collections.sort(attrList);
        List<String> attrNames = new ArrayList<>();
        for (MatchFieldEnum mfield : attrList) {
            attrNames.add(mfield.toString());
        }
        String attrHash = UnsignedLongs.toString(
                Integer.toUnsignedLong(String.join("+", attrNames).hashCode())
        );
        return attrHash + "." + InternalConstants.AUX_FILE_EXTENSION;
    }

    /**
     * Check whether an auxmap file exists for the given set of MatchFieldEnums
     *
     * @param attrs     A set of MatchFieldEnum
     * @return          Whether the file exists
     */
    @VisibleForTesting
    public static boolean auxMapExists(final Set<MatchFieldEnum> attrs) {
        File auxMapFile = new File(Config.AUX_DATA_ROOT + mfieldSetToFilename(attrs));
        return auxMapFile.exists();
    }

    /**
     * Save an AuxMap object to the filesystem
     *
     * @param aux   An AuxMap object
     */
    @VisibleForTesting
    public static synchronized void saveAuxMapToFile(AuxMap aux) {

        removeFromAuxManager(aux.getAttrs());

        try {
            File auxMapFile = new File(Config.AUX_DATA_ROOT + mfieldSetToFilename(aux.getAttrs()));
            if (auxMapFile.getParentFile() != null) {
                auxMapFile.getParentFile().mkdirs();
            }

            FileOutputStream fos = new FileOutputStream(auxMapFile);
            FileLock lock = fos.getChannel().lock();   //blocks until obtained
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(aux);
            oos.close();

            addToAuxManager(aux);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static AuxMap loadAuxMapFromFile(final String filename){
        try {
            FileInputStream fin = new FileInputStream(Config.AUX_DATA_ROOT + filename);
            FileLock lock = fin.getChannel().lock(0L, Long.MAX_VALUE, true);
            ObjectInputStream ois = new ObjectInputStream(fin);
            AuxMap aux = (AuxMap) ois.readObject();
            ois.close();
            return aux;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    public static AuxMap loadAuxMapFromAttrs(Set<MatchFieldEnum> attrs) {
        return loadAuxMapFromFile(mfieldSetToFilename(attrs));
    }

    /////////////////////////////////////////////////////////////////////////////
    // Section 3                                                               //
    // ----------------------------------------------------------------------- //
    // Functions which deal with manipulating the manager json object directly //
    // Although some are marked public so that they can be tested, you should  //
    // never need to call any of these yourself                                //
    /////////////////////////////////////////////////////////////////////////////

    /**
     * Load the manager object from the filesystem, assuming it exists
     *
     * @return  The manager json object
     */
    private static JsonObject loadAuxManagerFromFile() {
        try (FileReader reader = new FileReader(AUXMAP_MANAGER)) {
            return (new Gson()).fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the Json object stored in "manager.json" if it exists, otherwise create a new AuxMap object.
     *
     * @return      The Json object describing the currently cached AuxMaps
     */
    private static JsonObject loadAuxManager() {
        File managerFile = new File(AUXMAP_MANAGER);
        if (managerFile.exists()) {
            return loadAuxManagerFromFile();
        } else {
            return new JsonObject();
        }
    }

    /**
     * Update the AuxMap manager file with new contents
     *
     * @param manager   The Json object to be saved
     */
    private synchronized static void saveAuxManagerToFile(JsonObject manager) {

        File auxMapFile = new File(AUXMAP_MANAGER);
        auxMapFile.delete();

        try {
            File managerFile = new File(AUXMAP_MANAGER);
            if (managerFile.getParentFile() != null){
                managerFile.getParentFile().mkdirs();
            }
            FileWriter managerFileWriter = new FileWriter(managerFile);
            (new Gson()).toJson(manager, managerFileWriter);
            managerFileWriter.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add a new AuxMap to the manager file on disk
     * @param auxMap    The AuxMap
     */
    @VisibleForTesting
    public static synchronized void addToAuxManager(AuxMap auxMap){
        JsonObject manager = loadAuxManager();
        String fileName = mfieldSetToFilename(auxMap.getAttrs());

        JsonArray attrNames = new JsonArray();

        for (MatchFieldEnum mfield : auxMap.getAttrs()) {
            attrNames.add(mfield.toString());
        }

        manager.add(fileName, attrNames);
        saveAuxManagerToFile(manager);
    }

    /**
     * Updates the manager to reflect the removal of a specific AuxMap
     *
     * @param attrs     Attrs for the AuxMap to delete
     */
    @VisibleForTesting
    public static synchronized void removeFromAuxManager(final Set<MatchFieldEnum> attrs) {

        JsonObject manager = loadAuxManager();
        manager.remove(mfieldSetToFilename(attrs));
        saveAuxManagerToFile(manager);
        cleanup();
    }
}
