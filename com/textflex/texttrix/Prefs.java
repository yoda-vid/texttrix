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
import javax.swing.event.*;

/** Stores the user-preferences for Text Trix.
 * Presents a display panel for users to pick from various options to customize
 * their Text Trix experience.  The goal is to make the text tricky, but the text-making easy.
 * 
 * <p>The preferences utilize the Java preference scheme to store settings in a
 * centralized, systematic location for each platform.  The settings can also be exported to 
 * other systems for perpetual Text Trix fun.
 * @author davit
 */
public class Prefs extends JFrame {
	
	// houses and categorizes options
	private JTabbedPane tabbedPane = new JTabbedPane();
	// main prefs
	private Preferences prefs = Preferences.userNodeForPackage(TextTrix.class);






	/* Panel-specific prefs */
	private Preferences internalPrefs = prefs.node("Internal"); // panel-specific prefs holder
	private static final String PREFS_WIDTH = "prefsWidth"; // panel width
	private static final String PREFS_HEIGHT = "prefsHeight"; // panel height
	private static final String PRGM_WIDTH = "prgmWidth"; // program width
	private static final String PRGM_HEIGHT = "prgmHeight"; // program height
	// program horizontal location
	private static final String PRGM_X_LOC = "prgmXLoc";
	// program vertical location
	private static final String PRGM_Y_LOC = "prgmYLoc";
	
	
	
	
	
	
	
	/* General preferences */
	private Preferences generalPrefs = prefs.node("General"); // general prefs holder
	private static final int GENERAL_PANEL_INDEX = 0; // tab position
	private CreateGeneralPanel createGeneralPanel = new CreateGeneralPanel();

	//	open tabs left last session
	private static final String REOPEN_TABS = "reopenTabs";
	private JCheckBox reopenTabsChk = null; // check box
	// comma-delimited list of paths
	private static final String REOPEN_TABS_LIST = "reopenTabsList";

	// file history
	private static final String FILE_HIST_COUNT = "fileHistCount";
	// input max num of files to remember
	private JSpinner fileHistCountSpinner = null;
	private SpinnerNumberModel fileHistCountMdl; // numerical input
	// comma-delimited list of paths
	private static final String FILE_HIST = "fileHist";

	// auto-auto-indent
	private static final String AUTO_INDENT = "autoIndent";
	private JCheckBox autoIndentChk = null; // check box
	// comma-or-space-delimited, period-independent list of file extensions
	private static final String AUTO_INDENT_EXT = "autoIndentExt";
	private JTextField autoIndentExtFld = null; // input list
	
	private static final String ACTIVATE_WINDOWS_TOGETHER = "activateWindowsTogether";
	private JCheckBox activateWindowsTogetherChk = null;
	
	
	
	
	
	/* Shorts preferences--shortcuts */
	private Preferences shortsPrefs = prefs.node("Shorts"); // shorts-specific prefs holder
	private static final int SHORTS_PANEL_INDEX = 1; // tab index
	private CreateShortsPanel createShortsPanel = new CreateShortsPanel();
	
	// keybinding models
	private static final String KEYBINDINGS = "keybindings"; // model
	private static final String STD_KEYBINDINGS_MDL = "Standard"; // standard style
	// mix of standard and single line and character Emacs
	private static final String HYBRID_KEYBINDINGS_MDL = "Hybrid";
	// typical single key Emacs shortcuts
	private static final String EMACS_KEYBINDINGS_MDL = "Emacs";
	// array of models
	private static final String[] KEYBINDINGS_MDLS =
		{ STD_KEYBINDINGS_MDL, HYBRID_KEYBINDINGS_MDL, EMACS_KEYBINDINGS_MDL };
	private JComboBox keybindingsCombo = null; // combo box to select the model
	
	
	
	
	
