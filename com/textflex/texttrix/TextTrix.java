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
    private static JTabbedPane tabbedPane
	= new JTabbedPane(JTabbedPane.TOP); // multiple TextPads
    private static JPopupMenu popup = new JPopupMenu(); // make popup menu
    private static JFileChooser chooser = new JFileChooser(); // file dialog
    private static JCheckBoxMenuItem autoIndent = null;
    private static String openDir = ""; // most recently path opened to
    private static String saveDir = ""; // most recently path saved to
    private static int fileIndex = 0; // for giving each TextPad a unique name
    private static FindDialog findDialog; // find dialog
    //	private static int tabSize = 0; // user-defined tab display size
    private static PlugIn[] plugIns = null; // plugins from jar archives
    private static Action[] plugInActions = null; // plugin invokers
    private static String toolsCharsUnavailable = ""; // chars for shorcuts
    private static String trixCharsUnavailable = ""; // chars for shorcuts
    private JMenu trixMenu = new JMenu("Trix"); // trix plugins
    private JMenu toolsMenu = new JMenu("Tools"); // tools plugins
    private JToolBar toolBar = new JToolBar("Trix and Tools"); // icons

    /** Constructs a new <code>TextTrix</code> frame and with
	<code>TextPad</code>s for each of the specified paths or at least
	one <code>TextPad</code>.
     */
    public TextTrix(String[] paths) {
	setTitle("Text Trix");
	// pre-set window size
	setSize(500, 600); // TODO: adjust to user's screen size
	ImageIcon im = makeIcon("images/minicon-16x16.png"); // set frame icon
	if (im !=null) 
	    setIconImage(im.getImage());		
	// make first tab and text area
	addTextArea(textAreas, tabbedPane, makeNewFile());
	// adds a change listener to listen for tab switches and display
	// the options of the tab's TextPad
	tabbedPane.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent evt) {
		    TextPad t = getSelectedTextPad();
		    if (t != null) 
			setAutoIndent(t.getAutoIndent());
		}
	    });
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
	trixMenu.setMnemonic('T');
	toolsMenu.setMnemonic('O');
	JMenu helpMenu = new JMenu("Help");
	helpMenu.setMnemonic('H');

	// make tool bar
	toolBar.addMouseListener(new PopupListener());
	toolBar.setFloatable(false); // necessary since not BorderLayout




	/* File menu items */

	// make new tab and text area
	Action newAction = new AbstractAction("New") {
		public void actionPerformed(ActionEvent evt) {
		    addTextArea(textAreas, tabbedPane, makeNewFile());
		}
	    };
	// per Mozilla keybinding
	setAcceleratedAction(newAction, "New", 'T', 
			     KeyStroke.getKeyStroke("ctrl T"));
	fileMenu.add(newAction);
	/* tab shifts defined under View menu section;
	 * alternatively, move to tabs using default Java key-bindings:
	 * -Ctrl-up to tabs
	 * -Lt, rt to switch tabs
	 * -Tab back down to TextPad */

	// (ctrl-o) open file; use selected tab if empty
	Action openAction 
	    = new FileOpenAction(TextTrix.this, "Open", 
				 makeIcon("images/openicon-16x16.png"));
	setAcceleratedAction(openAction, "Open", 'O', 
			     KeyStroke.getKeyStroke("ctrl O"));
	fileMenu.add(openAction);
	JButton openButton = toolBar.add(openAction);
	openButton.setBorderPainted(false);
	setRollover(openButton, "images/openicon-roll-16x16.png");

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
			       makeIcon("images/closeicon-16x16.png")) {
		public void actionPerformed(ActionEvent evt) {
		    int i = tabbedPane.getSelectedIndex();
		    if (i >= 0) 
			closeTextArea(i, textAreas, tabbedPane);
		}
	    };
	setAcceleratedAction(closeAction, "Close", 'C', 
		  KeyStroke.getKeyStroke("ctrl W"));
	fileMenu.add(closeAction);

	// (ctrl-s) save file; no dialog if file already created
	Action saveAction = 
	    new AbstractAction("Save", makeIcon("images/saveicon-16x16.png")) {
		public void actionPerformed(ActionEvent evt) {
		    // can't use tabbedPane.getSelectedComponent() 
		    // b/c returns JScrollPane
		    int tabIndex = tabbedPane.getSelectedIndex();
		    TextPad t = null;
		    if (tabIndex != -1) {
			t = (TextPad)textAreas.get(tabIndex);
			// save directly to file if already created one
			if (t.fileExists())
			    saveFile(t.getPath());
			// otherwise, request filename for new file
			else
			    fileSaveDialog(TextTrix.this);
		    }
		}
	    };
	setAcceleratedAction(saveAction, "Save", 'S', 
			     KeyStroke.getKeyStroke("ctrl S"));
	fileMenu.add(saveAction);
	JButton saveButton = toolBar.add(saveAction);
	saveButton.setBorderPainted(false);
	setRollover(saveButton, "images/saveicon-roll-16x16.png");

	// save w/ file save dialog
	Action saveAsAction 
	    = new FileSaveAction(TextTrix.this, "Save as...", 
				 makeIcon("images/saveasicon-16x16.png"));
	setAction(saveAsAction, "Save as...", '.');
	fileMenu.add(saveAsAction);
	
	// Start exit functions
	fileMenu.addSeparator();
	
	// (ctrl-q) exit file; close each tab separately, checking for saves
	Action exitAction = new AbstractAction("Exit") {
		public void actionPerformed(ActionEvent evt) {
		    exitTextTrix();
		}
	    };
	// Doesn't work if close all tabs unless click ensure window focused, 
	// such as clicking on menu
	setAcceleratedAction(exitAction, "Exit", 'X', 
		  KeyStroke.getKeyStroke("ctrl Q"));
	fileMenu.add(exitAction);

	/* Edit menu items */

	// (ctrl-z) undo; multiple undos available
	Action undoAction = new AbstractAction("Undo") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas
		     .get(tabbedPane.getSelectedIndex())).undo();
		}
	    };
	setAcceleratedAction(undoAction, "Undo", 'U', 
		  KeyStroke.getKeyStroke("ctrl Z"));
	editMenu.add(undoAction);

	// (ctrl-y) redo; multiple redos available
	Action redoAction = new AbstractAction("Redo") {
		public void actionPerformed(ActionEvent evt) {
		    ((TextPad)textAreas.get(tabbedPane.getSelectedIndex()))
			.redo();
		}
	    };
	setAcceleratedAction(redoAction, "Redo", 'R', 
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
	setAcceleratedAction(cutAction, "Cut", 'C', 
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
	setAcceleratedAction(copyAction, "Copy", 'O', 
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
	setAcceleratedAction(pasteAction, "Paste", 'P', 
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
	setAcceleratedAction(selectAllAction, "Select all", 'S', 
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
	setAcceleratedAction(autoIndentAction, 
			     "Automatically repeat tabs with the next line",
			     'I', 
			     KeyStroke.getKeyStroke("ctrl shift I"));
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
	setAction(autoIndentAllAction, "Auto indent all files", 'A');
	optionsMenu.add(autoIndentAllAction);
		





	/* View menu items */
	
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
	setAcceleratedAction(prevTabAction, "Preeceding tab", 'P', 
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
	setAcceleratedAction(nextTabAction, "Next tab", 'N', 
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
	setAction(togglePlainViewAction, "View as plain text", 'P');
	viewMenu.add(togglePlainViewAction);

	// view as HTML formatted text
	Action toggleHTMLViewAction = new AbstractAction("Toggle HTML view") {
		public void actionPerformed(ActionEvent evt) {
		    viewHTML();
		}
	    };
	setAction(toggleHTMLViewAction, "View as HTML", 'H');
	viewMenu.add(toggleHTMLViewAction);
	
	// view as RTF formatted text
	Action toggleRTFViewAction = new AbstractAction("Toggle RTF view") {
		public void actionPerformed(ActionEvent evt) {
		    viewRTF();
		}
	    };
	setAction(toggleRTFViewAction, "View as RTF", 'R');
	viewMenu.add(toggleRTFViewAction);
		
	/* Help menu items */

	// about Text Trix, incl copyright notice and version number
	Action aboutAction 
	    = new AbstractAction("About...",
				 makeIcon("images/minicon-16x16.png")) {
		public void actionPerformed(ActionEvent evt) {
		    String text = readText("about.txt");
		    String iconPath = "images/texttrixsignature.png";
		    JOptionPane
			.showMessageDialog(null, 
					   text, 
					   "About Text Trix", 
					   JOptionPane.PLAIN_MESSAGE, 
					   makeIcon(iconPath));
		}
	    };
	setAction(aboutAction, "About...", 'A');
	helpMenu.add(aboutAction);
	
	// shortcuts description; opens new tab
	Action shortcutsAction = new AbstractAction("Shortcuts") {
		public void actionPerformed(ActionEvent evt) {
		    // reads from "shortcuts.txt" in same dir as this class
		    String path = "shortcuts.txt";
		    displayFile(path);
		}
	    };
	setAction(shortcutsAction, "Shortcuts", 'S');
	helpMenu.add(shortcutsAction);

	// features descriptions; opens new tab
	Action featuresAction = new AbstractAction("Features descriptions") {
		public void actionPerformed(ActionEvent evt) {
		    // reads from "features.txt" in same dir as this class
		    String path = "features.txt";
		    displayFile(path);
		}
	    };
	setAction(featuresAction, "Features descriptions", 'F');
	helpMenu.add(featuresAction);

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





	/* Trix and Tools menus */

	// Find and Replace: fixed tool, not a plugin
	// (ctrl-shift-F) find and replace Tools feature
	Action findAction 
	    = new AbstractAction("Find and replace", 
				 makeIcon("images/find-16x16.png")) {
		public void actionPerformed(ActionEvent evt) {
		    if (findDialog == null) 
			findDialog = new FindDialog(TextTrix.this);
		    findDialog.show();
		}
	    };
	// need capital "F" b/c "shift"
	setAcceleratedAction(findAction, "Find and replace", 'F', 
		  KeyStroke.getKeyStroke("ctrl shift F")); 
	toolsMenu.add(findAction);
	JButton findButton = toolBar.add(findAction);
	findButton.setBorderPainted(false);
	setRollover(findButton, "images/find-roll-16x16.png");
	findButton.setToolTipText(readText("findbutton.html"));


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
       	textTrix.getSelectedTextPad().requestFocusInWindow();
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

	// create the action
	Action action = 
	    new AbstractAction(name, icon) {
		public void actionPerformed(ActionEvent evt) {
		    // invoke the plugin's text manipulation on the current
		    // TextPad's text
		    textTinker(pl); 
		}
	    };
	
	// add the action to the appropriate menu
	if (category.equalsIgnoreCase("tools")) {
	    setAction(action, name, description, toolsCharsUnavailable);
	    toolsMenu.add(action);
	} else {
	    setAction(action, name, description, trixCharsUnavailable);
	    trixMenu.add(action);
	}

	// add the action to a tool bar menu
	JButton button = toolBar.add(action);
	button.setBorderPainted(false);
	setRollover(button, rollIcon);
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
	    try {
		// determines whether a region is selected or not;
		// if not, works on the text pad's entire text
		if (start == end) { // no selection
		    text = doc.getText(0, doc.getLength()); // all the text
		    text = pl.run(text); // invoke the plugin
		    doc.remove(0, doc.getLength()); // remove all the text
		    doc.insertString(0, text, null); // insert text
		    // approximates the original caret position
		    if (start > doc.getLength()) {
			// otherwise errors toward end of document sometimes
			t.setCaretPosition(doc.getLength());
		    } else {
			t.setCaretPosition(start);
		    }
		} else {
		    int len = end - start; // length of selected region
		    text = doc.getText(start, len); // only get the region
		    text = pl.run(text); // invoke the plugin
		    doc.remove(start, len); // remove only the region
		    doc.insertString(start, text, null); // insert text
		    // caret automatically returns to end of selected region
		}
			    
			    
	    } catch (BadLocationException e) {
		e.printStackTrace();
	    }
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
	// convert "%20", the escape character for a space, into " ";
	// required for starting with JRE v.1.4.0
	// (see http://developer.java.sun.com/developer/ //
	// bugParade/bugs/4466485.html)
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
	// if so, delete protocal and any preceding info
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

    /**Sets the given path as the most recently one used
     * to open a file.
     * @param anOpenPath path to set as last opened location
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

    /**Enable button rollover icon change.
     * @param button <code>JButton</code> to display icon rollover change
     * @param iconPath location of icon to change to
     */
    public void setRollover(JButton button, String iconPath) {
	button.setRolloverIcon(makeIcon(iconPath));
	button.setRolloverEnabled(true);
    }
		
    /**Enable button rollover icon change.
     * @param button <code>JButton</code> to display icon rollover change
     * @param iconPath location of icon to change to
     */
    public void setRollover(JButton button, ImageIcon icon) {
	button.setRolloverIcon(icon);
	button.setRolloverEnabled(true);
    }

    /**Set an action's properties.
     * @param action action to set
     * @param description tool tip
     * @param mnemonic menu shortcut
     * @param keyStroke accelerator key shortcut
     */
    public void setAcceleratedAction(Action action, String description, 
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

    /** Sets an action's properties.
	Assumes that the action has a name, and that the name is used
	in the menu that will house the action.
	@param action action to set
	@param name the action's name, from which to get the mnemonic
	@param description tool tip
	@param charsUnavailable a string of characters unavailable to use
	as mnemonics, as in those already in the menu where the action
	will be placed
     */
    public void setAction(Action action, String name, String description,
			  String charsUnavailable) {
	char mnemonic = 0;
	int i = 0;
	// tries to get a mnemonic that has not been taken
	for (i = 0; i < name.length()
		 && charsUnavailable
		 .indexOf((mnemonic = name.charAt(i)))
		 != -1;
	     i++);
	// otherwise haven't found a suitable char
	if (i < name.length()) { 
	    action.putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));
	    charsUnavailable += mnemonic;
	}
	// adds the description
	action.putValue(Action.SHORT_DESCRIPTION, description);
    }

    /** Creates an image icon.
	@param path image file location relative to TextTrix.class
	@return icon from archive; null if the file cannot be retrieved
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

    /** Displays a read-only file in a <code>Text Pad</code>.  
	Assumes the file exists and can be read as text.
	TODO: May change name to <code>readDocumentation</code> for 
	consistency with <code>readText</code>.
	@param path read-only text file's path.  Can also display
	non-read-only text files, but can't edit them.
     */
    public void displayFile(String path) {
	try {
	    // uses getResourceAsStream to ensure future usability as an applet
	    InputStreamReader in 
		= new InputStreamReader(TextTrix.class.
					getResourceAsStream(path));
	    BufferedReader reader = new BufferedReader(in);
	    String text = readText(reader); // retrieve the text
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
     * @param reader text file stream
     * @return text from file
     */
    public String readText(String path) {
	String text = "";
	try {
	    InputStream in = TextTrix.class.getResourceAsStream(path);
	    BufferedReader reader 
		= new BufferedReader(new InputStreamReader(in));
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
	tabbedPane.addTab(file.getName() + "  ", scrollPane);
	//		textPad.setLineWrap(true);
	//		textPad.setWrapStyleWord(true);
	textPad.getDocument().addDocumentListener(listener);
	textPad.addMouseListener(new PopupListener());
	// show " *" in tab title when text changed
	tabbedPane.setSelectedIndex(i);
	tabbedPane.setToolTipTextAt(i, textPad.getPath());
    }

    /**Changes tabbed pane title to indicate whether the file's changes 
     * have been changed or not.
     * Appends "<code> *</code>" if the file has unsaved changes; 
     * appends "<code>  </code>" otherwise.
     * @param arrayList array of <code>TextPad</code>s that the 
     * tabbed pane displays
     * @param tabbedPane tabbed pane to update
     */
    public void updateTitle(ArrayList arrayList, JTabbedPane tabbedPane) {
	int i = tabbedPane.getSelectedIndex();
	TextPad textPad = (TextPad)arrayList.get(i);
	//	String title = tabbedPane.getTitleAt(i);
	String title = textPad.getName();
	// convert to filename; -2 b/c added 2 spaces
	if (textPad.getChanged()) {
	    tabbedPane.setTitleAt(i, title + " *");
	} else {
	    tabbedPane.setTitleAt(i, title + "  ");
	    //		setTitleAt(i, title.substring(0, title.length() - 1) + " ");
	}
    }

    /**Adds additional listeners and other settings to a <code>TextPad</code>.
     * Useful to apply on top of the <code>TextPad</code>'s 
     * <code>applyDocumentSettings</code> function.
     * @param TextPad <code>TextPad</code> requiring applied settings
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
     * @param TextPad <code>TextPad</code> to read a file into
     * @param in file reader
     * @param desc reader stream description
     */
    public void read(TextPad textPad, Reader in, Object desc) throws IOException {
	textPad.read(in, desc);
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
	if (t != null) {
	    try {
		File f = new File(path);
		if (f.canWrite())
		    System.out.println("can write");
		else 
		    System.out.println("can't write");
		// open the stream to write to
		PrintWriter out = new 
		    PrintWriter(new FileWriter(path), true);
		// write to it
		out.print(t.getText());
		out.close();
		t.setChanged(false);
		t.setFile(path);
		tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), 
				      t.getName() + "  ");
		return true;
	    } else {
		return false;
	    }
	} catch(IOException exception) {
	    //	    exception.printStackTrace();
	    return false;
	}
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
	    try {
		BufferedReader reader = 
		    new BufferedReader(new FileReader(path));
		    
		// check if tabs exist; get TextPad if true
		/* t.getText() != null, even if have typed nothing 
		   in it.
		   Add tab and set its text if no tabs exist or 
		   if current tab has tokens; set current tab's 
		   text otherwise
		*/
		if (t == null || !t.isEmpty()) { 
		    addTextArea(textAreas, tabbedPane, file);
		    t = (TextPad)textAreas.get(tabbedPane
					       .getSelectedIndex());
		    read(t, reader, path);
		    //		    System.out.println("I'm here");
		} else {
		    read(t, reader, path);
		    //		    System.out.println("Oh, but I'm here");
		}
		t.setCaretPosition(0);
		t.setChanged(false);
		t.setFile(path);
		updateTitle(textAreas, tabbedPane);
		reader.close();
		setOpenDir(file.getParent());
		// openDir not yet set when opening first file
		if (getOpenDir() == null) 
		    setOpenDir(System.getProperty("user.dir"));
		return true;
	    } catch(IOException exception) {
		exception.printStackTrace();
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


    /**Evokes a save dialog, with a filter for text files.
     * Sets the tabbed pane tab to the saved file name.
     * @return true if the approve button is chosen, false if otherwise
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
	@param owner the frame to which the dialog will serve
    */
    private static boolean getSavePath(JFrame owner) {
	boolean repeat = false;
	// repeat the retrieval until gets an unused file name, 
	// overwrites a used one, or the user cancels the save
	do {
	    // display the file save dialog
	    int result = chooser.showSaveDialog(owner);
	    if (result == JFileChooser.APPROVE_OPTION) {
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
		    if (saveFile(path)) {
			setSaveDir(chooser.getSelectedFile().getParent());
			tabbedPane.setToolTipTextAt(tabbedPane
						    .getSelectedIndex(), 
						    path);
			return true;
		    } else {
			String msg = path + " couldn't be written to "
			    + "that location.\nWould you like to try "
			    + "another directory or filename?";
			String title = "Couldn't save";
			repeat = yesNoDialog(owner, msg, title);
		    }
		}
	    } else {
		return false;
	    }
	} while (repeat);
	return false;
    }

    private static boolean yesNoDialog(JFrame owner, String msg, 
				String title) {
	int choice = JOptionPane
	    .showConfirmDialog(owner,
			       msg,
			       title,
			       JOptionPane.YES_NO_OPTION,
			       JOptionPane.
			       QUESTION_MESSAGE);
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
		//		System.out.println("multi: " + chooser.isMultiSelectionEnabled());
		int result = chooser.showOpenDialog(owner);
		if (result == JFileChooser.APPROVE_OPTION) {
		    //		    String msg = "At least one chosen file 
		    String msg = "";
		    String title = "Couldn't open";
		    File[] files = chooser.getSelectedFiles();
		    boolean allFound = true;
		    //		    String[] unopenables = new String[files.length];
		    //		    int unopenablesIndx = 0;
		    for (int i = 0; i < files.length; i++) {
			if (!openFile(files[i]))
			    //			    unopenables[unopenablesIndex++] = files[i].getPath();
			    //			    System.out.println(files[i].getPath());
			//			    allFound = false;
			    msg = msg + files[i] + "\n";
		    }
		    if (msg.equals("")) {
			repeat = false;
		    } else {
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
			repeat = yesNoDialog(owner, msg, title);
		    }
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
		} else {
		    repeat = false;
		}
	    } while (repeat);
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

    /**Find and replace dialog.
     * Creates a dialog box accepting input for search and replacement 
     * expressions as well as options to tailor the search.
     */
    private class FindDialog extends JDialog {
	JTextField find; // search expression input
	JTextField replace; // replacement expression input
	JCheckBox word; // treat the search expression as a separate word
	JCheckBox wrap; // search to the bottom and start again from the top
	JCheckBox selection; // search only within a highlighted section
	JCheckBox replaceAll; // replace all instances of search expression
	JCheckBox ignoreCase; // ignore upper/lower case
		
	/**Construct a find/replace dialog box
	 * @param owner frame to which the dialog box will be attached; 
	 * can be null
	 */
	public FindDialog(JFrame owner) {
	    super(owner, "Find and Replace", false);
	    setSize(400, 150);
	    Container contentPane = getContentPane();
	    contentPane.setLayout(new GridBagLayout());
	    GridBagConstraints constraints = new GridBagConstraints();
	    constraints.fill = GridBagConstraints.HORIZONTAL;
	    constraints.anchor = GridBagConstraints.CENTER;
	    String msg = "";
			
	    // search expression input
	    add(new JLabel("Find:"), constraints, 0, 0, 1, 1, 100, 0);
	    add(find = new JTextField(20), constraints, 1, 0, 2, 1, 100, 0);
	    find.addKeyListener(new KeyAdapter() {
		    public void keyPressed(KeyEvent evt) {
			if (evt.getKeyCode() == KeyEvent.VK_ENTER) 
			    find();
		    }
		});		

	    // replace expression input
	    add(new JLabel("Replace:"), constraints, 0, 1, 1, 1, 100, 0);
	    add(replace = new JTextField(20), constraints, 1, 1, 2, 1, 100, 0);
	    replace.addKeyListener(new KeyAdapter() {
		    public void keyPressed(KeyEvent evt) {
			if (evt.getKeyCode() == KeyEvent.VK_ENTER) 
			    findReplace();
		    }
		});
			
	    // treat search expression as a separate word
	    add(word = new JCheckBox("Whole word only"), 
		constraints, 0, 2, 1, 1, 100, 0);
	    word.setMnemonic(KeyEvent.VK_N);
	    msg = "Search for the expression as a separate word";
	    word.setToolTipText(msg);
			
	    // wrap search through start of text if necessary
	    add(wrap = new JCheckBox("Wrap"), constraints, 2, 2, 1, 1, 100, 0);
	    wrap.setMnemonic(KeyEvent.VK_A);
	    msg = "Start searching from the cursor and wrap back to it";
	    wrap.setToolTipText(msg);
			
	    // replace all instances within highlighted section
	    add(selection = new JCheckBox("Replace within selection"), 
		constraints, 1, 2, 1, 1, 100, 0);
	    selection.setMnemonic(KeyEvent.VK_S);
	    msg = "Search and replace text within the entire "
		+ "highlighted section";
	    selection.setToolTipText(msg);
			
	    // replace all instances from cursor to end of text unless 
	    // combined with wrap, where replace all instances in whole text
	    add(replaceAll = new JCheckBox("Replace all"), 
		constraints, 0, 3, 1, 1, 100, 0);
	    replaceAll.setMnemonic(KeyEvent.VK_L);
	    msg = "Replace all instances of the expression";
	    replaceAll.setToolTipText(msg);
			
	    // ignore upper/lower case while searching
	    add(ignoreCase = new JCheckBox("Ignore case"), 
		constraints, 1, 3, 1, 1, 100, 0);
	    ignoreCase.setMnemonic(KeyEvent.VK_I);
	    msg = "Search for both lower and upper case versions "
		+ "of the expression";
	    ignoreCase.setToolTipText(msg);

	    // find action, using the appropriate options above
	    Action findAction = new AbstractAction("Find", null) {
		    public void actionPerformed(ActionEvent e) {
			find();
		    }
		};
	    setAcceleratedAction(findAction, "Find", 'F', 
				 KeyStroke.getKeyStroke("alt F"));
	    add(new JButton(findAction), constraints, 0, 4, 1, 1, 100, 0);

	    // find and replace action, using appropriate options above
	    Action findReplaceAction 
		= new AbstractAction("Find and Replace", null) {
		    public void actionPerformed(ActionEvent e) {
			findReplace();
		    }
		};
	    setAcceleratedAction(findReplaceAction, "Find and replace", 'R', 
				 KeyStroke.getKeyStroke("alt R"));
	    add(new JButton(findReplaceAction), 
		constraints, 1, 4, 1, 1, 100, 0);
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
		
	/**Finds the give search pattern.
	 */
	public void find() {
	    TextPad t = getSelectedTextPad();
	    if (t != null) {
		String findText = find.getText();
		// search from the current carat position
		int n = t.getCaretPosition();
		//		Document doc = t.getDocument();
		//		String text = doc.getText(
		n = Tools.find(t.getText(), findText, n, 
			       word.isSelected(), ignoreCase.isSelected());
		// wrap if wrap-enabled
		if (n == -1 && wrap.isSelected()) {
		    n = Tools.find(t.getText(), findText, 0, 
				   word.isSelected(), ignoreCase.isSelected());
		}
		// highlight the quarry if found
		if (n != -1) {
		    t.setCaretPosition(n);
		    t.moveCaretPosition(n + findText.length());
		    // to ensure selection visibility
		    t.getCaret().setSelectionVisible(true); 
		}
	    }
	}
		
	/**Finds and replaces the given search pattern.
	 * Allows the replacements to be undone.
	 */
	public void findReplace() {
	    TextPad t = getSelectedTextPad();
	    if (t != null) {
		String findText = find.getText();
		String replaceText = replace.getText();
		Document doc = t.getDocument();
		String text = null;
		int start = t.getSelectionStart();
		int end = t.getSelectionEnd();
		// works within the selected range
		try {
		    if (selection.isSelected()) {
			int len = end - start;
			text = doc.getText(start, len);
			text = Tools.findReplace(text, findText, replaceText,
						 word.isSelected(), 
						 true, 
						 false,
						 ignoreCase.isSelected());
			doc.remove(start, len);
			doc.insertString(start, text, null);
			// if no range is chosen, works within the whole text
		    } else {
			text = doc.getText(0, doc.getLength());
			text = Tools.findReplace(text, findText, replaceText,
						 t.getCaretPosition(),
						 text.length(),
						 word.isSelected(),
						 replaceAll.isSelected(),
						 wrap.isSelected(),
						 ignoreCase.isSelected());
			doc.remove(0, doc.getLength());
			doc.insertString(0, text, null);
			// approximates the original caret position
			if (start > doc.getLength()) {
			    t.setCaretPosition(doc.getLength());
			} else {
			    t.setCaretPosition(start);
			}
		    }
		} catch (BadLocationException e) {
		    e.printStackTrace();
		}
	    }
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

