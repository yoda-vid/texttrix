readme-src.txt

Text Trix
the text tinker
http://textflex.com/texttrix
This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the
Open Source Initiative.

Copyright (c) 2002-4, Text Flex
All rights reserved.

-----------------
Welcome to the Text Trix[tm] writer!  Text Trix tinkers with text. The text 
processor houses bunches of practical and goofy tools to mess with text. 
Make it really right, or wonderfully wrong.

System Req:
-Java[tm] 2 Standard Edition (J2SE) Software Development Kit (SDK), v.1.4+, 
freely available from Sun Microsystems 
(http://java.sun.com/j2se/downloads/index.html); also available from IBM 
(https://www6.software.ibm.com/dl/lxdk/lxdk-p), offering the only major kit for 
Linux PPC systems (IBM(R) SDK for 32-bit iSeries/pSeries)
-To use the build scripts on Windows(R) systems, use the Cygwin 
environment (http://www.cygwin.com/)

Instructions for compilation:
-Unzip the Text Trix source package:
	-Use a graphical unzip program, or
	-Change to the directory holding the zip file ("cd [zip file path]"), 
	and type "unzip texttrix-x.y.z-src.zip"
-Compile the Java classes:
	-Change into the Text Trix directory: "cd texttrix-x.y.z"
	-Build the Text Trix program and its plug-ins: "sh build.sh --plug --pkg".  
	If the Java compile commands are not located in your path, specify their 
	folder with the argument, "--java [path-to-java-compiler-binaries]".  If 
	necessary, modify the "DEST" variable in the script file, "build.sh", to 
	point to the final destination directory for the packages you are building.  
	Note: make sure *not* to set "BASE_DIR" to the directory containing 
	"texttrix-x.y.z-src".
-To make a self-extracting Jar Ajar[tm] package for your newly compiled Text 
Trix package, download the Jar Ajar packaging suite 
(http://textflex.com/jarajar).  Install it, launch its graphical packager, and 
you'lll be able to fill in the details in no time.

Troubleshooting:
-Ensure that the Java 2 Software Development Kit, v.1.4+, is installed.  To 
check, type on a command line, "java -version", and look for output 
indicating Java version 1.4 or higher.  Next, type "javac" to check for the 
compiler's presence.  If "java" or "javac" are not in your PATH environment 
variable, either add the commands directory to PATH or type the full path for 
each use of the command, such as "[path to java]/java".
-See "readme.txt" for information on running Text Trix
-See http://textflex.com/texttrix/faq.html for more details

Run...away?
-If any of the instructions have not worked, check out our website for the 
FAQs and forums, or email us at support@textflex.com
-And please don't run away...help is just some text away!



Text Trix, Jar Ajar, and Text Flex are trademarks of Text Flex.
Sun Microsystems, Java, and all Java-based marks are trademarks or 
registered trademarks of Sun Microsystems, Inc. in the United States and 
other countries. Text Flex is independent of Sun Microsystems, Inc.
Linux is a registered trademark of Linus Torvalds.
Microsoft and Windows are registered trademarks of Microsoft Corporation in 
the United States and/or other countries.
Apple is a registered trademark of Apple Computer, Inc.
IBM is a trademark of International Business Machines Corporation in the 
United States, other countries, or both.