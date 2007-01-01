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

# Text Trix packager

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
	2006-12-18
"

##############################
# User-defined variables
# Check them!
##############################

# version number
DATE=`date +'%Y-%m-%d'`
#VER="0.7.0-"$DATE
VER="0.7.0"

# the final destination of the resulting packages
PREFIX="/home/share" 

# the path to the compiler binaries
JAVA=""

# the root directory of the source files
BASE_DIR=""

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
	JAVA=/usr/java/default/bin
fi


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

# Determine the base dir if not specified above
if [ "x$BASE_DIR" = "x" ] # continue if BASE_DIR is empty string
then
	if [ `expr index "$0" "/"` -eq 1 ] # use script path if absolute
	then
		BASE_DIR="$0"
	else # assume that script path is relative to current dir
		script="${0#./}"
		BASE_DIR="$PWD/$script"
	fi
	BASE_DIR="${BASE_DIR%/texttrix/pkg.sh}" # set base dir to main Text Trix dir
	# BASE_DIR="${BASE_DIR%/.}"
	echo $BASE_DIR
fi
BLD_DIR="$BASE_DIR/build" # initial output directory
TTX_DIR="$BASE_DIR/texttrix" # root directory of main Text Trix source files
PLGS_DIR="$BASE_DIR/plugins" # root directory of Text Trix plug-in source files

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
	echo "Sorry, but $BASE_DIR isn't a directory,"
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
if [ -e "$TTX_DIR/readme.txt" ]
then
	cd "$TTX_DIR"
else
	cd "$BASE_DIR"
fi
cp -rf "$TTX_DIR"/com readme.txt readme-src.txt todo.txt changelog.txt \
	"$TTX_DIR/$DIR"/license.txt "$TTX_DIR"/logo.ico "$BLD_DIR/$PKGDIR"


# create the master package, which will eventually become the binary package
cd "$BLD_DIR/$PKGDIR"
# remove unnecessary files and directories
rm -rf com/CVS com/textflex/CVS $DIR/CVS $DIR/images/CVS $DIR/images/bak $DIR/*~
# make files readable in all sorts of systems
unix2dos *.txt $DIR/*.txt
chmod -f 664 *.txt $DIR/*.txt # prevent execution of text files

# create the source package from the master package
cd $BLD_DIR
cp -rf $PKGDIR texttrix # master --> one folder within source package
mkdir $SRCPKGDIR # create empty source package to hold copy of master
mv texttrix $SRCPKGDIR # copy to source package
cd "$TTX_DIR"
cp configure pkg.sh run.sh manifest-additions.mf \
	"$BLD_DIR/$SRCPKGDIR"/texttrix
cp plugins/*.jar "$BLD_DIR/$PKGDIR"/plugins # only want jars in binary package
cd "$BLD_DIR"
cp -rf $PLGS_DIR $SRCPKGDIR/plugins
rm $PKGDIR/readme-src.txt # remove source-specific files for binary package

# create binary package

# create binaries
cd $BLD_DIR/$PKGDIR
# self-executable jar via "java -jar [path to jar]/$JAR.jar", where $JAR is named above
if [ "$CYGWIN" = "true" ]
then
	"$JAVA"jar -cfm $JAR "`cygpath -p -w $TTX_DIR/manifest-additions.mf`" $DIR/*.class $DIR/*.txt $DIR/images/*.png "$DIR"/*.html
else
	"$JAVA"jar -cfm $JAR "$TTX_DIR/manifest-additions.mf" $DIR/*.class $DIR/*.txt $DIR/images/*.png "$DIR"/*.html
fi
rm -rf com

# convert master package to binary one
cd $BLD_DIR/$SRCPKGDIR # remove all but source and related files
rm -rf texttrix/$DIR/*.class
# remove non-source or src-related files from plugins;
# assumes that all the directories in the "plugins" are just that--plugins
rm -rf plugins/CVS plugins/*/CVS plugins/*/com/CVS plugins/*/com/textflex/CVS \
	plugins/*/$DIR/CVS plugins/*/$DIR/*.class plugins/*/$DIR/*~ \
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
