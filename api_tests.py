import base64
from collections import defaultdict
from datetime import datetime
from itertools import chain
import json
import pymongo
import os
import re
import requests
import shutil
import sys

import analysis

BASE_URL = "https://api.github.com"
LOCAL_PATH = "./pyfiles"
FILE_EXTENSION = ".py"

# GitHub JSON field names
GH_BRANCH_COMMIT = "commit"
GH_DEFAULT_BRANCH = "default_branch"
GH_FILE_TYPE = "type"
GH_FILE_NAME = "path"
GH_HTML_URL = "html_url"
GH_LOGIN = "login"
GH_NODE_ID = "node_id"
GH_REPO_FULL_NAME = "full_name"
GH_REPO_LANG = "language"
GH_REPO_NAME = "name"
GH_REPO_ORG = "organization"
GH_REPO_OWNER = "owner"
GH_REPO_PRIVATE = "private"
GH_SHA = "sha"
GH_SIZE = "size"
GH_TREE = "tree"
GH_TRUNCATED = "truncated"
GH_URL = "url"

PYFILE_CONTENTS = "content"
FILE_RESULTS_DICT = 0
FILE_RESULTS_LOCAL_PATH = 1
FILE_RESULTS_ERRS = 2

# Custom JSON field names
C_ANALYZED_COUNT = "analyzed_file_count"
C_FILE_COUNT = "file_count"
C_FILE_NAME = "file_name"
C_FILE_PATH = "file_path"
C_HTML_URL = "html_url"
C_LOGIN = "login"
C_NODE_ID = "node_id"
C_PROCESSED = "date_processed"
C_REPO_FULL_NAME = "full_name"
C_REPO_LANG = "language"
C_REPO_NAME = "name"
C_REPO_ORG = "organization"
C_REPO_OWNER = "owner"
C_REPO_PRIVATE = "private"
C_REPO_ANALYSIS = "repo_analysis"
C_SHA = "sha"
C_SIZE = "size"
C_SUMMARY = "summary"
C_TOTAL_CAT = "total_category_errors"
C_TOTAL_FILE = "total_file_errors"
C_TOTAL_REPO = "total_repo_errors"
C_TYPE = "type"
C_URL = "url"


NAMES = "naming"
INDENTS = "indentation"
TABS = "tabs_vs_spaces"
LENGTH = "line_length"
BLANKS = "blank_lines"
IMPORTS = "import"
ENCODING = "file_encoding"
C_DEF_SUMMARY = {C_SUMMARY:
                     {C_FILE_COUNT: 0,
                      C_ANALYZED_COUNT: 0,
                      C_TOTAL_REPO: 0,
                      C_TOTAL_CAT: {NAMES: 0, INDENTS: 0, TABS: 0, LENGTH: 0, BLANKS: 0, IMPORTS: 0}}}

# MongoDB field names
MDB_ID = "_id"

token = "void"


def make_get_request(url):
    headers = {"Authorization": "token " + token}
    resp = requests.get(url, headers=headers)
    if resp.status_code != 200:
        raise ConnectionError('GET {} {}'.format(url[len(BASE_URL):], resp.status_code))
    else:
        return resp

def repo_url(owner, repo):
    return BASE_URL + "/repos/" + owner + "/" + repo

def get_repo(owner, repo):
    url = repo_url(owner, repo)
    return make_get_request(url)

def get_branch(owner, repo, branch):
    branch_url = repo_url(owner, repo) + "/branches/" + branch
    return make_get_request(branch_url)

def get_tree(owner, repo, sha):
    tree_url = repo_url(owner, repo) + "/git/trees/" + sha
    return make_get_request(tree_url)

def get_tree_recursive(owner, repo, sha):
    tree_url = repo_url(owner, repo) + "/git/trees/" + sha + "?recursive=1"
    return make_get_request(tree_url)

def get_file(url):
    return make_get_request(url)

def create_dirs(path):
    folders = re.split("/", path)
    acc_path = ""
    for f in folders:
        acc_path += f + "/"
        if not os.path.exists(acc_path):
            os.makedirs(acc_path)

