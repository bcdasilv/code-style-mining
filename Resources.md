# Resources
A collection of resources consulted and referenced throughout the development of the coding style mining project. Last updated 2018/08/06 by [Kellie Banzon](https://github.com/kelliebanzon).

==TODO==:
- Add resources from emails
- General cleanup & organization

## General Information & Statistics
1. [GitHub Octoverse 2017](https://octoverse.github.com/): General usage information about GitHub during 2017
2. [GitHut](http://githut.info/): Interactive graphs about languages used on GitHub
3. [TIOBE Index](https://www.tiobe.com/tiobe-index/): In-depth analysis and history of top languages used worldwide

## Data Analysis & Visualization
1. ==TODO==: http://sideeffect.kr/popularconvention. 

## Style Guides
- Java
  1. [Java Style Guide](https://www.oracle.com/technetwork/java/codeconvtoc-136057.html): Archived official Java style guide from Oracle. Last updated 1999/04/20 and no longer actively maintained.
- Python
  1. [Style Guide for Python Code](https://www.python.org/dev/peps/pep-0008/): Official PEP 8 style guide from the Python Software Foundation

## GitHub Projects
Active projects used for testing purposes.
- Java
  1. [Spring Boot](https://github.com/spring-projects/spring-boot) 
- Python
  1. [Django](https://github.com/django/django)
  2. [Harry Potter Universe](https://github.com/zotroneneis/harry_potter_universe)

## Parsing
Our code style mining project is written entirely in Java, but mines projects in both Python and Java. This section is sorted by target languages.
- [Parsing Any Language in Java in 5 Minutes](https://tomassetti.me/parsing-any-language-in-java-in-5-minutes-using-antlr-for-example-python/): Federico Tomassetti's walkthrough, using Python 3 as an example
- Java
  1. [Parsing in Java](https://tomassetti.me/parsing-in-java/): Gabriele Tomassetti's collection of tools and libraries
  2. [JavaParser](http://javaparser.org/)
     * Note from [Bruno da Silva](https://github.com/bcdasilv):
       > Not useful for tracking whitespaces and brace positions. But parse libraries might be useful for capturing other style aspects such as naming conventions.
  3. [Roaster](https://github.com/forge/roaster): A Java Parser library that allows easy parsing and formatting of Java source files
     * Note from [Bruno da Silva](https://github.com/bcdasilv):
       > Very similar to JavaParser but with a slightly different API.
- Python
  1. [Parsing in Python](https://tomassetti.me/parsing-in-python/): Gabriele Tomassetti's collection of tools and libraries

## Git APIs & Libraries
1. [GitHub Java API (org.eclipse.egit.github.core)](https://github.com/eclipse/egit-github/tree/master/org.eclipse.egit.github.core): Listed as a third-party library on the [GitHub Developer API v3 site](https://developer.github.com/v3/libraries/).
2. [GitHub API for Java (org.kohsuke.github)](http://github-api.kohsuke.org/): Listed as a third-party library on the [GitHub Developer API v3 site](https://developer.github.com/v3/libraries/).
3. [JGit](http://www.eclipse.org/jgit/): The Eclipse Java library for Git version control
4. [Pydriller](https://github.com/ishepard/pydriller): Python framework to analyze Git repositories

## Tutorials
1. [Open Science MOOC](https://github.com/OpenScienceMOOC/Module-5-Open-Research-Software-and-Open-Source/tree/master/content_development): Introduction to GitHub repositories
