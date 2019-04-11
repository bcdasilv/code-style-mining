import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * In order to use this class, create a javaAnalysis.properties file in the root directory of the project.
 * Check the README for everything you will need to define in here.
 * Do not push the .properties file to the repo, as it holds private information.
 */
public class Config {
    // static instance
    private static Config instance;
    // private info
    private String authToken;
    // non private info
    private String tempJavaFilePath;
    private String tempJSONFilePath;
    private String repoURLsPath;

    public static Config getInstance() {
        if(instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private Config() {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties().setFileName("javaAnalysis.properties"));
        try {
            Configuration config = builder.getConfiguration();
            tempJavaFilePath = config.getString("tempJavaFilePath").replaceAll("\"", "");
            tempJSONFilePath = config.getString("tempJSONFilePath").replaceAll("\"", "");
            authToken = config.getString("authToken").replaceAll("\"", "");
            repoURLsPath = config.getString("repoURLsPath").replaceAll("\"", "");
        }
        catch(ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String getTempJavaFilePath() {
        return tempJavaFilePath;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getRepoURLsPath() {
        return repoURLsPath;
    }

    public String getTempJSONFilePath() {
        return tempJSONFilePath;
    }
}
