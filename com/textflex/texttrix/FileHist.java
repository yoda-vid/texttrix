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

import java.lang.Runnable;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Worker thread class to update the file history entries.
 * 
 * @author davit
 *  
 */
class FileHist implements Runnable {
	JMenu menu;
	TextTrix ttx;
	int fileHistStart;
	
	public FileHist(TextTrix aTtx) {
		ttx = aTtx;
		menu = ttx.getFileMenu();
		fileHistStart = ttx.getFileHistStart();
	}

	/**
	 * Updates the file history record and menu entries.
	 *  
	 */
	public void run() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				updateFileHist();
			}
		});
	}

	/**
	 * Creates the file history menu entries.
	 * 
	 *  
	 */
	public void createFileHist() {
		// assumes that the file history entries are at the entries in the
		// menu
		String[] files = ttx.getPrefs().retrieveFileHist();
		for (int i = 0; i < files.length; i++) {
			String file = files[i];
			Action fileAction = createFileHistAction(file);
			menu.add(fileAction);
		}
	}

	/**
	 * Creates the actions to add to the history menu.
	 * 
	 * @param file file to open when invoking the action
	 * @return action to open the given file
	 */
	public Action createFileHistAction(final String file) {
		String fileDisp = file;
		int pathLen = file.length();
		if (pathLen > 30) {
			fileDisp = file.substring(0, 10) + "..."
					+ file.substring(pathLen - 15);
		}
		// action to open the file
		Action act = new AbstractAction(fileDisp) {
			public void actionPerformed(ActionEvent evt) {
				ttx.openFile(new File(file), true, false, true, true);
			}
		};
		LibTTx.setAction(act, file); // tool tip displays full file path
		return act;
	}

	/**
	 * Updates the file history menu by deleting old entries and replacing
	 * them with the current ones.
	 *  
	 */
	public void updateFileHist() {
		if (fileHistStart > -1) {
			// remove previous file history entries if they had been
			// previously added
			for (int i = menu.getItemCount() - 1; i >= fileHistStart; i--) {
				menu.remove(i);
			}
		}
		createFileHist();
		menu.revalidate();
	}

}
