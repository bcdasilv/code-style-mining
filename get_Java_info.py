import os.path
import re
import requests
import time
import json
import config
import pymongo
from time import sleep
from verify_repo_mongo import check_duplicate_repo

BASE_URL = "https://api.github.com"

# Settings Keys
OAUTH = "OAUTH_TOKEN"
REPO_FILE = "REPO_FILE"
MDB_USER = "MDB_USERNAME"
MDB_PASS = "MDB_PASSWORD"
MDB_URL = "MDB_URL"
MDB_DB = "MDB_DATABASE"
MDB_COLL = "MDB_COLLECTION"

SETTINGS = {
    OAUTH: None,
    REPO_FILE: None,
    MDB_USER: None,
    MDB_PASS: None,
    MDB_URL: None,
    MDB_DB: None,
    MDB_COLL: None
}

ORIGOAUTH = "orig_oauth_token"
ALTOAUTH1 = "alt_oauth_1"
ALT1USED = "alt_oauth_1_used"
ALTOAUTH2 = "alt_oauth_2"
ALT2USED = "alt_oauth_2_used"

TOKENS = {
    ORIGOAUTH: None,
    ALTOAUTH1: None,
    ALT1USED: False,
    ALTOAUTH2: None,
    ALT2USED: False
}

# GH Keys
FULL_NAME = "full_name"
DEFAULT_BRANCH = "default_branch"
REPO_NAME = "name"
REPO_PRIVACY = "private"
REPO_LANG = "language"
OWNER = "owner"
OWNER_LOGIN = "login"
ID = "node_id"
TYPE = "type"
COMMIT = "commit"
SHA = "sha"
TREE = "tree"
FILE_TYPE = "type"
PATH = "path"
FILE_URL = "url"

# Self Created Keys
OWNER_NAME = "owner_name"
OWNER_ID = "owner_id"
OWNER_TYPE = "owner_type"
REPO_ID = "repo_id"
FILE_ID = "file_id"
FILE_NAME = "file_name"
MAIN_SHA = "main_sha"
FILES = "files"

FILE_INFO = {
    FILE_ID: None,
    FILE_NAME: None
}

JAVA_INFO = {
    # full name
    # Owner Name (owner.login)
    # Owner ID (owner.node_id)
    # Owner Type (owner.type)
    # Repo ID (node_id)
    # Repo Name (name)
    # Repo Privacy Setting (private)
    # Repo Language (language)
    # File ID (repo_analysis.<file_name>.node_id)

    FULL_NAME: None,
    DEFAULT_BRANCH: None,
    MAIN_SHA: None,
    OWNER_NAME: None,
    OWNER_ID: None,
    OWNER_TYPE: None,
    REPO_ID: None,
    REPO_NAME: None,
    REPO_PRIVACY: None,
    REPO_LANG: None,
    FILES: {}
}


def reset_file_info():
    global FILE_INFO
    FILE_INFO = {
        FILE_ID: None,
        FILE_NAME: None
    }


def reset_java_info():
    global JAVA_INFO
    JAVA_INFO = {
        FULL_NAME: None,
        DEFAULT_BRANCH: None,
        MAIN_SHA: None,
        OWNER_NAME: None,
        OWNER_ID: None,
        OWNER_TYPE: None,
        REPO_ID: None,
        REPO_NAME: None,
        REPO_PRIVACY: None,
        REPO_LANG: None,
        FILES: {}
    }


def check_settings():
    missing_key_msg = "The following info are missing: \n"
    missing_keys = 0
    for key in SETTINGS:
        if SETTINGS[key] is None:
            missing_key_msg += f'{key}\n'
            missing_keys += 1
    if missing_keys != 0:
        print(missing_key_msg)
        return False
    return True


