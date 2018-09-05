import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Base64.Decoder;

import org.apache.commons.codec.binary.Base64;

import java.util.ArrayList;

import java.net.HttpURLConnection;
import java.net.URL;

public class RawTextGet {

	static final String springboot = "https://api.github.com/repos/spring-projects/spring-boot/branches/master";
	static final String treeURL = "https://api.github.com/repos/spring-projects/spring-boot/git/trees/";
	
	public void testAPI() {
		
		String firstSHA = startTraversingMasterBranch(springboot);
		ArrayList<String> urls;
		if (firstSHA != null) {
			urls = startTreeTraversal(treeURL + firstSHA, new ArrayList<String>());
		}
		
		
	}
	
	public String startTraversingMasterBranch(String url) {

		String response = makeGetRequest(url);
		String SHA = null;
		if (response != null) {
			SHA = getSHA(response);
		}
		return SHA;
	}

	public String getSHA(String response) {
		String[] words = response.split(":");
		String SHA;
		int i = 0;
		
		for (; i < words.length; i++) {
			String word = words[i];
			if (word.contains("sha")) {
				break;
			}
		}
		
		SHA = words[i+1];
		SHA = SHA.trim();
		SHA = SHA.substring(1, SHA.length() - 1);
		return SHA;	
	}
	
	public ArrayList<String> startTreeTraversal(String url, ArrayList<String> finalURLS) {
		
		String response = makeGetRequest(url);
		String[] words = response.split(",");
		int i = 0;
		
		ArrayList<String> paths = new ArrayList<String>();
		ArrayList<String> types = new ArrayList<String>();
		ArrayList<String> pathURL = new ArrayList<String>();
		

		for (; i < words.length; i++) {
			String line = words[i];
			if (line.contains("path")) {
				if (words[i+2].contains("blob") && line.contains(".java")) {
					paths.add(line.split("\\s+")[1]);
					types.add("blob");
					pathURL.add(words[i+5].split("\\s+")[1]);
				} else if (words[i+2].contains("tree")) {
					paths.add(line.split("\\s+")[1]);
					types.add("tree");
					pathURL.add(words[i+5].split("\\s+")[1]);					
				}
				
			}
			
		}
		
		for (i = 0; i < types.size(); i++) {
			String type = types.get(i);
			if (type.equals("tree")) {
				String rawURL = pathURL.get(i);
				rawURL = rawURL.substring(1, rawURL.length() - 1);
				startTreeTraversal(rawURL, finalURLS);
			} else {
				finalURLS.add(pathURL.get(i));
			}
			
		}
		
		return finalURLS;

	}
	
	public byte[] decodeContent(String response) {
		String[] words = response.split(":");
		String rawContent = null;
		byte[] decodedContent = null;
		
		int i = 0;
		for (; i < words.length; i++) {
			if (words[i].contains("content")) {
				break;
			}
		}
		
		rawContent = words[i+1].trim();
		rawContent = rawContent.substring(1, rawContent.length() - 1);
		
		rawContent.replaceAll("\\n", "");
		decodedContent = Base64.decodeBase64(rawContent);
		return decodedContent;
		
	}
	
	
	public String makeGetRequest(String url) {
		
		String rawResponse = null;
		
		try {
			URL springboot = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)springboot.openConnection();

			conn.setRequestProperty("Content-Type","application/json");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("GET");

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine + "\n");
			}
			
			in.close();
			rawResponse = response.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rawResponse;
		
	}
	
	
}
