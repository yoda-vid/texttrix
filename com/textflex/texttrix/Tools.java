/* Tools.java - the tool functions for Text Trix
 * Text Trix
 * Meaningful Mistakes
 * http://texttrix.sourceforge.net
 * http://sourceforge.net/projects/texttrix
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

import java.lang.*;
import java.io.*;
import java.util.*;
import java.net.*;

/**The text tools for <code>Text Trix</code>.
 * These tools generally manipulate text at the user's will
 * according to chosen patterns.  Eventually the user should be able to 
 * manipulate those patterns to the user's own preferences.
 * This class provides the static methods that both the graphical 
 * <code>Text Trix</code> and its command-line version, <code>Txtrx</code>, 
 * use to tweak text.
 */
public class Tools {

	public Tools() {
	}

    /**Removes extra hard returns.
	 * For example, unformatted email arrives with hard returns inserted after 
	 * every line; this method strips all but the paragraph, double-spaced
	 * hard returns.
	 * Text within <code>&#060;pre&#062;</code>
	 * and <code>&#060;/pre&#062;</code> tags are
	 * left untouched.  Additionally, each line whose first character is a 
	 * dash, asterisk, or tab
	 * gets its own line.  The line above such lines also gets to remain
	 * by itself.  "&#062;" at the start of lines, such as " &#062; &#062; " 
	 * from inline email message replies, are also removed.
	 * @param s the full text from which to strip extra hard returns
	 * @return stripped text
	 */
    public static String removeExtraHardReturns(String s) {
		/* This function works by generally checking the characters afer
		 * a hard return to determine whether to keep it or not.
		 * To strip inline message reply characters, the function must also
		 * check the beginning of the string separately.  Additionally, the
		 * function completely excludes pre-tag-delimited areas from hard
		 * return removal.
		 */
		StringBuffer stripped = new StringBuffer(s.length()); // new string
		int n = 0; // string index
		String searchChars = " >"; // inline message reply chars
		String inlineReplySigns = ">"; // inline message indicators
		boolean isCurrentLineReply = false; // current line part of message reply
		boolean isNextLineReply = false; // next line part of message reply
		boolean ignorePre = false; // ignore <pre>'s within inline replies

		// check for inline reply symbols at start of string
		n = containingSeq(s, n, searchChars, inlineReplySigns);
		if (s.indexOf("<pre>") == 0 || n == 0) {
			isCurrentLineReply = false;
		} else {	
			isCurrentLineReply = true;
			stripped.append("----Original Message----\n\n"); // mark replies
			ignorePre = true;
		}
	
		while (n < s.length()) {
			int inlineReply = 0; // eg ">" or "<" from inline email msg replies
			int nextInlineReply = 0; // inline replies on next line
	    	int singleReturn = s.indexOf("\n", n); // next hard return occurrence
			boolean isDoubleReturn = false; // double hard return flag
		    boolean isDash = false; // dash flag
	    	boolean isAsterisk = false; // asterisk flag
			boolean isNumber = false; // number flag
			boolean isTab = false; // tab flag
	 	    int startPre = s.indexOf("<pre>", n); // next opening pre tag occurrence
		    int endPre = s.indexOf("</pre>", n); // next cloisng pre tag occurrence
			
			// check the character after a hard return
			if (singleReturn != -1) {
				int afterSingRet = singleReturn + 1;
				// get the length of chars inline msg reply chars after the return
				inlineReply = containingSeq(s, afterSingRet, searchChars, inlineReplySigns);
				// if the reply chars contine another hard return, find the length
				// of reply chars after it
				if (s.length() > afterSingRet + inlineReply
						&& s.charAt(afterSingRet + inlineReply) == '\n') {
					isDoubleReturn = true;
					nextInlineReply = containingSeq(s, afterSingRet + inlineReply + 1,
							searchChars, inlineReplySigns);
				}
				// check whether the character after a return is a 
				// tab, dash, asterisk, or number
				int afterInlineReply = singleReturn + inlineReply + 1;
				if (afterInlineReply < s.length()) {
					isTab = s.startsWith("\t", afterInlineReply);
					isDash = s.startsWith("-", afterInlineReply);
					isAsterisk = s.startsWith("*", afterInlineReply);
					String numbers = "1234567890";
					isNumber = (numbers.indexOf(s.charAt(afterInlineReply)) != -1) 
						? true : false;
				}					
			}
			isNextLineReply = (inlineReply != 0 || nextInlineReply != 0) ? true : false;
			
			/* Append the chars to keep while removing single returns
			 * and their inline msg reply chars appropriately.
			 */
			// skip <pre>-delimited sections, removing only the <pre> tags
			// The <pre> tags should each be on its own line.
		    if (startPre == n && !ignorePre
					&& (startPre < s.length() || startPre < singleReturn)) {
				// go to the end of the "pre" section
				if (endPre != -1) {
		    		stripped.append(s.substring(n + 6, endPre));
			    	n = endPre + 7;
			    // if user forgets closing "pre" tag, goes to end
				} else {
		    		stripped.append(s.substring(n + 6));
		    		n = s.length();
				}
			// add the rest of the text if no more single returns exist.
			// Also catches null strings
			// Skips final "--------" for inline replies if no singleReturn after
	    	} else if (singleReturn == -1) {
				stripped.append(s.substring(n));
				/* to add final dashed line after reply, even when no final
				 * return, uncomment these lines
				if (isCurrentReply)
					stripped.append("\n-----------------------\n\n");
				*/
				n = s.length();
			// mark that start of inline message reply
			} else if (!isCurrentLineReply && isNextLineReply) {
				stripped.append(s.substring(n, singleReturn)
						+ "\n\n----Original Message----\n\n");
				n = (isDoubleReturn) ? (singleReturn + inlineReply + 2 + nextInlineReply)
					: (singleReturn + inlineReply + 1);
			// mark that end of inline message reply
			} else if (isCurrentLineReply && !isNextLineReply) {
				stripped.append(s.substring(n, singleReturn)
						+ "\n------------------------\n\n"); // dashed start, so own line
				n = (isDoubleReturn) ? (singleReturn + inlineReply + 2 + nextInlineReply)
					: (singleReturn + inlineReply + 1);
			// preserve double returns
			} else if (isDoubleReturn) {
				stripped.append(s.substring(n, singleReturn) + "\n\n");
				n = singleReturn + inlineReply + 2 + nextInlineReply; // skip over processed rets
			// preserve separate lines for lines starting w/
			// dashes, asterisks, numbers, or tabs, as in lists
		    } else if (isDash || isAsterisk || isNumber || isTab) {
				// + 2 to pick up the dash
				stripped.append(s.substring(n, singleReturn + 1));
				n = singleReturn + inlineReply + 1;
			// join the tail-end of the text
		    } else {
				// don't add space if single return is at beginning of line
				// or a space exists right before the single return.
				if (singleReturn == n || s.charAt(singleReturn - 1) == ' ') {
					stripped.append(s.substring(n, singleReturn));
				// add space if none exists right before the single return
				} else {
					stripped.append(s.substring(n, singleReturn) + " ");
				}
				n = singleReturn + inlineReply + 1;
		    }
		// flag whether the current line is part of a msg reply
		isCurrentLineReply = isNextLineReply;
		// flag to ignore <pre> tags if in inline message reply
		ignorePre = (inlineReply != 0 || nextInlineReply != 0) ? true : false;
		}	
		return stripped.toString();
    }

