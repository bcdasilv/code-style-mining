import json
import re
import sys
import urllib.parse
import pymongo
import config
import os.path

from load_repo_options import load_top_keyword_repos, get_top_500_repos

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
    infile_pending = open(file_name, 'r')
    infile_error = open("error.txt", 'a')
    infile_analyzed = open("analyzed.txt", 'a')
    lines = infile_pending.readlines()
    # lines is having multiple file names appending to it on case of execution failure
    for i in range(len(lines)):
        if lines[i].startswith("#") or lines[i].startswith(" ")\
                or lines[i] == "" or lines[i] == "\n":
            continue
        temp = re.split("/|\n", lines[i])
        owner = temp[0]
        repo = temp[1]
        try:
            if output_setting != 'm' or check_duplicate_repo(owner, repo):
                results = analyze_repo(owner, repo)
                handle_output(output_setting, results, mdb_name, mdb_password, mdb_cluster, mdb_database, mdb_collection)
            else:
                print("The " + repo + " repository by " + owner + " has already been analyzed and uploaded to MongoDB. "
                                                                  "Skipping analysis of this repository.")

            infile_analyzed.write(lines[i])  # here we might be adding duplicate entries on the analyzed file
        except SyntaxError as e:
            print_err_msg(owner, repo, "SyntaxError", e.msg)
            infile_error.write(lines[i])
        except UnicodeDecodeError as u:
            print_err_msg(owner, repo, "UnicodeDecodeError", u.reason)
            infile_error.write(lines[i])
        # Remove the local clone of the file directories
        delete_local_tree_clone(LOCAL_PATH + "/" + repo)


def read_from_options(output_setting, mdb_name, mdb_password, mdb_cluster, mdb_database, mdb_collection):
    infile_error = open("error.txt", 'a')
    infile_analyzed = open("analyzed.txt", 'a')
    number_repos = None
    chosen_option = None
    lines = None

    while chosen_option not in ('k', 's'):
        chosen_option = input("What kind of repos do you want to analyze? By keywords [k] or by most number of "
                              "stars [s] or quit program [q]: ").strip()
        chosen_option = chosen_option.lower()
        print("\n")
        test_termination(chosen_option)

    keyword_list = []
    if chosen_option == 'k':
        c = input("Input your own keywords [y]? Or let use ours [n]?").strip()

        if c == 'y':
            while True:
                word = input("Add a Keyword ([c] to continue): ").strip()
                if word == 'c':
                    break
                keyword_list.append(word)

    if number_repos is None:
        try:
            number_repos = int(input("How many repos do you want to fetch from github "
                                     "(final analyzed count might be different then this number) "
                                     "(default: runs as long as it can or is stopped): ").strip())
            print("\n")
        except ValueError:
            number_repos = None

    if chosen_option == 'k':
        # If list is empty, it will default to our repo_keywords
        lines = load_top_keyword_repos(number_repos, keyword_list)
    elif chosen_option == 's':
        lines = get_top_500_repos(number_repos)

    print(lines)
    #sys.exit()

    # lines is having multiple file names appending to it on case of execution failure
    for i in range(len(lines)):
        if lines[i].startswith("#") or lines[i].startswith(" ") \
                or lines[i] == "" or lines[i] == "\n":
            continue
        temp = re.split("/|\n", lines[i])
        owner = temp[0]
        repo = temp[1]
        try:
            if output_setting != 'm' or check_duplicate_repo(owner, repo):
                results = analyze_repo(owner, repo)
                handle_output(output_setting, results, mdb_name, mdb_password, mdb_cluster, mdb_database, mdb_collection)
            else:
                print("The " + repo + " repository by " + owner + " has already been analyzed and uploaded to MongoDB. "
                                                                  "Skipping analysis of this repository.")

            infile_analyzed.write(lines[i])  # here we might be adding duplicate entries on the analyzed file
        except SyntaxError as e:
            print_err_msg(owner, repo, "SyntaxError", e.msg)
            infile_error.write(lines[i])
        except UnicodeDecodeError as u:
            print_err_msg(owner, repo, "UnicodeDecodeError", u.reason)
            infile_error.write(lines[i])
        # Remove the local clone of the file directories
        delete_local_tree_clone(LOCAL_PATH + "/" + repo)


# Write data to a cluster's database with a specific collection name
# Authenticate with given username and password
def write_to_mongodb(mongodb_user, mongodb_password, cluster, db_name, coll_name, data):
    client = pymongo.MongoClient(
        "mongodb+srv://" + mongodb_user + ":" + mongodb_password +
        "@" + cluster + "/python-analysis?retryWrites=true" +
        # "@" + cluster + "/test?retryWrites=true" +
        "&w=majority&tls=true&tlsAllowInvalidCertificates=true"
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
                  "first.".format(results[C_REPO_NAME]), flush=True)
        else:
            print("Finished analyzing " + results[C_REPO_NAME]
                  + " repository. Wrote results to MongoDB.", flush=True)
    else:
        raise ValueError("Unrecognized handle_output setting")


def test_termination(user_input):
    if user_input == "q":
        sys.exit()


