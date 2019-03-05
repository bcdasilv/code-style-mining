import pymongo

mongo_name = ""
mongo_password = ""
mongo_database = ""
mongo_cluster = ""
mongo_collection = ""


def get_client():
    if len(mongo_cluster) == 0 or len(mongo_name) == 0 or len(mongo_password) == 0:
        raise ValueError('MongoDB credentials not initialized successfully. ')
    else:
        return pymongo.MongoClient(
            "mongodb+srv://" + mongo_name + ":" + mongo_password +
            "@" + mongo_cluster + "/test?retryWrites=true"
        )
