/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is the Text Trix code.
 *
 * The Initial Developer of the Original Code is
 * Text Flex.
 * Portions created by the Initial Developer are Copyright (C) 2002-3
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): David Young <dvd@textflex.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */



package com.textflex.texttrix;

//import java.lang.*;
import java.io.*;
import java.util.*;
//import java.net.*;

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
