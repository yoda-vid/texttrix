#!/bin/bash
# ***** BEGIN LICENSE BLOCK *****
# Version: MPL 1.1/GPL 2.0/LGPL 2.1
#
# The contents of this file are subject to the Mozilla Public License Version
# 1.1 (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
# http://www.mozilla.org/MPL/
#
# Software distributed under the License is distributed on an "AS IS" basis,
# WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
# for the specific language governing rights and limitations under the
# License.
#
# The Original Code is Text Trix code.
#
# The Initial Developer of the Original Code is
# Text Flex.
# Portions created by the Initial Developer are Copyright (C) 2003-4, 2008, 
# 2015, 2018 the Initial Developer. All Rights Reserved.
#
# Contributor(s): David Young <dvd@textflex.com>
#
# Alternatively, the contents of this file may be used under the terms of
# either the GNU General Public License Version 2 or later (the "GPL"), or
# the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
# in which case the provisions of the GPL or the LGPL are applicable instead
# of those above. If you wish to allow use of your version of this file only
# under the terms of either the GPL or the LGPL, and not to allow others to
# use your version of this file under the terms of the MPL, indicate your
# decision by deleting the provisions above and replace them with the notice
# and other provisions required by the GPL or the LGPL. If you do not delete
# the provisions above, a recipient may use your version of this file under
# the terms of any one of the MPL, the GPL or the LGPL.
#
# ***** END LICENSE BLOCK *****

# Text Trix Plug-In Packager

HELP="
Compiles Text Trix plug-ins and the Text Trix code, which the plug-ins
extend.

Syntax:
	plug.sh [ --java java-compiler-binaries-path ] [ --help ]
(\"sh \" might need to precede the command on the same line, in case
the file pkg.sh does not have executable permissions.)

Parameters:
	--clean: Clean all .class files and exit.
	
	--java=java-compiler-binaries-path: Specify the path to javac, 
	jar, and other Java tools necessary for compilation.  
	Alternatively, the JAVA variable in pkg.sh can be hand-edited 
	to specify the path, which would override any command-line 
	specification.
	
	--help: Lend a hand by displaying yours truly.
	
	--plugins=\"list-of-plugins\": Specify the list of plugins
	to build and package.  Defaults to the full set of plugins
	included by default in the Text Trix editor.
	
Copyright:
	Copyright (c) 2003, 2018 Text Flex

Last updated:
	2018-05-12
"

####################

# compiler location
JAVA=""

# plugins to build; defaults to all folders in plugins folder 
# in parent directory of texttrix folder
PLUGINS=""

PAR_JAVA="--java"
PAR_PLUGINS="--plugins"
PAR_CLEAN="--clean"
CLEAN=0

# Sets the base directory to the script location
if [ "x$BASE_DIR" = "x" ] # empty string
then
	BASE_DIR=`dirname $0`
fi
cd "$BASE_DIR"
BASE_DIR="$PWD"

# Platform and GUI detection as well as additional variables such as 
# classes directory
source "$BASE_DIR"/build-setup.sh

##############
# Respond to user arguments

if [ $# -gt 0 ]
then
	echo "Parsing user arguments..."
	for arg in "$@"
	do
		# reads arguments
		if [ "x$arg" = "x--help" -o "x$arg" = "x-h" ] # help docs
		then
			if [ "`command -v more`" != '' ]
			then
				echo "$HELP" | more
			elif [ "`command -v less`" != "" ]
			then
				echo "$HELP" | less
			else
				echo "$HELP"
			fi
			exit 0
			
		# Java path
		elif [ ${arg:0:${#PAR_JAVA}} = "$PAR_JAVA" ]
		then
			JAVA="${arg#${PAR_JAVA}=}"
			echo "...set to use \"$JAVA\" as the Java compiler path"
			
		# plugins list
		elif [ ${arg:0:${#PAR_PLUGINS}} = "$PAR_PLUGINS" ]
		then
			PLUGINS="${arg#${PAR_PLUGINS}=}"
			echo "...set to use \"$PLUGINS\" as the list of plugins"
		
		# clean
		elif [ ${arg:0:${#PAR_CLEAN}} = "$PAR_CLEAN" ]
		then
			CLEAN=1
			echo "Set to clean class files and exit"
			
		fi
	done
	echo "...done"
fi

# Appends a file separator to end of Java compiler path if none there
if [ x$JAVA != "x" ]
then
	# appends the file separator after removing any separator already
	# present to prevent double separators
	JAVA=${JAVA%\/}/
fi

# Sets the texttrix and plugin source directories
TTX_DIR="$BASE_DIR" # texttrix folder within main dir
PLGS_DIR="${BASE_DIR}/../plugins" # plugins src folder
DIR="com/textflex/texttrix" # src package structure

#####################
# Build operations
#####################

# change to plugins directory and compile and package each plugin;
# list the directory names and their corresponding classes in the "for" line;
# the jars must have the same name and caps as their classes
cd "$PLGS_DIR"
if [ ! -d "$TTX_DIR/plugins" ]
then
	mkdir "$TTX_DIR/plugins"
fi

# get all directories in plugins folder if not set at command-line
if [[ "$PLUGINS" -eq "" ]]; then
	PLUGINS=`ls -d */`
fi

for plugin in $PLUGINS
do
	# remove trailing slash
	plugin="${plugin%%/}"
	echo "Building plugin $plugin..."
	plugin_dir=`echo "$plugin" | tr "[:upper:]" "[:lower:]"`
	
	# clean files only
	if [[ $CLEAN -eq 1 ]]; then
		rm -rf "$plugin_dir/$CLASSES_DIR"
		echo "All .class files removed from $plugin_dir"
		continue
	fi
	
	# creates the output directory
	if [[ ! -e "$plugin_dir/$CLASSES_DIR" ]]; then
		mkdir -p "$plugin_dir/$CLASSES_DIR"
	fi
	
	# extends the PlugIn or PlugInWindow classes of the Text Trix package
	CLASSPATH="$TTX_DIR/$CLASSES_DIR":"$plugin_dir/$CLASSES_DIR"
	FILES="$plugin_dir/$DIR/"*.java
	if [[ "$CYGWIN" = "true" ]]; then
		CLASSPATH="`cygpath -p -w $CLASSPATH`"
		FILES="`cygpath -p -w $FILES`"
	fi
	
	# build class files and package in JAR
	"$JAVA"javac -source $JAVA_VER_SRC -target $JAVA_VER_SRC \
		-classpath "$CLASSPATH" $FILES -d "$plugin_dir/$CLASSES_DIR"
	cd "$plugin_dir"
	"$JAVA"jar -0cf "$plugin.jar" -C "$CLASSES_DIR" . "$DIR"/*.png \
		"$DIR"/*.html && mv "$plugin.jar" "$TTX_DIR"/plugins
	cd "$PLGS_DIR"
	echo "Built and packaged $plugin"
done

echo "Plug-ins created and stored in $TTX_DIR/plugins"

exit 0