# Clone the file locally, run the analysis, and collect the results to be added to the JSON later
def process_file(blob, pyfile_local_path_partial):
    pyfile_resp = get_file(blob[GH_URL])
    pyfile_json = pyfile_resp.json()
    pyfile_contents = base64.standard_b64decode(strip_newlines(pyfile_json[PYFILE_CONTENTS]))
    pyfile_local_path_full = pyfile_local_path_partial + "/" + blob[GH_FILE_NAME]

    path_without_file_name = pyfile_local_path_full[0:pyfile_local_path_full.rfind("/")]

    # Clone the file locally
    if not os.path.exists(path_without_file_name):
        create_dirs(path_without_file_name)
    with open(pyfile_local_path_full, "wb") as f:
        f.write(pyfile_contents)
    # Run the analysis
    # Add the metadata from the GitHub API
    dict_results = {C_FILE_NAME: blob[GH_FILE_NAME],
                    C_FILE_PATH: pyfile_local_path_full,
                    C_SHA: pyfile_json[GH_SHA],
                    C_SIZE: pyfile_json[GH_SIZE],
                    C_NODE_ID: pyfile_json[GH_NODE_ID],
                    C_URL: pyfile_json[GH_URL],
                    C_PROCESSED: str(datetime.now())}
    # Add the file analysis results
    analysis_results = analysis.collect_file_dict_results(pyfile_local_path_full)
    dict_results.update(analysis_results[0])
    # Delete the local copy of the file before returning analysis results
    os.remove(pyfile_local_path_full)
    return dict_results, pyfile_local_path_full, analysis_results[1]


# TODO: fix this to work properly. compare to recursive. add file count fields
def check_tree_contents(contents, pyfile_local_path_partial, owner, repo, master_results=C_DEF_SUMMARY):
    for c in contents:
        if c[GH_FILE_TYPE] == "blob":
            # TODO: don't process empty files
            if c[GH_FILE_NAME].endswith(FILE_EXTENSION): # If it is a Python file, run the analysis
                print("    Python blob: {}".format(c[GH_FILE_NAME])) # Status update printed to console
                file_results = process_file(c, pyfile_local_path_partial)
                local_path = file_results[FILE_RESULTS_LOCAL_PATH]
                if local_path in master_results.keys():
                    raise Exception("A file with the path {} already exists.".format(local_path))
                else:
                    local_prefix = LOCAL_PATH + "/" + repo + "/"
                    start_gh_path = local_path.find(local_prefix) + len(local_prefix)
                    github_path = local_path[start_gh_path : len(local_path) - len(FILE_EXTENSION)]
                    master_results[re.sub("[.]", "", github_path)] = file_results[FILE_RESULTS_DICT]
                    master_results[C_SUMMARY][C_TOTAL_REPO] += file_results[FILE_RESULTS_DICT][C_TOTAL_FILE]
                    curr = master_results[C_SUMMARY][C_TOTAL_CAT]
                    new = file_results[FILE_RESULTS_ERRS]
                    temp = defaultdict(list)
                    for a, b in chain(curr.items(), new.items()):
                        temp[a] = curr[a] + b
                    master_results[C_SUMMARY][C_TOTAL_CAT] = temp
        elif c[GH_FILE_TYPE] == "tree":
            # If it is a directory, drill down
            print("    dir: {}".format(c[GH_FILE_NAME])) # Status update printed to console
            check_tree_contents(get_tree(owner, repo, c[GH_SHA]).json()[GH_TREE],
                                pyfile_local_path_partial + "/" + c[GH_FILE_NAME],
                                owner,
                                repo,
                                master_results)
    return master_results

