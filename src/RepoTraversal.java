import java.io.*;

import config.Config;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;
import util.UrlFilepathPair;

public class RepoTraversal {
    private static final Config config = Config.getInstance();
    private static final String tempFilePath = config.getTempJavaFilePath();

    private long numFiles;
    private long numJavaFiles;

    public RepoTraversal() {
    }

    public void findJavaFilesToParse() {
        ArrayList<String> repoURLs = getRepoURLsFromConfig();
        for(String url : repoURLs) {
            traverseRepoForFileContent(url);
        }
    }

    private void decodeAndParseFile(String content, String repoURL, String filePath, JSONifySummary summary) {
        try {
            FileParser fp = new FileParser();
            byte[] valueDecoded = Base64.decodeBase64(content);
            storeFileLocally(new String(valueDecoded));

            // send each file off to the FileParser class
            fp.parseFile(repoURL, filePath, summary);
        } catch(Exception e) {
            e.printStackTrace();
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

    private void traverseRepoForFileContent(String repoURL) {
        /**
         * delete
         */
        int count = 0;
        /**
         * End delete
         */

        try {
            ArrayList<UrlFilepathPair> urls = traverseTreeForFileURLs(repoURL);
            //create a summary obj for this repo
            JSONifySummary repoSummary = new JSONifySummary(numFiles, numJavaFiles);
            //UrlFilepathPair contains the blob url and the file path of the blob
            for(UrlFilepathPair url : urls) {
                JSONObject content = makeGetRequest(url.getRepoBlobUrl());
                String contentStr = content.getString("content");
                contentStr = contentStr.replaceAll("\n", "");
                decodeAndParseFile(contentStr, repoURL, url.getFilePath(), repoSummary);
            }
            //repo summary is populated with results for each file.
            //need to summarize the results into jsonobject
            JSONObject repoSummaryResults = repoSummary.writeResults();
            System.out.println(repoSummaryResults);
        } catch(Exception e) {
            e.printStackTrace();
        }
        //return summary
    }

    private ArrayList<UrlFilepathPair> traverseTreeForFileURLs(String repoURL) {
        ArrayList<UrlFilepathPair> urls = new ArrayList<>();
        try {
            JSONObject treeObj = getTreeObjectFromRepo(repoURL);
            String treeURL = treeObj.getString("url");
            JSONObject tree = makeGetRequest(treeURL + "?recursive=1");
            JSONArray array = getJSONArrayByKey(tree, "tree");

            for(int i = 0; i < array.length(); i++) {
                numFiles++;
                JSONObject obj = array.getJSONObject(i);
                String path = obj.getString("path");
                String type = obj.getString("type");
                String contentURL = obj.getString("url");

                if(path.contains(".java") && (type.contains("blob"))) {
                    numJavaFiles++;
                    urls.add(new UrlFilepathPair(contentURL, path));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return urls;
    }

    private JSONObject getTreeObjectFromRepo(String url) throws CustomException {
        try {
            JSONObject response = makeGetRequest(url);
            String[] keys = new String[] { "commit", "commit", "tree" };
            return recurseForJSONObject(response, keys);
        } catch(Exception e) {
            e.printStackTrace();
        }
        throw new CustomException("Cannot find JSON object from these keys.");
    }

    private JSONArray getJSONArrayByKey(JSONObject source, String key) throws CustomException {
        try {
            return source.getJSONArray(key);
        } catch(Exception e) {
            e.printStackTrace();
        }
        throw new CustomException("Cannot find JSON array by this key.");
    }

    private JSONObject getJSONObjectByKey(JSONObject source, String key) throws CustomException {
        try {
            return source.getJSONObject(key);
        } catch(Exception e) {
            e.printStackTrace();
        }
        throw new CustomException("Cannot find JSON object by this key.");
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

    private JSONObject makeGetRequest(String urlString) throws CustomException {
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
        throw new CustomException("Could not make get request.");
    }

    private String getDefaultBranch(String url) {
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
                sb.append(getDefaultBranch(sb.toString()));
                urls.add(sb.toString());
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return urls;
    }

    public long getNumFiles() {
        return numFiles;
    }

    public long getNumJavaFiles() {
        return numJavaFiles;
    }
}
