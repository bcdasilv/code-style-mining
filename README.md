# code-style-mining
This project aims at mining public software repositories on GitHub in order to map developers' coding style. To the best of our knowledge, ours is the first attempt to mine a huge dataset of public software projects and provide summarized quantitative data to understand how developers program in terms of coding style.

Currently this project examines repositories written in Java and Python. In the future, we hope to analyze additional languages. See our [wiki](https://github.com/bcdasilv/code-style-mining/wiki) for more information.

## Python
**Caution**: To run the analysis on a repo and output the results, our analyzer duplicates files onto your local machine. This practice may cause some vulnerabilities, so be mindful of which repositories you choose. We are not responsible for any damages or other harm that results from use of this code.

To access a GitHub repository, you will need to acquire an OAuth token. [Here's how](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/). (Though it is technically possible to query the GitHub API without this token, our program does not currently support this option due to the large volume of get requests our code makes.)

If you want to save your results to MongoDB, you will also need your username, password, cluster, and database information.
