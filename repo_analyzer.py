import base64
import os
import re
import shutil
from collections import defaultdict
from datetime import datetime
from itertools import chain

import requests
import file_analyzer

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
FILE_RESULTS_GH_PATH = 1
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
C_TOTAL_BLANK_LOC = "total_blank_loc"
C_TOTAL_CAT = "total_category_errors"
C_TOTAL_COMMENTS_LOC = "total_comments_loc"
C_TOTAL_DOUBLE_DOCSTRINGS_LOC = "total_double_docstrings_loc"
C_TOTAL_FILE = "total_file_errors"
C_TOTAL_LINE_COUNT = "total_line_count"
C_TOTAL_REPO = "total_repo_errors"
C_TOTAL_SINGLE_COMMENTS_LOC = "total_single_comments_loc"
C_TOTAL_SINGLE_DOCSTRINGS_LOC = "total_single_docstrings_loc"
C_TOTAL_SRC_LOC = "total_src_loc"
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
                      C_TOTAL_SRC_LOC: 0,
                      C_TOTAL_SINGLE_COMMENTS_LOC: 0,
                      C_TOTAL_SINGLE_DOCSTRINGS_LOC: 0,
                      C_TOTAL_DOUBLE_DOCSTRINGS_LOC: 0,
                      C_TOTAL_BLANK_LOC: 0,
                      C_TOTAL_COMMENTS_LOC: 0,
                      C_TOTAL_LINE_COUNT: 0,
                      C_TOTAL_CAT: {NAMES: 0, INDENTS: 0, TABS: 0,
                                    LENGTH: 0, BLANKS: 0, IMPORTS: 0}}}

# MongoDB field names
MDB_ID = "_id"

TOKEN = None


def reset_summary():
    global C_DEF_SUMMARY
    C_DEF_SUMMARY = {C_SUMMARY:
                         {C_FILE_COUNT: 0,
                          C_ANALYZED_COUNT: 0,
                          C_TOTAL_REPO: 0,
                          C_TOTAL_SRC_LOC: 0,
                          C_TOTAL_SINGLE_COMMENTS_LOC: 0,
                          C_TOTAL_SINGLE_DOCSTRINGS_LOC: 0,
                          C_TOTAL_DOUBLE_DOCSTRINGS_LOC: 0,
                          C_TOTAL_BLANK_LOC: 0,
                          C_TOTAL_COMMENTS_LOC: 0,
                          C_TOTAL_LINE_COUNT: 0,
                          C_TOTAL_CAT: {NAMES: 0, INDENTS: 0, TABS: 0,
                                        LENGTH: 0, BLANKS: 0, IMPORTS: 0}}}
    return C_DEF_SUMMARY


# work around function to bring summary to top level without major analysis refactoring
def summary_top_level(repo_analysis):
    summary = repo_analysis[C_REPO_ANALYSIS][C_SUMMARY]
    repo_analysis["summary"] = summary
    del repo_analysis[C_REPO_ANALYSIS][C_SUMMARY]
    return repo_analysis


# Set the GitHub OAuth TOKEN used for get requests
def set_oauth_token(oauth):
    global TOKEN
    TOKEN = oauth


# Make the get request from the GitHub v3 REST API
def make_get_request(url):
    headers = {"Authorization": "TOKEN " + TOKEN}
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


# Return file contents without newlines
# (Newlines ust be removed for proper decoding later)
def strip_newlines(content):
    return content.replace("\n", "")


# Create the nested directories in the path locally
def create_dirs(path):
    folders = re.split("/", path)
    acc_path = ""
    for f in folders:
        acc_path += f + "/"
        if not os.path.exists(acc_path):
            os.makedirs(acc_path)


# Delete the local nested directories
def delete_local_tree_clone(path):
    try:
        shutil.rmtree(path)
    except FileNotFoundError:
        pass


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


# Clone the file locally, run the analysis, and collect the results to be added to the JSON later
def process_file(blob, gh_path_partial):
    pyfile_resp = get_file(blob[GH_URL])
    pyfile_json = pyfile_resp.json()
    pyfile_contents = base64.standard_b64decode(strip_newlines(pyfile_json[PYFILE_CONTENTS]))
    pyfile_local_path_full = LOCAL_PATH + "/" + gh_path_partial + "/" + blob[GH_FILE_NAME]
    path_without_file_name = pyfile_local_path_full[0:pyfile_local_path_full.rfind("/")]

    # Clone the file locally
    if not os.path.exists(path_without_file_name):
        create_dirs(path_without_file_name)
    with open(pyfile_local_path_full, "wb") as f:
        f.write(pyfile_contents)

    # Run the analysis
    # Add the metadata from the GitHub API
    dict_results = {C_FILE_NAME: blob[GH_FILE_NAME],
                    C_FILE_PATH: gh_path_partial + "/" + blob[GH_FILE_NAME],
                    C_SHA: pyfile_json[GH_SHA],
                    C_SIZE: pyfile_json[GH_SIZE],
                    C_NODE_ID: pyfile_json[GH_NODE_ID],
                    C_URL: pyfile_json[GH_URL],
                    C_PROCESSED: str(datetime.now())}
    # Add the file analysis results
    analysis_results = file_analyzer.collect_file_dict_results(pyfile_local_path_full) # Syntax Error
    dict_results.update(analysis_results[0])
    # Delete the local copy of the file before returning analysis results
    os.remove(pyfile_local_path_full)
    return dict_results, gh_path_partial + "/" + blob[GH_FILE_NAME], analysis_results[1]


