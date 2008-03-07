JFLAGS=-classpath ../../..:/usr/lib/ant/lib/ant.jar
JAVA=java $(JFLAGS)
JAVAC=javac -g:none $(JFLAGS)
JAVADOC=javadoc $(JFLAGS)
JLEXFLAGS=-q
JLEX=$(JAVA) JFlex.Main $(JLEXFLAGS)
BTE=$(JAVA) $(JFLAGS) com.Ostermiller.bte.Compiler
CVS=cvs

.SUFFIXES:
.SUFFIXES: .lex .java
.SUFFIXES: .java .class
.SUFFIXES: .bte .html

.PHONY: all
all:
	@$(MAKE) -s --no-print-directory junkclean
	@$(MAKE) -s --no-print-directory spell
	@$(MAKE) -s --no-print-directory compile
	@$(MAKE) -s --no-print-directory web
	@$(MAKE) -s --no-print-directory build
	@$(MAKE) -s --no-print-directory javadoc

.PHONY : compile
compile: lexerclasses classes

JAVAFILES=$(wildcard *.java)
.PHONY: classes
classes: $(JAVAFILES:.java=.class)
	@# Write a bash script that will compile the files in the todo list
	@echo "#!/bin/bash" > tempCommand	
	@# If the todo list doesn't exist, don't compile anything
	@echo "if [ -e tempChangedJavaFileList ]" >> tempCommand
	@echo "then" >> tempCommand
	@# Make sure each file is only on the todo list once.
	@echo "sort tempChangedJavaFileList | uniq  > tempChangedJavaFileListUniq" >> tempCommand
	@echo "FILES=\`cat tempChangedJavaFileListUniq\`" >> tempCommand
	@# Compile the files.
	@echo "echo Make: Compiling: $$ FILES" >> tempCommand
	@echo "$(JAVAC) $$ FILES" >> tempCommand
	@echo "fi" >> tempCommand
	@# Remove extra spaces in the script that follow the dollar signs.
	@sed "s/\$$ /\$$/" tempCommand > tempCommand.sh
	@# Make the script executable.
	@chmod +x tempCommand.sh
	@# Call the script
	@./tempCommand.sh
	@rm -f tempCommand tempCommand.sh tempChangedJavaFileList tempChangedJavaFileListUniq

.java.class:
	@#for each changed java file, add it to the todo list.
	@echo "$<" >> tempChangedJavaFileList

.PHONY: htmlclean
htmlclean:
	@echo Make: Removing generated html documents.

NOOUTPUTBTE=(page|simple|syntax)
BTEFILES=$(wildcard *.bte)
.PHONY: html
html: $(BTEFILES:.bte=.html)
	@# Write a bash script that will compile the files in the todo list
	@echo "#!/bin/bash" > tempCommand	
	@# If the todo list doesn't exist, don't compile anything
	@echo "if [ -e tempChangedBTEFileList ]" >> tempCommand
	@echo "then" >> tempCommand
	@# Make sure each file is only on the todo list once.
	@echo "sort tempChangedBTEFileList | uniq | egrep -v \"$(NOOUTPUTBTE)\" > tempChangedBTEFileListUniq" >> tempCommand
	@echo "FILES=\`cat tempChangedBTEFileListUniq\`" >> tempCommand
	@echo "if [ \"$$ FILES\" ]" >> tempCommand
	@echo "then" >> tempCommand
	@# Compile the files.
	@echo "echo Make: Compiling: $$ FILES" >> tempCommand
	@echo "$(BTE) $$ FILES" >> tempCommand
	@echo "fi" >> tempCommand
	@echo "fi" >> tempCommand
	@# Remove extra spaces in the script that follow the dollar signs.
	@sed "s/\$$ /\$$/" tempCommand > tempCommand.sh
	@# Make the script executable.
	@chmod +x tempCommand.sh
	@# Call the script
	@./tempCommand.sh
	@rm -f tempCommand tempCommand.sh tempChangedBTEFileList tempChangedBTEFileListUniq

.bte.html:
	@#for each changed java file, add it to the todo list.
	@echo "$<" >> tempChangedBTEFileList

.PHONY: classesclean
classesclean: 
	@echo Make: Removing Syntax class files.
	@rm -f *.class
.PHONY: junkclean	        
junkclean: lexerjunkclean
	@echo Make: Removing Syntax detritus.
	@rm -rf ~* *~ *bak temp* com/ gnu/
.PHONY: javadocclean
javadocclean: 
	@echo Make: Removing Syntax documentation.
	@rm -rf docs/
.PHONY: buildclean
buildclean: 
	@echo Make: Removing Syntax jar files.
	@rm -rf syntax.jar

.PHONY: webclean
webclean: htmlclean

.PHONY: clean
clean: lexerclean junkclean javadocclean buildclean webclean
	@echo Make: Removing Syntax class files.
	@rm -f *.class javadoc release
	@$(MAKE) -C Lexer clean

.PHONY: allclean
allclean: lexerallclean clean
	@echo Make: Removing Syntax make locks.
	@rm -f javadoc release spell

.PHONY: build
build: syntax.jar

