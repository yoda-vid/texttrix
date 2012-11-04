:: run.bat
:: Copyright (c) 2011-2012, Text Flex
:: Simple script for launching Text Trix from Windows Explorer
:: or the command line. If supplied a file as argument, only 
:: that file will be opened.

@echo off

:: the main command to launch Text Trix from a jar file
set command=javaw -cp lib/jsyntaxpane.jar;lib/oster.jar;. -jar TextTrix.jar

:: changes to the script's directory
cd %~dp0

:: launches the editor with all saved tabs unless supplied with
:: an argument, in which case only the specified file will be opened
if [%1]==[] (start %command%) else (start %command% %1 --fresh)
exit