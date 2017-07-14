# Open File Count Util
Purpose:  Give an idea of how the open file descriptor counts for Java are behaving.
## Inputs
* testdir:  base directory for test file creation. Needs to exist.
* maxfiles:  number of files to create (this is not the same as file descriptors - there's a correlation, but I couldn't find a direct correlation)

Sample execution:
java -jar openFileCountTester.jar testdir maxfiles

