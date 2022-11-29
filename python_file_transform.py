# Transform a JSON file containing Python File Data into a csv
#
# Note: It would be good to change this script to have users enter the JSON
#       file name via command line instead of hard coding the JSON name.

import csv
import json

# Make sure to the JSON file name is correct before running
with open("filedata.json") as python_files:
    data = json.load(python_files)

result = []
for repo in data:
    repo_id = repo["node_id"]

    for _, file_data in repo["repo_analysis"].items():
        add_this = {}
        add_this["RepoID"] = repo_id
        for attr_name, attr_data in file_data.items():
            if isinstance(attr_data, dict):
                for attr_name2, attr_data2 in attr_data.items():
                    name = attr_name
                    name += f'.{attr_name2}'
                    if isinstance(attr_data2, dict):
                        for attr_name3, attr_data3 in attr_data2.items():
                            name2 = name
                            name2 += f'.{attr_name3}'
                            add_this[name2] = attr_data3
                    else:
                        add_this[name] = attr_data2
            else:
                add_this[attr_name] = attr_data
        result.append(add_this)

csv_header = set()
for entry in result:
    for key in entry.keys():
        csv_header.add(key)

with open('python_file_data.csv', 'w') as f:
    writer = csv.DictWriter(f, fieldnames=list(csv_header))
    writer.writeheader()
    for x in result:
        writer.writerow(x)
