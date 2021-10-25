# Generates several text files containing a list of repositories who's
# repo. language is either Java or Python.
#
# For each language, the script generates a list of repositories from
# companies on the 2019 Fortune 500 list, the top 500 public repos sorted by
# highest star value, and the top 100 repos for each keyword in
# repo_keywords.txt sorted by highest star value.
#
# The 2019 Fortune 500 list is used because fortune.com requires users to
# subscribe for the entire 2021 list and the copy/paste method produces an
# unusable format. The 2019 Fortune 500 list is pulled from
# https://github.com/cmusam/fortune500/
#
# For eaach language, the following text files containing repos. are generated:
# - <language>_repos.txt : Master list, contains all repos found, no duplicates
# - fortune_<language>_repos.txt : Repos from fortune 500 companies
# - keyword_<language>_repos.txt : Repos from the keywords list
# - top500_<language>_repos.txt : Top 500 public repos

import sys
import requests
import time
import json
import pandas as pd
from time import sleep


BASE_URL = "https://api.github.com"
USAGE_MSG = "USAGE: python3 generate_repo_list.py " \
    "-authtoken <GitHub_OAuth_token> [-lang <java | python>] " \
    "[-limit <max_results_per_keyword_search>]"
SETTINGS = {
    "oauth_token": None,  # required
    # language default: generates a list for Java and Python
    "language": ["java", "python"],
    # limit default: adds 100 repros from the keyword search to the list
    "keyword_limit": 100
}
FORTUNE500_REPOS = {
    "java": [],
    "python": []
}


def check_cmd_line_args(args):
    if len(args) > 0 and len(args) < 7:
        try:
            token_index = args.index("-authtoken")
        except ValueError:
            print("GitHub OAuth token required")
            return False
        else:
            try:
                SETTINGS["oauth_token"] = args[token_index + 1]
            except IndexError:
                return False

        try:
            language_index = args.index("-lang")
        except ValueError:
            pass
        else:
            try:
                given_lang = args[language_index + 1]
                if given_lang not in SETTINGS.get("language"):
                    return False
                SETTINGS["language"] = [args[language_index + 1]]
            except IndexError:
                return False

        try:
            limit_index = args.index("-limit")
        except ValueError:
            pass
        else:
            try:
                SETTINGS["keyword_limit"] = int(args[limit_index + 1])
            except IndexError:
                return False

        return True
    return False


# Sleeps the program until the GitHub API rate limit of 5000 API calls per hour
# or the rate limit of 30 API calls per minute is reset.
def reached_api_rate_limit(headers):
    resp = requests.get("https://api.github.com/rate_limit", headers=headers)
    resp_JSON = json.loads(resp.content)
    if resp_JSON["resources"]["core"]["remaining"] == 0:
        reset_time = resp_JSON["resources"]["core"]["reset"]
        current_time = int(time.time())
        sleep_time = (reset_time - current_time)
        if sleep_time > 0:
            sleep_time += (5 * 60)  # add 5 minutes to make sure prog. sleeps long enough
            print(
                f'reached api rate limit of 5000 per hour.. sleeping for \
                    {sleep_time} seconds ({sleep_time/60} minutes)'
            )
            sleep(sleep_time)

    elif resp_JSON["resources"]["search"]["remaining"] == 0:
        print(
            f'reached API search rate limit of 30 per minute... \
            sleeping for 60 seconds (1 minute)'
        )
        sleep(60)


def make_get_request(url):
    headers = {
        "Authorization": "TOKEN " + SETTINGS["oauth_token"]
    }
    resp = requests.get(url, headers=headers)
    if resp.status_code == 403:
        reached_api_rate_limit(headers)
        make_get_request(url)
    return resp


def get_top_500_repos(top500_repos, lang):
    count = 0
    for page in range(1, 11):  # max pages = 10 i.e. 1000 results
        url = f'{BASE_URL}/search/repositories?q=language:{lang}&sort=stars&' \
            f'order=desc&per_page=100&page={str(page)}'
        resp = make_get_request(url)
        if resp.status_code != 200:
            raise ConnectionError('GET {} {}'.format(url, resp.status_code))

        result = json.loads(resp.content)
        for i in result["items"]:
            if count < 500:
                pre_add_len = len(top500_repos)
                top500_repos.add(i["full_name"])
                if len(top500_repos) > pre_add_len:
                    count += 1
            else:
                break
        if count == 500:
            break


# get repos related to a keyword, sorted from highest to lowest number of stars
def get_keyword_repos(keyword_repo, lang):
    keywords_error = open(f'errors_keywords.txt', "w")
    keywords_file = open('repo_keywords.txt', 'r')
    keywords_list = keywords_file.readlines()
    for i in range(len(keywords_list)):
        keyword = keywords_list[i].rstrip()
        count = 0
        for page in range(1, 11):  # max pages = 10 i.e. 1000 results
            url = f'{BASE_URL}/search/repositories?q={keyword}&sort=stars' \
                f'&language:{lang}&order=desc&per_page=100&page={str(page)}'

            resp = make_get_request(url)
            if resp.status_code != 200:
                keywords_error.write(f'[ERR - Status Code: {resp.status_code}] {keyword} \n')
            else:
                result = json.loads(resp.content)
                for j in result["items"]:
                    if count < SETTINGS["keyword_limit"]:
                        pre_add_len = len(keyword_repo)
                        keyword_repo.add(j["full_name"])
                        if len(keyword_repo) > pre_add_len:
                            count += 1
                    else:
                        break
                if count == SETTINGS["keyword_limit"]:
                    break