def get_settings():
    if os.path.exists("javaInfo.properties"):
        file = open('javaInfo.properties', 'r')
        line = file.readline()
        while line:
            conf_line = [l.strip() for l in line.split('=')]
            line = file.readline()
            if conf_line[0] == "authToken":
                SETTINGS[OAUTH] = conf_line[1]
            elif conf_line[0] == "repoPath":
                SETTINGS[REPO_FILE] = conf_line[1]
            elif conf_line[0] == "mongoUsername":
                SETTINGS[MDB_USER] = conf_line[1]
            elif conf_line[0] == "mongoPassword":
                SETTINGS[MDB_PASS] = conf_line[1]
            elif conf_line[0] == "mongoUrl":
                SETTINGS[MDB_URL] = conf_line[1]
            elif conf_line[0] == "mongoDatabase":
                SETTINGS[MDB_DB] = conf_line[1]
            elif conf_line[0] == "mongoCollection":
                SETTINGS[MDB_COLL] = conf_line[1]
            elif conf_line[0] == "ALTOAUTH1":
                TOKENS[ALTOAUTH1] = conf_line[1]
            elif conf_line[0] == "ALTOAUTH2":
                TOKENS[ALTOAUTH2] = conf_line[1]
        file.close()
        return check_settings()
    print("No javaInfo.properties file found...")
    return False


def set_settings():
    config.mongo_name = SETTINGS[MDB_USER]
    config.mongo_password = SETTINGS[MDB_PASS]
    config.mongo_collection = SETTINGS[MDB_COLL]
    config.mongo_database = SETTINGS[MDB_DB]
    config.mongo_cluster = SETTINGS[MDB_URL]


# Sleeps the program until the GitHub API rate limit of 5000 API calls per hour
# or the rate limit of 30 API calls per minute is reset.
def reached_api_rate_limit(headers):
    if TOKENS[ORIGOAUTH] is None:
        TOKENS[ORIGOAUTH] = SETTINGS[OAUTH]
        SETTINGS[OAUTH] = TOKENS[ALTOAUTH1]
        TOKENS[ALT1USED] = True
        print('switched to alt oauth token 1...')
        return
    elif TOKENS[ALT1USED] is True and TOKENS[ALT2USED] is False:
        SETTINGS[OAUTH] = TOKENS[ALTOAUTH2]
        TOKENS[ALT2USED] = True
        print('switched to alt oauth token 2...')
        return
    elif TOKENS[ALT1USED] is True and TOKENS[ALT2USED] is True:
        SETTINGS[OAUTH] = TOKENS[ORIGOAUTH]
        TOKENS[ORIGOAUTH] = None
        TOKENS[ALT1USED] = False
        TOKENS[ALT2USED] = False
        print('switched to orig oauth token...')

    resp = requests.get(f'{BASE_URL}/rate_limit', headers=headers)
    resp_JSON = json.loads(resp.content)
    if resp_JSON["resources"]["core"]["remaining"] == 0:
        reset_time = resp_JSON["resources"]["core"]["reset"]
        current_time = int(time.time())
        sleep_time = (reset_time - current_time)
        if sleep_time > 0:
            sleep_time += (5 * 60)  # add 5 min
            print(
                f'reached api rate limit of 5000 per hour.. sleeping for \
                {sleep_time} seconds ({sleep_time/60} minutes)'
            )
            sleep(sleep_time)

    elif resp_JSON["resources"]["search"]["remaining"] == 0:
        print(
            f'reached API search rate limit of 30 per minute... sleeping for \
            60 seconds (1 minute)'
        )
        sleep(60)


def make_get_request(url):
    resp = requests.get(url)
    headers = {
        "Authorization": "TOKEN " + SETTINGS[OAUTH]
    }
    resp = requests.get(url, headers=headers)
    if resp.status_code == 403:
        reached_api_rate_limit(headers)
        make_get_request(url)
    return resp


def handle_repo_results(resp):
    result = json.loads(resp.content)
    JAVA_INFO[FULL_NAME] = result[FULL_NAME]
    JAVA_INFO[DEFAULT_BRANCH] = result[DEFAULT_BRANCH]
    JAVA_INFO[OWNER_NAME] = result[OWNER][OWNER_LOGIN]
    JAVA_INFO[OWNER_ID] = result[OWNER][ID]
    JAVA_INFO[OWNER_TYPE] = result[OWNER][TYPE]
    JAVA_INFO[REPO_ID] = result[ID]
    JAVA_INFO[REPO_NAME] = result[REPO_NAME]
    JAVA_INFO[REPO_PRIVACY] = result[REPO_PRIVACY]
    JAVA_INFO[REPO_LANG] = result[REPO_LANG]


