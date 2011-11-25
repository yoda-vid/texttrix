readme-src.txt

Text Trix[tm]
the text tinker
http://textflex.com/texttrix
This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the
Open Source Initiative.

Copyright (c) 2002-11, Text Flex
All rights reserved.

-----------------
The Text Trix editor is an open-source, cross-platform text editor whose 
goal is to make file and text navigation easier for coding and general text 
editing.

System Req:
-Java Development Kit (JDK), v.1.4+ required.
-Bash scripts (.sh) are included for compiling and packaging on Linux, 
MacOS X, or Windows (via Cygwin) platforms.
-PowerShell scripts (.ps1) are included for some tasks on Windows 
platforms.

Instructions for compilation:
-Unzip the Text Trix source package
-Compile the Java classes:
	-cd to the source directory.
	-Run "texttrix/build.sh --plug" to build the editor and all plugins.
-Run "texttrix/run.sh" to start the editor.

(Optional:) Installation
-You can also run the "texttrix/pkg.sh" script to create distributable 
packages, along with a double-clickable TextTrix.jar file.
-To make a self-extracting Jar Ajar[tm] package for your newly compiled 
Text Trix package, download the Jar Ajar packaging suite 
(http://textflex.com/jarajar).  Install it, launch its graphical packager, and 
you'lll be able to fill in the details in no time.

Write Your Own Plug-In:
-See the Developer's Corner on our website (http://textflex.com/texttrix)
for details on how to write your own plug-in for Text Trix!

Troubleshooting:
-Text Trix has been tested on Windows XP/7 (Sun Java 6 Update 29), 
Fedora 15 and Ubuntu 11.04 (OpenJDK 6), and MacOS X 10.6 (Java 6).  If 
you've tested it on another platform, please let us know about your 
experience.
-See "readme.txt" for information on running Text Trix.
-See http://textflex.com/texttrix for additional details.