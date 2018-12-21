import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;

public class RawTextGet {
	static final String newTempFile = "/Users/user/Desktop/400/newFile.java"; // CHANGE FOR USERS LOCAL 
	static final String springboot = "https://api.github.com/repos/pagseguro/java/branches/master"; // NOT ALWAYS MASTER 
	static final String treeURL = "https://api.github.com/repos/pagseguro/java/git/trees/";
	static final String tempToken = "";
	
	public void testAPI() {
		
		String firstSHA = startTraversingMasterBranch(springboot);
		ArrayList<String> urls = new ArrayList<String>();
		FileParser fp = new FileParser();
		
		//fp.parseFile("/Users/user/Desktop/400/newFile.java");
		
		int i = 0;
		if (firstSHA != null) {
			urls = startTreeTraversal(treeURL + firstSHA + "?recursive=1");
		}

		for(; i < urls.size(); i++){
			//System.out.println("here");
			String x = urls.get(i);
			x = x.replace("\\n", "");
			byte[] valueDecoded = Base64.decodeBase64(x);
			String pwd = createFile(new String(valueDecoded));
			fp.parseFile(pwd);
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
		String new_url;
		if(url.endsWith("]")) {
			new_url = url.substring(7, url.length() - 3);
		}
		else {
			new_url = url.substring(7, url.length() - 2);
		}
		String rawContent = null;
	
		String response = makeGetRequest(new_url);
		String[] words = response.split(",");
		int i = 0;
		
		for (; i < words.length; i++) {
			String line = words[i];
			if (line.contains("content")) {
				rawContent = words[i].substring(11, words[i].length()-6);
				rawContent.replaceAll("\\n", "");
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
			
//			conn.setRequestProperty("Content-Type","application/json");
			conn.setRequestProperty("Authorization", "token " + tempToken);//NEEDS TOKEN 
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
	
	public String createFile(String content) {
		  FileOutputStream fos = null;
	      File file = null;
	      String mycontent = content;
	      try {
			  file = new File(newTempFile);
			  fos = new FileOutputStream(file);

			  if (!file.exists()) {
			     file.createNewFile();
			  }

			  byte[] bytesArray = mycontent.getBytes();
		
			  fos.write(bytesArray);
			  fos.flush();
			  System.out.println("File Written Successfully\n");
	       } catch (IOException ioe) {
	    	   ioe.printStackTrace();
	       } finally {
			  try {
			     if (fos != null) {
			    	 fos.close();
			     }
	          } 
			  catch (IOException ioe) {
			     System.out.println("Error in closing the Stream");
			  }
	       }
		return newTempFile;
	}
}
