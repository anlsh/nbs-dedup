package abstraction;

import java.sql.ResultSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

import java.sql.SQLException;
import java.nio.channels.FileLock;

import hashing.HashUtils;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

public class AuxMapManager {

    private static String DATA_ROOT = "/tmp/aux-maps/";
    private static String AUXMAP_MANAGER = "/tmp/aux-maps/manager.json";

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
        return Integer.toString(attributes_str.hashCode());
    }

    private static JSONObject getOrCreateMapManager(){

        File managerFile = new File(AUXMAP_MANAGER);
        if (managerFile.exists()){
            return loadManagerFromFile();
        } else {
            JSONObject manager = new JSONObject();
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

    public static void saveAuxMapToFile(AuxMap aux) {

        deleteAuxMap(aux.attrs);

        try {
            File auxMapFile = new File(mfieldSetToFilename(aux.attrs));
            if (auxMapFile.getParentFile() != null) {
                auxMapFile.getParentFile().mkdirs();
            }


            FileOutputStream fos = new FileOutputStream(auxMapFile);
            FileLock lock = fos.getChannel().lock();   //blocks until obtained
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(aux);
            oos.close();
            lock.release();

            hookEditManager(aux);
        } catch (Exception e) {
            // TODO Exception handling
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public synchronized static void saveManagerToFile(JSONObject manager){
        deleteAuxMapManager();

        try {
            File managerFile = new File(AUXMAP_MANAGER);
            FileWriter managerFileWriter = new FileWriter(managerFile);
            if (managerFile.getParentFile() != null){
                managerFile.getParentFile().mkdirs();
            }

            managerFileWriter.write(manager.toJSONString());
            managerFileWriter.close();

        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static void hookEditManager(AuxMap auxMap){
        JSONObject manager = getOrCreateMapManager();
        String fileName = mfieldSetToFilename(auxMap.attrs);
        JSONArray attrString = new JSONArray();
        attrString.addAll(auxMap.attrs);

        manager.put(fileName, attrString);
        saveManagerToFile(manager);
    }

    public static JSONObject loadManagerFromFile(){
        try (FileReader reader = new FileReader(AUXMAP_MANAGER))
        {
            JSONParser jsonParser = new JSONParser();
            JSONObject manager = (JSONObject) jsonParser.parse(reader);

            return manager;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static AuxMap loadAuxMapFromFile(final Set<MatchFieldEnum> attrs) {

        try {
            FileInputStream fin = new FileInputStream(mfieldSetToFilename(attrs));
            FileLock lock = fin.getChannel().lock();
            ObjectInputStream ois = new ObjectInputStream(fin);
            AuxMap aux = (AuxMap) ois.readObject();
            ois.close();
            lock.release();
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

    public static AuxMap getAuxMap(NBS_DB db, Set<MatchFieldEnum> attrs, boolean delete_existing) throws SQLException {
        if (delete_existing) {
            deleteAuxMap(attrs);
        }
        if (auxMapExists(attrs)) {
            return loadAuxMapFromFile(attrs);
        } else {
            AuxMap aux = db.constructAuxMap(attrs);

            // Whenever an auxmap is newly created update the manager
            saveAuxMapToFile(aux);
            return aux;
        }
    }
    public static AuxMap getAuxMap(NBS_DB db, Set<MatchFieldEnum> attrs) throws SQLException {
        return getAuxMap(db, attrs, false);
    }

    public static void hookAddRecord(NBS_DB db, ResultSet rs, Set<MatchFieldEnum> attrs) {

        AuxMap map = getAuxMap(db, attrs);

        try {
            while (rs.next()) {
                Map<MatchFieldEnum, Object> attr_map = new HashMap<>();

                boolean include_entry = true;

                for (MatchFieldEnum mfield : attrs) {
                    Object mfield_val = mfield.getFieldValue(rs);
                    if (mfield.isUnknownValue(mfield_val)) {
                        include_entry = false;
                        break;
                    }
                    attr_map.put(mfield, mfield.getFieldValue(rs));
                }

                if (!include_entry) {
                    continue;
                } else {
                    long record_id = (long) MatchFieldEnum.UID.getFieldValue(rs);
                    HashCode hash = HashUtils.hashFields(attr_map);

                    map.getIdToHashMap().put(record_id, hash);

                    Set<Long> idsWithSameHash = map.getHashToIdMap().getOrDefault(hash, null);
                    if (idsWithSameHash != null) {
                        idsWithSameHash.add(record_id);
                    } else {
                        map.getHashToIdMap().put(hash, Sets.newHashSet(record_id));
                    }
                }
            }
        } catch (SQLException e) {
            // TODO Exception Handling
            e.printStackTrace();
            throw new RuntimeException("Error while trying to scan database entries");
        }

        saveAuxMapToFile(map);
    }

    public static void hookRemoveRecord(NBS_DB db, ResultSet rs, Set<MatchFieldEnum> attrs) {

        AuxMap map = getAuxMap(db, attrs);

        try {
            while (rs.next()) {
                Map<MatchFieldEnum, Object> attr_map = new HashMap<>();

                boolean include_entry = true;

                for (MatchFieldEnum mfield : attrs) {
                    Object mfield_val = mfield.getFieldValue(rs);
                    if (mfield.isUnknownValue(mfield_val)) {
                        include_entry = false;
                        break;
                    }
                    attr_map.put(mfield, mfield.getFieldValue(rs));
                }

                if (!include_entry) {
                    continue;
                } else {
                    long record_id = (long) MatchFieldEnum.UID.getFieldValue(rs);
                    HashCode hash = HashUtils.hashFields(attr_map);

                    map.getIdToHashMap().remove(record_id);
                    map.getHashToIdMap().remove(hash);
                }
            }
        } catch (SQLException e) {
            // TODO Exception Handling
            e.printStackTrace();
            throw new RuntimeException("Error while trying to scan database entries");
        }
        saveAuxMapToFile(map);
    }
}
