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
import java.lang.Thread;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledEditorKit;
import java.awt.EventQueue;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.KeyStroke;

/**
 * Creates the menu bar and its associated tool bar through a worker thread
 * to build concurrently with other processes. No other method should rely
 * upon the components that this class creates to be available until
 * sufficient time after the thread starts.
 * 
 * @author davit
 */
class MenuBarCreator implements Runnable {
	
	private TextTrix ttx;

	public MenuBarCreator(TextTrix aTtx) {
		ttx = aTtx;
	}

	/**
	 * Begins creating the bars.
	 *  
	 */
	public void start() {
		(new Thread(this, "thread")).start();
	}

	/**
	 * Performs the menu and associated bars' creation.
	 *  
	 */
	public void run() {
		// start creating the components after others methods that might use
		// the components have finalized their tasks
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				//System.out.println("Creating the menu bar...");

				/* Shortcuts */

				// Standard keybindings
				
				char fileMenuMnemonic = 'F'; // file menu
				
				char newActionMnemonic = 'N'; // new file
				KeyStroke newActionShortcut = KeyStroke
						.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
						
				char newWindowActionMnemonic = 'N'; // new file
				KeyStroke newWindowActionShortcut = KeyStroke
						.getKeyStroke("ctrl shift N");
						
				char newGroupActionMnemonic = 'G'; // tab group
				KeyStroke newGroupActionShortcut = KeyStroke
						.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
						
				char openActionMnemonic = 'C'; // open file
				KeyStroke openActionShortcut = KeyStroke
						.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
						
				char closeActionMnemonic = 'C'; // close file
				KeyStroke closeActionShortcut = KeyStroke
						.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
						
				char closeGroupActionMnemonic = 'R'; // close tab group
				KeyStroke closeGroupActionShortcut = KeyStroke
						.getKeyStroke("ctrl shift W");
						
				String exitActionTxt = "Exit"; // exit Text Trix
				char exitActionMnemonic = 'X';
				
				KeyStroke exitActionShortcut = KeyStroke // exit
						.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
						
				KeyStroke undoActionShortcut = KeyStroke // undo
						.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
						
				KeyStroke redoActionShortcut = KeyStroke // redo
						.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
						
				char cutActionMnemonic = 'T'; // cut
				KeyStroke cutActionShortcut = KeyStroke
						.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
						
				char copyActionMnemonic = 'C'; // copy
				KeyStroke copyActionShortcut = KeyStroke
						.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
				
				char pasteActionMnemonic = 'P'; // paste
				KeyStroke pasteActionShortcut = KeyStroke
						.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
						
				char selectAllActionMnemonic = 'A'; // select all
				KeyStroke selectAllActionShortcut = KeyStroke
						.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
						
				char printActionMnemonic = 'P'; // print
				KeyStroke printActionShortcut = KeyStroke
						.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
						
				char printPreviewActionMnemonic = 'R'; // print preview
				char printSettingsActionMnemonic = 'I'; // print settings

				char boldActionMnemonic = 'B'; // print
				KeyStroke boldActionShortcut = KeyStroke
						.getKeyStroke("ctrl B");
						
				char italicActionMnemonic = 'I'; // italic
				KeyStroke italicActionShortcut = KeyStroke
						.getKeyStroke("ctrl I");
						
				char underlineActionMnemonic = 'U'; // underline
				KeyStroke underlineActionShortcut = KeyStroke
						.getKeyStroke("ctrl I");
						



				// Alternate keybindings: shortcuts added to and with
				// preference over the standard shortcuts
				// TODO: add Mac-sytle shortcuts
				if (ttx.getPrefs().isHybridKeybindings()) {
				
				
					// Hybrid: standard + Emacs for single char and line
					// navigation
					fileMenuMnemonic = 'I';
					newActionMnemonic = 'T';
					newActionShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
					exitActionTxt = "Exit";
					exitActionMnemonic = 'X';
					exitActionShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
					selectAllActionMnemonic = 'L';
					selectAllActionShortcut = KeyStroke
							.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
					printActionShortcut = KeyStroke
							.getKeyStroke("ctrl shift P");
					boldActionShortcut = KeyStroke
						.getKeyStroke("ctrl shift B");
					italicActionShortcut = KeyStroke
						.getKeyStroke("ctrl shift I");
					underlineActionShortcut = KeyStroke
						.getKeyStroke("ctrl shift U");
						
				} else if (ttx.getPrefs().isEmacsKeybindings()) {
				
				
					//						System.out.println("applying emacs shortcuts");
					// Emacs: Hybrid + Emacs single-key shortcuts
					// TODO: create double-key shortcuts, such as ctrl-x,
					// ctrl-s for saving
					fileMenuMnemonic = 'I';
					newActionMnemonic = 'T';
					newActionShortcut = KeyStroke.getKeyStroke("ctrl T");
					closeActionMnemonic = 'K';
					closeActionShortcut = KeyStroke.getKeyStroke("ctrl K");
					exitActionTxt = "Exit";
					exitActionMnemonic = 'X';
					redoActionShortcut = KeyStroke.getKeyStroke("ctrl R");
					cutActionShortcut = KeyStroke.getKeyStroke("ctrl W");
					copyActionShortcut = KeyStroke.getKeyStroke("alt W");
					pasteActionShortcut = KeyStroke.getKeyStroke("ctrl Y");
					exitActionShortcut = KeyStroke.getKeyStroke("ctrl Q");
					selectAllActionMnemonic = 'L';
					selectAllActionShortcut = KeyStroke
							.getKeyStroke("ctrl L");
					printActionShortcut = KeyStroke
							.getKeyStroke("ctrl shift P");
				}








				/* Create new menu and tool bars */

				// remove the old components if necessary
				JMenuBar menuBar = ttx.getJMenuBar();
				JToolBar toolBar = ttx.getToolBar();
				if (menuBar != null) {
					ttx.getContentPane().remove(menuBar);
					ttx.getContentPane().remove(toolBar);
					ttx.resetMenus(); // resets file and view menus
				}

				// make menu bar and menus
				menuBar = new JMenuBar();
				JMenu fileMenu = ttx.getFileMenu();
				fileMenu.setMnemonic(fileMenuMnemonic);
				JMenu editMenu = new JMenu("Edit");
				editMenu.setMnemonic('E');
				JMenu viewMenu = ttx.getViewMenu();
				viewMenu.setMnemonic('V');
				JMenu formatMenu = new JMenu("Format");
				formatMenu.setMnemonic('F');
				JMenu trixMenu = new JMenu("Trix");
				ttx.setTrixMenu(trixMenu);
				trixMenu.setMnemonic('T');
				JMenu toolsMenu = new JMenu("Tools");
				ttx.setToolsMenu(toolsMenu);
				toolsMenu.setMnemonic('O');
				JMenu helpMenu = new JMenu("Help");
				helpMenu.setMnemonic('H');

				// make tool bar
				toolBar = new JToolBar("Trix and Tools");
				ttx.setToolBar(toolBar);
				toolBar.setBorderPainted(false);

				// create pop-up menu for right-mouse-clicking
				JPopupMenu popup = new JPopupMenu();
				ttx.setPopup(popup);
				JPopupMenu tabsPopup = new JPopupMenu();
				ttx.setTabsPopup(tabsPopup);
				ttx.getGroupTabbedPane().addMouseListener(
						new TabsPopupListener(tabsPopup));









				/* File menu items */

				// make new tab and text area
				Action newAction = new AbstractAction("New tab", 
						LibTTx.makeIcon("images/newtabicon-16x16.png")) {
					public void actionPerformed(ActionEvent evt) {
						ttx.addTextArea(ttx.getSelectedTabbedPane(), 
								ttx.makeNewFile());
					}
				};
				LibTTx.setAcceleratedAction(newAction, "New tab",
						newActionMnemonic, newActionShortcut);
				fileMenu.add(newAction);
				
				// toolbar version
				JButton newTabButton = toolBar.add(newAction);
				newTabButton.setBorderPainted(false);
				LibTTx.setRollover(newTabButton,
						"images/newtabicon-roll-16x16.png");




				// make a new window
				Action newWindowAction = new AbstractAction("New window") {
					public void actionPerformed(ActionEvent evt) {
						ttx.setFresh(true);
						ttx.openTTXWindow(null);
						ttx.setFresh(false);
					}
				};
				LibTTx.setAcceleratedAction(newWindowAction, "New window",
						newWindowActionMnemonic, newWindowActionShortcut);
				fileMenu.add(newWindowAction);




				// (ctrl-o) open file; use selected tab if empty
				// file menu version
				Action openAction = new FileOpenAction(ttx,
						"Open", LibTTx
								.makeIcon("images/openicon-roll-16x16.png"));
				LibTTx.setAcceleratedAction(openAction, "Open", openActionMnemonic,
						openActionShortcut);
				fileMenu.add(openAction);

				// toolbar version
				Action openActionForBtn = new FileOpenAction(ttx,
						"Open", LibTTx
								.makeIcon("images/openicon-16x16.png"));
				LibTTx.setAction(openActionForBtn, "Open file(s)", 'O');
				JButton openButton = toolBar.add(openActionForBtn);
				openButton.setBorderPainted(false);
				LibTTx.setRollover(openButton,
						"images/openicon-roll-16x16.png");



				
					
				
				
				
				
				


				// Close file; check if saved
				// file menu version
				Action closeAction = new FileCloseAction("Close", LibTTx
						.makeIcon("images/closeicon-16x16.png"));
				LibTTx.setAcceleratedAction(closeAction, "Close",
						closeActionMnemonic, closeActionShortcut);
				fileMenu.add(closeAction);
				
				// toolbar version
				/*
				Action closeActionForBtn = new FileCloseAction("Close",
						LibTTx.makeIcon("images/closeicon-16x16.png"));
				LibTTx.setAction(closeActionForBtn, "Close file",
						closeActionMnemonic);
				JButton closeButton = toolBar.add(closeActionForBtn);
				*/
				JButton closeButton = toolBar.add(closeAction);
				closeButton.setBorderPainted(false);
				LibTTx.setRollover(closeButton,
						"images/closeicon-roll-16x16.png");






				// make new tab group
				Action newGroupAction = new AbstractAction("New tab group") {
					public void actionPerformed(ActionEvent evt) {
						ttx.addTabbedPane(ttx.getGroupTabbedPane(), "");
					}
				};
				LibTTx.setAcceleratedAction(newGroupAction, "New tab group",
						newGroupActionMnemonic, newGroupActionShortcut);
				fileMenu.add(newGroupAction);


				// close tab group
				Action closeGroupAction = new AbstractAction("Close tab group") {
					public void actionPerformed(ActionEvent evt) {
						if (LibTTx.yesNoDialog(
							ttx,
							"You are about to close the tab group and all its tabs."
								+ "\nAre you sure you want to continue?",
							"Close tab group?")) {
							ttx.removeTabbedPane(ttx.getGroupTabbedPane());
						}
					}
				};
				LibTTx.setAcceleratedAction(closeGroupAction, "Close tab group",
						closeGroupActionMnemonic, closeGroupActionShortcut);
				fileMenu.add(closeGroupAction);



				// (ctrl-s) save file; no dialog if file already created
				Action saveAction = new AbstractAction("Save", LibTTx
						.makeIcon("images/saveicon-16x16.png")) {
					public void actionPerformed(ActionEvent evt) {
						TextPad t = ttx.getSelectedTextPad(); //null;
						// can't use getSelectedTabbedPane().getSelectedComponent() b/c
						// returns JScrollPane;
						// check if pad exists
						if (t != null) {
							// Check if file exists
							if (t.fileExists()) {
								// file exists, so attempt to save to file path
								if (!ttx.saveFile(t.getPath(), null)) {
									// error dialog to user if can't save, for 
									// whatever reason
									String msg = t.getPath()
											+ " couldn't be written.\n"
											+ "Would you like to try saving it somewhere else?";
									String title = "Couldn't write";
									if (LibTTx.yesNoDialog(ttx, msg,
											title))
										ttx.fileSaveDialog(t, ttx);
								}
								
							} else {
								// otherwise, request filename for new file
								ttx.fileSaveDialog(t, ttx);
							}
						}
					}
				};
				LibTTx.setAcceleratedAction(saveAction, "Save file", 'S',
						KeyStroke.getKeyStroke(KeyEvent.VK_S, 
							Toolkit.getDefaultToolkit()
								.getMenuShortcutKeyMask()));
				fileMenu.add(saveAction);
				JButton saveButton = toolBar.add(saveAction);
				saveButton.setBorderPainted(false);
				LibTTx.setRollover(saveButton,
						"images/saveicon-roll-16x16.png");









				// Tool Bar: begin plug-ins
				toolBar.addSeparator();

				// save w/ file save dialog
				Action saveAsAction = new FileSaveAction(ttx,
						"Save as...", LibTTx
								.makeIcon("images/saveasicon-16x16.png"));
				LibTTx.setAction(saveAsAction, "Save as...", '.');
				fileMenu.add(saveAsAction);
				
				






				// Menu: begin print entries
				fileMenu.addSeparator();
				
				// Print action
				Action printAction = new AbstractAction("Print...") {
					public void actionPerformed(ActionEvent e) {
						ttx.printTextPad();
					}
				};
				LibTTx.setAcceleratedAction(printAction, "Print...",
						printActionMnemonic, printActionShortcut);
				fileMenu.add(printAction);
				
				// Print preview action
				Action printPreviewAction = new AbstractAction(
						"Print preview...") {
					public void actionPerformed(ActionEvent e) {
						ttx.printPreview();
					}
				};
				LibTTx.setAction(printAction, "Print...",
						printPreviewActionMnemonic);
				fileMenu.add(printPreviewAction);
				
				// Printer settings action
				Action printSettingsAction = new AbstractAction(
						"Print settings...") {
					public void actionPerformed(ActionEvent e) {
						ttx.printTextPadSettings();
					}
				};
				LibTTx.setAction(printSettingsAction, "Print settings...",
						printSettingsActionMnemonic);
				fileMenu.add(printSettingsAction);

				// Menu: begin exit entries
				fileMenu.addSeparator();

				// exit file; close each tab separately, checking for saves
				Action exitAction = new AbstractAction(exitActionTxt) {
					public void actionPerformed(ActionEvent evt) {
						ttx.exitTextTrix();
					}
				};
				// Doesn't work if close all tabs unless click ensure window
				// focused,
				// such as clicking on menu
				LibTTx.setAcceleratedAction(exitAction, exitActionTxt,
						exitActionMnemonic, exitActionShortcut);
				fileMenu.add(exitAction);

				fileMenu.addSeparator();
				//System.out.println("About to create the menu entries");

				
				
				
				
				/* Edit menu items */

				// (ctrl-z) undo; multiple undos available
				Action undoAction = new AbstractAction("Undo") {
					public void actionPerformed(ActionEvent evt) {
						((TextPad) ttx.getSelectedTextPad()).undo();
					}
				};
				LibTTx.setAcceleratedAction(undoAction, "Undo", 'U',
					undoActionShortcut);
				editMenu.add(undoAction);

				// redo; multiple redos available
				Action redoAction = new AbstractAction("Redo") {
					public void actionPerformed(ActionEvent evt) {
						((TextPad) ttx.getSelectedTextPad()).redo();
					}
				};
				LibTTx.setAcceleratedAction(redoAction, "Redo", 'R',
						redoActionShortcut);
				editMenu.add(redoAction);

				// Begins Cut, Copy, Paste entries;
				// create here instead of within TextPad so can use as
				// menu entries;
				/*
				 * The JVM apparently overrides these shortcus with its own
				 * when the standard keys map to them. For example, when
				 * using ctrl-V for paste, the JVM seems to call the paste
				 * mechanism directly rather than following code from the
				 * pasteAction, below.
				 *  
				 */
				editMenu.addSeparator();

				// cut
				Action cutAction = new AbstractAction("Cut") {
					public void actionPerformed(ActionEvent evt) {
						((TextPad) ttx.getSelectedTextPad()).cut();
					}
				};
				LibTTx.setAcceleratedAction(cutAction, "Cut",
						cutActionMnemonic, cutActionShortcut);
				editMenu.add(cutAction);
				popup.add(cutAction);

				// copy
				Action copyAction = new AbstractAction("Copy") {
					public void actionPerformed(ActionEvent evt) {
						((TextPad) ttx.getSelectedTextPad()).copy();
					}
				};
				LibTTx.setAcceleratedAction(copyAction, "Copy",
						copyActionMnemonic, copyActionShortcut);
				editMenu.add(copyAction);
				popup.add(copyAction);

				// paste
				Action pasteAction = new AbstractAction("Paste") {
					public void actionPerformed(ActionEvent evt) {
						TextPad t = ttx.getSelectedTextPad();
						t.paste();
					}
				};
				LibTTx.setAcceleratedAction(pasteAction, "Paste",
						pasteActionMnemonic, pasteActionShortcut);
				editMenu.add(pasteAction);
				popup.add(pasteAction);

				// Start selection items
				editMenu.addSeparator();

				// select all text in current text area
				Action selectAllAction = new AbstractAction("Select all") {
					public void actionPerformed(ActionEvent evt) {
						((TextPad) ttx.getSelectedTextPad()).selectAll();
					}
				};
				LibTTx.setAcceleratedAction(selectAllAction, "Select all",
						selectAllActionMnemonic, selectAllActionShortcut);
				editMenu.add(selectAllAction);
				popup.add(selectAllAction);

				// edit menu preferences separator
				editMenu.addSeparator();
								
				// group tab title
				Action chgGrpTabTitleAction = new AbstractAction(
						"Change group tab title...") {
					public void actionPerformed(ActionEvent evt) {
						// opens a dialog pane initialized with current
						// group tab title
						int i = ttx.getGroupTabbedPane().getSelectedIndex();
						String title = JOptionPane.showInputDialog(
							ttx, 
							"What would you like to name the tab group?", 
							ttx.getGroupTabbedPane().getTitleAt(i));
						
						// sets the tab title
						if (title != null) {
							ttx.getGroupTabbedPane().setTitleAt(
								ttx.getGroupTabbedPane().getSelectedIndex(), title);
						}
					}
				};
				LibTTx.setAcceleratedAction(chgGrpTabTitleAction, 
						"Change group tab title...", 'H',
						KeyStroke.getKeyStroke("ctrl shift G"));
				editMenu.add(chgGrpTabTitleAction);
				tabsPopup.add(chgGrpTabTitleAction);

				// auto-indent (menu item; toolbar button is below)
				Action autoIndentAction = 
						getAutoIndentAction(
							"images/wrapindenticon-roll-16x16.png", false);
				JCheckBoxMenuItem autoIndent = 
						new JCheckBoxMenuItem(autoIndentAction);
				ttx.setAutoIndentJCheckBox(autoIndent);
				editMenu.add(autoIndent);
				
				// Add the toolbar button later, after the navigation buttons
				
				
				
				
				
				
				
				
				
				// Preferences panel starter;
				// also reloads the plug-ins
				Action prefsAction = new AbstractAction(
						"It's your preference...") {
					public void actionPerformed(ActionEvent evt) {
						ttx.refreshPlugInsPanel();
						ttx.getPrefs().setVisible(true); //show();
					}
				};
				LibTTx.setAction(prefsAction, "It's your preference...",
						'Y');
				editMenu.add(prefsAction);						
			
				
				/* View menu items */

				/*
				 * Tab switching attempts to combine several elements of web
				 * browser behavior. Users can cycle through the tabs in the
				 * order in which they were created by using the Ctrl-]/[
				 * key combinations, similar to the tab cycling in the
				 * Mozilla browser. Occasionally the user will open a group
				 * of files but want to switch among only a particular
				 * subset of them. Web browsers' "Back" and "Forward"
				 * buttons become useful here. By first clicking on the
				 * desired sequence of tabs, the order becomes stored in the
				 * Text Trix history. To traverse up and down that history,
				 * the user can use the Ctrl-Shift-]\[ shortcut keys.
				 * 
				 * The default Java key-bindings for tab switchin no longer
				 * apply since the focus switches automatically to the newly
				 * selected tab.
				 */

				// (ctrl-shift-[) switch back in the tab history
				Action backTabAction = new AbstractAction(
					"Back",
					LibTTx.makeIcon("images/backicon-16x16.png")) {
					
					/*
					 * Switch back only up through the first record and keep
					 * from recording the past selected tabs as newly
					 * selected one. The current index always refers to the
					 * next available position to add selections, while the
					 * previous index refers to the current selection. To go
					 * back, the value at two positions back must be
					 * checked.
					 */
					public void actionPerformed(ActionEvent evt) {
						MotherTabbedPane pane = ttx.getSelectedTabbedPane();
						int backIdx = pane.goBackward();
						if (backIdx != -1 
							&& backIdx < pane.getTabCount()
							&& backIdx != pane.getSelectedIndex()) {
							ttx.setUpdateTabIndexHistory(false);
							pane.setSelectedIndex(backIdx);
						}
					}
				};
				LibTTx.setAcceleratedAction(backTabAction, "Go to previously visited tab", 'B',
						KeyStroke.getKeyStroke("ctrl shift OPEN_BRACKET"));
				viewMenu.add(backTabAction);
				
				// Back toolbar button
				JButton backButton = toolBar.add(backTabAction);
				backButton.setBorderPainted(false);
				LibTTx.setRollover(backButton,
						"images/backicon-roll-16x16.png");






				// (ctrl-shift-]) switch forwared in the tab history
				Action forwardTabAction = new AbstractAction(
					"Forward",
					LibTTx.makeIcon("images/forwardicon-16x16.png")) {
					
					public void actionPerformed(ActionEvent evt) {
						MotherTabbedPane pane = ttx.getSelectedTabbedPane();
						int forwardIdx = pane.goForward();
						if (forwardIdx != -1 
							&& forwardIdx < pane.getTabCount()
							&& forwardIdx != pane.getSelectedIndex()) {
//							System.out.println("here");
							ttx.setUpdateTabIndexHistory(false);
							pane.setSelectedIndex(forwardIdx);
						}
					}
				};
				LibTTx.setAcceleratedAction(forwardTabAction, "Go to the next visited tab",
						'F', KeyStroke.getKeyStroke("ctrl shift "
								+ "CLOSE_BRACKET"));
				viewMenu.add(forwardTabAction);
				
				// Forward toolbar button
				JButton forwardButton = toolBar.add(forwardTabAction);
				forwardButton.setBorderPainted(false);
				LibTTx.setRollover(forwardButton,
						"images/forwardicon-roll-16x16.png");
				
				
				
				
				
				

				// (ctrl-[) switch to the preceding tab
				Action prevTabAction = new AbstractAction("Preceeding tab") {
					public void actionPerformed(ActionEvent evt) {
						int tab = ttx.getSelectedTabbedPane().getSelectedIndex();
						if (tab > 0) {
							ttx.getSelectedTabbedPane()
								.setSelectedIndex(tab - 1);
						} else if (tab == 0) {
							ttx.getSelectedTabbedPane()
								.setSelectedIndex(ttx.getSelectedTabbedPane()
									.getTabCount() - 1);
						}
					}
				};
				LibTTx.setAcceleratedAction(prevTabAction,
						"Preeceding tab", 'P', KeyStroke
								.getKeyStroke("ctrl OPEN_BRACKET"));
				viewMenu.add(prevTabAction);

				// (ctrl-]) switch to the next tab
				Action nextTabAction = new AbstractAction("Next tab") {
					public void actionPerformed(ActionEvent evt) {
						int tab = 
							ttx.getSelectedTabbedPane().getSelectedIndex();
						if ((tab != -1)
								&& (tab == ttx.getSelectedTabbedPane()
									.getTabCount() - 1)) {
							ttx.getSelectedTabbedPane().setSelectedIndex(0);
						} else if (tab >= 0) {
							ttx.getSelectedTabbedPane()
								.setSelectedIndex(tab + 1);
						}
					}
				};
				LibTTx.setAcceleratedAction(nextTabAction, "Next tab", 'N',
						KeyStroke.getKeyStroke("ctrl CLOSE_BRACKET"));
				viewMenu.add(nextTabAction);

				// (ctrl-]) switch to the next tab
				Action refreshTabAction = new AbstractAction("Refresh tab") {
					public void actionPerformed(ActionEvent evt) {
						ttx.refreshTab();
					}
				};
				LibTTx.setAcceleratedAction(refreshTabAction, "Refresh tab", 'R',
						KeyStroke.getKeyStroke("F5"));
				viewMenu.add(refreshTabAction);
				
				// Start pad view types
				viewMenu.addSeparator();

				// view as plain text
				Action togglePlainViewAction = new AbstractAction(
						"Toggle plain text view") {
					public void actionPerformed(ActionEvent evt) {
						ttx.viewPlain();
					}
				};
				LibTTx.setAction(togglePlainViewAction,
						"View as plain text", 'A');
				viewMenu.add(togglePlainViewAction);

				// view as HTML formatted text
				Action toggleHTMLViewAction = new AbstractAction(
						"Toggle HTML view") {
					public void actionPerformed(ActionEvent evt) {
						ttx.switchToHTMLView(null, false);
					}
				};
				LibTTx.setAction(toggleHTMLViewAction, "View as HTML", 'H');
				viewMenu.add(toggleHTMLViewAction);

				// view as RTF formatted text
				Action toggleRTFViewAction = new AbstractAction(
						"Toggle RTF view") {
					public void actionPerformed(ActionEvent evt) {
						ttx.viewRTF();
					}
				};
				LibTTx.setAction(toggleRTFViewAction, "View as RTF", 'T');
				viewMenu.add(toggleRTFViewAction);
				
				
				// Start view menu navigational aids
				viewMenu.addSeparator();
				
				// Line Dance action
				Action lineDanceViewAction = new AbstractAction(
						"Line Dance...", LibTTx.makeIcon("images/linedance.png")) {
					public void actionPerformed(ActionEvent evt) {
						// Line Dance for the current Text Pad
						LineDanceDialog lineDanceDialog = 
							ttx.getLineDanceDialog();
						if (lineDanceDialog == null) {
							lineDanceDialog = new LineDanceDialog(ttx);
							ttx.setLineDanceDialog(lineDanceDialog);
						}
						lineDanceDialog.updatePadPanel();
						lineDanceDialog.setVisible(true);
					}
				};
				LibTTx.setAction(lineDanceViewAction, "Line Dance", 'L');
				viewMenu.add(lineDanceViewAction);
				
				// Line Dance toolbar button
				JButton lineDanceButton = toolBar.add(lineDanceViewAction);
				lineDanceButton.setBorderPainted(false);
				LibTTx.setRollover(lineDanceButton,
						"images/linedance-roll.png");
				String lineDanceDetailedDesc = LibTTx.readText("desc-linedance.html");
				if (lineDanceDetailedDesc != null) {
					lineDanceButton.setToolTipText(lineDanceDetailedDesc);
				}

				
				
				// Add the Wrap Indent toolbar button, whose action
				// was created earlier
				/*
				Action autoIndentActionForBtn = new AbstractAction("Wrap indent",
						LibTTx.makeIcon("images/wrapindenticon-16x16.png"));
				*/
				Action autoIndentActionForBtn = getAutoIndentAction(
						"images/wrapindenticon-16x16.png", true);
				JButton autoIndentButton = toolBar.add(autoIndentActionForBtn);
				autoIndentButton.setBorderPainted(false);
				LibTTx.setRollover(autoIndentButton,
						"images/wrapindenticon-roll-16x16.png");

				
				
				// adds action to view menu
				viewMenu.add(ttx.getLineSaverAction());
				
				// tool bar separator before format options
				toolBar.addSeparator();
				
				
				
				
				/* Format menu items */

				// Bold operation
				// (ctrl-B) Abreviation of keyboard
				// Create a toolbar button for the bold action
				Action boldAction = new BoldAction();
				LibTTx.setAcceleratedAction(boldAction, "Bold",
						boldActionMnemonic, boldActionShortcut);
				LibTTx.setActionIcon(boldAction, "images/bold.png");
				formatMenu.add(boldAction);
				JButton boldButton = toolBar.add(boldAction);
				boldButton.setIcon(LibTTx.makeIcon("images/boldicon-16x16.png"));
				boldButton.setBorderPainted(false);
				LibTTx.setRollover(boldButton, "images/boldicon-roll-16x16.png");

				// Italic operation
				// (ctrl-I) Abreviation of keyboard
				// Create a toolbar button for the italic action
				Action italicAction = new ItalicAction();
				LibTTx.setAcceleratedAction(italicAction, "Italic",
						italicActionMnemonic, italicActionShortcut);
				LibTTx.setActionIcon(italicAction, "images/italic.png");
				formatMenu.add(italicAction);
				JButton italicButton = toolBar.add(italicAction);
				italicButton.setIcon(LibTTx
						.makeIcon("images/italicicon-16x16.png"));
				italicButton.setBorderPainted(false);
				italicButton.setToolTipText("Italic");
				LibTTx.setRollover(italicButton, "images/italicicon-roll-16x16.png");

				// Underline operation
				// (ctrl-U) Abreviation of keyboard
				// Create a toolbar button for the underline action
				Action underlineAction = new UnderlineAction();
				LibTTx.setAcceleratedAction(underlineAction, "Underline",
						underlineActionMnemonic, underlineActionShortcut);
				LibTTx.setActionIcon(underlineAction, "images/underline.png");
				formatMenu.add(underlineAction);
				JButton underlineButton = toolBar.add(underlineAction);
				underlineButton.setIcon(LibTTx
						.makeIcon("images/underlineicon-16x16.png"));
				underlineButton.setBorderPainted(false);
				underlineButton.setText(null);
				underlineButton.setToolTipText("Underline");
				LibTTx.setRollover(underlineButton,
						"images/underlineicon-roll-16x16.png");

				// toolbar separator before plugin icons
				toolBar.addSeparator();

				// format menu separator
				formatMenu.addSeparator();
				ButtonGroup group = new ButtonGroup();

				// Font size operation
				JMenu fontSize = new JMenu("Fontsize");
				fontSizeGroupOfButtons("Size: 10", 10, group, fontSize);
				fontSizeGroupOfButtons("Size: 12", 12, group, fontSize);
				fontSizeGroupOfButtons("Size: 14", 14, group, fontSize);
				fontSizeGroupOfButtons("Size: 16", 16, group, fontSize);
				fontSizeGroupOfButtons("Size: 18", 18, group, fontSize);
				fontSizeGroupOfButtons("Size: 20", 20, group, fontSize);
				fontSizeGroupOfButtons("Size: 22", 22, group, fontSize);
				fontSizeGroupOfButtons("Size: 24", 24, group, fontSize);
				formatMenu.add(fontSize);
				
				// format menu separator
				formatMenu.addSeparator();

				// Alignment operation
				JMenu alignment = new JMenu("Alignment");
				alignmentGroupOfButton("Alignment: Beginning", 3, group, 
						alignment);
				alignmentGroupOfButton("Alignment: Middle", 1, group,
						alignment);
				alignmentGroupOfButton("Alignment: End", 2, group, 
						alignment);
				formatMenu.add(alignment);

				// format menu separator
				formatMenu.addSeparator();

				// Coloring operation
				JMenu textColor = new JMenu("Color");
				colorGroupOfButton("Black", Color.BLACK, group, textColor);
				colorGroupOfButton("Blue", Color.BLUE, group, textColor);
				colorGroupOfButton("Orange", Color.ORANGE, group, textColor);
				colorGroupOfButton("Red", Color.RED, group, textColor);
				colorGroupOfButton("Yellow", Color.YELLOW, group, textColor);
				colorGroupOfButton("Cyan", Color.CYAN, group, textColor);
				colorGroupOfButton("Dark Gray", Color.DARK_GRAY, group, textColor);
				colorGroupOfButton("Green", Color.GREEN, group, textColor);
				colorGroupOfButton("Magenta", Color.MAGENTA, group, textColor);
				colorGroupOfButton("Pink", Color.PINK, group, textColor);
				colorGroupOfButton("White", Color.WHITE, group, textColor);
				formatMenu.add(textColor);

				// Background Coloring operation
				JMenu bgColor = new JMenu("Background Coloring");
				backColorGroupOfButton("Red", Color.RED, group, bgColor);
				backColorGroupOfButton("Black", Color.BLACK, group, bgColor);
				backColorGroupOfButton("Blue", Color.BLUE, group, bgColor);
				backColorGroupOfButton("Yellow", Color.YELLOW, group, bgColor);
				backColorGroupOfButton("Cyan", Color.CYAN, group, bgColor);
				backColorGroupOfButton("Dark Gray", Color.DARK_GRAY, group, bgColor);
				backColorGroupOfButton("Magenta", Color.MAGENTA, group, bgColor);
				backColorGroupOfButton("Green", Color.GREEN, group, bgColor);
				backColorGroupOfButton("Pink", Color.PINK, group, bgColor);
				backColorGroupOfButton("White", Color.WHITE, group, bgColor);
				formatMenu.add(bgColor);
				
				
				
				
				
				/* Help menu items */

				// about Text Trix, incl copyright notice and version number
				Action aboutAction = new AbstractAction("About", LibTTx
						.makeIcon("images/minicon-16x16.png")) {
					public void actionPerformed(ActionEvent evt) {
						String path = "about.txt";
						String text = LibTTx.readText(path);
						if (text == "") {
							text = "Text Trix" + "\nthe text tinker"
									+ "\nCopyright (c) 2002-8, Text Flex"
									+ "\nhttp://textflex.com/texttrix";
							ttx.displayMissingResourceDialog(path);
						}
						String iconPath = "images/texttrixsignature.png";
						JOptionPane.showMessageDialog(ttx, text,
								"About Text Trix",
								JOptionPane.PLAIN_MESSAGE, LibTTx
										.makeIcon(iconPath));
					}
				};
				LibTTx.setAction(aboutAction, "About", 'A');
				helpMenu.add(aboutAction);	

								
				// shortcuts description; opens new tab;
				// reads from "shortcuts.txt" in same dir as this class
				Action shortcutsAction = new AbstractAction("Shortcuts", 
					LibTTx.makeIcon("images/shortcuts-16x16.png")) {
					
					public void actionPerformed(ActionEvent evt) {
						String path = "shortcuts.html";
						// ArrayIndexOutOfBoundsException while opening file
						// from menu is an JVM 1.5.0-beta1 bug (#4962642)
						if (!ttx.openFile(new File(path), false, true, false)) {
							ttx.displayMissingResourceDialog(path);
						} else {
							// place at end of EDT because file reading occurs in 
							// an invokeLater as well
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									TextPad textPad = ttx.getSelectedTextPad();
									textPad.turnOnHTML();//viewHTML();
									textPad.setCaretPosition(0);
								}
							});
						}
					}
				};
				LibTTx.setAction(shortcutsAction, "Shortcuts", 'S');
				helpMenu.add(shortcutsAction);

					
				
				
				
				// license; opens new tab;
				// reads from "license.txt" in same directory as this class
				Action licenseAction = new AbstractAction("License", 
					LibTTx.makeIcon("images/license-16x16.png")) {
					
					public void actionPerformed(ActionEvent evt) {
						String path = "license.txt";
						// ArrayIndexOutOfBoundsException while opening file
						// from menu is an JVM 1.5.0-beta1 bug (#4962642)
						if (!ttx.openFile(new File(path), false, true, false)) {
							ttx.displayMissingResourceDialog(path);
						}
					}
				};
				LibTTx.setAction(licenseAction, "License", 'L');
				helpMenu.add(licenseAction);

				/* Trix and Tools menus */

				// Load plugins; add to appropriate menu
				ttx.setupPlugIns();

				/* Place menus and other UI components */

				// must add tool bar before set menu bar lest tool bar
				// shortcuts
				// take precedence
				ttx.getContentPane().add(toolBar, BorderLayout.NORTH);

				// add menu bar and menus
				ttx.setJMenuBar(menuBar);
				menuBar.add(fileMenu);
				menuBar.add(editMenu);
				menuBar.add(viewMenu);
				menuBar.add(formatMenu);
				menuBar.add(trixMenu);
				menuBar.add(toolsMenu);
				menuBar.add(helpMenu);

				// prepare the file history menu entries
				ttx.setFileHistStart(fileMenu.getItemCount());
				
				// commenting out sync menus because seems to be conflicting with
				// other sync calls, creating multiple sets of file history menu items
				// TODO: create unified fileHist object to synchronize across windows,
				// perhaps picking up entries directly from prefs
