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

public class Prefs extends JFrame {
	private Preferences prefs = Preferences.userNodeForPackage(TextTrix.class);
	private Preferences fileHist = prefs.node("fileHist");
	private int fileHistCount = 4;
	private static final String FILE_HIST_COUNT = "fileHistCount";
	//private int fileHistIndex = 0;
	private static final String FILE_HIST = "fileHist";
	
	public Prefs() {
		fileHistCount = fileHist.getInt(FILE_HIST_COUNT, 4);
		System.out.println("fileHistCount: " + fileHistCount);
	}
	
	public void storePrefs() {
	}
	/*
	public void storeFileHist(String file) {
		System.out.println("I'm here");
		String[] files = retrieveFileHist();
		//String tmpFile = "";
		boolean shift = false;
		for (int i = 0; i < files.length; i++) {
			// shift the records as necessary to move a potential
			// duplicate to the front of the history
			if (shift) { // shift the records
				files[i - 1] = files[i];
			} else if (files[i].equals(file)) { // find where to start shifting, if necessary
				shift = true;
			}
			System.out.println("files[" + i + "]: " + files[i] + ", file: " + file);
		}
		if (shift) {
			// no need to check that below fileHistCount since merely substituting an entry
			files[files.length - 1] = file;
			int i = 0;
			for (i = 0; i < files.length; i++) {
				fileHist.put(FILE_HIST + i, files[i]);
			}
			fileHistIndex = i++;
		} else {
			if (fileHistIndex >= fileHistCount) {
				for (int i = 1; i < fileHistCount; i++) {
					fileHist.put(FILE_HIST + (i - 1), fileHist.get(FILE_HIST + i, ""));
				}
				fileHistIndex = fileHistCount - 1;
			}
			fileHist.put(FILE_HIST + fileHistIndex++, file);
		}
		

	}
	*/
	
	public void storeFileHist(String file) {
		System.out.println("Storing: " + file);
		String[] files = retrieveFileHist();
		boolean shift = false;
		for (int i = files.length - 1; i >=0; i--) {
			// shift the records as necessary to move a potential
			// duplicate to the front of the history
			if (shift) { // shift the records
				files[i + 1] = files[i];
			} else if (files[i].equals(file)) { // find where to start shifting, if necessary
				shift = true;
			}
			System.out.println("files[" + i + "]: " + files[i] + ", file: " + file);
		}
		if (shift) {
			// no need to check that below fileHistCount since merely substituting an entry
			files[0] = file;
			int i = 0;
			for (i = 0; i < files.length; i++) {
				fileHist.put(FILE_HIST + i, files[i]);
			}
			//fileHistIndex = i++;
		} else {
			//if (files.length >= fileHistCount) {
			System.out.println("I'm here");
			//String tmpFile = file;
			for (int i = fileHistCount - 1; i > 0; i--) {
				//System.out.println("Adding: " + tmpFile);
				fileHist.put(FILE_HIST + i, fileHist.get(FILE_HIST + (i - 1), ""));
				//tmpFile = fileHist.get(FILE_HIST + (i + 1), "");
			}
				//fileHistIndex = fileHistCount - 1;
			//}
		fileHist.put(FILE_HIST + 0, file);
		}
		

	}
	public String[] retrieveFileHist() {
		String[] files = new String[fileHistCount];
		//fileHistIndex = 0;
		for (int i = 0; i < files.length; i++) {
			//fileHistIndex = i + 1;
			files[i] = fileHist.get(FILE_HIST + i, "");
			//System.out.println("files[" + i + "]: " + files[i]);
			if (files[i] == "") {
				return (String[])LibTTx.truncateArray(files, i);
			}
			//fileHistIndex++;
		}
		return files;
	}
	
	
	
	
	
	
	public int getFileHistCount() { 
		return fileHistCount;
	}
}