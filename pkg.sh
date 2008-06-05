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
# Portions created by the Initial Developer are Copyright (C) 2003-8
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
	--branch=path/to/branch: The branch (or trunk) from which to
	compile source code.  For example, to compile from the 0.7.1
	branch, specify \"--branch=branches/0.7.1\".  To compile from the trunk, 
	simply specify \"--branch=trunk\".  When compiling from the source code 
	release package, which unlike the Subversion repository does 
	not contain branches and tags, \"--branch=.\" (single period) suffices.  
	Defaults to \"trunk\".
	
	--help: Lends a hand by displaying yours truly.
	
	--java=java-compiler-binaries-path: Specifies the path to javac, 
	jar, and other Java tools necessary for compilation.  
	Alternatively, the JAVA variable in pkg.sh can be hand-edited 
	to specify the path, which would override any command-line 
	specification.  On Linux, this path defaults to
	"/usr/java/default", the new link found in Java 6.  
	
	--prefix=install-location: the directory in which to install Text Trix.
	Defaults to "/usr/share".
		
Copyright:
	Copyright (c) 2003-8 Text Flex

Last updated:
	2008-04-11
"

#####################
# User-defined variables
# Check them!
####################

# version number
DATE=`date +'%Y-%m-%d-%Hh%M'`
#VER="0.9.0rc1-"$DATE
VER="0.9.0rc1"

# the final destination of the resulting packages
PREFIX=""

# the root directory of the source files
BASE_DIR=""

# compiler location
JAVA=""

# the chosen plugins; not currently implemented
PLUGINS="Search ExtraReturnsRemover HTMLReplacer LetterPulse SongSheet"

# SVN texttrix src branch directory
BRANCH_DIR="trunk"

# SVN plugins src branch directory
PLUGINS_BRANCH_DIR="$BRANCH_DIR"

##############################
# System setup
##############################

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

	# Java/Jar binary detection mechanism
	if [ "`command -v jar`" != '' ]
	then
		JAVA=""
	elif [ "`command -v /usr/bin/jar`" != "" ]
	then
		JAVA="/usr/bin"
	elif [ "`command -v /usr/lib/jvm/java-1.6.0/bin/jar`" != "" ]
	then
		# OpenJDK directory on Fedora/Ubuntu distributions
		JAVA="/usr/lib/jvm/java-1.6.0/bin"
	elif [ "`command -v /usr/lib/jvm/java-6-openjdk/bin/jar`" != "" ]
	then
		# OpenJDK directory on Ubuntu distributions
		JAVA="/usr/lib/jvm/java-6-openjdk/bin"
	elif [ "`command -v /usr/java/default/bin/jar`" != "" ]
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
##############

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
		
	# install location
	elif [ ${arg:0:${#PAR_PREFIX}} = "$PAR_PREFIX" ] 
	then
		PREFIX="${arg#${PAR_PREFIX}=}"
		echo "...set to use \"$PREFIX\" as the install path"
		
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

# The working directory is the directory from which this script is run
WK_DIR="$PWD"

# initial output directory
BLD_DIR="$WK_DIR/build" 

# Sets the base directory to the script location
if [ "x$BASE_DIR" = "x" ] # empty string
then
	BASE_DIR=`dirname $0`
fi
cd "$BASE_DIR"
BASE_DIR="$PWD"

# Sets the texttrix and plugin source directories
TTX_DIR="$BASE_DIR" # texttrix folder within main dir
PLGS_DIR="${BASE_DIR%$BRANCH_DIR}/../plugins" # plugins src folder
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
ALL="$PKGDIR $PKG $SRCPKGDIR $SRCPKG"

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

# remove old build packages and set up new ones
rm -rf $ALL
mkdir $PKGDIR
mkdir $PKGDIR/plugins
cp -rf "$TTX_DIR"/com "$TTX_DIR"/readme.txt "$TTX_DIR"/readme-src.txt \
	"$TTX_DIR"/changelog.txt \
	"$TTX_DIR/$DIR"/license.txt "$TTX_DIR"/logo.ico "$BLD_DIR/$PKGDIR"


# create the master package, which will eventually become the binary package
cd "$BLD_DIR/$PKGDIR"
# remove unnecessary files and directories
rm -rf com/.svn com/*/.svn com/*/*/.svn com/*/*/*/.svn
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
# remove the branch assignments because no branches in src pkg
#sed 's/BRANCH=.*/BRANCH=\./' configure > "$BLD_DIR/$SRCPKGDIR"/texttrix/configure
sed 's/BRANCH_DIR=\"trunk\"/BRANCH_DIR=/' plug.sh | \
	sed 's/PLUGINS_BRANCH_DIR=\"$BRANCH_DIR\"/PLUGINS_BRANCH_DIR=\./' > \
	"$BLD_DIR/$SRCPKGDIR"/texttrix/plug.sh
cp -rf pkg.sh run.sh manifest-additions.mf build.sh run.ps1 build.ps1 gnu \
	"$BLD_DIR/$SRCPKGDIR"/texttrix
#chmod 755	"$BLD_DIR/$SRCPKGDIR"/texttrix/configure
chmod 755	"$BLD_DIR/$SRCPKGDIR"/texttrix/pkg.sh \
	"$BLD_DIR/$SRCPKGDIR"/texttrix/run.sh \
	"$BLD_DIR/$SRCPKGDIR"/texttrix/build.sh \
	"$BLD_DIR/$SRCPKGDIR"/texttrix/plug.sh

# add the plugins, copying the entire folder
# WARNING: Remove any unwanted contents from this folder, as the whole folder
# is currently copied, with only specific files later removed.
cd "$BLD_DIR"
cp -rf $PLGS_DIR $SRCPKGDIR
# move the working branch to the each plugin's root folder, removing all other branches
for file in `ls $SRCPKGDIR/plugins`
do
	mv $SRCPKGDIR/plugins/$file/$PLUGINS_BRANCH_DIR/* $SRCPKGDIR/plugins/$file
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
	"$JAVA"jar -cfm $JAR "`cygpath -p -w $TTX_DIR/manifest-additions.mf`" $DIR/*.class $DIR/*.txt $DIR/images/*.png "$DIR"/*.html com/Ostermiller
else
	"$JAVA"jar -cfm $JAR "$TTX_DIR/manifest-additions.mf" $DIR/*.class $DIR/*.txt $DIR/images/*.png "$DIR"/*.html com/Ostermiller
fi
# make executable so can be run as binary on systems where jexec is installed
chmod 755 $JAR
rm -rf com

# finish the source package
# remove all but source and related files
cd $BLD_DIR/$SRCPKGDIR 
rm -rf texttrix/*/*/*.class texttrix/*/*/*/*.class texttrix/*/*/*/*/*.class
# remove non-source or src-related files from plugins;
# assumes that all the directories in the "plugins" are just that--plugins
rm -rf plugins/.svn plugins/*/.svn plugins/*/com/.svn plugins/*/com/textflex/.svn \
	plugins/*/$DIR/.svn plugins/*/$DIR/*.class plugins/*/$DIR/*~ \
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
# "latest" link to the current packages
rm -f latest latest-src
ln -s $PKGDIR latest
ln -s $SRCPKGDIR latest-src
ls -l $ALL
#sh $WIN_MOUNT

exit 0
