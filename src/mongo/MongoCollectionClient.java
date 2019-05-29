package mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import config.Config;
import org.bson.Document;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Singleton implementation for accessing MongoDB mongo that returns only the necessary java collection to save analysis results.
 * The collection instance returned is the one specified in the javaAnalysis.properties file.
 * @author Kyler Ramsey
 * @version 1.0
 * @since 2019-04-22
 */

public class MongoCollectionClient {

    private static MongoCollectionClient client;
    private MongoCollection<Document> javaCollection;
    private static final Integer port = 27017;

    private MongoCollectionClient() {
        Config mdbConfig = Config.getInstance();
        String database = mdbConfig.getMongoDatabase();
        String username = mdbConfig.getMongoUsername();
        String password = mdbConfig.getMongoPassword();
        String url = mdbConfig.getMongoUrl();
        String collection = mdbConfig.getMongoCollection();
        try {
            MongoClientURI uri = new MongoClientURI(
                    "mongodb+srv://"+username+":"+password+"@"+url+"/test?retryWrites=true");
            MongoClient mongoClient = new MongoClient(uri);
            MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
            javaCollection = mongoDatabase.getCollection(collection);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid MongoDB configuration details");
            e.printStackTrace();
        }
    }

    public static MongoCollectionClient getInstance() {
        if (client == null)
            return new MongoCollectionClient();
        return client;
    }

    public MongoCollection<Document> getJavaCollection() {
        return javaCollection;
    }

}
