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
		boolean isCurrentInlineReply = false; // current line part of message reply
//		System.out.println(s.length() + "");

		// check for inline reply symbols at start of string
		n = containingSeq(s, n, searchChars, inlineReplySigns);
		isCurrentInlineReply = (n != 0) ? true : false;
	
		while (n < s.length()) {
//			System.out.println(n + "");
			int inlineReply = 0; // eg ">" or "<" from inline email msg replies
			int nextInlineReply = 0; // inline replies on next line
	    	int singleReturn = s.indexOf("\n", n); // next hard return occurrence
			boolean isDoubleReturn = false; // double hard return flag
//		    int nextReturn = -1;
		    int dash = s.indexOf("-", n); // next dash occurrence 
	    	int asterisk = s.indexOf("*", n); // next asterisk occurrence
			int tab = -1; // next tab occurence
//			int spaces = 0;
	 	    int startPre = s.indexOf("<pre>", n); // next opening pre tag occurrence
		    int endPre = s.indexOf("</pre>", n); // next cloisng pre tag occurrence

			/*
			boolean isInlineReply = false;
			while (s.length() > i
					&& searchChars.indexOf(nextChar = s.charAt(i)) != -1) {
				i++;
				if (inlineReplySigns.indexOf(nextChar) != -1) {
					isInlineReply = true;
				}
			}
			if (isInlineReply) 
				inlineReply = i - n;
			*/
			
			// check the character after a hard return
			if (singleReturn != -1) {
				int afterSingRet = singleReturn + 1;
//				nextReturn = s.indexOf('\n', afterSingRet);
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
//				System.out.println("inlineReply is " + inlineReply);
//				System.out.println("isCurrentReply is " + isCurrentInlineReply);
				/*
//				searchChars = " <>";
				i = singleReturn + 1;
				boolean isNextInlineReply = false;
				while (s.length() > i
						&& searchChars.indexOf(nextChar = s.charAt(i)) != -1) {
					i++;
					if (inlineReplySigns.indexOf(nextChar) != -1) {
						isNextInlineReply = true;
					}
				}
				if (isNextInlineReply) 
					nextInlineReply = i - singleReturn - 1;
				*/
				tab = s.indexOf("\t", singleReturn + 1);
				/*
				int afterSingleReturnNextInlineReply
				if (s.length() > afterSingleReturnNextInlineReply
						&& s.charAt(afterSingleReturnNextInlineReply) == '\n') {
					nextReturn = afterSingleReturnNextInlineReply;
				}
				*/
			}
						

			/*
			if (inlineReply == 0 && singleReturn != -1) {
				tab = s.indexOf("\t", n + singleReturn + 1);
				int afterSingleReturn = singleReturn + 1;
				// check whether will exceed length of text
				// add one to spaces if next leading character has a space.
				while (s.length() > afterSingleReturn && 
						s.charAt(afterSingleReturn++) == ' ') {
			    	spaces++;
				}
			}
			*/
	
		    // only catch dashes and asterisks after newline
	    	while (dash != -1 && dash < singleReturn) {
				dash = s.indexOf("-", n + dash + 1);
		    }
	    	while (asterisk != -1 && asterisk < singleReturn) {
				asterisk = s.indexOf("*", n + asterisk + 1);
		    }
	
		    // find all leading spaces after a single return

//			System.out.println("Past dashes, asterisks, and leading spaces");

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
	    	} else if (singleReturn == -1) {
//				System.out.println("I'm here.");
				stripped.append(s.substring(n));
				n = s.length();
			// preserve doubly-returned lines, as between paragraphs
//	    	} else if (nextReturn != -1 
//					&& singleReturn == nextReturn - 1 - inlineReply) {
			} else if (isDoubleReturn) {
				stripped.append(s.substring(n, singleReturn) + "\n\n");
				n = singleReturn + inlineReply + 2 + nextInlineReply; // skip over processed rets
			// may eventually show explicity that "Original Message"
			} else if (!isCurrentInlineReply && inlineReply != 0) {
				stripped.append(s.substring(n, singleReturn) + "\n\n----\n\n");
				n = singleReturn + inlineReply + 1;
				System.out.println("I'm here");
			} else if (singleReturn != -1 && isCurrentInlineReply 
						&& inlineReply == 0 && singleReturn != s.length()) {
				stripped.append(s.substring(n, singleReturn) + "\n\n----\n\n");
				n = singleReturn + inlineReply + 1;
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
		isCurrentInlineReply = (inlineReply != 0 || nextInlineReply != 0) ? true : false;
		}	
		
		/*
		return stripped;
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

			// skip <pre>-delimited sections, removing only the <pre> tags
		    if (startPre != -1 && 
					(startPre < s.length() || startPre < singleReturn)) {
				// go to the end of the "pre" section
				if (endPre != -1) {
		    		stripped = stripped 
						+ s.substring(0, startPre) 
						+ s.substring(startPre + 5, endPre);
			    	s = s.substring(endPre + 6);
			    // if user forgets closing "pre" tag, goes to end
				} else {
		    		stripped = stripped 
						+ s.substring(0, startPre) 
						+ s.substring(startPre + 5);
		    		s = "";
				}
			// add the rest of the text if no more single returns exist.
			// Also catches null strings
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
				// don't add space if single return is at beginning of line
				// or a space exists right before the single return.
				if (singleReturn == 0 || s.charAt(singleReturn - 1) == ' ') {
					stripped = stripped + s.substring(0, singleReturn);
				// add space if none exists right before the single return
				} else {
					stripped = stripped + s.substring(0, singleReturn) + " ";
				}
				s = s.substring(singleReturn + 1);
		    }
		}
		*/
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
