/* Practice.java - the practical functions for Text Trix
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

public class Practical {

	public Practical() {
	}

    /**Removes extra hard returns.
	 * For example, unformatted email arrives with hard returns inserted after 
	 * every line; this method strips all but the paragraph, double-spaced
	 * hard returns.
	 * Text within <code>&#60pre&#62</code>
	 * and <code>&#60/pre&#62</code> tags are
	 * left untouched.  Additionally, each line whose first
	 * non-space character is a dash, asterisk, or tab
	 * gets its own line.  The line above such lines also gets to remain
	 * by itself.
	 * @param s the full text from which to strip
	 * extra hard returns
	 * @return stripped text
	 */
    public static String removeExtraHardReturns(String s) {
		String stripped = "";
	
		while (!s.equals("")) {
	    	int singleReturn = s.indexOf("\n");
		    int doubleReturn = s.indexOf("\n\n");
		    int dash = s.indexOf("-");
	    	int asterisk = s.indexOf("*");
//	    int space = s.indexOf(" ", singleReturn + 1);
			int tab = s.indexOf("\t", singleReturn + 1);
	    	int spaces = 0;
	 	    int startPre = s.indexOf("<pre>");
		    int endPre = s.indexOf("</pre>");
	
		    // only catch dashes and asterisks after newline
	    	while (dash != -1 && dash < singleReturn) {
			dash = s.indexOf("-", dash + 1);
		    }
	    	while (asterisk != -1 && asterisk < singleReturn) {
			asterisk = s.indexOf("*", asterisk + 1);
		    }
	
		    // find all leading spaces
			int oneAfterSingleReturn = singleReturn + 1;
			// check whether have exceeded length of text
			// add one to spaces if next leading character has a space.
			while (s.length() > oneAfterSingleReturn && 
					(String.valueOf(s.charAt(oneAfterSingleReturn))).equals(" ")) {
			    spaces++;
			}

			// skip "pre"-delimited sections
		    if (startPre != -1 && startPre < singleReturn) {
				// go to the end of the "pre" section
				if (endPre != -1) {
		    		stripped = stripped 
						+ s.substring(0, endPre + 6);
			    	s = s.substring(endPre + 6);
			    // if user forgets closing "pre" tag, goes to end
				} else {
		    		stripped = stripped + s;
		    		s = "";
				}
			// join singly-returned lines
	    	} else if (singleReturn == -1) {
				stripped = stripped + s;
				s = "";
			// preserve doubly-returned lines, as between paragraphs
	    	} else if (singleReturn == doubleReturn) {
				stripped = stripped 
			    	+ s.substring(0, doubleReturn + 2);
				s = s.substring(doubleReturn + 2);
			// preserve separate lines for lines starting w/
			// dashes or asterisks or spaces before them
		    } else if (dash == singleReturn + 1 + spaces
			    	   || asterisk == singleReturn + 1 + spaces) {
				// + 2 to pick up the dash
				stripped = stripped 
			    	+ s.substring(0, singleReturn + 2 + spaces);
				s = s.substring(singleReturn + 2 + spaces);
			// preserve separate lines for ones starting with tabs
			} else if (tab == singleReturn + 1) {
				stripped = stripped + s.substring(0, singleReturn + 1);
				s = s.substring(singleReturn + 1);
			// join the tail-end of the text
		    } else {
				stripped = stripped 
		    		+ s.substring(0, singleReturn) + " ";
				s = s.substring(singleReturn + 1);
		    }
		}	
		return stripped;
    }

	public static String removeHTMLTags(String atext) {
		String text = aText;
		return text;
	}
}
