/* Txtrx.java - standalone command-line version of Text Trix
 * Text Trix
 * a goofy gui editor
 * http://texttrix.sourceforge.net
 * http://sourceforge.net/projects/texttrix

 * Copyright (c) 2002, David Young
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Text Trix nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package net.sourceforge.texttrix;

import java.io.*;

public class Txtrx {

	public Txtrx() {
	}

	public static void main(String[] args) {
		if (args.length > 1) {
			applyCmds(args);
		} else if (args.length == 1 && args[0].indexOf("-") == 0) {
			System.out.println("Please supply files to goof with.");
		} else {
			// likely want to replace by automatically calling --help
			System.out.println("Type \"txtrx --help\" for more information.");
		}
	}

	/**Applies the selected commands to the given files.
	 * Commands are preceded by a dash, "-", and files come afterware.
	 * Specifying no commands defaults to the "v" command, "verbose" operation.
	 * @param args array of commands and files.  Commands are optional
	 * and come before the filenames.  Assumes that args specifies at least
	 * one file.
	 */
	public static void applyCmds(String[] args) {
		String cmds = args[0];
		String cmd;
		boolean verbose = false;
		int fileIndex = 1;
		
		// one or more args: files
		if (cmds.indexOf("-") != 0) {
			verbose = true;
			cmd = "v";
			fileIndex = 0;
		// one or more args: commands that include "v", files
		} else if (cmds.indexOf("v") != -1) {
			verbose = true;
			cmds.replace("-", "");
			cmds.replace("v", "");
		}
	
		for (fileIndex; fileIndex < args.length; fileIndex++) {
			String path = args[fileIndex];
			String text;
			/** prob not necessary since prob does automatically
			if (path.charAt(0) == "/") {
				path = args[i];
			} else {
				path = "./" + args[i];
			}
			*/
			
			try {
				BufferedReader reader =
					new BufferedReader(new FileReader(path));
				text = readFile(reader, path);
			} catch(IOException e) {
				System.out.println(path + " is not a file");
			}
			
			if (cmd == "v") {
				displayText(text);
			} else if (cmds != null) {
				text = applyCmd(cmds.substring(0, 1), text);
				cmds = cmds.substring(1);
				if (verbose)
					displayText(text);
				writeFile(text);
			}			
		}
	}

	/**Applies a single command to a given string.
	 * @param cmd goofy or practical command to apply:
	 * "r" is the Extra Hard Return Remover (practical function),
	 * "h" is the HTML tag replacer (practical function).
	 * @param text text to modify
	 */
	public String applyCmd(String cmd, String text) {
		if (cmd == "r") {
			return Practical.removeExtraHardReturns(text);
		} else if (cmd == "h") {
			return Practical.replaceHTMLTags(text);
		} else {
			return text;
		}
	}
	
	public static String readFile(BufferedReader reader) {
		try {
			String text = "";
			String line;
			while (line = reader.readLine() != null) {
				text = text + line + "\n";
			}
			return text;
		} catch(IOException e) {
			System.out.println(path + " is apparently not a text file");
			return text;
		}
	}

	public static void writeFile(String text) {
	}
}
