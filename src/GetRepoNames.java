import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetRepoNames {

//    public ArrayList<String> getReposByKeywords (int resultLimit, String [] keywords) {
//        ArrayList results = new ArrayList();
//
//        return results;
//    }

    public ArrayList<String> getReposByStars (int resultLimit) {
        ArrayList results = new ArrayList();
        RepoTraversal util = new RepoTraversal(); //The makeGetRequest() should be moved into its own class to avoid implementation here

        try {
            JSONObject result = util.makeGetRequest("https://api.github.com/search/repositories?" +
                    "q=language:java&sort=stars&order=desc&per_page=" + resultLimit + "&page=1");
            JSONArray jsonArray = result.getJSONArray("items");
            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                JSONObject objectInArray = jsonArray.getJSONObject(i);
                String r = "https://api.github.com/repos/" + objectInArray.getString("full_name");
                r += util.getDefaultBranch(r);
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

