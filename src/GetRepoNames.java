import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetRepoNames {
    public ArrayList<String> getReposByKeywords (int resultLimit, ArrayList<String> keywords) {
        ArrayList results = new ArrayList();
        GitHubFetcher fetcher = new GitHubFetcher();

        try {
            double originalLimit = new Double (resultLimit);

            for (String word : keywords) {
                //Mixes up the categories a bit among the keywords
                int numRepos = (int) Math.ceil(originalLimit / keywords.size());
                numRepos = (numRepos > resultLimit)? resultLimit : numRepos;
                resultLimit -= numRepos;

                if (numRepos == 0) {
                    break;
                }

                JSONObject result = fetcher.makeGetRequest("https://api.github.com/search/repositories?q=" +
                        word + "+language:java&sort=stars&order=desc&per_page=" + numRepos + "&page=1");
                JSONArray jsonArray = result.getJSONArray("items");
                for (int i = 0, size = jsonArray.length(); i < size; i++) {
                    JSONObject objectInArray = jsonArray.getJSONObject(i);
                    String r = "https://api.github.com/repos/" + objectInArray.getString("full_name");
                    r += fetcher.getDefaultBranch(r);
                    results.add(r);
                }
            }
        } catch (CustomException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return results;
    }

    public ArrayList<String> getReposByStars (int resultLimit) {
        ArrayList results = new ArrayList();
        GitHubFetcher fetcher = new GitHubFetcher();

        try {
            JSONObject result = fetcher.makeGetRequest("https://api.github.com/search/repositories?" +
                    "q=language:java&sort=stars&order=desc&per_page=" + resultLimit + "&page=1");
            JSONArray jsonArray = result.getJSONArray("items");
            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                JSONObject objectInArray = jsonArray.getJSONObject(i);
                String r = "https://api.github.com/repos/" + objectInArray.getString("full_name");
                r += fetcher.getDefaultBranch(r);
                results.add(r);
            }
        } catch (CustomException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return results;
    }
}

