import glob
import errno

path = '/users/brunodasilva/Documents/code-style-mining/repo_names/*.txt'

repos_file = open('v04.txt', 'w')

files = glob.glob(path)

file_count = 0
repo_count = 0

unique_repos = set()
for fname in files:
	try:
		with open(fname) as f:
			file_count+=1
			lines = f.readlines()
			for i in range(len(lines)):
				repo = lines[i]
				if repo not in unique_repos:
					repos_file.write(repo)
					repo_count+=1
					unique_repos.add(repo)
	except IOError as exc:
		if exc.errno != errno.EISDIR:
			raise
print(file_count,' files.\n')
print(repo_count,' repos added for analysis.')
repos_file.close()