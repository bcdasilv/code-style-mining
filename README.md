# code-style-mining
This project aims at mining public software repositories on GitHub in order to map developers' coding style. To be the best of our knowledge, this is the first attempt to mine a huge dataset of public software projects and provide to the community summarized quantitative data to understand how developers program in terms of coding style.

Properties file:
The properties file provides a way to supply information for running the Java code without pushing private or unnecessary data to the repository.
One needs to create a file called javaAnalysis.properties file in the same directory as the project.
The Config class communicates with this .properties file to supply information to the Java project.

The structure of the .properties file is such:

\<name\> = "\<data\>"

For the purposes of running the Java analysis code, one needs to supply the given information:

- The personal auth token generated from GitHub.

authToken = "\<auth token\>"

- The file path for the local storage of the file being parsed.

tempFilePath = "\<temp file path>\"

- The :owner/:repo that is used in a GET through the GitHub API.

repoURLsPath = "\<repo urls path\>"
