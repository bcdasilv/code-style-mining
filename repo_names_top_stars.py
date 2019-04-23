import json
import requests
import time

#https://developer.github.com/v3/search/#rate-limit
#The Search API has a custom rate limit. For requests using Basic Authentication, 
#OAuth, or client ID and secret, you can make up to 30 requests per minute. 
#For unauthenticated requests, the rate limit allows you to make up to 10 requests per minute.

#GH 500 public python repos based on number of stars
fout = open("v04_top500.txt", 'w')
for i in range(1,6):
	url = 'https://api.github.com/search/repositories?q=language:python&sort=stars&order=desc&per_page=100&page='+str(i)
	response = requests.get(url)
	loaded_json = json.loads(response.content)
	for k in loaded_json['items']:
		fout.write(k['full_name']+'\n')
fout.close()
response.close()