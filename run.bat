:: run.bat
:: Copyright (c) 2011, Text Flex
:: Simple script for launching Text Trix from Windows Explorer
:: or the command line. Can be supplied a file as argument to
:: open that file alone.

@echo off

:: the main command to launch Text Trix without a jar file
set command=javaw -cp lib/jsyntaxpane.jar;. com.textflex.texttrix.TextTrix

:: changes to the script's directory
cd %~dp0

:: launches only the given file if specified as an argument
if [%1]==[] (start %command%) else (start %command% %1 --fresh)
exit