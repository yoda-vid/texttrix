/* TextTrix.java
 * Text Trix
 * Meaningful Mistakes
 * http://texttrix.sourceforge.net
 * http://sourceforge.net/projects/texttrix

 * Copyright (c) 2002, David Young
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Text Trix nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package net.sourceforge.texttrix;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.filechooser.FileFilter;
import java.net.*;
import javax.swing.event.*;

/**The main <code>TextTrix</code> class.
 * Takes care of most graphical user interface operations, such as 
 * setting up and responding to changes in the <code>Text Pad</code>s, 
 * tool bar, menus, and dialogs.
*/
public class TextTrix extends JFrame {
	// to keep track of all the TextPads
    private static ArrayList textAreas = new ArrayList();
	// tabbed window for multiple TextPads
    private static JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	// file open/save dialog
    private static JFileChooser chooser = new JFileChooser();
	// most recently path opened to
	private static String openPath = "";
	// most recently path saved to
	private static String savePath = "";
	// for giving each TextPad a unique name
	private static int fileIndex = 0;
	// find dialog
	private static FindDialog findDialog;

	
	private static int currentTabIndex = 0;
	private static int prevTabIndex = 0;

	/**Constructs a new <code>TextTrix</code> frame and
	 * <code>TextPad</code> to begin with.
	 */
    public TextTrix() {
	setTitle("Text Trix");
	// pre-set window size; may change to adjust to user's screen size
	setSize(500, 600);

	// make first tab and text area
	addTextArea(textAreas, tabbedPane, makeNewFile());

//	tabbedPane.addChangeListener(new TabSwitchListener());

	// make menu bar and menus
	JMenuBar menuBar = new JMenuBar();
	JMenu fileMenu = new JMenu("File");
	fileMenu.setMnemonic('F');
	JMenu editMenu = new JMenu("Edit");
	editMenu.setMnemonic('E');
	JMenu mistakerMenu = new JMenu("Mistaker");
	mistakerMenu.setMnemonic('M');
	JMenu toolsMenu = new JMenu("Tools");
	toolsMenu.setMnemonic('T');
	JMenu helpMenu = new JMenu("Help");
	helpMenu.setMnemonic('H');

	// make tool bar
	JToolBar toolBar = new JToolBar("Functions and features");

	/* File menu items */

	// make new tab and text area
	Action newAction = new AbstractAction("New") {
		public void actionPerformed(ActionEvent evt) {
		    addTextArea(textAreas, tabbedPane, makeNewFile());
		}
	    };
	// Mozilla tabbed-pane key-binding
	setAction(newAction, "New", 'N', KeyStroke.getKeyStroke(KeyEvent.VK_T,
						      InputEvent.CTRL_MASK));
	fileMenu.add(newAction);
	/* move to tabs using default Java key-bindings:
	 * Ctrl-up to tabs
	 * Lt, rt to switch tabs
	 * Tab back down to TextPad */

	// (ctrl-o) open file; use selected tab if empty
	Action openAction = new FileOpenAction(TextTrix.this, "Open", makeIcon("images/openicon-16x16.png"));
	setAction(openAction, "Open", 'O', KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.CTRL_MASK));
	fileMenu.add(openAction);
	JButton openButton = toolBar.add(openAction);
	openButton.setBorderPainted(false);
	setRollover(openButton, "images/openicon-roll-16x16.png");

	// set "*.txt" and "*.html" file filters for open/save dialog boxes
	final ExtensionFileFilter webFilter = new ExtensionFileFilter();
	webFilter.addExtension("html");
	webFilter.addExtension("htm");
	webFilter.addExtension("xhtml");
	webFilter.addExtension("shtml");
	webFilter.setDescription("Web files (*.html, *.htm, *.xhtml, *.shtml)");
	chooser.setFileFilter(webFilter);

	final ExtensionFileFilter txtFilter = new ExtensionFileFilter();
	txtFilter.addExtension("txt");
	txtFilter.setDescription("Text files (*.txt)");
	chooser.setFileFilter(txtFilter);
	
