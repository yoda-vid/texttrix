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
# Portions created by the Initial Developer are Copyright (C) 2003-7
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

##############################
# User-defined variables
# Check them!
##############################

# version number
DATE=`date +'%Y-%m-%d'`
VER="0.7.1beta1-"$DATE
#VER="0.7.1alpha1"

# the final destination of the resulting packages
PREFIX="/home/share" 

# the path to the compiler binaries
JAVA=""

# the root directory of the source files
BASE_DIR=""

# the current working branch for packaging
BRANCH="trunk"

################################
# Help
################################

HELP="
Packages both binary and source code archives of Text Trix.

Syntax:
	pkg.sh [ --java java-compiler-binaries-path ] [ --help ]
(\"sh \" might need to precede the command on the same line, in case
the file pkg.sh does not have executable permissions.)

Parameters:
	--java=java-compiler-binaries-path: Specifies the path to javac, 
	jar, and other Java tools necessary for compilation.  
	Alternatively, the JAVA variable in pkg.sh can be hand-edited 
	to specify the path, which would override any command-line 
	specification.  On Linux, this path defaults to
	"/usr/java/default", the new link found in Java 6.  
	
	--prefix=install-location: the directory in which to install Text Trix.
	Defaults to "/usr/share".
		
	--help: Lends a hand by displaying yours truly.
	
Copyright:
	Copyright (c) 2003-7 Text Flex

Last updated:
	2007-10-07
"

##############################
# System setup
##############################

SYSTEM=`uname -s`
CYGWIN="false"
LINUX="false"
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
	fi
fi
echo "found $SYSTEM"