	/**Finds the first continuous string consisting of any of a given
	 * set of chars and returns the sequence's length if it contains any of 
	 * another given set of chars.
	 * @param seq string to search
	 * @param start <code>seq</code>'s index at which to start searching
	 * @param chars chars for which to search in <code>seq</code>
	 * @param innerChars required chars to return the length of the first
	 * continuous string of chars from <code>chars</code>; if no
	 * <code>innerChars</code> are found, returns 0
	 */
	public static int containingSeq(String seq, int start, 
			String chars, String innerChars) {
		char nextChar = '\0';
		boolean inSeq = false;
		int i = start;
		while (seq.length() > i && chars.indexOf(nextChar = seq.charAt(i)) != -1) {
			i++;
			if (innerChars.indexOf(nextChar) != -1) {
				inSeq = true; // set flag that found a char from innerChar
			}
		}
		return (inSeq) ? i - start : 0;
	}

	/**Replaces HTML tags with their appropriate plain-text substitutes, 
	 * if available.  Otherwise, simply removes the tag.
	 * @param text text from which to remove HTML tags
	 * @return text with HTML tags removed
	 */
	public static String htmlReplacement(String text) {
		String lowerCase = text.toLowerCase();
		int len = text.length();
		StringBuffer s = new StringBuffer(len);
		int n = 0;
		char c;
		while (n < len) {
			if ((c = text.charAt(n)) == '<' && n < len - 1) {
				n++;
				if (lowerCase.startsWith("p>", n) || lowerCase.startsWith("p ", n)) {
					s.append("\n\n");
				} else if (lowerCase.startsWith("br>", n) 
						|| lowerCase.startsWith("br ", n)) {
					s.append("\n");
				} else if (lowerCase.startsWith("b>", n) 
						|| lowerCase.startsWith("b ", n)	
						|| lowerCase.startsWith("/b>", n) 
						|| lowerCase.startsWith("/b ", n)) {
					s.append("*");
				} else if (lowerCase.startsWith("i>", n) 
						|| lowerCase.startsWith("i ", n)
						|| lowerCase.startsWith("/i>", n) 
						|| lowerCase.startsWith("/i ", n)) {
					s.append("/");
				} else if (lowerCase.startsWith("u>", n)
						|| lowerCase.startsWith("u ", n)
						|| lowerCase.startsWith("/u>", n)
						|| lowerCase.startsWith("/u >", n)) {
					s.append("_");
				} else if (lowerCase.startsWith("/", n)) {
				}
				n = lowerCase.indexOf('>', n);
			} else if (c == '\n' || c == '\t') {
			} else {
				s.append(c);
			}
			n++;
		}
		return s.toString();
	}

	
	/**Shows non-printing characters.
	 * Adds String representations for non-printing characters, such as paragraph 
	 * and tab markers.  The representations become part of the text.
	 * @param text text to convert
	 * @return text with added String representations
	 */
	public static String showNonPrintingChars(String text) {
		StringBuffer s = new StringBuffer(text.length());
		char c;
//		String unicodeTable[][] = loadUnicodeArray("unicodetable.txt");
		for (int i = 0; i < text.length(); i++) {
			c = text.charAt(i);
			switch (c) {
				case '\n':
					s.append("\\n" + c);
					break;
				case '\t':
					s.append("\\t" + c);
					break;
				default:
					s.append(c);
					break;
			}
			/*
//			charText = unicodeConversion(unicodeTable, c);
			if (charText != null) {
				s.append(charText);
			} else {
				s.append(c);
			}
			*/
		}
		return s.toString();
	}

