#!/bin/sh
# Package Text Trix plugin binaries
# Copyright (c) 2003, Text Flex

#####################
# Change to fit your particular setup
#####################
WKDIR="/home/davit/src/texttrix" # work directory
DIR="com/textflex/texttrix" # src package structure
#PLUGINS=( "nonprintingchars NonPrintingChars" "extrareturnsremover ExtraReturnsRemover" "htmlreplacer HTMLReplacer" ) # the chosen plugins
PLUGINS="Search NonPrintingChars ExtraReturnsRemover HTMLReplacer" # the chosen plugins
# CYGWIN USERS: change the first argument to "javac -classpath"
# below from "..:$plugin_dir" to "`cygpath -p -w ..:$plugin_dir`" 
# by commenting or uncommenting the appropriate lines below
JAVA="/usr/java/j2sdk1.4.2/bin"

#####################
# Only change for new plugins and extra tweaking
#####################
# change to work directory and compile Text Trix classes
cd $WKDIR
$JAVA/javac $DIR/*.java

# change to plugins directory and compile and package each plugin;
# list the directory names and their corresponding classes in the "for" line;
# the jars must have the same name and caps as their classes
cd plugins
for plugin in $PLUGINS
do
	plugin_dir=`echo "$plugin" | tr "[:upper:]" "[:lower:]"`
	# extends the PlugIn class of the Text Trix package
	# CYGWIN USERS: uncomment the following line, and comment the next:
	#$JAVA/javac -classpath "`cygpath -p -w ..:$plugin_dir`" $plugin_dir/$DIR/*.java
	$JAVA/javac -classpath ..:$plugin_dir $plugin_dir/$DIR/*.java
	#cd $plugin_dir
	# gives the jar the same name and caps as the plugin's class
	cd $plugin_dir
	$JAVA/jar -0cvf $plugin.jar $DIR/*.class $DIR/*.png \
	$DIR/*.html && mv $plugin.jar ..
	cd ..
	#mv $plugin.jar ..
	#cd ..
done

exit 0
