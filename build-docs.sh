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

# Text Trix document builder
# Copyright (c) 2003, Text Flex

######################
# User-defined variables
# Check them!
######################
JAVA="/usr/java/j2sdk1.4.2/bin"
if [ "$OSTYPE" = "cygwin" ]
then
	JAVA="/cygdrive/c/j2sdk1.4.2/bin"
fi
BASE_DIR=""
if [ "x$BASE_DIR" = "x" ]
then
	if [ "${0:0:1}" = "/" ]
	then
		BASE_DIR="$0"
	else
		BASE_DIR="$PWD/$0"
	fi
	BASE_DIR="${BASE_DIR%/texttrix/build-docs.sh}"
fi
TTX_DIR="$BASE_DIR/texttrix"
API_DIR="$BASE_DIR/docs/api"




###################
# Build operations
###################
if [ ! -d "$API_DIR" ]
then
	echo "$API_DIR does not exist.  Please create it or set \"API_DIR\""
	echo "in $0 to a different location."
	exit 1
fi
cd $TTX_DIR
$JAVA/javadoc -d $API_DIR -link "http://java.sun.com/j2se/1.4.2/docs/api" -overview "overview.html" "com.textflex.texttrix"
