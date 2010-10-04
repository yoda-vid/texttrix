#!/bin/bash
# Text Trix start-up script

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
# Portions created by the Initial Developer are Copyright (C) 2003-9
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

HELP="
Runs Text Trix, the super text tool chest. 

Syntax:
	run.sh [options]

Assumptions:
	The compilation takes place in a Bash shell within a Un*x-based
environment, even if the resulting executables will run in the Windows
envrionemnt.

Parameters:	
	--cleartabs: Clears the saved tabs history.  Similar to \"fresh\", 
	but the tabs history is completely erased rather than preserved for 
	the next launch.
	
	--files [file1] [file2] ... : Specifies files to open at start-up.  
	Files will be placed in their own group tab, labeled "Start".  All 
	arguments listed without a switch as the first argument will be 
	opened until the first switch is reached.
	
	--fresh: Open a session withou reopening previously saved tabs, 
	while still preserving the names of the most recently stored tabs.
	
	--help: Lends a hand by displaying yours truly.
		
	--java=java-compiler-binaries-path: Specifies the path to javac, 
	jar, and other Java tools necessary for compilation.  
	Alternatively, the JAVA variable in pkg.sh can be hand-edited 
	to specify the path, which would override any command-line 
	specification.
	
	--nohigh: Turns on syntax highlighting.
	
	--verbose: Verbose command-line output.

Copyright:
	Copyright (c) 2003-10 Text Flex

Last updated:
	2010-10-04
"


##################
# Prep the Environment
##################

###############
# User argument variables

PAR_JAVA="--java"
JAVA=""
READ_JAVA=0
PAR_14="--ver14"
VER_14="false"

################
# Automatically detect the Cygwin environment

echo "Welcome to Text Trix!"
echo ""
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
	if [ "`command -v java`" != '' ]
	then
		JAVA=""
	elif [ "`command -v /usr/bin/java`" != "" ]
	then
		JAVA="/usr/bin"
	elif [ "`command -v /usr/lib/jvm/java-1.6.0/bin/java`" != "" ]
	then
		# OpenJDK directory on Fedora distributions
		JAVA="/usr/lib/jvm/java-1.6.0/bin"
	elif [ "`command -v /usr/lib/jvm/java-6-openjdk/bin/java`" != "" ]
	then
		# OpenJDK directory on Ubuntu distributions
		JAVA="/usr/lib/jvm/java-6-openjdk/bin"
	elif [ "`command -v /usr/java/default/bin/java`" != "" ]
	then
		JAVA="/usr/java/default/bin"
	else
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
		elif [ ${arg:0:${#PAR_14}} = "$PAR_14" ]
		then
			VER14="true"
			echo "...set to run in Java 1.4 compatibility mode"
		else
			echo "...passing \"$arg\" to Text Trix session"
		fi
	done
	echo "...done"
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

# Sets the base directory to the script location
if [ "x$BASE_DIR" = "x" ] # empty string
then
	BASE_DIR=`dirname $0`
fi
cd "$BASE_DIR"
BASE_DIR="$PWD"

DIR="com/textflex/texttrix" # src package structure

##############
# Run Text Trix
##############

cd "$BASE_DIR"
if [ $VER_14 = "true" ]
then
	"$Java"java -cp .:retroweaver-rt-2.0.7.jar com.textflex.texttrix.TextTrix $@
else
	"$JAVA"java -cp . com.textflex.texttrix.TextTrix $@
fi
