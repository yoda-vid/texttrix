/* TextTrix.java    
   Text Trix
   the text tinker
   http://textflex.com/texttrix
   
   Copyright (c) 2002-3, Text Flex
   All rights reserved.
   
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions 
   are met:
   
   * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
   * Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
   * Neither the name of the Text Trix nor the names of its
   contributors may be used to endorse or promote products derived
   from this software without specific prior written permission.
   
   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
   IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
   TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
   PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
   OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  
*/

package com.textflex.texttrix;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.filechooser.FileFilter;
import java.net.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**The main <code>TextTrix</code> class.
 * Takes care of most graphical user interface operations, such as 
 * setting up and responding to changes in the <code>Text Pad</code>s, 
 * tool bar, menus, and dialogs.
 */
public class TextTrix extends JFrame {
    private static ArrayList textAreas = new ArrayList(); // all the TextPads
    private static JTabbedPane tabbedPane = null; // multiple TextPads
    private static JPopupMenu popup = null; // make popup menu
    private static JFileChooser chooser = null; // file dialog
    private static JCheckBoxMenuItem autoIndent = null;
    private static String openDir = ""; // most recently path opened to
    private static String saveDir = ""; // most recently path saved to
    private static int fileIndex = 0; // for giving each TextPad a unique name
    //    private static FindDialog findDialog = null; // find dialog
    //	private static int tabSize = 0; // user-defined tab display size
    private static PlugIn[] plugIns = null; // plugins from jar archives
    private static Action[] plugInActions = null; // plugin invokers
    private JMenu trixMenu = null; // trix plugins
    private JMenu toolsMenu = null; // tools plugins
    private JToolBar toolBar = null; // icons
    private String toolsCharsUnavailable = ""; // chars for shorcuts
    private String trixCharsUnavailable = ""; // chars for shorcuts
    private int[] tabIndexHistory = new int[10]; // records for back/forward
    private int tabIndexHistoryIndex = 0; // index of next record
    //    private int currTabIndex = 0; // index of the current tab
    private boolean updateTabIndexHistory = true; // flag to update the record
    private Focuser focuser = null;

