import glob
import errno

path = '/users/brunodasilva/Documents/code-style-mining/repo_names/*.txt'

v02_file = open('v02.txt', 'a')
v02_file.write('\n')

files = glob.glob(path)

file_count = 0
repo_count = 0

for fname in files:
    try:
        with open(fname) as f:
        	file_count+=1
        	lines = f.readlines()
        	for i in range(len(lines)):
        		repo = lines[i]
        		v02_file.write(repo)
        		repo_count+=1
    except IOError as exc:
        if exc.errno != errno.EISDIR:
            raise
print(file_count,' files.\n')
print(repo_count,' repos added for analysis.')