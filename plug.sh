#!/bin/sh
# Package Text Trix plugin binaries
# Copyright (c) 2003, Text Flex

#####################
# Change to fit your particular setup
#####################
BASE_DIR="$HOME/myworkspace/TextTrix" # work directory
TTX_DIR="$BASE_DIR/texttrix"
PLGS_DIR="$BASE_DIR/plugins"
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
cd $BASE_DIR
#echo "$PWD and $TTX_DIR"
$JAVA/javac $TTX_DIR/$DIR/*.java

# change to plugins directory and compile and package each plugin;
# list the directory names and their corresponding classes in the "for" line;
# the jars must have the same name and caps as their classes
cd $PLGS_DIR
for plugin in $PLUGINS
do
	plugin_dir=`echo "$plugin" | tr "[:upper:]" "[:lower:]"`
	# extends the PlugIn class of the Text Trix package
	# CYGWIN USERS: uncomment the following line, and comment the next:
	#$JAVA/javac -classpath "`cygpath -p -w $TTX_DIR:$plugin_dir`" $plugin_dir/$DIR/*.java
	$JAVA/javac -classpath $TTX_DIR:$plugin_dir $plugin_dir/$DIR/*.java
	$JAVA/jar -0cvf $plugin.jar $DIR/*.class $DIR/*.png \
	$DIR/*.html && mv $plugin.jar $TTX_DIR/plugins 
	cd ..
done

exit 0
