## key-value data base

### How to use

Commands for data base:
```
1. content - displays the content of the database in key -> value format
2. insert key -> value - inserts a (key, value) pair into the database
3. update key -> value - changes value of the key field to value
4. find key - finds value for the field key
5. findRegex pattern - finds all the keys (and their values) that match the regex pattern.
6. erase key - deletes the key field
7. eraseRegex pattern - removes all fields which key corresponds to the regex pattern
8. clear - clears the database
9. createGroup name - creates group with name
10. eraseGroup name - deletes the group named name
11. insertInGroup key -> name - adds the key to the name group (the key must exist in the database)
12. eraseFromGroup name -> key - deletes the key from the group name
13. findInGroup name -> key - searches for the values for the key in the group name
14. contentOfGroup name - displays the content of the name group
15. contentOfAllGroups - displays the content of all groups
17. rollback - cancel the last command
18. exit - exit
19. save - saves data to a file
```

The database is encrypted after each save. To encrypt the database, the user is required to provide a key. The key may be different at
at each save, the main thing is that at login the key coincides with the one specified at the time of saving.

There is an opportunity to run a program to work with the commands written in the file. To do this you must specify
option -f (or --file) in the command-line arguments, then the name of the file with the commands, and then (optionally) the name of the file
where to redirect the output of the program.

### The way the program works

#### General principles

The database is stored in the database file. At every start-up the database is decrypted and loaded into RAM.
All user interaction is done in the workingProcess function. At each iteration of the loop, the following happens:
The user command is read and processed. Then the arguments are checked for correctness (the keys to be added are not
added keys are present, deleted keys are present, etc.). At the next step, the information sufficient for rolling back that
operation. Then a function is called that handles the given command.

#### Encryption

The program implements the usual encryption algorithm
["key transposition"](https://neudoff.net/info/informatika/metod-shifruyushhix-tablic-s-odinochnoj-perestanovkoj-po-klyuchu/)
The only addition is to append a certain string to the end of the original text and check after 
decryption that it remains (the improvement is that if the wrong key is entered of the same length, the
the last line will be different, and the program will report the wrong key, instead of loading the incorrect data).

### Testing

In addition to unit tests to individual functions, there are several sets of input data in the tests folder. For
tests you should specify in the command line parameters "-f tests/testN/input.txt tests/testN/output.txt", where N is
test number.
