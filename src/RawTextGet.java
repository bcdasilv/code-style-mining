import java.io.BufferedReader;
import java.io.InputStreamReader;
//import java.util.Base64.Decoder;
import org.apache.commons.codec.binary.Base64;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;

public class RawTextGet {

	static final String springboot = "https://api.github.com/repos/TheAlgorithms/Java/branches/master";
	static final String treeURL = "https://api.github.com/repos/TheAlgorithms/Java/git/trees/";
	
	public void testAPI() {
		
		String firstSHA = startTraversingMasterBranch(springboot);
		ArrayList<String> urls;
		ArrayList<String> content;
		if (firstSHA != null) {
			urls = startTreeTraversal(treeURL + firstSHA + "?recursive=1");
		}

//		for(i = 0; i < content.length){
//			byte[] valueDecoded = Base64.decodeBase64("url[i]");
//			content.add(new String(valueDecoded));
//			System.out.println("\n" + new String(valueDecoded));
//		}
		
//		FileParser fp = new FileParser();
//		fp.parseFile(localFile);
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
		String returnThis = "";
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

		for(i = 0; i<SHA.length(); i++){
			if(SHA.charAt(i) != '\"')
				returnThis = (returnThis + SHA.charAt(i));
			else
				break;
		}
//		System.out.println(returnThis + "\n");
		return returnThis;	
	}
	
	public ArrayList<String> startTreeTraversal(String url) {
		
		String response = makeGetRequest(url);
		String[] words = response.split(",");
		int i = 0;
		
		ArrayList<String> pathURL = new ArrayList<String>();

		for (; i < words.length; i++) {
			String line = words[i];
			if (line.contains("path")) {
				if(i > words.length-5) {
					break;
				}
				else if (words[i+2].contains("blob") && line.contains(".java")) {
					pathURL.add(decodeContent(words[i+5]));
				} 
			}
		}
		return pathURL;
	}
	
	public String decodeContent(String url) {
		String new_url = url.substring(7, url.length() - 2);
		String rawContent = null;
	
		String response = makeGetRequest(new_url);
		String[] words = response.split(",");
		int i = 0;
		
		for (; i < words.length; i++) {
			String line = words[i];
			if (line.contains("content")) {
				rawContent = words[i].substring(11, words[i].length()-6);
				rawContent.replaceAll("\\n", "");
//				System.out.println(rawContent);
				break;
			}
		}
		return rawContent;
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
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
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
