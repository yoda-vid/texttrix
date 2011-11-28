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
import java.io.*;
import java.awt.event.*;
import java.awt.*;

/** Allows the user to browse for a single file.
 * @see BrowseFilesFromTextPad
 */
public class BrowseSingleFile extends BrowseFiles {
	
	/** Constructor for browsing a single file.
	 * @param aOwner the chooser will be centered on this owner; if null, the chooser
	 * is placed at the center of the screen
	 * @param aName the file browser and accept button will use this name
	 * @param icon the icon for the file browser; apparently not used
	 * @param aChooser a file chooser; if null, a new chooser will be created wtih the 
	 * approve button set to <code>name</code>
	 */
	public BrowseSingleFile(Component aOwner, String aName, Icon icon, JFileChooser aChooser) {
		super(aOwner, aName, icon, aChooser);
	}
	
	/** Constructor for browsing a single file.
	 * A generic chooser will be created.
	 * @param aOwner the chooser will be centered on this owner; if null, the chooser
	 * is placed at the center of the screen
	 * @param aName the file browser and accept button will use this name
	 * @param icon the icon for the file browser; apparently not used
	 */
	public BrowseSingleFile(Component aOwner, String aName, Icon icon) {
		super(aOwner, aName, icon);
	}
	
	
	/** Constructor for browsing a single file.
	 * @param aOwner the chooser will be centered on this owner; if null, the chooser
	 * is placed at the center of the screen
	 * @param aName the file browser and accept button will use this name
	 * @param icon the icon for the file browser; apparently not used
	 * @param aCurrentDir a directory that the file chooser can open to
	 * @param aDefaultFile a file that the chooser can automatically try to select
	 * @param aChooser a file chooser; if null, a new chooser will be created wtih the 
	 * approve button set to <code>name</code>
	 */
	public BrowseSingleFile(Component aOwner, String aName, Icon icon, 
		File aCurrentDir, File aDefaultFile, JFileChooser aChooser) {
		super(aOwner, aName, icon, aCurrentDir, aDefaultFile, aChooser);
	}
	
	/** Constructor for browsing a single file.
	 * A generic chooser will be created.
	 * @param aOwner the chooser will be centered on this owner; if null, the chooser
	 * is placed at the center of the screen
	 * @param aName the file browser and accept button will use this name
	 * @param icon the icon for the file browser; apparently not used
	 * @param aCurrentDir a directory that the file chooser can open to
	 * @param aDefaultFile a file that the chooser can automatically try to select
	 */
	public BrowseSingleFile(Component aOwner, String aName, Icon icon, 
		File aCurrentDir, File aDefaultFile) {
		super(aOwner, aName, icon, aCurrentDir, aDefaultFile);
	}
	
	/**
	 * Displays a file open chooser when the action is invoked.
	 * Defaults to the directory set as the current directory
	 * (@see #setCurrentDir).  Only allows the user to open a 
	 * single file, which is recorded as a selected file
	 * (@see #getSelectedFile) if the user presses the approve button.
	 * If no file is selected, the selected file will be null.
	 * 
	 * @param evt
	 *            action invocation
	 */
	public void actionPerformed(ActionEvent evt) {
	
		// clears any previously selected files
		setSelectedFile(null);
		
		// gets, displays, and records the selected file
		// from the chooser
		JFileChooser chooser = getChooser();
		chooser.setCurrentDirectory(getCurrentDir());
		chooser.setSelectedFile(getDefaultFile());
		int result = chooser.showOpenDialog(getOwner());
		if (result == JFileChooser.APPROVE_OPTION) {
			setSelectedFile(chooser.getSelectedFile());
		}
	}
	
}