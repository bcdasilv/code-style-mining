import sys
import os
import base64
import requests

BASE_URL = "https://api.github.com"
LOCAL_PATH = "./pyfiles"

# JSON field names
DEFAULT_BRANCH = "default_branch"
BRANCH_COMMIT = "commit"
SHA = "sha"
TREE = "tree"
FILE_TYPE = "type"
FILE_NAME = "path"
URL = "url"
PYFILE_CONTENTS = "content"

def make_get_request(url):
    resp = requests.get(url)
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

def get_file(url):
    return make_get_request(url)

# file contents are returned with newlines that must be removed for proper decoding
def strip_newlines(content):
    return content.replace("\n", "")


def main(argv):
    # TODO: error-check cmdline args
    print(argv)
    owner = argv[0]
    repo = argv[1]
    print("owner: {}\nrepo: {}".format(owner, repo))
    repo_resp = get_repo(owner, repo)
    def_branch = repo_resp.json()[DEFAULT_BRANCH]
    branch_resp = get_branch(owner, repo, def_branch)
    current_url = def_branch
    pyfile_local_path_partial = LOCAL_PATH + "/" + repo + "/" + current_url
    main_sha = branch_resp.json()[BRANCH_COMMIT][SHA] # the sha of the most recent commit to the default
    print(main_sha)
    tree_resp = get_tree(owner, repo, main_sha)
    print(tree_resp.status_code)
    #print(tree_resp.json())
    tree_contents = tree_resp.json()[TREE]
    print(tree_contents)
    for c in tree_contents:
        if c[FILE_TYPE] == "blob":
            if c[FILE_NAME].endswith(".py"):
                # if it is a Python file, run the analysis
                print("python blob: {}".format(c['path']))
                pyfile_sha = c[SHA]
                pyfile_url = c[URL]
                pyfile_resp = get_file(pyfile_url)
                pyfile_contents = base64.standard_b64decode(strip_newlines(pyfile_resp.json()[PYFILE_CONTENTS]))
                if not os.path.exists(pyfile_local_path_partial):
                    os.makedirs(pyfile_local_path_partial)
                pyfile_local_path_full = pyfile_local_path_partial + "/" + c[FILE_NAME]
                with open(pyfile_local_path_full, "wb") as f:
                    f.write(pyfile_contents)
                os.system("python3 analysis.py " + pyfile_local_path_full)
        elif c[FILE_TYPE] == "tree":
            # if it is a directory, drill down
            print("dir: {}".format(c['path']))
        else:
            print(c['type'])


if __name__ == '__main__':
    main(["django", "django"])
    #main(sys.argv[1:])