	/**Load a table of equivalencies between two strings.
	 * The first column consists of one string, and the second column of the 
	 * correspondingly equivalent strings
	 * Eg, Col. 1, Row 1 might contain "USA", and Col. 2, Row 1 would contain 
	 * "United States of America".
	 * @param path file path to table, with lines of the form: 
	 * <code><i>variable</i> = <i>value</i></code>; 
	 * a space or bar also separates the two variables
	 * @return an array of 2-element arrays, each consisting of a string  
	 * and its equivalent string
	 */
	public static String[][] loadEquivalencyTable(String path) {
		try {
			InputStream in = Tools.class.getResourceAsStream(path);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringTokenizer token;
			String delimiters = "| =";
			String line;
			ArrayList vars = new ArrayList();
			ArrayList vals = new ArrayList();
			for (int i = 0; (line = reader.readLine()) != null; i++) {
				token = new StringTokenizer(line, delimiters);
				vars.add(token.nextToken());
				vals.add(token.nextToken());
			}
			int n = vars.size();
			String equivs[][] = new String[n][2];
			for (int i = 0; i < n; i++) {
				equivs[i][0] = (String)vars.get(i);
				equivs[i][1] = (String)vals.get(i);
			}
			sortEquivalencyTable(equivs);
			return equivs;
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**Sorts a 2-dimensional String array according to the first element
	 * of each array.
	 * @param array[][] an array of 2-element arrays
	 */
	public static void sortEquivalencyTable(String[][] array) {
		int start;
		int end;
		int gap;
		int n = array.length;
		String tmp[];
		for (gap = n / 2; gap > 0; gap /= 2) {
			for (end = gap; end < n; end++) {
				for (start = end - gap; start >= 0 
						&& (array[start][0].compareTo(array[start + gap][0]) > 0); 
						start -= gap) {
					tmp = array[start];
					array[start] = array[start + gap];
					array[start + gap] = tmp;
				}
			}
		}
	}

	/**Displays a string's equivalent string.
	 * @param equivalencyTable[][] array of 2-element arrays, each holding 
	 * a string and its equivalent string
	 * @return equivalent string, the original string if it is not in the table
	 */
	public static String strConverter(String[][] equivalencyTable, String quarry) {
		String s = null;
		int start = 0;
		int end = equivalencyTable.length - 1;
		int mid = end / 2;
		while (start <= end) {
//			System.out.println("start == " + start + ", mid == " + mid + ", end == " + end);
			if ((s = equivalencyTable[mid][0]).equals(quarry)) {
				return equivalencyTable[mid][1];
			} else if (quarry.compareTo(s) < 0) {
				end = mid - 1;
			} else {
				start = mid + 1;
			}
//			System.out.println("c == " + c + ", quarry == " + quarry + ", tableval == " + unicodeTable[mid][0]);
			mid = (start + end) / 2;
		}
		return s;
	}
			

}
