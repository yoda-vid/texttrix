#!/bin/sh
# Use the Jar Ajar packager to package Text Trix into a self-extracting
# package.
# Copyright (c) 2003, Text Flex

####################
# Change for your particular setup
####################
WKDIR="/home/davit/src/jarajar"
TTXDIR="/home/davit/src/texttrix"
ZIPDIR="/home/share"
PKGNAME="texttrix-0.3.2"
ZIP="$PKGNAME.zip"

###################
# Only change for tweaking
###################
cd $WKDIR
sh $TTXDIR/pkg.sh
cp $ZIPDIR/$ZIP .
sh pkg.sh $ZIP
cp $PKGNAME*.jar $ZIPDIR
ls -l $ZIPDIR/$PKGNAME*.jar
