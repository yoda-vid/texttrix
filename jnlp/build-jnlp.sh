#!/bin/base
BASE_DIR=""
# Sets the base directory to the script location
if [ "x$BASE_DIR" = "x" ] # empty string
then
	BASE_DIR=`dirname $0`
fi
cd "$BASE_DIR"
BASE_DIR="$PWD"

cp ../TextTrix.jar .
jarsigner -keystore keystore TextTrix.jar myself