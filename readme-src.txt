readme.txt

Text Trix
the text tinker
http://textflex.com/texttrix
This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the
Open Source Initiative.

Copyright (c) 2002-3, Text Flex
All rights reserved.

-----------------
Welcome to Text Trix!  We're just getting underway here.
Please try us out!

System Reqs:
-Java[tm] 2 Software Development Kit 1.4+, freely available from Sun (http://java.sun.com/getjava/index.html)

Instructions for compilation:

-Unzip Text Trix by typing on a command line: 
"unzip texttrix-x.y.z-src.zip" from the directory holding the zip file
-Change into the Text Trix directory: "cd [path to Text Trix]/texttrix-x.y.z"
-Compile the Java classes:
	-The easiest way is to run the shell script: "sh pkg.sh," which runs on Linux and Cygwin systems
	-Without the script, first compile the main Text Trix "javac com/textflex/texttrix/*.java", and
	-compile each plugin: "cd plugins", "javac -classpath ..:[plugin folder] [plugin folder]/com/textflex/texttrix/*.java"
-Package the plugins into uncompressed JARs: in the plugins folder, type for each plugin, "cd [plugin folder]", "jar -cvf0 [plugin name, using the same caps as in the plugin .java file] com"
-See "readme.txt" for information on running the 

Troubleshooting:
-Ensure that the Java 2 Software Development Kit, v.1.4+ is installed: type "javac -version" to see if the output displays version 1.4 or higher

Run...away?
-if any of the instructions have not worked, check out 
http://textflex.com/texttrix/downloads.html, 
http://textflex.com/texttrix/faq.html, or 
email support@textflex.com
-and please don't run away...help is just some text away!



Text Trix and Text Flex are trademarks of Text Flex.
Java and all Java-based marks are trademarks or registered trademarks of Sun Microsystems, Inc. in the United States and other countries. Text Flex is independent of Sun Microsystems, Inc.
Linux is a registered trademark of Linus Torvalds.
Microsoft and Windows are registered trademarks of Microsoft Corporation in the United States and/or other countries.
Apple is a registered trademark of Apple Computer, Inc.