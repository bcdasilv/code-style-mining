import json
import requests
import time

#https://developer.github.com/v3/search/#rate-limit
#The Search API has a custom rate limit. For requests using Basic Authentication, 
#OAuth, or client ID and secret, you can make up to 30 requests per minute. 
#For unauthenticated requests, the rate limit allows you to make up to 10 requests per minute.

#I'll leave this script with anonymous requests for the sake of simplicity
#TODO: make authenticated requests. It'll increase the performance a little bit unless we have a list with hundreds of keywords
#For now, I've been using a list of 91 kewwords

f_keywords = open('repo_keywords.txt', 'r')
lines = f_keywords.readlines()
for i in range(len(lines)):
	if lines[i].startswith("#") or lines[i].startswith(" ")\
                or lines[i] == "" or lines[i] == "\n":
            continue
	keyword = lines[i].rstrip()
	url = 'https://api.github.com/search/repositories?q='+keyword+':python&sort=stars&order=desc'
	if i != 0 and i % 10 == 0 :
		print('	...waiting to clear GH API rate limit')
		time.sleep(60)
	print('Searching for pyhon repos. Keyword: '+keyword+'\n');
	response = requests.get(url)
	loaded_json = json.loads(response.content)
	fout = open('repo_names/'+keyword+"_python_repos.txt", 'a')
	for k in loaded_json['items']:
		fout.write(k['full_name']+'\n')
	fout.close()
	response.close()
f_keywords.close()