spell: *.bte *.java Lexer/*.java Lexer/*.lex
	@echo Make: Running spell check.
	@./spell.sh $?
	@touch spell

syntax.jar: ../util/StringHelper.* ../util/CircularCharBuffer* ../util/BufferOverflowException.* \
	*.dict *.bte *.java *.sh COPYING.TXT package.html Makefile \
	 Lexer/*.*  Lexer/Makefile doc/*.* ../bte/CompileException.* \
	 ../../../gnu/getopt/*.* *.sh	
	@echo Make: Building jar file
	@mkdir -p com/Ostermiller/Syntax/Lexer
	@cp *.sh *.bte  *.java *.class *.properties COPYING.TXT package.html Makefile com/Ostermiller/Syntax/
	@cp Lexer/*.* Lexer/Makefile com/Ostermiller/Syntax/Lexer/	
	@rm -f com/Ostermiller/Syntax/Lexer/*?Lexer.java
	@mkdir com/Ostermiller/Syntax/doc
	@cp doc/*.* com/Ostermiller/Syntax/doc/        
	@mkdir com/Ostermiller/util
	@cp ../util/StringHelper.* ../util/CircularCharBuffer* ../util/BufferOverflowException.* com/Ostermiller/util/
	@mkdir com/Ostermiller/bte
	@cp ../bte/CompileException.* com/Ostermiller/bte/
	@mkdir -p gnu/getopt		
	@cp ../../../gnu/getopt/*.* gnu/getopt
	@jar cmfv syntax.mf syntax.jar com/ gnu/ > /dev/null
	@rm -rf com/ gnu/

javadoc: *.java Lexer/*.java
	@echo Make: Generating javadoc
	@rm -rf docs
	@mkdir docs
	@mv package.html temp
	@mv Lexer/package.html Lexer/temp
	@$(JAVADOC) -quiet -link http://java.sun.com/j2se/1.3/docs/api/ -d docs/ com.Ostermiller.Syntax com.Ostermiller.Syntax.Lexer > /dev/null
	@mv temp package.html
	@mv Lexer/temp Lexer/package.html
	@touch javadoc
 
.PHONY: web 
web: html
               
.PHONY: test
test:
	$(JAVA) com.Ostermiller.Syntax.ToHTML -f -t simple.bte -T "C Syntax Highlighting Test" -l com.Ostermiller.Syntax.Lexer.CLexer -i whitespace -o temp.html doc/TestCases.c 
	@diff temp.html doc/TestCases.c.html 
	$(JAVA) com.Ostermiller.Syntax.ToHTML -f -t simple.bte -T "Simple HTML Syntax Highlighting Test" -l com.Ostermiller.Syntax.Lexer.HTMLLexer -i whitespace -o temp.html doc/TestCases.html
	@diff temp.html doc/TestCases.html.simple.html 
	$(JAVA) com.Ostermiller.Syntax.ToHTML -f -t simple.bte -T "Complex HTML Syntax Highlighting Test" -l com.Ostermiller.Syntax.Lexer.HTMLLexer1 -i whitespace -o temp.html doc/TestCases.html
	@diff temp.html doc/TestCases.html.complex.html 
	$(JAVA) com.Ostermiller.Syntax.ToHTML -f -t simple.bte -T "Java Syntax Highlighting Test" -l com.Ostermiller.Syntax.Lexer.JavaLexer -i whitespace -o temp.html doc/TestCases.java
	@diff temp.html doc/TestCases.java.html 	
	$(JAVA) com.Ostermiller.Syntax.ToHTML -f -t simple.bte -T "Java Properties Syntax Highlighting Test" -l com.Ostermiller.Syntax.Lexer.PropertiesLexer -i whitespace -o temp.html doc/TestCases.properties
	@diff temp.html doc/TestCases.properties.html 	
	$(JAVA) com.Ostermiller.Syntax.ToHTML -f -t simple.bte -T "LaTeX Syntax Highlighting Test" -l com.Ostermiller.Syntax.Lexer.LatexLexer -i whitespace -o temp.html doc/TestCases.latex
	@diff temp.html doc/TestCases.latex.html 	
	$(JAVA) com.Ostermiller.Syntax.ToHTML -f -t simple.bte -T "SQL Syntax Highlighting Test" -l com.Ostermiller.Syntax.Lexer.SQLLexer -i whitespace -o temp.html doc/TestCases.sql
	@diff temp.html doc/TestCases.sql.html 	
	@rm temp.html syntax.css

.PHONY: update
update: 
	@echo Make: Updating from CVS
	@$(CVS) update -RPd .

.PHONY: commit
commit: 
	@echo Make: Committing to CVS
	@$(CVS) commit

release: syntax.jar *.html simple.bte page.bte *.css *.gif doc/syntax.css doc/TestCases.*.html docs	
	@./release.sh $?
	@touch release

.PHONY: lexerclasses
lexerclasses:
	@$(MAKE) -C Lexer classes --no-print-directory

.PHONY: lexerclean
lexerclean:
	@$(MAKE) -C Lexer clean --no-print-directory

.PHONY: lexerjunkclean
lexerjunkclean:
	@$(MAKE) -C Lexer junkclean --no-print-directory

.PHONY: lexerallclean
lexerallclean:
	@$(MAKE) -C Lexer allclean --no-print-directory


