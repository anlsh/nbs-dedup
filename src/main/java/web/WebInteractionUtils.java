package web;

import abstraction.Constants;
import abstraction.MatchFieldEnum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Set;

public class WebInteractionUtils {
    //https://www.baeldung.com/java-http-request
    public Set<List<Set<MatchFieldEnum>>> getAllConfigs() {
        //TODO
        //TODO change config from being a List<Set<MatchFieldEnum> to a Set<Set<MatchFieldEnum>
        return null;
    }
    public String getAllConfigsAsString() {
        URL url = null;
        HttpURLConnection con = null;
        int status;
        BufferedReader in;
        StringBuffer content;
        try {
            url = new URL(Constants.WEB_SERVER + ":" + Constants.WEB_PORT + Constants.GET_CONFIGS_REQUEST);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            status = con.getResponseCode();
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            content = new StringBuffer();
            while((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();
            return content.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
