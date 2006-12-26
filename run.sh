#!/bin/sh
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
# Portions created by the Initial Developer are Copyright (C) 2003-5
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
Runs Text Trix, the super text tool chest. 

Syntax:
      configure [--java=java-runtime-path] [--help] 
(The command may need to be invoked with \"sh configure\", if
the file \"configure\" does not have executable permissions.)

Assumptions:
	The compilation takes place in a Bash shell within a Un*x-based
environment, even if the resulting executables will run in the Windows
envrionemnt.

Parameters:	
	--help: Lends a hand by displaying yours truly.
		
	--java=java-compiler-binaries-path: Specifies the path to javac, 
	jar, and other Java tools necessary for compilation.  
	Alternatively, the JAVA variable in pkg.sh can be hand-edited 
	to specify the path, which would override any command-line 
	specification.

Copyright:
	Copyright (c) 2003-4 Text Flex

Last updated:
	2004-12-27
"


##################
# Prep the Makefile
##################

###############
# User argument variables

PAR_JAVA="--java"
JAVA=""
READ_JAVA=0

################
# Automatically detect the Cygwin environment

echo "Welcome to Text Trix!"
echo ""
echo -n "Detecting environment..."
SYSTEM=`uname -s`
CYGWIN="false"
LINUX="false"
if [ `expr "$SYSTEM" : "CYGWIN"` -eq 6 ]
then
	CYGWIN="true"
elif [ `expr "$SYSTEM" : "Linux"` -eq 5 ]
then
	LINUX="true"
	JAVA=/usr/java/default/bin
fi
echo "found $SYSTEM"

##############
# Respond to user arguments

echo "Parsing user arguments..."
READ_PARAMETER=0
for arg in "$@"
do
	n=`expr index $arg "="`
	n=`expr $n - 1`
	
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
	elif [ `expr substr $arg 1 ${#PAR_JAVA}` \
			= $PAR_JAVA \
		-a ${#PAR_JAVA} -eq $n ] # Java path
	then
		READ_JAVA=1
		READ_PARAMETER=1
	fi
	
	
	n=`expr $n + 2`
	# checks whether to read the option following an argument
	if [ $READ_PARAMETER -eq 1 ]
	then
		if [ $READ_JAVA -eq 1 ]
		then
			JAVA=`expr substr $arg $n ${#arg}`
			READ_JAVA=0
			echo "...set to use $JAVA as the Java compiler path..."
		fi
		READ_PARAMETER=0
	fi
done
echo "...done"

# Appends a file separator to end of Java compiler path if not empty
# and no separator there
if [ `expr index "$JAVA" "/"` -ne ${#JAVA} ]
then
	JAVA="$JAVA"/
fi

# Source directories
# Note that currently requires the user to remain case-sensitive with the name
# of the base dir, even if Cygwin navigates w/o regard to case
if [ "x$BASE_DIR" = "x" ] # empty string
then
	if [ `expr index "$0" "/"` -eq 1 ] # use script path if absolute
	then
		BASE_DIR="$0"
	else # assume that script path is relative to current dir
		script="${0#./}"
		BASE_DIR="$PWD/$script"
	fi
	BASE_DIR="${BASE_DIR%/run.sh}" # assumes the script's name is run.sh
	BASE_DIR="${BASE_DIR%/.}"
fi

cd "$BASE_DIR"
"$JAVA"java -cp . com/textflex/texttrix/TextTrix $@
