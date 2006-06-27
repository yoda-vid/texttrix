
package com.textflex.texttrix;

import javax.swing.*;
import java.awt.*;

public class MotherTabbedPane extends JTabbedPane {
//	private ArrayList arrayList = new ArrayList();
	private int[] tabIndexHistory = new int[10]; // records for back/forward
	private int tabIndexHistoryIndex = 0; // index of next record
	
	public MotherTabbedPane(int tabPlacement, int tabLayoutPolicy) {
		super(tabPlacement, tabLayoutPolicy);
		for (int i = 0; i < tabIndexHistory.length; i++)
			tabIndexHistory[i] = -1;
		
		
	}
	
	public MotherTabbedPane(int tabPlacement) {
		this(tabPlacement, JTabbedPane.WRAP_TAB_LAYOUT);
	}
	
	public void addTab(String title, Component component) {
		super.addTab(title, component);
//		arrayList.add(component);
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
	 * @param mostRecent
	 *            the tab selection index to record
	 * @see #removeTabIndexHistory(int)
	 */
	public void addTabIndexHistory(int mostRecent) {
		/*
		 * The current tabIndexHistoryIndex value refers to the next available
		 * element in the tabIndexHistory array in which to add tab selections.
		 * Adding a tab selection places the tab index value into this element
		 * and increments tabIndexHistoryIndex so that it continues to point to
		 * the next available position. "tabIndexHistoryIndex - 1" now refers to
		 * the current tab, while "tabIndexHistoryIndex - 2" refers to the last
		 * tab, the one to return to while going "Back". The "Back" method must
		 * therefore not only decrement tabIndexHistoryIndex, but also refer to
		 * the position preceding the new index.
		 */
		boolean repeat = true;
		boolean shift = false;
		for (int i = 0; i < tabIndexHistoryIndex && repeat; i++) {
			// shift the records as necessary to move a potential
			// duplicate to the front of the history
			if (shift) { // shift the records
				if (tabIndexHistory[i] == -1) {
					repeat = false;
				} else {
					tabIndexHistory[i - 1] = tabIndexHistory[i];
				}
			} else { // find where to start shifting, if necessary
				if (tabIndexHistory[i] == mostRecent) {
					shift = true;
				} else if (tabIndexHistory[i] == -1) {
					repeat = false;
				}
			}
		}
		// add the tab selection
		if (shift) { // add the potential duplicate to the front of the record
			tabIndexHistory[--tabIndexHistoryIndex] = mostRecent;
		} else if (tabIndexHistoryIndex >= tabIndexHistory.length) {
			// increase the array size if necessary
			tabIndexHistory = (int[]) LibTTx.growArray(tabIndexHistory);
			tabIndexHistory[tabIndexHistoryIndex] = mostRecent;
		} else if (tabIndexHistoryIndex >= 0) {
			// ensure that the tab during TextTrix's startup has no entry;
			// otherwise, the 0 tab selection index duplicates
			tabIndexHistory[tabIndexHistoryIndex] = mostRecent;
		}
		for (int i = ++tabIndexHistoryIndex; i < tabIndexHistory.length; i++) {
			tabIndexHistory[i] = -1;
		}
	}

	/**
	 * Removes an entry from the tab selection history. Shifts the tab indices
	 * as necessary.
	 * 
	 * @param removed
	 *            index of removed tab
	 * @see #addTabIndexHistory(int)
	 */
	public void removeTabIndexHistory(int removed) {
		boolean shift = false;
		// cycle through the entire history, removing the tab and shifting
		// both the tab indices and the tab history index appropriately
		for (int i = 0; i < tabIndexHistory.length; i++) {
			// flag for a record shift and check whether to decrement
			// the tab history index
			if (tabIndexHistory[i] == removed) {
				if (i <= tabIndexHistoryIndex)
					--tabIndexHistoryIndex;
				shift = true;
			}
			// shift the tab record
			if (shift) {
				tabIndexHistory[i] = (i < tabIndexHistory.length - 1) ? tabIndexHistory[i + 1]
						: -1;
			}
			// decrease tab indices for those above the that of the removed tab
			if (tabIndexHistory[i] > removed) {
				tabIndexHistory[i] = --tabIndexHistory[i];
			}
			//	    System.out.print(tabIndexHistory[i] + ",");
		}
	}
	
	public void incrementTabIndexHistoryIndex() {
		tabIndexHistoryIndex++;
	}
	
	public void decrementTabIndexHistoryIndex() {
		tabIndexHistoryIndex--;
	}
	
	public int getTabIndexHistoryIndex() {
		return tabIndexHistoryIndex;
	}
	
	public int getTabIndexHistoryCount() {
		return tabIndexHistory.length;
	}
	
	public int getTabIndexHistoryAt(int i) {
		if (i < tabIndexHistory.length) {
			return tabIndexHistory[i];
		} else {
			return -1;
		}
	}
	
	/*
	public Object getSelectedComponent() {
		return arrayList.get(getSelectedIndex());
	}
	
	public Object getComponent
	*/
}