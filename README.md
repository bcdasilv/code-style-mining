# code-style-mining
This project aims at mining public software repositories on GitHub in order to map developers' coding style. To be the best of our knowledge, this is the first attempt to mine a huge data set of public software projects and provide to the community summarized quantitative data to understand how developers program in terms of coding style.

Properties file:
The properties file provides a way to supply information for running the Java code without pushing private or unnecessary data to the repository.
One needs to create a file called javaAnalysis.properties file in the same directory as the project (above src).
The config.Config class communicates with this .properties file to supply information to the Java project.


The structure of the .properties file is as follows (don't include the brackets):

\<name\> = \<data\>

For the purposes of running the Java analysis code, one needs to supply the given information:

- The personal auth token generated from GitHub. Found [here](https://github.com/settings/tokens)

authToken = \<auth token\>

- The file path for the local storage of the Java file being parsed.

tempJavaFilePath = \<path\>

- The file path for the local storage of the JSONification of analysis.

tempJSONFilePath = \<path\>

- The file path for the repo list. It will be file with lines in this format :owner/:repo and will be fetched with a GET through the GitHub API.

repoURLsPath = \<repo urls path\>

 - MongoDB project username. Found on MongoDB User page for the project.
 
mongoUsername = \<mongoUsername\>

 - MongoDB project password. Found on MongoDB User page for the project.

mongoPassword = \<mongoPassword\>

 - MongoDB cluster url. Get this from Overview page > connect > Connect your application
 
 mongoUrl = \<mongoUrl\>
 
  - MongoDB database name

 mongoDatabase = \<mongoDatabase\>
 
   - MongoDB collection name
   
 mongoCollection = \<mongoCollection\>



  

