readme-src.txt

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
Microsystems (http://java.sun.com/j2se/1.4.2/download.html); also available from 
IBM (https://www6.software.ibm.com/dl/lxdk/lxdk-p), the only major kit for 
Linux PPC systems (IBM SDK for 32-bit iSeries/pSeries)
-To use the compile script on Windows(R) systems, use the Cygwin environment (http://www.cygwin.com/)

Instructions for compilation:
-Unzip Text Trix:
	-Use a graphical unzip program, or
	-Change to the directory holding the zip file: 
	"cd [zip file path]", and
	-type "unzip texttrix-x.y.z-src.zip"
-Compile the Java classes:
	-Change into the Text Trix directory: "cd texttrix-x.y.z"
	-Update the "JAVA" variable in the shell scripts, "texttrix/pkg.sh" and "texttrix/plug.sh", to point to the location of the compilers on your system.  Set "DEST" to the final destination for the packages you are building.  Note: make sure *not* to set "BASE_DIR" 
	to the directory containing "texttrix-0.3.4-src".
	-Run "sh texttrix/pkg.sh"
-To make a self-installing Jar Ajar[tm] package, configure the variables in "texttrix/pkg-jaj.sh" and run it: "sh texttrix/pkg-jaj.sh"
-See "readme.txt" for information on running Text Trix

Troubleshooting:
-Ensure that the Java 2 Software Development Kit, v.1.4+, is installed.  To 
check, type on a command line, "java -version", and look for output indicating 
Java version 1.4 or higher.  Next, type "javac" to check for the compiler's 
presence.  If "java" or "javac" are not in your PATH envrionment variable, 
either add the commands' directory to PATH or type the full path for each use 
of the command, such as "[path to the java]/java".
-Note that the script "pkg-jaj.sh" has not yet worked under all tested 
environments
-If not using the script:
	-First compile the main Text Trix classes: 
	"cd texttrix", "javac com/textflex/texttrix/*.java", and next
	-Compile each plugin: "cd ../plugins", 
	"javac -classpath ..:[plugin folder] [plugin folder]/com/textflex/texttrix/*.java"
-See http://textflex.com/texttrix/faq.html for more details

Run...away?
-If any of the instructions have not worked, check out 
http://textflex.com/texttrix/faq.html or email support@textflex.com
-And please don't run away...help is just some text away!



Text Trix, Jar Ajar, and Text Flex are trademarks of Text Flex.
Sun Microsystems, Java, and all Java-based marks are trademarks or registered trademarks of Sun Microsystems, Inc. in the United States and 
other countries. Text Flex is independent of Sun Microsystems, Inc.
Linux is a registered trademark of Linus Torvalds.
Microsoft and Windows are registered trademarks of Microsoft Corporation in 
the United States and/or other countries.
Apple is a registered trademark of Apple Computer, Inc.