# Analyze all Python files found in the repository tree
def check_recursive_tree_contents(contents, local_path_partial, repo):
    master_results = reset_summary()
    file_count = 0
    analyzed_count = 0
    for c in contents:
        if c[GH_FILE_TYPE] == "blob":
            # file count in summary
            file_count += 1
            # If it is a non-empty Python file, run the analysis
            if c[GH_FILE_NAME].endswith(FILE_EXTENSION) and c[GH_SIZE] != 0:
                # analyzed count in summary
                analyzed_count += 1

                # Status update printed to console
                #print("    Python blob: {}".format(c[GH_FILE_NAME]))

                file_results = process_file(c, local_path_partial)
                gh_partial_path = file_results[FILE_RESULTS_GH_PATH]
                gh_path = gh_partial_path[len(repo) + 1:len(gh_partial_path) - len(FILE_EXTENSION)]
                master_results[re.sub("[.]", "", gh_path)] = file_results[FILE_RESULTS_DICT]
                master_results[C_SUMMARY][C_TOTAL_REPO] +=\
                    file_results[FILE_RESULTS_DICT][C_TOTAL_FILE]
                curr = master_results[C_SUMMARY][C_TOTAL_CAT]
                new = file_results[FILE_RESULTS_ERRS]
                temp = defaultdict(list)
                for a, b in chain(curr.items(), new.items()):
                    temp[a] = curr[a] + b
                master_results[C_SUMMARY][C_TOTAL_CAT] = temp

                master_results[C_SUMMARY][C_TOTAL_SRC_LOC] += file_results[FILE_RESULTS_DICT]["src_loc_analysis"]
                master_results[C_SUMMARY][C_TOTAL_BLANK_LOC] += file_results[FILE_RESULTS_DICT]["blank_loc_analysis"]
                master_results[C_SUMMARY][C_TOTAL_SINGLE_COMMENTS_LOC] += file_results[FILE_RESULTS_DICT]["single_comments_loc_analysis"]
                master_results[C_SUMMARY][C_TOTAL_SINGLE_DOCSTRINGS_LOC] += file_results[FILE_RESULTS_DICT]["single_docstring_loc_analysis"]
                master_results[C_SUMMARY][C_TOTAL_DOUBLE_DOCSTRINGS_LOC] += file_results[FILE_RESULTS_DICT]["double_docstring_loc_analysis"]
                master_results[C_SUMMARY][C_TOTAL_COMMENTS_LOC] += file_results[FILE_RESULTS_DICT]["total_comments_loc_analysis"]
                master_results[C_SUMMARY][C_TOTAL_LINE_COUNT] += file_results[FILE_RESULTS_DICT]["line_count_analysis"]
    master_results[C_SUMMARY][C_FILE_COUNT] = file_count
    master_results[C_SUMMARY][C_ANALYZED_COUNT] = analyzed_count
    return master_results


# Query a repo from GitHub and run the analyzer on the default branch of the repo
def analyze_repo(owner, repo):
    print("\nAnalyzing " + owner + "'s " + repo + " repository...")

    # Get the repo
    repo_resp = get_repo(owner, repo)
    repo_json_dict = collect_repo_json_dict(repo_resp.json())
    def_branch = repo_resp.json()[GH_DEFAULT_BRANCH]  # TODO: only explores default branch

    # Get the default branch of the repo
    branch_resp = get_branch(owner, repo, def_branch)
    current_url = def_branch
    gh_path_partial = repo + "/" + current_url

    # The SHA of the most recent commit to the default branch
    main_sha = branch_resp.json()[GH_BRANCH_COMMIT][GH_SHA]

    tree_resp = get_tree_recursive(owner, repo, main_sha)
    #Syntax Error
    analysis_results = check_recursive_tree_contents(tree_resp.json()[GH_TREE],
                                                     gh_path_partial, repo)
    repo_json_dict[C_REPO_ANALYSIS] = analysis_results
    # workaround fix for json summary @ top level
    #summary = repo_json_dict[C_REPO_ANALYSIS][C_SUMMARY]
    #repo_json_dict["summary"] = summary
    #del repo_json_dict[C_REPO_ANALYSIS][C_SUMMARY]
    repo_json_dict = summary_top_level(repo_json_dict)
    return repo_json_dict
