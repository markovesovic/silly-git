[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# Silly Git 

Project from *Concurrent and distributed systems* course at Faculty of Computing in Belgrade.

## Description

Fully functional distributed system for versioning of ASCII coded files.
Available functionalities:

- Adding of new files
- Getting of existing file
- Keeping history of changes for files (previous versions) and dealing with merge conflicts
- Robust design - system can handle unexpected behaviour on up to 2 nodes at any given time

System design is based on [chord protocol](https://en.wikipedia.org/wiki/Chord_(peer-to-peer)).