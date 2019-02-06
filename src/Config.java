import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class Config {
    // static instance
    private static Config instance;
    // private info
    private String authToken;
    // non private info
    private String tempFilePath;
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
            tempFilePath = config.getString("tempFilePath").replaceAll("\"", "");
            authToken = config.getString("authToken").replaceAll("\"", "");
            repoURLsPath = config.getString("repoURLsPath").replaceAll("\"", "");
        }
        catch(ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String getTempFilePath() {
        return tempFilePath;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getRepoURLsPath() {
        return repoURLsPath;
    }
}
