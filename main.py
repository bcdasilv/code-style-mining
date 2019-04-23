import json
import re
import sys
import urllib.parse
import pymongo
import config

from repo_analyzer import C_REPO_FULL_NAME, C_REPO_NAME, LOCAL_PATH, \
    analyze_repo, delete_local_tree_clone, set_oauth_token, reset_summary
from verify_repo_mongo import check_duplicate_repo
import naming_analyzer as naming



def print_err_msg(owner, repo, err_type, msg):
    print("{} in {}'s {} repository: {}".format(err_type, owner, repo, msg))
    print("The repository has been skipped.")

# Analyze the list of repositories in an input file
# Lines that are empty or begin with a space or pound character will be skipped
def read_from_file(file_name, output_setting, mdb_name, mdb_password, mdb_cluster, mdb_database, mdb_collection):
    infile = open(file_name, 'r')
    lines = infile.readlines()
    # lines is having multiple file names appending to it on case of execution failure
    for i in range(len(lines)):
        if lines[i].startswith("#") or lines[i].startswith(" ")\
                or lines[i] == "" or lines[i] == "\n":
            continue
        temp = re.split("/|\n", lines[i])
        owner = temp[0]
        repo = temp[1]
        try:
            if check_duplicate_repo(owner, repo):
                results = analyze_repo(owner, repo)
                handle_output(output_setting, results, mdb_name,
                              mdb_password, mdb_cluster, mdb_database, mdb_collection)
            else:
                print("The " + repo + " repository by " + owner + " has already been analyzed and uploaded to MongoDB. "
                                      "Skipping analysis of this repository.")
        except SyntaxError as e:
            print_err_msg(owner, repo, "SyntaxError", e.msg)
        except UnicodeDecodeError as u:
            print_err_msg(owner, repo, "UnicodeDecodeError", u.reason)
        # Remove the local clone of the file directories
        delete_local_tree_clone(LOCAL_PATH + "/" + repo)


# Write data to a cluster's database with a specific collection name
# Authenticate with given username and password
def write_to_mongodb(mongodb_user, mongodb_password, cluster, db_name, coll_name, data):
    client = pymongo.MongoClient(
        "mongodb+srv://" + mongodb_user + ":" + mongodb_password +
        "@" + cluster + "/test?retryWrites=true"
    )
    db = client[db_name]
    coll = db[coll_name]
    mongo_id = coll.insert_one(data)

    return mongo_id

# Either write results to MongoDB if setting is "m" or print to the console if setting is "c"
# Name, password, cluster, and database parameters can be None if setting is "c"
def handle_output(setting, results, mdb_name, mdb_password, mdb_cluster, mdb_database, mdb_collection):
    if setting == "c":
        master_dict = {results[C_REPO_FULL_NAME]: results}
        print(json.dumps(master_dict))
    elif setting == "m":
        try:
            write_to_mongodb(mdb_name, mdb_password, mdb_cluster,
                             mdb_database, mdb_collection, results)
        except pymongo.errors.DuplicateKeyError:
            print("The {} repository has already been analyzed and uploaded to MongoDB. "
                  "To update the analysis, delete the existing results from MongoDB "
                  "first.".format(results[C_REPO_NAME]))
        else:
            print("Finished analyzing " + results[C_REPO_NAME]
                  + " repository. Wrote results to MongoDB.")
    else:
        raise ValueError("Unrecognized handle_output setting")


# Input and output will both be in the console, unless cmdline args specify otherwise
def main(argv):

    usage_msg = "Usage: python3 main.py <GitHub_OAuth_token> [-file <file_name>] " \
                "[-mongodb <username> <password> <cluster> <database> <collection>]"

    if len(argv) < 1:
        raise ValueError(usage_msg)

    set_oauth_token(argv[0])

    input_setting = "c"
    output_setting = "c"
    mdb_name = None
    mdb_password = None
    mdb_cluster = None
    mdb_database = None
    mdb_collection = None

    if len(argv) > 1:

        try:
            file_cmd_index = argv.index("-file")
        except ValueError:
            pass
        else:
            input_setting = "f"
            try:
                file_name = argv[file_cmd_index + 1]
            except IndexError:
                print(usage_msg)
                return

        try:
            mongodb_cmd_index = argv.index("-mongodb")
        except ValueError:
            pass
        else:
            output_setting = "m"
            try:
                mdb_name = argv[mongodb_cmd_index + 1]
                mdb_password = argv[mongodb_cmd_index + 2]
                mdb_cluster = argv[mongodb_cmd_index + 3]
                mdb_database = argv[mongodb_cmd_index + 4]
                mdb_collection = argv[mongodb_cmd_index + 5]
                config.mongo_name = mdb_name
                config.mongo_password = mdb_password
                config.mongo_collection = mdb_collection
                config.mongo_database = mdb_database
                config.mongo_cluster = mdb_cluster
            except IndexError:
                print(usage_msg)
                return

    else:
        input_setting = None
        output_setting = None

        if output_setting not in ('m', 'c'):
            output_setting = input("Output setting: Would you like to write the "
                                   "results to MongoDB [m] or print them to the console [c]? ")
            output_setting.lower()
        if output_setting == "m":
            mdb_name = input("MongoDB username: ")
            mdb_password = input("MongoDB password: ")
            mdb_cluster = input("MongoDB cluster: ")
            mdb_database = input("MongoDB database: ")
            mdb_collection = input("MongoDB collection: ")
        if input_setting not in ('f', 'c'):
            input_setting = input("Input setting: Would you like to read the "
                                  "repos from a file [f] or from the console [c]? ")
            input_setting.lower()
        print()
        if input_setting == "f":
            file_name = input("File name: ")

    if input_setting == "c":
        # TODO: got to be a better way to do this
        print("Enter \">quit\" for the Owner name when you would like to exit the program.")

        repeat = None
        while repeat != "n":
            owner = input("\nOwner name: ")
            if owner == '>quit':
                break
            repo = input("Repository name: ")
            try:
                if check_duplicate_repo(owner, repo):
                    results = analyze_repo(owner, repo)
            except SyntaxError as e:
                print_err_msg(owner, repo, "SyntaxError", e.msg)
            except UnicodeDecodeError as u:
                print_err_msg(owner, repo, "UnicodeDecodeError", u.reason)
            else:
                handle_output(output_setting, results, mdb_name,
                              mdb_password, mdb_cluster, mdb_database, mdb_collection)

            # Remove the local clone of the file directories
            delete_local_tree_clone(LOCAL_PATH + "/" + repo)

    elif input_setting == "f":
        read_from_file(file_name, output_setting, mdb_name, mdb_password, mdb_cluster, mdb_database, mdb_collection)


if __name__ == '__main__':
    main(sys.argv[1:])