//				syncMenus();
				//System.out.println("Validating the menu bar...");
				ttx.validate();
			}
		});
		
	}
	
	/** Gets an action for the auto-wrap-indent tool.
	 * @param iconPath the path to the normal, non-rollover icon; note that
	 * the check box only dispalys this non-rollover icon
	 * @param swapChkBox if true, the auto-wrap-indent check box will change
	 * to its oppositive selection just prior to determining whether to start or stop
	 * wrap-indent.
	 * @return the auto-wrap-indent action
	 */
	private Action getAutoIndentAction(String iconPath, final boolean swapChkBox) {
		Action autoIndentAction = new AbstractAction(
				"Auto Wrap Indent the selected file",
				LibTTx.makeIcon(iconPath)) {
			public void actionPerformed(ActionEvent evt) {
				TextPad t = ttx.getSelectedTextPad();
				// Check if a pad is selected
				if (t != null) {
					// If necessary, swap the selection of the check box
					JCheckBoxMenuItem autoIndent = ttx.getAutoIndentCheckBox();
					if (swapChkBox) {
						autoIndent.setSelected(!autoIndent.isSelected());
					}
					// Retrieve the auto-wrap-indent setting
					t.setAutoIndent(autoIndent.isSelected());
				}
			}
		};
		String autoIndentToolTipText = 
				"<html>Automatically repeat tabs on the next line and "
				+ "<br>graphically wraps the indentations,"
				+ "<br>without modifying the underlying text.</html>";
		LibTTx
				.setAcceleratedAction(
						autoIndentAction,
						autoIndentToolTipText, 
						'I', KeyStroke.getKeyStroke("alt shift I"));
		return autoIndentAction;
	}
	
	/** 
	 * Creates a button for the given font size and adds the button to the 
	 * group of font size buttons.
	 * @param nameOfSize size name to be displayed in button
	 * @param size the actual size of the font
	 */
	private void fontSizeGroupOfButtons(final String nameOfSize, 
			final int size, ButtonGroup group, JMenu menu) {
		JRadioButtonMenuItem button = new JRadioButtonMenuItem(nameOfSize);
		group.add(button);
		menu.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (ttx.switchToHTMLView(null, true)) {
					new StyledEditorKit.FontSizeAction(nameOfSize, size)
							.actionPerformed(event);
				}
			}
		});
	}
	
	
	/** This method takes as inputs the number of alignment
	* which user can use in his text and
	* the name of the alignment
	* Then adds a button for each number and organize
	* all the buttons in a group
	* End, adds this group at Alignment operation
	*/
	private void alignmentGroupOfButton(final String nameOfAlignment,
			final int location, ButtonGroup group, JMenu menu) {
		JRadioButtonMenuItem button = new JRadioButtonMenuItem(nameOfAlignment);
		group.add(button);
		menu.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (ttx.switchToHTMLView(null, true)) {
					new StyledEditorKit.AlignmentAction(nameOfAlignment, 
							location).actionPerformed(event);
				}
			}
		});
	}
	
	
	/** This method takes as inputs the color in
	* which user can "paint" his text and
	* the name of the color
	* Then adds a button for each color and organize
	* all the buttons in a group
	* End, adds this group at Color operation
	*/
	private void colorGroupOfButton(final String nameOfColor, final Color color,
			ButtonGroup group, JMenu menu) {
		JRadioButtonMenuItem button = new JRadioButtonMenuItem(nameOfColor);
		group.add(button);
		menu.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (ttx.switchToHTMLView(null, true)) {
					new StyledEditorKit.ForegroundAction(nameOfColor, color)
							.actionPerformed(event);
				}
			}
		});
	}
	
	
	/** This method takes as inputs the color in
	* which user can "paint" the backgroundtext and
	* the name of the color
	* Then adds a button for each color and organize
	* all the buttons in a group
	* End, adds this group at Background Color operation
	*/
	private void backColorGroupOfButton(final String nameOfColor,
			final Color color, ButtonGroup group, JMenu menu) {
		JRadioButtonMenuItem button = new JRadioButtonMenuItem(nameOfColor);
		group.add(button);
		menu.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (ttx.switchToHTMLView(null, true)) {
					ttx.getSelectedTextPad().setBackground(color);
				}
			}
		});
	}
	
	
	/**
	 * Closes files and removes them from the tab history.
	 *  
	 */
	private class FileCloseAction extends AbstractAction {

		/**
		 * Constructs the file close action.
		 * 
		 * @param name name of the action
		 * @param icon graphics for the action
		 */
		public FileCloseAction(String name, Icon icon) {
			putValue(Action.NAME, name);
			putValue(Action.SMALL_ICON, icon);
		}

		/**
		 * Removes the tab from the tab history and closes the tab.
		 *  
		 */
		public void actionPerformed(ActionEvent evt) {
			MotherTabbedPane pane = ttx.getSelectedTabbedPane();
			int i = pane.getSelectedIndex();
			if (i >= 0) {
				ttx.setUpdateTabIndexHistory(false);
				pane.removeTabHistory(i);
				ttx.setUpdateTabIndexHistory(true);
				ttx.closeTextArea(i, pane);
			}
		}
	}

	/**
	 * Responds to user input calling for a save dialog.
	 */
	private class FileSaveAction extends AbstractAction {
		JFrame owner;

		/**
		 * Constructs the file open action
		 * 
		 * @param aOwner the parent frame
		 * @param name the action's name
		 * @param icon the action's icon
		 */
		public FileSaveAction(JFrame aOwner, String name, Icon icon) {
			owner = aOwner;
			putValue(Action.NAME, name);
			putValue(Action.SMALL_ICON, icon);
		}

		/**
		 * Displays a file save chooser when the action is invoked.
		 * 
		 * @param evt action invocation
		 * @see #fileSaveDialog(JFrame)
		 */
		public void actionPerformed(ActionEvent evt) {
			ttx.fileSaveDialog(ttx.getSelectedTextPad(), owner);
		}
	}

	/** Applies bold formatting.
	 * Switches to HTML view to allow saving the styled format.
	 */
	private class BoldAction extends StyledEditorKit.BoldAction {
		public void actionPerformed(ActionEvent e) {
			if (ttx.switchToHTMLView(null, true)) {
				super.actionPerformed(e);
			}
		}
	}

	/** Applies italic formatting.
	 * Switches to HTML view to allow saving the styled format.
	 */
	private class ItalicAction extends StyledEditorKit.ItalicAction {
		public void actionPerformed(ActionEvent e) {
			if (ttx.switchToHTMLView(null, true)) {
				super.actionPerformed(e);
			}
		}
	}

	/** Applies underline formatting.
	 * Switches to HTML view to allow saving the styled format.
	 */
	private class UnderlineAction extends StyledEditorKit.UnderlineAction {
		public void actionPerformed(ActionEvent e) {
			if (ttx.switchToHTMLView(null, true)) {
				super.actionPerformed(e);
			}
		}
	}


}