# Input and output will both be in the console, unless cmdline args specify otherwise
def main(argv):
    oauth_token = None
    file_name = None
    mdb_name = None
    mdb_password = None
    mdb_cluster = None
    mdb_database = None
    mdb_collection = None

    usage_msg = "Usage (credentials after flags must match) (not recommended for credentials): " \
                "python3 main.py [-authtoken <GitHub_OAuth_token>] [-file <file_name>] " \
                "[-mongodb <username> <password> <cluster> <database> <collection>]"

    # Test for command line credentials
    if len(argv) != 0:
        try:
            token_index = argv.index("-authtoken")
        except ValueError:
            pass
        else:
            try:
                oauth_token = argv[token_index + 1]
            except IndexError:
                print(usage_msg)
                return

        try:
            file_cmd_index = argv.index("-file")
        except ValueError:
            pass
        else:
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
            try:
                mdb_name = argv[mongodb_cmd_index + 1]
                mdb_password = argv[mongodb_cmd_index + 2]
                mdb_cluster = argv[mongodb_cmd_index + 3]
                mdb_database = argv[mongodb_cmd_index + 4]
                mdb_collection = argv[mongodb_cmd_index + 5]
            except IndexError:
                print(usage_msg)
                return
    else:
        print("No command line args found...")

    # Test for config file credentials. Does not override if put into args
    if os.path.exists('pythonAnalysis.properties'):
        print("")
        try:
            file = open('pythonAnalysis.properties', 'r')
            line = file.readline()
            while line:
                conf_line = [l.strip() for l in line.split('=')]
                line = file.readline()
                if conf_line[0] == "authToken" and oauth_token is None:
                    oauth_token = conf_line[1]
                elif conf_line[0] == "repoURLsPath" and file_name is None:
                    file_name = conf_line[1]
                elif conf_line[0] == "mongoUsername" and mdb_name is None:
                    mdb_name = conf_line[1]
                elif conf_line[0] == "mongoPassword" and mdb_password is None:
                    mdb_password = conf_line[1]
                elif conf_line[0] == "mongoUrl" and mdb_cluster is None:
                    mdb_cluster = conf_line[1]
                elif conf_line[0] == "mongoDatabase" and mdb_database is None:
                    mdb_database = conf_line[1]
                elif conf_line[0] == "mongoCollection" and mdb_collection is None:
                    mdb_collection = conf_line[1]

            file.close()
        except IOError:
            pass
    else:
        print("No pythonAnalysis.properties file found...")

    # Start checking for missing credentials and ask for input how to run the program
    input_setting = None
    output_setting = None

    if oauth_token is None:
        oauth_token = input("Missing necessary github token. Type it in (not recommended) or type [q] to exit: ").strip()
        print("\n")
        test_termination(oauth_token.lower())

    while output_setting not in ('m', 'c'):
        output_setting = input("Output setting: Would you like to write the "
                               "results to MongoDB [m] or print them to the console [c] or quit [q]: ").strip()
        print("\n")
        output_setting = output_setting.lower()
        test_termination(output_setting)

    while output_setting == "m" and ((mdb_name is None) or (mdb_password is None) or (mdb_cluster is None) or (mdb_database is None) or (mdb_collection is None)):
        print("Missing several monogoDB credentials. Input them (not recommended) or type q to quit")
        if mdb_name is None:
            i = input("MongoDB username: ").strip()
            mdb_name = i if i != "" else None
            test_termination(i.lower())
        if mdb_password is None:
            i = input("MongoDB password: ").strip()
            mdb_password = i if i != "" else None
            test_termination(i.lower())
        if mdb_cluster is None:
            i = input("MongoDB cluster: ").strip()
            mdb_cluster = i if i != "" else None
            test_termination(i.lower())
        if mdb_database is None:
            i = input("MongoDB database: ").strip()
            mdb_database = i if i != "" else None
            test_termination(i.lower())
        if mdb_collection is None:
            i = input("MongoDB collection: ").strip()
            mdb_collection = i if i != "" else None
            test_termination(i.lower())

    set_oauth_token(oauth_token)
    config.mongo_name = mdb_name
    config.mongo_password = mdb_password
    config.mongo_collection = mdb_collection
    config.mongo_database = mdb_database
    config.mongo_cluster = mdb_cluster

    while input_setting not in ('f', 'c', 'o'):
        input_setting = input("Input setting: Read the repo names from a file [f] or "
                              "from the console [c] or from options [o] or type [q] to exit: ").strip()
        print("\n")
        input_setting = input_setting.lower()
        test_termination(input_setting)

    if input_setting == "f" and file_name is None:
        file_name = input("File name: ").strip()

    if input_setting == "c":
        # TODO: got to be a better way to do this
        print("Enter [>quit] for the Owner name when you would like to exit the program.")

        repeat = None
        while repeat != "n":
            owner = input("\nOwner name: ").strip()
            if owner == '>quit':
                break
            repo = input("Repository name: ").strip()
            try:
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
    elif input_setting == 'o':
        read_from_options(output_setting, mdb_name, mdb_password, mdb_cluster, mdb_database, mdb_collection)
    elif input_setting == "f":
        read_from_file(file_name, output_setting, mdb_name, mdb_password, mdb_cluster, mdb_database, mdb_collection)


if __name__ == '__main__':
    main(sys.argv[1:])
