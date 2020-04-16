package server;
import java.sql.SQLException;
import java.util.*;

import abstraction.AuxMapManager;
import abstraction.MatchFieldEnum;

import algorithm.Deduplication;
import com.google.gson.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin
public class DedupAPIController {

    static Set<MatchFieldEnum> nameArrayToMatchFieldSet(JsonArray payload) {
        Set<MatchFieldEnum> subconfig = new HashSet<>();

        for (JsonElement el : payload) {
            subconfig.add(MatchFieldEnum.valueOf(el.getAsString()));
        }

        return subconfig;
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

    @GetMapping("deduplicate")
    public String deduplicate(@RequestParam("data") String data) throws SQLException {

        JsonArray payload_ls = (new Gson()).fromJson(data, JsonArray.class);

        List<Set<MatchFieldEnum>> config_ls = new ArrayList<>();

        for (JsonElement el : payload_ls) {
            config_ls.add(nameArrayToMatchFieldSet(el.getAsJsonArray()));
        }

        Set<Set<Long>> duplicates = Deduplication.getMatchingMerged(RestServiceApplication.database, config_ls);

        return (new Gson()).toJson(duplicates);
    }
}