def check_recursive_tree_contents(contents, pyfile_local_path_partial, owner, repo, master_results=C_DEF_SUMMARY):
    file_count = 0
    analyzed_count = 0
    for c in contents:
        if c[GH_FILE_TYPE] == "blob":
            file_count += 1
            if c[GH_FILE_NAME].endswith(FILE_EXTENSION) and c[GH_SIZE] is not 0: # If it is a non-empty Python file, run the analysis
                analyzed_count += 1
                print("    Python blob: {}".format(c[GH_FILE_NAME])) # Status update printed to console
                file_results = process_file(c, pyfile_local_path_partial)
                local_path = file_results[FILE_RESULTS_LOCAL_PATH]
                if local_path in master_results.keys():
                    raise Exception("A file with the path {} already exists.".format(local_path))
                else:
                    local_prefix = LOCAL_PATH + "/" + repo + "/"
                    start_gh_path = local_path.find(local_prefix) + len(local_prefix)
                    github_path = local_path[start_gh_path : len(local_path) - len(FILE_EXTENSION)]
                    master_results[re.sub("[.]", "", github_path)] = file_results[FILE_RESULTS_DICT]
                    master_results[C_SUMMARY][C_TOTAL_REPO] += file_results[FILE_RESULTS_DICT][C_TOTAL_FILE]
                    curr = master_results[C_SUMMARY][C_TOTAL_CAT]
                    new = file_results[FILE_RESULTS_ERRS]
                    temp = defaultdict(list)
                    for a, b in chain(curr.items(), new.items()):
                        temp[a] = curr[a] + b
                    master_results[C_SUMMARY][C_TOTAL_CAT] = temp
    master_results[C_SUMMARY][C_FILE_COUNT] = file_count
    master_results[C_SUMMARY][C_ANALYZED_COUNT] = analyzed_count
    # Remove the local clone of the file directories before returning
    shutil.rmtree(LOCAL_PATH + "/" + repo)
    return master_results

# Returns a dictionary of the Login, Node ID, URL, HTML URL, and Type
# Used for Owner and Organization objects
def collect_repo_simple_dict(obj):
    return {C_LOGIN: obj[GH_LOGIN],
            C_NODE_ID: obj[GH_NODE_ID],
            C_URL: obj[GH_URL],
            C_HTML_URL: obj[GH_HTML_URL],
            C_TYPE: obj[GH_FILE_TYPE]}

# All of the information for the repo
def collect_repo_json_dict(resp):
    repo_dict = {MDB_ID: resp[GH_NODE_ID],
                 C_NODE_ID: resp[GH_NODE_ID],
                 C_REPO_NAME: resp[GH_REPO_NAME],
                 C_REPO_FULL_NAME: resp[GH_REPO_FULL_NAME],
                 C_REPO_OWNER: collect_repo_simple_dict(resp[GH_REPO_OWNER]),
                 C_REPO_PRIVATE: resp[GH_REPO_PRIVATE],
                 C_HTML_URL: resp[GH_HTML_URL],
                 C_URL: resp[GH_URL],
                 C_SIZE: resp[GH_SIZE],
                 C_REPO_LANG: resp[GH_REPO_LANG],
                 C_REPO_ORG: None,
                 C_REPO_ANALYSIS: None}
    if GH_REPO_ORG in resp:
        repo_dict[C_REPO_ORG] = collect_repo_simple_dict(resp[GH_REPO_ORG])
    return repo_dict

# Write data to a cluster's database with a specific collection name
# Authenticate with given username and password
def write_to_mongodb(mongodb_user, mongodb_password, cluster, db_name, coll_name, data):
    client = pymongo.MongoClient(
        "mongodb+srv://" + mongodb_user + ":" + mongodb_password + "@" + cluster + "/test?retryWrites=true"
    )
    db = client[db_name]
    coll = db[coll_name]
    mongo_id = coll.insert_one(data)
    return mongo_id

# Return file contents without newlines
# (Newlines ust be removed for proper decoding later)
def strip_newlines(content):
    return content.replace("\n", "")

def check_input(str, msg):
    if not str.isalnum():
        raise ValueError(msg)

