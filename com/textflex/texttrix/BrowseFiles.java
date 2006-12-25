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

/**
 * Evokes a open file dialog, from which the user can select a file to
 * display in the currently selected tab's text area. Filters for text
 * files, though provides option to display all files.
 */
public abstract class BrowseFiles extends AbstractAction {
	private Component owner;
	private TextPad textPad = null;
	private String name = "";
	private String openDir = "";
	
	private File selectedFile = null;
	private File[] selectedFiles = null;
	private JFileChooser chooser = null; // file dialog
	private File currentDir = new File("");
	private File defaultFile = new File("");

	/**
	 * Constructs the file open action
	 * 
	 * @param aOwner
	 *            the parent frame
	 * @param name
	 *            the action's name
	 * @param icon
	 *            the action's icon
	 */
	public BrowseFiles(Component aOwner, String aName, Icon icon) {
		owner = aOwner;
		name = aName;
		putValue(Action.NAME, name);
		putValue(Action.SMALL_ICON, icon);
		chooser = new JFileChooser();
		chooser.setApproveButtonText(name);
	}
	
	public BrowseFiles(Component aOwner, String aName, Icon icon, File aCurrentDir, File aDefaultFile) {
		this(aOwner, aName, icon);
		currentDir = aCurrentDir;
		defaultFile = aDefaultFile;
	}
	
	/*
	public BrowseFiles(Component aOwner, String aName, Icon icon, TextPad aTextPad, String aOpenDir) {
		this(aOwner, aName, icon);
		textPad = aTextPad;
		openDir = aOpenDir;
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
	public abstract void actionPerformed(ActionEvent evt);
	
	
	
	
	
	
	
	
	
	public void setSelectedFile(File aSelectedFile) {
		selectedFile = aSelectedFile;
	}
	
	public void setSelectedFiles(File[] aSelectedFiles) {
		selectedFiles = aSelectedFiles;
	}
	
	public void setCurrentDir(File aCurrentDir) {
		currentDir = aCurrentDir;
	}
	
	public void setDefaultFile(File aDefaultFile) {
		defaultFile = aDefaultFile;
	}
	
	public void setChooser(JFileChooser aChooser) {
		chooser = aChooser;
	}
	
	public void setOwner(Component aOwner) {
		owner = aOwner;
	}
	
	public void setName(String aName) {
		name = aName;
	}
	
	public void setTextPad(TextPad aTextPad) {
		textPad = aTextPad;
	}
	
	public void setOpenDir(String aOpenDir) {
		openDir = aOpenDir;
	}
	
	
	
	
	
	
	public File getSelectedFile() {
		return selectedFile;
	}
	
	public File[] getSelectedFiles() {
		return selectedFiles;
	}
	
	public File getCurrentDir() {
		return currentDir;
	}
	
	public File getDefaultFile() {
		return defaultFile;
	}
	
	public JFileChooser getChooser() {
		return chooser;
	}
	
	public Component getOwner() {
		return owner;
	}
	
	public String getName() {
		return name;
	}
	
	public TextPad getTextPad() {
		return textPad;
	}
	
	public String getOpenDir() {
		return openDir;
	}
		

}
