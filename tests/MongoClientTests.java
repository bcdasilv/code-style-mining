import mongo.MongoCollectionClient;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MongoClientTests {

    @Test
    void testConnection(){
        MongoCollectionClient testMongoClient = MongoCollectionClient.getInstance();
        long documents = testMongoClient.getJavaCollection().count();
        assertTrue(documents >= 0);
    }
}
