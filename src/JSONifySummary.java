import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This class was created to provide a summary of the whole project.
 * The idea is that it will loop through a local file that has all the JSON objects of each file from the repo.
 * It'll iterate through this file and create a summary JSON object.
 */
public class JSONifySummary {
    ArrayList<JSONObject> fileJSONObjects = new ArrayList<>();

    public JSONifySummary() {

    }

    public void addObject(JSONObject o) {
        fileJSONObjects.add(o);
    }
}
