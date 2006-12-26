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
import java.awt.*;

/** A tabbed pane that can contain other tabbed panes (hence "Mother")
 * or other objects and which keeps a history a selected tabs for 
 * easier navigation among tabs.
 */
public class MotherTabbedPane extends JTabbedPane {
//	private ArrayList arrayList = new ArrayList();
	private int[] tabHistory = new int[10]; // records for back/forward
	private int tabHistoryIndex = 0; // index of next record
//	private int tabHistoryTot = 0; // total num of tab history entries
	
	/** Constructs a Mother tabbed pane and initializes a tab history.
	 * @param tabPlacement the JTabbedPane placement setting
	 * @param tabLayoutPolicy the JTabbedPane layout setting
	 */
	public MotherTabbedPane(int tabPlacement, int tabLayoutPolicy) {
		super(tabPlacement, tabLayoutPolicy);
		for (int i = 0; i < tabHistory.length; i++)
			tabHistory[i] = -1;
	}
	
	/** Constructs a Mother tabbed pane with the wrap-tab layout,
	 * and initializes a tab history.
	 * @param tabPlacement the JTabbedPane placement setting
	 */
	public MotherTabbedPane(int tabPlacement) {
		this(tabPlacement, JTabbedPane.WRAP_TAB_LAYOUT);
	}
	
	/** Adds a tab.
	 * @param title a title to display in the tab
	 * @param component the component to place in the tab
	 */
	public void addTab(String title, Component component) {
		super.addTab(title, component);
	}
	
	/**
	 * Adds a tab selection to the index record. Creates a trail of tab
	 * selections that the user can progress through forward or backward,
	 * similar to "Back" and "Forward" buttons in a web browser. With each
	 * addition, the record shifts any potential duplicate selections to the
	 * head so that the user will only progress once through a given tab during
	 * a complete cycle in one direction. The history expands as necessary,
	 * allowing an unlimited number of records.
	 * 
	 * <p>The current tabHistoryIndex value refers to the next available
	 * element in the tabHistory array in which to add tab selections.
	 * Adding a tab selection places the tab index value into this element
	 * and increments tabHistoryIndex so that it continues to point to
	 * the next available position. "tabHistoryIndex - 1" refers to
	 * the current tab, while "tabHistoryIndex - 2" refers to the last
	 * tab, the one to return to while going "Back". The "Back" method must
	 * therefore not only decrement tabHistoryIndex, but also refer to
	 * the position preceding the new index.
	*
	 * @param mostRecent
	 *            the tab selection index to record
	 * @see #removeTabHistory(int)
	 */
	public void addTabHistory(int mostRecent) {
		boolean repeat = true;
		boolean shift = false;
//		System.out.println("tabhxidx (at start): " + tabHistoryIndex);
		if (tabHistoryIndex >= tabHistory.length) {
			// increase the array size if necessary
			System.out.println("growing array");
			tabHistory = (int[]) LibTTx.growArray(tabHistory);
		}
		for (int i = 0; i < tabHistoryIndex && repeat; i++) {
			// shift the records as necessary to move a potential
			// duplicate to the front of the history
			if (shift) { // shift the records
				if (tabHistory[i] == -1) {
					repeat = false;
				} else {
					tabHistory[i - 1] = tabHistory[i];
				}
			} else { // find where to start shifting, if necessary
				if (tabHistory[i] == mostRecent) {
//					System.out.println("shift");
					shift = true;
				} else if (tabHistory[i] == -1) {
					repeat = false;
				}
			}
		}
		// add the tab selection
		if (shift) { // add the potential duplicate to the front of the record
			tabHistory[--tabHistoryIndex] = mostRecent;
		} else if (tabHistoryIndex >= 0) {
			// ensure that the tab during TextTrix's startup has no entry;
			// otherwise, the 0 tab selection index duplicates
			tabHistory[tabHistoryIndex] = mostRecent;
//			tabHistoryTot++;
		}
		for (int i = ++tabHistoryIndex; i < tabHistory.length; i++) {
			tabHistory[i] = -1;
		}
/*		
		System.out.print("tab Hx: ");
		for (int i = 0; i < tabHistory.length; i++) {
			 System.out.print(tabHistory[i] + ", ");
		}
		System.out.println("");
*/		
	}

	/**
	 * Removes an entry from the tab selection history. Shifts the tab indices
	 * as necessary.
	 * 
	 * @param removed
	 *            index of removed tab
	 * @see #addTabHistory(int)
	 */
	public void removeTabHistory(int removed) {
		boolean shift = false;
		// cycle through the entire history, removing the tab and shifting
		// both the tab indices and the tab history index appropriately
		for (int i = 0; i < tabHistory.length; i++) {
			// flag for a record shift and check whether to decrement
			// the tab history index
			if (tabHistory[i] == removed) {
//				tabHistoryTot--;
				if (i <= tabHistoryIndex)
					--tabHistoryIndex;
				shift = true;
			}
			// shift the tab record
			if (shift) {
				tabHistory[i] = (i < tabHistory.length - 1) ? tabHistory[i + 1]
						: -1;
			}
			// decrease tab indices for those above the that of the removed tab
			if (tabHistory[i] > removed) {
				tabHistory[i] = --tabHistory[i];
			}
//	    System.out.print(tabHistory[i] + ",");
		}
	}
	
	/** Shifts the tab history backward by one and returns the 
	 * recorded index.
	 */
	public int goBackward() {
//		System.out.println("tabhxidx (at start): " + tabHistoryIndex);
		// decrements the history index
		decrementTabHistoryIndex();
		// refers to the index prior to the decremented one since that
		// index points to the currently selected tab
		return getTabHistoryAt(getTabHistoryIndex() - 1);
	}
	
	/** Shifts the tab history forward by one and returns the 
	 * recorded index.
	 */
	public int goForward() {
//		System.out.println("tabhxidx (at start): " + tabHistoryIndex);
		// increments the history index
		incrementTabHistoryIndex();
		// the current index pointed to the next history entry, so
		// the incremented one points to the next, next entry;
		// the index prior to the incremented should be accessed
		// to get the next entry
		return getTabHistoryAt(getTabHistoryIndex() - 1);
	}
	
	/** Increments the tab index history by one, so long
	 * as the current index is less than the number of tabs,
	 * and the history entry at the resulting index is not -1.
	 * @see #getTabCount
	 */
	public void incrementTabHistoryIndex() {
		if (tabHistoryIndex < getTabCount()
			&& getTabHistoryAt(tabHistoryIndex) != -1)	{
			tabHistoryIndex++;
		}
	}
	
	/** Decrements the tab index history by one, so long
	 * as the resulting index is >= 0.
	 */
	public void decrementTabHistoryIndex() {
		if (tabHistoryIndex > 1)	tabHistoryIndex--;
	}
	
	/** Gets the tab index history.
	 * @return the index
	 */
	public int getTabHistoryIndex() {
		return tabHistoryIndex;
	}
	
	/** Gets the tab index history total count.
	 * @return the length of the index history array
	 */
	public int getTabHistoryCount() {
		return tabHistory.length;
	}
	
	/** Gets the history value at the given index.
	 * @return the history value
	 */
	public int getTabHistoryAt(int i) {
		if (i >= 0 && i < tabHistory.length) {
			return tabHistory[i];
		} else {
			return -1;
		}
	}
}