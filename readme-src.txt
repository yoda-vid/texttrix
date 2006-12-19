readme-src.txt

Text Trix
the text tinker
http://textflex.com/texttrix
This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the
Open Source Initiative.

Copyright (c) 2002-5, Text Flex
All rights reserved.

-----------------
Text Flex[tm] Text Trix[tm] is general purpose editor that makes coding easier and more precise.  Navigational tools help make large files easier to read and organize.  Special plugins manipulate text according to user-defined settings.

System Req:
-Java[tm] Development Kit (JDK), v.1.4+, freely available from Sun Microsystems 
(http://java.sun.com/javase/downloads/index.jsp).  Java 6 is recommended.
-To use the build scripts on Windows(R) systems, use the Cygwin 
environment (http://www.cygwin.com/).

Instructions for compilation:
-Unzip the Text Trix source package
-Compile the Java classes:
	-Change into the Text Trix directory: "cd texttrix-x.y.z"
	-Auto-configure the build environment: "sh texttrix/configure".  To 
	specify the location of the Java compiler, pass the argument, 
	"--java=[path-to-java-compiler-binaries]".  Pass "--help" to see other options.
	-Build Text Trix and its plug-ins: "make all".
	-Text Trix is ready to go!  To run it, type: "sh texttrix/run.sh".

(Optional:) Installation
-Type: "make install".  By default, Text Trix will be installed into 
/usr/local/texttrix-x.y.z.  To customize the location, pass, 
"--prefix=[install-path]", while running "configure" (see above).
-You can also run the "texttrix/pkg.sh" script to create distributable 
packages.
-To make a self-extracting Jar Ajar[tm] package for your newly compiled 
Text Trix package, download the Jar Ajar packaging suite 
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



Text Trix, Jar Ajar, and Text Flex are trademarks of Text Flex.
Sun Microsystems, Java, and all Java-based marks are trademarks or 
registered trademarks of Sun Microsystems, Inc. in the United States and 
other countries. Text Flex is independent of Sun Microsystems, Inc.
Linux is a registered trademark of Linus Torvalds.
Microsoft and Windows are registered trademarks of Microsoft Corporation in 
the United States and/or other countries.
Apple is a registered trademark of Apple Computer, Inc.