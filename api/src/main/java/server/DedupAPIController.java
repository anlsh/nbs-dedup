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

    @PostMapping("create_subconfig")
    public Boolean create_subconfig(@RequestParam(value = "data") String data) {

        Gson gson = new Gson();
        JsonArray payload = gson.fromJson(data, JsonArray.class);
        Set<MatchFieldEnum> subconfig = nameArrayToMatchFieldSet(payload);

        AuxMapManager.getAuxMap(RestServiceApplication.database, subconfig);
        return true;
    }

    @PostMapping("delete_subconfig")
    public Boolean delete_subconfig(@RequestParam(value = "data") String data) {

        Gson gson = new Gson();
        JsonArray payload = gson.fromJson(data, JsonArray.class);
        Set<MatchFieldEnum> subconfig = nameArrayToMatchFieldSet(payload);

        AuxMapManager.removeFromAuxManager(subconfig);
        return true;
    }

    @GetMapping("deduplicate_merged")
    public String deduplicate(@RequestParam("data") String data) throws SQLException {

        Set<Set<Long>> duplicates = Deduplication.getMatchingMerged(
                RestServiceApplication.database,
                configFromString(data)
        );

        return (new Gson()).toJson(duplicates);
    }
}
