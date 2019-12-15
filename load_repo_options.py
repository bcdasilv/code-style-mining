import json
import requests
import time

# https://developer.github.com/v3/search/#rate-limit
# The Search API has a custom rate limit. For requests using Basic Authentication,
# OAuth, or client ID and secret, you can make up to 30 requests per minute.
# For unauthenticated requests, the rate limit allows you to make up to 10 requests per minute.


# I'll leave this script with anonymous requests for the sake of simplicity
# TODO: make authenticated requests. It'll increase the performance a little
def load_top_keyword_repos(number_repos, given_list=[]):
    f_keywords = open('repo_keywords.txt', 'r')

    if len(given_list) == 0:
        lines = f_keywords.readlines()
    else:
        lines = given_list
    unique_repos = set()

    # For now, I've been using a list of 100 key words
    for i in range(len(lines)):
        if number_repos is not None and len(unique_repos) >= number_repos:
            break

        if lines[i].startswith("#") or lines[i].startswith(" ") \
                or lines[i] == "" or lines[i] == "\n":
            continue
        keyword = lines[i].rstrip()

        # https://api.github.com/search/repositories?q=language:python&sort=stars&order=desc
        url = 'https://api.github.com/search/repositories?q='+keyword+':python&sort=stars&order=desc'
        if i != 0 and i % 10 == 0:
            print('	...waiting to clear GH API rate limit')
            time.sleep(60)
        print('Searching for python repos. Keyword: '+keyword+'\n')
        response = requests.get(url)
        loaded_json = json.loads(response.content)

        for k in loaded_json['items']:
            if number_repos is None or len(unique_repos) < number_repos:
                unique_repos.add(k['full_name'])

        response.close()
    f_keywords.close()

    return list(unique_repos)


# GH 500 public python repos based on number of stars
def get_top_500_repos(number_repos):
    unique_repos = set()

    for i in range(1, 6):
        if number_repos is not None and len(unique_repos) >= number_repos:
            break

        url = 'https://api.github.com/search/repositories?q=language:python&sort=stars&order=desc&per_' \
              'page=100&page='+str(i)
        response = requests.get(url)
        loaded_json = json.loads(response.content)
        for k in loaded_json['items']:
            if number_repos is None or len(unique_repos) < number_repos:
                unique_repos.add(k['full_name'])

    response.close()

    return list(unique_repos)
