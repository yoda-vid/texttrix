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
# Portions created by the Initial Developer are Copyright (C) 2003
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

# Text Trix packager

##############################
# Variables to change for your particular setup
##############################
VER="0.3.4" # version info
BASE_DIR="/home/davit/myworkspace/TextTrix" # work directory
BLD_DIR="$BASE_DIR/build"
TTX_DIR="$BASE_DIR/texttrix"
PLGS_DIR="$BASE_DIR/plugins"
DEST="/home/share" # final destination
WIN_MOUNT="$BASE_DIR/pkg-winmount.sh" # script to copy files to a Windows partition
JAVA="/usr/java/j2sdk1.4.2/bin"

##############################
# Only change for tweaking
##############################
NAME="texttrix" # UNIX program name
DIR="com/textflex/$NAME" # Java package directory structure
PKGDIR="$NAME-$VER" # name of binary package
PKG=$PKGDIR.zip # name of compressed binary package
SRCPKGDIR="$PKGDIR-src" # name of source package
SRCPKG="$SRCPKGDIR.zip" # name of compressed package of source
JAR="TextTrix.jar" # executable jar
ALL="$PKGDIR $PKG $SRCPKGDIR $SRCPKG"

if [ ! -d $BLD_DIR ]
then
	mkdir $BLD_DIR
fi
cd $BLD_DIR # base of operations
sh $TTX_DIR/plug.sh # build the plugins

# remove old build packages and setup new ones
rm -rf $PKGDIR $PKG $SRCPKGDIR $SRCPKG
mkdir $PKGDIR
mkdir $PKGDIR/plugins
cp -rf $TTX_DIR/com $TTX_DIR/readme.txt $TTX_DIR/readme-src.txt \
	$TTX_DIR/todo.txt $TTX_DIR/changelog.txt \
	$TTX_DIR/$DIR/license.txt $PKGDIR

cd $PKGDIR
# remove unnecessary files and directories
rm -rf com/CVS com/textflex/CVS $DIR/CVS $DIR/images/CVS $DIR/images/bak $DIR/*~
# make files readable in all sorts of systems
unix2dos *.txt $DIR/*.txt
chmod 664 *.txt $DIR/*.txt # prevent execution of text files

cd $BLD_DIR
cp -rf $PKGDIR texttrix
rm $PKGDIR/readme-src.txt
mkdir $SRCPKGDIR # copy to source package
mv texttrix $SRCPKGDIR
cp $TTX_DIR/plug.sh $TTX_DIR/pkg.sh $TTX_DIR/pkg-jaj.sh \
	$TTX_DIR/manifest-additions.mf \
	$SRCPKGDIR/texttrix # copy scripts to source pkg
cp $TTX_DIR/plugins/*.jar $PKGDIR/plugins # only want jars in binary package
cp -rf $PLGS_DIR $SRCPKGDIR/plugins

# create binaries
cd $BLD_DIR/$PKGDIR
# self-executable jar via "java -jar [path to jar]/$JAR.jar", where $JAR is named above
$JAVA/jar -cvfm $JAR $TTX_DIR/manifest-additions.mf $DIR/*.class $DIR/*.txt $DIR/images/*.png
rm -rf com

# remove all but source and related files
cd $BLD_DIR/$SRCPKGDIR
rm -rf texttrix/$DIR/*.class
# remove non-source or src-related files from plugins;
# assumes that all the directories in the "plugins" are just that--plugins
rm -rf plugins/CVS plugins/*/CVS plugins/*/com/CVS plugins/*/com/textflex/CVS \
	plugins/*/$DIR/CVS plugins/*/$DIR/*.class plugins/*/$DIR/*~ \
	plugins/*.jar
mv texttrix/*.txt .

# zip up and move to destination
cd $BLD_DIR
cp $PKGDIR/$JAR $TTX_DIR
zip -r $PKG $PKGDIR
zip -r $SRCPKG $SRCPKGDIR
rm -rf $DEST/$PKGDIR*
mv $ALL $DEST
cd $DEST && ls -l $ALL
sh $WIN_MOUNT

exit 0
