#!/bin/sh
# Use the Jar Ajar packager to package Text Trix into a self-extracting
# package.
# Copyright (c) 2003, Text Flex

####################
# Change for your particular setup
####################
# replace w/ "." or $PWD for current directory
WKDIR="/home/davit/src/jarajar" # Jar Ajar build script directory
TTXDIR="/home/davit/src/texttrix" # Text Trix build script directory
ZIPDIR="/home/share" # Text Trix build destination
PKGNAME="texttrix-0.3.2" # Text Trix build name, where x.y.z refer to ver num
ZIP="$PKGNAME.zip" # name of zipped package
VFATDIR="/mnt/vfat/tmp" # alternate location; repeat for more sites

###################
# Only change for tweaking
###################

# run the Text Trix build script and package the resulting zip file in
# a Jar Ajar self-extractor
cd $WKDIR
sh $TTXDIR/pkg.sh
cp $ZIPDIR/$ZIP .
sh pkg.sh $ZIP
cp $PKGNAME*.jar $ZIPDIR
ls -l $ZIPDIR/$PKGNAME*.jar

# copy to alternative location
rm $VFATDIR/$PKGNAME*.jar
cp $PKGNAME*.jar $VFATDIR
