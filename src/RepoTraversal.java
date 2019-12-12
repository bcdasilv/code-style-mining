import com.mongodb.client.FindIterable;
import config.Config;
import mongo.MongoCollectionClient;
import org.apache.commons.codec.binary.Base64;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import util.UrlFilepathPair;

import java.io.*;
import java.util.ArrayList;

public class RepoTraversal {
    private long numFiles;
    private long numJavaFiles;
    private GitHubFetcher fetcher = new GitHubFetcher();

    public RepoTraversal() {
    }

    public void findJavaFilesToParse(String inputType, String optionType, Integer limitRepos, ArrayList<String> keywords) {
        ArrayList<String> repoURLs;

        if (inputType.equals("f")) {
            repoURLs = getRepoURLsFromConfig();
        } else {
            GetRepoNames getRepoNames = new GetRepoNames();

            if (optionType.equals("s")) {
                repoURLs = getRepoNames.getReposByStars(limitRepos);
            } else {
                repoURLs = getRepoNames.getReposByKeywords(limitRepos, keywords);
            }
        }

        int count = 0;
        for(String url : repoURLs) {
            System.out.println("Analyzing " + url + " (" + (++count) + "/" + repoURLs.size() + ")");
            JSONObject result = traverseRepoForFileContent(url);
            if (result != null) {
                insertRepoSummary(result);
                String repoName = url.split("/repos/")[1].split("/branches")[0];
                if (inputType.equals("f")) {
                    markFileAsDone(repoName, repoURLs);
                }
                System.out.println("Finished analyzing " + url+ " (" + (count) + "/" + repoURLs.size() + ")");
            }
        }
    }

    private void markFileAsDone(String repoName, ArrayList<String> repoURLS) {
        ArrayList<String> lines = new ArrayList<String>();
        try {
            String repoListPath = Config.getRepoURLsPath();
            BufferedReader br = new BufferedReader(new FileReader(repoListPath));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equals(repoName)) {
                    lines.add("#" + line);
                } else {
                    lines.add(line);
                }
            }
            br.close();
            FileWriter f1 = new FileWriter(repoListPath);
            BufferedWriter bw = new BufferedWriter(f1);
            for (String s: lines) {
                bw.write(s);
                bw.newLine();
                bw.flush();
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
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
            file = new File(Config.getTempJavaFilePath());
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

    private JSONObject traverseRepoForFileContent(String repoURL) {
        JSONObject repoAnalysis;
        try {
            ArrayList<UrlFilepathPair> urls = traverseTreeForFileURLs(repoURL);
            //create a summary obj for this repo
            //System.out.println(repoURL);
            JSONifySummary repoSummary = new JSONifySummary(numFiles, numJavaFiles);
            //UrlFilepathPair contains the blob url and the file path of the blob
            for(UrlFilepathPair url : urls) {
                JSONObject content = fetcher.makeGetRequest(url.getRepoBlobUrl());
                String contentStr = content.getString("content");
                contentStr = contentStr.replaceAll("\n", "");
                decodeAndParseFile(contentStr, repoURL, url.getFilePath(), repoSummary);
            }
            //repo summary is populated with results for each file.
            //need to summarize the results into jsonobject
            repoAnalysis = repoSummary.getRepoErrorSummary();
            //save results
            //insertRepoSummary(repoAnalysis.toString());
            //System.out.println(repoAnalysis);
            return repoAnalysis;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        //return summary
    }

    private boolean insertRepoSummary(JSONObject repo) {
        String repoSummary = repo.toString();
        MongoCollectionClient client = MongoCollectionClient.getInstance();
        JSONObject query = new JSONObject();
        try {
            query.put("repo_url", repo.get("repo_url"));
        } catch (JSONException e){
            e.printStackTrace();
        }
        FindIterable<Document> results = client.getJavaCollection().find(Document.parse(query.toString()));
        boolean exists = false;
        for (Document doc:results) {
            if (doc != null)
                exists = true;
        }
        if (!exists) {
            client.getJavaCollection().insertOne(Document.parse(repoSummary));
            return true;
        }
        return false;
    }

    private ArrayList<UrlFilepathPair> traverseTreeForFileURLs(String repoURL) {
        ArrayList<UrlFilepathPair> urls = new ArrayList<>();
        try {
            JSONObject treeObj = getTreeObjectFromRepo(repoURL);
            String treeURL = treeObj.getString("url");
            JSONObject tree = fetcher.makeGetRequest(treeURL + "?recursive=1");
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
            JSONObject response = fetcher.makeGetRequest(url);
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

    private ArrayList<String> getRepoURLsFromConfig() {
        String repoURLsPath = Config.getRepoURLsPath();
        ArrayList<String> urls = new ArrayList<>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(repoURLsPath));
            String line;
            while((line = br.readLine()) != null && line.length()>0)   {
                if (line.charAt(0) == '#') {
                    System.out.println("Skipping " + line + " repo");
                    continue;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("https://api.github.com/repos/");
                sb.append(line);
                sb.append(fetcher.getDefaultBranch(sb.toString()));
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
