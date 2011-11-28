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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

import javax.swing.Icon;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.Thread;

/**
 * Evokes a open file dialog, from which the user can select a file to
 * display in the currently selected tab's text area. Filters for text
 * files, though provides option to display all files.
 */
class FileOpenAction extends BrowseFilesFromTextPad {
	
	private TextTrix ttx;

	/**
	 * Constructs the file open action
	 * 
	 * @param aOwner the parent frame
	 * @param aName the action's name
	 * @param aIcon the action's icon
	 */
	public FileOpenAction(TextTrix aTtx, String aName, Icon aIcon) {
		super(aTtx, aName, aIcon, aTtx.getChooser());
		ttx = aTtx;
	}

	/**
	 * Displays a file open chooser when the action is invoked. Defaults to
	 * the directory from which the last file was opened or, if no files
	 * have been opened, to the user's home directory.
	 * 
	 * @param evt
	 *            action invocation
	 */
	public void actionPerformed(ActionEvent evt) {
		
		setTextPad(ttx.getSelectedTextPad());
		setCurrentDir(new File(ttx.getOpenDir()));

		// displays the dialog and opens all files selected
		boolean repeat = false;
		do {
			super.actionPerformed(evt);
			String msg = "";
			File[] files = getSelectedFiles();
			// bring up the dialog and retrieve the result
			if (files != null) {
				
				msg = ttx.openFiles(files, 0, false);
				
				// request another opportunity to open files if any
				// failures
				if (msg.equals("")) { // no unopened files
					repeat = false;
				} else { // some files left unopened
					// notify the user which files couldn't be opened
					String title = "Couldn't open";
					msg = "The following files couldn't be opened:\n" + msg
							+ "Would you like to try again?";
					// request another chance to open them or other files
					repeat = LibTTx.yesNoDialog(getOwner(), msg, title);
				}
				//ttx.getFileHist().start(ttx.getFileMenu());
				Thread fileHistThread = new Thread(new FileHist(ttx));
				fileHistThread.start();
				ttx.setAutoIndent();
			} else { // Cancel button
				repeat = false;
			}
		} while (repeat); // repeat if failed opens for user to retry
	}

}