	// close file; check if saved
	Action closeAction = new AbstractAction("Close", makeIcon("images/closeicon-16x16.png")) {
		public void actionPerformed(ActionEvent evt) {
			int i = tabbedPane.getSelectedIndex();
			closeTextArea(i, textAreas, tabbedPane);
		}
	};
	setAction(closeAction, "Close", 'C');
	fileMenu.add(closeAction);

	// (ctrl-s) save file; no dialog if file already created
	Action saveAction = new AbstractAction("Save", makeIcon("images/saveicon-16x16.png")) {
		public void actionPerformed(ActionEvent evt) {
			// can't use tabbedPane.getSelectedComponent() b/c returns JScrollPane
			TextPad t = (TextPad)textAreas
				.get(tabbedPane.getSelectedIndex());
			// save directly to file if already created one
			if (t.fileExists())
				saveFile(t.getPath());
			// otherwise, request filename for new file
			else
				fileSaveDialog(TextTrix.this);
		}
	};
	setAction(saveAction, "Save", 'S', KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK));
	fileMenu.add(saveAction);
	JButton saveButton = toolBar.add(saveAction);
	saveButton.setBorderPainted(false);
	setRollover(saveButton, "images/saveicon-roll-16x16.png");

	// save w/ file save dialog
	Action saveAsAction = new FileSaveAction(TextTrix.this, "Save as...", 
			makeIcon("images/saveasicon-16x16.png"));
	setAction(saveAsAction, "Save as...", '.');
	fileMenu.add(saveAsAction);

	// Start exit functions
	fileMenu.addSeparator();
	
	// (ctrl-q) exit file; close each tab separately, checking if each saved
	Action exitAction = new AbstractAction("Exit") {
		public void actionPerformed(ActionEvent evt) {
			exitTextTrix();
		}
	    };
	setAction(exitAction, "Exit", 'E', KeyStroke.getKeyStroke(KeyEvent.VK_Q,
						       InputEvent.CTRL_MASK));
	fileMenu.add(exitAction);

	/* Edit menu items */

	// (ctrl-z) undo; multiple undos available
	Action undoAction = new AbstractAction("Undo") {
		public void actionPerformed(ActionEvent evt) {
			((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).undo();
		}
	};
	setAction(undoAction, "Undo", 'U', KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				InputEvent.CTRL_MASK));
	editMenu.add(undoAction);

	// (ctrl-y) redo; multiple redos available
	Action redoAction = new AbstractAction("Redo") {
		public void actionPerformed(ActionEvent evt) {
			((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).redo();
		}
	};
	setAction(redoAction, "Redo", 'R', KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				InputEvent.CTRL_MASK));
	editMenu.add(redoAction);

	// Start Cut, Copy, Paste actions
	editMenu.addSeparator();
	
	// (ctrl-x) cut
	Action cutAction = new AbstractAction("Cut") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).cut();
		}
	};
	setAction(cutAction, "Cut", 'C', KeyStroke.getKeyStroke(KeyEvent.VK_X,
						      InputEvent.CTRL_MASK));
	editMenu.add(cutAction);

	// (ctrl-c) copy
	Action copyAction = new AbstractAction("Copy") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).copy();
		}
	};
	setAction(copyAction, "Copy", 'O', KeyStroke.getKeyStroke(KeyEvent.VK_C,
						       InputEvent.CTRL_MASK));
	editMenu.add(copyAction);

	// (ctrl-v) paste
	Action pasteAction = new AbstractAction("Paste") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).paste();
		}
	};
	setAction(pasteAction, "Paste", 'P', KeyStroke.getKeyStroke(KeyEvent.VK_V,
							InputEvent.CTRL_MASK));
	editMenu.add(pasteAction);

	// Start selection items
	editMenu.addSeparator();

	// select all text in current text area
	Action selectAllAction = new AbstractAction("Select all") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).selectAll();
		}
	};
	setAction(selectAllAction, "Select all", 'S', KeyStroke.getKeyStroke(KeyEvent.VK_L,
				InputEvent.CTRL_MASK));
	editMenu.add(selectAllAction);
	
	/* Help menu items */

	// about Text Trix, incl copyright notice and version number
	Action aboutAction = new AbstractAction("About...") {
		public void actionPerformed(ActionEvent evt) {
			String text = "";
			text = readText("about.txt");
			JOptionPane.showMessageDialog(null, text, "About Text Trix", 
					JOptionPane.PLAIN_MESSAGE, 
					makeIcon("images/texttrixsignature.png"));
		}
	};
	setAction(aboutAction, "About...", 'A');
	helpMenu.add(aboutAction);

	// shortcuts and goofy features description; opens new tab
	Action shortcutsAction = new AbstractAction("Shortcuts") {
		public void actionPerformed(ActionEvent evt) {
			// reads from "shortcuts.txt" in same directory as this class
			String path = "shortcuts.txt";
			displayFile(path);
		}
	};
	setAction(shortcutsAction, "Shortcuts", 'S');
	helpMenu.add(shortcutsAction);

	// license; opens new tab
	Action licenseAction = new AbstractAction("License") {
		public void actionPerformed(ActionEvent evt) {
			// reads from "license.txt" in same directory as this class
			String path = "license.txt";
			displayFile(path);
		}
	};
	setAction(licenseAction, "License", 'L');
	helpMenu.add(licenseAction);

	Action findAction = new AbstractAction("Find", makeIcon("images/find-16x16.png")) {
		public void actionPerformed(ActionEvent evt) {
			if (findDialog == null) 
				findDialog = new FindDialog(TextTrix.this);
			findDialog.show();
		}
	};
	setAction(findAction, "Find", 'F');
	toolsMenu.add(findAction);
	JButton findButton = toolBar.add(findAction);
	findButton.setBorderPainted(false);
	setRollover(findButton, "images/find-roll-16x16.png");
	findButton.setToolTipText(readText("find.html"));

	// Text Trix's first "goofy" function! (it's actually a practical one)
	Action removeReturnsAction = new AbstractAction("Remove extra hard returns", 
			makeIcon("images/returnicon-16x16.png")) {
		public void actionPerformed(ActionEvent evt) {
		    TextPad t = (TextPad)textAreas.get(tabbedPane.getSelectedIndex());
			// may need to add original text to history buffer
			// before making the change
		    t.setText(Tools.removeExtraHardReturns(t.getText()));
		}
	};
	setAction(removeReturnsAction, "Remove extra hard returns", 'R');
	toolsMenu.add(removeReturnsAction);
	JButton removeReturnsButton = toolBar.add(removeReturnsAction);
	removeReturnsButton.setBorderPainted(false);
	setRollover(removeReturnsButton, "images/returnicon-roll-16x16.png");
	removeReturnsButton.setToolTipText(readText("removeReturnsButton.html"));

	// Non-printing-character display
	Action nonPrintingCharViewerAction = new AbstractAction(
			"View non-printing characters", makeIcon("images/nonprinting-16x16.png")) {
		public void actionPerformed(ActionEvent evt) {
			TextPad t = (TextPad)textAreas.get(tabbedPane.getSelectedIndex());
			t.setText(Tools.showNonPrintingChars(t.getText()));
		}
	};
	setAction(nonPrintingCharViewerAction, "View non-printing characters", 'V');
	toolsMenu.add(nonPrintingCharViewerAction);
	JButton nonPrintingCharViewerButton = toolBar.add(nonPrintingCharViewerAction);
	nonPrintingCharViewerButton.setBorderPainted(false);
	setRollover(nonPrintingCharViewerButton, "images/nonprinting-roll-16x16.png");
	nonPrintingCharViewerButton.setToolTipText(readText("nonPrintingButton.html"));

	// HTML replacement
	Action htmlReplacerAction = new AbstractAction(
			"Replace HTML tags", makeIcon("images/htmlreplacer-16x16.png")) {
		public void actionPerformed(ActionEvent evt) {
			TextPad t = (TextPad)textAreas.get(tabbedPane.getSelectedIndex());
				t.setText(Tools.htmlReplacer(t.getText()));
		}
	};
	setAction(htmlReplacerAction, "Replace HTML tags", 'H');
	toolsMenu.add(htmlReplacerAction);
	JButton htmlReplacerButton = toolBar.add(htmlReplacerAction);
	htmlReplacerButton.setBorderPainted(false);
	setRollover(htmlReplacerButton, "images/htmlreplacer-roll-16x16.png");
	htmlReplacerButton.setToolTipText(readText("htmlreplacer.html"));
	
	toolBar.setFloatable(false); // necessary since not BorderLayout

	// add menu bar and menus
	setJMenuBar(menuBar);
	menuBar.add(fileMenu);
	menuBar.add(editMenu);
	menuBar.add(mistakerMenu);
	menuBar.add(toolsMenu);
	menuBar.add(helpMenu);

	// add components to frame; "add" function to set GridBag parameters
	Container contentPane = getContentPane();
	GridBagLayout layout = new GridBagLayout();
	contentPane.setLayout(layout);

	GridBagConstraints constraints = new GridBagConstraints();

	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	add(toolBar, constraints, 0, 0, 1, 1, 0, 0);

	constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	add(tabbedPane, constraints, 0, 1, 1, 1, 100, 100);
	
    }

    /**Publically executable starter method.
	 * Creates the <code>TextTrix</code> object, displays it,
	 * an makes sure that it will still undergo its
	 * exit routine when closed manually.
	 * @param args command-line arguments; not yet used
     */
    public static void main(String[] args) {
		TextTrix textTrix = new TextTrix();
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
		textTrix.getSelectedTextPad().requestFocusInWindow();
    }

	/**Gets the last path for opening a file.
	 * @return most recent path for opening a file
	 */
	public static String getOpenPath() {
		return openPath;
	}

	/**Gets the last path for saving a file.
	 * @return most recent path for saving a file
	 */
	public static String getSavePath() {
		return savePath;
	}

	public static TextPad getSelectedTextPad() {
		int i = tabbedPane.getSelectedIndex();
		if (i != -1) {
			return (TextPad)textAreas.get(tabbedPane.getSelectedIndex());
		} else {
			return null;
		}
	}

	public static int getCurrentTabIndex() {
		return currentTabIndex;
	}

	public static int getPrevTabIndex() {
		return prevTabIndex;
	}

	/**Sets the given path as the most recently one used
	 * to open a file.
	 * @param anOpenPath path to set as last opened location
	 */
	public static void setOpenPath(String anOpenPath) {
		openPath = anOpenPath;
	}

	/**Sets the given path as the most recently one used
	 * to save a file.
	 * @param aSavePath path to set as last saved location.
	 */
	public static void setSavePath(String aSavePath) {
		savePath = aSavePath;
	}

	public static void setCurrentTabIndex(int i) {
		currentTabIndex = i;
	}

	public static void setPrevTabIndex(int i) {
		prevTabIndex = i;
	}

	/**Enable button rollover icon change.
	 * @param button <code>JButton</code> to display icon rollover change
	 * @param iconPath location of icon to change to
	 */
	public void setRollover(JButton button, String iconPath) {
		button.setRolloverIcon(makeIcon(iconPath));
		button.setRolloverEnabled(true);
	}
		

	/**Set an action's properties.
	 * @param action action to set
	 * @param description tool tip
	 * @param mnemonic menu shortcut
	 * @param keyStroke accelerator key shortcut
	 */
	public void setAction(Action action, String description, 
			char mnemonic, KeyStroke keyStroke) {
		action.putValue(Action.SHORT_DESCRIPTION, description);
		action.putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));
		action.putValue(Action.ACCELERATOR_KEY, keyStroke);
	}
	
	/**Sets an action's properties.
	 * @param action action to set
	 * @param description tool tip
	 * @param mnemonic menu shortcut
	 */
	public void setAction(Action action, String description, char mnemonic) {
		action.putValue(Action.SHORT_DESCRIPTION, description);
		action.putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));
	}

	/**Creates an image icon.
	 * Retrieves the image file from a jar archive.
	 * @param path image file location relative to TextTrix.class
	 * @return icon from archive; null if the file cannot be retrieved
	 */
	public ImageIcon makeIcon(String path) {
		URL iconURL = TextTrix.class.getResource(path);
		return (iconURL != null) ? new ImageIcon(iconURL) : null;
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

	/**Displays a read-only file in a <code>Text Pad</code>.  
	 * May change name to <code>readDocumentation</code> for 
	 * consistency with <code>readText</code>.
	 * @param path read-only text file's path.  Can also display
	 * non-read-only text files, but can't edit.
	 */
	public void displayFile(String path) {
		try {
			// getResourceAsStream to make future usable as an applet
			BufferedReader reader = new BufferedReader(new
				InputStreamReader(TextTrix.class.
					getResourceAsStream(path)));
			String text = readText(reader);
			reader.close();
			addTextArea(textAreas, tabbedPane, new File(path));
			TextPad t = (TextPad)textAreas.get(tabbedPane.getSelectedIndex());
			t.setEditable(false); // so appropriate for read-only
			t.setText(text);
			t.setChanged(false);
			updateTitle(textAreas, tabbedPane);
			t.setCaretPosition(0);
		} catch(IOException exception) {
			exception.printStackTrace();
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
			int choice = JOptionPane.showOptionDialog(					
				null,
				"This file has not yet been saved.\nWhat would you like me to do with it?",
				"Save before close",
				JOptionPane.WARNING_MESSAGE,
				JOptionPane.DEFAULT_OPTION,
				null,				
				new String[] { "Save", "Toss away", "Cancel" },
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
	 * @param reader text file stream
	 * @return text from file
	 */
	public String readText(String path) {
		String text = "";
		try {
			InputStream in = TextTrix.class.getResourceAsStream(path);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null)
				text = text + line + "\n";
		} catch(IOException exception) {
			exception.printStackTrace();
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
    public void addTextArea(ArrayList arrayList, 
			final JTabbedPane tabbedPane, File file) {
			final TextPad textPad = new TextPad(30, 20, file);
			// final variables so can use in inner class;
			
			JScrollPane scrollPane = new JScrollPane(textPad);
			DocumentListener listener = new TextPadDocListener();
			
			// 1 more than highest tab index since will add tab
			final int i = tabbedPane.getTabCount();
			tabbedPane.addTab(file.getName() + "  ", scrollPane);
			textPad.setLineWrap(true);
			textPad.setWrapStyleWord(true);
			textPad.getDocument().addDocumentListener(listener);
			// show " *" in tab title when text changed
			arrayList.add(textPad);
			tabbedPane.setSelectedIndex(i);
			tabbedPane.setToolTipTextAt(i, textPad.getPath());
	}

	/**Changes tabbed pane title to indicate whether the file's changes 
	 * have been changed or not.
	 * Appends "<code> *</code>" if the file has unsaved changes; 
	 * appends "<code>  </code>" otherwise.
	 * @param arrayList array of <code>TextPad</code>s that the tabbed pane displays
	 * @param tabbedPane tabbed pane to update
	 */
	public void updateTitle(ArrayList arrayList, JTabbedPane tabbedPane) {
		int i = tabbedPane.getSelectedIndex();
		TextPad textPad = (TextPad)arrayList.get(i);
		String title = tabbedPane.getTitleAt(i);
		// convert to filename; -2 b/c added 2 spaces
		if (textPad.getChanged()) {
			tabbedPane.setTitleAt(i, textPad.getName() + " *");
		} else {
			tabbedPane.setTitleAt(i, title.substring(0, title.length() - 1) + " ");
		}
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
       manager.
       @param c component to add
       @param constraints layout constraints object
       @param x column number
       @param y row number
       @param w number of columns to span
       @param h number of rows to span
       @param wx column weight
       @param wy row weight
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
		try {
			TextPad t = (TextPad)textAreas
				.get(tabbedPane.getSelectedIndex());
			PrintWriter out = new 
			PrintWriter(new FileWriter(path), true);
			out.print(t.getText());
		    out.close();
			t.setChanged(false);
			t.setFile(path);
			tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), 
					t.getName() + "  ");
			return true;
		} catch(IOException exception) {
			exception.printStackTrace();
			return false;
		}
	}

	/**Evokes a save dialog, with a filter for text files.
	 * Sets the tabbed pane tab to the saved file name.
	 * @return true if the approve button is chosen, false if otherwise
	 */
	public static boolean fileSaveDialog(JFrame owner) {
		chooser.setCurrentDirectory(new File(savePath));

    	int result = chooser.showSaveDialog(owner);
    	if (result == JFileChooser.APPROVE_OPTION) {
			String path = chooser.getSelectedFile().getPath();
			saveFile(path);
			setSavePath(path);
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
		
		public FileOpenAction(JFrame aOwner, String name, Icon icon) {
			owner = aOwner;
			putValue(Action.NAME, name);
			putValue(Action.SMALL_ICON, icon);
		}
		
		public void actionPerformed(ActionEvent evt) {
	    	chooser.setCurrentDirectory(new File(openPath));

	    	int result = chooser.showOpenDialog(owner);

	    	if (result == JFileChooser.APPROVE_OPTION) {
				String path = chooser.getSelectedFile().getPath();

				try {
		    		BufferedReader reader = 
						new BufferedReader(new FileReader(path));
				    String text = readText(reader);
				
		    		// check if tabs exist; get TextPad if true
		    		int tabIndex = tabbedPane.getSelectedIndex();
					TextPad t = null;
		    		if (tabIndex != -1)
						t = (TextPad)textAreas.get(tabIndex);
		    		/* t.getText() != null, even if have typed nothing in it.
					 * Add tab and set its text if no tabs exist or if current
					 * tab has tokens; set current tab's text otherwise */
		    		if (tabIndex == -1 
							|| (new StringTokenizer(t.getText()))
							.hasMoreTokens()) { 
						addTextArea(textAreas, tabbedPane, new File(path));
						t = (TextPad)textAreas
							.get(tabbedPane.getSelectedIndex());
						t.setText(text);
		    		} else {
						t.setText(text);
		    		}
					t.setCaretPosition(0);
					t.setChanged(false);
					t.setFile(path);
					tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), 
							chooser.getSelectedFile().getName());
	
			    	reader.close();
					setOpenPath(path);
				} catch(IOException exception) {
				    exception.printStackTrace();
				}
		    }
		}
	}
	
	/**Responds to user input calling for a save dialog.
	 */
    private class FileSaveAction extends AbstractAction {
		JFrame owner;
		
		public FileSaveAction(JFrame aOwner, String name, Icon icon) {
			owner = aOwner;
			putValue(Action.NAME, name);
			putValue(Action.SMALL_ICON, icon);
		}
		
		public void actionPerformed(ActionEvent evt) {
    		fileSaveDialog(owner);
		}
	}

	private class FindDialog extends JDialog {
		JTextField find;
		JTextField replace;
		JCheckBox word;
		JCheckBox wrap;
		JCheckBox selection;
		JCheckBox replaceAll;
		JCheckBox ignoreCase;
		
		public FindDialog(JFrame owner) {
			super(owner, "Find and Replace", false);
			setSize(350, 150);
			Container contentPane = getContentPane();
			contentPane.setLayout(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.CENTER;
			add(new JLabel("Find:"), constraints, 0, 0, 1, 1, 100, 0);
			add(find = new JTextField(20), constraints, 1, 0, 2, 1, 100, 0);
			add(new JLabel("Replace:"), constraints, 0, 1, 1, 1, 100, 0);
			add(replace = new JTextField(20), constraints, 1, 1, 2, 1, 100, 0);
			add(word = new JCheckBox("Find word"), constraints, 0, 2, 1, 1, 100, 0);
			word.setMnemonic(KeyEvent.VK_N);
			word.setToolTipText("Search for the expression as a separate word");
			add(wrap = new JCheckBox("Wrap"), constraints, 2, 2, 1, 1, 100, 0);
			wrap.setMnemonic(KeyEvent.VK_A);
			wrap.setToolTipText("Start searching from the cursor and wrap back to it");
			add(selection = new JCheckBox("Replace within selection"), constraints, 1, 2, 1, 1, 100, 0);
			selection.setMnemonic(KeyEvent.VK_S);
			selection.setToolTipText("Search and replace text within the entire highlighted section");
			add(replaceAll = new JCheckBox("Replace all"), constraints, 0, 3, 1, 1, 100, 0);
			replaceAll.setMnemonic(KeyEvent.VK_L);
			replaceAll.setToolTipText("Replace all instances of the expression");
			add(ignoreCase = new JCheckBox("Ignore case"), constraints, 1, 3, 1, 1, 100, 0);
			ignoreCase.setMnemonic(KeyEvent.VK_I);
			ignoreCase.setToolTipText("Search for both lower and upper case versions of the expression");

			Action findAction = new AbstractAction("Find", null) {
				public void actionPerformed(ActionEvent e) {
					TextPad t = getSelectedTextPad();
					String findText = find.getText();
					int n = t.getCaretPosition();
					n = Tools.find(t.getText(), findText, n, word.isSelected(), ignoreCase.isSelected());
					if (n != -1) {
						if (wrap.isSelected()) {
							n = Tools.find(t.getText(), findText, 0, word.isSelected(), ignoreCase.isSelected());
						}
						t.setSelectionStart(n);
						t.setSelectionEnd(n + findText.length());
					}
				}
			};
			setAction(findAction, "Find", 'F', 
					KeyStroke.getKeyStroke(KeyEvent.VK_F, 
						InputEvent.ALT_MASK));
			add(new JButton(findAction), constraints, 0, 4, 1, 1, 100, 0);

			Action findReplaceAction = new AbstractAction("Find and Replace", null) {
				public void actionPerformed(ActionEvent e) {
					TextPad t = getSelectedTextPad();
					String text = t.getText();
					String findText = find.getText();
					String replaceText = replace.getText();
					if (selection.isSelected()) {
						t.setText(Tools.findReplace(text, findText, replaceText,
								t.getSelectionStart(), 
								t.getSelectionEnd(), word.isSelected(), true,
								false, ignoreCase.isSelected()));
					} else {
						t.setText(Tools.findReplace(text, findText, replaceText,
								t.getCaretPosition(), text.length(), 
								word.isSelected(), replaceAll.isSelected(), 
								wrap.isSelected(), ignoreCase.isSelected()));
					}
				}
			};
			setAction(findReplaceAction, "Find and replace", 'R', 
					KeyStroke.getKeyStroke(KeyEvent.VK_R,
						InputEvent.ALT_MASK));
			add(new JButton(findReplaceAction), constraints, 1, 4, 1, 1, 100, 0);
		}

		
    	/**Adds a new component to the <code>GridBagLayout</code> manager.
		 * @param c component to add
		 * @param constraints layout constraints object
		 * @param x column number
		 * @param y row number
		 * @param w number of columns to span
		 * @param h number of rows to span
		 * @param wx column weight
		 * @param wy row weight
		 * */
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
	}
	
	/**Responds to changes in the <code>TextPad</code> text areas.
	 * Updates the titles to reflect text alterations.
	 */
	private class TextPadDocListener implements DocumentListener {
	
		/**Flags a text insertion.
		 * @param e insertion event
		 */
		public void insertUpdate(DocumentEvent e) {
			((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).setChanged(true);
			updateTitle(textAreas, tabbedPane);
		}

		/**Flags a text removal.
		 * @param e removal event
		 */
		public void removeUpdate(DocumentEvent e) {
			((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).setChanged(true);
			updateTitle(textAreas, tabbedPane);
		}

		/**Flags any sort of text change.
		 * @param e any text change event
		 */
		public void changedUpdate(DocumentEvent e) {
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
