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
 * Portions created by the Initial Developer are Copyright (C) 2004
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

/**Stores the text and its on-screen formatting for copying to printer paper.
 * By creating an array of these objects, one <code>PrintPadText</code>
 * object per visible, displayed line of text, the contents of the 
 * <code>TextPad</code> can be reproduced according to their layout
 * on the screen, rather than compressed or expanded according to paper
 * size.
*/
public class PrintPadText {
	
	String text = ""; // the line of text
	float indent = 0; // horizontal, left-side indent
	
	/**Creates an object from the text and its left-side indentation.
	*/
	public PrintPadText(String aText, float aIndent) {
		text = aText;
		indent = aIndent;
	}
	
	/**Gets the text.
	 * @return text
	*/
	public String getText() { return text; }
	/**Gets the left-side indent.
	 * @return left-side indent
	*/
	public float getIndent() { return indent; }
	
}