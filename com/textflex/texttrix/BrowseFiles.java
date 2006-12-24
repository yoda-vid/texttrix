
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
	public class BrowseFiles extends AbstractAction {
		private Component owner;
		private TextPad t = null;
		
		private File selectedFile = null;
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
		public BrowseFiles(Component aOwner, String name, Icon icon) {
			owner = aOwner;
			putValue(Action.NAME, name);
			putValue(Action.SMALL_ICON, icon);
			chooser = new JFileChooser();
			chooser.setApproveButtonText(name);
		}
		public BrowseFiles(Component aOwner, String name, Icon icon, File aCurrentDir, File aDefaultFile) {
			this(aOwner, name, icon);
			currentDir = aCurrentDir;
			defaultFile = aDefaultFile;
		}
		
		public BrowseFiles(Component aOwner, String name, Icon icon, TextPad aTextPad) {
			this(aOwner, name, icon);
			t = aTextPad;
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
			chooser.setCurrentDirectory(getCurrentDir());
			chooser.setSelectedFile(getDefaultFile());
//			TextPad t = getSelectedTextPad();
			if (t != null) {
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
	
				/*
				 * WORKAROUND: call true, false, true on setMultiSelectionEnabled to
				 * ensure that the same file can be opened
				 */
				/*
				chooser.setMultiSelectionEnabled(true);
				chooser.setMultiSelectionEnabled(false);
				chooser.setMultiSelectionEnabled(true);
				String dir = openDir;
				if (t != null && (dir = t.getDir()).equals(""))
					dir = openDir;
				chooser.setCurrentDirectory(new File(dir));
				chooser.setSelectedFile(new File(""));
				// allows one to open multiple files;
				// must disable for save dialog
	
				// displays the dialog and opens all files selected
				boolean repeat = false;
				do {
					int result = chooser.showOpenDialog(owner);
					// bring up the dialog and retrieve the result
					if (result == JFileChooser.APPROVE_OPTION) {
						// Open button
						String msg = "";
						String title = "Couldn't open";
						File[] files = chooser.getSelectedFiles();
						boolean allFound = true;
						for (int i = 0; i < files.length; i++) {
							if (!openFile(files[i]))
								// record unopened files
								msg = msg + files[i] + "\n";
						}
						// request another opportunity to open files if any
						// failures
						if (msg.equals("")) { // no unopened files
							repeat = false;
						} else { // some files left unopened
							// notify the user which files couldn't be opened
							msg = "The following files couldn't be opened:\n" + msg
									+ "Would you like to try again?";
							// request another chance to open them or other files
							repeat = yesNoDialog(owner, msg, title);
						}
						fileHist.start(fileMenu);
						setAutoIndent();
						/*
						 * Original workaround. Utilizes the fact that
						 * getSelectedFiles() returns an array of length 0, while
						 * getSelectedFile() returns the intended file object.
						 *
						/*
						 * if (files.length == 0) { File f1 =
						 * chooser.getSelectedFile();
						 * System.out.println(f1.getPath()); // OLDDO: dialog
						 * informing that the file doesn't exist if (f1 != null) {
						 * openFile(f1); repeat = false; } else { repeat =
						 * yesNoDialog(owner, msg, title); } } else { boolean
						 * allFound = true; for (int i = 0; i < files.length; i++) {
						 * if (!openFile(files[i])) allFound = false; } if
						 * (allFound) { repeat = false; } else { repeat =
						 * yesNoDialog(owner, msg, title); } }
						 *
					} else { // Cancel button
						repeat = false;
					}
				} while (repeat);
				// repeat if failed opens for user to retry
				*/
			} else {
				int result = chooser.showOpenDialog(owner);
				if (result == JFileChooser.APPROVE_OPTION) {
					setSelectedFile(chooser.getSelectedFile());
				}
			}
		}
		
		public void setSelectedFile(File aSelectedFile) {
			selectedFile = aSelectedFile;
		}
		
		public void setCurrentDir(File aCurrentDir) {
			currentDir = aCurrentDir;
		}
		
		public void setDefaultFile(File aDefaultFile) {
			defaultFile = aDefaultFile;
		}
		
		
		
		
		
		
		public File getSelectedFile() {
			return selectedFile;
		}
		
		public File getCurrentDir() {
			return currentDir;
		}
		
		public File getDefaultFile() {
			return defaultFile;
		}
			

	}
