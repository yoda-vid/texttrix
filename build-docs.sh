#!/bin/sh
# Text Trix document builder
# Copyright (c) 2003, Text Flex

WK_DIR="/home/davit/src/texttrix"
JAVA="/usr/java/j2sdk1.4.2/bin"

cd $WK_DIR
$JAVA/javadoc -d docs/api -link "http://java.sun.com/j2se/1.4.2/docs/api" -overview "overview.html" "com.textflex.texttrix"
