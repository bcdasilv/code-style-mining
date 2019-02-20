import java.io.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;

public class RepoTraversal {
    private static final Config config = Config.getInstance();
    private static final String tempFilePath = config.getTempFilePath();

    public void findJavaFilesToParse() {
        ArrayList<String> repoURLs = getRepoURLsFromConfig();
        for(String url : repoURLs) {
            traverseRepoForFileContent(url);
        }
    }

    public void storeFileLocally(String content) {
        FileOutputStream fos = null;
        File file;
        try {
            file = new File(tempFilePath);
            if(!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            byte[] bytesArray = content.getBytes();
            fos.write(bytesArray);
            fos.flush();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            }
            catch(IOException e) {
                System.out.println("Error in closing the Stream");
            }
        }
    }

    private void decodeAndParseFile(String content) {
        try {
            FileParser fp = new FileParser();
            byte[] valueDecoded = Base64.decodeBase64(content);
            storeFileLocally(new String(valueDecoded));
            fp.parseFile();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void traverseRepoForFileContent(String repoURL) {
        try {
            ArrayList<String> urls = traverseTreeForFileURLs(repoURL);
            for(String url : urls) {
                JSONObject content = makeGetRequest(url);
                String contentStr = content.getString("content");
                contentStr = contentStr.replaceAll("\n", "");
                decodeAndParseFile(contentStr);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> traverseTreeForFileURLs(String repoURL) {
        JSONObject treeObj = getTreeObjectFromRepo(repoURL);
        ArrayList<String> urls = new ArrayList<>();
        try {
            String treeURL = treeObj.getString("url");
            JSONObject tree = makeGetRequest(treeURL + "?recursive=1");
            JSONArray array = getJSONArrayByKey(tree, "tree");

            for(int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String path = obj.getString("path");
                String type = obj.getString("type");
                String contentURL = obj.getString("url");

                if(path.contains(".java") && (type.contains("blob"))) {
                    urls.add(contentURL);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return urls;
    }

    private JSONObject getTreeObjectFromRepo(String url) {
        try {
            JSONObject response = makeGetRequest(url);
            String[] keys = new String[] { "commit", "commit", "tree" };
            return recurseForJSONObject(response, keys);
        } catch(Exception e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    private JSONArray getJSONArrayByKey(JSONObject source, String key) {
        try {
            return source.getJSONArray(key);
        } catch(Exception e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    private JSONObject getJSONObjectByKey(JSONObject source, String key) {
        try {
            return source.getJSONObject(key);
        } catch(Exception e) {
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    private JSONObject recurseForJSONObject(JSONObject source, String[] keys) {
        JSONObject object = source;
        try {
            for(String k : keys) {
                object = getJSONObjectByKey(object, k);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    private JSONObject makeGetRequest(String urlString) {
        try {
            String authToken = config.getAuthToken();
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
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
        throw new NullPointerException();
    }

    private ArrayList<String> getRepoURLsFromConfig() {
        String repoURLsPath = config.getRepoURLsPath();
        ArrayList<String> urls = new ArrayList<>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(repoURLsPath));
            String line;
            while((line = br.readLine()) != null)   {
                StringBuilder sb = new StringBuilder();
                sb.append("https://api.github.com/repos/");
                sb.append(line);
                sb.append("/branches/master");
                urls.add(sb.toString());
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return urls;
    }
}
