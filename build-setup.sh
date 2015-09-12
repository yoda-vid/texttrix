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
# Portions created by the Initial Developer are Copyright (C) 2003-12
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
	
	--branch=path/to/branch: The branch (or trunk) with reference to
	the repository root from which to compile source code. For example, 
	to compile from the 0.7.1 branch, specify \"--branch=branches/0.7.1\". 
	To compile from the trunk, simply specify \"--branch=trunk\", or 
	leave it blank. The source code release package sets the branch to 
	\".\", since the source package does not contain branches and tags.
	Note that this arguments is not used directly in this script, but 
	passed to the plugins script when the \"--plug\" flag is set.
	
	--clean: Cleans all .class files and exits.
	
	--java=java//binaries/path: Specifies the path to javac, 
	jar, and other Java tools necessary for compilation.  
	Alternatively, the JAVA variable can be hand-edited 
	to specify the path, which would override any command-line 
	specification.
	
	--help: Lends a hand by displaying yours truly.
	
	--log: Builds the SVN log from the date specified in the
	CHANGELOG_END variable through now.
	
	--plug: Compiles and packages the plug-ins after compiling the
	Text Trix program.
	
Copyright:
	Copyright (c) 2003-12 Text Flex

Last updated:
	2012-11-07
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
CHANGELOG_END="" # insert date as YYYY-MM-DD format
CHANGELOG=0

JAVA_VER_SRC="1.5"

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
PAR_CLEAN="--clean"
CLEAN=0

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
