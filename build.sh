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
# Portions created by the Initial Developer are Copyright (C) 2003-4, 2008
# the Initial Developer. All Rights Reserved.
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
	
	--java=java-compiler-binaries-path: Specifies the path to javac, 
	jar, and other Java tools necessary for compilation.  
	Alternatively, the JAVA variable in pkg.sh can be hand-edited 
	to specify the path, which would override any command-line 
	specification.
	
	--help: Lends a hand by displaying yours truly.
	
	--log: Builds the SVN log from the date specified in the
	CHANGELOG_END variable through now.
	
	--plug: Compiles and packages the plug-ins after compiling the
	Text Trix program.
	
Copyright:
	Copyright (c) 2003-4, 2008 Text Flex

Last updated:
	2008-05-31
"

#####################
# User-defined variables
# Check them!
####################

# compiler location
JAVA=""

# build plugins; 1 = build
PLUG=0

# SVN texttrix src branch directory
BRANCH_DIR="trunk"

# API documentation directory relative to base directory; 1 = build
API_DIR="docs/api"
API=0

# build changelog; 1 = build
CHANGELOG_END="2007-01-02"
CHANGELOG=0

####################
# Setup variables
####################

PAR_JAVA="--java"
PAR_PLUGINS="--plugins"
PAR_BRANCH_DIR="--branch"
PAR_PLUGINS_BRANCH_DIR="--plgbranch"
PAR_PLUG="--plug"
PAR_API="--api"
PAR_CHANGELOG="--log"

echo -n "Detecting environment..."
SYSTEM=`uname -s`
CYGWIN="false"
LINUX="false"
MAC="false"
if [ `expr "$SYSTEM" : "CYGWIN"` -eq 6 ]
then
	CYGWIN="true"
elif [ `expr "$SYSTEM" : "Linux"` -eq 5 ]
then
	LINUX="true"

	# Java binary detection mechanism
	if [ "`command -v /usr/lib/jvm/java-1.6.0/bin/javac`" != "" ]
	then
		# OpenJDK directory on Fedora distributions
		JAVA="/usr/lib/jvm/java-1.6.0/bin"
	elif [ "`command -v /usr/lib/jvm/java-6-openjdk/bin/javac`" != "" ]
	then
		# OpenJDK directory on Ubuntu distributions
		JAVA="/usr/lib/jvm/java-6-openjdk/bin"
	elif [ "`command -v /usr/java/default/bin/javac`" != "" ]
	then
		JAVA="/usr/java/default/bin"
	elif [ "`command -v javac`" != '' ]
	then
		JAVA=""
	elif [ "`command -v /usr/bin/javac`" != "" ]
	then
		JAVA="/usr/bin"
	else
		JAVA="false"
		echo "Java software doesn't appear to be installed..."
		echo "Please download it (for free!) from http://java.com."
		echo "Or if it's already installed, please add it to your"
		echo "PATH or to the JAVA variable in this script."
		read -p "Press Enter to exit this script..."
		exit 1
	fi
elif [ `expr "$SYSTEM" : "Darwin"` -eq 6 ]
then
	MAC="true"
fi
echo "found $SYSTEM"

##############
# Respond to user arguments

echo "Parsing user arguments..."
READ_PARAMETER=0
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
		
	# texttrix branch dir
	elif [ ${arg:0:${#PAR_BRANCH_DIR}} = "$PAR_BRANCH_DIR" ]
	then
		BRANCH_DIR="${arg#${PAR_BRANCH_DIR}=}"
		echo "...set to use \"$BRANCH_DIR\" as the texttrix branch dir"
	
	# build plugins
	elif [ ${arg:0:${#PAR_PLUG}} = "$PAR_PLUG" ]
	then
		PLUG=1
		echo "...set to build plugins"
		
	# build API
	elif [ ${arg:0:${#PAR_API}} = "$PAR_API" ]
	then
		API=1
		echo "...set to build API documentation"
		
	# build SVN changelog
	elif [ ${arg:0:${#PAR_CHANGELOG}} = "$PAR_CHANGELOG" ]
	then
		CHANGELOG=1
		echo "...set to build changelog"
		
	fi
done
echo "...done"

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

# Sets the base directory to the script location
if [ "x$BASE_DIR" = "x" ] # empty string
then
	BASE_DIR=`dirname $0`
fi
cd "$BASE_DIR"
BASE_DIR="$PWD"

DIR="com/textflex/texttrix" # src package structure

#####################
# Build operations
#####################

#############
# Compile Text Trix classes
cd "$BASE_DIR" # change to work directory
echo "Compiling the Text Trix program..."
echo "Using the Java binary directory at [defaults to PATH]:"
echo "$JAVA"
if [ "$CYGWIN" = "true" ]
then
	"$JAVA"javac -cp `cygpath -p -w gnu/getopt:.` -target 1.5 -source 1.5 "`cygpath -p -w com/Ostermiller/Syntax/`"*.java
	"$JAVA"javac -target 1.5 -source 1.4 "`cygpath -p -w $DIR/`"*.java
else
	"$JAVA"javac -cp gnu/getopt:. -target 1.5 -source 1.5 com/Ostermiller/Syntax/*.java
	"$JAVA"javac -target 1.5 -source 1.4 $DIR/*.java
fi

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
		"$JAVA"javadoc -d "`cygpath -p -w $API_DIR`" -link "http://java.sun.com/javase/6/docs/api" -overview "overview.html" "com.textflex.texttrix"
	else
		"$JAVA"javadoc -d "$API_DIR" -link "http://java.sun.com/javase/6/docs/api" -overview "overview.html" "com.textflex.texttrix"
	fi
	echo "...done"
fi

############
# Build SVN changelog

if [ $CHANGELOG -eq 1 ]
then
	NOW=`date +'%Y-%m-%d'`
	echo ""
	echo "Building changelog from $NOW until $CHANGELOG_END..."
	svn2cl -r {$NOW}:{$CHANGELOG_END}
	echo "...written to ChangeLog"
fi

exit 0
