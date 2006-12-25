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
import java.io.*;
import java.awt.event.*;
import java.awt.*;

public class BrowseFilesFromTextPad extends BrowseFiles {
	
	/** Constructor for browsing multiple files, using a Text Pad as a reference
	 * for default directories and the link.
	 * @param aOwner the chooser will be centered on this owner; if null, the chooser
	 * is placed at the center of the screen
	 * @param aName the file browser and accept button will use this name
	 * @param aIcon the icon for the file browser; apparently not used
	 * @param aChooser a file chooser; if null, a new chooser will be created wtih the 
	 * approve button set to <code>name</code>
	 */
	public BrowseFilesFromTextPad(Component aOwner, String aName, Icon aIcon, JFileChooser aChooser) {
		super(aOwner, aName, aIcon, aChooser);
	}
	
	/** Constructor for browsing multiple files, using a Text Pad as a reference
	 * for default directories and the link.
	 * A generic chooser will be created.
	 * @param aOwner the chooser will be centered on this owner; if null, the chooser
	 * is placed at the center of the screen
	 * @param aName the file browser and accept button will use this name
	 * @param aIcon the icon for the file browser; apparently not used
	 */
	public BrowseFilesFromTextPad(Component aOwner, String aName, Icon aIcon) {
		super(aOwner, aName, aIcon);
	}
	
	
	/** Constructor for browsing multiple files, using a Text Pad as a reference
	 * for default directories and the link.
	 * @param aOwner the chooser will be centered on this owner; if null, the chooser
	 * is placed at the center of the screen
	 * @param aName the file browser and accept button will use this name
	 * @param aIcon the icon for the file browser; apparently not used
	 * @param aCurrentDir a directory that the file chooser can open to
	 * @param aDefaultFile a file that the chooser can automatically try to select
	 * @param aChooser a file chooser; if null, a new chooser will be created wtih the 
	 * approve button set to <code>name</code>
	 */
	public BrowseFilesFromTextPad(Component aOwner, String aName, Icon aIcon, 
		File aCurrentDir, File aDefaultFile, JFileChooser aChooser) {
		super(aOwner, aName, aIcon, aCurrentDir, aDefaultFile, aChooser);
	}
	
	/** Constructor for browsing multiple files, using a Text Pad as a reference
	 * for default directories and the link.
	 * A generic chooser will be created.
	 * @param aOwner the chooser will be centered on this owner; if null, the chooser
	 * is placed at the center of the screen
	 * @param aName the file browser and accept button will use this name
	 * @param aIcon the icon for the file browser; apparently not used
	 * @param aCurrentDir a directory that the file chooser can open to
	 * @param aDefaultFile a file that the chooser can automatically try to select
	 */
	public BrowseFilesFromTextPad(Component aOwner, String aName, Icon aIcon, File aCurrentDir, File aDefaultFile) {
		super(aOwner, aName, aIcon, aCurrentDir, aDefaultFile);
	}
	
	
	/**
	 * Displays a file open chooser when the action is invoked.
	 * Defaults to the directory set as the current directory
	 * (@see #setCurrentDir).  Allows the user to open multiple
	 * file, which are recorded as an array of selected files
	 * (@see #getSelectedFile) if the user presses the approve button.
	 * If no file is selected, the selected file array will be null.
	 * 
	 * @param evt
	 *            action invocation
	 */
	public void actionPerformed(ActionEvent evt) {
		
		// clears any previously selected files
		setSelectedFiles(null);

		/*
		 * getSelectedFiles() returns an array of length 0 with the
		 * following sequence: -file opened -file saved via save chooser
		 * -same file opened by double-clicking
		 * 
		 * In other words, a file just saved cannot be reopened.
		 * 
		 * The problem does not appear when the chooser accept button is
		 * chosen, a directory is changed before the file is chosen, or the
		 * name in the text input area is altered in any way.
		 * 
		 * UPDATED: Workaround by calling setMultiSelectionEnabled with
		 * "true", "false", and "true" arguments in succession.
		 * 
		 * OLD: The workaround is to use getSelectedFile() if the array has
		 * length 0; getSelectedFile() for some reason works though its
		 * multi-partner does not. The file-not-found dialogs refrain from
		 * specifying the chosen file name since it cannot be retrieved from
		 * chooser.getSelectedFile() in the situation where the array has
		 * length 0.
		 */
		JFileChooser chooser = getChooser();
		// allows one to open multiple files;
		// must disable for save dialog
		chooser.setMultiSelectionEnabled(true);
		chooser.setMultiSelectionEnabled(false);
		chooser.setMultiSelectionEnabled(true);
		
		// sets the default directory to the parent directory of the file in
		// the currently selected Text Pad; if not possible, falls back on
		// the user-defined (@see #getCurrentDir).
		File dir = new File("");
		if (getTextPad() != null && !(dir = new File(getTextPad().getDir())).exists()) {
			dir = getCurrentDir();
		}
		chooser.setCurrentDirectory(dir);
		chooser.setSelectedFile(new File(""));

		// bring up the dialog and retrieve the result
		int result = chooser.showOpenDialog(getOwner());
		if (result == JFileChooser.APPROVE_OPTION) {
			setSelectedFiles(chooser.getSelectedFiles());
		}
	}
	
}