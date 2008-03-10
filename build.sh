#!/bin/sh
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
# Portions created by the Initial Developer are Copyright (C) 2003-4
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
	--java java-compiler-binaries-path: Specifies the path to javac, 
	jar, and other Java tools necessary for compilation.  
	Alternatively, the JAVA variable in pkg.sh can be hand-edited 
	to specify the path, which would override any command-line 
	specification.
	
	--plug: Compiles and packages the plug-ins after compiling the
	Text Trix program.
	
	--pkg: Creates the Text Trix binary and source packages.
	
	--help: Lends a hand by displaying yours truly.
	
Copyright:
	Copyright (c) 2004, 2008 Text Flex

Last updated:
	2008-03-08
"

#####################
# User-defined variables
# Check them!
####################

# compiler location
JAVA=""

# the chosen plugins
PLUGINS="Search ExtraReturnsRemover HTMLReplacer LetterPulse SongSheet"

# SVN texttrix src branch directory
BRANCH_DIR="trunk"

# SVN plugins src branch directory
PLUGINS_BRANCH_DIR="$BRANCH_DIR"

####################
# Setup variables
####################

PAR_JAVA="--java"
PAR_PLUGINS="--plugins"
PAR_BRANCH_DIR="--branch"
PAR_PLUGINS_BRANCH_DIR="--plgbranch"

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
	elif [ "`command -v /usr/java/default/bin/java`" != "" ]
	then
		JAVA="/usr/java/default/bin"
	elif [ "`command -v /usr/lib/jvm/java-1.7.0/bin/java`" != "" ]
	then
		# Java Iced Tea directory on Fedora distributions
		JAVA="/usr/lib/jvm/java-1.7.0/bin"
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
		
	# plugins list
	elif [ ${arg:0:${#PAR_PLUGINS}} = "$PAR_PLUGINS" ]
	then
		PLUGINS="${arg#${PAR_PLUGINS}=}"
		echo "...set to use \"$PLUGINS\" as the list of plugins"
	
	# texttrix branch dir
	elif [ ${arg:0:${#PAR_BRANCH_DIR}} = "$PAR_BRANCH_DIR" ]
	then
		BRANCH_DIR="${arg#${PAR_BRANCH_DIR}=}"
		echo "...set to use \"$BRANCH_DIR\" as the texttrix branch dir"
	
	# plugins branch dir
	elif [ ${arg:0:${#PAR_PLUGINS_BRANCH_DIR}} = "$PAR_PLUGINS_BRANCH_DIR" ]
	then
		PLUGINS_BRANCH_DIR="${arg#${PAR_PLUGINS_BRANCH_DIR}=}"
		echo "...set to use \"$PLUGINS_BRANCH_DIR\" as the plugins branch dir"
	fi
done
echo "...done"

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
	"$JAVA"javac -cp gnu/getopt:. -source 1.5 "`cygpath -p -w com/Ostermiller/Syntax`"/*.java
	"$JAVA"javac -source 1.4 "`cygpath -p -w $TTX_DIR/$DIR`"/*.java
else
	"$JAVA"javac -cp gnu/getopt:. -target 1.5 -source 1.5 com/Ostermiller/Syntax/*.java
	"$JAVA"javac -target 1.5 -source 1.4 com/textflex/texttrix/*.java
fi

exit 0
