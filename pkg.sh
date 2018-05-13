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
# Portions created by the Initial Developer are Copyright (C) 2003, 2017, 
# 2018 the Initial Developer. All Rights Reserved.
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

################################
# Help
################################

HELP="
Packages both binary and source code archives of Text Trix. Assumes
that the binaries have already been built, typically using build.sh.

Syntax:
	pkg.sh [options]

Parameters:
	--help: Lends a hand by displaying yours truly.
	
	--java=javac/binary/path: Specifies the path to javac, 
	jar, and other Java tools necessary for compilation.  
	Alternatively, the JAVA variable in pkg.sh can be hand-edited 
	to specify the path, which would override any command-line 
	specification.  On Linux, this path defaults to
	"/usr/java/default/bin", the new link found in Java 6.  
	
	--prefix=install/location: the directory in which to install Text Trix.
	Defaults to "/usr/share".
	
	--timestamp: adds a mm-dd-yy-hh\'h\'mm timestamp to each package
		
	--ver=version: The version number to append to the package names.
	
Copyright:
	Copyright (c) 2003, 2018 Text Flex

Last updated:
	2018-05-12
"

#####################
# User-defined variables
# Check them!
####################

# version number
DATE=`date +'%Y-%m-%d-%Hh%M'`
TIMESTAMP=0
VER="1.1.0"

# the final destination of the resulting packages
PREFIX=""

# the root directory of the source files
BASE_DIR=""

# compiler location
JAVA=""

LAUNCH4J="launch4j-3.9"
LAUNCH4J_CONFIG="launch4j-config.xml"

##############################
# System setup
##############################

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
##############

