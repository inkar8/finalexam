#!/bin/bash

echo "=================================================="
echo "  Magical Labyrinth: Escape from the Dungeon"
echo "  A Java Dungeon Crawler with Design Patterns"
echo "=================================================="
echo

echo "Cleaning previous build..."
# Clear any previous class files
rm -rf game

echo "Compiling Java files..."
# Compile all Java files
javac -d . $(find src/main/java -name "*.java")

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo
    echo "Starting the game..."
    echo "The graphical interface will appear in a separate window."
    echo
    echo "Game Features:"
    echo "✓ Rich graphical interface with themed dark UI"
    echo "✓ Enhanced player graphics with animation effects"
    echo "✓ Smooth player movement with interpolated animations"
    echo "✓ Dynamic room visualization with special effects"
    echo "✓ Message styling with emoji indicators and flash effects"
    echo "✓ Visual feedback for different room types"
    echo
    echo "Controls:"
    echo "- Use arrow keys for movement"
    echo "- Type commands in the input field"
    echo "- Use navigation buttons on the right"
    echo "- Use command buttons for common actions"
    echo
    echo "Loading game..."
    echo "=================================================="
    
    # Run the game
    java game.MagicalLabyrinth
else
    echo "Compilation failed. Please check the error messages above."
    exit 1
fi