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

# Text Trix release packager
# Uses the Jar Ajar packager to package Text Trix into a self-extracting
# package.
# Copyright (c) 2003, Text Flex

####################
# Change for your particular setup
####################
WKDIR="$HOME/src/jarajar" # Jar Ajar build script directory
TTXDIR="$HOME/myworkspace/TextTrix/texttrix" # Text Trix build script directory
DEST="/home/share" # Text Trix build destination
PKGNAME="texttrix-0.3.4" # Text Trix build name, where x.y.z refer to ver num
ZIP="$PKGNAME.zip" # name of zipped package
VFATDIR="/mnt/vfat/tmp" # alternate location; repeat for more sites

###################
# Only change for tweaking
###################

# run the Text Trix build script and package the resulting zip file in
# a Jar Ajar self-extractor
cd $WKDIR
sh $TTXDIR/pkg.sh
cp $DEST/$ZIP .
sh pkg.sh $ZIP
cp $PKGNAME*.jar $DEST
ls -l $DEST/$PKGNAME*.jar

# copy to alternative location
rm -f $VFATDIR/$PKGNAME*.jar
cp $PKGNAME*.jar $VFATDIR
