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

import javax.swing.*;
import java.util.prefs.*;
import java.awt.*;
import java.awt.event.*;

public class Prefs extends JFrame {
	private JTabbedPane tabbedPane = new JTabbedPane();
	// houses and categorizes options
	private Preferences prefs = Preferences.userNodeForPackage(TextTrix.class);
	// main prefs

	/* Panel-specific prefs */
	private Preferences prefsPanelPrefs = prefs.node("prefsPanel");
	private static final String PREFS_WIDTH = "prefsWidth"; // panel width
	private static final String PREFS_HEIGHT = "prefsHeight"; // panel height

	/* General preferences */
	private Preferences generalPrefs = prefs.node("General");
	private static final String PRGM_WIDTH = "prgmWidth"; // program width
	private static final String PRGM_HEIGHT = "prgmHeight"; // program height
	private static final String PRGM_X_LOC = "prgmXLoc";
	// program horizontal location
	private static final String PRGM_Y_LOC = "prgmYLoc";
	// program vertical location

	//	open tabs left last session
	private static final String REOPEN_TABS = "reopenTabs";
	private JCheckBox reopenTabsChk = null; // check box
	// comma-delimited list of paths
	private static final String REOPEN_TABS_LIST = "reopenTabsList";

	// file history
	private static final String FILE_HIST_COUNT = "fileHistCount";
	private JSpinner fileHistCountSpinner = null;
	// input max num of files to remember
	private SpinnerNumberModel fileHistCountMdl; // numerical input
	private static final String FILE_HIST = "fileHist";

	// auto-auto-indent
	private static final String AUTO_INDENT = "autoIndent";
	private JCheckBox autoIndentChk = null; // check box
	// comma-or-space-delimited, period-independent list of file extensions
	private static final String AUTO_INDENT_EXT = "autoIndentExt";
	private JTextField autoIndentExtFld = null; // input list

	/** Constructs a preferences interface.
	 * Loads its previous size settings.  Relies on the calling function to
	 * supply an action that generally "okays," or stores, the preferences,
	 * and an action that usually "cancels," or rejects the changes by 
	 * destroying the object.  The calling functin must also 
	 * call <code>show()</code> to display the interface
	 * @param okAction action that generally stores the preferences
	 * @param cancelAction action that generally rejects the changes by
	 * destroying the object
	 */
	public Prefs(Action okAction, Action cancelAction) {
		int width = prefsPanelPrefs.getInt(PREFS_WIDTH, 500); // panel width
		int height = prefsPanelPrefs.getInt(PREFS_HEIGHT, 300); // panel height
		setSize(width, height);
		setTitle("You've Got Options");
		//		System.out.println("fileHistCount: " + fileHistCount);

		// sets up the tabbed pane
		tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		tabbedPane.addTab(
			"General",
			null,
			createGeneralPanel(),
			"In general...");

		// creates an action that could store preferences from individual panels;
		// the class, not the calling function, creates the action b/c no need to report
		// back to the calling function;
		// contrast "cancelAction", which requires the calling function to both dispose of
		// and destroy the object 
		Action applyAction = new AbstractAction("Apply now", null) {
			public void actionPerformed(ActionEvent evt) {
				storePrefs();
			}
		};
		LibTTx.setAcceleratedAction(
			applyAction,
			"Apply the current tabs settings immediately",
			'A',
			KeyStroke.getKeyStroke("alt A"));

		// adds the components to the panel
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;

		// anchor to the top of the panel in case don't have enough optons
		// to fill the tab; otherwise the entire JTabbedPane would become
		// centered, eg?
		constraints.anchor = GridBagConstraints.NORTH;
		LibTTx.addGridBagComponent(
			tabbedPane,
			constraints,
			0,
			0,
			3,
			1,
			100,
			100,
			contentPane);
		JButton okBtn = new JButton(okAction);
		constraints.anchor = GridBagConstraints.SOUTH;
		LibTTx.addGridBagComponent(
			okBtn,
			constraints,
			0,
			1,
			1,
			1,
			100,
			0,
			contentPane);

		// immediately stores the preferences in the given tab
		JButton applyBtn = new JButton(applyAction);
		LibTTx.addGridBagComponent(
			applyBtn,
			constraints,
			1,
			1,
			1,
			1,
			100,
			0,
			contentPane);

		// generally used to cancal changes made since the last preferences storage
		JButton cancelBtn = new JButton(cancelAction);
		LibTTx.addGridBagComponent(
			cancelBtn,
			constraints,
			2,
			1,
			1,
			1,
			100,
			0,
			contentPane);
	}

