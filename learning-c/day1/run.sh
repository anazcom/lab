#!/bin/bash

# *********************************************************************************************************************
#       .  -std=c2x tells GCC/Clang to compile using the C2x standard 
#       |   (now officially ratified as C23), which is the latest revision
#       |   of the C language standard published in 2023
#       |
#       |      . -Wall tells it to warn us about anything that it finds unusual.
#       |      |
#       |      |    . -lm tells it to add some standard numerical functions if necessary
#       |      |    |
#       |      |    |   . specifies where the compiler output is going to be saved
#       |      |    |   |
gcc -std=c2x -Wall -lm -o  getting-started getting-started.c
# *********************************************************************************************************************