    /** Constructs a new <code>TextTrix</code> frame and with
	<code>TextPad</code>s for each of the specified paths or at least
	one <code>TextPad</code>.
     */
    public TextTrix(String[] paths) {
	setTitle("Text Trix");
	// pre-set window size
	setSize(500, 600); // TODO: adjust to user's screen size
	ImageIcon im = LibTTx.makeIcon("images/minicon-32x32.png"); // set frame icon
	if (im !=null) 
	    setIconImage(im.getImage());
	tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	// keep the tabs the same width when substituting chars
	tabbedPane.setFont(new Font("Monospaced", Font.PLAIN, 11));
	// initialize tabIndexHistory
	for (int i = 0; i < tabIndexHistory.length; i++) 
	    tabIndexHistory[i] = -1;
	// adds a change listener to listen for tab switches and display
	// the options of the tab's TextPad
	focuser = new Focuser();
	tabbedPane.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent evt) {
		    TextPad t = getSelectedTextPad();
		    if (t != null) {
			setAutoIndent(t.getAutoIndent());
			// update the tab index record;
			// addTabIndexHistory increments the record index;
			// add the current tab selection now to ensure that
			// all selections are recorded
			if (updateTabIndexHistory) {
			    addTabIndexHistory(tabbedPane.getSelectedIndex());
			    //			    addTabIndexHistory(currTabIndex);
			}
			focuser.start();
			// doesn't work when creating new tabs via
			// the keyboard accelerator;
			// only works when changing between already created
			// tabs or creating new ones via the menu item
			//t.requestFocusInWindow();
			/*
			t.setVisible(true);
			t.setFocusable(true);
  			t.requestFocusInWindow();
			*/
			/*
			for (int i = 0; i < 10; i++) {
			    try { Thread.sleep(50); } catch (Exception e) {}
			    getSelectedTextPad().requestFocusInWindow();
			}
			*/
			//			try { Thread.sleep(2000); } catch (InterruptedException e) {}
			//			System.out.println(getFocusOwner().toString());
			// record the current tab to update the record
			// after switching to a new one
			//			currTabIndex = tabbedPane.getSelectedIndex();
		    }
		}
	    });

	/*
	tabbedPane.addFocusListener(new FocusListener() {
		public void focusGained(FocusEvent evt) {
		    getSelectedTextPad().requestFocusInWindow();
		}

		public void focusLost(FocusEvent evt) {
		}
	    });
	*/

	// display tool tips for up to 100s
	ToolTipManager.sharedInstance().setDismissDelay(100000);

	// make menu bar and menus
	JMenuBar menuBar = new JMenuBar();
	JMenu fileMenu = new JMenu("File");
	fileMenu.setMnemonic('I'); // not 'F' since Alt-f for word-forward
	JMenu editMenu = new JMenu("Edit");
	editMenu.setMnemonic('E');
	JMenu viewMenu = new JMenu("View");
	viewMenu.setMnemonic('V');
	trixMenu = new JMenu("Trix");
	trixMenu.setMnemonic('T');
	toolsMenu = new JMenu("Tools");
	toolsMenu.setMnemonic('O');
	JMenu helpMenu = new JMenu("Help");
	helpMenu.setMnemonic('H');

	// make tool bar
	toolBar = new JToolBar("Trix and Tools");
	toolBar.addMouseListener(new PopupListener());
	//	toolBar.setFloatable(false); // necessary since not BorderLayout
	toolBar.setBorderPainted(false);
	//	SwingUtilities.updateComponentTreeUI(TextTrix.this);

	popup = new JPopupMenu();
	chooser = new JFileChooser();

	/* File menu items */

	// make new tab and text area
	Action newAction = new AbstractAction("New") {
		public void actionPerformed(ActionEvent evt) {
		    addTextArea(textAreas, tabbedPane, makeNewFile());
		    /*
		    try { Thread.sleep(2000); } catch (Exception e) {}
		    getSelectedTextPad().requestFocusInWindow();
		    */
		    /*
		    for (int i = 0; i < 3; i++) {
			try { Thread.sleep(1000); } catch (Exception e) {}
			getSelectedTextPad().requestFocusInWindow();
		    }
		    */
		    //		    transferFocus();
		    //		    toolsMenu.requestFocusInWindow();
		    //		    getSelectedTextPad().requestFocusInWindow();
		    //		    System.out.println(getFocusOwner().toString());
		}
	    };
	// per Mozilla keybinding
	LibTTx.setAcceleratedAction(newAction, "New", 'T', 
			     KeyStroke.getKeyStroke("ctrl T"));
	fileMenu.add(newAction);

	// (ctrl-o) open file; use selected tab if empty
	Action openAction 
	    = new FileOpenAction(TextTrix.this, "Open", 
				 LibTTx.makeIcon("images/openicon-16x16.png"));
	LibTTx.setAcceleratedAction(openAction, "Open", 'O', 
			     KeyStroke.getKeyStroke("ctrl O"));
	fileMenu.add(openAction);
	JButton openButton = toolBar.add(openAction);
	openButton.setBorderPainted(false);
	LibTTx.setRollover(openButton, "images/openicon-roll-16x16.png");

	// set text and web file filters for open/save dialog boxes
	final ExtensionFileFilter webFilter = new ExtensionFileFilter();
	webFilter.addExtension("html");
	webFilter.addExtension("htm");
	webFilter.addExtension("xhtml");
	webFilter.addExtension("shtml");
	webFilter.addExtension("css");
	webFilter.setDescription("Web files (*.html, *.htm, " 
				 + "*.xhtml, *.shtml, *.css)");
	chooser.setFileFilter(webFilter);
		
	final ExtensionFileFilter rtfFilter = new ExtensionFileFilter();
	rtfFilter.addExtension("rtf");
	rtfFilter.setDescription("RTF files (*.rtf)");
	chooser.setFileFilter(rtfFilter);

	final ExtensionFileFilter txtFilter = new ExtensionFileFilter();
	txtFilter.addExtension("txt");
	txtFilter.setDescription("Text files (*.txt)");
	chooser.setFileFilter(txtFilter);

	
	// close file; check if saved
	Action closeAction = 
	    new AbstractAction("Close", 
			       LibTTx.makeIcon("images/closeicon-16x16.png")) {
		public void actionPerformed(ActionEvent evt) {
		    int i = tabbedPane.getSelectedIndex();
		    if (i >= 0) {
			updateTabIndexHistory = false;
			removeTabIndexHistory(i);
			updateTabIndexHistory = true;
			closeTextArea(i, textAreas, tabbedPane);
		    }
		}
	    };
	LibTTx.setAcceleratedAction(closeAction, "Close", 'C', 
		  KeyStroke.getKeyStroke("ctrl W"));
	fileMenu.add(closeAction);

	// (ctrl-s) save file; no dialog if file already created
	Action saveAction = 
	    new AbstractAction("Save", LibTTx.makeIcon("images/saveicon-16x16.png")) {
		public void actionPerformed(ActionEvent evt) {
		    // can't use tabbedPane.getSelectedComponent() 
		    // b/c returns JScrollPane
		    int tabIndex = tabbedPane.getSelectedIndex();
		    TextPad t = null;
		    if (tabIndex != -1) {
			t = (TextPad)textAreas.get(tabIndex);
			// save directly to file if already created one
			if (t.fileExists()) {
			    if (!saveFile(t.getPath())) {
				String msg = t.getPath() 
				    + " couldn't be written.\n"
				    + "Would you like to try saving it "
				    + "somewhere else?";
				String title = "Couldn't write";
				if (yesNoDialog(TextTrix.this, msg, title))
				    fileSaveDialog(TextTrix.this);
			    }
			// otherwise, request filename for new file
			} else {
			    fileSaveDialog(TextTrix.this);
			}
		    }
		}
	    };
	LibTTx.setAcceleratedAction(saveAction, "Save", 'S', 
			     KeyStroke.getKeyStroke("ctrl S"));
	fileMenu.add(saveAction);
	JButton saveButton = toolBar.add(saveAction);
	saveButton.setBorderPainted(false);
	LibTTx.setRollover(saveButton, "images/saveicon-roll-16x16.png");

	// save w/ file save dialog
	Action saveAsAction 
	    = new FileSaveAction(TextTrix.this, "Save as...", 
				 LibTTx.makeIcon("images/saveasicon-16x16.png"));
	LibTTx.setAction(saveAsAction, "Save as...", '.');
	fileMenu.add(saveAsAction);
	
	// Start exit functions
	fileMenu.addSeparator();
	
	// (ctrl-q) exit file; close each tab separately, checking for saves
	Action quitAction = new AbstractAction("Quit") {
		public void actionPerformed(ActionEvent evt) {
		    exitTextTrix();
		}
	    };
	// Doesn't work if close all tabs unless click ensure window focused, 
	// such as clicking on menu
	LibTTx.setAcceleratedAction(quitAction, "Quit", 'Q', 
		  KeyStroke.getKeyStroke("ctrl Q"));
	fileMenu.add(quitAction);

	/* Edit menu items */

	// (ctrl-z) undo; multiple undos available
	Action undoAction = new AbstractAction("Undo") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas
		     .get(tabbedPane.getSelectedIndex())).undo();
		}
	    };
	LibTTx.setAcceleratedAction(undoAction, "Undo", 'U', 
		  KeyStroke.getKeyStroke("ctrl Z"));
	editMenu.add(undoAction);

	// (ctrl-y) redo; multiple redos available
	Action redoAction = new AbstractAction("Redo") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas.get(tabbedPane.getSelectedIndex()))
			.redo();
		}
	    };
	LibTTx.setAcceleratedAction(redoAction, "Redo", 'R', 
			     KeyStroke.getKeyStroke("ctrl R"));
	editMenu.add(redoAction);

	// Start Cut, Copy, Paste actions
	editMenu.addSeparator();
	
	// (ctrl-x) cut
	Action cutAction = new AbstractAction("Cut") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas
		     .get(tabbedPane.getSelectedIndex())).cut();
		}
	    };
	LibTTx.setAcceleratedAction(cutAction, "Cut", 'C', 
			     KeyStroke.getKeyStroke("ctrl X"));
	editMenu.add(cutAction);
	popup.add(cutAction);

	// (ctrl-c) copy
	Action copyAction = new AbstractAction("Copy") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas
		     .get(tabbedPane.getSelectedIndex())).copy();
		}
	    };
	LibTTx.setAcceleratedAction(copyAction, "Copy", 'O', 
			     KeyStroke.getKeyStroke("ctrl C"));
	editMenu.add(copyAction);
	popup.add(copyAction);

	// (ctrl-v) paste
	Action pasteAction = new AbstractAction("Paste") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas
		     .get(tabbedPane.getSelectedIndex())).paste();
		}
	    };
	LibTTx.setAcceleratedAction(pasteAction, "Paste", 'P', 
			     KeyStroke.getKeyStroke("ctrl V"));
	editMenu.add(pasteAction);
	popup.add(pasteAction);

	// Start selection items
	editMenu.addSeparator();

	// select all text in current text area
	Action selectAllAction = new AbstractAction("Select all") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas
		     .get(tabbedPane.getSelectedIndex())).selectAll();
		}
	    };
	LibTTx.setAcceleratedAction(selectAllAction, "Select all", 'S', 
			     KeyStroke.getKeyStroke("ctrl L"));
	editMenu.add(selectAllAction);
	popup.add(selectAllAction);

	// edit menu preferences separator
	editMenu.addSeparator();

	// options sub-menu
	JMenu optionsMenu = new JMenu("Options");
	optionsMenu.setMnemonic('O');
	editMenu.add(optionsMenu);

	// auto-indent
	// apply the selection to the current TextPad
	Action autoIndentAction 
	    = new AbstractAction("Auto indent the selected file") {
		public void actionPerformed(ActionEvent evt) {
		    TextPad t = getSelectedTextPad();
		    if (t != null) 
			t.setAutoIndent(autoIndent.isSelected());
		}
	    };
	LibTTx.setAcceleratedAction(autoIndentAction, 
			     "Automatically repeat tabs with the next line",
			     'I', 
			     KeyStroke.getKeyStroke("alt shift I"));
	//	autoIndent.addActionListener(autoIndentAction);
	autoIndent = new JCheckBoxMenuItem(autoIndentAction); // auto-indent
	optionsMenu.add(autoIndent);

	Action autoIndentAllAction = 
	    new AbstractAction("Auto-indent all current files") {
		public void actionPerformed(ActionEvent evt) {
		    for (int i = 0; i < textAreas.size(); i++) {
			TextPad t = (TextPad)textAreas.get(i);
			if (t != null) 
			    t.setAutoIndent(true);
		    }
		    setAutoIndent(true);
		}
	    };
	LibTTx.setAction(autoIndentAllAction, "Auto indent all files", 'A');
	optionsMenu.add(autoIndentAllAction);
		





	/* View menu items */

	/* Tab switching attempts to combine several elements of web
	   browser behavior.  Users can cycle through the tabs in the order
	   in which they were created by using the Ctrl-]/[ key combinations,
	   similar to the tab cycling in the Mozilla browser.  Occasionally
	   the user will open a group of files but want to switch among
	   only a particular subset of them.  Web browsers' "Back" and
	   "Forward" buttons become useful here.  By first clicking on the
	   desired sequence of tabs, the order becomes stored in the
	   Text Trix history.  To traverse up and down that history,
	   the user can use the Ctrl-Shift-]\[ shortcut keys.
	   
	   These shortcuts intend to expand upon the default Java key-bindings
	   for tab cycling.  These shortcuts remain available to those
	   more familiar with other Java applications employing these default
	   settings:
	   -Ctrl-up to tabs
	   -Leftt/Right to switch tabs
	   -Tab back down to TextPad area
	*/
	
	// (ctrl-shift-[) switch back in the tab history
	Action backTabAction = new AbstractAction("Back") {
		public void actionPerformed(ActionEvent evt) {
		    // switch back only up through the first record;
		    // keep from recording the past selected tabs
		    // as newly selected one;
		    // the current index always refers to the next
		    // available position to add selections, while
		    // the previous index refers to the current selection;
		    // to go back, the value at two positions back must be 
		    // checked
		    if (--tabIndexHistoryIndex >= 1) {
			//			&& tabIndexHistoryIndex < tabIndexHistory.length) {
			// uncouple the tab index history while switching
			// to the past tabs -- leave the tabs as a
			// trail until a new tab is chosesn
			updateTabIndexHistory = false;
			//			System.out.println("tabIndexHistoryIndex: " + tabIndexHistoryIndex + "; tabIndexHistory[]: " + tabIndexHistory[tabIndexHistoryIndex]);
			tabbedPane.setSelectedIndex(tabIndexHistory
						    [tabIndexHistoryIndex - 1]);
			updateTabIndexHistory = true;
			//			try { Thread.sleep(1000); } catch (InterruptedException e) {}
		    } else { // reset the index to its orig val, -1
			++tabIndexHistoryIndex;
		    }
		}
	    };
	LibTTx.setAcceleratedAction(backTabAction, "Back", 'B', 
			     KeyStroke.getKeyStroke("ctrl shift OPEN_BRACKET"));
	viewMenu.add(backTabAction);

	// (ctrl-shift-]) switch forwared in the tab history
	Action forwardTabAction = new AbstractAction("Forward") {
		public void actionPerformed(ActionEvent evt) {
		    int i = 0;
		    // switch only through the the last recorded selected tab;
		    // i == -1 signifies that no tab has been recorded
		    // for that index;
		    // keep from updating the history with past selections
		    if (++tabIndexHistoryIndex < tabIndexHistory.length
			&& (i = tabIndexHistory[tabIndexHistoryIndex - 1]) != -1) {
			// uncouple the history, preserving it as a 
			// trail of selections past and future from 
			// the current position
			updateTabIndexHistory = false;
			//			System.out.println("tabIndexHistoryIndex: " + tabIndexHistoryIndex + "; tabIndexHistory[]: " + i);
			tabbedPane.setSelectedIndex(i);
			updateTabIndexHistory = true;
			//			++tabIndexHistoryIndex;
		    } else {
			--tabIndexHistoryIndex;
		    }
		}
	    };
	LibTTx.setAcceleratedAction(forwardTabAction, "Foreward", 'F', 
			     KeyStroke.getKeyStroke("ctrl shift "
						    + "CLOSE_BRACKET"));
	viewMenu.add(forwardTabAction);

	// (ctrl-[) switch to the preceding tab
	Action prevTabAction = new AbstractAction("Preceeding tab") {
		public void actionPerformed(ActionEvent evt) {
		    int tab = tabbedPane.getSelectedIndex();
		    if (tab > 0) {
			tabbedPane.setSelectedIndex(tab - 1);
		    } else if (tab == 0) {
			tabbedPane.setSelectedIndex(tabbedPane
						    .getTabCount() - 1);
		    }
		}
	    };
	LibTTx.setAcceleratedAction(prevTabAction, "Preeceding tab", 'P', 
			     KeyStroke.getKeyStroke("ctrl OPEN_BRACKET"));
	viewMenu.add(prevTabAction);

	// (ctrl-]) switch to the next tab
	Action nextTabAction = new AbstractAction("Next tab") {
		public void actionPerformed(ActionEvent evt) {
		    int tab = tabbedPane.getSelectedIndex();
		    if ((tab != -1) && (tab == tabbedPane.getTabCount() - 1)) {
			tabbedPane.setSelectedIndex(0);
		    } else if (tab >= 0) {
			tabbedPane.setSelectedIndex(tab + 1);
		    }
		}
	    };
	LibTTx.setAcceleratedAction(nextTabAction, "Next tab", 'N', 
			     KeyStroke.getKeyStroke("ctrl CLOSE_BRACKET"));
	viewMenu.add(nextTabAction);

	viewMenu.addSeparator();

	// view as plain text
	Action togglePlainViewAction 
	    = new AbstractAction("Toggle plain text view") {
		public void actionPerformed(ActionEvent evt) {
		    viewPlain();
		}
	    };
	LibTTx.setAction(togglePlainViewAction, "View as plain text", 'A');
	viewMenu.add(togglePlainViewAction);

	// view as HTML formatted text
	Action toggleHTMLViewAction = new AbstractAction("Toggle HTML view") {
		public void actionPerformed(ActionEvent evt) {
		    viewHTML();
		}
	    };
	LibTTx.setAction(toggleHTMLViewAction, "View as HTML", 'H');
	viewMenu.add(toggleHTMLViewAction);
	
	// view as RTF formatted text
	Action toggleRTFViewAction = new AbstractAction("Toggle RTF view") {
		public void actionPerformed(ActionEvent evt) {
		    viewRTF();
		}
	    };
	LibTTx.setAction(toggleRTFViewAction, "View as RTF", 'R');
	viewMenu.add(toggleRTFViewAction);
		
	/* Help menu items */

	// about Text Trix, incl copyright notice and version number
	Action aboutAction 
	    = new AbstractAction("About...",
				 LibTTx.makeIcon("images/minicon-16x16.png")) {
		public void actionPerformed(ActionEvent evt) {
		    String text = readText("about.txt");
		    String iconPath = "images/texttrixsignature.png";
		    JOptionPane
			.showMessageDialog(null, 
					   text, 
					   "About Text Trix", 
					   JOptionPane.PLAIN_MESSAGE, 
					   LibTTx.makeIcon(iconPath));
		}
	    };
	LibTTx.setAction(aboutAction, "About...", 'A');
	helpMenu.add(aboutAction);
	
	// shortcuts description; opens new tab
	Action shortcutsAction = new AbstractAction("Shortcuts") {
		public void actionPerformed(ActionEvent evt) {
		    // reads from "shortcuts.txt" in same dir as this class
		    String path = "shortcuts.txt";
		    displayFile(path);
		}
	    };
	LibTTx.setAction(shortcutsAction, "Shortcuts", 'S');
	helpMenu.add(shortcutsAction);

	// features descriptions; opens new tab
	Action featuresAction = new AbstractAction("Features descriptions") {
		public void actionPerformed(ActionEvent evt) {
		    // reads from "features.txt" in same dir as this class
		    String path = "features.txt";
		    displayFile(path);
		}
	    };
	LibTTx.setAction(featuresAction, "Features descriptions", 'F');
	helpMenu.add(featuresAction);

	// license; opens new tab
	Action licenseAction = new AbstractAction("License") {
		public void actionPerformed(ActionEvent evt) {
		    // reads from "license.txt" in same directory as this class
		    String path = "license.txt";
		    displayFile(path);
		}
	    };
	LibTTx.setAction(licenseAction, "License", 'L');
	helpMenu.add(licenseAction);

	//	toolBar.addSeparator();





	/* Trix and Tools menus */

	/*
	// Find and Replace: fixed tool, not a plugin
	// (ctrl-shift-F) find and replace Tools feature
	Action findAction 
	    = new AbstractAction("Find and replace", 
				 LibTTx.makeIcon("images/find-16x16.png")) {
		public void actionPerformed(ActionEvent evt) {
		    if (findDialog == null) 
			findDialog = new FindDialog(TextTrix.this);
		    findDialog.show();
		}
	    };
	// need capital "F" b/c "shift"
	LibTTx.setAcceleratedAction(findAction, "Find and replace", 'F', 
		  KeyStroke.getKeyStroke("ctrl shift F")); 
	toolsMenu.add(findAction);
	JButton findButton = toolBar.add(findAction);
	findButton.setBorderPainted(false);
	LibTTx.setRollover(findButton, "images/find-roll-16x16.png");
	findButton.setToolTipText(readText("findbutton.html"));
	*/


	// Load plugins; add to appropriate menu
	setupPlugins();


	/* Place menus and other UI components */

	// add menu bar and menus
	setJMenuBar(menuBar);
	menuBar.add(fileMenu);
	menuBar.add(editMenu);
	menuBar.add(viewMenu);
	menuBar.add(trixMenu);
	menuBar.add(toolsMenu);
	menuBar.add(helpMenu);

	/*	
	// add components to frame; "add" function to set GridBag parameters
	Container contentPane = getContentPane();
	GridBagLayout layout = new GridBagLayout();
	contentPane.setLayout(layout);
	
	GridBagConstraints constraints = new GridBagConstraints();
	
	// add toolbar menu
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	add(toolBar, constraints, 0, 0, 1, 1, 0, 0);
	
	// add tabbed pane
	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	add(tabbedPane, constraints, 0, 2, 1, 1, 100, 100);
	*/

	Container contentPane = getContentPane();
	contentPane.add(toolBar, BorderLayout.NORTH);
	contentPane.add(tabbedPane, BorderLayout.CENTER);
	//	contentPane.add(new TextArea(10, 5), BorderLayout.SOUTH);


	// make first tab and text area;
	// can only create after making several other user interface
	// components, such as the autoIndent check menu item
	addTextArea(textAreas, tabbedPane, makeNewFile());

	// load files specified at start from command-line
	if (paths != null) {
	    for (int i = 0; i < paths.length; i++) {
		if (!openFile(new File(paths[i]))) {
		    String msg = "Sorry, but " + paths[i] + " can't be read.\n"
			+ "Is it a directory?  Does it have the right "
			+ "permsissions for reading?";
		    System.out.println(msg);
		}
	    }
	}

    }
	








    /**Publically executable starter method.
     * Creates the <code>TextTrix</code> object, displays it,
     * an makes sure that it will still undergo its
     * exit routine when closed manually.
     * @param args command-line arguments; not yet used
     */
    public static void main(String[] args) {
	// set the look and feel;
	// TODO: allow dynamic switching in Preferences section
	try {
	    //	    System.out.println("java ver num: " + System.getProperty("java.vm.version") + ", " + System.getProperty("os.name"));
	    /** GTK+ look and feel for Java v.1.4.2 running on Linux,
		otherwise the default look and feel, such as the new XP 
		interface for Microsoft Windows XP systems with Java v.1.4.2.
		According to http://java.sun.com/j2se/1.4.2/docs/guide/ //
		swing/1.4/Post1.4.html, UIManager
		.getSystemLookAndFeelClassName() will return GTK+ by default
		in Java v.1.5.
	    */
	    if (System.getProperty("os.name").equals("Linux")
		&& System.getProperty("java.vm.version").indexOf("1.4.2") 
		!= -1) { // GTK+ only for available systems
		/*
		UIManager.setLookAndFeel("com.sun.java.swing.plaf"
					 + ".gtk.GTKLookAndFeel");
		*/
		//	    } else if (System.getProperty("mrj.version") != null) {

	    } else { // default interface
		UIManager.setLookAndFeel(UIManager
					 .getSystemLookAndFeelClassName());
	    }
	    //	    SwingUtilities.updateComponentTreeUI(TextTrix.this);
	} catch (Exception e) {
	    //	    e.printStackTrace();
	    String msg = "Sorry, couldn't find that look-and-feel."
		+ "  Defaulting to the Metal one.";
	    System.out.println(msg);
	}
	TextTrix textTrix = new TextTrix(args);
	textTrix.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	// make sure still goes through the exit routine if close
	// window manually
	textTrix.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    exitTextTrix();
		}
		/*
		  public void windowGainedFocus(WindowEvent e) {
		  TextPad t;
		  if ((t = getSelectedTextPad()) != null) {
		  t.requestFocusInWindow();
		  }
		  }
		*/
	    });
	textTrix.show();
	//       	textTrix.getSelectedTextPad().requestFocusInWindow();
	/*
	while(true) {
	try { Thread.sleep(2000); } catch (InterruptedException e) {}
	textTrix.getSelectedTextPad().requestFocusInWindow();}
	*/
    }











    /** Creates a plugin action.
	Allows the plugin to be invoked from a button or other action-capable
	interface.
	@param pl plugin from which to make an action
    */
    public void makePlugInAction(final PlugIn pl) {
	String name = pl.getName(); // plugin name
	String category = pl.getCategory(); // plugin category, for menu adding
	String description = pl.getDescription(); // brief description
	// reader for extended description
	BufferedReader detailedDescriptionBuf = pl.getDetailedDescription();
	ImageIcon icon = pl.getIcon(); // icon
	ImageIcon rollIcon = pl.getRollIcon(); // icon for mouse-rollover

	/*
	// create the action
	Action runAction = 
	    new AbstractAction(name, icon) {
		public void actionPerformed(ActionEvent evt) {
		    // invoke the plugin's text manipulation on the current
		    // TextPad's text
		    textTinker(pl); 
		}
	    };
	
	pl.LibTTx.setAction(runAction);
	*/

	// create the listener to respond to events that the plug in fires
	PlugInAction listener = new PlugInAction() {
		public void runPlugIn(PlugInEvent event) {
		    //		    if (evt.getType() == PlugInEvent.COMPLETE)
		    textTinker(pl); 
		}
	    };
	// register the listener so the plug in knows to fire it
	pl.addPlugInListener(listener);
	
	// action to start the plug in, such as invoking its options
	// panel if it has one
	Action startAction = 
	    new AbstractAction(name, icon) {
		public void actionPerformed(ActionEvent evt) {
		    // invoke the plugin's text manipulation on the current
		    // TextPad's text
		    pl.startPlugIn();
		}
	    };

	// add the action to the appropriate menu
	if (category.equalsIgnoreCase("tools")) {
	    toolsCharsUnavailable 
		= LibTTx.setAction(startAction, name, description, 
				   toolsCharsUnavailable);
	    toolsMenu.add(startAction);
	} else {
	    trixCharsUnavailable 
		= LibTTx.setAction(startAction, name, description, 
				   trixCharsUnavailable);
	    trixMenu.add(startAction);
	}

	// add the action to a tool bar menu
	JButton button = toolBar.add(startAction);
	button.setBorderPainted(false);
	LibTTx.setRollover(button, rollIcon);
	if (detailedDescriptionBuf != null)
	button.setToolTipText(LibTTx.readText(detailedDescriptionBuf));
    }

    /** Run a text-manipulating plugin on the selected text pad's text.
	If a given region is selected, the plugin will only work on that
	area.
	@param pl plugin to invoke
    */
    public void textTinker(PlugIn pl) {
	TextPad t = getSelectedTextPad();
	if (t != null) {
	    viewPlain(); // plugins generally need to work on displayed text
	    // work through Document rather than getText/setText since
	    // the latter method does not seem to work on all systems,
	    // evidently only delivering and working on the current line
	    Document doc = t.getDocument(); 
	    String text = null; // text from the Document methods

	    // only modify the selected text, and make 
	    // the action undoable
	    int start = t.getSelectionStart();
	    int end = t.getSelectionEnd(); // at the first unselected character
	    PlugInOutcome outcome = null;
	    try {
		// determines whether a region is selected or not;
		// if not, works on the text pad's entire text
		if (start == end || pl.getIgnoreSelection()) { // no selection
		    text = doc.getText(0, doc.getLength()); // all the text
		    outcome = pl.run(text, start, end); // invoke the plugin
		    doc.remove(0, doc.getLength()); // remove all the text
		    doc.insertString(0, outcome.getText(), null); // insert text
		    // approximates the original caret position
		    int i = -1;
		    if ((i = outcome.getSelectionStart()) != -1) {
			textSelection(t, 0, i, outcome.getSelectionEnd());
		    } else if (start > doc.getLength()) {
			// otherwise errors toward end of document sometimes
			t.setCaretPosition(doc.getLength());
		    } else {
			t.setCaretPosition(start);
		    }
		} else {
		    int len = end - start; // length of selected region
		    text = doc.getText(start, len); // only get the region
		    outcome = pl.run(text, start, end); // invoke the plugin
		    doc.remove(start, len); // remove only the region
		    doc.insertString(start, outcome.getText(), null); // insert text
		    // caret automatically returns to end of selected region
		    int i = -1;
		    if ((i = outcome.getSelectionStart()) != -1) 
			textSelection(t, start, i, outcome.getSelectionEnd());
		}
		//		if (t.getAutoIndent()) t.setIndentTabs(t.getTabSize());
			    
	    } catch (BadLocationException e) {
		e.printStackTrace();
	    }
	}
    }

    public void textSelection(TextPad t, int baseline, int start, int end) {
	if (end != -1) {
	    t.setCaretPosition(baseline + start);
	    t.moveCaretPosition(baseline + end);
	    // to ensure selection visibility
	    t.getCaret().setSelectionVisible(true); 
	} else {
	    t.setCaretPosition(baseline + start);
	}
    }

    /** Loads and set up the plugins.
	Retrieves them from the "plugins" directory, located in the same
	directory as the executable JAR for TextTrix.class or the 
	"com" directory in the "com/textflex/texttrix" sequence holding
	TextTrix.class.
	TODO: also search the user's home directory or user determined
	locations, such as ones a user specifies via a preferences panel.
    */
    public void setupPlugins() {

	/* The code has a relatively elaborate mechanism to locate
	   the plugins folder and its JAR files.  Why not use
	   the URL that the Text Trix class supplies?
	   Text Trix needs to locate each JAR plugin's absolute path and name.
	   Text Trix's URL must
	   be truncated to its root directory's location and built
	   back up through the plugins directory.  Using getParentFile()
	   to the program's root and appending the rest of the 
	   path to the plugins allows one to use URLClassLoader directly with
	   the resulting URL.

	   Unfortunately, some systems do not
	   locate local files with this method.  The following
	   elaborate system works around this apparent JRE bug by
	   further breaking the URL into a normal path and loading
	   a file from it.

	   Unfortunately again, a new feature from JRE v.1.4
	   causes spaces in URL strings to be converted to "%20"
	   turning URL's into strings.  The JRE cannot load files
	   with "%20" in them, however; for example, 
	   "c:\Program Files\texttrix-x.y.z\plugins"
	   never gets loaded.  The workaround is to replace all "%20"'s in
	   the string with " ".  Along with v.1.4 comes new String regex
	   tools to make the operation simple, but prior versions
	   crash after a NoSuchMethodError.  The replacement must be done 
	   manually.
	*/

	// TODO: add additional plugins on the fly
	// this class's location
	String relClassLoc = "com/textflex/texttrix/TextTrix.class";
       	URL urlClassDir = ClassLoader.getSystemResource(relClassLoc);
	String strClassDir = urlClassDir.getPath(); // to check whether JAR
	File fileClassDir = new File(urlClassDir.getPath());
	File baseDir = null;
	// move into JAR's parent directory only if launched from a JAR
	if (strClassDir.indexOf(".jar!/" + relClassLoc) != -1) {
	    baseDir = fileClassDir.getParentFile().getParentFile()
		.getParentFile().getParentFile().getParentFile();
	} else { // not from JAR; one less parent directory
	    baseDir = fileClassDir.getParentFile().getParentFile()
		.getParentFile().getParentFile();
	}
	/* convert "%20", the escape character for a space, into " ";
	   conversion necessary starting with JRE v.1.4.0
	   (see http://developer.java.sun.com/developer/ //
	   bugParade/bugs/4466485.html)
	*/
	String strBaseDir = baseDir.toString();
	int space = 0;
	// continue while still have "%20", the spaces symbol
	while ((space = strBaseDir.indexOf("%20")) != -1) {
	    if (strBaseDir.length() > space + 3) {
		strBaseDir = strBaseDir.substring(0, space) 
		    + " " + strBaseDir.substring(space + 3);
	    } else {
		strBaseDir = strBaseDir.substring(0, space) + " ";
	    }
	}
	/* Though simpler, this alternative solution crashes 
	   after a NoSuchMethodError under JRE <= 1.3.
	*/
	/*
	baseDir = new File(baseDir.toString().replaceAll("%20", " "));
	File pluginsFile = new File(baseDir, "plugins");
	*/

	// plugins directory;
	// considered nonexistent since baseDir's path in URL syntax
       	File pluginsFile = new File(strBaseDir, "plugins");
	String pluginsPath = pluginsFile.getPath();

	// directory path given as URL; need to parse into normal syntax
	String protocol = "file:";
	int pathStart = pluginsPath.indexOf(protocol);
	// check if indeed given as URL;
	// if so, delete protocol and any preceding info
	if (pathStart != -1)
	    pluginsPath = pluginsPath.substring(pathStart + protocol.length());
	// pluginsPath now in normal syntax
	pluginsFile = new File(pluginsPath); // the actual file



	/* A possible workaround for an apparent JRE v.1.4 bug that
	   fails to open files with spaces in their paths.
	   This workaround converts any file or directory names
	   with their "8.3" formatted equivalents.
	   For example, "Program Files" is converted to 
	   "PROGRA~1", which some systems might map to the intended file.
	*/
	/*
	if (!pluginsFile.exists()) {
	    String seg = "";
	    StringTokenizer tok = new StringTokenizer(pluginsPath, "/\\");
	    StringBuffer buf = new StringBuffer(pluginsPath.length());
	    for (int i = 0; tok.hasMoreTokens(); i++) {
		seg = tok.nextToken();
		if (seg.length() > 8) 
		    seg = seg.substring(0, 6).toUpperCase() + "~1";
		buf.append(File.separator + seg);
	    }
	    pluginsPath = buf.toString();
	    pluginsFile = new File(pluginsPath); // the actual file
	    //	    System.out.println(pluginsPath);
	}
	*/

	// load the plugins and create actions for them
	plugIns = LibTTx.loadPlugIns(pluginsFile);
	if (plugIns != null) {
	    for (int i = 0; i < plugIns.length; i++) {
		makePlugInAction(plugIns[i]);
	    }
	}
    }

    /**Gets the last path for opening a file.
     * @return most recent path for opening a file
     */
    public static String getOpenDir() {
	return openDir;
    }

    /**Gets the last path for saving a file.
     * @return most recent path for saving a file
     */
    public static String getSaveDir() {
	return saveDir;
    }

    /**Gets the currently selected <code>TextPad</code>
     * @return <code>TextPad</code> whose tab is currently selected
     */
    public static TextPad getSelectedTextPad() {
	int i = tabbedPane.getSelectedIndex();
	if (i != -1) {
	    return (TextPad)textAreas.get(i);
	} else {
	    return null;
	}
    }

    /**Gets whether the auto-indent function is selected.
     * @return <code>true</code> if auto-indent is selected
     */
    public static boolean getAutoIndent() {
	return autoIndent.isSelected();
    }










    /** Adds a tab selection to the index record.
	Creates a trail of tab selections that the user can progress
	through forward or backward, similar to "Back" and "Forward"
	buttons in a web browser.  With each addition, the record
	shifts any potential duplicate selections to the head so that
	the user will only progress once through a given tab during
	a complete cycle in one direction.  The history expands
	as necessary, allowing an unlimited number of records.
	@param mostRecent the tab selection index to record
	@see #removeTabIndexHistory(int)
    */
    public void addTabIndexHistory(int mostRecent) {
	/* The current tabIndexHistoryIndex value refers to the next
	   available element in the tabIndexHistory array in which to add
	   tab selections.  Adding a tab selection places the tab index
	   value into this element and increments tabIndexHistoryIndex so 
	   that it continues to point to the next availabe position.
	   "tabIndexHistoryIndex - 1" now refers to the current tab,
	   while "tabIndexHistoryIndex - 2" refers to the last tab, the 
	   one to return to while going "Back".  The "Back" method must
	   therefore not only decrement tabIndexHistoryIndex, but also
	   refer to the position preceding the new index.
	*/
	boolean repeat = true;
	boolean shift = false;
	/*
	if (!(tabIndexHistoryIndex >= tabIndexHistory.length)
	    && tabIndexHistoryIndex >= 0
	    && tabIndexHistory[tabIndexHistoryIndex] != -1)
	    ++tabIndexHistoryIndex;
	*/
	//	System.out.println("mostRecent: " + mostRecent + "; tabIndexHistoryIndex: " + tabIndexHistoryIndex);
	for (int i = 0; i < tabIndexHistoryIndex && repeat; i++) {
	    // shift the records as necessary to move a potential
	    // duplicate to the front of the history
	    if (shift) { // shift the records
		if (tabIndexHistory[i] == -1) {
		    //		    tabIndexHistory[i] = mostRecent;
		    repeat = false;
		} else {
		    tabIndexHistory[i - 1] = tabIndexHistory[i];
		}
	    } else { // find where to start shifting, if necessary
		if (tabIndexHistory[i] == mostRecent) {
		    shift = true;
		} else if (tabIndexHistory[i] == -1) {
		    //		    tabIndexHistory[i] = mostRecent;
		    repeat = false;
		}
	    }
	}
	// add the tab selection
	if (shift) { // add the potential duplicate to the front of the record
	    //	    --tabIndexHistoryIndex; 
	    tabIndexHistory[--tabIndexHistoryIndex] = mostRecent;
	} else if (tabIndexHistoryIndex >= tabIndexHistory.length) {
	    // increase the array size if necessary
	    tabIndexHistory = (int[])LibTTx.growArray(tabIndexHistory);
	    tabIndexHistory[tabIndexHistoryIndex] = mostRecent;
	    for (int i = tabIndexHistoryIndex + 1; 
		 i < tabIndexHistory.length; i++)
		tabIndexHistory[i] = -1;
	    /*
	    --tabIndexHistoryIndex;
	    if (repeat) {
		System.arraycopy(tabIndexHistory, 1, 
				 tabIndexHistory, 0, tabIndexHistory.length - 1);
		tabIndexHistory[tabIndexHistory.length - 1] = mostRecent;
	    }
	    */
	} else if (tabIndexHistoryIndex >= 0) {
	    // ensure that the tab during TextTrix's startup has no entry;
	    // otherwise, the 0 tab selection index duplicates
	    tabIndexHistory[tabIndexHistoryIndex] = mostRecent;
	    /*
	    */
	}
	/*
	for (int i = 0; i < tabIndexHistory.length; i++) {
	    System.out.print("tabIndexHistory[" + i + "]: " + tabIndexHistory[i] + "; ");
	    System.out.println();
	}
	*/
	tabIndexHistoryIndex++;
    }

    /** Removes an entry from the tab selection history.
	Shifts the tab indices as necessary.
	@param removed index of removed tab
	@see #addTabIndexHistory(int)
    */
    public void removeTabIndexHistory(int removed) {
	boolean shift = false;
	//	System.out.print("removed: " + removed + "; tabIndexHistoryIndex: " + tabIndexHistoryIndex + "; new: ");
	// cycle through the entire history, removing the tab and shifting
	// both the tab indices and the tab history index appropriately
	for (int i = 0; i < tabIndexHistory.length; i++) {
	    // flag for a record shift and check whether to decrement
	    // the tab history index
	    if (tabIndexHistory[i] == removed) {
		if (i <= tabIndexHistoryIndex) --tabIndexHistoryIndex;
		shift = true;
	    }
	    // shift the tab record
	    if (shift) {
		tabIndexHistory[i] = (i < tabIndexHistory.length - 1)
		    ? tabIndexHistory[i + 1] : -1;
	    }
	    // decrease tab indices for those above the that of the removed tab
	    if (tabIndexHistory[i] > removed) {
		tabIndexHistory[i] = --tabIndexHistory[i];
	    }
	    /*
	    if (tabIndexHistory[i] == -1)
		tabIndexHistoryIndex = i;
	    */
	    //	    System.out.print(tabIndexHistory[i] + ",");
	}
	//	System.out.println();
	//	--tabIndexHistoryIndex;
    }



    /**Sets the given path as the most recently one used
     * to open a file.
     * @param anOpenDir path to set as last opened location
     */
    public static void setOpenDir(String anOpenDir) {
	openDir = anOpenDir;
    }

    /**Sets the given path as the most recently one used
     * to save a file.
     * @param aSaveDir path to set as last saved location.
     */
    public static void setSaveDir (String aSaveDir) {
	saveDir = aSaveDir;
    }

    public static void setAutoIndent(boolean b) {
	autoIndent.setSelected(b);
    }


    /**Makes new file with next non-existent file of name format,
     * <code>NewFile<i>n</i>.txt</code>, where <code>n</code>
     * is the next number that Text Trix has not made for 
     * a text area and that is not a curently existing file.
     * @return file with unique, non-existing name
     */
    public File makeNewFile() {
	File file;
	do {
	    fileIndex++; // ensures that no repeat of name in current session
	    // check exisiting files to ensure unique name
	} while ((file = new File("NewFile" + fileIndex + ".txt")).exists());
	// okay to return mutable file since created it anew in this fn?
	// true--prob only unwise if accessor, which gives instance field
	return file;
    }

    /** Displays a read-only file in a <code>Text Pad</code>.  
	Assumes the file exists and can be read as text.
	TODO: May change name to <code>readDocumentation</code> for 
	consistency with <code>readText</code>.
	@param path read-only text file's path.  Can also display
	non-read-only text files, but can't edit them.
	@return true if the file displays, false if otherwise
     */
    public boolean displayFile(String path) {
	InputStream in = null;
	BufferedReader reader = null;
	try {
	    // uses getResourceAsStream to ensure future usability as an applet
	    in = TextTrix.class.getResourceAsStream(path);
	    // TODO: notify user if file doesn't exist
	    if (in == null) return false;
	    reader = new BufferedReader(new InputStreamReader(in));
	    String text = readText(reader); // retrieve the text
	    addTextArea(textAreas, tabbedPane, new File(path));
	    TextPad t = (TextPad)textAreas.get(tabbedPane.getSelectedIndex());
	    t.setEditable(false); // so appropriate for read-only
	    t.setText(text);
	    t.setChanged(false);
	    updateTitle(textAreas, tabbedPane);
	    t.setCaretPosition(0);
	    return true;
	} finally {
	    try {
		if (reader != null) reader.close();
		if (in != null) in.close();
	    } catch (IOException e) {
		return false;
	    }
	}
    }
	
    /**Exits <code>TextTrix</code> by closing each tab individually,
     * checking for unsaved text areas in the meantime.
     */
    public static boolean exitTextTrix() {
	boolean b = true;
	int i = tabbedPane.getTabCount();
	// closes each tab individually, using the function that checks
	// for unsaved changes
	while (i > 0 && b) {
	    b = closeTextArea(i - 1, textAreas, tabbedPane);
	    i = tabbedPane.getTabCount();
	}
	if (b == true)
	    System.exit(0);
	return b;
    }
	
    /**Closes a text area.  Checks if the text area is unsaved;
     * if so, evokes a dialog asking whether the user wants to
     * save the text area, discard it, or cancel the closure.
     * If the file has not been saved before, a <code>Save as...</code>
     * dialog appears.  Canceling the <code>Save as...</code>
     * dialog discards the text area, though maybe not so in
     * future releases.
     * @param tabIndex tab to close
     * @param textAreas array of <code>TextPad</code>s
     * @param tabbedPane pane holding a tab to be closed
     * @return <code>true</code> if the tab successfully closes
     */
    public static boolean closeTextArea(int tabIndex, ArrayList textAreas,
					JTabbedPane tabbedPane) {
	boolean successfulClose = false;
		
	TextPad t = (TextPad)textAreas.get(tabIndex);
	// check if unsaved text area
	if (t.getChanged()) {
	    String s = "Please save first.";
	    tabbedPane.setSelectedIndex(tabIndex);
	    // dialog with 3 choices: save, discard, cancel
	    String msg = "This file has not yet been saved."
		+ "\nWhat would you like me to do with this new version?";
	    int choice = 
		JOptionPane.showOptionDialog(null,
					     msg,
					     "Save before close",
					     JOptionPane.WARNING_MESSAGE,
					     JOptionPane.DEFAULT_OPTION,
					     null,
					     new String[] { 
						 "Save", "Toss it out", 
						 "Cancel" },
					     "Save");
	    switch (choice) {
		// save the text area's contents
	    case 0:
		// bring up "Save as..." dialog if never saved file before
		if (t.fileExists()) {
		    successfulClose = saveFile(t.getPath());
		} else {
		    // still closes tab if cancel "Save as..." dialog
		    // may need to change in future releases
		    successfulClose = fileSaveDialog(null);
		}
		if (successfulClose) {
		    removeTextArea(tabIndex, textAreas, tabbedPane);
		}
		break;
		// discard the text area's contents
	    case 1:
		removeTextArea(tabIndex, textAreas, tabbedPane);
		successfulClose = true;
		break;
		// cancel the dialog and return unsuccessful closure;
		// could likely remove default case as well as case 2's break
	    case 2:
		successfulClose = false;
		break;
	    default:
		successfulClose = false;
		break;
	    }
	    // if unchanged, simply remove the tab
	} else {
	    removeTextArea(tabIndex, textAreas, tabbedPane);
	    successfulClose = true;
	}
	return successfulClose;
    }

    /**Read in text from a file and return the text as a string.
     * Differs from <code>displayFile(String path)</code> because
     * allows editing.
     * @param path text file stream
     * @return text from file
     */
    public String readText(String path) {
	String text = "";
	InputStream in = null;
	BufferedReader reader = null;
	try {
	    in = TextTrix.class.getResourceAsStream(path);
	    // TODO: notify user if file doesn't exist
	    if (in == null) return "";
	    reader = new BufferedReader(new InputStreamReader(in));
	    String line;
	    while ((line = reader.readLine()) != null)
		text = text + line + "\n";
	} catch(IOException exception) {
	    //	    exception.printStackTrace();
	    return "";
	} finally {
	    try {
		if (reader != null) reader.close();
		if (in != null) in.close();
	    } catch(IOException exception) {
		//	    exception.printStackTrace();
		return "";
	    }
	}
	return text;
    }
	
    /**Read in text from a file and return the text as a string.
     * Differs from <code>displayFile(String path)</code> because
     * allows editing.
     * @param reader text file stream
     * @return text from file
     */
    public String readText(BufferedReader reader) {
	String text = "";
	String line;
	try {
	    while ((line = reader.readLine()) != null)
		text = text + line + "\n";
	} catch(IOException exception) {
	    exception.printStackTrace();
	}
	return text;
    }

    /**Creates a new <code>TextPad</code> object, a text area 
     * for writing, and gives it a new tab.  Can call for
     * each new file; names the tab, <code>Filen.txt</code>,
     * where <code>n</code> is the tab number.
     */
    public void addTextArea(ArrayList arrayList, JTabbedPane tabbedPane, 
			    File file) {
	TextPad textPad = new TextPad(file);
	// final variables so can use in inner class;
			
	JScrollPane scrollPane = 
	    new JScrollPane(textPad, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
			    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	//		scrollPane.setRequestFocusEnabled(false);
	DocumentListener listener = new TextPadDocListener();
			
	// must add to array list before adding scroll pane to tabbed pane
	// or else get IndexOutOfBoundsException from the array list
	arrayList.add(textPad);
	// 1 more than highest tab index since will add tab
	int i = tabbedPane.getTabCount();
	tabbedPane.addTab(file.getName() + " ", scrollPane);
	//		textPad.setLineWrap(true);
	//		textPad.setWrapStyleWord(true);
	textPad.getDocument().addDocumentListener(listener);
	textPad.addMouseListener(new PopupListener());
	// show " *" in tab title when text changed
	tabbedPane.setSelectedIndex(i);
	tabbedPane.setToolTipTextAt(i, textPad.getPath());
	//	textPad.requestFocusInWindow();
    }

    /**Changes tabbed pane title to indicate whether the file's changes 
     * have been changed or not.
     * Appends "<code> *</code>" if the file has unsaved changes; 
     * appends "<code>  </code>" otherwise.
     * @param arrayList array of <code>TextPad</code>s that the 
     * tabbed pane displays
     * @param tabbedPane tabbed pane to update
     */
    public static void updateTitle(ArrayList arrayList, JTabbedPane tabbedPane) {
	int i = tabbedPane.getSelectedIndex();
	TextPad textPad = (TextPad)arrayList.get(i);
	//	String title = tabbedPane.getTitleAt(i);
	String title = textPad.getFilename();
	// convert to filename; -2 b/c added 2 spaces
	if (textPad.getChanged()) {
	    tabbedPane.setTitleAt(i, title + "*");
	} else {
	    tabbedPane.setTitleAt(i, title + " ");
	    //		setTitleAt(i, title.substring(0, title.length() - 1) + " ");
	}
    }

    /**Adds additional listeners and other settings to a <code>TextPad</code>.
     * Useful to apply on top of the <code>TextPad</code>'s 
     * <code>applyDocumentSettings</code> function.
     * @param textPad <code>TextPad</code> requiring applied settings
     */
    public void addExtraTextPadDocumentSettings(TextPad textPad) {
	textPad.getDocument().addDocumentListener(new TextPadDocListener());
	textPad.setChanged(true);
	updateTitle(textAreas, tabbedPane);
    }

    /**Displays the given <code>TextPad</code> in plain text format.
     * Calls the <code>TextPad</code>'s <code>viewPlain</code> function
     * before adding <code>TextTrix</code>-specific settings, such as 
     * a <code>TextPadDocListener</code>.
     */
    public void viewPlain() {
	TextPad t = getSelectedTextPad();
	if (t != null) {
	    if (!t.getContentType().equals("text/plain")) {
		t.viewPlain();
		addExtraTextPadDocumentSettings(t);
	    }
	}
    }
	
    /**Displays the given <code>TextPad</code> in html text format.
     * Calls the <code>TextPad</code>'s <code>viewHTML</code> function
     * before adding <code>TextTrix</code>-specific settings, such as 
     * a <code>TextPadDocListener</code>.
     */
    public void viewHTML() {
	TextPad t = getSelectedTextPad();
	if (t != null) {
	    if (!t.getContentType().equals("text/html")) {
		t.viewHTML();
		addExtraTextPadDocumentSettings(t);
	    }
	}
    }
	
    /**Displays the given <code>TextPad</code> in RTF text format.
     * Calls the <code>TextPad</code>'s <code>viewRTF</code> function
     * before adding <code>TextTrix</code>-specific settings, such as 
     * a <code>TextPadDocListener</code>.
     */
    public void viewRTF() {
	TextPad t = getSelectedTextPad();
	if (t != null) {
	    if (!t.getContentType().equals("text/rtf")) {
		t.viewRTF();
		addExtraTextPadDocumentSettings(t);
	    }
	}
    }

    /**Front-end to the <code>TextPad</code>'s <code>read</code> method from 
     * <code>JTextPane</code>.
     * Reads in a file, applies <code>TextPad</code>'s settings, and
     * finally adds <code>TextTrix</code>-specific settings.
     * @param textPad <code>TextPad</code> to read a file into
     * @param in file reader
     * @param desc reader stream description
     */
    public void read(TextPad textPad, Reader in, Object desc) throws IOException {
	textPad.read(in, desc);
	//	textPad.setDefaultTabs(4);
	textPad.applyDocumentSettings();
	addExtraTextPadDocumentSettings(textPad);
    }

    /**Removes a tab containing a text area.
     * @param i tab index
     * @param l text area array list
     * @param tp tabbed pane from which to remove a tab
     */
    public static void removeTextArea(int i, ArrayList l, JTabbedPane tp) {
	l.remove(i);
	tp.remove(i);
    }

    /**Adds a new component to the <code>GridBagLayout</code>
     * manager.
     * @param c component to add
     * @param constraints layout constraints object
     * @param x column number
     * @param y row number
     * @param w number of columns to span
     * @param h number of rows to span
     * @param wx column weight
     * @param wy row weight
     */
    public void add(Component c, GridBagConstraints constraints,
		    int x, int y, int w, int h, 
		    int wx, int wy) {
	constraints.gridx = x;
	constraints.gridy = y;
	constraints.gridwidth = w;
	constraints.gridheight = h;
	constraints.weightx = wx;
	constraints.weighty = wy;
	getContentPane().add(c, constraints);
    }

    /**Saves text area contents to a given path.
     * @param path file path in which to save
     * @return true for a successful save, false if otherwise
     */
    public static boolean saveFile(String path) {
	//	System.out.println("printing");
	TextPad t = getSelectedTextPad();
	PrintWriter out = null;
	try {
	    if (t != null) {
		File f = new File(path);
		/* if don't use canWrite(), work instead by 
		   catching exception and either handling it there
		   or returning signal of the failure
		 */
		/*
		// ensure that the file can be written
		if (f.canWrite())
		    System.out.println("can write");
		else 
		    System.out.println("can't write");
		*/
		// open the stream to write to
		out = new 
		    PrintWriter(new FileWriter(path), true);
		// write to it
		out.print(t.getText());
		t.setChanged(false);
		t.setFile(path);
		// the the tab title to indicate that no unsaved changes;
		// in place of updateTitle b/c static context
		//		tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), 
		//				      t.getName() + " ");
		//		tabbedPane.setToolTipTextAt(tabbedPane.getSelectedIndex(), 
		//					    t.getPath());
		updateTitle(textAreas, tabbedPane);
		return true;
		/*
	    } else {
		return false;
		*/
	    }
	} catch(IOException e) {
	    //	    e.printStackTrace();
	    return false;
	} finally { // release system resources from stream
	    if (out != null) out.close();
	}
	return false;
    }

    /** Opens a file into a text pad.
	Calls the file open dialog.
	Opens the file into a new pad unless the currently selected one
	is empty.
	Sets the file's name as a the tab's title and the path as the
	tab's tool tip.
	Assumes that the file is readable as text.
	@param file file to open
    */
    public boolean openFile(File file) {
	String path = file.getPath();
	// ensures that the file exists and is not a directory
	if (file.canRead()) { // readable file
	    TextPad t = getSelectedTextPad();
	    BufferedReader reader = null;
	    try {
		reader = new BufferedReader(new FileReader(path));
		    
		// check if tabs exist; get TextPad if true
		/* t.getText() != null, even if have typed nothing 
		   in it.
		   Add tab and set its text if no tabs exist or 
		   if current tab has tokens; set current tab's 
		   text otherwise
		*/
		if (t == null || !t.isEmpty()) {  // open file in new pad
		    addTextArea(textAreas, tabbedPane, file);
		    t = (TextPad)textAreas.get(tabbedPane
					       .getSelectedIndex());
		    read(t, reader, path);
		} else { // open file in current, empty pad
		    read(t, reader, path);
		}
		t.setCaretPosition(0);
		t.setChanged(false);
		t.setFile(path);
		tabbedPane.setToolTipTextAt(tabbedPane.getSelectedIndex(), 
					    t.getPath());
		updateTitle(textAreas, tabbedPane);
		// set the path to the last opened directory
		setOpenDir(file.getParent());
		// file.getParent() returns null when opening file
		// from the command-line and passing in a relative path
		if (getOpenDir() == null) 
		    setOpenDir(System.getProperty("user.dir"));
		return true;
	    } catch(IOException exception) {
		//		exception.printStackTrace();
		return false;
	    } finally {
		try { 
		    if (reader != null) reader.close();
		} catch (IOException e) { 
		    //    e.printStackTrace(); 
		    return false;
		}
	    }
	    /*
	} else if (file.isDirectory()) { // directory: can't open
	    System.out.println(path + " is a directory");
	} else if (!file.exists()) { // non-existent
	    System.out.println(path + " does not exist");
	    */
	}
	return false;
    }


    /** Evokes a save dialog, with a filter for text files.
	Sets the tabbed pane tab to the saved file name.
	@param owner parent frame; can be null
	@return true if the approve button is chosen, false if otherwise
     */
    public static boolean fileSaveDialog(JFrame owner) {
	//	int tabIndex = tabbedPane.getSelectedIndex();
	TextPad t = getSelectedTextPad();
	//	if (tabIndex != -1) {
	if (t != null) {
	    //	    TextPad t = (TextPad)textAreas.get(tabIndex);
	    if (t.fileExists()) {
		chooser.setCurrentDirectory(new File(t.getDir()));
		// save to file's current location
		chooser.setSelectedFile(new File(t.getPath()));
	    } else {
		// if the file hasn't been created, default to the directory
		// last saved to
		chooser.setCurrentDirectory(new File(saveDir));
	    }
	    // can't save to multiple files;
	    // if set to true, probably have to use double-quotes
	    // when typing names
	    chooser.setMultiSelectionEnabled(false); 
	    return getSavePath(owner);
	}
	return false;
    }

    /** Helper function to <code>fileSaveDialog</code>.
	Opens the file save dialog to retrieve the file's new name.
	If the file will overwrite another file, prompts the user
	with a dialog box to determine whether to continue with the 
	overwrite, get another name, or cancel the whole operation.
	@param owner the frame to which the dialog will serve; can be null
	@return true if the file is saved successfully
    */
    private static boolean getSavePath(JFrame owner) {
	boolean repeat = false;
	// repeat the retrieval until gets an unused file name, 
	// overwrites a used one, or the user cancels the save
	do {
	    // display the file save dialog
	    int result = chooser.showSaveDialog(owner);
	    if (result == JFileChooser.APPROVE_OPTION) { // save button chosen
		String path = chooser.getSelectedFile().getPath();
		File f = new File(path);
		int choice = 0;
		// check whether a file by the chosen name already exists
		if (f.exists()) {
		    String overwrite = path 
			+ "\nalready exists.  Should I overwrite it?";
		    String[] options = { "But of course",
					 "Please, no!",
					 "Cancel" };
		    // dialog warning of a possible overwrite
		    choice 
			= JOptionPane
			.showOptionDialog(owner,
					  overwrite,
					  "Overwrite?",
					  JOptionPane
					  .YES_NO_CANCEL_OPTION,
					  JOptionPane
					  .WARNING_MESSAGE,
					  null,
					  options,
					  options[1]);
		}
		if (choice == 1) { // don't overwrite, but choose another name
		    repeat = true;
		} else if (choice == 2) { // don't overwrite.
		    return false;
		} else { // write, even if overwriting
		    // try to save the file and check if successful
		    if (saveFile(path)) { // success
			setSaveDir(chooser.getSelectedFile().getParent());
			tabbedPane.setToolTipTextAt(tabbedPane
						    .getSelectedIndex(), 
						    path);
			return true;
		    } else { // fail; request another try at saving
			String msg = path + " couldn't be written to "
			    + "that location.\nWould you like to try "
			    + "another directory or filename?";
			String title = "Couldn't save";
			repeat = yesNoDialog(owner, msg, title);
		    }
		}
	    } else { // cancel button chosen
		return false;
	    }
	} while (repeat); // repeat if retrying save after failure
	return false;
    }

    /** Front-end, helper function to ask yes/no questions.
	@param owner parent frame; can be null
	@param msg message to display in the main window section
	@param title title to display in the title bar
	@return true for "Yes", false for "No"
    */
    private static boolean yesNoDialog(JFrame owner, String msg, 
				String title) {
	int choice = JOptionPane
	    .showConfirmDialog(owner,
			       msg,
			       title,
			       JOptionPane.YES_NO_OPTION,
			       JOptionPane.
			       QUESTION_MESSAGE);
	// true for Yes, false for No
	if (choice == JOptionPane.YES_OPTION) {
	    return true;
	} else {
	    return false;
	}
    }


    /**Evokes a open file dialog, from which the user can
     * select a file to display in the currently selected tab's
     * text area.  Filters for text files, though provides
     * option to display all files.
     */
    private class FileOpenAction extends AbstractAction {
	JFrame owner;
	
	/** Constructs the file open action
	    @param aOwner the parent frame
	    @param name the action's name
	    @param icon the action's icon
	*/
	public FileOpenAction(JFrame aOwner, String name, Icon icon) {
	    owner = aOwner;
	    putValue(Action.NAME, name);
	    putValue(Action.SMALL_ICON, icon);
	}

	/** Displays a file open chooser when the action is invoked.
	    Defaults to the directory from which the last file was
	    opened or, if no files have been opened, to the user's home
	    directory.
	    @param evt action invocation
	*/
	public void actionPerformed(ActionEvent evt) {
	    TextPad t = getSelectedTextPad();
	    // File("") evidently brings file dialog to last path, 
	    // whether last saved or opened path

	    /* getSelectedFiles() returns an array of length 0
	       with the following sequence:
	       -file opened
	       -file saved via save chooser
	       -same file opened by double-clicking

	       In other words, a file just saved cannot be
	       reopened.
		   
	       The problem does not appear when the chooser accept 
	       button is chosen, a directory is changed before the file
	       is chosen, or the name in the text input area is altered
	       in any way.

	       UPDATED: Workaround by calling 
	       setMultiSelectionEnabled with 
	       "true", "false", and "true" arguments in succession. 
		       
	       OLD: The workaround is to use getSelectedFile()
	       if the array has length 0; 
	       getSelectedFile() for some reason
	       works though its multi-partner does not.
	       The file-not-found dialogs refrain from specifying
	       the chosen file name since it cannot be retrieved
	       from chooser.getSelectedFile() in the situation
	       where the array has length 0.
	    */

	    /* WORKAROUND: call true, false, true on setMultiSelectionEnabled
	       to ensure that the same file can be opened
	    */
	    chooser.setMultiSelectionEnabled(true); 
	    chooser.setMultiSelectionEnabled(false); 
	    chooser.setMultiSelectionEnabled(true); 
	    String dir = openDir;
	    //	    System.out.println("Here");
	    if (t != null && (dir = t.getDir()).equals("")) 
		dir = openDir;
	    //	    System.out.println("dir: " + dir);
	    chooser.setCurrentDirectory(new File(dir));
	    // allows one to open multiple files;
	    // must disable for save dialog
	    
	    // displays the dialog and opens all files selected
	    boolean repeat = false;
	    do {
		int result = chooser.showOpenDialog(owner);
		// bring up the dialog and retrieve the result
		if (result == JFileChooser.APPROVE_OPTION) { // Open button
		    String msg = "";
		    String title = "Couldn't open";
		    File[] files = chooser.getSelectedFiles();
		    boolean allFound = true;
		    for (int i = 0; i < files.length; i++) {
			if (!openFile(files[i])) // record unopened files
			    msg = msg + files[i] + "\n";
		    }
		    // request another opportunity to open files if any
		    // failures
		    if (msg.equals("")) { // no unopened files
			repeat = false;
		    } else { // some files left unopened
			// notify the user which files couldn't be opened
			msg = "The following files couldn't be opened:\n"
			    + msg + "Would you like to try again?";
			/*
			String msg = "";
			for (int j = 0; j < unopenables.length; j++) {
			    if (j == unopenables - 1) {
				msg = msg + unopenables[j];
			    } else {

			String msg = unopenables + "wasn't
			+ "found.\nWould you like to try again?";
			*/
			// request another chance to open them or other files
			repeat = yesNoDialog(owner, msg, title);
		    }
		    /* Original workaround.
		       Utilizes the fact that getSelectedFiles() returns an 
		       array of length 0, which getSelectedFile() returns the
		       intended file object.
		    */
		    /*
		    if (files.length == 0) {
			File f1 = chooser.getSelectedFile();
			System.out.println(f1.getPath());
			// TODO: dialog informing that the file doesn't exist
			if (f1 != null) {
			    openFile(f1);
			    repeat = false;
			} else {
			    repeat = yesNoDialog(owner, msg, title);
			}
		    } else {
			//		System.out.println("files: " + files.length + ", file: ");// + f1.exists());
			boolean allFound = true;
			for (int i = 0; i < files.length; i++) {
			    if (!openFile(files[i]))
				allFound = false;
			}
			if (allFound) {
			    repeat = false;
			} else {
			    repeat = yesNoDialog(owner, msg, title);
			}
		    }
		    */
		} else { // Cancel button
		    repeat = false;
		}
	    } while (repeat); // repeat if failed opens for user to retry
	}

    }
	
    /**Responds to user input calling for a save dialog.
     */
    private class FileSaveAction extends AbstractAction {
	JFrame owner;
		
	/** Constructs the file open action
	    @param aOwner the parent frame
	    @param name the action's name
	    @param icon the action's icon
	*/
	public FileSaveAction(JFrame aOwner, String name, Icon icon) {
	    owner = aOwner;
	    putValue(Action.NAME, name);
	    putValue(Action.SMALL_ICON, icon);
	}
		
	/** Displays a file save chooser when the action is invoked.
	    @param evt action invocation
	    @see #fileSaveDialog(JFrame)
	*/
	public void actionPerformed(ActionEvent evt) {
	    fileSaveDialog(owner);
	}
    }

    /**Responds to changes in the <code>TextPad</code> text areas.
     * Updates the titles to reflect text alterations.
     */
    private class TextPadDocListener implements DocumentListener {
	
	/**Flags a text insertion.
	 * @param e insertion event
	 */
	public void insertUpdate(DocumentEvent e) {
	    ((TextPad)textAreas.get(tabbedPane.getSelectedIndex()))
		.setChanged(true);
	    updateTitle(textAreas, tabbedPane);
	}

	/**Flags a text removal.
	 * @param e removal event
	 */
	public void removeUpdate(DocumentEvent e) {
	    ((TextPad)textAreas.get(tabbedPane.getSelectedIndex()))
		.setChanged(true);
	    updateTitle(textAreas, tabbedPane);
	}

	/**Flags any sort of text change.
	 * @param e any text change event
	 */
	public void changedUpdate(DocumentEvent e) {
	}
    }

    private class PopupListener extends MouseAdapter {
	public void mousePressed(MouseEvent e) {
	    if (e.isPopupTrigger()) {
		popup.show(e.getComponent(), e.getX(), e.getY());
	    }
	}

	public void mouseReleased(MouseEvent e) {
	    if (e.isPopupTrigger()) {
		popup.show(e.getComponent(), e.getX(), e.getY());
	    }
	}
    }

    /** Solicits user command-line input on an independent thread.
     */
    private class Focuser implements Runnable {
	/* Components within JTabbedPane tabs apparently do not respond to 
	   requestFocusInWindow() until the tab becomes visible.  
	   If a JTextArea within a JTabbedPane requests focus via within
	   the tabbed pane's change listener, for example, the text area
	   only receives focus sporadically.  Often the cursor merely
	   blinks once in the area before disappearing.
	   
	   The potential solution is to wait for the tab to become 
	   visible before requesting focus.  The action creating 
	   the tab seems like a suitable place to wait and make 
	   the request.  During tests, the component still does 
	   not receive focus even after a delay, however, possibly because
	   the action must finish before the GUI updates itself.
	   The alternative is to start a thread to wait and requests.  
	   While the thread pauses, the the change listener processor can
	   come to completion and display the tab before the thread
	   requests focus.  The thread does not know precisely when the
	   tab becomes visible, however; the tab is enabled, but 
	   evidently not yet visible.  Rather than guess how long to
	   delay, the thread delays for a brief period, request focus, 
	   and repeats itself as many time as necessary.  Not until
	   the component reports that it has focus does the thread
	   stop itself.  Systems may vary in their delay depending on
	   processing speed and other specs, but the thread will make
	   as many requests as necessary anyway.
	   
	   An apparent JRE bug causes the component's focus
	   report have a delayed update, so that the text area continues
	   to say that it is not focused even after the cursor begins
	   to blink in the area.  Nonetheless, the component
	   eventually does update its focus report, and the thread can
	   know to stop.
	*/
	private Thread thread = null;

	public Focuser() {
	}

	/** Starts the thread.
	*/
	public void start() {
	    if (thread == null) {
		thread = new Thread(this, "Thread");
		thread.start();
	    }
	}
    
	/** Requests user command-line input.
	    Does not exit until the user hits "Return".
	*/
	public void run() {
	    TextPad pad = getSelectedTextPad();
	    //		    pad.requestFocusInWindow();
	    while (thread != null && pad != getFocusOwner()) {
		try {
		    //		    System.out.println("tab enabled: " + tabbedPane.isEnabledAt(tabbedPane.getSelectedIndex()));
		    pad.requestFocusInWindow();
		    Thread.sleep(20);
		} catch (InterruptedException e) {}
	    }
	    //	    System.out.println("...done");
	    stop();
	}

	/** Stops the thread and resets the input value.
	 */
	public void stop() {
	    thread = null;
	}
    }
    /**Responds to tab changes in the JTabbedPane.
     * Not presently working.
     *
     private class TabSwitchListener implements ChangeListener {
     public void stateChanged(ChangeEvent e) {
     if (tabbedPane.getTabCount() > 0) {
     getSelectedTextPad().requestFocusInWindow();
     setPrevTabIndex(getCurrentTabIndex());
     setCurrentTabIndex(tabbedPane.getSelectedIndex());
     }
     }
     }
    */
    /*	
      private class TextPadTabbedPane extends JTabbedPane {
      public TextPadTabbedPane(int tabPlacement) {
      super(tabPlacement);
      }

      public static ChangeListener createChangeListener() {
      return new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
      System.out.println("I'm here: " + tabbedPane.getSelectedIndex());
      TextPad t = null;
      if ((t = getSelectedTextPad()) != null) {
      t.setVisible(true);
      t.requestFocus();
      }
      }
      };
      }
      }
    */
}

/**Filters for files with specific extensions.
 */
class ExtensionFileFilter extends FileFilter {
    private String description = "";
    private ArrayList extensions = new ArrayList();

    /**Add extension to include for file display.
     * May need to modify to check whether extension has already
     * been added.
     * @param file extension, such as <code>.txt</code>, though
     * the period is optional
     */
    public void addExtension(String extension) {
	if (!extension.startsWith("."))
	    extension = "." + extension;
	extensions.add(extension.toLowerCase());
    }

    /**Sets the file type description.
     * @param aDescription file description, such as <code>text
     * files</code> for <code>.txt</code> files
     */
    public void setDescription(String aDescription) {
	description = aDescription;
    }

    /**Gets file type's description.
     * @return file type description.
     */
    public String getDescription() {
	return description;
    }

    /**Accept a given file to display if it has the extension
     * currently being filtered for.
     * @param f file whose extension need to check
     * @return <code>true</code> if accepts file, <code>false</code>
     * if don't
     */
    public boolean accept(File f) {
	if (f.isDirectory()) 
	    return true;
	String name = f.getName().toLowerCase();

	for (int i = 0; i < extensions.size(); i++)
	    if (name.endsWith((String)extensions.get(i)))
		return true;
	return false;
    }
}

