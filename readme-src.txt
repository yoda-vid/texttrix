readme-src.txt

Text Trix
the text tinker
http://textflex.com/texttrix
This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the
Open Source Initiative.

Copyright (c) 2002-7, Text Flex
All rights reserved.

-----------------
Text Flex[tm] Text Trix[tm] is general purpose editor that makes coding 
easier and more precise.  Navigational tools help make large files easier 
to read and organize.  Special plugins manipulate text according to user-
defined settings.

System Req:
-Java[tm] Development Kit (JDK), v.1.4+, freely available from Sun 
Microsystems[tm] (http://java.sun.com/javase/downloads/index.jsp).  
Java 6 is recommended.
-To use the build scripts on Windows(R) systems, use the Cygwin 
environment (http://www.cygwin.com/).

Instructions for compilation:
-Unzip the Text Trix source package
-Compile the Java classes:
	-Auto-configure the build environment: "sh texttrix/configure".  To 
	specify the location of the Java compiler, pass the argument, 
	"--java=[path-to-java-compiler-binaries]".  See "--help" for other options.
	-Build Text Trix and its plug-ins: "make all".
	-Text Trix is ready to go!  To run it, type: "sh texttrix/run.sh".

(Optional:) Installation
-Type: "make install".  By default, Text Trix will be installed into 
/usr/local/texttrix-x.y.z.  To customize the location, specify
"--prefix=[install-path]".
-You can also run the "texttrix/pkg.sh" script to create distributable 
packages.
-To make a self-extracting Jar Ajar[tm] package for your newly compiled 
Text Trix package, download the Jar Ajar packaging suite 
(http://textflex.com/jarajar).  Install it, launch its graphical packager, and 
you'lll be able to fill in the details in no time.

Write Your Own Plug-In:
-See the Developer's Corner on our website (http://textflex.com/texttrix)
for details on how to write your own plug-in for Text Trix!

Troubleshooting:
-Text Trix has been tested on Windows XP SP2, Fedora Core 6, and
MacOS X 10.4.  If you've tested it on another platform, please let us 
know about your experience.
-See "readme.txt" for information on running Text Trix
-See http://textflex.com/texttrix/faq.html for more details


-----------------
Text Trix, Jar Ajar, and Text Flex are trademarks of Text Flex.
Sun Microsystems, Java, and all Java-based marks are trademarks or 
registered trademarks of Sun Microsystems, Inc. in the United States and 
other countries. Text Flex is independent of Sun Microsystems, Inc.
Linux is a registered trademark of Linus Torvalds.
Microsoft and Windows are registered trademarks of Microsoft Corporation 
in the United States and/or other countries.
Apple is a registered trademark of Apple Computer, Inc.