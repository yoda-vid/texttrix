Txtrx Readme

Txtrx
the text tinker
http://textflex.com/texttrix
This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the
Open Source Initiative.
The standalone executable version of Txtrx uses the 
libgcj library under the GNU General Public License 
with the libgcc exception.  See gpl.txt and 
LIBGCJ_LICENCE for a copy of the license and exception.

Copyright (c) 2002-3, Text Flex
All rights reserved.

-------------
Welcome to Txtrx, the command-line version of Text Trix!

Txtrx lets you make the same Meaningful Mistakes 
to text files as Text Trix does, but converting multiple files 
in one fell swoop.  Rather than opening each file in a 
graphical editor and using each text changing feature 
separately, you can now list the features--even multiple 
ones--and files--again, even multiple ones--at the 
command prompt.  Txtrx backs up your orignal files and 
spits out newly trixed ones!

-------------
Installation: no Java[tm] Runtime Environment required!

-by now, you've probably already unpacked the file, 
but if not, here's how.  At the command prompt, type:
$ bunzip2 Txtrx-x.y.z-lin.tar.bz2
$ tar xvf Txtrx-x.y.z-lin.tar

-place the file Txtrx in a location listed as part of your PATH, 
such as /usr/local/bin or /usr/bin.  For example:
$ su [to become root and gain access to /usr directories]
$ cp Txtrx /usr/local/bin
-note that you can just run Txtrx from any directory, but 
usually by having to type Txtrx's whole path each time

-------------
Running

-type "txtrx", a list of commands, and the files to goof with.  
E.g.:
$ txtrx -vr Test01.txt Test03.txt
-note the "-" in front of the command list, just as with other 
Linux commands.  "v" stands for "verbose" and tells txtrx to 
let you know what it has done and show you the new text.  
"r" means "extra hard Return remover", which removes 
hard returns at the end of lines except for double spaces, 
lists, and within <pre></pre> tags

-type "txtrx --help" for a list of more commands



Text Trix and Text Flex are trademarks of Text Flex.
Java and all Java-based marks are trademarks or registered trademarks of Sun Microsystems, Inc. in the United States and other countries. Text Flex is independent of Sun Microsystems, Inc.