/* Tools.java    
   Text Trix
   the text tinker
   http://textflex.com/texttrix
   
   Copyright (c) 2002-3, Text Flex
   All rights reserved.
   
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions 
   are met:
   
   * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
   * Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
   * Neither the name of the Text Trix nor the names of its
   contributors may be used to endorse or promote products derived
   from this software without specific prior written permission.
   
   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
   IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
   TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
   PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
   OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  
*/

package com.textflex.texttrix;

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
	    BufferedReader reader 
		= new BufferedReader(new InputStreamReader(in));
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
     * @param array an array of 2-element arrays
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
			 && (array[start][0].compareTo(array[start + gap][0]) 
			     > 0); 
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
     * @param equivalencyTable array of 2-element arrays, each holding 
     * a string and its equivalent string
     * @return equivalent string, the original string if it is not in the table
     */
    public static String strConverter(String[][] equivalencyTable, 
				      String quarry) {
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
    public static int find(String text, String quarry, int n, boolean word, 
			   boolean ignoreCase) {
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
	//	System.out.println(text);
	while (n < len) {
	    // skip over non-letters/non-digits
	    while (n < len && !Character.isLetterOrDigit(text.charAt(n)))
		n++;
	    // progress to the end of a word
	    end = n + 1;
	    while (end < len && Character.isLetterOrDigit(text.charAt(end)))
		end++;
	    // compare the word with the quarry to see if they match
	    //	    System.out.println("n: " + n + ", end: " + end);
	    if (end <= len && text.substring(n, end).equals(quarry)) {
		return n;
		// continue search with next word if no match yet
	    } else {
		n = end;
		//		end++;
	    }
	}
	return -1;
    }
	
    /** Find and replace occurences of a given sequence.
	Employs options for specific word searching, replacing all 
	occurrences, wrap around to the text's beginning, and ignoring upper/
	lower case.
	@param text string to search
	@param quarry sequence to find
	@param replacement sequence with which to substitute
	@param start index to start searching
	@param end index at which to no longer begin another search; 
	a search can continue past it, but cannot start once exceeding it
	@param word treat the quarry as a separate word, with only 
	non-letters/non-digits surrounding it
	@param all if true, replace all occurrences of the sequence, starting 
	with the current cursor position and continuing through the text's 
	end, though only wrapping around to the start and back to the cursor 
	if <code>wrap</code> is enabled; 
	if false, replace only the first occurrence
	@param wrap if necessary, continue to the beginning of the text and 
	return to the cursor
	@param ignoreCase ignore upper/lower case
	@return text with appropriate replacements
	@see #findReplace(String, String, String, boolean, boolean,
	boolean, boolean)
     */
    public static String findReplace(String text, String quarry, 
				     String replacement, int start, int end, 
				     boolean word, boolean all, boolean wrap, 
				     boolean ignoreCase) {
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
	    // if not found, and wrap is enabled, continue from text beginning
	    if (quarryLoc == -1 && wrap) {
		text = findReplace(text, quarry, replacement, 0, start - 1, 
				   word, all, false, ignoreCase);
	    }
	    return text;
	}
    }

    /** Front end for finding and replacing occurences of a given sequence.
	@param text string to search
	@param quarry sequence to find
	@param replacement sequence with which to substitute
	@param word treat the quarry as a separate word, with only 
	non-letters/non-digits surrounding it
	@param all if true, replace all occurrences of the sequence, starting 
	with the current cursor position and continuing through the text's 
	end, though only wrapping around to the start and back to the cursor 
	if <code>wrap</code> is enabled; 
	if false, replace only the first occurrence
	@param wrap if necessary, continue to the beginning of the text and 
	return to the cursor
	@param ignoreCase ignore upper/lower case
	@return text with appropriate replacements
	@see #findReplace(String, String, String, int, int, boolean, boolean,
	boolean, boolean)
     */
    public static String findReplace(String text, String quarry, 
				     String replacement, boolean word, 
				     boolean all, boolean wrap, 
				     boolean ignoreCase) {
	return findReplace(text, quarry, replacement, 0, text.length(), word,
			   all, wrap, ignoreCase);
    }

    /** Locate escape characters and replace them with string 
	representations.
	@param text string to manipulate
	@param start index in <code>s</code> at which to start manipulation
	@param end index in <code>s</code> at which to no longer manipulate
	@return text with added string representations
    */	
    public String print(String text, int start, int end) {
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

}