# TODO: update this to be consistent with main? or delete it entirely
def get_repo_results(owner, repo):
    global token
    token = input("OAuth token: ")
    check_input(token, "That is not a valid token. Please obtain an OAuth token from GitHub.")

    # Get the repo
    repo_resp = get_repo(owner, repo)
    repo_json_dict = collect_repo_json_dict(repo_resp.json())
    def_branch = repo_resp.json()[GH_DEFAULT_BRANCH]  # TODO: only explores default branch

    # Get the default branch of the repo
    branch_resp = get_branch(owner, repo, def_branch)
    current_url = def_branch
    pyfile_local_path_partial = LOCAL_PATH + "/" + repo + "/" + current_url
    main_sha = branch_resp.json()[GH_BRANCH_COMMIT][GH_SHA]  # The SHA of the most recent commit to the default branch

    # Get the tree for the default branch
    tree_resp = get_tree(owner, repo, main_sha)
    analysis_results = check_tree_contents(tree_resp.json()[GH_TREE], pyfile_local_path_partial, owner, repo)
    repo_json_dict[C_REPO_ANALYSIS] = analysis_results

    tree_resp2 = get_tree_recursive(owner, repo, main_sha)

    master_name = owner + "/" + repo
    master_dict = {master_name: repo_json_dict}
    master_json = json.dumps(master_dict)
    return master_json


def main(argv):

    global token
    token = input("OAuth token: ")
    check_input(token, "That is not a valid token. Please obtain an OAuth token from GitHub.")

    """mult = ""
    while mult != "y" and mult != "n":
        mult = input("Would you like to analyze multiple repositories? [y/n]")
    if mult == "n":
        owner = input("Repo Owner: ")
        #check_input(owner, "That is not a valid owner username. Please double-check your input.")
        repo = input("Repository Name: ")
        #check_input(repo, "That is not a valid repository name. Please double-check your input.")"""

    mdb_name = input("MongoDB username: ")
    mdb_password = input("MongoDB password: ")
    mdb_cluster = input("MongoDB cluster: ")
    mdb_database = input("MongoDB database: ")

    file_name = input("File Name: ")
    infile = open(file_name, "r")
    lines = infile.readlines()

    for i in range(len(lines)):
        if lines[i].startswith("#") or lines[i].startswith(" ") or lines[i] is "" or lines[i] is "\n":
            continue
        temp = re.split("/|\n", lines[i])
        owner = temp[0]
        repo = temp[1]
        print("Analyzing " + owner + "'s " + repo + " repository...")

        # Get the repo
        repo_resp = get_repo(owner, repo)
        repo_json_dict = collect_repo_json_dict(repo_resp.json())
        def_branch = repo_resp.json()[GH_DEFAULT_BRANCH] # TODO: only explores default branch

        # Get the default branch of the repo
        branch_resp = get_branch(owner, repo, def_branch)
        current_url = def_branch
        pyfile_local_path_partial = LOCAL_PATH + "/" + repo + "/" + current_url
        main_sha = branch_resp.json()[GH_BRANCH_COMMIT][GH_SHA] # The SHA of the most recent commit to the default branch

        tree_resp = get_tree_recursive(owner, repo, main_sha)
        if tree_resp.json()[GH_TRUNCATED] is True:
            # Get the tree for the default branch
            tree_resp = get_tree(owner, repo, main_sha)
            analysis_results = check_tree_contents(tree_resp.json()[GH_TREE], pyfile_local_path_partial, owner, repo)
        else:
            analysis_results = check_recursive_tree_contents(tree_resp.json()[GH_TREE], pyfile_local_path_partial, owner, repo)
        repo_json_dict[C_REPO_ANALYSIS] = analysis_results

        master_name = owner + "/" + repo
        master_dict = {master_name: repo_json_dict}
        master_json = json.dumps(master_dict)

        print(master_json)

        write_to_mongodb(mdb_name, mdb_password, mdb_cluster, mdb_database, master_name, repo_json_dict)
        print("Finished analyzing " + owner + "'s " + repo + " repository. Wrote results to MongoDB.")




if __name__ == '__main__':
    main(sys.argv[1:])


