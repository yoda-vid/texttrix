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
	
	public BrowseFilesFromTextPad(Component aOwner, String aName, Icon icon) {
		super(aOwner, aName, icon);
	}
	
	
	public BrowseFilesFromTextPad(Component aOwner, String aName, Icon icon, File aCurrentDir, File aDefaultFile) {
		super(aOwner, aName, icon, aCurrentDir, aDefaultFile);
	}
	
	/*
	public BrowseFilesFromTextPad(Component aOwner, String aName, Icon icon, 
		TextPad aTextPad, String aOpenDir) {
		super(aOwner, aName, icon, aTextPad, aOpenDir);
	}
	*/
	
	/**
	 * Displays a file open chooser when the action is invoked. Defaults to
	 * the directory from which the last file was opened or, if no files
	 * have been opened, to the user's home directory.
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
		chooser.setMultiSelectionEnabled(true);
		chooser.setMultiSelectionEnabled(false);
		chooser.setMultiSelectionEnabled(true);
		String dir = "";
		if (getTextPad() != null && (dir = getTextPad().getDir()).equals(""))
			dir = getOpenDir();
		chooser.setCurrentDirectory(new File(dir));
		chooser.setSelectedFile(new File(""));
		// allows one to open multiple files;
		// must disable for save dialog

		int result = chooser.showOpenDialog(getOwner());
		// bring up the dialog and retrieve the result
		if (result == JFileChooser.APPROVE_OPTION) {
			setSelectedFiles(chooser.getSelectedFiles());
		}
	}
	
}