def get_main_sha(owner, repo):
    url = f'{BASE_URL}/repos/{owner}/{repo}/branches/{JAVA_INFO[DEFAULT_BRANCH]}'
    resp = make_get_request(url)
    if resp.status_code != 200:
        return False
    JAVA_INFO[MAIN_SHA] = json.loads(resp.content)[COMMIT][SHA]
    return True


def get_repo_files(owner, repo):
    url = f'{BASE_URL}/repos/{owner}/{repo}/git/trees/{JAVA_INFO[MAIN_SHA]}?recursive=1'
    resp = make_get_request(url)
    if resp.status_code != 200:
        return False
    repo_files = json.loads(resp.content)[TREE]
    print(f'\t ... {len(repo_files)} files to get')
    for i in range(len(repo_files)):
        if i > 0 and i % 1000 == 0:
            print(f'\t ... got {i} files so far')
        reset_file_info()
        rf = repo_files[i]
        if rf[FILE_TYPE] == "blob":
            FILE_INFO[FILE_NAME] = rf[PATH]
            file_resp = make_get_request(rf[FILE_URL])
            if file_resp.status_code == 200:
                FILE_INFO[FILE_ID] = json.loads(file_resp.content)[ID]
            JAVA_INFO[FILES][str(i)] = FILE_INFO
    return True


def write_to_mongo():
    client = pymongo.MongoClient(
        "mongodb+srv://" + SETTINGS[MDB_USER] + ":" + SETTINGS[MDB_PASS] +
        "@" + SETTINGS[MDB_URL] + "/test?retryWrites=true" +
        "&w=majority&tls=true&tlsAllowInvalidCertificates=true"
    )
    db = client[SETTINGS[MDB_DB]]
    coll = db[SETTINGS[MDB_COLL]]
    mongo_id = coll.insert_one(JAVA_INFO)

    return mongo_id


# TODO: check if changing the mongo url in main and config can be switched back
if __name__ == '__main__':
    if get_settings():
        set_settings()
        file_repos = open(f'{SETTINGS[REPO_FILE]}', 'r')
        file_error = open(f'java_info_errors.txt', "a")
        file_analyzed = open(f'java_info_analyzed.txt', "a")
        java_repos = file_repos.readlines()

        for i in range(len(java_repos)):
            reset_java_info()
            temp = re.split("/|\n", java_repos[i])
            owner = temp[0]
            repo = temp[1]
            print(f'getting info for {owner}\'s repo: {repo} ({i + 1}/{len(java_repos)})')
            if check_duplicate_repo(owner, repo):
                url = f'{BASE_URL}/repos/{owner}/{repo}'
                resp = make_get_request(url)

                if resp.status_code != 200:
                    print(f'error connecting to repo \n')
                    file_error.write(f'[REPO ERR] \t {owner}/{repo}\n')
                else:
                    print(f'\t ... got general repo info.')
                    handle_repo_results(resp)

                    write_to_mongo()
                    file_analyzed.write(f'{owner}/{repo}\n')

                    # if get_main_sha(owner, repo):
                    #     print(f'\t ... got main SHA.')
                    #     if get_repo_files(owner, repo):
                    #         print(f'\t ... finished getting data for repo, writing data to mongo... \n')
                    #         write_to_mongo()
                    #         file_analyzed.write(f'{owner}/{repo}\n')
                    #     else:
                    #         print(f'error getting files \n\n')
                    #         file_error.write(f'[FILE ERR] \t {owner}/{repo}\n')
                    # else:
                    #     print(f'error getting main SHA \n\n')
                    #     file_error.write(f'[SHA ERR] \t {owner}/{repo}\n')
            else:
                print(f'Already have data for {repo} by {owner}, skipping...\n')
