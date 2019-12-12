import config.Config;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MainParser {
//	public static final String gitURL = "https://github.com/janani-sridhar/CaesarCipher";
//	public static final String directory = "/Users/jananisridhar/Desktop/CC";
//	public static final String localFile = "./RandomFile.java";
//
//	public static FileParser fp;
//	public static JSONifySummary summary;


	public static void testTermination(String s) {
		if(s.equals("q")) {
			System.exit(0);
		}
	}

	public static void main(String[] args) throws JSONException {
		//summary = new JSONifySummary();
		//fp = new FileParser(summary);
		RepoTraversal traverser = new RepoTraversal();
		Scanner reader = new Scanner(System.in);

		Map<String, String> configMap = new HashMap<>();
		configMap.put("authToken", null);
		configMap.put("mongoUrl", null);
		configMap.put("mongoUsername", null);
		configMap.put("mongoPassword", null);
		configMap.put("mongoDatabase", null);
		configMap.put("mongoCollection", null);
		configMap.put("tempJavaFilePath", null);
		configMap.put("tempJSONFilePath", null);
		configMap.put("repoURLsPath", null);

		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
				new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
						.configure(params.properties().setFileName("javaAnalysis.properties"));
		try {
			Configuration config = builder.getConfiguration();
			String temp;
			//Properties not found will remain null
			temp = config.getString("tempJavaFilePath");
			configMap.put("tempJavaFilePath", (temp !=null)? temp.replaceAll("\"", "") : null);
			temp = config.getString("tempJSONFilePath");
			configMap.put("tempJSONFilePath", (temp !=null)? temp.replaceAll("\"", "") : null);
			temp = config.getString("authToken");
			configMap.put("authToken", (temp !=null)? temp.replaceAll("\"", "") : null);
			temp = config.getString("repoURLsPath");
			configMap.put("repoURLsPath", (temp !=null)? temp.replaceAll("\"", "") : null);

			configMap.put("mongoUsername", config.getString("mongoUsername"));
			configMap.put("mongoPassword", config.getString("mongoPassword"));
			configMap.put("mongoUrl", config.getString("mongoUrl"));
			configMap.put("mongoDatabase", config.getString("mongoDatabase"));
			configMap.put("mongoCollection", config.getString("mongoCollection"));
		}
		catch(ConfigurationException e) {
			System.out.println("No javaAnalysis.properties found...\n");
			//e.printStackTrace();
		}

		//Ask for missing creds via the CL (exception for filename)
		for (Map.Entry<String, String> entry : configMap.entrySet()) {
			String key = entry.getKey();

			if (key.equals("repoURLsPath")) {
				continue;
			}

			while (configMap.get(key) == null || configMap.get(key).isEmpty()) {
				System.out.print("Missing " + key + ". Enter one or [q] to quit: ");
				String s = reader.nextLine().trim();
				System.out.print("\n");
				testTermination(s.toLowerCase());
				configMap.put(key, s);
			}
		}

		String inputType = "";
		Integer limitRepos = null;
		ArrayList<String> keywords = new ArrayList<>();
		String optionType = "";
		while (!inputType.equals("f") && !inputType.equals("g")) {
			System.out.print("Do you want to get repo names from file [f] or generate them [g]. Enter one or [q] to quit: ");
			inputType = reader.nextLine().trim();
			System.out.print("\n");
			testTermination(inputType.toLowerCase());
		}

		if (inputType.equals("f")) {
			while (configMap.get("repoURLsPath") == null || configMap.get("repoURLsPath").isEmpty()) {
				System.out.print("Missing repoURLsPath. Enter one or [q] to quit: ");
				String s = reader.nextLine().trim();
				System.out.print("\n");
				testTermination(s.toLowerCase());
				configMap.put("repoURLsPath", s);
			}
		} else {
			while (!optionType.equals("s") && !optionType.equals("k")) {
				System.out.print("Get repo names by top stars [s] or by keywords [k]. Enter one of these or [q] to quit: ");
				optionType = reader.nextLine().trim();
				System.out.print("\n");
				testTermination(optionType.toLowerCase());
			}
			
			if (optionType.equals("k")) {
				String t = "";
				while (!t.equals("c")) {
					System.out.print("Enter keyword or [c] to continue: ");
					t = reader.nextLine().trim();
					System.out.print("\n");
					if (t.equals("c")) {
						break;
					} else if (!t.isEmpty()){
						keywords.add(t);
					}
				}
				System.out.println("Using keywords: " + keywords);
			}
			
			while (limitRepos == null) {
				System.out.print("How many repos do you want to fetch (final results might not match this number)?" +
						" Enter one or [q] to quit: ");
				String s = reader.nextLine().trim();
				System.out.print("\n");
				testTermination(s.toLowerCase());
				try {
					if (Integer.parseInt(s) >= 0) {
						limitRepos = Integer.parseInt(s);
					}
				} catch (NumberFormatException e) {
					continue;
				}
			}
		}

		Config.init(configMap.get("authToken"), configMap.get("mongoUsername"), configMap.get("mongoPassword"),
				configMap.get("mongoUrl"), configMap.get("mongoDatabase"), configMap.get("mongoCollection"),
				configMap.get("tempJavaFilePath"), configMap.get("tempJSONFilePath"), configMap.get("repoURLsPath"));

		//TODO might want to handle these options more dynamically.
		// In some cases you dont need all of these put they are passed anyway
		traverser.findJavaFilesToParse(inputType, optionType, limitRepos, keywords);
	}

	/*public static void getGitFiles(String url, String directory) {
		File gitFolder = new File(directory);
		Git git = null;

		if(!gitFolder.exists()) {
			try {
				git = Git.cloneRepository()
						.setURI( url )
						.setDirectory(gitFolder)
						.call();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				git = Git.open(gitFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Repository repo = git.getRepository();		

		for(File content : repo.getWorkTree().listFiles()) {
			classifyFiles(content);
		}
	}*/

	/*public static void classifyFiles(File f) {
		if(f.isFile()) {
			if(f.getName().contains("java")) {
				fp.parseFile(f.getAbsolutePath());
			}
		}
		else {
			for(File k : f.listFiles()) {
				classifyFiles(k);
			}
		}
	}
	
	public static void analyzeResults() {
		
	}*/
}
