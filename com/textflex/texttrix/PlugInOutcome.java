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

public class PlugInOutcome {
	private String text = null;
	private int selectionStart = -1;
	private int selectionEnd = -1;
	private boolean noTextChange = false;
	//    private int caretPosition = -1;

	public PlugInOutcome() {
	}

	public PlugInOutcome(String aText) {
		text = aText;
	}

	public PlugInOutcome(
		String aText,
		int aSelectionStart,
		int aSelectionEnd) {
		this(aText);
		//	caretPosition = aCaretPosition; 
		selectionStart = aSelectionStart;
		selectionEnd = aSelectionEnd;
	}
	
	public PlugInOutcome(
		String aText,
		int aSelectionStart,
		int aSelectionEnd,
		boolean aNoTextChange) {
		this(aText, aSelectionStart, aSelectionEnd);
		noTextChange = aNoTextChange;
	}

	//    public void setCaretPosition(int aCaretPosition) { caretPosition = aCaretPosition; }
	public void setText(String aText) {
		text = aText;
	}
	public void setSelectionStart(int aSelectionStart) {
		selectionStart = aSelectionStart;
	}
	public void setSelectionEnd(int aSelectionEnd) {
		selectionEnd = aSelectionEnd;
	}
	
	public void setNoTextChange(boolean aNoTextChange) {
		noTextChange = aNoTextChange;
	}

	//    public int getCaretPosition() { return caretPosition; }
	public String getText() {
		return text;
	}
	public int getSelectionStart() {
		return selectionStart;
	}
	public int getSelectionEnd() {
		return selectionEnd;
	}
	public boolean getNoTextChange() {
		return noTextChange;
	}

}
