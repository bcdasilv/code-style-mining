import config


def check_duplicate_repo(owner, repo):
    client = config.get_client()
    db = client[config.mongo_database]
    coll = db[config.mongo_collection]
    if coll.find_one({"name": repo}):
        return False
    else:
        return True