	/** Makes a panel for the "General" preferences tab.
	 * Displays options related to the program as a whole.
	 * @return the panel
	 */
	private JPanel createGeneralPanel() {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.CENTER;
		JPanel panel = new JPanel();

		// re-opens the tabs left open during the previous session
		String reopenTabsTxt = "Reopen tabs from last session";
		String reopenTabsTipTxt =
			"<html>Reopen the files left open the last time<br> you used the program.</html>";
		reopenTabsChk = new JCheckBox(reopenTabsTxt, getReopenTabs());
		reopenTabsChk.setToolTipText(reopenTabsTipTxt);

		// keeps the given number of last-opened files in memory for quick re-opening
		JLabel fileHistCountLbl =
			new JLabel("Number of files in history area:");
		String fileHistCountTipTxt =
			"<html>The number of files to include in the File menu"
				+ "<br>history area to quickly open recent files.</html>";
		fileHistCountLbl.setToolTipText(fileHistCountTipTxt);
		fileHistCountMdl =
			new SpinnerNumberModel(getFileHistCount(), 0, 100, 1);
		fileHistCountSpinner = new JSpinner(fileHistCountMdl);

		// automatically auto-indents files whose extensions match any of those in
		// the customizable list
		String autoIndentTxt = "Auto-indent file types:";
		String autoIndentTipTxt =
			"<html>Auto-indent files with the following extensions."
				+ "<br>Including the period and space is optional,"
				+ "<br>but be sure to separate each extension with a comma.</html>";
		autoIndentChk = new JCheckBox(autoIndentTxt, getAutoIndent());
		autoIndentChk.setToolTipText(autoIndentTipTxt);
		// default list of extension
		String autoIndentExt = ".java, .c, .cpp, .html, .css, .shtml, .xhtml";
		autoIndentExtFld = new JTextField(autoIndentExt, 30);

		// add components to a grid-bag layout
		panel.setLayout(new GridBagLayout());

		LibTTx.addGridBagComponent(
			reopenTabsChk,
			constraints,
			0,
			0,
			1,
			1,
			100,
			0,
			panel);
		LibTTx.addGridBagComponent(
			fileHistCountLbl,
			constraints,
			0,
			1,
			1,
			1,
			100,
			0,
			panel);
		LibTTx.addGridBagComponent(
			fileHistCountSpinner,
			constraints,
			1,
			1,
			1,
			1,
			100,
			0,
			panel);
		LibTTx.addGridBagComponent(
			autoIndentChk,
			constraints,
			0,
			2,
			1,
			1,
			100,
			0,
			panel);
		LibTTx.addGridBagComponent(
			autoIndentExtFld,
			constraints,
			1,
			2,
			1,
			1,
			100,
			0,
			panel);
		return panel;
	}

	/** Front-end to storing all the preferences at once.
	 * Includes the panel-specific preferences.
	 *
	 */
	public void storePrefs() {
		storePrefsPanelPrefs();
		storeGeneralPrefs();
	}

	/** Stores the panel-specific preferences.
	 * 
	 *
	 */
	public void storePrefsPanelPrefs() {
		prefsPanelPrefs.putInt(PREFS_WIDTH, getWidth());
		prefsPanelPrefs.putInt(PREFS_HEIGHT, getHeight());
	}

	/** Stores the General preferences.
	 * 
	 *
	 */
	public void storeGeneralPrefs() {
		generalPrefs.putBoolean(REOPEN_TABS, reopenTabsChk.isSelected());
		storeFileHistCount(fileHistCountMdl.getNumber().intValue());
		generalPrefs.putBoolean(AUTO_INDENT, autoIndentChk.isSelected());
		generalPrefs.put(AUTO_INDENT_EXT, autoIndentExtFld.getText());
	}

	/** Stores the program window size.
	 * Size values correspond to <code>java.awt.window.getWidth()</code>
	 * and similar functions' return values
	 * @param width program window width
	 * @param height program window height
	 */
	public void storeSize(int width, int height) {
		generalPrefs.putInt(PRGM_WIDTH, width);
		generalPrefs.putInt(PRGM_HEIGHT, height);
	}

	/** Stores the program's window location.
	 * 
	 * @param p upper-left-hand corner of window relative to the upper-left-hand
	 * corner of the screen
	 */
	public void storeLocation(Point p) {
		generalPrefs.putInt(PRGM_X_LOC, (int)p.getX());
		generalPrefs.putInt(PRGM_Y_LOC, (int)p.getY());
	}

	/** Stores whether the program should reopen tabs automatically.
	 * The program can reopen the tabs left open at exit during the
	 * previous session.
	 * @param b If <code>true</code>, the program will reopen the tabs.
	 */
	public void storeReopenTabs(boolean b) {
		generalPrefs.putBoolean(REOPEN_TABS, b);
	}

	/** Stores a list of files left open at exit.
	 * The list consists of comma-delimited paths, with no spaces 
	 * in-between paths.
	 * @param paths list of paths
	 */
	public void storeReopenTabsList(String paths) {
		generalPrefs.put(REOPEN_TABS_LIST, paths);
	}

	/** Stores the number of recently opened files to remember quick re-opening.
	 * If the new count is less than the previous one, the extra entries are removed.
	 * @param newCount updated number files to remember
	 */
	public void storeFileHistCount(int newCount) {
		int oldCount = generalPrefs.getInt(FILE_HIST_COUNT, newCount);
		if (newCount < oldCount) {
			for (int i = newCount; i < oldCount; i++) {
				//System.out.println("Removing file history entry: " + generalPrefs.get(FILE_HIST + i, ""));
				generalPrefs.remove(FILE_HIST + i);
			}
		}
		generalPrefs.putInt(FILE_HIST_COUNT, newCount);
	}

