package abstraction;

import java.io.*;
import java.util.*;

public class AuxMap { //manages the aux maps
    public static boolean auxTableExists(final Set<MatchFieldEnum> attrs) {
        List<MatchFieldEnum> attrList = new ArrayList<MatchFieldEnum>(attrs);
        Collections.sort(attrList);
        String attributes_str = "";
        for (MatchFieldEnum mfield : attrList) {
            attributes_str += mfield.toString() + "_";
        }

        String id2fieldName = "data/id_to_" + attributes_str + ".ser";
        String field2idName = "data/" + attributes_str + "_to_id.ser";

        File file1 = new File(id2fieldName);
        File file2 = new File(field2idName);

        boolean file1Exists = false;
        boolean file2Exists = false;
        try {
            return file1.exists() && file2.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void serializeTable(Set<MatchFieldEnum> attrs, List<Map<Long, Long>> pair) {
        //serialize hashmap locally
        Map<Long, Long> auxTable_idToAttr = pair.get(0);
        Map<Long, Long> auxTable_attrToId = pair.get(1);

        String attributes_str = calculateAttrStr(attrs);
        String id2fieldName = "data/id_to_" + attributes_str + ".ser";
        String field2idName = "data/" + attributes_str + "_to_id.ser";


        try {
            FileOutputStream fos = new FileOutputStream(id2fieldName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(auxTable_idToAttr);
            oos.close();
            fos.close();
            System.out.printf("Serialized HashMap data is saved in data/id2fields.ser");

            FileOutputStream fos2 = new FileOutputStream(field2idName);
            ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
            oos2.writeObject(auxTable_attrToId);
            oos2.close();
            fos2.close();
            System.out.printf("Serialized HashMap data is saved in data/fields2id.ser");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    public static List<Map<Long, Long>> deserializeTables(Set<MatchFieldEnum> attrs) {
        List<MatchFieldEnum> attrList = new ArrayList<MatchFieldEnum>(attrs);
        Collections.sort(attrList);
        String attributes_str = "";
        for (MatchFieldEnum mfield : attrList) {
            attributes_str += mfield.toString() + "_";
        }
        String id2fieldName = "data/id_to_" + attributes_str + ".ser";
        String field2idName = "data/" + attributes_str + "_to_id.ser";

        HashMap<Long, Long> id2field = null;
        HashMap<Long, Long> field2id = null;

        try {
            FileInputStream fis = new FileInputStream(id2fieldName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            id2field = (HashMap) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return null;
        }

        try {
            FileInputStream fis = new FileInputStream(field2idName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            field2id = (HashMap) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return null;
        }

        if (id2field != null && field2id != null) {
            return List.of(id2field, field2id);
        }

        return null;
    }

    public static String calculateAttrStr(Set<MatchFieldEnum> attrs) {
        List<MatchFieldEnum> attrList = new ArrayList<MatchFieldEnum>(attrs);
        Collections.sort(attrList);
        String attributes_str = "";
        for (MatchFieldEnum mfield : attrList) {
            attributes_str += mfield.toString() + "_";
        }
        return attributes_str;
    }
}
