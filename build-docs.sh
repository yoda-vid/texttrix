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

HELP="
Builds the Text Trix API documentation, based on the J2SE, 
v.1.5.0 API.

Syntax:
	build-docs.sh [ --java java-compiler-binaries-path ] [ --help ]
(\"sh \" might need to precede the command on the same line, in case
the file pkg.sh does not have executable permissions.)

Parameters:
	--java java-compiler-binaries-path: Specifies the path to javac, 
	jar, and other Java tools necessary for compilation.  
	Alternatively, the JAVA variable in pkg.sh can be hand-edited 
	to specify the path, which would override any command-line 
	specification.
	
	--help: Lends a hand by displaying yours truly.
	
Copyright:
	Copyright (c) 2004 Text Flex

Last updated:
	2004-05-29
"

#####################
# User-defined variables
# Check them!
####################

JAVA="" # compiler location

####################
# System setup
####################

SYSTEM=`uname -s`
CYGWIN="false"
PLUG="false"
PKG="false"
if [ `expr "$SYSTEM" : "CYGWIN"` -eq 6 ]
then
	CYGWIN="true"
fi

echo "Set to build the Text Trix API documentation"
READ_PARAMETER=0
for arg in "$@"
do
	# checks whether to read the option following an argument
	if [ $READ_PARAMETER -eq 1 ]
	then
		if [ "x$JAVA" = "x" ]
		then
			JAVA=$arg
		fi
		READ_PARAMETER=0
	fi
	
	# reads arguments
	
	if [ "x$arg" = "x--help" -o "x$arg" = "x-h" ] # help docs
	then
		echo "$HELP"
		exit 0
	elif [ "x$arg" = "x--java" ] # Java path
	then
		READ_PARAMETER=1
	fi
done

if [ `expr index "$JAVA" "/"` -ne ${#JAVA} ]
then
	JAVA="$JAVA"/
fi
echo "Using the Java binary directory at [defaults to PATH]:"
echo "$JAVA"

# Source directories
BASE_DIR=""
if [ "x$BASE_DIR" = "x" ] # empty string
then
	if [ `expr index "$0" "/"` -eq 1 ]
	then
		BASE_DIR="$0"
	else
		BASE_DIR="$PWD/$0"
	fi
	BASE_DIR="${BASE_DIR%/texttrix/build-docs.sh}" # assumes name is build-docs.sh
fi
TTX_DIR="$BASE_DIR/texttrix"
API_DIR="$BASE_DIR/docs/api"




###################
# Build operations
###################

if [ ! -d "$API_DIR" ]
then
	echo "$API_DIR does not exist.  Please create it or set \"API_DIR\""
	echo "in $0 to a different location."
	exit 1
fi
cd "$TTX_DIR"
if [ "$CYGWIN" = "true" ]
then	
	"$JAVA"javadoc -d "`cygpath -p -w $API_DIR`" -link "http://java.sun.com/j2se/1.5.0/docs/api" -overview "overview.html" "com.textflex.texttrix"
else
	"$JAVA"javadoc -d "$API_DIR" -link "http://java.sun.com/j2se/1.5.0/docs/api" -overview "overview.html" "com.textflex.texttrix"
fi

exit 0
