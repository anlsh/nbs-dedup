package abstraction;

import java.sql.*;
import java.util.*;
import java.io.*;

import com.google.common.collect.Lists;
import hashing.HashUtils;

public class NBS_DB {

    public Connection conn;

    public NBS_DB(String server, int port, String dbName, String username, String password) throws SQLException {
        String connectionUrl = "jdbc:sqlserver://" + server + ":" + port + ";databaseName=" + dbName
                + ";user=" + username +  ";password=" + password;

        conn = DriverManager.getConnection(connectionUrl);
    }



    public List<Map<Long, Long>> constructAuxTable(final Set<MatchFieldEnum> attrs) throws SQLException {

        Set<String> requiredColumns = new HashSet<String>();
        List<MatchFieldEnum> attrList = new ArrayList<MatchFieldEnum>(attrs);
        Collections.sort(attrList);
        String attributes_str = "";
        for (MatchFieldEnum mfield : attrList) {
            requiredColumns.addAll(MatchFieldUtils.getRequiredColumns(mfield));
            attributes_str += mfield.toString() + "_";
        }
        String id2fieldName = "data/id_to_" + attributes_str + ".ser";
        String field2idName = "data/" + attributes_str + "_to_id.ser";

        requiredColumns.add(Constants.COL_PERSON_UID);

        Statement query = conn.createStatement();
        ResultSet rs = query.executeQuery(
                "SELECT " + String.join(",", Lists.newArrayList(requiredColumns)) +
                        " from Person"
        );

        HashMap<Long, Long> auxTable_attrToId = new HashMap<Long, Long>();
        HashMap<Long, Long> auxTable_idToAttr = new HashMap<Long, Long>();

        // TODO This methodology is sourced from https://stackoverflow.com/questions/7507121/efficient-way-to-handle-resultset-in-java
        // But should be abstracted using a standard library like DBUtils or MapListHandler
        while (rs.next()) {
            HashMap attr_map = new HashMap<MatchFieldEnum, Object>();

            for (MatchFieldEnum mfield : attrs) {
                attr_map.put(mfield, MatchFieldUtils.getFieldValue(rs, mfield));
            }

            auxTable_idToAttr.put(
                    (long) MatchFieldUtils.getFieldValue(rs, MatchFieldEnum.UID),
                    //HashUtils.hashFields(attr_map)
                    1000L
            );

            auxTable_attrToId.put(
                    //HashUtils.hashFields(attr_map)
                    1000L,
                    (long) MatchFieldUtils.getFieldValue(rs, MatchFieldEnum.UID)
            );
        }


        //serialize hashmap locally
        try
        {
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
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        return List.of(auxTable_idToAttr, auxTable_attrToId);
    }
}
