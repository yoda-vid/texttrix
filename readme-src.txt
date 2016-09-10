readme-src.txt

Text Trix[tm]
the text tinker
http://textflex.com/texttrix
This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the
Open Source Initiative.

Copyright (c) 2002, 2016, Text Flex
All rights reserved.

=============================================
The Text Trix editor is an open-source, cross-platform text editor whose 
goal is to make file and text navigation easier for coding and general text 
editing.

System Reqs
----------------
-Java Development Kit (JDK), v.6+, is required for compiling Text Trix.
-Bash scripts (.sh) are included for compiling and packaging on Linux, 
MacOS X, or Windows (via Cygwin) platforms.
-PowerShell scripts (.ps1) are included for some tasks on Windows 
platforms.

Instructions for compilation
--------------------------------
1) Unzip the Text Trix source package
2) Compile the Java classes:
  -cd to the source directory.
  -Run "texttrix/build.sh --plug" to build the editor and all plugins.
3) Run "texttrix/run.sh" to start the editor.

https://sourceforge.net/p/texttrix/wiki/Building%20Text%20Trix/
outlines all of the different ways to compile Text Trix, whether from this 
source package or from the various branches in the Subversion repository.

(Optional:) Installation
--------------------------
-You can also run the "texttrix/pkg.sh" script to create distributable 
packages, along with a double-clickable TextTrix.jar file.
-To make a self-extracting Jar Ajar[tm] package for your newly compiled 
Text Trix package, download the Jar Ajar packaging suite 
(http://textflex.com/jarajar).  Install it, launch its graphical packager, and 
you'lll be able to fill in the details in no time.

Write Your Own Plug-In
----------------------------
-See the https://sourceforge.net/p/texttrix/wiki/PlugIn/
for details on how to write your own plug-in for Text Trix!

Troubleshooting
-------------------
-Text Trix has been tested on Windows XP/7/10 (Java 6/7/8), 
Fedora 22 and Ubuntu 15.04 (OpenJDK 8), and MacOS X 10.11 (Java 8).  If 
you've tested it on another platform, please let us know about your 
experience.
-See "readme.txt" for information on running Text Trix.
-See http://textflex.com/texttrix for additional details.