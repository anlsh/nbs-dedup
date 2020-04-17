package server;
import java.sql.SQLException;
import java.util.*;

import abstraction.AuxMapManager;
import abstraction.MatchFieldEnum;

import algorithm.Deduplication;
import com.google.gson.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Class specifying INSECURE rest endpoints for the deduplication API. Documentation is provided in the provided
 * PostMan project.
 */
@RestController
@CrossOrigin
public class DedupAPIController {

    /**
     * Given a Json list of strings, parse the corresponding Java set of MatchFieldEnums
     * @param payload   A JsonArray of strings, each element of which is the name of a MatchFieldEnum value
     * @return
     */
    private static Set<MatchFieldEnum> nameArrayToMatchFieldSet(JsonArray payload) {
        Set<MatchFieldEnum> subconfig = new HashSet<>();

        for (JsonElement el : payload) {
            subconfig.add(MatchFieldEnum.valueOf(el.getAsString()));
        }

        return subconfig;
    }

    /**
     * Given a string argument representing a configuration (list of subconfigs), parse the corresponding Java argument
     * @param data  An (encoded) json list of list of strings.
     * @return
     */
    static List<Set<MatchFieldEnum>> configFromString(String data) {
        JsonArray payload_ls = (new Gson()).fromJson(data, JsonArray.class);

        List<Set<MatchFieldEnum>> config_ls = new ArrayList<>();

        for (JsonElement el : payload_ls) {
            config_ls.add(nameArrayToMatchFieldSet(el.getAsJsonArray()));
        }
        return config_ls;
    }

    /**
     * This request provides a list of fields which a user can select to deduplicate on. This list may change over time,
     * so instead of being hard-coded in the UI it is retrieved dynamically from the API
     *
     * Specification
     * -------------
     * A list of entries describing deduplicable fields is returned. Each entry has the following subfields
     *
     * -- "attr_code":  A string with a unique identifier for this attribute to be used in other REST calls
     * -- "parent":     Describes hierarchical information relevant to how attributes which are displayed in the UI. If
     *                  null, then the attribute is "top-level". If non-null, then it is the attr_code of the parent
     *                  attribute. For instance, an attribute describing a first initial is logically a child of the
     *                  first name attribute, and would thus have a "parent" value of "FIRST_NAME"
     * -- "desc":       A string which gives a human-readable name for the attribute which will be displayed in the UI.
     * @return          The Json array described above
     */
    @GetMapping("get_dedup_flags")
    public String get_dedup_flags() {

        JsonArray retObject = new JsonArray();
        for (MatchFieldEnum mfield : MatchFieldEnum.values()) {
            if (!mfield.isDeduplicableField()) {
                continue;
            }
            JsonObject curr_obj = new JsonObject();
            curr_obj.add("attr_code", new JsonPrimitive(mfield.name()));
            curr_obj.add("parent",
                    mfield.getParent() == null ? new JsonNull() : new JsonPrimitive(mfield.getParent().name()));
            curr_obj.add("desc", new JsonPrimitive(mfield.getHumanReadableName()));

            retObject.add(curr_obj);
        }
        return retObject.toString();
    }

    /**
     * Creates and caches an AuxMap tracking the given subconfig in the server's AuxMapManager.
     *
     * Specification
     * -------------
     * Takes a single parameter ("data"), specifying a subconfig. Passed as a list of attr_codes (see get_dedup_flags)
     *
     * # Returns
     *
     * @param data      A list of attr_code strings: example ["first_nm", "last_nm"]
     * @return          true
     */
    @PostMapping("create_subconfig")
    public Boolean create_subconfig(@RequestParam(value = "data") String data) {

        Gson gson = new Gson();
        JsonArray payload = gson.fromJson(data, JsonArray.class);
        Set<MatchFieldEnum> subconfig = nameArrayToMatchFieldSet(payload);

        AuxMapManager.getAuxMap(RestServiceApplication.database, subconfig);
        return true;
    }

    /**
     * Deletes the AuxMap tracking the given subconfig from the server's AuxMapManager.
     *
     * Specification
     * -------------
     * Takes a single parameter ("data"), specifying a subconfig. Passed as a list of attr_codes (see get_dedup_flags)
     *
     * # Returns
     *
     * @param data      A list of attr_code strings: example ["first_nm", "last_nm"]
     * @return          true
     */
    @PostMapping("delete_subconfig")
    public Boolean delete_subconfig(@RequestParam(value = "data") String data) {

        Gson gson = new Gson();
        JsonArray payload = gson.fromJson(data, JsonArray.class);
        Set<MatchFieldEnum> subconfig = nameArrayToMatchFieldSet(payload);

        AuxMapManager.removeFromAuxManager(subconfig);
        return true;
    }


    /**
     * Deduplicate using the config given by the "data" parameter (creating and storing corresponding AuxMaps in
     * AuxMapManager if they don't yet exist), and return a list of record groupings. The groupings returned by this
     * function are merged rather aggressively: see documentation for the getMatchingMerged function.
     *
     * Specification
     * --------------
     * Takes a single parameter ("data"), specifying a config. Passed as a list of  subconfigs, where each subconfig
     * is a list of attr_codes (see get_dedup_flags)

     * Returns
     * A Json list of ID groupings, where groupings of more than one element indicate that the IDs in question have
     * been marked as duplicates.
     *
     * Example return value:
        [[104, 98, 337], [42], [1000, 800, 496, 312], ...]

     * @param data
     * @return
     * @throws SQLException
     */
    @GetMapping("deduplicate_merged")
    public String deduplicate(@RequestParam("data") String data) throws SQLException {

        Set<Set<Long>> duplicates = Deduplication.getMatchingMerged(
                RestServiceApplication.database,
                configFromString(data)
        );

        return (new Gson()).toJson(duplicates);
    }
}