	/* Plug-ins preferences */
	private Preferences plugInsPrefs = prefs.node("PlugIns"); // plug-ins-specific prefs holder
	private static final int PLUG_INS_PANEL_INDEX = 2; // tab index
	private CreatePlugInsPanel createPlugInsPanel = new CreatePlugInsPanel();
	
	// include all plug-ins
	private static final String ALL_PLUG_INS = "allPlugIns";
	private JCheckBox allPlugInsChk = null;
	
	// include only specific plug-ins
	private JList includePlugInsList = null; // lists plug-ins to include...
	private JList ignorePlugInsList = null; // ...to exclude
	private String[] plugInsList = null; // master list of all plug-ins; must be updated externally
	private String[] includes = null; // list of the plug-ins to include...
	private String[] ignores = null; // ...to exclude
	private static final String INCLUDE_PLUG_INS = "includePlugIns"; // includes
	private static final String IGNORE_PLUG_INS = "ignorePlugIns"; // excludes

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
	public Prefs(Action okAction, Action applyAction, Action cancelAction) {
		int width = internalPrefs.getInt(PREFS_WIDTH, 500); // panel width
		int height = internalPrefs.getInt(PREFS_HEIGHT, 300); // panel height
		setSize(width, height);
		setTitle("You've Got Options");
		//generalPrefs.addPreferenceChangeListener(prefsListener);
		//shortsPrefs.addPreferenceChangeListener(prefsListener);
		//		System.out.println("fileHistCount: " + fileHistCount);

		// sets up the tabbed pane
		tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		tabbedPane.insertTab(
			"General",
			null,
			null,
			"In general...",
			GENERAL_PANEL_INDEX);
		//tabbedPane.setComponentAt(0, createGeneralPanel());
		createGeneralPanel.start();

		tabbedPane.insertTab(
			"Shorts",
			null,
			null,
			"Shortcuts...",
			SHORTS_PANEL_INDEX);
		createShortsPanel.start();

		tabbedPane.insertTab(
			"Plug-Ins",
			null,
			null,
			"Don't get electricuted...",
			PLUG_INS_PANEL_INDEX);

		// adds the components to the panel
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;

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
		constraints.fill = GridBagConstraints.HORIZONTAL;
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
	
	/** Updates the plug-ins panel with the new list of plug-ins.
	 * Re-creates the panel.  This list must be updated or else the panel will
	 * have no idea which plug-ins are loaded.
	 * @param list list of all loaded plug-ins, regardless of whether included
	 * or ignored
	 */
	public void updatePlugInsPanel(String[] list) {
		setPlugInsList(list);
		updatePlugInsList(list);
		createPlugInsPanel.start();
	}
	
	/** Updates the list of plug-ins.
	 * 
	 * @param list list of all loaded plug-ins, regardless of whether included
	 * or ignored
	 */
	private void updatePlugInsList(String[] list) {
		includes = LibTTx.createArrayFromString(getIncludePlugIns());
		ignores = LibTTx.createArrayFromString(getIgnorePlugIns());
		String[] updatedIncludes = new String[list.length];
		int updatedInclInd = 0;
		String[] updatedIgnores = new String[list.length];
		int updatedIgnInd = 0;
		// runs through the list of loaded plug-ins and adds those previously
		// ignored to the "ignores" list and adding all the rest to the "includes"
		// list; any plug-ins previously in the lists but no longer loaded are not
		// included in either list
		for (int i = 0; i < list.length; i++) {
			if (LibTTx.inUnsortedList(list[i], ignores)) {
				updatedIgnores[updatedIgnInd++] = list[i];
			} else {
				updatedIncludes[updatedInclInd++] = list[i];
			}
		}
		includes = updatedIncludes;
		ignores = updatedIgnores;
	}

	/** Front-end to storing all the preferences at once.
	 * Includes the panel-specific preferences.
	 *
	 */
	public void storePrefs() {
		storeInternalPrefs();
		storeGeneralPrefs();
		storeShortsPrefs();
		storePlugInsPrefs();
	}

	/** Stores the program's internal preferences.
	 * Does not store program size or location information, but does store
	 * the panel's size.
	 *
	 */
	public void storeInternalPrefs() {
		internalPrefs.putInt(PREFS_WIDTH, getWidth());
		internalPrefs.putInt(PREFS_HEIGHT, getHeight());
	}

	/** Stores the General preferences.
	 * General preferences include settings that affect Text Trix as a whole.
	 *
	 */
	public void storeGeneralPrefs() {
		generalPrefs.putBoolean(REOPEN_TABS, reopenTabsChk.isSelected());
		storeFileHistCount(fileHistCountMdl.getNumber().intValue());
		generalPrefs.putBoolean(AUTO_INDENT, autoIndentChk.isSelected());
		generalPrefs.put(AUTO_INDENT_EXT, autoIndentExtFld.getText());
		generalPrefs.putBoolean(ACTIVATE_WINDOWS_TOGETHER, 
			activateWindowsTogetherChk.isSelected());
	}
	
	/** Stores the Shorts preferences.
	 * Shorts preferences are those referring to shortcuts settings.
	 *
	 */
	public void storeShortsPrefs() {
		shortsPrefs.put(
			KEYBINDINGS,
			(String) keybindingsCombo.getSelectedItem());
	}
	
	/** Stores the PlugIns preferences.
	 * Plug-ins preferences manage which plug-ins are loaded.
	 *
	 */
	public void storePlugInsPrefs() {
		plugInsPrefs.putBoolean(ALL_PLUG_INS, allPlugInsChk.isSelected());
		plugInsPrefs.put(INCLUDE_PLUG_INS, getListAsString(includePlugInsList));
		plugInsPrefs.put(IGNORE_PLUG_INS, getListAsString(ignorePlugInsList));
	}
	
	/** Gets the contents of a <code>JList</code> as a comma-delimited string.
	 * Many Text Trix preferences for multiple but an unknown number of settings.
	 * @param list list with contents to be recorded as a <code>String</code>
	 * @return the string
	 */
	public String getListAsString(JList list) {
		String s = "";
		DefaultListModel mdl = (DefaultListModel) list.getModel();
		Object[] elts = mdl.toArray();
		for (int i = 0; i < elts.length; i++) {
			// add a comma only internally in the list
			if (i == 0) {
				s = (String) elts[i];
			} else {
				s = s + "," + (String) elts[i];
			}
		}
		return s;
	}

	/** Stores the program window size.
	 * Size values correspond to <code>java.awt.window.getWidth()</code>
	 * and similar functions' return values
	 * @param width program window width
	 * @param height program window height
	 */
	public void storeSize(int width, int height) {
		internalPrefs.putInt(PRGM_WIDTH, width);
		internalPrefs.putInt(PRGM_HEIGHT, height);
	}

	/** Stores the program's window location.
	 * 
	 * @param p upper-left-hand corner of window relative to the upper-left-hand
	 * corner of the screen
	 */
	public void storeLocation(Point p) {
		internalPrefs.putInt(PRGM_X_LOC, (int) p.getX());
		internalPrefs.putInt(PRGM_Y_LOC, (int) p.getY());
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
				System.out.println(
					"Removing file history entry: "
						+ generalPrefs.get(FILE_HIST + i, ""));
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
				return (String[]) LibTTx.truncateArray(files, i);
			}
		}
		return files;
	}

	/** Checks whether the the given preferences are the set of
	 * "General" preferences, or holistic settings for Text Trix.
	 * @param p
	 * @return <code>true</code> if the preferences are the set of
	 * "General" ones
	 */
	public boolean isGeneralPrefs(Preferences p) {
		return p == generalPrefs;
	}

	/** Checks whether the the given preferences are the set of
	 * Shorts preferences, or shortcut settings for Text Trix.
	 * @param p
	 * @return <code>true</code> if the preferences are the set of
	 * Shorts ones
	 */
	public boolean isShortsPrefs(Preferences p) {
		return p == shortsPrefs;
	}

	/** Checks whether the keybindings are set for the "Standard" shortcuts,
	 * or those in applications on most desktop systems.
	 * @return <code>true</code> if the keybindings are the set of
	 * "Standard" ones
	 */
	public boolean isStandardKeybindings() {
		return getKeybindings().equals(STD_KEYBINDINGS_MDL);
	}

	/** Checks whether the keybindings are set for the "Hybrid" shortcuts,
	 * or a mesh of "Standard" shortcuts and those of Emacs single character
	 * and line navigation, where the Emacs shortcuts take precedence.
	 * @return <code>true</code> if the keybindings are the set of
	 * "Hybrid" ones
	 */
	public boolean isHybridKeybindings() {
		return getKeybindings().equals(HYBRID_KEYBINDINGS_MDL);
	}

	/** Checks whether the keybindings are set for the "Emacs" shortcuts,
	 * or typical Emacs single key shortcuts layered on top of the "Hybrid" shortcuts. 
	 * @return <code>true</code> if the keybindings are the set of
	 * "Emacs" ones
	 */
	public boolean isEmacsKeybindings() {
		return getKeybindings().equals(EMACS_KEYBINDINGS_MDL);
	}

	/** Sets the list of plug-ins to the given list to display in the plug-ins
	 * choice box.
	 * 
	 * @param s array of plug-ins
	 */
	public void setPlugInsList(String[] s) {
		plugInsList = s;
	}

	/** Gets the stored width for the program.
	 * 
	 * @return width
	 */
	public int getPrgmWidth() {
		return internalPrefs.getInt(PRGM_WIDTH, 500);
	}
	/** Gets the stored height for the program.
	 * 
	 * @return height
	 */
	public int getPrgmHeight() {
		return internalPrefs.getInt(PRGM_HEIGHT, 600);
	}
	/** Gets the stored vertical location for the program.
	 * 
	 * @return vertical location of upper-left corner othe program window,
	 * relative to the upper-left corner of the screen
	 */
	public int getPrgmXLoc() {
		return internalPrefs.getInt(PRGM_X_LOC, 0);
	}
	/** Gets the stored vertical location for the program.
	 * 
	 * @return vertical location of upper-left corner othe program window,
	 * relative to the upper-left corner of the screen
	 */
	public int getPrgmYLoc() {
		return internalPrefs.getInt(PRGM_Y_LOC, 0);
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
	public boolean getActivateWindowsTogether() {
		return generalPrefs.getBoolean(ACTIVATE_WINDOWS_TOGETHER, true);
	}
	/** Gets the stored keybindings preference.
	 * 
	 * @return the keybindings preference
	 */
	public String getKeybindings() {
		return shortsPrefs.get(KEYBINDINGS, "");
	}
	/** Gets the preference to include all the plug-ins.
	 * 
	 * @return <code>true</code> if all the plug-ins should be included
	 */
	public boolean getAllPlugIns() {
		return plugInsPrefs.getBoolean(ALL_PLUG_INS, true);
	}
	public String getIncludePlugIns() {
		return plugInsPrefs.get(INCLUDE_PLUG_INS, "");
	}
	/** Gets the stored list of plug-ins to include.
	 * 
	 * @return the list of plug-ins to make usable in Text Trix
	 */
	public String[] getIncludePlugInsNames() {
		return includes;
	}
	/** Gets the stored list of plug-ins to ignore.
	 * The plug-ins will still be loaded, but not used.
	 * 
	 * @return the list of plug-ins to ignore in Text Trix
	 */
	public String getIgnorePlugIns() {
		return plugInsPrefs.get(IGNORE_PLUG_INS, "");
	}
	/*
	public String[] getIgnorePlugInsNames() {
		return ignores;
	}
	*/

	/** Worker thread to create the preferences panel.
	 * 
	 * @author davit
	 */
	private class CreateGeneralPanel extends Thread {

		/** Starts the thread.
		 * 
		 */
		public void start() {
			(new Thread(this, "thread")).start();
		}

		/** Makes a panel for the "General" preferences tab.
		 * Displays options related to the program as a whole.
		 * @return the panel
		 */
		public void run() {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					GridBagConstraints constraints = new GridBagConstraints();
					constraints.fill = GridBagConstraints.HORIZONTAL;
					constraints.anchor = GridBagConstraints.NORTH;
					JPanel panel = new JPanel();
					panel.setLayout(new GridBagLayout());

					// re-opens the tabs left open during the previous session
					String reopenTabsTxt = "Reopen tabs from last session";
					String reopenTabsTipTxt =
						"<html>Reopen the files left open the last time<br> you used the program.</html>";
					reopenTabsChk =
						new JCheckBox(reopenTabsTxt, getReopenTabs());
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
					autoIndentChk =
						new JCheckBox(autoIndentTxt, getAutoIndent());
					autoIndentChk.setToolTipText(autoIndentTipTxt);
					// default list of extension
					String autoIndentExt =
						".java, .c, .cpp, .html, .css, .shtml, .xhtml";
					autoIndentExtFld = new JTextField(autoIndentExt, 30);
					


					// re-opens the tabs left open during the previous session
					String activateWindowsTogetherTxt = "Bring all windows to the front";
					String activateWindowsTogetherTipTxt =
						"<html>When activating one window, all other windows are"							+ "brought just below it.</html>";
					activateWindowsTogetherChk =
						new JCheckBox(activateWindowsTogetherTxt, getActivateWindowsTogether());
					activateWindowsTogetherChk.setToolTipText(activateWindowsTogetherTipTxt);

					// add components to a grid-bag layout
					panel.setLayout(new GridBagLayout());

					LibTTx.addGridBagComponent(
						reopenTabsChk,
						constraints,
						0,
						0,
						2,
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
						0,
						0,
						panel);
					LibTTx.addGridBagComponent(
						fileHistCountSpinner,
						constraints,
						1,
						1,
						1,
						1,
						0,
						0,
						panel);
					LibTTx.addGridBagComponent(
						autoIndentChk,
						constraints,
						0,
						2,
						1,
						1,
						0,
						0,
						panel);
					LibTTx.addGridBagComponent(
						autoIndentExtFld,
						constraints,
						1,
						2,
						1,
						1,
						0,
						0,
						panel);
					LibTTx.addGridBagComponent(
						activateWindowsTogetherChk,
						constraints,
						0,
						3,
						2,
						1,
						0,
						100,
						panel);
					tabbedPane.setComponentAt(GENERAL_PANEL_INDEX, panel);
				}
			});
		}
	}

	/** Worker thread to create the "Shorts" panel of shortcuts options.
	 * 
	 * @author davit
	 */
	private class CreateShortsPanel extends Thread {
		/** Starts the thread.
		 * 
		 */
		public void start() {
			(new Thread(this, "thread")).start();
		}

		/** Creates the panel.
		 * 
		 */
		public void run() {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					GridBagConstraints constraints = new GridBagConstraints();
					constraints.fill = GridBagConstraints.HORIZONTAL;
					constraints.anchor = GridBagConstraints.NORTH;
					JPanel panel = new JPanel();
					panel.setLayout(new GridBagLayout());

					// Keybindings models selector
					JLabel keybindingsLbl = new JLabel("Keybindings model");
					String keybindingsTipTxt =
						"<html>\'Standard\' shortcuts are the ones found on most desktops."
							+ "<br>\'Hybrid\' refers to Emacs shortcuts for all single line and character navigation."
							+ "<br>\'Emacs\' adds more single-key Emacs shortcuts for hard-core Emacs fans."
							+ "<br>See <i>Help &gt; Shortcuts</i> for a table of the keybindings.</html>";
					keybindingsLbl.setToolTipText(keybindingsTipTxt);
					keybindingsCombo = new JComboBox(KEYBINDINGS_MDLS);
					keybindingsCombo.setSelectedItem(getKeybindings());

					// Add the components to the panel
					LibTTx.addGridBagComponent(
						keybindingsLbl,
						constraints,
						0,
						0,
						1,
						1,
						100,
						100,
						panel);
					LibTTx.addGridBagComponent(
						keybindingsCombo,
						constraints,
						1,
						0,
						1,
						1,
						100,
						0,
						panel);

					tabbedPane.setComponentAt(SHORTS_PANEL_INDEX, panel);
				}
			});

		}
	}

	/** Worker thread to create the "PlugIns" panel of plug-in options.
	 * 
	 * @author davit
	 */
	private class CreatePlugInsPanel extends Thread {
		JButton moveToIgnoresBtn = null;
		JButton moveToIncludesBtn = null;
		JLabel plugInsSelectionLbl = new JLabel("Which plug-ins do you want?");

		/** Starts the thread.
		 * 
		 */
		public void start() {
			(new Thread(this, "thread")).start();
		}

		/** Creates the panel.
		 * 
		 */
		public void run() {

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					JPanel panel = new JPanel();
					GridBagConstraints constraints = new GridBagConstraints();
					constraints.fill = GridBagConstraints.HORIZONTAL;
					constraints.anchor = GridBagConstraints.NORTH;
					panel.setLayout(new GridBagLayout());

					// Option to include all the plug-ins, which disables the plug-in
					// selection components
					String allPlugInsTxt = "Include all the plug-ins";
					allPlugInsChk =
						new JCheckBox(allPlugInsTxt, getAllPlugIns());
					allPlugInsChk.addChangeListener(new ChangeListener() {
						public void stateChanged(ChangeEvent evet) {
							setPlugInsSelectorsEnabled(
								!allPlugInsChk.isSelected());
						}
					});

					// Plug-in selectors

					// list of plug-ins to include
					DefaultListModel includeListModel = new DefaultListModel();
					includePlugInsList = createList(includeListModel, includes);
					JScrollPane includeListPane =
						new JScrollPane(includePlugInsList);
					includeListPane.setPreferredSize(new Dimension(250, 80));

					// list of plug-ins to ignore
					DefaultListModel ignoreListModel = new DefaultListModel();
					ignorePlugInsList = createList(ignoreListModel, ignores);
					JScrollPane ignoreListPane =
						new JScrollPane(ignorePlugInsList);
					ignoreListPane.setPreferredSize(new Dimension(250, 80));

					// button to ignore the selected plug-ins from the list of included plug-ins
					String moveToIgnoresTxt = "Ignore";
					Action moveToIgnoresAction =
						createMoveAction(
							includePlugInsList,
							moveToIgnoresBtn,
							moveToIgnoresTxt,
							ignorePlugInsList,
							"images/arrow-down.png");
					moveToIgnoresBtn = new JButton(moveToIgnoresAction);
					String moveToIgnoresTip =
						"Move the selected plug-ins to the list of those to ignore, below";
					moveToIgnoresBtn.setToolTipText(moveToIgnoresTip);
					addEnabledListener(includePlugInsList, moveToIgnoresBtn);
					moveToIgnoresBtn.setEnabled(
						includeListModel.getSize() != 0);

					// button to include the selected plug-ins from the list of ignored plug-ins
					String moveToIncludesTxt = "Include";
					Action moveToIncludesAction =
						createMoveAction(
							ignorePlugInsList,
							moveToIncludesBtn,
							moveToIncludesTxt,
							includePlugInsList,
							"images/arrow-up.png");
					moveToIncludesBtn = new JButton(moveToIncludesAction);
					String moveToIncludesTip =
						"Move the selected plug-ins to the list of those to include, above";
					moveToIncludesBtn.setToolTipText(moveToIncludesTip);
					addEnabledListener(ignorePlugInsList, moveToIncludesBtn);
					moveToIncludesBtn.setEnabled(
						ignoreListModel.getSize() != 0);

					// initial setting to disable the plug-in selectors if the option 
					//to incluce all plug-ins is checked
					setPlugInsSelectorsEnabled(!allPlugInsChk.isSelected());

					// add the components to the panel
					LibTTx.addGridBagComponent(
						allPlugInsChk,
						constraints,
						0,
						0,
						2,
						1,
						100,
						0,
						panel);
					LibTTx.addGridBagComponent(
						plugInsSelectionLbl,
						constraints,
						0,
						1,
						2,
						1,
						0,
						0,
						panel);
					LibTTx.addGridBagComponent(
						includeListPane,
						constraints,
						0,
						2,
						2,
						1,
						0,
						0,
						panel);
					LibTTx.addGridBagComponent(
						moveToIgnoresBtn,
						constraints,
						0,
						3,
						1,
						1,
						100,
						0,
						panel);
					LibTTx.addGridBagComponent(
						moveToIncludesBtn,
						constraints,
						1,
						3,
						1,
						1,
						100,
						0,
						panel);
					LibTTx.addGridBagComponent(
						ignoreListPane,
						constraints,
						0,
						4,
						2,
						1,
						0,
						100,
						panel);
					tabbedPane.setComponentAt(PLUG_INS_PANEL_INDEX, panel);
				}
			});
		}

		/** Enables or disables the plug-ins boxes and buttons.
		 * 
		 * @param b <code>true</code> to enable all of the boxes and buttons,
		 * necessary to pick individual plug-ins when the check box to include all 
		 * plug-ins is unchecked.
		 */
		private void setPlugInsSelectorsEnabled(boolean b) {
			plugInsSelectionLbl.setEnabled(b);
			includePlugInsList.setEnabled(b);
			ignorePlugInsList.setEnabled(b);
			moveToIgnoresBtn.setEnabled(b);
			moveToIncludesBtn.setEnabled(b);
		}

		/** Creates the list of plug-ins.
		 * 
		 * @param listMdl display list model
		 * @param listElts names of plug-ins to include
		 * @return the list
		 */
		private JList createList(DefaultListModel listMdl, String[] listElts) {
			String addElt = null;
			for (int i = 0;
				i < listElts.length && (addElt = listElts[i]) != null;
				i++) {
				listMdl.addElement(addElt);
			}
			JList list = new JList(listMdl);
			list.setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			list.setLayoutOrientation(JList.VERTICAL);
			list.setVisibleRowCount(-1);
			return list;
		}

		/** Creates an action to move a list item from one list to another.
		 * 
		 * @param fromList source list
		 * @param btn
		 * @param txt
		 * @param toList
		 * @return
		 */
		private Action createMoveAction(
			final JList fromList,
			final JButton btn,
			String txt,
			final JList toList,
			String iconPath) {
			Action action =
				new AbstractAction(txt, LibTTx.makeIcon(iconPath)) {
				public void actionPerformed(ActionEvent evt) {
					Object[] selected = fromList.getSelectedValues();
					DefaultListModel fromListModel =
						(DefaultListModel) fromList.getModel();
					for (int i = 0; i < selected.length; i++) {
						fromListModel.removeElement(selected[i]);
					}
					for (int i = 0; i < selected.length; i++) {
						((DefaultListModel) toList.getModel()).addElement(
							selected[i]);
					}
				}
			};
			return action;
		}

		/** Adds a listener to enable a button once an empty list is gains
		 * an item.
		 * @param list the list
		 * @param btn the button
		 */
		private void addEnabledListener(JList list, final JButton btn) {
			final DefaultListModel mdl = (DefaultListModel) list.getModel();
			list.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent evt) {
					btn.setEnabled(mdl.getSize() != 0);
				}
			});
		}
	}
}