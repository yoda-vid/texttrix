/* Tools.java - the tool functions for Text Trix
 * Text Trix
 * the text tinker
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
    public static String removeExtraHardReturns(String s, int start, int end) {
	/* This function works by generally checking the characters afer
	 * a hard return to determine whether to keep it or not.
	 * To strip inline message reply characters, the function must also
	 * check the beginning of the string separately.  Additionally, the
	 * function completely excludes pre-tag-delimited areas from hard
	 * return removal.
	 */
	int len = s.length();
	StringBuffer stripped = new StringBuffer(len); // new string
	int n = start; // string index
	String searchChars = " >"; // inline message reply chars
	String inlineReplySigns = ">"; // inline message indicators
	boolean isCurrentLineReply = false; // current line part of message reply
	boolean isNextLineReply = false; // next line part of message reply
	boolean ignorePre = false; // ignore <pre>'s within inline replies

	// append text preceding the selection
	stripped.append(s.substring(0, n));
	// check for inline reply symbols at start of string
	n = containingSeq(s, n, searchChars, inlineReplySigns);
	if (s.indexOf("<pre>") == 0 || n == start) {
	    isCurrentLineReply = false;
	} else {	
	    isCurrentLineReply = true;
	    stripped.append("----Original Message----\n\n"); // mark replies
	    ignorePre = true;
	}
	
	while (n < end) {
	    int inlineReply = 0; // eg ">" or "<" from inline email msg replies
	    int nextInlineReply = 0; // inline replies on next line
	    int singleReturn = s.indexOf("\n", n); // next hard return occurrence
	    boolean isDoubleReturn = false; // double hard return flag
	    boolean isDash = false; // dash flag
	    boolean isAsterisk = false; // asterisk flag
	    boolean isNumber = false; // number flag
	    boolean isLetterList = false; // lettered list flag
	    boolean isTab = false; // tab flag
	    int startPre = s.indexOf("<pre>", n); // next opening pre tag
	    int endPre = s.indexOf("</pre>", n); // next cloisng pre tag
			
	    // check the character after a hard return
	    if (singleReturn != -1) {
		int afterSingRet = singleReturn + 1;
		// get the length of chars inline msg reply chars after 
		// the return
		inlineReply = containingSeq(s, afterSingRet, searchChars, 
					    inlineReplySigns);
		// if the reply chars contine another hard return, 
		// find the length
		// of reply chars after it
		if (s.length() > (afterSingRet += inlineReply)
		    && s.charAt(afterSingRet) == '\n') {
		    isDoubleReturn = true;
		    nextInlineReply = 
			containingSeq(s, afterSingRet + 1,
				      searchChars, inlineReplySigns);
		}
		// check whether the character after a return is a 
		// tab, dash, asterisk, or number
		int afterInlineReply = singleReturn + inlineReply + 1;
		if (afterInlineReply < s.length()) {
		    isTab = s.startsWith("\t", afterInlineReply);
		    isDash = s.startsWith("-", afterInlineReply);
		    isAsterisk = s.startsWith("*", afterInlineReply);
		    String listDelimiters = ").";
		    String numbers = "1234567890";
		    int potentialListPos = 0;
		    for (potentialListPos = afterInlineReply; 
			 potentialListPos < s.length() 
			     && Character.isDigit(s.charAt(potentialListPos));
			 potentialListPos++);
		    isNumber = ((potentialListPos != afterInlineReply) 
				&& listDelimiters
				.indexOf(s.charAt(potentialListPos)) != -1) 
			? true : false;
		    isLetterList = 
			(Character.isLetter(s.charAt(afterInlineReply)) 
			 && (potentialListPos = afterInlineReply + 1) 
			 < s.length()
			 && listDelimiters
			 .indexOf(s.charAt(potentialListPos)) != -1) 
			? true : false;
		}					
	    }
	    isNextLineReply = 
		(inlineReply != 0 || nextInlineReply != 0) ? true : false;
			
	    /* Append the chars to keep while removing single returns
	     * and their inline msg reply chars appropriately.
	     */
	    // skip <pre>-delimited sections, removing only the <pre> tags
	    // The <pre> tags should each be at the start of its own line.
	    if (startPre == n && !ignorePre) {
		// go to the end of the "pre" section
		if (endPre != -1) {
		    stripped.append(s.substring(n + 6, endPre));
		    n = endPre + 7;
		    // if user forgets closing "pre" tag, goes to end
		} else if (n + 6 < end) {
		    stripped.append(s.substring(n + 6, end));
		    n = end;
		} else {
		    n = end;
		}
		// add the rest of the text if no more single returns exist.
		// Also catches null strings
		// Skips final "--------" for inline replies if no singleReturn after
	    } else if (singleReturn == -1) {
		stripped.append(s.substring(n, end));
		/* to add final dashed line after reply, even when no final
		 * return, uncomment these lines
		 if (isCurrentReply)
		 stripped.append("\n-----------------------\n\n");
		*/
		n = end;
		// mark that start of inline message reply
	    } else if (!isCurrentLineReply && isNextLineReply) {
		stripped.append(s.substring(n, singleReturn)
				+ "\n\n----Original Message----\n\n");
		n = (isDoubleReturn) 
		    ? (singleReturn + inlineReply + 2 + nextInlineReply)
		    : (singleReturn + inlineReply + 1);
		// mark that end of inline message reply
	    } else if (isCurrentLineReply && !isNextLineReply) {
		// dashed start, so own line
		stripped.append(s.substring(n, singleReturn)
				+ "\n------------------------\n\n"); 
		n = (isDoubleReturn) 
		    ? (singleReturn + inlineReply + 2 + nextInlineReply)
		    : (singleReturn + inlineReply + 1);
		// preserve double returns
	    } else if (isDoubleReturn) {
		stripped.append(s.substring(n, singleReturn) + "\n\n");
		// skip over processed rets
		n = singleReturn + inlineReply + 2 + nextInlineReply; 
		// preserve separate lines for lines starting w/
		// dashes, asterisks, numbers, or tabs, as in lists
	    } else if (isDash || isAsterisk || isNumber || isLetterList 
		       || isTab) {
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
	    ignorePre = 
		(inlineReply != 0 || nextInlineReply != 0) ? true : false;
	}
	/* n should have never exceeded len
	   String finalText = stripped.toString();
	   if (n < len)
	   finalText += s.substring(n);
	   return finalText;
	*/
	return stripped.toString() + s.substring(n);
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
	char nextChar;
	boolean inSeq = false;
	int i = start;
	while (seq.length() > i 
	       && chars.indexOf(nextChar = seq.charAt(i)) != -1) {
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
    public static String htmlReplacer(String text, int start, int end) {
	String lowerCase = text.toLowerCase(); // so ignore tag upper/lower case
	int len = text.length();
	StringBuffer s = new StringBuffer(len);
	int n = start;
	int orderedListIndex = -1;
	char c = '\0';

	// append text preceding the selection
	// Add to the stringbuffer char by char, deleting and replacing 
	// tags as appropriate
	while (n < end) {
	    // eliminate each line's leading spaces
	    while (n < end && ((c = text.charAt(n)) == ' ' || c == '\t')) 
		n++;
	    while (n < end && (c = text.charAt(n)) != '\n') {
		// tags
		if (c == '<' && n < len - 1) {
		    n++;
		    // <p> tags
		    if (startsWithAny(lowerCase, 
				      new String[] {
					  "p>", "p "
				      }, n)) {
			s.append("\n\n");
			// <br> tags
		    } else if (startsWithAny(lowerCase, 
					     new String[] {
						 "br>", "br>"
					     }, n)) {
			s.append("\n");
			// <b> tags: replace with "*"
		    } else if (startsWithAny(lowerCase, 
					     new String[] {
						 "b>", "b ", "/b>", "/b "
					     }, n)) {
			s.append("*");
			// <i> tags: replace with "/"
		    } else if (startsWithAny(lowerCase, 
					     new String[] {
						 "i>", "i ", "/i>", "/i "
					     }, n)) {
			s.append("/");
			// <u> tags: replace with "_"
		    } else if (startsWithAny(lowerCase, 
					     new String[] {
						 "u>", "u ", "/u>", "/u >"
					     }, n)) {
			s.append("_");
			// <ul> tages: set the list index to -1 for "false"
		    } else if (startsWithAny(lowerCase,
					     new String[] {
						 "ul>", "ul "
					     }, n)) {
			orderedListIndex = -1;
			// <ol> tags: reset the list index to 1
		    } else if (startsWithAny(lowerCase,
					     new String[] {
						 "ol>", "ol "
					     }, n)) {
			orderedListIndex = 1;
			// <li> tags: add "-" for unordered lists, the current index for ordered lists
		    } else if (startsWithAny(lowerCase,
					     new String[] {
						 "li>", "li "
					     }, n)) {
			if (orderedListIndex == -1) {
			    s.append("\n\t-");
			} else {
			    s.append("\n\t" + orderedListIndex++ + ". ");
			}
			// any other closing tag
		    } else if (startsWithAny(lowerCase, 
					     new String[] {"/"}, n)) {
		    }
		    int nToBe = -1;
		    n = ((nToBe = lowerCase.indexOf('>', n)) != -1) 
			? nToBe : end - 1;
		    // unicode characters
		} else if (c == '&' && n < len -1) {
		    n++;
		    if (startsWithAny(lowerCase, 
				      new String[] {
					  "nbsp;", "#161;"
				      }, n)) {
			s.append(" ");
		    } else if (startsWithAny(lowerCase, 
					     new String[] {
						 "#151;"
					     }, n)) {
			s.append("--");
		    } else if (startsWithAny(lowerCase, 
					     new String[] {
						 "copy;", "#169;"
					     }, n)) {
			s.append("(c)");
		    } else if (startsWithAny(lowerCase, 
					     new String[] {
						 "reg;", "#174;"
					     }, n)) {
			s.append("(R)");
		    } else if (startsWithAny(lowerCase, 
					     new String[] {
						 "#153;"
					     }, n)) {
			s.append("TM");
		    }
		    int nToBe = -1;
		    // end - 1 so that n won't ever exceed end; n++ later
		    n = ((nToBe = lowerCase.indexOf(';', n)) != -1) 
			? nToBe : end -1 ;
		    // skip over hard returns and tabs
		} else if (c == '\n' || c == '\t') {
		    // add non-tag, non hard return/tab chars
		} else {
		    s.append(c);
		}
		n++;
	    }
	    // ensures that end on next character
	    if (n < end)
		n++;
	}

	// wipe out multiple spaces within the tagless text
	String taglessText = s.toString();
	int taglessLen = taglessText.length();
	s = new StringBuffer(taglessLen);
	for (int i = 0; i < taglessLen; i++) {
	    s.append(c = taglessText.charAt(i));
	    if (c == ' ') {
		while (i < taglessLen - 1 
		       && (c = taglessText.charAt(i + 1)) == ' ')
		    i++;
	    }
	}
	// stay within original text length; n shouldn't have exceeded end or len, though
	String finalText = text.substring(0, start) + s.toString();
	if (n < len) 
	    finalText += text.substring(n);
	return finalText;
	/*
	  System.out.println("start: " + start + ", end: " + end + ", n: " + n);
	  return text.substring(0, start) + s.toString() + text.substring(n);
	*/
    }

    /**Checks whether any of the strings in an array are at the start
     * of another string.
     * @param s string to check
     * @param strs[] array of strings that may be at the beginning 
     * of <code>s</code>
     * @param offset index to start checking in <code>s</code>
     * @return <code>true</code> if any of the array's strings 
     * start <code>s</code>, 
     * <code>false</code> if otherwise
     */
    public static boolean startsWithAny(String s, String strs[], int offset) {
	for (int i = 0; i < strs.length; i++) {
	    //			System.out.println("starts' i: " + i);
	    if (s.startsWith(strs[i], offset)) {
		return true;
	    }
	}
	return false;
    }
	
    /**Shows non-printing characters.
     * Adds String representations for non-printing characters, such as 
     * paragraph and tab markers.  The representations become part of the text.
     * @param text text to convert
     * @return text with added String representations
     */
    public static String showNonPrintingChars(String text, int start, int end) {
	int len = text.length();
	StringBuffer s = new StringBuffer(len);
	char c;

	// append text preceding the selection
	s.append(text.substring(0, start));
	// progress char by char, revealing newlines and tabs explicitly
	for (int i = start; i < end; i++) {
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
	}
	return s.toString() + text.substring(end);
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
	    ArrayList vars = new ArrayList(); // variable-like string
	    ArrayList vals = new ArrayList(); // value-like string: var equivs
	    // separately retrieve each string and its equivalent, with 
	    // "|", " ", "=", or a combo of them delineating the two strings
	    for (int i = 0; (line = reader.readLine()) != null; i++) {
		token = new StringTokenizer(line, delimiters);
		vars.add(token.nextToken());
		vals.add(token.nextToken());
	    }
	    int n = vars.size();
	    // storage array based on number of equivalencies
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
     * Works according to the Shell sort algorithm.
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
     * Works according to the Binary search algorithm.
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

    /**Find a the first occurrence of a given sequence in a string.
     * @param text string to search
     * @param quarry sequence to find
     * @param start index to start searching
     * @return index of the sequence's start in the string; -1 if not found
     */
    public static int find(String text, String quarry, int start) {
	if (start < text.length())
	    return text.indexOf(quarry, start);
	return -1;
    }
	
    /**Front-end to the <code>find</code> and <code>findWord</code> methods.
     * Depending on the options given to it, checks for either a 
     * any occurrence of a given sequence in a string or only when 
     * the string occurs as a separate word, ie with only non-letter or 
     * non-digits surrounding it.  Also can choose to ignore upper/lower 
     * case.
     * @param text string to search
     * @param quarry sequence to search for
     * @param n index to start searching
     * @param word if true, treat the sequence as a separate word, with only
     * non-letters/non-digits surrounding it
     * @param ignoreCase if true, ignore upper/lower case
     * @return index of sequence's start in the string; -1 if not found
     */
    public static int find(String text, String quarry, int n, boolean word, boolean ignoreCase) {
	if (ignoreCase) {
	    text = text.toLowerCase();
	    quarry = quarry.toLowerCase();
	}
	return word ? findWord(text, quarry, n) : find(text, quarry, n);
    }

    /**Find a given expression as a separate word.
     * Searches through text to find the given expression so long 
     * as it is surrounded by non-letter, non-digit characters, such 
     * as spaces, dashes, or quotation marks.
     * @param text text to search
     * @param quarry word to find; can contain letter and/or numbers
     * @param start index at which to start searching
     * @return int starting index of matching expression; -1 if not found
     */
    public static int findWord(String text, String quarry, int start) {
	int n = start;
	int end = start + 1;
	int len = text.length();
	while (n < len) {
	    // skip over non-letters/non-digits
	    while (!Character.isLetterOrDigit(text.charAt(n)))
		n++;
	    // progress to the end of a word
	    while (end < len && Character.isLetterOrDigit(text.charAt(end)))
		end++;
	    // compare the word with the quarry to see if they match
	    if (end <= len && text.substring(n, end).equals(quarry)) {
		return n;
		// continue search with next word if no match yet
	    } else {
		n = end;
		end++;
	    }
	}
	return -1;
    }
	
    /**Find and replace occurences of a given sequence.
     * Employs options for specific word searching, replacing all 
     * occurrences, wrap around to the text's beginning, and ignoring upper/
     * lower case.
     * @param text string to search
     * @param quarry sequence to find
     * @param replacement sequence with which to substitute
     * @param start index to start searching
     * @param end index at which to no longer begin another search; 
     * a search can continue past it, but cannot start once exceeding it
     * @param word treat the quarry as a separate word, with only 
     * non-letters/non-digits surrounding it
     * @param all if true, replace all occurrences of the sequence, starting 
     * with the current cursor position and continuing through the text's end, 
     * though only wrapping around to the start and back to the cursor if 
     * <code>wrap</code> is enabled; 
     * if false, replace only the first occurrence
     * @param wrap if necessary, continue to the beginning of the text and 
     * return to the cursor
     * @param ignoreCase ignore upper/lower case
     * @return text with appropriate replacements
     */
    public static String findReplace(String text, String quarry, 
				     String replacement, int start, int end, boolean word, boolean all, boolean wrap, boolean ignoreCase) {
	StringBuffer s = new StringBuffer(text.length());
	int n = start;
	int prev;
	// replace all occurrences of the quarry
	if (all) {
	    // append unmodified the text preceding the caret
	    s.append(text.substring(0, n));
	    // continue until the reaching text's end or the quarry has
	    // not been found
	    while (n < end && n != -1) {
		prev = n;
		n = find(text, quarry, n, word, ignoreCase);
		// replace the quarry if found
		if (n != -1) {
		    s.append(text.substring(prev, n) + replacement);
		    n += quarry.length();
		    // if not found, append the rest of the text unmodified
		} else {
		    s.append(text.substring(prev));
		    text = s.toString();
		}
	    }
	    // append the rest of the text if a quarry occurrence extended 
	    // beyond the search boundary
	    if (n != -1) {
		s.append(text.substring(n));
		text = s.toString();
	    }
	    // recursively call the method on the beginning portion of the 
	    // text if wrap is enabled
	    if (wrap) {
		text = findReplace(text, quarry, replacement, 0, start - 1,
				   word, all, false, ignoreCase);
	    }
	    return text;
	    // replace first occurrence only
	} else {
	    int quarryLoc = -1;
	    // stay within given boundary
	    if (n < end) {
		quarryLoc = find(text, quarry, n, word, ignoreCase);
		// replace quarry if found
		if (quarryLoc!= -1) {
		    text = (text.substring(0, quarryLoc) + replacement
			    + text.substring(quarryLoc + quarry.length()));
		}
	    }
	    // if not found and wrap is enabled, continue from beginning of text
	    if (quarryLoc == -1 && wrap) {
		text = findReplace(text, quarry, replacement, 0, start - 1, word, all, false, ignoreCase);
	    }
	    return text;
	}
    }
}
