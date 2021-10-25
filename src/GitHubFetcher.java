import config.Config;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

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
            
            if (conn.getResponseCode() == 403) {
                reachedAPIRateLimit();
                makeGetRequest(urlString);
            }

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

    public void reachedAPIRateLimit() throws CustomException {
        String urlString = "https://api.github.com/rate_limit";
        try {
            JSONObject respJSON = makeGetRequest(urlString);
            int coreRemaining = respJSON.getJSONObject("resources").getJSONObject("core").getInt("remaining");
            int searchRemaining = respJSON.getJSONObject("resources").getJSONObject("search").getInt("remaining");
            if (coreRemaining == 0) {
                int resetTime = respJSON.getJSONObject("resources").getJSONObject("core").getInt("reset");
                int currentTime = (int) Instant.now().getEpochSecond();
                int sleepTime = resetTime - currentTime;
                if (sleepTime > 0) {
                    sleepTime += (60 * 5);
                    System.out.println(
                        "reached API rate limit of 5000 per hour... sleeping for " + sleepTime +
                        " seconds (" + (sleepTime/60) + " minutes)"
                    );
                    TimeUnit.SECONDS.sleep(sleepTime);
                }
            } else if (searchRemaining == 0) {
                System.out.println("reached minute API rate limit of 30 per minute... sleeping for 60 seconds");
                TimeUnit.SECONDS.sleep(60);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
