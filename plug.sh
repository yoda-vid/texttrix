#!/bin/sh
# Package Text Trix plugin binaries
# Copyright (c) 2003, Text Flex

#####################
# Change to fit your particular setup
#####################
WKDIR="/home/davit/src/texttrix" # work directory
DIR="com/textflex/texttrix" # src package structure

#####################
# Only change for new plugins and extra tweaking
#####################
# change to work directory and compile Text Trix classes
cd $WKDIR
javac $DIR/*.java

# change to plugins directory and compile and package each plugin;
# list the directory names and their corresponding classes in the "for" line;
# the jars must have the same name and caps as their classes
cd plugins
for dirs in "nonprintingchars NonPrintingChars" "extrareturnsremover ExtraReturnsRemover" "htmlreplacer HTMLReplacer"
do
	set -- $dirs
	# extends the PlugIn class of the Text Trix package
	javac -classpath ..:$1 $1/$DIR/*.java
	cd $1
	# gives the jar the same name and caps as the plugin's class
	jar -0cvf $2.jar $DIR/$2.class $DIR/*.png $DIR/*.html
	mv $2.jar ..
	cd ..
done

exit 0
