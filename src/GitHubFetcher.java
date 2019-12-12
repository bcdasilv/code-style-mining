import config.Config;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GitHubFetcher {
    public JSONObject makeGetRequest(String urlString) throws CustomException {
        try {
            String authToken = Config.getAuthToken();
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
            conn.setRequestProperty("User-Agent", "code-style-mining");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while((inputLine = in.readLine()) != null) {
                response.append(inputLine + "\n");
            }
            in.close();
            return new JSONObject(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new CustomException("Could not make get request.");
    }

    public String getDefaultBranch(String url) {
        StringBuilder sb = new StringBuilder();
        sb.append("/branches/");
        try {
            JSONObject response = makeGetRequest(url);
            sb.append(response.getString("default_branch"));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