PAR_JAVA="--java"
PAR_PLUGINS="--plugins"
PAR_PLUG="--plug"
PAR_API="--api"
PAR_CHANGELOG="--log"
PAR_PREFIX="--prefix"
PAR_TIMESTAMP="--timestamp"
PAR_VER="--ver"

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
			
		# install location
		elif [ ${arg:0:${#PAR_PREFIX}} = "$PAR_PREFIX" ] 
		then
			PREFIX="${arg#${PAR_PREFIX}=}"
			echo "Set to use \"$PREFIX\" as the install path"
			
		# verion number, which overrides default version
		elif [ ${arg:0:${#PAR_VER}} = "$PAR_VER" ]
		then
			VER="${arg#${PAR_VER}=}"
			echo "...set to use \"$VER\" as the version number..."
		
		# timestamp labeling
		elif [ ${arg:0:${#PAR_TIMESTAMP}} = "$PAR_TIMESTAMP" ]
		then
			TIMESTAMP=1
			VER="$VER-$DATE"
			echo "Set to label packages with \"$VER\""
		fi
	done
fi

# Appends a file separator to end of Java compiler path if none there
if [ x$JAVA != "x" ]
then
	# appends the file separator after removing any separator already
	# present to prevent double separators
	JAVA=${JAVA%\/}/
fi

# The working directory is the directory from which this script is run
WK_DIR="$PWD"

# initial output directory
BLD_DIR="$WK_DIR/build" 

# Sets the texttrix and plugin source directories
TTX_DIR="$BASE_DIR" # texttrix folder within main dir
PLGS_DIR="${BASE_DIR}/../plugins" # plugins src folder
DIR="com/textflex/texttrix" # src package structure

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
ALL="$PKGDIR $PKG $PKG14DIR $PKG14 $SRCPKGDIR $SRCPKG"

# create build directory if doesn't already exist
if [ ! -d "$BLD_DIR" ]
then
	mkdir "$BLD_DIR"
fi

# attempt to clean up build directory
if [ -d "$BLD_DIR" ]
then
	cd "$BLD_DIR" # base of operations
	rm -rf $ALL
else
	echo "Sorry, but $BLD_DIR isn't a directory,"
	echo "so I won't be very useful."
	echo "Goodbye."
	exit 1
fi

##########
# Packaging

echo "Packaging the files..."

# set up new package directories
mkdir $PKGDIR
mkdir $PKGDIR/plugins
mkdir -p $PKGDIR/$DIR/images
cp -rf "$TTX_DIR"/$CLASSES_DIR/com "$TTX_DIR"/readme.txt \
	"$TTX_DIR"/README.md \
	"$TTX_DIR"/changelog.txt "$TTX_DIR"/lib \
	"$TTX_DIR/$DIR"/license.txt "$TTX_DIR"/logo.ico \
	"$TTX_DIR"/dictionaries "$TTX_DIR"/run.bat \
	"$TTX_DIR"/$LAUNCH4J_CONFIG $PKGDIR
cp "$TTX_DIR"/$DIR/images/*.png $PKGDIR/$DIR/images
cp "$TTX_DIR"/$DIR/*.txt "$TTX_DIR"/$DIR/*.html $PKGDIR/$DIR
JORTHO_DIR=com/inet/jortho
cp -rf "$TTX_DIR"/$JORTHO_DIR/i18n $PKGDIR/$JORTHO_DIR

# create the master package, which will eventually become the binary package
cd "$BLD_DIR/$PKGDIR"
# remove unnecessary files and directories
rm -rf com/.svn com/*/.svn com/*/*/.svn com/*/*/*/.svn dictionaries/.svn dictionaries/User*

# create the source package from the master package
cd $BLD_DIR
mkdir $SRCPKGDIR # create empty source package to hold copy of master
cp -rf $PKGDIR $SRCPKGDIR/texttrix # master --> one folder within source package

# add the build files
cd "$TTX_DIR"
cp -rf pkg.sh run.sh manifest-additions.mf build*.sh README.md \
		"$BLD_DIR/$SRCPKGDIR"/texttrix
sed 's/build:/build: '$DATE'/' $DIR/about.txt > \
		"$BLD_DIR/$SRCPKGDIR"/texttrix/$DIR/about.txt

# add the plugins, copying the entire folder
# WARNING: Remove any unwanted contents from this folder, as the whole folder
# is currently copied, with only specific files later removed.
cd "$BLD_DIR"
cp -rf $PLGS_DIR $SRCPKGDIR
rm $PKGDIR/README.md # remove source-specific files for binary package
cp "$TTX_DIR"/plugins/*.jar $BLD_DIR/$PKGDIR/plugins # only want jars in binary package

# create binary package

# create/package binaries
cd $BLD_DIR/$PKGDIR
#cp "$TTX_DIR"/plugins/*.jar plugins # only want jars in binary package
rm -rf com

# create JAR from source package and remove binary files from source
cd $BLD_DIR/$SRCPKGDIR/texttrix
# self-executable jar via "java -jar [path to jar]/$JAR.jar", where $JAR is named above
if [ "$CYGWIN" = "true" ]
then
	"$JAVA"jar -cfm "`cygpath -p -w $BLD_DIR/$PKGDIR/$JAR`" "`cygpath -p -w manifest-additions.mf`" $DIR/*.class $DIR/*.txt $DIR/images/*.png $DIR/*.html com/inet
else
	"$JAVA"jar -cfm $BLD_DIR/$PKGDIR/$JAR manifest-additions.mf $DIR/*.txt $DIR/*.class $DIR/images/*.png $DIR/*.html com/inet
fi
# make executable so can be run as binary on systems where jexec is installed
chmod 755 $BLD_DIR/$PKGDIR/$JAR
rm -rf */*/*.class */*/*/*.class */*/*/*/*.class

# Builds launch4j executable
echo -n "Creating Windows executable file..."
cd $BLD_DIR/$PKGDIR
L4J_EXE="$WK_DIR/$LAUNCH4J/launch4j"
if [ -e $L4J_EXE ]
then
	L4J_PATH="$BLD_DIR/$PKGDIR/$LAUNCH4J_CONFIG"
	if [ "$CYGWIN" = "true" ]
	then
		L4J_PATH=`cygpath -wp $L4J_PATH`
	fi
	$L4J_EXE $L4J_PATH
	# waits until exe file is built since launch4j appears to run
	# in a separate process
	while [ ! -f "$BLD_DIR/$PKGDIR/TextTrix.exe" ]
	do
		sleep 2
	done
else
	echo -n "could not find launch4j...skipping..."
fi
rm $L4J_PATH
echo "done"

# finish the source package
cd $BLD_DIR/$SRCPKGDIR
# replace classes-oriented com with full com directory
rm -rf texttrix/com
cp -rf "$TTX_DIR"/com texttrix
# remove non-source or src-related files from plugins;
# assumes that all the directories in the "plugins" are just that--plugins
rm -rf plugins/.svn plugins/*/.svn plugins/*/com/.svn plugins/*/com/textflex/.svn \
	plugins/*/$DIR/.svn plugins/*/$DIR/*.class plugins/*/$DIR/*~ \
	plugins/*.jar
mv texttrix/*.txt texttrix/*.md .

# Add PKGDIR-specific files
cp $TTX_DIR/$DIR/images/minicon-32x32.png $BLD_DIR/$PKGDIR/icon.png

# zip up and move to PREFIXination
cd $BLD_DIR
cp $PKGDIR/$JAR $TTX_DIR

echo -n "Creating $PKG package..."
zip -rq $PKG $PKGDIR
echo "done"

echo -n "Creating $SRCPKG package..."
zip -rq $SRCPKG $SRCPKGDIR
echo "done"
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
# "latest" link to the current packages
rm -f latest latest-src
ln -s $PKGDIR latest
ln -s $SRCPKGDIR latest-src

exit 0
