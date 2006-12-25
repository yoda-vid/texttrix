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
 * Portions created by the Initial Developer are Copyright (C) 2006-7
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): David Young <david@textflex.com>
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

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/** Filters user input into a text component to make the input
 * more useful when searching through text.
 * Originally based on 
 * <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/generaltext.html#filter">code
 * from the Sun Java Tutorial</a>.
 * 
 */
public class DocumentSearchFilter extends DocumentFilter {
	int maxChars = 1000; // max characters allowed as input
	
	/** Creates a new filter with the given limit on search characters.
	 * @param aMaxChars the maximum number of characters allowed
	 * in the input field document
	 */
	public DocumentSearchFilter(int aMaxChars) {
		maxChars = aMaxChars;
	}
	
	/** Overrides the insertString method to check that the number of input
	 * characters is less than or equal to the maximum allowed characters.
	 * If not, a beep will sound.
	 * @param fb the filter bypass
	 * @param offset the offset in which to add the text
	 * @param s the string being input
	 * @param attr the input attribute set
	 */
	public void insertString(FilterBypass fb, int offset, String s, AttributeSet attr) 
		throws BadLocationException {
		
		// checks the length of input, adding the original length and
		// the length of the new input
		if ((fb.getDocument().getLength() + s.length()) <= maxChars) {
			// if within the limit, calls the original method
			super.insertString(fb, offset, s, attr);
		} else {
			// if not, sounds a beep
			Toolkit.getDefaultToolkit().beep();
		}
	}
	
	/** Overrides the replace method to check that the number of input
	 * characters is less than or equal to the maximum allowed characters,
	 * when text is being added in place of other text, such as over
	 * selected text.
	 * If the maximal limit is exceeded, a beep will sound.
	 * @param fb the filter bypass
	 * @param offset the offset in which to add the text
	 * @param len the length of text that will be replaced
	 * @param s the string being input
	 * @param attr the input attribute set
	 */
	public void replace(FilterBypass fb, int offset, int len, String s, AttributeSet attr)
		throws BadLocationException {
		
		// checks the length of input, adding the length of the new input
		// and the original text, minus the amound of text being replaced
		if ((fb.getDocument().getLength() + s.length() - len) <= maxChars) {
			// if within the limit, calls the original method
			super.replace(fb, offset, len, s, attr);
		} else {
			// if not, sounds a beep
			Toolkit.getDefaultToolkit().beep();
		}
	}
}