package config;

/**
 * In order to use this class, create a javaAnalysis.properties file in the root directory of the project or manually
 * input these as they are found missing during the runtime.
 * Check the README for everything you will need to define in here.
 * Do not push the .properties file to the repo, as it holds private information.
 */

public class Config {
    // static instance
    private static Config instance;
    // private info
    private static String authToken;
    private static String mongoUrl;
    private static String mongoUsername;
    private static String mongoPassword;
    private static String mongoDatabase;
    private static String mongoCollection;
    // non private info
    private static String tempJavaFilePath;
    private static String tempJSONFilePath;
    private static String repoURLsPath;

    public static void init(String newAuthToken, String newMongoUsername, String newMongoPassword, String newMongoUrl,
                            String newMongoDatabase, String newMongoCollection, String newTempJavaFilePath,
                            String newTempJSONFilePath, String newRepoURLsPath) {
        authToken = newAuthToken;
        mongoUsername = newMongoUsername;
        mongoPassword = newMongoPassword;
        mongoUrl = newMongoUrl;
        mongoDatabase = newMongoDatabase;
        mongoCollection = newMongoCollection;
        tempJavaFilePath = newTempJavaFilePath;
        tempJSONFilePath = newTempJSONFilePath;
        repoURLsPath = newRepoURLsPath;
    }

    /*
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
            mongoUsername = config.getString("mongoUsername");
            mongoPassword = config.getString("mongoPassword");
            mongoUrl = config.getString("mongoUrl");
            mongoDatabase = config.getString("mongoDatabase");
            mongoCollection = config.getString("mongoCollection");
        }
        catch(ConfigurationException e) {
            e.printStackTrace();
        }
    }
    */

    public static String getTempJavaFilePath() {
        return tempJavaFilePath;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static String getRepoURLsPath() {
        return repoURLsPath;
    }

    public static String getTempJSONFilePath() {
        return tempJSONFilePath;
    }

    public static String getMongoUrl() {
        return mongoUrl;
    }

    public static String getMongoUsername() {
        return mongoUsername;
    }

    public static String getMongoPassword() {
        return mongoPassword;
    }

    public static String getMongoDatabase() {
        return mongoDatabase;
    }

    public static String getMongoCollection() {
        return mongoCollection;
    }
}
