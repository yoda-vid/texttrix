#!/bin/sh
# Package Text Trix
# Copyright (C) 2003, Text Flex

##############################
# Variables to change for your particular setup
##############################
VER="0.3.3" # version info
WKDIR="/home/davit/src/texttrix" # work directory
DEST="/home/share" # final destination
WIN_MOUNT="pkg-winmount.sh" # script to copy files to a Windows partition

##############################
# Only change for tweaking
##############################
NAME="texttrix" # UNIX program name
DIR="com/textflex/$NAME" # Java package directory structure
PKGDIR="$NAME-$VER" # name of binary package
PKG=$PKGDIR.zip # name of compressed binary package
SRCPKGDIR="$PKGDIR-src" # name of source package
SRCPKG="$SRCPKGDIR.zip" # name of compressed package of source
JAR=$NAME.jar # executable jar
ALL="$PKGDIR $PKG $SRCPKGDIR $SRCPKG"

cd $WKDIR # base of operations
sh plug.sh # build the plugins

# remove old build packages and setup new ones
rm -rf $PKGDIR $PKG $SRCPKGDIR $SRCPKG
mkdir $PKGDIR
mkdir $PKGDIR/plugins
for docs in "about.txt" "shortcuts.txt" "features.txt" "license.txt"
do
	if [[ ! -f $docs ]]
	then
		cp $DIR/$docs .
	fi
	cp $docs $PKGDIR
done
cp -rf com changelog.txt todo.txt readme.txt $PKGDIR
cd $PKGDIR
# remove unnecessary files and directories
rm -rf com/CVS com/textflex/CVS $DIR/CVS $DIR/images/CVS $DIR/images/bak $DIR/*~
# make files readable in all sorts of systems
unix2dos *.txt
chmod 664 *.txt # prevent execution of files
# copy to master folder
cp about.txt license.txt shortcuts.txt features.txt $WKDIR/$DIR
mv about.txt shortcuts.txt features.txt $DIR # move to pkg folder
cp license.txt $DIR
cd $WKDIR
cp -rf $PKGDIR $SRCPKGDIR # copy to source package
cp plug.sh pkg.sh pkg-jaj.sh $SRCPKGDIR # copy scripts to source pkg
cp plugins/*.jar $PKGDIR/plugins # only want jars in binary package
cp -rf plugins/* $SRCPKGDIR/plugins # will later delete jars from source pkg

# create binaries
cd $WKDIR/$PKGDIR
# self-executable jar via "java -jar [path to jar]/$JAR.jar", where
# $JAR is named above
jar -cvfm $JAR $WKDIR/manifest-additions.mf $DIR/*.class $DIR/*.txt $DIR/*.html $DIR/images/*.png
rm -rf com

# remove all but source and related files
cd $WKDIR/$SRCPKGDIR
rm -rf $DIR/*.class
cd plugins 
rm -rf *.jar CVS
# remove non-source or src-related files from plugins;
# assumes that all the directories in the "plugins" are just that--plugins
for file in *
do
	if [[ -d $file ]]
	then
		cd $file
		rm -rf CVS com/CVS com/textflex/CVS $DIR/CVS $DIR/*.class $DIR/*~
		cd ..
	fi
done

# zip up and move to destination
cd $WKDIR
zip -r $PKG $PKGDIR
zip -r $SRCPKG $SRCPKGDIR
rm -rf $DEST/$PKGDIR*
mv $ALL $DEST
cd $DEST && ls -l $ALL
cd $WKDIR && sh $WIN_MOUNT

exit 0
