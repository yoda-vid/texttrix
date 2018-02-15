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
 * Portions created by the Initial Developer are Copyright (C) 2006-7, 2018
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

/**
 * An abstract class for opening a file browser window,  from which 
 * the user can select a file or files.  Subclasses should manage what
 * to do with the selected files and how to filters for specific file types.
 */
public abstract class BrowseFiles extends AbstractAction {
	private Component owner; // the owner for placement of the chooser
	private TextPad textPad = null; // the Text Pad
	private String name = ""; // name of the dialog and accept button
//	private String openDir = ""; // the last opened directory
	private boolean saveDialog = false;
	
	private File selectedFile = null; // user-chosen file
	private File[] selectedFiles = null; // user-chosen files
	private JFileChooser chooser = null; // file dialog
	private File currentDir = new File(""); // current directory
	private File defaultFile = new File(""); // default file to select

	/** Creates a file browser.
	 * @param aOwner the chooser will be centered on this owner; if null, the chooser
	 * is placed at the center of the screen
	 * @param aName the file browser and accept button will use this name
	 * @param icon the icon for the file browser; apparently not used
	 * @param aChooser a file chooser; if null, a new chooser will be created wtih the 
	 * approve button set to <code>name</code>
	 */
	public BrowseFiles(Component aOwner, String aName, Icon icon, JFileChooser aChooser) {
		owner = aOwner;
		name = aName;
		putValue(Action.NAME, name);
		putValue(Action.SMALL_ICON, icon);
		chooser = aChooser;
		if (chooser == null) {
			chooser = new JFileChooser();
			chooser.setApproveButtonText(name);
		}
	}
	
	/** Creates a file browser.
	 * A generic chooser will be created.
	 * @param aOwner the chooser will be centered on this owner; if null, the chooser
	 * is placed at the center of the screen
	 * @param aName the file browser and accept button will use this name
	 * @param icon the icon for the file browser; apparently not used
	 */
	public BrowseFiles(Component aOwner, String aName, Icon icon) {
		this(aOwner, aName, icon, null);
	}
	
	/** Creates a file browser.
	 * @param aOwner the chooser will be centered on this owner; if null, the chooser
	 * is placed at the center of the screen
	 * @param aName the file browser and accept button will use this name
	 * @param icon the icon for the file browser; apparently not used
	 * @param aCurrentDir a directory that the file chooser can open to
	 * @param aDefaultFile a file that the chooser can automatically try to select
	 * @param aChooser a file chooser; if null, a new chooser will be created wtih the 
	 * approve button set to <code>name</code>
	 */
	public BrowseFiles(Component aOwner, String aName, Icon icon, 
		File aCurrentDir, File aDefaultFile, JFileChooser aChooser) {
		
		this(aOwner, aName, icon, aChooser);
		currentDir = aCurrentDir;
		defaultFile = aDefaultFile;
	}
	
	/** Creates a file browser.
	 * A generic chooser will be created.
	 * @param aOwner the chooser will be centered on this owner; if null, the chooser
	 * is placed at the center of the screen
	 * @param aName the file browser and accept button will use this name
	 * @param icon the icon for the file browser; apparently not used
	 * @param aCurrentDir a directory that the file chooser can open to
	 * @param aDefaultFile a file that the chooser can automatically try to select
	 */
	public BrowseFiles(Component aOwner, String aName, Icon icon, File aCurrentDir, File aDefaultFile) {
		this(aOwner, aName, icon, aCurrentDir, aDefaultFile, null);
	}
	
	/**
	 * Performs the action, usually opening a file chooser and
	 * allowing the user to select one or more files.
	 * 
	 * @param evt
	 *            action invocation
	 */
	public abstract void actionPerformed(ActionEvent evt);
	
	
	
	
	
	
	
	
	/** Sets the selected file, usually the first file that
	 * the user selected.
	 * @param aSelectedFile a file to save as the user-selected file
	 */
	public void setSelectedFile(File aSelectedFile) {
		selectedFile = aSelectedFile;
	}
	
	/** Sets the selected files, usually useful when mult-file
	 * selection is enabled in the chooser
	 * @param aSelectedFiles the files to save as the user-selected file
	 */
	public void setSelectedFiles(File[] aSelectedFiles) {
		selectedFiles = aSelectedFiles;
	}
	
	/** Sets the current directory so that the chooser can reference
	 * a directory to open to by default.
	 * @param aCurrentDir a directory
	 */
	public void setCurrentDir(File aCurrentDir) {
		currentDir = aCurrentDir;
	}
	
	/** Sets the default file so that the chooser can enter a default
	 * file for the user to select.
	 * @param aDefaultFile a file
	 */
	public void setDefaultFile(File aDefaultFile) {
		defaultFile = aDefaultFile;
	}
	
	/** Sets the file chooser.
	 * Choosers with pre-embedded file filters or approve button
	 * names can be set here.
	 * @param aChooser a chooser
	 */
	public void setChooser(JFileChooser aChooser) {
		chooser = aChooser;
	}
	
	/** Sets the owner, which is usually the component that
	 * the file chooser is centered on.
	 * @param aOwner a component
	 */
	public void setOwner(Component aOwner) {
		owner = aOwner;
	}
	
	/** Sets the name.
	 * @param aName the name
	 */	
	public void setName(String aName) {
		name = aName;
	}
	
	/** Sets a {@link TextPad} for reference, such as file paths.
	 * @param aTextPad a Text Pad
	 */
	public void setTextPad(TextPad aTextPad) {
		textPad = aTextPad;
	}
	
	/**
	 * Sets the flag for whether to open a "save" rather than an "open" file 
	 * dialog
	 * 
	 * @param val true if a "save" dialog should be opened; false for an "open" 
	 * file dialog
	 */
	public void setSaveDialog(boolean val) {
		saveDialog = val;
	}
	
	
	
	
	
	/** Gets the selected file, usually the first file that
	 * the user selected.
	 * @return a file to save as the user-selected file
	 */
	public File getSelectedFile() {
		return selectedFile;
	}
	
	/** Gets the selected files, usually useful when mult-file
	 * selection is enabled in the chooser
	 * @return the files to save as the user-selected file
	 */
	public File[] getSelectedFiles() {
		return selectedFiles;
	}
	
	/** Gets the current directory so that the chooser can reference
	 * a directory to open to by default.
	 * @return a directory
	 */
	public File getCurrentDir() {
		return currentDir;
	}
	
	/** Gets the default file so that the chooser can enter a default
	 * file for the user to select.
	 * @return a file
	 */
	public File getDefaultFile() {
		return defaultFile;
	}
	
	/** Sets the file chooser.
	 * Choosers with pre-embedded file filters or approve button
	 * names can be set here.
	 * @return the chooser
	 */
	public JFileChooser getChooser() {
		return chooser;
	}
	
	/** Gets the owner, which is usually the component that
	 * the file chooser is centered on.
	 * @return a component
	 */
	public Component getOwner() {
		return owner;
	}
	
	/** Gets the name.
	 * @return the name
	 */	
	public String getName() {
		return name;
	}
	
	/** Gets a {@link TextPad} for reference, such as file paths.
	 * @return a Text Pad
	 */
	public TextPad getTextPad() {
		return textPad;
	}
	
	/**
	 * Gets the flag for whether to open a "save" rather than an "open" file 
	 * dialog
	 * 
	 * @return true if a "save" dialog should be opened; false for an "open" 
	 * file dialog
	 */
	public boolean getSaveDialog() {
		return saveDialog;
	}

}
