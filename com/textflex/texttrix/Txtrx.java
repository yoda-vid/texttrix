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
		} else if (args.length == 1) {
			System.out.println("Please supply one or more files");
		} else {
			// likely want to replace by automatically calling --help
			System.out.println("Type \"txtrx --help\" for more information.");
		}
	}

	public static void applyCmds(String[] args) {
		for (i = args.length - 1; i > 0; i++) {
			String path = args[i];
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
}
