/* ShowText.java - help viewer for Txtrx,
 * the standalone command-line version of Text Trix
 * 
 * Text Trix
 * Meaningful Mistakes
 * http://texttrix.sourceforge.net
 * http://sourceforge.net/projects/texttrix
 *
 * The Java-Runtime-Environment-independent version of Txtrx uses 
 * libgcj, a library under the GNU General Public License and the
 * libgcc exception.  See gpl.txt and LIBGCJ_LICENSE in the
 * version's packages for a copy of the license and exception.
 * 
 * Copyright (c) 2002, David Young
 * All rights reserved.
 *
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

/**Shows <code>Txtrx</code>'s help documentation.
 * The documentation is hard-coded to allow a standalone Txtrx version
 * to consist of a single file.  Txtrx could then be used without
 * installation.
 *
 * The Java version should work with almost any Java Runtime Environment.
 * Care has been taken to avoid any classes or methods beyond Sun's JDK 1.1
 * to ensure widespread compatibility with existing JRE's, including
 * the GNU gcj and libgcj.
 */
public class ShowText {

	/**Creates a <code>ShowText</code> text object.
	 * Not used much since the <code>main</code> function handles
	 * command-line input and uses the rest of <code>ShowText</code>
	 * statically.
	 */
	public ShowText() {
	}

	/**Prints the command help documentation to the screen.
	 * Uses hard-coded text.
	 */
	public static void showHelp() {
		System.out.println(
				"Txtrx, Text Trix's command-line maker of Meaningful Mistakes.\n\n"
				+ "Commands:\n\n"
				+ "\tList together, preceding with a single \"-\":\n\n"
				+ "-v - Verbose - default; explicit output, including file contents\n"
				+ "-h - Help - show this notification\n"
				+ "-r - extra hard Return remover - remove single hard returns,\n preserving double returns, \"-\" or \"*\" lists,\n and hard returns within <pre></pre> tags\n\n"
				+ "\tList each of these commands by itself, including\n\tthe \"--\"; list no other commands:\n\n"
				+ "--help - Help - show this notification\n"
				+ "--license - License - show Txtrx's license\n"
				);
	}

	/**Prints the command help documentation to the screen.
	 * Inputs the text from a file.
	 * @param path command help documentation file location
	 */
	public static void showHelp(String path) {
		showFile(path);
	}

	/**Prints the license documentation to the screen.
	 * Uses hard-coded text.
	 */
	public static void showLicense() {
		System.out.println(
			"Text Trix\n"
			+ "Meaningful Mistakes\n"
			+ "http://texttrix.sourceforge.net\n"
			+ "http://sourceforge.net/projects/texttrix\n"
			+ "This software is OSI Certified Open Source Software.\n"
			+ "OSI Certified is a certification mark of the\n"
			+ "Open Source Initiative.\n\n"
			
			+ "The Java-Runtime-Environment-independent version of\n"
			+ "Txtrx uses libgcj, a library under the\n"
			+ "GNU General Public license with the libgcc exception.\n"
			+ "See gpl.txt and LIBGCJ_LICENSE in the version's packages\n"
			+ "for a copy of the license and exception.\n\n"

			+ "Copyright (c) 2002, David Young\n"
			+ "All rights reserved.\n\n"

			+ "Redistribution and use in source and binary forms, with\n"
			+ "or without modification, are permitted provided that\n"
			+ "the following conditions are met:\n\n"

			+ "\t* Redistributions of source code must retain the\n"
			+ "\tabove copyright notice, this list of conditions\n"
			+ "\tand the following disclaimer.\n"
			+ "\t* Redistributions in binary form must reproduce\n"
			+ "\tthe above copyright notice, this list of conditions\n"
			+ "\tand the following disclaimer in the documentation\n"
			+ "\tand/or other materials provided with the distribution.\n"
			+ "\t* Neither the name of the Text Trix nor the names of\n"
			+ "\tits contributors may be used to endorse or promote\n"
			+ "\tproducts derived from this software without specific\n"
			+ "\tprior written permission.\n\n"

			+ "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND\n"
			+ "CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES,\n"
			+ "INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF\n"
			+ "MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE\n"
			+ "DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR\n"
			+ "CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,\n"
			+ "INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES\n"
			+ "(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE\n"
			+ "GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR\n"
			+ "BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF\n"
			+ "LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n"
			+ "(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT\n"
			+ "OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE\n"
			+ "POSSIBILITY OF SUCH DAMAGE.\n");
	}

	/**Prints the license documentation to the screen.
	 * Inputs the text from a file.
	 * @param path license documentation file location
	 */
	public static void showLicense(String path) {
		showFile(path);
	}

	/**Prints the version information to the screen.
	 * Uses hard-coded text.
	 */
	public static void showVersion() {
		System.out.println("Txtrx, v.0.1.4-3");
	}

	/**Prints the version information to the screen.
	 * Inputs the text from a file.
	 * @param path version information file location
	 */
	public static void showVersion(String path) {
		showFile(path);
	}

	/**Reads a given file and outputs it to the screen.
	 * @param path location of file to output
	 */
	public static void showFile(String path) {
		try {
			BufferedReader reader = 
				new BufferedReader(new FileReader(path));
			String line = "";

			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			reader.close();
		} catch(IOException e) {
			System.out.println("Please see the package in which Text Trix came\n for this documentation.");
		}
	}
}
