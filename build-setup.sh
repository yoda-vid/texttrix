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
# Portions created by the Initial Developer are Copyright (C) 2015
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

# Text Trix environment setup

#####################
# User-defined variables
# Check them!
####################

JAVA_VER_SRC="8" # source and target version
CLASSES_DIR="classes" # output folder

####################
# Setup variables
####################

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

	# Java binary detection mechanism
	if [[ -z "$JAVA" ]]; then
		# Java path already set
		:
	elif [ "`command -v /usr/lib/jvm/java-1.6.0/bin/javac`" != "" ]
	then
		# OpenJDK directory on Fedora distributions
		JAVA="/usr/lib/jvm/java-1.6.0/bin"
	elif [ "`command -v /usr/lib/jvm/java-6-openjdk/bin/javac`" != "" ]
	then
		# OpenJDK directory on Ubuntu distributions
		JAVA="/usr/lib/jvm/java-6-openjdk/bin"
	elif [ "`command -v /usr/java/default/bin/javac`" != "" ]
	then
		JAVA="/usr/java/default/bin"
	elif [ "`command -v javac`" != '' ]
	then
		JAVA=""
	elif [ "`command -v /usr/bin/javac`" != "" ]
	then
		JAVA="/usr/bin"
	else
		JAVA="false"
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

# ensure file separator present at end of Java path if set
if [[ -n "$JAVA" ]]
then
	JAVA=${JAVA%\/}/
fi
