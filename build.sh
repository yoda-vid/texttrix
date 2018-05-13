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
# Portions created by the Initial Developer are Copyright (C) 2003-12, 
# 2015-8
# the Initial Developer. All Rights Reserved.
#
# Contributor(s): David Young <david@textflex.com>
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

# Text Trix Builder

HELP="
Builds the Text Trix program and its packages.

Syntax:
	build.sh [ --java java-compiler-binaries-path ] [ --plug ]
	[ --pkg ] [ --help ]
(\"sh \" might need to precede the command on the same line, in case
the file build.sh does not have executable permissions.)

Parameters:
	--api: Builds the API documentation files.
	
	--clean: Cleans all .class files and exits.
	
	--java=java//binaries/path: Specifies the path to javac, 
	jar, and other Java tools necessary for compilation.  
	Alternatively, the JAVA variable can be hand-edited 
	to specify the path, which would override any command-line 
	specification.
	
	--jsyn: Build jsyntaxpanettx library and copy output jar to lib.
	
	--help: Lends a hand by displaying yours truly.
	
	--oster: Build osterttx library and copy output jar to lib.
	
	--plug: Compiles and packages the plug-ins after compiling the
	Text Trix program.
	
Copyright:
	Copyright (c) 2003-12, 2015-8 Text Flex

Last updated:
	2018-05-12
"

#####################
# User-defined variables
# Check them!
####################

# compiler location
JAVA=""

# build plugins; 1 = build
PLUG=0

# API documentation directory relative to base directory; 1 = build
API_DIR="docs/api"
API=0

# build changelog; 1 = build
CHANGELOG_END="" # insert date as YYYY-MM-DD format
CHANGELOG=0

# build libraries
JSYN=0
OSTER=0

####################
# Setup variables
####################

PAR_JAVA="--java"
PAR_PLUGINS="--plugins"
PAR_PLUG="--plug"
PAR_API="--api"
PAR_CLEAN="--clean"
PAR_JSYN="--jsyn"
PAR_OSTER="--oster"
CLEAN=0

# Sets the base directory to the script location
if [ "x$BASE_DIR" = "x" ] # empty string
then
	BASE_DIR=`dirname $0`
fi
cd "$BASE_DIR"
BASE_DIR="$PWD"

# Platform and GUI detection
source "$BASE_DIR"/build-setup.sh

##############
# Respond to user arguments

if [ $# -gt 0 ]
then
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
			echo "Set to use \"$JAVA\" as the Java compiler path"
			
		# build plugins
		elif [ ${arg:0:${#PAR_PLUG}} = "$PAR_PLUG" ]
		then
			PLUG=1
			echo "Set to build plugins"
			
		# build JSyntaxPaneTTX
		elif [ ${arg:0:${#PAR_JSYN}} = "$PAR_JSYN" ]
		then
			JSYN=1
			echo "Set to build JSyntaxPaneTTX"
			
		# build OsterTTx
		elif [ ${arg:0:${#PAR_OSTER}} = "$PAR_OSTER" ]
		then
			OSTER=1
			echo "Set to build OsterTTx"
			
		# clean
		elif [ ${arg:0:${#PAR_CLEAN}} = "$PAR_CLEAN" ]
		then
			CLEAN=1
			echo "Set to clean files and exit"
			
		# build API
		elif [ ${arg:0:${#PAR_API}} = "$PAR_API" ]
		then
			API=1
			echo "Set to build API documentation"
			
		fi
	done
fi

if [ x$JAVA = x"false" ]
then
	echo "Java software doesn't appear to be installed..."
	echo "Please download it (for free!) from http://java.com."
	echo "Or if it's already installed, please add it to your"
	echo "PATH or to the JAVA variable in this script."
	read -p "Press Enter to exit this script..."
	exit 1
fi

# Appends a file separator to end of Java compiler path if none there
if [ x$JAVA != "x" ]
then
	# appends the file separator after removing any separator already
	# present to prevent double separators
	JAVA=${JAVA%\/}/
fi

DIR="com/textflex/texttrix" # src package structure

#####################
# Build operations
#####################

cd "$BASE_DIR" # change to work directory

# creates the output directory
if [ ! -e "$CLASSES_DIR" ]
then
	mkdir -p "$CLASSES_DIR"
	cp -rf dictionaries $CLASSES_DIR
fi

#############
# Clean files and exit
if [ $CLEAN -eq 1 ]; then
	rm -rf $CLASSES_DIR
	echo "All .class files removed"
	exit 0
fi

# make lib folder if doesn't exist
if [ ! -e "$BASE_DIR/lib" ]; then
	mkdir "$BASE_DIR/lib"
fi

#############
# Build JSyntaxPaneTTx

jsyn_dest="$BASE_DIR/lib/jsyntaxpane.jar"
if [ $JSYN -eq 1 ] || [ ! -e "$jsyn_dest" ]
then
	echo ""
	echo "Building JSyntaxPaneTTx..."
	cd "$BASE_DIR/../jsyntaxpanettx"
	mvn package
	cp target/jsyntaxpane-0.9.6.jar "$jsyn_dest"
	cd "$BASE_DIR"
fi

#############
# Build OsterTTx

oster_dest="$BASE_DIR/lib/oster.jar"
if [ $OSTER -eq 1 ] || [ ! -e "$oster_dest" ]
then
	echo ""
	echo "Building OsterTTx..."
	cd "$BASE_DIR/../osterttx"
	./build.sh --jar
	cp oster.jar "$oster_dest"
	cd "$BASE_DIR"
fi

#############
# Compile Text Trix classes
echo ""
echo "Compiling the Text Trix program..."
echo "Using the Java binary directory at [defaults to PATH]:"
echo "$JAVA"
CLASSPATH=lib/jsyntaxpane.jar:lib/oster.jar:.
JAVA_FILES=`find . -path ./com/inet/jorthotests -prune -o -path ./com/*.java -print`
if [ "$CYGWIN" = "true" ]
then
	CLASSPATH=`cygpath -wp $CLASSPATH`
	JAVA_FILES=`cygpath -wp $JAVA_FILES`
fi
"$JAVA"javac -cp $CLASSPATH -source $JAVA_VER_SRC -target $JAVA_VER_SRC $JAVA_FILES -d $CLASSES_DIR

#############
# Build plugins

if [ $PLUG -eq 1 ]
then
	echo ""
	echo "Building plugins..."
	"$BASE_DIR/plug.sh" "$@"
fi

#############
# Build API

if [ $API -eq 1 ]
then
	if [ ! -d "$API_DIR" ]
	then
		mkdir -p "$API_DIR"
		# exit if fail to create directory
		if [ ! -d "$API_DIR" ]
		then
			echo "$API_DIR does not exist.  Please create it or set \"API_DIR\""
			echo "in $0 to a different location."
			exit 1
		fi
	fi
	echo ""
	echo "Building API documentation..."
	if [ "$CYGWIN" = "true" ]
	then
		API_DIR="`cygpath -p -w $API_DIR`"
	fi
	"$JAVA"javadoc -d "$API_DIR" -classpath $CLASSPATH -link "http://java.sun.com/javase/6/docs/api" -overview "overview.html" "com.textflex.texttrix"
	echo "...done"
fi

exit 0
