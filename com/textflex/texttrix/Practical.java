/* Practice.java - the practical functions for Text Trix
 * Text Trix
 * a goofy gui editor
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

/**The practical text tools for <code>Text Trix</code>.
 * These tools generally manipulate text at the user's will
 * according to chosen patterns.  Eventually the user should be able to 
 * manipulate those patterns to the user's own preferences.
 * This class provides the static methods that both the graphical 
 * <code>Text Trix</code> and its command-line version, <code>Txtrx</code>, 
 * use to tweak text.
 */
public class Practical {

	public Practical() {
	}

    /**Removes extra hard returns.
	 * For example, unformatted email arrives with hard returns inserted after 
	 * every line; this method strips all but the paragraph, double-spaced
	 * hard returns.
	 * Text within <code>&#60pre&#62</code>
	 * and <code>&#60/pre&#62</code> tags are
	 * left untouched.  Additionally, each line whose first character is a 
	 * dash, asterisk, or tab
	 * gets its own line.  The line above such lines also gets to remain
	 * by itself.  " &#62" at the start of lines, such as from inline
	 * email message replies, are also removed.
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
		String searchChars = " <>"; // inline message reply chars
		String inlineReplySigns = "<>"; // inline message indicators
		boolean isCurrentLineReply = false; // current line part of message reply
		boolean isNextLineReply = false; // next line part of message reply

		// check for inline reply symbols at start of string
		n = containingSeq(s, n, searchChars, inlineReplySigns);
		if (n != 0) {
			isCurrentLineReply = true;
			stripped.append("----Original Message----\n\n");
		} else {
			isCurrentLineReply = false;
		}
	
		while (n < s.length()) {
			int inlineReply = 0; // eg ">" or "<" from inline email msg replies
			int nextInlineReply = 0; // inline replies on next line
	    	int singleReturn = s.indexOf("\n", n); // next hard return occurrence
			boolean isDoubleReturn = false; // double hard return flag
		    int dash = -1; // next dash occurrence 
	    	int asterisk = -1; // next asterisk occurrence
			int tab = -1; // next tab occurence
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
				tab = s.indexOf("\t", singleReturn + 1);
				dash = s.indexOf("-", singleReturn + 1);
				asterisk = s.indexOf("*", singleReturn + 1);
			}
			isNextLineReply = (inlineReply != 0 || nextInlineReply != 0) ? true : false;
			
			/* Append the chars to keep while removing single returns
			 * and their inline msg reply chars appropriately.
			 */
			// skip <pre>-delimited sections, removing only the <pre> tags
		    if (startPre != -1 && 
					(startPre < s.length() || startPre < singleReturn)) {
				// go to the end of the "pre" section
				if (endPre != -1) {
		    		stripped.append(s.substring(n, startPre) 
							+ s.substring(startPre + 5, endPre));
			    	n = endPre + 6;
			    // if user forgets closing "pre" tag, goes to end
				} else {
		    		stripped.append(s.substring(n, startPre) 
							+ s.substring(startPre + 5));
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
			// may eventually show explicity that "Original Message": by flagging prev line?
			} else if (!isCurrentLineReply && isNextLineReply) {
				stripped.append(s.substring(n, singleReturn)
						+ "\n\n----Original Message----\n\n");
				n = (isDoubleReturn) ? (singleReturn + inlineReply + 2 + nextInlineReply)
					: (singleReturn + inlineReply + 1);
			} else if (isCurrentLineReply && !isNextLineReply) {
				stripped.append(s.substring(n, singleReturn)
						+ "\n------------------------\n\n"); // dashed start, so own line
				n = (isDoubleReturn) ? (singleReturn + inlineReply + 2 + nextInlineReply)
					: (singleReturn + inlineReply + 1);
			} else if (isDoubleReturn) {
				stripped.append(s.substring(n, singleReturn) + "\n\n");
				n = singleReturn + inlineReply + 2 + nextInlineReply; // skip over processed rets
			// preserve separate lines for lines starting w/
			// dashes or asterisks or spaces before them
		    } else if (dash == singleReturn + 1 + inlineReply
			    	   || asterisk == singleReturn + 1 + inlineReply) {
				// + 2 to pick up the dash
				stripped.append(s.substring(n, singleReturn + 2));
				n = singleReturn + inlineReply + 2;
			// preserve separate lines for ones starting with tabs
			} else if (tab == singleReturn + 1 + inlineReply) {
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
	 * Function not yet functional.
	 * @param aText text from which to remove HTML tags
	 */
	public static String replaceHTMLTags(String aText) {
		String text = aText;
		return text;
	}
}
