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
Welcome to Text Trix!  We're just getting underway here.  Please try us out!

System Req:
-Java[tm] 2 Software Development Kit 1.4+, freely available from Sun 
Microsystems (http://java.sun.com/getjava/index.html)

Instructions for compilation:
-Unzip Text Trix:
	-Use a graphical unzip program, or
	-Change to the directory holding the zip file: 
	"cd [zip file path]", and
	-type "unzip texttrix-x.y.z-src.zip"
-Compile the Java classes:
	-Change into the Text Trix directory: "cd texttrix-x.y.z"
	-The easiest way to compile is running the shell script: 
	"sh pkg.sh," which works on Linux and Cygwin systems.  Be sure 
	to change its variables to point to your desired locations.
	-Without the script, first compile the main Text Trix classes:
	"javac com/textflex/texttrix/*.java", and next
	-compile each plugin: "cd plugins", 
	"javac -classpath ..:[plugin folder] [plugin 
	folder]/com/textflex/texttrix/*.java"
-Package the plugins into uncompressed JARs: in the plugins folder, type for 
each plugin, "cd [plugin folder]", "jar -cvf0 [plugin name, using upper case
characters identically to that in the plugin .java file] com"
-See "readme.txt" for information on running Text Trix

Troubleshooting:
-Ensure that the Java 2 Software Development Kit, v.1.4+, is installed.  To 
check, type on a command line, "java -version", and look for output indicating 
Java version 1.4 or higher.  Next, type "javac" to check for the compiler's 
presence.  If "java" or "javac" are not in your PATH envrionment variable, 
either add the commands' directory to PATH or type the full path for each use 
of the command, such as "[path to the java]/java".
-Note that the script "pkg-jaj.sh" has not worked under all tested 
environments
-See http://textflex.com/texttrix/faq.html for more details

Run...away?
-If any of the instructions have not worked, check out 
http://textflex.com/texttrix/faq.html or email support@textflex.com
-And please don't run away...help is just some text away!



Text Trix and Text Flex are trademarks of Text Flex.
Java and all Java-based marks are trademarks or registered trademarks of 
Sun Microsystems, Inc. in the United States and other countries. Text Flex is 
independent of Sun Microsystems, Inc.
Linux is a registered trademark of Linus Torvalds.
Microsoft and Windows are registered trademarks of Microsoft Corporation in 
the United States and/or other countries.
Apple is a registered trademark of Apple Computer, Inc.