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
 * Portions created by the Initial Developer are Copyright (C) 2003
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

/** Stores the outcome from a <code>PlugIn</code> event.
 * Encapsulates the text, selection region, and flags for how the plug-in's
 * calling function should handle the outcome.
 * @author davit
 */
public class PlugInOutcome {
	private String text = null; // resulting text
	private int selectionStart = -1; // first char of region to highlight
	private int selectionEnd = -1; // first char of region to no longer highlight
	private boolean noTextChange = false; // flag to not update the text

	/** Creates an instance of the class.
	 * 
	 *
	 */
	public PlugInOutcome() {
	}
	
	/** Creates an instance of the class.
	 * 
	 * @param aText the resulting text
	 */
	public PlugInOutcome(String aText) {
		text = aText;
	}

	/** Creates an instance of the class.
	 * 
	 * @param aText the resulting text
	 * @param aSelectionStart first char of region to highlight; <code>-1</code>
	 * indicates that the region should not be highlighted
	 * @param aSelectionEnd first char of region to no longer highlight
	 */
	public PlugInOutcome(
		String aText,
		int aSelectionStart,
		int aSelectionEnd) {
		this(aText);
		selectionStart = aSelectionStart;
		selectionEnd = aSelectionEnd;
	}

	/** Creates an instance of the class.
	 * 
	 * @param aText the resulting text
	 * @param aSelectionStart first char of region to highlight; <code>-1</code>
	 * indicates that the region should not be highlighted
	 * @param aSelectionEnd first char of region to no longer highlight
	 * @param aNoTextChange flag to not update the text
	 */
	public PlugInOutcome(
		String aText,
		int aSelectionStart,
		int aSelectionEnd,
		boolean aNoTextChange) {
		this(aText, aSelectionStart, aSelectionEnd);
		noTextChange = aNoTextChange;
	}





	/** Sets the resulting text.
	 * 
	 * @param aText text returning to the calling function
	 */
	public void setText(String aText) {
		text = aText;
	}
	/** Sets the beginning of the selection region.
	 * 
	 * @param aSelectionStart first char of region to highlight; <code>-1</code>
	 * indicates that the region should not be highlighted
	 */
	public void setSelectionStart(int aSelectionStart) {
		selectionStart = aSelectionStart;
	}
	/** Sets the end of the selection region.
	 * 
	 * @param aSelectionEnd first char of region to no longer highlight
	 */
	public void setSelectionEnd(int aSelectionEnd) {
		selectionEnd = aSelectionEnd;
	}
	/** Sets the flag that indicates whether the calling function should update
	 * its text with the outcome object's resulting text.
	 * @param aNoTextChange <code>true</code> if the calling function should
	 * not update its text; <code>false</code> by default
	 */
	public void setNoTextChange(boolean aNoTextChange) {
		noTextChange = aNoTextChange;
	}



	/** Gets the resulting text.
	 * 
	 * @return the text with which the calling function should update its own text
	 */
	public String getText() {
		return text;
	}
	/** Gets the position of the first character to highlight.
	 * 
	 * @return position of first char of region to select; <code>-1</code>
	 * flags that the calling function should not select any text
	 */
	public int getSelectionStart() {
		return selectionStart;
	}
	/** Gets the position of the first character to no longer highlight.
	 * Only useful when <code>selectionStart != -1</code>
	 * @return
	 */
	public int getSelectionEnd() {
		return selectionEnd;
	}
	/** Gets the flag indicating whether the calling function should update
	 * its text.
	 * @return <code>true</code> if the calling function should not update
	 * its text; <code>false</code> by default
	 */
	public boolean getNoTextChange() {
		return noTextChange;
	}

}
