package api_server;
import java.util.*;
import java.util.stream.Stream;

import abstraction.AuxMapManager;
import abstraction.MatchFieldEnum;

import algorithm.Deduplication;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.web.bind.annotation.*;

@RestController
public class DedupAPIController {

    static Set<MatchFieldEnum> subconfig_boolmap_to_mfieldset(JsonObject payload) {
        Set<MatchFieldEnum> subconfig = new HashSet<>();

        for (MatchFieldEnum mfield : MatchFieldEnum.values()) {
            if (payload.has(mfield.name()) && payload.get(mfield.name()).getAsBoolean()) {
                subconfig.add(mfield);
            }
        }

        return subconfig;
    }

    @GetMapping("get_dedup_flags")
    public String[] get_dedup_flags() {
        return Stream.of(MatchFieldEnum.values()).map(MatchFieldEnum::name).toArray(String[]::new);
    }

    @GetMapping("get_dedup_flag_parents")
    public HashMap<String, String> get_dedup_flags_parent() {
        HashMap<String, String> flagInfoMap = new HashMap<String, String>();

        for (MatchFieldEnum mfield : MatchFieldEnum.values()) {
            flagInfoMap.put(mfield.name(),
                    mfield.getParent() == null ? null : mfield.getParent().name());
        }
        return flagInfoMap;
    }

    @GetMapping("get_dedup_flag_descs")
    public HashMap<String, String> get_dedup_flags_desc() {
        HashMap<String, String> flagInfoMap = new HashMap<String, String>();

        for (MatchFieldEnum mfield : MatchFieldEnum.values()) {
            flagInfoMap.put(mfield.name(),  mfield.getHumanReadableName());
        }
        return flagInfoMap;
    }

    @PostMapping("create_subconfig")
    public Boolean create_subconfig(@RequestParam(value = "data") String data) {

        // Working with arbitrary data kinda sucks, or maybe I'm just dumb.
        // See https://www.quora.com/Can-I-fetch-a-JSON-object-in-my-spring-controller-without-using-a-model-object
        // and https://www.baeldung.com/gson-string-to-jsonobject
        // for the resources I ended up using

        Gson gson = new Gson();
        JsonObject payload = gson.fromJson(data, JsonObject.class);

        Set<MatchFieldEnum> subconfig = subconfig_boolmap_to_mfieldset(payload);

        AuxMapManager.getAuxMap(RestServiceApplication.database, subconfig);

        return true;
    }

    @PostMapping("delete_subconfig")
    public Boolean delete_subconfig(@RequestParam(value = "data") String data) {

        Gson gson = new Gson();
        JsonObject payload = gson.fromJson(data, JsonObject.class);

        Set<MatchFieldEnum> subconfig = subconfig_boolmap_to_mfieldset(payload);
        AuxMapManager.deleteAuxMap(subconfig);

        return true;
    }

    @PostMapping("deduplicate")
    public String deduplicate(@RequestParam(value = "data") String data) {
        // Apparently the square brackets aren't allowed in posts?
        // https://stackoverflow.com/questions/11944410/passing-array-in-get-for-a-rest-call

        // I asked a question @ https://stackoverflow.com/questions/60479864/spring-boot-does-not-accept-list-params-enclosed-in-brackets

        Gson gson = new Gson();
        JsonObject[] payload_ls = gson.fromJson("["+data+"]", JsonObject[].class);

        List<Set<MatchFieldEnum>> config_ls = new ArrayList<>();

        for (int i = 0; i < payload_ls.length; ++i) {
            config_ls.add(subconfig_boolmap_to_mfieldset(payload_ls[i].getAsJsonObject()));
        }

        Set<Set<Long>> duplicates = Deduplication.getMatchingMerged(RestServiceApplication.database, config_ls);

        return duplicates.toString();
    }
}