	/** Stores a history of the most recently opened files.
	 * The earlier the file in the history, the more recently the file
	 * was opened.  When a file is added that already exists in
	 * the list, the file is simply shifted to the head of the list
	 * rather than duplicated.
	 * @param file path of file to add to list
	 */
	public void storeFileHist(String file) {
		//System.out.println("Storing: " + file);
		String[] files = retrieveFileHist(); // gets the current history array
		boolean shift = false;
		// shift files if moving one entry to top of list
		// shifts the records as necessary to move a potential
		// duplicate to the front of the history;
		// traverses backward through the list since the entries are shifted
		// in the opposite direction, meaning that each record can be
		// checked before replacing it with a future entry
		for (int i = files.length - 1; i >= 0; i--) {
			if (shift) { // shift the records, overwriting the duplicate record
				files[i + 1] = files[i];
				// move the current record to the next spot
			} else if (
				// find where to start shifting, if necessary
			files[i].equals(
					file)) {
				shift = true;
			}
			//System.out.println("files[" + i + "]: " + files[i] + ", file: " + file);
		}
		// if already shifted files, regenerate the stored list
		if (shift) {
			// no need to check that below fileHistCount since merely substituting an entry
			files[0] = file;
			int i = 0;
			for (i = 0; i < files.length; i++) {
				generalPrefs.put(FILE_HIST + i, files[i]);
			}
		} else { // shift all records back, discarding the last one if no more room
			int fileHistCount = getFileHistCount();
			for (int i = fileHistCount - 1; i > 0; i--) {
				generalPrefs.put(
					FILE_HIST + i,
					generalPrefs.get(FILE_HIST + (i - 1), ""));
			}
			generalPrefs.put(FILE_HIST + 0, file); // add new entry to head
		}

	}

	/** Retrieves the file history as an array.
	 * 
	 * @return array of paths from each file history entry, where every element
	 * of the array is occupied by a file path
	 */
	public String[] retrieveFileHist() {
		int fileHistCount = getFileHistCount();
		String[] files = new String[fileHistCount];
		for (int i = 0; i < files.length; i++) {
			files[i] = generalPrefs.get(FILE_HIST + i, "");
			//System.out.println("files[" + i + "]: " + files[i]);
			if (files[i] == "") {
				// ensure that every array element is occuped by a path
				return (String[])LibTTx.truncateArray(files, i);
			}
		}
		return files;
	}

	/** Gets the stored width for the program.
	 * 
	 * @return width
	 */
	public int getPrgmWidth() {
		return generalPrefs.getInt(PRGM_WIDTH, 500);
	}
	/** Gets the stored height for the program.
	 * 
	 * @return height
	 */
	public int getPrgmHeight() {
		return generalPrefs.getInt(PRGM_HEIGHT, 600);
	}
	/** Gets the stored vertical location for the program.
	 * 
	 * @return vertical location of upper-left corner othe program window,
	 * relative to the upper-left corner of the screen
	 */
	public int getPrgmXLoc() {
		return generalPrefs.getInt(PRGM_X_LOC, 0);
	}
	/** Gets the stored vertical location for the program.
	 * 
	 * @return vertical location of upper-left corner othe program window,
	 * relative to the upper-left corner of the screen
	 */
	public int getPrgmYLoc() {
		return generalPrefs.getInt(PRGM_Y_LOC, 0);
	}
	/** Gets the stored flag for re-opening tabs.
	 * 
	 * @return width If <code>true</code>, the pogram will try
	 * to open all the files left open in the program during its last session
	 * @see #getReopenTabsList()
	 */
	public boolean getReopenTabs() {
		return generalPrefs.getBoolean(REOPEN_TABS, false);
	}
	/** Gets the stored list of last-opened tabs.
	 * 
	 * @return comma-delimited list of file paths
	 * @see #getReopenTabs()
	 */
	public String getReopenTabsList() {
		return generalPrefs.get(REOPEN_TABS_LIST, "");
	}
	/** Gets the stored number of files paths to remember.
	 * 
	 * @return the number of paths to store in the most-recently-opened-files
	 * history
	 */
	public int getFileHistCount() {
		return generalPrefs.getInt(FILE_HIST_COUNT, 7);
	}
	/** Gets the stored flag for automatically turning on auto-indent.
	 * 
	 * @return If <code>true</code>, the program will automatically
	 * auto-indent files with particular extensions when opening or saving them
	 * @see #getAutoIndentExt()
	 */
	public boolean getAutoIndent() {
		return generalPrefs.getBoolean(AUTO_INDENT, false);
	}
	/** Gets the stored the list of file extensions of files to automatically
	 * turn on auto-indent for.
	 * 
	 * @return comma-or-space-delimited list of file extensions, where the
	 * period is assumed and optional
	 */
	public String getAutoIndentExt() {
		return generalPrefs.get(AUTO_INDENT_EXT, "");
	}
}