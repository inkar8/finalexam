#!/bin/bash

# Clear any previous class files
rm -rf game

# Compile all Java files
javac -d . $(find src/main/java -name "*.java")

# Run the game
java game.MagicalLabyrinth