echo "Parsing user arguments..."
READ_PARAMETER=0
for arg in "$@"
do
	n=`expr index "$arg" "="`
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
	elif [ `expr substr "$arg" 1 ${#PAR_JAVA}` \
			= $PAR_JAVA \
		-a ${#PAR_JAVA} -eq $n ] # Java path
	then
		READ_JAVA=1
		READ_PARAMETER=1
	elif [ `expr substr "$arg" 1 ${#PAR_PREFIX}` \
			= $PAR_PREFIX \
		-a ${#PAR_PREFIX} -eq $n ] # specify the install location
	then
		READ_PREFIX=1
		READ_PARAMETER=1
	fi
	
	
	n=`expr $n + 2`
	# checks whether to read the option following an argument
	if [ $READ_PARAMETER -eq 1 ]
	then
		if [ $READ_JAVA -eq 1 ]
		then
			JAVA=`expr substr "$arg" $n ${#arg}`
			READ_JAVA=0
			echo "...set to use $JAVA as the Java compiler path..."
		elif [ $READ_PREFIX -eq 1 ]
		then
			PREFIX=`expr substr "$arg" $n ${#arg}`
			echo "...set to use the $PREFIX prefix..."
			READ_PREFIX=0
		fi
		READ_PARAMETER=0
	fi
done
echo "...done"

if [ `expr index "$JAVA" "/"` -ne ${#JAVA} ]
then
	JAVA="$JAVA"/
fi

# The working directory is the directory from which this script is run
WK_DIR="$PWD"

# Source directories
# The base directory is the directory containing this script
if [ "x$BASE_DIR" = "x" ] # continue if BASE_DIR is empty string
then
	BASE_DIR=`dirname $0`
fi

# sets the base direction to the script location
cd "$BASE_DIR"
BASE_DIR="$PWD"

# sets the plugins directory based on the location found from the script
PLGS_DIR=""
if [ $BRANCH = "." ]
then
	PLGS_DIR="${BASE_DIR%texttrix}"plugins
else
	PLGS_DIR="${BASE_DIR%texttrix/$BRANCH}"plugins
fi

# initial output directory
BLD_DIR="$WK_DIR/build" 

# root directory of main Text Trix source files
# Same directory as base directory for now.
TTX_DIR="$BASE_DIR" 

##############################
# Build operations
##############################
NAME="texttrix" # UNIX program name
DIR="com/textflex/$NAME" # Java package directory structure
PKGDIR="$NAME-$VER" # name of binary package
PKG=$PKGDIR.zip # name of compressed binary package
SRCPKGDIR="$PKGDIR-src" # name of source package
SRCPKG="$SRCPKGDIR.zip" # name of compressed package of source
JAR="TextTrix.jar" # executable jar
ALL="$PKGDIR $PKG $SRCPKGDIR $SRCPKG"

if [ ! -d "$BLD_DIR" ]
then
	mkdir "$BLD_DIR"
fi

if [ -d "$BLD_DIR" ]
then
	cd "$BLD_DIR" # base of operations
else
	echo "Sorry, but $BLD_DIR isn't a directory,"
	echo "so I won't be very useful."
	echo "Goodbye."
	exit 1
fi

##########
# Packaging

echo "Packaging the files..."

# remove old build packages and set up new ones
rm -rf $ALL
mkdir $PKGDIR
mkdir $PKGDIR/plugins
cp -rf "$TTX_DIR"/com "$TTX_DIR"/readme.txt "$TTX_DIR"/readme-src.txt \
	"$TTX_DIR"/todo.txt "$TTX_DIR"/changelog.txt \
	"$TTX_DIR/$DIR"/license.txt "$TTX_DIR"/logo.ico "$BLD_DIR/$PKGDIR"


# create the master package, which will eventually become the binary package
cd "$BLD_DIR/$PKGDIR"
# remove unnecessary files and directories
rm -rf com/.svn com/textflex/.svn $DIR/.svn $DIR/images/.svn
# make files readable in all sorts of systems
unix2dos *.txt $DIR/*.txt
chmod -f 664 *.txt $DIR/*.txt # prevent execution of text files

# create the source package from the master package
cd $BLD_DIR
cp -rf $PKGDIR texttrix # master --> one folder within source package
mkdir $SRCPKGDIR # create empty source package to hold copy of master
mv texttrix $SRCPKGDIR # copy to source package

# add the build files
cd "$TTX_DIR"
sed 's/BRANCH=.*/BRANCH=\./' configure > "$BLD_DIR/$SRCPKGDIR"/texttrix/configure
cp pkg.sh run.sh manifest-additions.mf \
	"$BLD_DIR/$SRCPKGDIR"/texttrix
chmod 755 "$BLD_DIR/$SRCPKGDIR"/texttrix/configure \
	"$BLD_DIR/$SRCPKGDIR"/texttrix/pkg.sh \
	"$BLD_DIR/$SRCPKGDIR"/texttrix/run.sh

# add the plugins, copying the entire folder
# WARNING: Remove any unwanted contents from this folder, as the whole folder
# is currently copied, with only specific files later removed.
cd "$BLD_DIR"
cp -rf $PLGS_DIR $SRCPKGDIR
# move the working branch to the each plugin's root folder, removing all other branches
for file in `ls $SRCPKGDIR/plugins`
do
	mv $SRCPKGDIR/plugins/$file/$BRANCH/* $SRCPKGDIR/plugins/$file
	rm -rf $SRCPKGDIR/plugins/$file/tags $SRCPKGDIR/plugins/$file/branches $SRCPKGDIR/plugins/$file/trunk
done
rm $PKGDIR/readme-src.txt # remove source-specific files for binary package

# create binary package

# create/package binaries
cd $BLD_DIR/$PKGDIR
cp "$TTX_DIR"/plugins/*.jar plugins # only want jars in binary package
# self-executable jar via "java -jar [path to jar]/$JAR.jar", where $JAR is named above
if [ "$CYGWIN" = "true" ]
then
	"$JAVA"jar -cfm $JAR "`cygpath -p -w $TTX_DIR/manifest-additions.mf`" $DIR/*.class $DIR/*.txt $DIR/images/*.png "$DIR"/*.html
else
	"$JAVA"jar -cfm $JAR "$TTX_DIR/manifest-additions.mf" $DIR/*.class $DIR/*.txt $DIR/images/*.png "$DIR"/*.html
fi
rm -rf com

# finish the source package
# remove all but source and related files
cd $BLD_DIR/$SRCPKGDIR 
rm -rf texttrix/$DIR/*.class
# remove non-source or src-related files from plugins;
# assumes that all the directories in the "plugins" are just that--plugins
rm -rf plugins/.svn plugins/*/.svn plugins/*/com/.svn plugins/*/com/textflex/.svn \
	plugins/*/$DIR/.svn plugins/*/$.svn/*.class plugins/*/$DIR/*~ \
	plugins/*.jar
mv texttrix/*.txt .

# Add PKGDIR-specific files
cp $TTX_DIR/$DIR/images/minicon-32x32.png $BLD_DIR/$PKGDIR/icon.png

# zip up and move to PREFIXination
cd $BLD_DIR
cp $PKGDIR/$JAR $TTX_DIR
zip -rq $PKG $PKGDIR
zip -rq $SRCPKG $SRCPKGDIR
echo ""
if [ -d "$PREFIX" ]
then
	cd "$PREFIX"
	rm -rf $ALL # removes any lingering Text Trix build pkgs in PREFIX dir
	cd "$BLD_DIR"
	mv $ALL "$PREFIX" # moves pkgs to PREFIX
	cd "$PREFIX"
	echo "Packages output to $PREFIX"
else
	echo "Packages output to $BLD_DIR"
fi
ls -l $ALL
#sh $WIN_MOUNT

exit 0
