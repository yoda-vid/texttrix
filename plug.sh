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

# Text Trix Plug-In Packager

#####################
# User-defined variables
# Check them!
####################

# Compiler location
JAVA="/usr/java/j2sdk1.4.2_01/bin" # set for J2SDK ver. 1.4.2
SYSTEM=`uname -s`
CYGWIN="false"
if [ `expr "$SYSTEM" : "CYGWIN"` -eq 6 ]
then
	JAVA="/cygdrive/c/j2sdk1.4.2_01/bin" # assuming compiler in C drive
	CYGWIN="true"
fi

# Source directories
BASE_DIR=""
if [ "x$BASE_DIR" = "x" ] # empty string
then
	if [ `expr index "$0" "/"` -eq 1 ]
	then
		BASE_DIR="$0"
	else
		BASE_DIR="$PWD/$0"
	fi
	BASE_DIR="${BASE_DIR%/texttrix/plug.sh}" # assumes the script's name is plug.sh
	#BASE_DIR="${BASE_DIR%/}"
fi
TTX_DIR="$BASE_DIR/texttrix" # texttrix folder within main dir
PLGS_DIR="$BASE_DIR/plugins" # plugins folder within main dir
DIR="com/textflex/texttrix" # src package structure
PLUGINS="Search NonPrintingChars ExtraReturnsRemover HTMLReplacer LetterPulse" # the chosen plugins

#####################
# Build operations
#####################
# change to work directory and compile Text Trix classes
cd $BASE_DIR
if [ "$CYGWIN" = "true" ]
then
	$JAVA/javac "`cygpath -p -w $TTX_DIR/$DIR`"/*.java
else
	$JAVA/javac "$TTX_DIR/$DIR/"*.java
fi
# change to plugins directory and compile and package each plugin;
# list the directory names and their corresponding classes in the "for" line;
# the jars must have the same name and caps as their classes
cd $PLGS_DIR
if [ ! -d "$TTX_DIR/plugins" ]
then
	mkdir "$TTX_DIR/plugins"
fi
for plugin in $PLUGINS
do
	plugin_dir=`echo "$plugin" | tr "[:upper:]" "[:lower:]"`
	# extends the PlugIn class of the Text Trix package
	# CYGWIN USERS: uncomment the following line, and comment the next:
	if [ "$CYGWIN" = "true" ]
	then
		$JAVA/javac -classpath "`cygpath -p -w $TTX_DIR:$plugin_dir`" "`cygpath -p -w $plugin_dir/$DIR`"/*.java
	else
		$JAVA/javac -classpath "$TTX_DIR":"$plugin_dir" "$plugin_dir/$DIR/"*.java
	fi
	cd $plugin_dir
	$JAVA/jar -0cvf $plugin.jar $DIR/*.class $DIR/*.png \
	$DIR/*.html && mv $plugin.jar $TTX_DIR/plugins 
	cd ..
done

exit 0
