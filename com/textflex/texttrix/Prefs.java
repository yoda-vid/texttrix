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
	private Preferences prefs = Preferences.userNodeForPackage(TextTrix.class);
	private Preferences prefsPanelPrefs = prefs.node("prefsPanel");
	private static final String PREFS_WIDTH = "prefsWidth";
	private static final String PREFS_HEIGHT = "prefsHeight";
	private Preferences generalPrefs = prefs.node("General");
	private static final String PRGM_WIDTH = "prgmWidth";
	private static final String PRGM_HEIGHT = "prgmHeight";
	private static final String PRGM_X_LOC = "prgmXLoc";
	private static final String PRGM_Y_LOC = "prgmYLoc";
	//private int fileHistCount = 7;
	private static final String FILE_HIST_COUNT = "fileHistCount";
	private JSpinner fileHistCountSpinner = null;
	private SpinnerNumberModel fileHistCountMdl;
	private static final String AUTO_INDENT = "autoIndent";
	private JCheckBox autoIndentChk = null;
	private static final String AUTO_INDENT_EXT = "autoIndentExt";
	private JTextField autoIndentExtFld = null;
	//private int fileHistIndex = 0;
	private static final String FILE_HIST = "fileHist";

	public Prefs(Action okAction, Action cancelAction) {
		int width = prefsPanelPrefs.getInt(PREFS_WIDTH, 500);
		int height = prefsPanelPrefs.getInt(PREFS_HEIGHT, 300);
		setSize(width, height);
		setTitle("You've Got Options");
		//		System.out.println("fileHistCount: " + fileHistCount);

		tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		tabbedPane.addTab(
			"General",
			null,
			createGeneralPanel(),
			"In general...");

		Action applyAction = new AbstractAction("Apply now", null) {
			public void actionPerformed(ActionEvent evt) {
				storePrefs();
			}
		};

		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;

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

	private JPanel createGeneralPanel() {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.CENTER;
		JPanel panel = new JPanel();

		JLabel fileHistCountLbl =
			new JLabel("Number of files in history area:");
		String fileHistCountTipTxt =
			"<html>The number of files to include in the File menu"
				+ "<br>history area to quickly open recent files.</html>";
		fileHistCountLbl.setToolTipText(fileHistCountTipTxt);
		fileHistCountMdl =
			new SpinnerNumberModel(getFileHistCount(), 0, 100, 1);
		fileHistCountSpinner = new JSpinner(fileHistCountMdl);

		//JLabel autoIndentLbl = new JLabel();
		boolean autoIndent = generalPrefs.getBoolean(AUTO_INDENT, false);
		String autoIndentTxt = "Auto-indent file types:";
		String autoIndentTipTxt =
			"<html>Auto-indent files with the following extensions."
				+ "<br>Including the period and space is optional,"
				+ "<br>but be sure to separate each extension with a comma.</html>";
		autoIndentChk = new JCheckBox(autoIndentTxt, autoIndent);
		autoIndentChk.setToolTipText(autoIndentTipTxt);
		String autoIndentExt = ".java, .c, .cpp, .html, .css, .shtml, .xhtml";
		autoIndentExtFld = new JTextField(autoIndentExt, 30);

		panel.setLayout(new GridBagLayout());
		LibTTx.addGridBagComponent(
			fileHistCountLbl,
			constraints,
			0,
			0,
			1,
			1,
			100,
			0,
			panel);
		LibTTx.addGridBagComponent(
			fileHistCountSpinner,
			constraints,
			1,
			0,
			1,
			1,
			100,
			0,
			panel);
		LibTTx.addGridBagComponent(
			autoIndentChk,
			constraints,
			0,
			1,
			1,
			1,
			100,
			0,
			panel);
		LibTTx.addGridBagComponent(
			autoIndentExtFld,
			constraints,
			1,
			1,
			1,
			1,
			100,
			0,
			panel);
		return panel;
	}

	public void storePrefs() {
		storePrefsPanelPrefs();
		storeGeneralPrefs();
	}

	public void storePrefsPanelPrefs() {
		prefsPanelPrefs.putInt(PREFS_WIDTH, getWidth());
		prefsPanelPrefs.putInt(PREFS_HEIGHT, getHeight());
	}

	public void storeGeneralPrefs() {
		generalPrefs.putInt(
			FILE_HIST_COUNT,
			fileHistCountMdl.getNumber().intValue());
		generalPrefs.putBoolean(AUTO_INDENT, autoIndentChk.isSelected());
		generalPrefs.put(AUTO_INDENT_EXT, autoIndentExtFld.getText());
	}

	public void storeSize(int width, int height) {
		generalPrefs.putInt(PRGM_WIDTH, width);
		generalPrefs.putInt(PRGM_HEIGHT, height);
	}

	public void storeLocation(Point p) {
		generalPrefs.putInt(PRGM_X_LOC, (int)p.getX());
		generalPrefs.putInt(PRGM_Y_LOC, (int)p.getY());
	}

	public void storeFileHist(String file) {
		//System.out.println("Storing: " + file);
		String[] files = retrieveFileHist();
		boolean shift = false;
		for (int i = files.length - 1; i >= 0; i--) {
			// shift the records as necessary to move a potential
			// duplicate to the front of the history
			if (shift) { // shift the records
				files[i + 1] = files[i];
			} else if (
				files[i].equals(
					file)) { // find where to start shifting, if necessary
				shift = true;
			}
			//System.out.println("files[" + i + "]: " + files[i] + ", file: " + file);
		}
		if (shift) {
			// no need to check that below fileHistCount since merely substituting an entry
			files[0] = file;
			int i = 0;
			for (i = 0; i < files.length; i++) {
				generalPrefs.put(FILE_HIST + i, files[i]);
			}
			//fileHistIndex = i++;
		} else {
			//if (files.length >= fileHistCount) {
			//System.out.println("I'm here");
			//String tmpFile = file;
			int fileHistCount = getFileHistCount();
			for (int i = fileHistCount - 1; i > 0; i--) {
				//System.out.println("Adding: " + tmpFile);
				generalPrefs.put(
					FILE_HIST + i,
					generalPrefs.get(FILE_HIST + (i - 1), ""));
				//tmpFile = fileHist.get(FILE_HIST + (i + 1), "");
			}
			//fileHistIndex = fileHistCount - 1;
			//}
			generalPrefs.put(FILE_HIST + 0, file);
		}

	}
	public String[] retrieveFileHist() {
		int fileHistCount = getFileHistCount();
		String[] files = new String[fileHistCount];
		//fileHistIndex = 0;
		for (int i = 0; i < files.length; i++) {
			//fileHistIndex = i + 1;
			files[i] = generalPrefs.get(FILE_HIST + i, "");
			//System.out.println("files[" + i + "]: " + files[i]);
			if (files[i] == "") {
				return (String[])LibTTx.truncateArray(files, i);
			}
			//fileHistIndex++;
		}
		return files;
	}

	public int getPrgmWidth() {
		return generalPrefs.getInt(PRGM_WIDTH, 500);
	}

	public int getPrgmHeight() {
		return generalPrefs.getInt(PRGM_HEIGHT, 600);
	}

	public int getPrgmXLoc() {
		return generalPrefs.getInt(PRGM_X_LOC, 0);
	}

	public int getPrgmYLoc() {
		return generalPrefs.getInt(PRGM_Y_LOC, 0);
	}

	public int getFileHistCount() {
		return generalPrefs.getInt(FILE_HIST_COUNT, 7);
		//fileHistCountSpinner.getModel().getNumber().intValue();
	}

	public boolean getAutoIndent() {
		return generalPrefs.getBoolean(AUTO_INDENT, false);
	}

	public String getAutoIndentExt() {
		return generalPrefs.get(AUTO_INDENT_EXT, "");
	}
}