# gets latest Fortune 500 csv
def find_latest_csv(fortune_url):
    url = f'{fortune_url}/contents/csv'
    resp = make_get_request(url)
    if resp.status_code != 200:
        raise ConnectionError('GET {} {}'.format(url, resp.status_code))
    return json.loads(resp.content)[-1]["path"].split('/')[1]


# gets the contents from the latest Fortune 500 csv
def get_csv_content(fortune_url, repo, csv_name):
    url_branches = f'{fortune_url}/branches'
    resp = make_get_request(url_branches)
    if resp.status_code != 200:
        raise ConnectionError(
            'GET {} {}'.format(url_branches, resp.status_code)
        )
    branch = json.loads(resp.content)[0]["name"]

    url = f'https://raw.githubusercontent.com/{repo}/{branch}/csv/{csv_name}'
    resp_content = make_get_request(url)
    if resp.status_code != 200:
        raise ConnectionError(
            'GET {} {}'.format(url, resp_content.status_code)
        )
    with open(csv_name, 'wb') as file:
        file.write(resp_content.content)
    return pd.read_csv(csv_name)


def get_fortune500_companies():
    repo_name = "cmusam/fortune500"
    fortune_url = f'{BASE_URL}/repos/{repo_name}'
    csv_name = find_latest_csv(fortune_url)
    return get_csv_content(fortune_url, repo_name, csv_name)


def get_FORTUNE500_REPOS():
    fortune500_error = open(f'errors_fortune500.txt', "w")
    df_fortune500 = get_fortune500_companies()

    for company in df_fortune500["company"]:

        company_split = company.split()
        company_name = company_split[0]
        if len(company_split) > 1:
            for i in range(1, len(company_split)):
                company_name += "+"
                company_name += company_split[i]

        company_name = company_name.replace("&", "%26amp")
        url = f'{BASE_URL}/search/users?q={company_name}+type:org'

        resp = make_get_request(url)
        if resp.status_code != 200:
            fortune500_error.write(
                f'[Company ERR - Status Code: {resp.status_code}] \t {company} \n'
            )
        else:
            user_result = json.loads(resp.content)
            if user_result["total_count"] > 0:
                user = user_result["items"][0]["login"]
                company_repo_url = f'{BASE_URL}/orgs/{user}/repos'
                resp_repos = make_get_request(company_repo_url)
                if resp_repos.status_code != 200:
                    fortune500_error.write(
                        f'[Company Repo ERR - Status Code: {resp_repos.status_code}] \t {company} \n'
                    )
                else:
                    repo_result = json.loads(resp_repos.content)
                    for repo in repo_result:
                        if repo["private"] is False:
                            if repo["language"] == "Java":
                                FORTUNE500_REPOS["java"].append(
                                    repo["full_name"]
                                )
                            elif repo["language"] == "Python":
                                FORTUNE500_REPOS["python"].append(
                                    repo["full_name"]
                                )


if __name__ == '__main__':
    arguments = sys.argv[1:]
    if check_cmd_line_args(arguments):
        print(f'gathering fortune500 repos for java and python...')
        get_FORTUNE500_REPOS()

        for lang in SETTINGS["language"]:
            lang_file = open(f'{lang}_repos.txt', "w")  # master text file
            top500_lang_file = open(f'top500_{lang}_repos.txt', "w")
            keyword_lang_file = open(f'keyword_{lang}_repos.txt', "w")
            fortune_lang_file = open(f'fortune_{lang}_repos.txt', "w")
            master_repo_set = set()

            print(f'gathering top 500 public repos for language: {lang}...')
            top500_repos = set()
            get_top_500_repos(top500_repos, lang)

            print(f'writing top 500 public repos for language: {lang}...')
            for repo in top500_repos:
                top500_lang_file.write(f'{repo}\n')
                master_repo_set.add(repo)

            print(f'gathering keyword repos for language: {lang}...')
            keyword_repos = set()
            get_keyword_repos(keyword_repos, lang)

            print(f'writing keyword repos for language: {lang}...')
            for repo in keyword_repos:
                keyword_lang_file.write(f'{repo}\n')
                master_repo_set.add(repo)

            print(f'writing fortune500 repos for lang: {lang}...')
            for repo in FORTUNE500_REPOS[lang]:
                fortune_lang_file.write(f'{repo}\n')
                master_repo_set.add(repo)

            print(f'writing all repos for lang: {lang} to master file...')
            for repo in master_repo_set:
                lang_file.write(f'{repo}\n')

            print(f'finished gathering repos for language: {lang}...')
            print(f'total {lang} repos gathered: {len(master_repo_set)}\n')
    else:
        print(USAGE_MSG)
