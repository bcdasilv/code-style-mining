import config.Config;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.json.JSONException;

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

			configMap.put("tempJavaFilePath", config.getString("tempJavaFilePath").replaceAll("\"", ""));
			configMap.put("tempJSONFilePath", config.getString("tempJSONFilePath").replaceAll("\"", ""));
			configMap.put("authToken", config.getString("authToken").replaceAll("\"", ""));
			configMap.put("repoURLsPath", config.getString("repoURLsPath").replaceAll("\"", ""));
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

		for (Map.Entry<String, String> entry : configMap.entrySet()) {
			String key = entry.getKey();

			while (configMap.get(key) == null || configMap.get(key).isEmpty()) {
				System.out.print("Missing " + key + ". Enter one or [q] to quit: ");
				String s = reader.nextLine().trim();
				System.out.print("\n");
				testTermination(s.toLowerCase());
				configMap.put(key, s);
			}
		}

		Config.init(configMap.get("authToken"), configMap.get("mongoUsername"), configMap.get("mongoPassword"),
				configMap.get("mongoUrl"), configMap.get("mongoDatabase"), configMap.get("mongoCollection"),
				configMap.get("tempJavaFilePath"), configMap.get("tempJSONFilePath"), configMap.get("repoURLsPath"));

		RepoTraversal traverser = new RepoTraversal();

		traverser.findJavaFilesToParse();
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
