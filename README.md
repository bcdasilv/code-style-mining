# code-style-mining
[![Build Status](https://travis-ci.org/kramsey458/code-style-mining.svg?branch=python_analysis)](https://travis-ci.org/kramsey458/code-style-mining) .  
This project aims at mining public software repositories on GitHub in order to map developers' coding style. To the best of our knowledge, ours is the first attempt to mine a huge dataset of public software projects and provide summarized quantitative data to understand how developers program in terms of coding style.

Currently this project examines repositories written in Java and Python. In the future, we hope to analyze additional languages. See our [wiki](https://github.com/bcdasilv/code-style-mining/wiki) for more information.

## Python
**Caution**: To run the analysis on a repo and output the results, our analyzer duplicates files onto your local machine. This practice may cause some vulnerabilities, so be mindful of which repositories you choose. We are not responsible for any damages or other harm that results from use of this code.

To access a GitHub repository, you will need to acquire an OAuth token. [Here's how](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/). (Though it is technically possible to query the GitHub API without this token, our program does not currently support this option due to the large volume of get requests our code makes.)

If you want to save your results to MongoDB, you will also need your username, password, cluster, and database information.

### Dependencies
The following dependencies are necessary to run the python project. 
* [dnspython](http://www.dnspython.org/)
* [requests](http://docs.python-requests.org)
* [pymongo](https://api.mongodb.com/python/current/)
* [pycodestyle](https://pypi.org/project/pycodestyle/)   
* [pytest](https://docs.pytest.org/en/latest/)
* [python-loc-counter](https://github.com/BryceV/python_loc_counter/)

These dependencies can be installed to a Python pipenv.
Install these dependencies from the Pipfile with the command   
```pipenv install```   
Start the pipenv with   
```pipenv shell```   
Start the application from CLI (credentials after flags must match) (not recommended for credentials)
```python3 main.py [-authtoken <GitHub_OAuth_token>] [-file <file_name>] [-mongodb <username> <password> <cluster> <database> <collection>]```

OR  

Run \"python main.py\" with a file at root level called pythonAnalysis.properties with these options:  
authToken = <github api token>  
repoURLsPath= <file path that has repo list>  
mongoUsername = <mongodb username>  
mongoPassword = <mongodb password>  
mongoUrl = <mongodb cluster>  
mongoDatabase = <mongodb database name>    
mongoCollection = <mongodb collection name>

It is possible to run this with some of these credentials missing. You can for instance, input the repos manually instead of using a file.
Also, it is possible to run this script in several different ways. This will check for credentials in the CLI, then the properties file, then ask the user for the missing ones. If you want to re-type a certain credential for each run, we recommend leaving it out of the properties file.


## Data Analytics
### Pulling data from MongoDB and Transforming the data from MongoDB to Google Sheets
Download [**MongoDB Compass**](https://www.mongodb.com/products/compass) and connect it to the "**python-analysis**" database. In MongoDB Compass, you'll need to export half of the data as a .CSV and the other half as a .JSON. 

For the .CSV file, export only the fields that are repository information (e.g. owner name, owner type, summary total_src_loc, etc.). This data does not to be transformed.

For the .JSON file, add a new field "**repo_analysis**" to the Export Collection page and only export this new field. Once the file is exported, run the "**python_file_transform.py**" script. The .JSON file name is hard coded in the script so make sure the correct .JSON file name is entered. 

When you have both .CSV files, add the data to the google sheets on Google Drive ("**Python Data Only**" Folder), following the [**schema**](https://dbdiagram.io/embed/61709fc66239e146477a93f8)


### Connecting to Superset 
Currently, the tables (google sheets) in the "**Python Data Only**" Folder from Google Drive are connected to [**Superset**](https://preset.io/). To connect more table(s) (google sheet(s)) to the database in Superset, add the google sheet(s) url to the database "**Python Analyzer Data**" (Data tab -> Database). Then, add the new table(s) as a dataset (Data tab --> datasets). 

*Note: You need to be invited to the Senior Project group on Superset in order to interact with the data on Superset*