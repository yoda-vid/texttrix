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
 * Portions created by the Initial Developer are Copyright (C) 2002-6
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
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.filechooser.FileFilter;
import java.net.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.print.*;
import javax.print.attribute.*;

/**
 * The main Text Trix class. Takes care of all basic graphical user interface
 * operations, such as setting up and responding to changes in the
 * <code>Text Pad</code>s, tool bar, menus, and dialogs. Manages and mediates
 * the plug-ins' actions.
 */
public class TextTrix extends JFrame {

	/* Constants */
	private static final String newline = System.getProperty("line.separator");
	private static final String FILE_SPLITTER = "::";
	private static final String FILE_GROUP_SPLITTER = "{}";
	private static final String FILE_GROUP_SPLITTER_REGEX = "\\{\\}";
	
	/* Storage variables */
	private static String openDir = ""; // most recently path opened to
	private static String saveDir = ""; // most recently path saved to
	private static int fileIndex = 0; // for giving each TextPad a unique name
	private String toolsCharsUnavailable = ""; // chars for shorcuts
	private String trixCharsUnavailable = ""; // chars for shorcuts
	private int[] tabIndexHistory = new int[10]; // records for back/forward
	private int tabIndexHistoryIndex = 0; // index of next record
	private boolean updateTabIndexHistory = true; // flag to update the record
	// flag to update file history menu entries
	private static boolean updateFileHist = false;
	private FileHist fileHist = null; // file history
	// starting position of file history in file menu
	private static int fileHistStart = -1;
	
	/* General GUI components */
	private static ArrayList textAreas = new ArrayList(); // all the TextPads
	private Container contentPane = getContentPane();
	private static JTabbedPane tabbedPane = null; // multiple TextPads
	private static JPopupMenu popup = null; // make popup menu
	private static JPopupMenu tabsPopup = null; // make popup menu
	private static JFileChooser chooser = null; // file dialog
	private static FileFilter allFilter = null; // TODO: may be unnecessary
	private static MotherTabbedPane groupTabbedPane = null;
//	private static ArrayList tabbedPaneGroups = new ArrayList();
//	private boolean tmpActivated = false;
	
	/* Menu bar controls */
	// menu and tool bar worker thread
	private MenuBarCreator menuBarCreator = null;
	private JMenuBar menuBar = null;
	private static JCheckBoxMenuItem autoIndent = null;
	private JMenu trixMenu = null; // trix plugins
	private JMenu toolsMenu = null; // tools plugins
	private JToolBar toolBar = null; // icons
	private static JMenu fileMenu = null; // file menu, which incl file history
	
	/* Preferences panel controls */
	private static Prefs prefs = null; // preferences
	// prefs action signaling to accept
	private static Action prefsOkayAction = null;
	// prefs action signaling to immediately accept
	private static Action prefsApplyAction = null;
	private static Action prefsCancelAction = null; // prefs action to reject
	
	/* Plug-in controls */
	private JDialog[] plugInDiags = null;
	private int plugInDiagsIdx = 0;
	private static PlugIn[] plugIns = null; // plugins from jar archives
	private static Action[] plugInActions = null; // plugin invokers
	
	/* Printer controls */
	// print request attributes
	private HashPrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
	private PageFormat pageFormat = null; // page formatting
	
	/* Status bar */
	private StatusBarCreator statusBarCreator = null; // worker thread
	private JPanel statusBarPanel = null; // the panel
	private JLabel statusBar = null; // the status label; not really a "bar"
	private JTextField lineNumFld = null; // Line Find

	/**
	 * Constructs a new <code>TextTrix</code> frame and with
	 * <code>TextPad</code> s for each of the specified paths or at least one
	 * <code>TextPad</code>.
	 */
	public TextTrix(final String[] paths) {

		// create file menu in constructor rather than when defining class
		// variables b/c would otherwise use bold font for this menu alone
		fileMenu = new JMenu("File");

		// adds a window listener that responds to closure of the main
		// window by calling the exit function
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitTextTrix();
			}
		});

		/* Load preferences to create prefs panel */

		// create the accept action
		prefsOkayAction = new AbstractAction("Okay", null) {
			public void actionPerformed(ActionEvent evt) {
				if (continuePrefsUpdate()) {
					getPrefs().storePrefs();
					applyPrefs();
					getPrefs().dispose();
				}
			}
		};
		LibTTx.setAcceleratedAction(prefsOkayAction, "Okay", 'O', KeyStroke
				.getKeyStroke("alt O"));

		// creates an action that could store and apply preferences
		// without closing the window;
		// the class, not the calling function, creates the action b/c
		// no need to report back to the calling function;
		// contrast "cancelAction", which requires the calling function to
		// both dispose of and destroy the object
		prefsApplyAction = new AbstractAction("Apply now", null) {
			public void actionPerformed(ActionEvent evt) {
				if (continuePrefsUpdate()) {
					getPrefs().storePrefs();
					applyPrefs();
				}
			}
		};
		LibTTx.setAcceleratedAction(prefsApplyAction,
				"Apply the current tabs settings immediately", 'A', KeyStroke
						.getKeyStroke("alt A"));

		// creates the reject action, something I'm all too familiar with
		prefsCancelAction = new AbstractAction("No way", null) {
			public void actionPerformed(ActionEvent evt) {
				prefs.dispose();
				prefs = null;
			}
		};
		LibTTx.setAcceleratedAction(prefsCancelAction, "Cancel", 'N', KeyStroke
				.getKeyStroke("alt C"));
		getPrefs();

		/* Setup the main Text Trix window */

		setTitle("Text Trix");

		// restore window size and location
		setSize(getPrefs().getPrgmWidth(), getPrefs().getPrgmHeight());
		setLocation(new Point(getPrefs().getPrgmXLoc(), getPrefs()
				.getPrgmYLoc()));
		// store window size and location with each movement
		addComponentListener(new ComponentListener() {
			public void componentMoved(ComponentEvent evt) {
				getPrefs().storeLocation(getLocation());
			}

			public void componentResized(ComponentEvent evt) {
				getPrefs().storeSize(getWidth(), getHeight());
			}

			public void componentShown(ComponentEvent evt) {
			}

			public void componentHidden(ComponentEvent evt) {
			}
		});

		// set frame icon
		ImageIcon im = LibTTx.makeIcon("images/minicon-16x16.png");
		if (im != null) {
	    		setIconImage(im.getImage());
		}

		/* Create the main Text Trix frame components */
		
		groupTabbedPane = new MotherTabbedPane(
			JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		addTabbedPane(groupTabbedPane, "");

		// display tool tips for up to 100s
		ToolTipManager.sharedInstance().setDismissDelay(100000);

		// set text and web file filters for open/save dialog boxes
		chooser = new JFileChooser();
		allFilter = chooser.getFileFilter();
		final ExtensionFileFilter webFilter = new ExtensionFileFilter();
		webFilter.addExtension("html");
		webFilter.addExtension("htm");
		webFilter.addExtension("xhtml");
		webFilter.addExtension("shtml");
		webFilter.addExtension("css");
		webFilter.addExtension("js");
		webFilter.setDescription("Web files (*.html, *.htm, "
				+ "*.xhtml, *.shtml, *.css, *.js)");
		chooser.setFileFilter(webFilter);

		// RTF file filters
		final ExtensionFileFilter rtfFilter = new ExtensionFileFilter();
		rtfFilter.addExtension("rtf");
		rtfFilter.setDescription("RTF files (*.rtf)");
		chooser.setFileFilter(rtfFilter);

		// source code filters
		final ExtensionFileFilter prgmFilter = new ExtensionFileFilter();
		prgmFilter.addExtension("java");
		prgmFilter.addExtension("cpp");
		prgmFilter.addExtension("c");
		prgmFilter.addExtension("sh");
		prgmFilter.addExtension("js");
		prgmFilter
				.setDescription("Programming source code (*.java, *.cpp, *.c, *.sh, *.js)");
		chooser.setFileFilter(prgmFilter);

		// Text! filters
		final ExtensionFileFilter txtFilter = new ExtensionFileFilter();
		txtFilter.addExtension("txt");
		txtFilter.setDescription("Text files (*.txt)");
		chooser.setFileFilter(txtFilter);

		chooser.setFileFilter(allFilter);

		// prepare the file history
		fileHist = new FileHist();

		// invoke the worker thread to create the initial menu bar;
		(menuBarCreator = new MenuBarCreator()).start();
		
		// invoke worker thread to create status bar
		statusBarPanel = new JPanel(); // worker builds on the panel
		// other thread interacts with statusBar label, so need to create early
		statusBar = new JLabel("Welcome to Text Trix, the super text tool chest!");
		(statusBarCreator = new StatusBarCreator()).start();

		// open the initial files and create the status bar;
		// must make sure that all of the operations do not require anything
		// from
		// the menu or tool bars, which MenuBarCreator is the process of making
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// creates a panel to store the components that will
				// fit into the center position of the main window
				JPanel centerPanel = new JPanel(new BorderLayout());

				// adds the panel's compoenents
				centerPanel.add(getGroupTabbedPane(), BorderLayout.CENTER);
				centerPanel.add(statusBarPanel, BorderLayout.SOUTH);
				// adds the panel to the main window, central position
				contentPane.add(centerPanel, BorderLayout.CENTER);

				// make first tab and text area;
				// can only create after making several other user interface
				// components, such as the autoIndent check menu item
//				addTextArea(getSelectedTabbedPane(), makeNewFile());

				// load files specified at start from command-line
				if (paths != null) {
					for (int i = 0; i < paths.length; i++) {
						openInitialFile(paths[i]);
					}
				}

				// load files left open at the close of the last session
				String reopenPaths = getPrefs().getReopenTabsList();
				if (getPrefs().getReopenTabs()) {
					// the list consists of a comma-delimited string of
					// filenames
					String[] grpTokens = reopenPaths.split(FILE_GROUP_SPLITTER_REGEX);
					
					// Start at 1 b/c first token is the empty group splitter
					for (int i = 1; i < grpTokens.length; i++) {
						System.out.println("grpTokens[" + i + "]:" + grpTokens[i]);
						String[] tokens = grpTokens[i].split(FILE_SPLITTER);
						// Blank group already open initally, so simply update title
						// and start adding files
						if (i == 1) {
							setSelectedTabbedPaneTitle(tokens[0]);
						} else {
							addTabbedPane(getGroupTabbedPane(), tokens[0]);
						}
						for (int h = 1; h < tokens.length; h++) {
							openInitialFile(tokens[h]);
						}
					}
					/*
					// the list consists of a comma-delimited string of
					// filenames
					String[] tokens = getPrefs().getReopenTabsList().split(FILE_SPLITTER);
					for (int i = 0; i < tokens.length; i++) {
//						System.out.println("tokens[" + i + "]:" + tokens[i]);
						String[] grpTokens = tokens[i].split(FILE_GROUP_SPLITTER_REGEX);
						openInitialFile(grpTokens[0]);
						String title = "";
						for (int h = 1; h < grpTokens.length; h++) {
							addTabbedPane(getGroupTabbedPane());
							openInitialFile(grpTokens[h]);
						}
					}
					StringTokenizer tokenizer = new StringTokenizer(getPrefs()
							.getReopenTabsList(), ",");
					while (tokenizer.hasMoreElements()) {
						String token = tokenizer.nextToken();
						StringTokenizer grpTok = new StringTokenizer(token, "|");
						openInitialFile(token);
					}
					*/
				}
				getGroupTabbedPane().setSelectedIndex(0);

				// make the file history menu entries and set the auto-indent
				// check box
				syncMenus();

			}
		});

	}

	/**
	 * Publically executable starter method. Creates the <code>TextTrix</code>
	 * object, displays it, an makes sure that it will still undergo its exit
	 * routine when closed manually.
	 * 
	 * @param args
	 *            command-line arguments; not yet used
	 */
	public static void main(String[] args) {
		// set the look and feel;
		try {

			/*
			 * JRE Bug 4275928, "RFE: Mnemonics for non-character keys are
			 * displayed incorectly in the tooltip"
			 * (http://developer.java.sun.com/developer/bugParade/bugs/435928.html)
			 * notes that tool tips often append "Alt-" to the shortcut key,
			 * even if the shortcut should be for "Ctrl-" or another modifier
			 * key. In particular, Actions in JMenus display the correct
			 * accelerator key in both the tool tip and the on the right side of
			 * the menu item, but JToolBars show the accelerator key as using
			 * "Alt-" instead of the true modifier. To avoid confusing the user,
			 * the pseudo-workaround is to simply prevent the tool tips from
			 * showing the accelerator keys. More extensive workarounds subclass
			 * several swing and java package classes.
			 */
			UIManager.getDefaults().put("ToolTipUI",
					"javax.swing.plaf.basic.BasicToolTipUI");

			/* UPDATE: This code appears unnecessary now that JVM v.1.5 has
			 * been released.
			 * 
			 * GTK+ look and feel for Java v.1.4.2 running on Linux, otherwise
			 * the default look and feel, such as the new XP interface for
			 * Microsoft Windows XP systems with Java v.1.4.2. According to
			 * http://java.sun.com/j2se/1.4.2/docs/guide/swing/1.4/Post1.4.html,
			 * UIManager.getSystemLookAndFeelClassName() will return GTK+ by
			 * default in Java v.1.5.
			 */
			if (System.getProperty("os.name").equals("Linux")
					&& System.getProperty("java.vm.version").indexOf("1.4.2") != -1) {
				// GTK+ only for available systems
				/*
				 * UIManager.setLookAndFeel("com.sun.java.swing.plaf" +
				 * ".gtk.GTKLookAndFeel");
				 */
				//	    } else if (System.getProperty("mrj.version") != null) {
				/*
				 * } else if
				 * (System.getProperty("java.vm.version").indexOf("1.5") != -1) { //
				 * the new Java v.1.5.0, Tiger release, contains a revamped //
				 * Swing look-and-feel called "Ocean," implemented by default
				 */
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
		textTrix.setVisible(true);

		/*
		 * Something apparently grabs focus after he tabbed pane ChangeListener
		 * focuses on the selected TextPad. Calling focus after displaying the
		 * window seems to restore this focus at least most of the time.
		 */
		focuser();
	}

	/**
	 * Synchronizes the menus with the current text pad settings. Creates the
	 * file history menu entries in the File menu and flags the auto-indent
	 * check box according to the current text pad's setting.
	 *  
	 */
	public void syncMenus() {
		if (fileHistStart != -1) {
			fileHist.start(fileMenu); // assumes fileHistStart is up-to-date
		}
		setAutoIndent();
	}

	/**
	 * Opens the given file. Useful for opening files at program start-up
	 * because only gives command-line feedback if the file cannot be opened;
	 * the user may not have expected the opening in the first place and thus
	 * does not have to be needlessly concerned.
	 * 
	 * @param path
	 *            path to file
	 */
	private void openInitialFile(String path) {
		System.out.print("Opening file from path " + path + "...");
		if (!openFile(new File(path), true, false, false)) {
			String msg = newline + "Sorry, but " + path + " can't be read."
					+ newline + "Is it a directory?  Does it have the right "
					+ "permsissions for reading?";
			System.out.println(msg);
		} else {
			System.out.println("got it!");
		}
	}

	/**
	 * Switches focus synchronously to the selected <code>TextPad</code>, if
	 * one exists.
	 */
	public static void focuser() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				TextPad t = getSelectedTextPad();
				if (t != null)
					t.requestFocusInWindow();
			}
		});
	}

	/**
	 * Updates the main Text Trix frame's heading. Window managers often use the
	 * title to display in taskbar icons. The updater is useful to display the
	 * name of the currently selected file, for example. This name is
	 * automatically appended to the front of the text, " - Text Trix".
	 * 
	 * @param frame
	 *            the Text Trix window frame
	 * @param filename
	 *            name of given file, such as the currently displayed one
	 */
	public static void updateTitle(JFrame frame, String filename) {
		String titleSuffix = " - Text Trix";
		frame.setTitle(filename + titleSuffix);
	}

	/**
	 * Updates the main Text Trix frame's heading. Window managers often use the
	 * title to display in taskbar icons. The updater is useful to display the
	 * name of the currently selected file, for example. This name is
	 * automatically appended to the front of the text, " - Text Trix".
	 * 
	 * @param filename
	 *            name of given file, such as the currently displayed one
	 */
	public void updateTitle(String filename) {
		String titleSuffix = " - Text Trix";
		setTitle(filename + titleSuffix);
	}

	/**
	 * Gets the preferences panel object. Creates a new object if necessary,
	 * such as after the old panel has been cancelled and thus set to
	 * <code>null</code>. Assumes that the necessary "okay," "apply," and
	 * "cancel" actions have already been created.
	 * 
	 * @return the preferences panel object
	 */
	public Prefs getPrefs() {
		return (prefs == null) ? prefs = new Prefs(this, prefsOkayAction,
				prefsApplyAction, prefsCancelAction) : prefs;
	}

	/**
	 * Applies the settings from the preferences panel. Reloads the plug-ins,
	 * applies the preferences from the General and Shorts tabs, and creates new
	 * menu and tool bars. Front end for several functions that update the
	 * program to match the user's preferences settings.
	 * 
	 * @see #applyGeneralPrefs()
	 * @see #applyShortsPrefs()
	 * @see #reloadPlugIns()
	 */
	public void applyPrefs() {
		reloadPlugIns();
		// no applyPlugInsPrefs b/c CreateMenuPanel takes care of GUI updates
		applyGeneralPrefs();
		applyShortsPrefs();
		menuBarCreator.start();
	}

	/**
	 * Applies preferences from the General tab in the preferences panel.
	 * 
	 *  
	 */
	public void applyGeneralPrefs() {
		fileHist.start(fileMenu);
		MotherTabbedPane pane = getGroupTabbedPane();
		int origPaneIndex = pane.getSelectedIndex();
		// TODO: access text pad directly, rather than through selection
		for (int h = 0; h < pane.getTabCount(); h++) {
			pane.setSelectedIndex(h);
//			System.out.println("h: " + h);
			for (int i = 0; i < getSelectedTabbedPane().getTabCount(); i++) {
				TextPad pad = getTextPadAt(i);
				if (getPrefs().getAutoSave()) {
					if (pad.getChanged()) {
						startTextPadAutoSaveTimer(pad);
					}
				} else {
					stopTextPadAutoSaveTimer(pad);
				}
			}
		}
		pane.setSelectedIndex(origPaneIndex);
	}

	/**
	 * Applies preferences from the Shorts tab in the preferences panel. Relies
	 * on a separate call to <code>#menuBarCreator.start()</code> to complete
	 * the shortcuts update. For example, <ocde>applyPrefs()</code> calls the
	 * method to create the menu bar.
	 *  
	 */
	public void applyShortsPrefs() {
		MotherTabbedPane pane = getGroupTabbedPane();
		int origPaneIndex = pane.getSelectedIndex();
		// applies shortcuts according to user choice in preferences
		// TODO: access text pad directly, rather than through selection
		if (prefs.isHybridKeybindings()) {
			for (int h = 0; h < pane.getTabCount(); h++) {
				pane.setSelectedIndex(h);
				// mix of standard + Emacs-style shortcuts
				for (int i = 0; i < getSelectedTabbedPane().getTabCount(); i++) {
					getTextPadAt(i).hybridKeybindings();
				}
			}
		} else if (prefs.isEmacsKeybindings()) {
			for (int h = 0; h < pane.getTabCount(); h++) {
				pane.setSelectedIndex(h);
				// Emacs-style shortcuts
				for (int i = 0; i < getSelectedTabbedPane().getTabCount(); i++) {
					getTextPadAt(i).emacsKeybindings();
				}
			}
		} else {
			for (int h = 0; h < pane.getTabCount(); h++) {
				pane.setSelectedIndex(h);
				// standard shortcuts
				for (int i = 0; i < getSelectedTabbedPane().getTabCount(); i++) {
					getTextPadAt(i).standardKeybindings();
				}
			}
		}
		pane.setSelectedIndex(origPaneIndex);
	}

	/**
	 * Asks the user whether to continue updating the preferences. Updating the
	 * preferences may close some plug-in windows, which the user may choose to
	 * avoid by canceling the update.
	 * 
	 * @return <code>true</code> if the user opts to continue with the update
	 */
	public boolean continuePrefsUpdate() {
		// cycles through the plug-ins to check if any has a visible window;
		// stops at the first such plug-in, queries the user, and returns
		// the user's response
		for (int i = 0; i < plugInDiagsIdx; i++) {
			// checks if plug-in has open window
			if (plugInDiags[i].isVisible()) {
				// found open window--will query user
				int choice = JOptionPane.showConfirmDialog(getPrefs(),
						"Some plug-in windows may be closed.  Keep on going?",
						"Plug-in windows...", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				// returns user's response, exiting plug-in cycling
				if (choice == JOptionPane.YES_OPTION) {
					return true;
				} else {
					return false;
				}
			}
		}
		// assumes true if no open plug-in window
		return true;
	}

	/**
	 * Creates a plugin action. Allows the plugin to be invoked from a button or
	 * other action-capable interface.
	 * 
	 * @param pl
	 *            plugin from which to make an action
	 */
	public void makePlugInAction(final PlugIn pl) {

		// checks that user has chosen to include the plug-in

		// assumes prefs' includes is udpated
		String[] includes = getPrefs().getIncludePlugInsList();
		// exits if plug-in ignored
		if (!getPrefs().getAllPlugIns()
				&& !LibTTx.inUnsortedList(pl.getPath(), includes)) {
			return;
		}

		// retrieves the plug-in information
		String name = pl.getName(); // plugin name
		String category = pl.getCategory();
		// plugin category, for menu adding
		String description = pl.getDescription();
		// brief description;
		// reader for extended description
		BufferedReader detailedDescriptionBuf = pl.getDetailedDescription();
		ImageIcon icon = pl.getIcon(); // icon
		ImageIcon rollIcon = pl.getRollIcon(); // icon for mouse-rollover

		// create the listener to respond to events that the plug in fires
		PlugInAction listener = new PlugInAction() {
			public void runPlugIn(PlugInEvent event) {
				textTinker(pl);
			}
		};
		// register the listener so the plug in knows to fire it
		pl.addPlugInListener(listener);

		// sets up a plug-in window for PlugInWindow objects
		if (pl instanceof PlugInWindow) {
			final JDialog dialog = new JDialog(this, name);
			JPanel panel = pl.getWindow();
			final String filename = pl.getFilename();
			if (panel != null) {
				dialog.setContentPane(panel);
				getPrefs().applyPlugInSizeLoc(dialog, filename, 
					pl.getWindowWidth(), pl.getWindowHeight());
				dialog.setName(filename);
				addPlugInDialog(dialog);
			}

			// store window size and location with each movement
			ComponentListener compListener = new ComponentListener() {
				public void componentMoved(ComponentEvent evt) {
					getPrefs().storePlugInLocation(filename,
							dialog.getLocation());
				}

				public void componentResized(ComponentEvent evt) {
					getPrefs().storePlugInSize(filename, dialog.getWidth(),
							dialog.getHeight());
				}

				public void componentShown(ComponentEvent evt) {
				}

				public void componentHidden(ComponentEvent evt) {
				}
			};
			dialog.addComponentListener(compListener);
			WindowAdapter winAdapter = new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					pl.stopPlugIn();
				}
			};
			dialog.addWindowListener(winAdapter);

		}

		// action to start the plug in, such as invoking its options
		// panel if it has one;
		// invokes the plugin's text manipulation on the current TextPad's text
		Action startAction = new AbstractAction(name, icon) {
			public void actionPerformed(ActionEvent evt) {
				pl.startPlugIn();
				if (pl instanceof PlugInWindow) {
					JDialog diag = null;
					//System.out.println("looking for: " + pl.getFilename());
					if ((diag = getPlugInDialog(pl.getFilename())) != null) {
						//System.out.println("found it!");
						TextPad t = getSelectedTextPad();
						int selectionStart = -1;
						int selectionEnd = -1;
						if (t != null) {
							selectionStart = t.getSelectionStart();
							selectionEnd = t.getSelectionEnd();
						}
						diag.setVisible(true);
						if (t != null && selectionStart != selectionEnd) {
							textSelection(t, 0, selectionStart, selectionEnd);
						}
					}
				}
			}
		};

		// add the action to the appropriate menu
		if (category.equalsIgnoreCase("tools")) {
			toolsCharsUnavailable = LibTTx.setAction(startAction, name,
					description, toolsCharsUnavailable);
			toolsMenu.add(startAction);
		} else {
			trixCharsUnavailable = LibTTx.setAction(startAction, name,
					description, trixCharsUnavailable);
			trixMenu.add(startAction);
		}

		// add the action to a tool bar menu
		JButton button = toolBar.add(startAction);
		button.setBorderPainted(false);
		LibTTx.setRollover(button, rollIcon);
		if (detailedDescriptionBuf != null)
			button.setToolTipText(LibTTx.readText(detailedDescriptionBuf));
	}
	
	/**Adds a plug-in dialog to an array of plug-in dialogs.
	 * Updates the dialog if a new one is given.
	 * @param diag the dialog to add
	*/
	public void addPlugInDialog(JDialog diag) {
		// finds the array index for the plug-in's dialog
		int found = getPlugInDialogIndex(diag.getName());
		// if the plug-in already has a dialog, it is replaced with the 
		// given one
		if (found != -1) {
			plugInDiags[found] = diag;
		} else {
			// added as a new element in the array if plug-in
			// previously non-existant or had no dialog
			if (plugInDiagsIdx >= plugInDiags.length) {
				plugInDiags = (JDialog[]) LibTTx.growArray(plugInDiags);
			}
			plugInDiags[plugInDiagsIdx++] = diag;
			sortPlugInDialogs();
		}
	}
	
	/**Sorrts the array of plug-in dialogs by name of plug-in.
	*/
	public void sortPlugInDialogs() {
		int start = 0;
		int end = 0;
		int gap = 0;
		int n = plugInDiagsIdx;
		JDialog tmp = null;
		for (gap = n / 2; gap > 0; gap /= 2) {
			for (end = gap; end < n; end++) {
				for (start = end - gap; start >= 0
						&& (plugInDiags[start].getName()
								.compareToIgnoreCase(plugInDiags[start + gap]
										.getName())) > 0; start -= gap) {
					tmp = plugInDiags[start];
					plugInDiags[start] = plugInDiags[start + gap];
					plugInDiags[start + gap] = tmp;
				}
			}
		}

	}
	
	/**Gets a plug-in dialog by name.
	 * @param name name of plug-in
	 * @return dialog window that corresponds to the plug-in
	*/
	public JDialog getPlugInDialog(String name) {
		int found = getPlugInDialogIndex(name);
		return (found != -1) ? plugInDiags[found] : null;
		
	}
	
	/**Gets the index value of the plug-in by name.
	 * @param name name of the plug-in
	 * @return index of the corresponding plug-in dialog in the array
	 * of plug-in dialogs
	*/
	public int getPlugInDialogIndex(String name) {
		if (name == null)
			return -1;
		int start = 0;
		int end = plugInDiagsIdx - 1;
		int mid = end / 2;
		int found = -1;
		String s = "";
		// convert name and later the roster name to lower case in case
		// user uses capital letters inconsistently
		while (start <= end && found == -1) {
			// cycle through the roster and pick by name
			if ((s = plugInDiags[mid].getName()).equalsIgnoreCase(name)) {
				found = mid;
			} else if (name.compareToIgnoreCase(s) < 0) {
				end = mid - 1;
			} else {
				start = mid + 1;
			}
			mid = (start + end) / 2;
		}
		return found;

	}

	/**
	 * Run a text-manipulating plug-in on the selected text pad's text. If a
	 * given region is selected, the plug-in will only work on that area, unless
	 * the plug-in's <code>alwaysEntireText</code> variable is
	 * <code>true</code>. If so, the plug-in will receive the entire body of
	 * text as well as the positions of selected text. The plug-in receives the
	 * entire body but only the caret position when no text is selected.
	 * 
	 * @param pl
	 *            plugin to invoke
	 */
	public void textTinker(PlugIn pl) {
		TextPad t = getSelectedTextPad();
		if (t != null) {
			viewPlain();
			// plugins generally need to work on displayed text
			// works through Document rather than getText/setText since
			// the latter method does not seem to work on all systems,
			// evidently only delivering and working on the current line
			Document doc = t.getDocument();
			String text = null; // text from the Document methods

			// unless flagged otherwise, only modify the selected text, and make
			// the action undoable
			int start = t.getSelectionStart();
			int end = t.getSelectionEnd(); // at the first unselected character
			PlugInOutcome outcome = null;
			try {
				t.startCompoundEdit();
				// determines whether a region is selected or not;
				// if not, or plug-in flags to works on the text pad's entire
				// text,
				// does so
				if (start == end || pl.getAlwaysEntireText()) { // no selection
					text = doc.getText(0, doc.getLength()); // all the text

					// invokes the plugin: need both start and ending selection
					// positions when "alwaysEntireText" b/c want to both get
					// all of the text and reshow its highlighted portion,
					// rather than only getting the highlighted part
					outcome = pl.getAlwaysEntireText() 
						? pl.run(text, start, end) : pl.run(text, end);

					// if the plug-in flags that it has not changed the text,
					// don't even try to do so
					if (!outcome.getNoTextChange()) {
						replaceSection(t, doc, outcome, 0, doc.getLength());
					}
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

					// start and end only
					outcome = pl.run(text); // invoke the plugin

					// if the plug-in flags that it has not changed the text,
					// don't even try to do so
					if (!outcome.getNoTextChange()) {
						replaceSection(t, doc, outcome, start, len);
					}
					// caret automatically returns to end of selected region
					int i = -1;
					if ((i = outcome.getSelectionStart()) != -1)
						textSelection(t, start, i, outcome.getSelectionEnd());
				}
				t.stopCompoundEdit();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**Replaces a section of the given pad's document according
	 * to the specifications in the plug-in outcome object.
	 * @param t the pad to alter
	 * @param outcome the specifications for altering the text
	 * @param start the default char at which to begin replacing
	 * text with that from <code>outcome</code>
	 * @param len the default length of the section to replace
	*/
	public void replaceSection(TextPad t, 
		Document doc, 
		PlugInOutcome outcome, 
		int start, 
		int len) throws BadLocationException {
		
		// gathers the position of the region to replace if replaceStart
		// from the outcome specifies it explicitly
		if (outcome.getReplaceStart() != -1) {
			start = outcome.getReplaceStart();
			len = outcome.getReplaceEnd() - start;
		}
		
		// replaces the section
		doc.remove(start, len); // remove all the text
		doc.insertString(start, outcome.getText(), null);	// insert text
		
		// auto-wrap-indent the new section
		if (t.isAutoIndent() ) t.indentRegion(start, outcome.getText().length());
	}

	/**
	 * Selects the given region of text. Works on the given text pad.
	 * 
	 * @param t
	 *            given text pad, not necessarily the selected one, though
	 *            probably only relevant if so
	 * @param baseline
	 *            starting point from which to measure <code>start</code> and
	 *            <cdoe>end</code>
	 * @param start
	 *            beginning point of selection, relative to baseline
	 * @param end
	 *            end point of selection, relative to baseline
	 */
	public void textSelection(TextPad t, int baseline, int start, int end) {
		if (start == end) {
			t.setCaretPosition(baseline + start);
//			System.out.println("I'm here");
		} if (end != -1) {
			t.setCaretPosition(baseline + start);
			t.moveCaretPosition(baseline + end);
			t.getCaret().setSelectionVisible(true);
			// to ensure selection visibility
		} else {
			t.setCaretPosition(baseline + start);
		}
	}
	
	/**Selection the given region of text, but in reverse order as
	 * <code>textSelection(TextPad, int, int, int)</code>.
	 * Useful when the caret should be at the beginning of the text rather
	 * than at the end.  The same as the normal textSelection function,
	 * but with <code>start</code> and <code>end</code> given
	 * in reverse, so that <code>start</code> still refers to th earlier
	 * portion of the text, while <code>end</code> refers to the latter.
	 * Note that "loaded plug-in" does not mean that the plug-in is in use,
	 * but includes both used and ignored plug-ins that Text Trix has
	 * read and at least prepared for use.
	 * @param t
	 *            given text pad, not necessarily the selected one, though
	 *            probably only relevant if so
	 * @param baseline
	 *            starting point from which to measure <code>start</code> and
	 *            <cdoe>end</code>
	 * @param start
	 *            beginning point of selection, relative to baseline
	 * @param end
	 *            end point of selection, relative to baseline
	 */
	public void textSelectionReverse(TextPad t, int baseline, int start, int end) {
		textSelection(t, baseline, end, start);
	}

	/**
	 * Reloads all the plug-ins in the current <code>plugins</code> folder.
	 * Prepares the preferences panel to offer all available plug-ins.  Also
	 * called when loading applying preferences again.
	 * 
	 * @see #getPlugInsFile()
	 *  
	 */
	public void reloadPlugIns() {
		// determine the available plug-ins
		File file = getPlugInsFile();
		String[] list = LibTTx.getPlugInPaths(file);
		if (list == null)
			return;

		// determine the currently loaded plug-ins
		String[] paths = getPlugInPaths();
		PlugIn[] extraPlugs = null;
		int extraPlugsInd = 0;

		// load unloaded plug-ins; drop plug-ins no longer available
		if (plugIns == null || plugIns.length == 0) {
			setupPlugIns();
		} else if (paths != null) {
			extraPlugs = new PlugIn[list.length + plugIns.length];
			// check for extant but unloaded plug-ins files
			for (int i = 0; i < list.length; i++) {
				if (!LibTTx.inUnsortedList(list[i], paths)) {
					// ensure that plug-in loaded properly before adding to array
					PlugIn pl = LibTTx.loadPlugIn(list[i]);
					if (pl != null) extraPlugs[extraPlugsInd++] = pl;
				}
			}
			// check for loaded but now missing plug-in files
			for (int i = 0; i < plugIns.length; i++) {
				if (LibTTx.inUnsortedList(plugIns[i].getPath(), list)) {
					extraPlugs[extraPlugsInd++] = plugIns[i];
				}
			}
			/*
			 * TODO: May need to create a temporary collection of all loaded
			 * plug-ins rather than adding the extraPlugs to the official,
			 * active plugIns group.
			 */
			plugIns = (PlugIn[]) LibTTx
					.truncateArray(extraPlugs, extraPlugsInd);

			if (!getPrefs().getAllPlugIns()) {
				getPrefs().updatePlugInsPanel(getPlugInPaths());
				String[] includes = getPrefs().getIncludePlugInsList();
				JDialog diag = null;
				for (int i = 0; i < plugIns.length; i++) {
					if (plugIns[i] instanceof PlugInWindow
							&& !LibTTx.inUnsortedList(plugIns[i].getPath(),
									includes)) {
						if ((diag = getPlugInDialog(plugIns[i].getFilename())) != null) {
							diag.setVisible(false);//closeWindow();
						}
					}
				}
			}
		}
	}
	
	/**Refreshes the list of available plug-ins.
	 * "Available plug-ins" are those that are in the "plugins" folder
	 * and have a <code>.jar</code> extension.
	*/
	public void refreshPlugInsPanel() {

		// determine the available plug-ins
		File file = getPlugInsFile();
		String[] list = LibTTx.getPlugInPaths(file);
		getPrefs().updatePlugInsPanel(list);
	}

	/**
	 * Gets the names of the currently loaded plug-ins. Each plug-in has a
	 * descriptive name, usually different from the filename.
	 * 
	 * @return array of all the loaded plug-ins' descriptive name; the array's
	 *         length is equal to the number off names
	 */
	public String[] getPlugInNames() {
		if (plugIns == null)
			return null;
		String[] names = new String[plugIns.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = plugIns[i].getName();
		}
		return names;
	}

	/**
	 * Gets the paths of all currently loaded plug-ins.
	 * These plug-ins may have just been loaded by a call to 
	 * <code>reloadPlugIns</code>.
	 * Note that "loaded plug-in" does not mean that the plug-in is in use,
	 * but includes both used and ignored plug-ins that Text Trix has
	 * read and at least prepared for use.
	 * 
	 * @return array of all the loaded plug-in's paths and length equal to the
	 *         number of these paths
	 */
	public String[] getPlugInPaths() {
		if (plugIns == null)
			return null;
		String[] paths = new String[plugIns.length];
		for (int i = 0; i < paths.length; i++) {
			paths[i] = plugIns[i].getPath();
		}
		return paths;
	}

	/**
	 * Loads and set up the plugins. Retrieves them from the "plugins"
	 * directory, located in the same directory as the executable JAR for
	 * TextTrix.class or the "com" directory in the "com/textflex/texttrix"
	 * sequence holding TextTrix.class. TODO: also search the user's home
	 * directory or user determined locations, such as ones a user specifies via
	 * a preferences panel.
	 */
	public void setupPlugIns() {
		File plugInsFile = getPlugInsFile(); //refreshPlugIns();

		// load the plugins and create actions for them
		plugIns = LibTTx.loadPlugIns(plugInsFile);
		plugInDiags = new JDialog[plugIns.length];
		plugInDiagsIdx = 0;
		getPrefs().updatePlugInsPanel(getPlugInPaths());
		if (plugIns != null) {
			for (int i = 0; i < plugIns.length; i++) {
				//				plugIns[i].removeWindowAdapter();
				makePlugInAction(plugIns[i]);
			}
		}
	}

	/**
	 * Gets the <code>plugins</code> folder file. TODO: Add preferences option
	 * to specify alternative or additional <code>plugins</code> folder
	 * location, such as a permanent storage place to reuse plug-ins after
	 * installing a new version of Text Trix.
	 * 
	 * @return <code>plugins</code> folder
	 */
	public File getPlugInsFile() {
		/*
		 * The code has a relatively elaborate mechanism to locate the plugins
		 * folder and its JAR files. Why not use the URL that the Text Trix
		 * class supplies? Text Trix needs to locate each JAR plugin's absolute
		 * path and name. Text Trix's URL must be truncated to its root
		 * directory's location and built back up through the plugins directory.
		 * Using getParentFile() to the program's root and appending the rest of
		 * the path to the plugins allows one to use URLClassLoader directly
		 * with the resulting URL.
		 * 
		 * Unfortunately, some systems do not locate local files with this
		 * method. The following elaborate system works around this apparent JRE
		 * bug by further breaking the URL into a normal path and loading a file
		 * from it.
		 * 
		 * Unfortunately again, a new feature from JRE v.1.4 causes spaces in
		 * URL strings to be converted to "%20" turning URL's into strings. The
		 * JRE cannot load files with "%20" in them, however; for example,
		 * "c:\Program Files\texttrix-x.y.z\plugins" never gets loaded. The
		 * workaround is to replace all "%20"'s in the string with " ". Along
		 * with v.1.4 comes new String regex tools to make the operation simple,
		 * but prior versions crash after a NoSuchMethodError. The replacement
		 * must be done manually.
		 */

		// this class's location
		String relClassLoc = "com/textflex/texttrix/TextTrix.class";
		URL urlClassDir = ClassLoader.getSystemResource(relClassLoc);
		String strClassDir = urlClassDir.getPath();
		// to check whether JAR
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
		/*
		 * convert "%20", the escape character for a space, into " "; conversion
		 * necessary starting with JRE v.1.4.0 (see
		 * http://developer.java.sun.com/developer/ //
		 * bugParade/bugs/4466485.html)
		 */
		String strBaseDir = baseDir.toString();
		int space = 0;
		// continue while still have "%20", the spaces symbol
		while ((space = strBaseDir.indexOf("%20")) != -1) {
			if (strBaseDir.length() > space + 3) {
				strBaseDir = strBaseDir.substring(0, space) + " "
						+ strBaseDir.substring(space + 3);
			} else {
				strBaseDir = strBaseDir.substring(0, space) + " ";
			}
		}
		/*
		 * Though simpler, this alternative solution crashes after a
		 * NoSuchMethodError under JRE <= 1.3.
		 */
		/*
		 * baseDir = new File(baseDir.toString().replaceAll("%20", " ")); File
		 * plugInsFile = new File(baseDir, "plugins");
		 */

		// plugins directory;
		// considered nonexistent since baseDir's path in URL syntax
		File plugInsFile = new File(strBaseDir, "plugins");
		String plugInsPath = plugInsFile.getPath();

		// directory path given as URL; need to parse into normal syntax
		String protocol = "file:";
		int pathStart = plugInsPath.indexOf(protocol);
		// check if indeed given as URL;
		// if so, delete protocol and any preceding info
		if (pathStart != -1)
			plugInsPath = plugInsPath.substring(pathStart + protocol.length());
		// plugInsPath now in normal syntax
		plugInsFile = new File(plugInsPath); // the actual file
		
		// If necessary, adjust path to properly navigate across the network
		if (!plugInsFile.exists()) {
			// According to testing on Windows XP, an extra backslash needs
			// to be added to the start of the path to create the format: 
			// "\\COMPUTER_NAME\ShareName"
			plugInsFile = new File("\\" + plugInsPath);
		}
		
		if (!plugInsFile.exists()) {
			System.out.println("We truly apologize, but we haven't been able to locate"
				+ newline + plugInsFile.toString()
				+ newline + "for your plug-ins.");
		}

		/*
		 * A possible workaround for an apparent JRE v.1.4 bug that fails to
		 * open files with spaces in their paths. This workaround converts any
		 * file or directory names with their "8.3" formatted equivalents. For
		 * example, "Program Files" is converted to "PROGRA~1", which some
		 * systems might map to the intended file.
		 */
		/*
		 * if (!plugInsFile.exists()) { String seg = ""; StringTokenizer tok =
		 * new StringTokenizer(plugInsPath, "/\\"); StringBuffer buf = new
		 * StringBuffer(plugInsPath.length()); for (int i = 0;
		 * tok.hasMoreTokens(); i++) { seg = tok.nextToken(); if (seg.length() >
		 * 8) seg = seg.substring(0, 6).toUpperCase() + "~1";
		 * buf.append(File.separator + seg); } plugInsPath = buf.toString();
		 * plugInsFile = new File(plugInsPath); // the actual file //
		 * System.out.println(plugInsPath); }
		 */
		return plugInsFile;
	}

	/**
	 * Gets the last path for opening a file.
	 * 
	 * @return most recent path for opening a file
	 */
	public static String getOpenDir() {
		return openDir;
	}

	/**
	 * Gets the last path for saving a file.
	 * 
	 * @return most recent path for saving a file
	 */
	public static String getSaveDir() {
		return saveDir;
	}
	
	public static MotherTabbedPane getGroupTabbedPane() {
		return groupTabbedPane;
	}
	
	public static MotherTabbedPane getSelectedTabbedPane() {
		MotherTabbedPane pane = getGroupTabbedPane();
		int i = pane.getSelectedIndex();
		if (i != -1) {
			return (MotherTabbedPane) pane.getSelectedComponent();
		} else {
			return null;
		}
	}

	/**
	 * Gets the currently selected <code>TextPad</code>
	 * 
	 * @return <code>TextPad</code> whose tab is currently selected
	 */
	public static TextPad getSelectedTextPad() {
		MotherTabbedPane pane = getSelectedTabbedPane();
		int i = pane.getSelectedIndex();
		if (i != -1) {
			return (TextPad) ((JScrollPane) pane.getComponentAt(i)).getViewport().getView();//textAreas.get(i);
		} else {
			return null;
		}
	}

	/**
	 * Gets the <code>TextPad</code> at a given index in the tabbed pane.
	 * 
	 * @param i
	 *            index of <code>TextPad</code>
	 * @return the given Text Pad; <code>null</code> if the tabbed pane lacks
	 *         the index value
	 */
	public static TextPad getTextPadAt(int i) {
		return getTextPadAt(getSelectedTabbedPane(), i);
	}
	
	public static TextPad getTextPadAt(JTabbedPane pane, int i) {
		if (i < -1 || i >= pane.getTabCount())
			return null;
		return (TextPad) ((JScrollPane) pane.getComponentAt(i)).getViewport().getView();//(textAreas.get(i));
		
	}
	
	public static MotherTabbedPane getTabbedPaneAt(int i) {
		return (MotherTabbedPane) getGroupTabbedPane().getComponentAt(i);
	}

	public static MotherTabbedPane getTabbedPane(TextPad textPad) {
		int lenGroup = getGroupTabbedPane().getTabCount();
		int tabIndex = -1;
		MotherTabbedPane pane = null;
		for (int i = 0; i < lenGroup; i++) {
			pane = getTabbedPaneAt(i);
			if (pane.indexOfComponent(textPad.getScrollPane()) != -1) {
				return pane;
			}
		}
		/*
			pane = getTabbedPaneAt(i);
			int len = pane.getTabCount();
			for (int h = 0; h < len; h++) {
				if (getTextPadAt(pane, h) == textPad) {
					return pane;
				}
			}
		}
		*/
		System.out.println("didn't find it");
		return null;
	}
	
	public static int getTextPadIndex(JTabbedPane pane, TextPad textPad) {
		return pane.indexOfComponent(textPad.getScrollPane());
	}

/*
	public static int getTextPadIndex(TextPad textPad) {
		int len = getGroupTabbedPane().getTabCount();
		int tabIndex = -1;
		for (int i = 0; 
			i < len 
				&& (tabIndex = getTabbedPaneAt(i++)
					.indexOfComponent(textPad)) == -1;
			i++);
		return tabIndex;
	}
*/
	
	/**
	 * Gets whether the auto-indent function is selected.
	 * 
	 * @return <code>true</code> if auto-indent is selected
	 */
	public static boolean getAutoIndent() {
		return autoIndent.isSelected();
	}

	/**
	 * Sets the given path as the most recently one used to open a file.
	 * 
	 * @param anOpenDir
	 *            path to set as last opened location
	 */
	public static void setOpenDir(String anOpenDir) {
		openDir = anOpenDir;
	}

	/**
	 * Sets the given path as the most recently one used to save a file.
	 * 
	 * @param aSaveDir
	 *            path to set as last saved location.
	 */
	public static void setSaveDir(String aSaveDir) {
		saveDir = aSaveDir;
	}

	/**
	 * Flags the menu check box to indicate whether the current Text Pad is in
	 * auto-indent mode.
	 *  
	 */
	public static void setAutoIndent() {
		TextPad t = getSelectedTextPad();
		if (autoIndent != null && t != null) {
			autoIndent.setSelected(t.getAutoIndent());
		}
	}

	/**
	 * Makes new file with next non-existent file of name format,
	 * <code>NewFile<i>n</i>.txt</code>, where <code>n</code> is the next
	 * number that Text Trix has not made for a text area and that is not a
	 * curently existing file.
	 * 
	 * @return file with unique, non-existing name
	 */
	public File makeNewFile() {
		File file;
		do {
			fileIndex++;
			// ensures that no repeat of name in current session
			// check exisiting files to ensure unique name
		} while ((file = new File("NewFile" + fileIndex + ".txt")).exists());
		// okay to return mutable file since created it anew in this fn?
		// true--prob only unwise if accessor, which gives instance field
		return file;
	}

	/**
	 * Exits <code>TextTrix</code> by closing each tab individually, checking
	 * for unsaved text areas in the meantime.
	 */
	public boolean exitTextTrix() {
		// closes each tab individually, using the function that checks
		// for unsaved changes
		String openedPaths = "";
		boolean reopenTabs = getPrefs().getReopenTabs();
		//if (i <= 0 && reopenTabs) {
		getPrefs().storeReopenTabsList("");
		//}
		//int i = 0;
		boolean b = true;
		boolean newGrp = false;
		MotherTabbedPane pane = getGroupTabbedPane();
		for (int i = 0; i < pane.getTabCount() && b; i++) {
			openedPaths = openedPaths + FILE_GROUP_SPLITTER + getGroupTabbedPane().getTitleAt(i);
//			newGrp = true;
			pane.setSelectedIndex(i);
			int totTabs = getSelectedTabbedPane().getTabCount();
	
			// close the files and prepare to store their paths in the list
			// of files left open at the end of the session
			while (totTabs > 0 && b) {
				if (reopenTabs) {
					TextPad t = getTextPadAt(0);
					if (t.fileExists()) {
						openedPaths = openedPaths + FILE_SPLITTER + t.getPath();
					}
				}
				b = closeTextArea(0, getSelectedTabbedPane());
				totTabs = getSelectedTabbedPane().getTabCount();
			}
		}

		// store the file list and exit Text Trix all the files closed
		// successfully
		if (b == true) {
			if (reopenTabs) {
//				openedPaths = "";
				getPrefs().storeReopenTabsList(openedPaths);
			}
			System.exit(0);
		}
		return b;
	}
	
	public String getSelectedTabbedPaneTitle() {
		JTabbedPane pane = getGroupTabbedPane();
		return pane.getTitleAt(pane.getSelectedIndex());
	}
	
	public void setSelectedTabbedPaneTitle(String title) {
		JTabbedPane pane = getGroupTabbedPane();
		pane.setTitleAt(pane.getSelectedIndex(), title);
	}

	/**
	 * Closes a text area. Checks if the text area is unsaved; if so, evokes a
	 * dialog asking whether the user wants to save the text area, discard it,
	 * or cancel the closure. If the file has not been saved before, a
	 * <code>Save as...</code> dialog appears. Canceling the
	 * <code>Save as...</code> dialog discards the text area, though maybe not
	 * so in future releases.
	 * 
	 * @param tabIndex
	 *            tab to close
	 * @param tabbedPane
	 *            pane holding a tab to be closed
	 * @return <code>true</code> if the tab successfully closes
	 */
	public boolean closeTextArea(int tabIndex, MotherTabbedPane tabbedPane) {
		boolean successfulClose = false;
		
		TextPad t = getTextPadAt(tabbedPane, tabIndex);//(TextPad) tabbedPane.getComponentAt(tabIndex);// textAreas.get(tabIndex);
		// check if unsaved text area
		if (t.getChanged()) {
			String s = "Please save first.";
			tabbedPane.setSelectedIndex(tabIndex);
			// dialog with 3 choices: save, discard, cancel
			String msg = "This file has not yet been saved."
					+ "\nWhat would you like me to do with this new version?";
			int choice = JOptionPane.showOptionDialog(getThis(), msg,
					"Save before close", JOptionPane.WARNING_MESSAGE,
					JOptionPane.DEFAULT_OPTION, null, new String[] { "Save",
							"Toss it out", "Cancel" }, "Save");
			switch (choice) {
			// save the text area's contents
			case 0:
				// bring up "Save as..." dialog if never saved file before
				if (t.fileExists()) {
					successfulClose = saveFileOnExit(t.getPath());
				} else {
					// still closes tab if cancel "Save as..." dialog
					// may need to change in future releases
					successfulClose = fileSaveDialogOnExit(null);
				}
				if (successfulClose) {
					removeTextArea(tabIndex, tabbedPane);
				}
				break;
			// discard the text area's contents
			case 1:
				removeTextArea(tabIndex, tabbedPane);
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
			removeTextArea(tabIndex, tabbedPane);
			successfulClose = true;
		}

		/*
		 * Potential JVM bug (>= v.1.4.2): The tab change listener doesn't
		 * appear to respond to tab changes when closing a tab. Our listener
		 * updates the main window title when changing tabs, but the title
		 * remains the same after the user closes a window.
		 * 
		 * Workaround: The text pad close method manually updates the title for
		 * the newly selected pad, if it exists.
		 */
		t = getSelectedTextPad();
		if (successfulClose && t != null) {
			updateTitle(t.getFilename());
		}
		return successfulClose;
	}

	/**
	 * Reads in text from a file and return the text as a string. Uses
	 * <code>Class.getResourcesAsStream</code> so can read from JAR files; the
	 * <code>path</code> is relative to the given class.
	 * 
	 * @param path
	 *            text file stream
	 * @return text from file
	 */
	public String readText(String path) {
		String text = "";
		InputStream in = null;
		BufferedReader reader = null;
		try {
			in = TextTrix.class.getResourceAsStream(path);
			if (in == null)
				return "";
			reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null)
				text = text + line + "\n";
		} catch (IOException exception) {
			//	    exception.printStackTrace();
			return "";
		} finally { // stream closure operations
			try {
				if (reader != null)
					reader.close();
				if (in != null)
					in.close();
			} catch (IOException exception) {
				//	    exception.printStackTrace();
				return "";
			}
		}
		return text;
	}
	
	public void addTabbedPane(MotherTabbedPane tabbedPane, String title) {
		final MotherTabbedPane newTabbedPane = new MotherTabbedPane(JTabbedPane.TOP);
		
		// keep the tabs the same width when substituting chars
		newTabbedPane.setFont(new Font("Monospaced", Font.PLAIN, 11));

		int i = tabbedPane.getTabCount();
		if (title.equals("")) {
			title = "Group " + i;
		}
		tabbedPane.addTab(title, newTabbedPane);
		tabbedPane.setSelectedIndex(i);
		
		// TODO: focus in newly created tab
		addTextArea(newTabbedPane, makeNewFile());
		
		// adds a change listener to listen for tab switches and display the
		// options of the tab's TextPad
		newTabbedPane.addChangeListener(new TextPadChangeListener(newTabbedPane));
		
	}
	
	/**
	 * Creates a new <code>TextPad</code> object, a text area for writing, and
	 * gives it a new tab. Can call for each new file; names the tab,
	 * <code>Filen.txt</code>, where <code>n</code> is the tab number.
	 */
	public void addTextArea(MotherTabbedPane tabbedPane,
			File file) {
		final TextPad textPad = new TextPad(file, getPrefs());

		final JScrollPane scrollPane = new JScrollPane(textPad,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		textPad.setScrollPane(scrollPane);
		DocumentListener listener = new TextPadDocListener();

		// must add to array list before adding scroll pane to tabbed pane
		// or else get IndexOutOfBoundsException from the array list
//		tabbedPane.addTab(textPad);
//		arrayList.add(textPad);
		// 1 more than highest tab index since will add tab
		int i = tabbedPane.getTabCount();
		tabbedPane.addTab(file.getName() + " ", scrollPane);
		textPad.getDocument().addDocumentListener(listener);
		textPad.addMouseListener(new PopupListener(popup));
		textPad.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				updateStatusBarLineNumbers(textPad);
			}
		});
		// show " *" in tab title when text changed
		tabbedPane.setSelectedIndex(i);
		tabbedPane.setToolTipTextAt(i, textPad.getPath());
	}

	/**
	 * Changes given tab's title in the tabbed pane title to indicate whether
	 * the file has unsaved changes. Appends "<code> *</code>" if the file
	 * has unsaved changes; appends "<code>  </code>" otherwise.
	 * 
	 * @param textPad
	 *            tabbed pane to update
	 */
	public static void updateTabTitle(TextPad textPad) {
		MotherTabbedPane pane = getTabbedPane(textPad);
		int tabIndex = getTextPadIndex(pane, textPad);
		// if the tab index is given as -1, assumes that the current
		// tab should be updated
		/*
		if (i < 0) {
			i = tabbedPane.getSelectedIndex();
		}
		TextPad textPad = getTextPadAt(tabbedPane, i);//(TextPad) tabbedPane.getComponentAt(i);//arrayList.get(i);
		*/

		// updates the title with the filename and a flag indicating
		// whether the file has unsaved changes
		String title = textPad.getFilename();
		if (textPad.getChanged()) { // unsaved changes present
			pane.setTitleAt(tabIndex, title + "*");
		} else { // all changes saved
			pane.setTitleAt(tabIndex, title + " ");
		}
	}

	/**
	 * Changes current tab's title in the tabbed pane title to indicate whether
	 * the file has unsaved changes. Appends "<code> *</code>" if the file
	 * has unsaved changes; appends "<code>  </code>" otherwise.
	 * 
	 * @param arrayList
	 *            array of <code>TextPad</code> s that the tabbed pane
	 *            displays
	 * @param tabbedPane
	 *            tabbed pane to update
	 *
	public static void updateTabTitle(JTabbedPane tabbedPane) {
		// -1 indicates that the currently selected tab is the one
		// to update
		updateTabTitle(tabbedPane, -1);
	}
	*/

	/**
	 * Adds additional listeners and other settings to a <code>TextPad</code>.
	 * Useful to apply on top of the <code>TextPad</code>'s
	 * <code>applyDocumentSettings</code> function.
	 * 
	 * @param textPad
	 *            <code>TextPad</code> requiring applied settings
	 */
	public void addExtraTextPadDocumentSettings(TextPad textPad) {
		textPad.getDocument().addDocumentListener(new TextPadDocListener());
		textPad.setChanged(true);
		updateTabTitle(textPad);//getSelectedTabbedPane());
	}

	/**
	 * Displays the given <code>TextPad</code> in plain text format. Calls the
	 * <code>TextPad</code>'s<code>viewPlain</code> function before adding
	 * <code>TextTrix</code> -specific settings, such as a
	 * <code>TextPadDocListener</code>.
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

	/**
	 * Displays the given <code>TextPad</code> in html text format. Calls the
	 * <code>TextPad</code>'s<code>viewHTML</code> function before adding
	 * <code>TextTrix</code> -specific settings, such as a
	 * <code>TextPadDocListener</code>.
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

	/**
	 * Displays the given <code>TextPad</code> in RTF text format. Calls the
	 * <code>TextPad</code>'s<code>viewRTF</code> function before adding
	 * <code>TextTrix</code> -specific settings, such as a
	 * <code>TextPadDocListener</code>.
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

	/**
	 * Front-end to the <code>TextPad</code>'s<code>read</code> method
	 * from <code>JTextPane</code>. Reads in a file, applies
	 * <code>TextPad</code>'s settings, and finally adds
	 * <code>TextTrix</code> -specific settings.
	 * 
	 * @param textPad
	 *            <code>TextPad</code> to read a file into
	 * @param in
	 *            file reader
	 * @param desc
	 *            reader stream description
	 */
	public void read(TextPad textPad, Reader in, Object desc)
			throws IOException {
		textPad.read(in, desc);
		textPad.applyDocumentSettings();
		addExtraTextPadDocumentSettings(textPad);
	}

	public static void removeTabbedPane(MotherTabbedPane tp) {
		int i = tp.getSelectedIndex();
		if (i >= 0 && i < tp.getTabCount()) {
			tp.remove(i);
		}
	}
	/**
	 * Removes a tab containing a text area.
	 * 
	 * @param i
	 *            tab index
	 * @param tp
	 *            tabbed pane from which to remove a tab
	 */
	public static void removeTextArea(int i, MotherTabbedPane tp) {
		TextPad t = getSelectedTextPad();
		if (t != null) {
			stopTextPadAutoSaveTimer(t);
//			l.remove(i);
			tp.remove(i);
		}
	}

	/**
	 * Saves text area contents to a given path.
	 * 
	 * @param path
	 *            file path in which to save
	 * @param t
	 *            the pad to save; if <code>null</code>, defaults to the
	 *            currently selected pad
	 * @return true for a successful save, false if otherwise
	 * @see #saveFile(TextPad)
	 * @see #saveFile(String)
	 */
	public boolean saveFile(String path, TextPad t) {
		//	System.out.println("printing");
		if (t == null)
			t = getSelectedTextPad();
		PrintWriter out = null;
		try {
			if (t != null) {
				/*
				 * if don't use canWrite(), work instead by catching exception
				 * and either handling it there or returning signal of the
				 * failure
				 */
				// open the stream to write to
				out = new PrintWriter(new FileWriter(path), true);
				// write to it
				out.print(t.getText());
				t.setChanged(false);
				t.setFile(path);

				// stops any auto-save timer attached to the pad
				// since the file has just been saved;
				// relies on TextPadDocListener to restart the timer
				stopTextPadAutoSaveTimer(t);
				
				updateTabTitle(t);//textAreas.indexOf(t));
				getPrefs().storeFileHist(path);
				autoAutoIndent(t); // prevents undos from before the save
				return true;
			}
		} catch (IOException e) {
			//	    e.printStackTrace();
			return false;
		} finally { // release system resources from stream
			if (out != null)
				out.close();
		}
		return false;
	}

	/**
	 * Saves text from the currently selected <code>TextPad</code> to a given
	 * path.
	 * 
	 * @param path
	 *            file path in which to save
	 * @return true if the file saved successfully
	 * @see #saveFile(String, TextPad)
	 * @see #saveFile(TextPad)
	 */
	public boolean saveFile(String path) {
		return saveFile(path, null);
	}

	/**
	 * Saves text from the given <code>TextPad</code> to a given path.
	 * 
	 * @param pad
	 *            the pad whose conents will be saved
	 * @return true if the file saved successfully
	 * @see #saveFile(String, TextPad)
	 * @see #saveFile(String)
	 */
	public boolean saveFile(TextPad pad) {
		return saveFile(pad.getPath(), pad);
	}

	/**
	 * Saves the file to the given path. Similar to
	 * <code>saveFile(String)</code>, but tailored for the program exit by
	 * ignoring updates to the graphical user interface.
	 * 
	 * @param path
	 *            the path to the modified file
	 * @return true if the file saves successfully
	 * @see #saveFile(String)
	 */
	public static boolean saveFileOnExit(String path) {
		//	System.out.println("printing");
		TextPad t = getSelectedTextPad();
		PrintWriter out = null;
		try {
			if (t != null) {
				File f = new File(path);
				/*
				 * if don't use canWrite(), work instead by catching exception
				 * and either handling it there or returning signal of the
				 * failure
				 */
				// open the stream to write to
				out = new PrintWriter(new FileWriter(path), true);
				// write to it
				out.print(t.getText());
				//updateFileHist(fileMenu, path);
				return true;
			}
		} catch (IOException e) {
			//	    e.printStackTrace();
			return false;
		} finally { // release system resources from stream
			if (out != null)
				out.close();
		}
		return false;
	}

	/**
	 * Opens a file into a text pad. Calls the file open dialog. Opens the file
	 * into a new pad unless the currently selected one is empty. Sets the
	 * file's name as a the tab's title and the path as the tab's tool tip.
	 * Assumes that the file is readable as text.
	 * 
	 * @param file
	 *            file to open
	 * @param editable
	 *            <code>true</code> if the resulting text pad should be
	 *            editable
	 * @param resource
	 *            <code>true</code> if the file should be accessed as a
	 *            resource, via
	 *            <code>TextTrix.class.getResourceAsStream(path)</code>
	 * @param reuseTab <code>true</code> if a tab should be reused, even
	 * if it is isn't empty or is empty but with unsaved changes
	 * @see #openFile(File)
	 * @return true if the file is successfully opened or already open
	 */
	public boolean openFile(File file, boolean editable, boolean resource, boolean reuseTab) {
		String path = file.getPath();
		
		// Checks for open file before creating new tab
		if (!path.equals("") && getSelectedTabbedPane().getTabCount() > 0 && !reuseTab) {
			int idPath = getIdenticalTextPadIndex(path);
//			System.out.println("idPath: " + idPath);
			if (idPath != -1) {
				getSelectedTabbedPane().setSelectedIndex(idPath);
//				openFile(file, editable, resource, true);
				return true;
			}
		}
		// ensures that the file exists and is not a directory
		if (file.canRead() || resource) { // readable file
			TextPad t = getSelectedTextPad();
			BufferedReader reader = null;
			try {
				if (resource) {
					InputStream in = TextTrix.class.getResourceAsStream(path);
					if (in != null) {
						reader = new BufferedReader(new InputStreamReader(in));
					} else {
						return false;
					}
				} else {
					// assumes can read file b/c of earler if condition
					reader = new BufferedReader(new FileReader(path));
				}

				// check if tabs exist; get TextPad if true
				/*
				 * t.getText() != null, even if have typed nothing in it. Add
				 * tab and set its text if no tabs exist or if current tab has
				 * tokens; set current tab's text otherwise.
				 * Defaults to creating a new tab, unless a tab already exists and 
				 * tab reuse is forced or the tab is empty or has no unsaved changes.
				 */
				if (t != null && (reuseTab || (t.isEmpty() && !t.getChanged()))) {
					read(t, reader, path);
				} else {
					addTextArea(getSelectedTabbedPane(), file);
					t = getSelectedTextPad();
					read(t, reader, path);
				}
				/*
				if (t == null || (!reuseTab && (!t.isEmpty() || t.getChanged()))) { // open file in new pad
					addTextArea(getSelectedTabbedPane(), file);
					t = getSelectedTextPad();
					read(t, reader, path);
				} else { // open file in current, empty pad
					read(t, reader, path);
				}
				*/
				t.setEditable(editable);
				t.setCaretPosition(0);
				t.setChanged(false);
				t.setFile(path);
				getSelectedTabbedPane().setToolTipTextAt(getSelectedTabbedPane().getSelectedIndex(), t
						.getPath());
				updateTabTitle(t);//getSelectedTabbedPane());
				updateTitle(t.getFilename());
				// set the path to the last opened directory
				setOpenDir(file.getParent());
				// file.getParent() returns null when opening file
				// from the command-line and passing in a relative path
				if (getOpenDir() == null) {
					setOpenDir(System.getProperty("user.dir"));
				}
				getPrefs().storeFileHist(path);
				//updateFileHist(fileMenu);
				autoAutoIndent(t);
				return true;
			} catch (IOException exception) {
				//		exception.printStackTrace();
				JOptionPane.showMessageDialog(getThis(),
						"Hm, I can't seem to find\n" + file.getPath()
								+ "\n...really sorry 'bout that.",
						"Sorry, but I couldn't find that",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			} finally {
				try {
					if (reader != null)
						reader.close();
				} catch (IOException e) {
					//    e.printStackTrace();
					return false;
				}
			}
		}
		return false;
	}
	
	/** Finds the tab with the given path.
	 * @param path the path of the file to find
	 * @return the index of the tab with the file of the given tab; -1 if no such tab exists
	*/
	public int getIdenticalTextPadIndex(String path) {
		int len = getSelectedTabbedPane().getTabCount(); // the number of tabs
		// checks each tab to see if any have the given path
		for (int i = 0; i < len; i++) {
			if (((TextPad) getTextPadAt(i)).getPath().equals(path)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Opens a file into a text pad. Calls the file open dialog. Opens the file
	 * into a new pad unless the currently selected one is empty. Sets the
	 * file's name as a the tab's title and the path as the tab's tool tip.
	 * Assumes that the file is readable as text and should be accessed
	 * directly, rather than as as a resource. Also assumed that the resulting
	 * text pad should be editable and that the currently selected tab should not 
	 * be force reused.  Front-end for <code>openFile(File, boolean, boolean, 
	 * boolean)</code>, with the arguments, 
	 * <code>openFile(File, true, false, false)</code>.
	 * 
	 * @param file
	 *            file to open
	 * @see #openFile(File, boolean, boolean)
	 */
	public boolean openFile(File file) {
		return openFile(file, true, false, false);
	}
	
	/**Opens a file as editable and not from a resource.
	 * Simply acts as a front end to 
	 * <code>openFile(File, boolean, boolean, boolean)</code>,
	 * with <code>resueTab</code> set to false.
	 * @param file
	 *            file to open
	 * @param editable
	 *            <code>true</code> if the resulting text pad should be
	 *            editable
	 * @param resource
	 *            <code>true</code> if the file should be accessed as a
	 *            resource, via
	 *            <code>TextTrix.class.getResourceAsStream(path)</code>
	 * @see #openFile(File, boolean, boolean, boolean)
	*/
	public boolean openFile(File file, boolean editable, boolean resource) {
//		return openFile(file, true, false, false);
		return openFile(file, editable, resource, false);
	}
	
	/**Refreshes a tab without the user having to close and reopen it.
	 * Useful when an open file is externally changed.
	*/
	public void refreshTab() {
		// refreshes the currently selected TextPad
		TextPad t = getSelectedTextPad();
		if (t != null) {
			// Ensure that has a saved file to refresh
			if (!t.fileExists()) {
				String title = "Refreshing ain't always easy";
				String msg = "This is all we've got.  There's no saved file yet"
					+ "\nfor us to refresh.  Sorry about that.";
				JOptionPane.showMessageDialog(getThis(), msg, title,
					JOptionPane.INFORMATION_MESSAGE, null);
				return;
			}
			
			// Confirms with user that willing to override any unsaved changes
			if (t.getChanged()) {
				
				String s = "Refresh request";
				// dialog with 2 choices: discard, cancel
				String msg = "This file has not yet been saved."
						+ "\nShould I still refresh it with the currently saved version?";
				int choice = JOptionPane.showOptionDialog(getThis(), msg,
						"Save before refreshing", JOptionPane.WARNING_MESSAGE,
						JOptionPane.DEFAULT_OPTION, null, new String[] { 
								"Refresh me now", "Cancel" }, "Cancel"
						);
				switch (choice) {
				// preserve the text area's contents by default
				case 0:
					break;
				default:
					return;
				}
			}
			
			// Refreshes the tab and tries to restore the caret position
			// to its original position
			int pos = t.getCaretPosition();
			openFile(t.getFile(), t.isEditable(), false, true);
			// prevent caret from exceeding length of newly refreshed file
			if (pos <= t.getDocument().getLength()) {
				t.setCaretPosition(pos);
			} else {
				t.setCaretPosition(t.getDocument().getLength());
			}
		}
	}

	/**
	 * Automatically auto-indents the given Text Pad. Determines whether the
	 * Text Pad's filename extension matches the user-defined list of files to
	 * automatically auto-indent.
	 * 
	 * @param t
	 *            Text Pad whose file is to be checked
	 */
	public void autoAutoIndent(TextPad t) {
		String path = t.getPath();
		// if tab set to auto-indent, or file fits auto-indent extension in prefs
		// and prefs set to auto-indent, then auto-indent
		if (t.isAutoIndent() || getPrefs().getAutoIndent() && isAutoIndentExt(path)) {
			t.setAutoIndent(true);
		}
	}

	/**
	 * Checks if the given file extension is in the user-defined list of files
	 * to automatically auto-indent.
	 * 
	 * @param path
	 *            file to check
	 * @return <code>true</code> if the file's extension is in the list
	 * @see #autoAutoIndent(TextPad)
	 */
	public boolean isAutoIndentExt(String path) {
		// get the file extension index
		int extIndex = LibTTx.reverseIndexOf(path, ".", path.length()) + 1;
		// stop searching if no extension
		if (extIndex < 0 || extIndex >= path.length())
			return false;
		// get the extension str
		String ext = path.substring(extIndex);
		// get the list of extensions to check
		StringTokenizer tokenizer = new StringTokenizer(getPrefs()
				.getAutoIndentExt(), " ,.");
		String token = "";
		// compare the extension with the list of extensions;
		// return true once find
		while (tokenizer.hasMoreTokens()) {
			if (tokenizer.nextElement().equals(ext))
				return true;
		}
		return false;
	}

	/**
	 * Evokes a save dialog to save a file just before exiting the program, when
	 * the text pad will no longer exist.
	 * 
	 * @param owner
	 *            parent frame; can be null
	 * @return true if the approve button is chosen, false if otherwise
	 */
	public static boolean fileSaveDialogOnExit(JFrame owner) {
		if (!prepFileSaveDialog())
			return false;
		return getSavePathOnExit(owner);
	}

	/**
	 * Evokes a save dialog. Sets the tabbed pane tab to the saved file name.
	 * Assumes that the text pad from which to save is the currently
	 * selected text pad.
	 * 
	 * @param owner
	 *            parent frame; can be null
	 * @return true if the approve button is chosen, false if otherwise
	 */
	public boolean fileSaveDialog(JFrame owner) {
		return fileSaveDialog(getSelectedTextPad(), owner);
	}

	/**
	 * Evokes a save dialog to save the given pad's file. Sets the tabbed pane
	 * tab to the saved file name.
	 * 
	 * @param pad
	 *            the text pad with the file to save
	 * @param owner
	 *            parent frame; can be null
	 * @return true if the approve button is chosen, false if otherwise
	 */
	public boolean fileSaveDialog(TextPad pad, JFrame owner) {
		if (chooser.isShowing() || !prepFileSaveDialog(pad))
			return false;
		return getSavePath(pad, owner);
	}

	/**
	 * Prepares the file save dialog. Finds the current directory if the file
	 * and selects it, if the file has already been saved. If not, returns to
	 * the most recent directory in the current session and selects no file.
	 * 
	 * @param t
	 *            the pad from which to gather the path defaults; if
	 *            <code>null</code> the pad defaults to the currently selected
	 *            pad
	 * @return <code>true</code> if a Text Pad is selected, necessary to save
	 *         a file
	 * @see #prepFileSaveDialog()
	 */
	public static boolean prepFileSaveDialog(TextPad t) {
		//	int tabIndex = getSelectedTabbedPane().getSelectedIndex();
		if (t == null)
			t = getSelectedTextPad();
		//	if (tabIndex != -1) {
		if (t != null) {
			//	    TextPad t = (TextPad)textAreas.get(tabIndex);
			if (t.fileExists()) {
				//chooser.setCurrentDirectory(new File(t.getDir()));
				// save to file's current location
				chooser.setSelectedFile(new File(t.getPath()));
			} else {
				// if the file hasn't been created, default to the directory
				// last saved to
				chooser.setCurrentDirectory(new File(saveDir));
				chooser.setSelectedFile(new File(""));
			}
			// can't save to multiple files;
			// if set to true, probably have to use double-quotes
			// when typing names
			chooser.setMultiSelectionEnabled(false);
			return true;
		}
		return false;
	}

	/**
	 * Prepares the file save dialog for the currently selected
	 * <code>TextPad</code>
	 * 
	 * @return <code>true</code> if a Text Pad is selected, necessary to save
	 *         a file
	 * @see #prepFileSaveDialog(TextPad)
	 */
	public static boolean prepFileSaveDialog() {
		return prepFileSaveDialog(null);
	}

	/**
	 * Helper function to <code>fileSaveDialog</code> when exiting Text Trix.
	 * Unlike <code>getSavePath(JFrame)</code>, this method does not attempt
	 * to update the graphical components, currently in the process of closing.
	 * Opens the file save dialog to retrieve the file's new name. If the file
	 * will overwrite another file, prompts the user with a dialog box to
	 * determine whether to continue with the overwrite, get another name, or
	 * cancel the whole operation.
	 * 
	 * @param owner
	 *            the frame to which the dialog will serve; can be null
	 * @return true if the file is saved successfully
	 * @see #getSavePath(TextPad, JFrame)
	 */
	public static boolean getSavePathOnExit(JFrame owner) {
		boolean repeat = false;
		File f = null;
		// repeat the retrieval until gets an unused file name,
		// overwrites a used one, or the user cancels the save
		do {
			// display the file save dialog
			int result = chooser.showSaveDialog(owner);
			if (result == JFileChooser.APPROVE_OPTION) {
				// save button chosen
				String path = chooser.getSelectedFile().getPath();
				f = new File(path);
				int choice = 0;
				// check whether a file by the chosen name already exists
				if (f.exists()) {
					String overwrite = path
							+ "\nalready exists.  Should I overwrite it?";
					String[] options = { "But of course", "Please, no!",
							"Cancel" };
					// dialog warning of a possible overwrite
					choice = JOptionPane.showOptionDialog(owner, overwrite,
							"Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options,
							options[1]);
				}
				if (choice == 1) {
					// don't overwrite, but choose another name
					repeat = true;
				} else if (choice == 2) { // don't overwrite.
					return false;
				} else { // write, even if overwriting
					// try to save the file and check if successful
					if (saveFileOnExit(path)) { // success
						setSaveDir(chooser.getSelectedFile().getParent());
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

	/**
	 * Opens the file save dialog to retrieve the new name of the given
	 * <code>TextPad</code>. If the file will overwrite another file, prompts
	 * the user with a dialog box to determine whether to continue with the
	 * overwrite, get another name, or cancel the whole operation. Unlike
	 * <code>getSavePathOnExit(JFrame)</code>, this method attempts to update
	 * the graphical components.
	 * 
	 * @param pad
	 *            the <code>TextPad</code> whose file will be saved
	 * @param owner
	 *            the frame to which the dialog will serve; can be null
	 * @return true if the file is saved successfully
	 * @see #getSavePathOnExit(JFrame)
	 */
	public boolean getSavePath(TextPad pad, JFrame owner) {
		boolean repeat = false;
		File f = null;
		// repeat the retrieval until gets an unused file name,
		// overwrites a used one, or the user cancels the save
		do {
			// display the file save dialog
			int result = chooser.showSaveDialog(owner);
			if (result == JFileChooser.APPROVE_OPTION) {
				// save button chosen
				String path = chooser.getSelectedFile().getPath();
				f = new File(path);
				int choice = 0;
				// check whether a file by the chosen name already exists
				if (f.exists()) {
					String overwrite = path
							+ "\nalready exists.  Should I overwrite it?";
					String[] options = { "But of course", "Please, no!",
							"Cancel" };
					// dialog warning of a possible overwrite
					choice = JOptionPane.showOptionDialog(owner, overwrite,
							"Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options,
							options[1]);
				}
				if (choice == 1) {
					// don't overwrite, but choose another name
					repeat = true;
				} else if (choice == 2) { // don't overwrite.
					return false;
				} else { // write, even if overwriting
					// try to save the file and check if successful
					boolean success = false;
					success = saveFile(path, pad);
					
					/*
					int idx = tabIndex;//-1;
					if (pad == null) {
						success = saveFile(path, tabIndex);
						idx = getSelectedTabbedPane().getSelectedIndex();
					} else {
						success = saveFile(path, pad, tabIndex);
//						idx = textAreas.indexOf(pad);
					}
					*/
					if (success) {
						setSaveDir(chooser.getSelectedFile().getParent());
						// update graphical components
						MotherTabbedPane pane = getTabbedPane(pad);
						pane.setToolTipTextAt(getTextPadIndex(pane, pad), path);
						updateTitle(owner, f.getName());
						/*
						if (idx == getSelectedTabbedPane().getSelectedIndex()) {
							updateTitle(owner, f.getName());
						}
						*/
						getPrefs().storeFileHist(path);
						fileHist.start(fileMenu);

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

	/**
	 * Front-end, helper function to ask yes/no questions.
	 * 
	 * @param owner
	 *            parent frame; can be null
	 * @param msg
	 *            message to display in the main window section
	 * @param title
	 *            title to display in the title bar
	 * @return true for "Yes", false for "No"
	 */
	private static boolean yesNoDialog(JFrame owner, String msg, String title) {
		int choice = JOptionPane.showConfirmDialog(owner, msg, title,
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		// true for Yes, false for No
		if (choice == JOptionPane.YES_OPTION) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Prints the currently selected <code>TextPad</code>. A printer dialog
	 * pops up for the user to select printing options, such as page size and
	 * layout. The output travels to an active printer. The printer formats text
	 * exactly as the <code>TextPad</code> displays it, including new lines
	 * where text wraps within the pad.
	 */
	public void printTextPad() {
		try {
			// begins the print job and creats a book of pages to print
			PrinterJob job = PrinterJob.getPrinterJob();
			Book bk = createBook();
			if (bk == null)
				return;
			job.setPageable(bk);
			// stores the print attributes from a print dialog
			if (job.printDialog(printAttributes)) {
				job.print(printAttributes);
			}
		} catch (PrinterException e) {
			// printer error message
			JOptionPane.showMessageDialog(this, e);
		}
	}

	/**
	 * Creats a book of multiple pages for a print job. Returns
	 * <code>null</code> if no <code>TextPad</code> exists.
	 */
	public Book createBook() {
		TextPad textPad = getSelectedTextPad();
		if (textPad == null)
			return null;
		/*
		 * System.out.println("font fam: " + textPad.getFont().getFamily() + ",
		 * font name: " + textPad.getFont().getName() + ", font size: " +
		 * textPad.getFont().getSize());
		 */
		if (pageFormat == null) {
			PrinterJob job = PrinterJob.getPrinterJob();
			pageFormat = job.defaultPage();
		}
		// consists of text formatted to mimic the TextPad's visible
		// text layout
		PrintPad pad = textPad.createPrintPad();
		Book bk = new Book();
		int pp = pad.getPageCount((Graphics2D) getGraphics(), pageFormat);
		bk.append(pad, pageFormat, pp);
		return bk;
	}

	/**
	 * Displays a dialog for the user to select print job settings. Settings
	 * include paper size and orientation.
	 *  
	 */
	public void printTextPadSettings() {
		PrinterJob job = PrinterJob.getPrinterJob();
		job.pageDialog(printAttributes);
	}

	/**
	 * Displays a preview of what would be printed, given the current page
	 * format. The preview window includes a button for the user to issue a
	 * print command directly from the window. The window resizes the preview
	 * canvas when the window itself is resized.
	 *  
	 */
	public void printPreview() {
		Book bk = createBook();
		if (bk == null)
			return;
		// print action to issue a print command from the window
		Action printAction = new AbstractAction("Print...", null) {
			public void actionPerformed(ActionEvent e) {
				printTextPad();
			}
		};
		LibTTx.setAcceleratedAction(printAction, "Print...", 'I', KeyStroke
				.getKeyStroke("alt I"));
		PrintPadPreview preview = new PrintPadPreview(this, bk, printAction);
		preview.setVisible(true);
	}

	/**
	 * Starts the auto-save timer for a given <code>TextPad</code>. If the
	 * timer object does not exist, it is created; if it already exists, it is
	 * restarted. Auto-save allows the program to save the file after a given
	 * time interval rather than only after the user issues a save command. The
	 * timer follows the current preferences timer interval. If the user saves
	 * the document before the timer has expired, the timer stops because
	 * <code>getSavePath(String, TextPad)</code> calls
	 * <code>stopTextPadAutoSaveTimer(TextPad)</code>, which interrupts and
	 * destroys the timer object.
	 * 
	 * @param pad
	 *            the pad containing the document to save automatically
	 * @see #stopTextPadAutoSaveTimer(TextPad)
	 * @see #getSavePath(TextPad, JFrame)
	 */
	public void startTextPadAutoSaveTimer(TextPad pad) {
		// retrieves stored timer in given TextPad
		StoppableThread timer = pad.getAutoSaveTimer();
		// creates a new timer if it doesn't exist, the case when auto-save
		// hasn't started, or stopTextPadAutoSaveTimer has stopped it;
		// if try to restart, get ThreadStateException for some reason

		if (timer == null) {
			pad.setAutoSaveTimer(timer = new TextPadAutoSaveTimer(pad));
			timer.start();
		} else if (timer.isStopped()) {
			timer.start();
		}

	}

	/**
	 * Stops the auto-save timer by calling its interrupt method and destroying
	 * the object.
	 * 
	 * @param pad
	 *            the pad with the auto-save timer to stop
	 * @see #startTextPadAutoSaveTimer(TextPad)
	 */
	public static void stopTextPadAutoSaveTimer(TextPad pad) {
		StoppableThread timer = pad.getAutoSaveTimer();
		if (timer != null) {
			//timer.interrupt();
			// destroys object to ensure that the startTextPadAutoSaveTimer
			// creates a new object
			//pad.setAutoSaveTimer(null);
			timer.requestStop();
		}
	}

	/**
	 * Gets this <code>TextTrix</code> object. Useful for private classes that
	 * need to access this object as the owner of a dialog.
	 * 
	 * @return the main program
	 */
	private JFrame getThis() {
		return this;
	}

	/**
	 * Gets the number of the current newline in the given pad. Word-wrapped
	 * lines are not counted, but only lines with hard breaks.
	 * 
	 * @param pad
	 *            the pad
	 * @return the line number, relative to 1 as the first line of the document
	 */
	public int getLineNumber(TextPad pad) {
		//		TextPad pad = getSelectedTextPad();
		//		Element root = pad.getDocument().getDefaultRootElement();
		int offset = pad.getCaretPosition();
		return pad.getDocument().getDefaultRootElement()
				.getElementIndex(offset) + 1;
	}

	/**
	 * Gets the number of newlines in the given pad. Word-wrapped lines are not
	 * counted, but only lines with hard breaks.
	 * 
	 * @param pad
	 *            the pad
	 * @return the number of lines with hard breaks
	 */
	public int getTotalLineNumber(TextPad pad) {
		return pad.getDocument().getDefaultRootElement().getElementCount();
	}
	
	/**Gets the index position within the document, given the line number.
	 * @param pad the text pad to search for the given line number
	 * @param line the number of the line, starting at 1; add 1 to the
	 * document element number
	 * @return a <code>Point</code> object whose X value corresponds
	 * to the start of the line and whose Y value corresponds to the end,
	 * the index of the last character - 1, dropping the last char to avoid
	 * including a newline char or exceeding the document length;
	 * both values relative to the start
	 * of the document
	*/
	public Point getPositionFromLineNumber(TextPad pad, int line) {
		// adjusts the line number from the user-friendly, 1 to n+1 numbering
		// system, to the standard 0 to n system
		line--;
		Element elt = pad.getDocument().getDefaultRootElement();
		int len = pad.getDocument().getLength();
		// returns x=0, y=0 if the line number precedes the doc
		if (line < 0) {
			return new Point(0, 0);
		} else if (line >= elt.getElementCount()) {
			// returns x=len,y=len if the line num exceeds the doc
			int i = len;
			return new Point(i, i);
		}
		// otherwise, returns the boundary indices of the line
		Element lineElt = elt.getElement(line);
		int end = lineElt.getEndOffset() - 1;
//		if (end >= len) end--;
		return new Point(lineElt.getStartOffset(), end);
	}

	/**
	 * Updates the status bar with the latest line number information.
	 * 
	 * @param pad
	 *            the pad
	 */
	public void updateStatusBarLineNumbers(TextPad pad) {
		// TODO: make one component of a larger status-bar update operation
		int lineNum = getLineNumber(pad);
		int totLines = getTotalLineNumber(pad);
		// cast to float for float division rather than int division
		int percentage = (int) ((float) lineNum / (float) totLines * 100);
		statusBar.setText(lineNum + ", " + totLines + " " + "(" + percentage
				+ "%)");
	}

	/**
	 * Displays a message dialog to inform the user that a resource, such as a
	 * documentation or icon file, is missing. Points the user to the Text Trix
	 * website to find the resource and notify its maintainers to include the
	 * file in the next release.
	 * 
	 * @param path
	 *            the path to the missing resource
	 */
	private void displayMissingResourceDialog(String path) {
		JOptionPane.showMessageDialog(getThis(), "Hm, I can't seem to find \""
				+ path + "\""
				+ "\n...really sorry 'bout that.  You might find it at"
				+ "\nhttp://textflex.com/texttrix, and in the meantime,"
				+ "\nwe invite you to drop us a line so that we can "
				+ "\ninclude the file in the next release.  Thanks!",
				"Really sorry, but I couldn't find that",
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Evokes a open file dialog, from which the user can select a file to
	 * display in the currently selected tab's text area. Filters for text
	 * files, though provides option to display all files.
	 */
	private class FileOpenAction extends AbstractAction {
		JFrame owner;

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
		public FileOpenAction(JFrame aOwner, String name, Icon icon) {
			owner = aOwner;
			putValue(Action.NAME, name);
			putValue(Action.SMALL_ICON, icon);
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
			TextPad t = getSelectedTextPad();
			// File("") evidently brings file dialog to last path,
			// whether last saved or opened path

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
					 */
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
					 */
				} else { // Cancel button
					repeat = false;
				}
			} while (repeat);
			// repeat if failed opens for user to retry
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
		 * @param aOwner
		 *            the parent frame
		 * @param name
		 *            the action's name
		 * @param icon
		 *            the action's icon
		 */
		public FileSaveAction(JFrame aOwner, String name, Icon icon) {
			owner = aOwner;
			putValue(Action.NAME, name);
			putValue(Action.SMALL_ICON, icon);
		}

		/**
		 * Displays a file save chooser when the action is invoked.
		 * 
		 * @param evt
		 *            action invocation
		 * @see #fileSaveDialog(JFrame)
		 */
		public void actionPerformed(ActionEvent evt) {
			fileSaveDialog(owner);
		}
	}

	/**
	 * Closes files and removes them from the tab history.
	 *  
	 */
	private class FileCloseAction extends AbstractAction {

		/**
		 * Constructs the file close action.
		 * 
		 * @param name
		 *            name of the action
		 * @param icon
		 *            graphics for the action
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
			MotherTabbedPane pane = getSelectedTabbedPane();
			int i = pane.getSelectedIndex();
			if (i >= 0) {
				updateTabIndexHistory = false;
				pane.removeTabIndexHistory(i);
				updateTabIndexHistory = true;
				closeTextArea(i, pane);
			}
		}
	}

	/**
	 * Responds to changes in the <code>TextPad</code> text areas. Updates the
	 * titles to reflect text alterations.
	 */
	private class TextPadDocListener implements DocumentListener {

		/**
		 * Flags a text insertion.
		 * 
		 * @param e
		 *            insertion event
		 */
		public void insertUpdate(DocumentEvent e) {
			setChanged();
		}

		/**
		 * Flags a text removal.
		 * 
		 * @param e
		 *            removal event
		 */
		public void removeUpdate(DocumentEvent e) {
			setChanged();
		}

		/**
		 * Flags any sort of text change.
		 * 
		 * @param e
		 *            any text change event
		 */
		public void changedUpdate(DocumentEvent e) {
		}

		/**
		 * Updates the pad's tab and starts the auto-save timer if the pad's
		 * contents have changed for the first time since the last save. The
		 * timer will only start if the auto-save preference has been selected.
		 *  
		 */
		public void setChanged() {
			final TextPad pad = getSelectedTextPad();
			if (!pad.getChanged()) {
				pad.setChanged(true);
				updateTabTitle(pad);//getSelectedTabbedPane());
				if (getPrefs().getAutoSave()) {
					//					System.out.println("i'm here");
					startTextPadAutoSaveTimer(pad);
				}
			}
		}

	}
	
	/**
	 * A timer to automatically save the <code>TextPad</code>'s contents. The
	 * timer checks the preferences to determine the interval between saves and
	 * to see whether the user would like a prompt before the timer saves the
	 * file automatically.
	 */
	private class TextPadAutoSaveTimer extends StoppableThread {

		//		private boolean stopped = false;
		private TextPad textPad = null;

		private boolean chooserShowing = false;

		private Thread thread = null;

		/**
		 * Creates a timer to work on the given <code>TextPad</code>. The pad
		 * will in turn store the timer.
		 * 
		 * @param aTextPad
		 */
		public TextPadAutoSaveTimer(TextPad aTextPad) {
			textPad = aTextPad;
		}

		/**
		 * Starts the auto-save timer.
		 *  
		 */
		public void start() {

			setStopped(false);
			thread = new Thread(this, "thread");
			thread.start();

		}

		/**
		 * Saves the file after the time interval that the preferences specify.
		 * Once the information dialogs have displayed, canceling the chooser,
		 * whether opened manually or by the auto-save, disables the auto-save
		 * function until the next save.
		 */
		public void run() {
			interrupted(); // clears any interrupt during a previous run
			try {
				sleep(getPrefs().getAutoSaveInterval() * 60000);
				// don't need getPrefs().getAutoSave() && b/c only start
				// timer if auto-save pref set, and interrupt already called
				// if unset while timer running
				if (!interrupted()) {
					// to avoid breaking the single thread rule, invokeLater
					// runs all of the UI code to ensure that it synchronizes
					// with events in the main dispatch thread
					EventQueue.invokeLater(new Runnable() {
						public void run() {

							// prompt if the preference selected;
							// skip if file doesn't exist b/c will ask for file
							// name later,
							// when can still cancel the save
							if (getPrefs().getAutoSavePrompt()
									&& textPad.fileExists()) {
								// creates a save prompt dialog
								int choice = JOptionPane
										.showConfirmDialog(
												getThis(),
												"We're about to auto-save this baby"
														+ " ("
														+ textPad.getFilename()
														+ ")."
														+ "\nYou OK with that?"
														+ "\n(\"No\" means we won't ask again "
														+ "about this file.)",
												"Auto-Save Prompt",
												JOptionPane.YES_NO_OPTION,
												JOptionPane.QUESTION_MESSAGE);
								if (choice == JOptionPane.NO_OPTION) {
									return;
								}
							}
							
//							int tabIndex = getTextPadIndex(textPad);
							// saves the pad directly if it already exists;
							// otherwise, asks for a file path
							if (textPad.fileExists()) {
								saveFile(textPad);
							} else {
								// asks users whether they would like to supply
								// a file
								// path rather than diving immediately and
								// cryptically
								// into the file save dialog
								int choice = JOptionPane
										.showConfirmDialog(
												getThis(),
												"We're about to auto-save this baby"
														+ " ("
														+ textPad.getFilename()
														+ "), "
														+ "\nbut we need a name for it.  "
														+ "Mind if we got that from you?"
														+ "\n(\"No\" means we won't ask again "
														+ "about this file.)",
												"Auto-Save Prompt",
												JOptionPane.YES_NO_OPTION,
												JOptionPane.QUESTION_MESSAGE);
								// exit immediately if users cancel the save
								if (choice == JOptionPane.NO_OPTION) {
									return;
								}
								// solicits users for a file name;
								// main prgm as dialog owner
								fileSaveDialog(textPad, getThis());
							}
						}
					});
				}
				setStopped(true); // thread stops after clean-up fns
			} catch (InterruptedException e) {
				// ensures that an interrupt during the sleep is still flagged
				setStopped(true);
				Thread.currentThread().interrupt();
			}
		}

		/**
		 * Requests the thread to stop by setting the <code>stopped</code>
		 * flag, interrupting the running thread, and setting the current thread
		 * to <code>null</code>. Note that the <code>StoppableThread</code>
		 * object remains, only the <ocde>Thread</code> object created at the
		 * starting of the <code>StoppableThread</code> is destroyed
		 *  
		 */
		public void requestStop() {
			setStopped(true);
			if (thread != null) {
				thread.interrupt();
				thread = null;
			}
		}
	}

	/**
	 * Listener to pop up a context menu when right-clicking.
	 * 
	 * @author davit
	 */
	private class PopupListener extends MouseAdapter {
		private JPopupMenu popup = null;
		
		public PopupListener(JPopupMenu aPopup) {
			super();
			popup = aPopup;
		}
	
		/**
		 * Press right mouse button.
		 *  
		 */
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		/**
		 * Release right mouse button.
		 *  
		 */
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	/**
	 * Creates the menu bar and its associated tool bar through a worker thread
	 * to build concurrently with other processes. No other method should rely
	 * upon the components that this class creates to be available until
	 * sufficient time after the thread starts.
	 * 
	 * @author davit
	 */
	private class MenuBarCreator implements Runnable { //extends Thread {

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
					char fileMenuMnemonic = 'F';
					char newActionMnemonic = 'N';
					KeyStroke newActionShortcut = KeyStroke
							.getKeyStroke("ctrl N");
					char newGroupActionMnemonic = 'G';
					KeyStroke newGroupActionShortcut = KeyStroke
							.getKeyStroke("ctrl G");
					char closeActionMnemonic = 'C';
					KeyStroke closeActionShortcut = KeyStroke
							.getKeyStroke("ctrl W");
					char closeGroupActionMnemonic = 'R';
					KeyStroke closeGroupActionShortcut = KeyStroke
							.getKeyStroke("ctrl shift W");
					String exitActionTxt = "Exit";
					char exitActionMnemonic = 'X';
					KeyStroke exitActionShortcut = KeyStroke
							.getKeyStroke("ctrl Q");
					char cutActionMnemonic = 'T';
					KeyStroke cutActionShortcut = KeyStroke
							.getKeyStroke("ctrl X");
					KeyStroke redoActionShortcut = KeyStroke
							.getKeyStroke("ctrl Y");
					char copyActionMnemonic = 'C';
					KeyStroke copyActionShortcut = KeyStroke
							.getKeyStroke("ctrl C");
					char pasteActionMnemonic = 'P';
					KeyStroke pasteActionShortcut = KeyStroke
							.getKeyStroke("ctrl V");
					char selectAllActionMnemonic = 'A';
					KeyStroke selectAllActionShortcut = KeyStroke
							.getKeyStroke("ctrl A");
					char printActionMnemonic = 'P';
					KeyStroke printActionShortcut = KeyStroke
							.getKeyStroke("ctrl P");
					char printPreviewActionMnemonic = 'R';
					char printSettingsActionMnemonic = 'I';

					// Alternate keybindings: shortcuts added to and with
					// preference over the standard shortcuts
					// TODO: add Mac-sytle shortcuts
					if (prefs.isHybridKeybindings()) {
						// Hybrid: standard + Emacs for single char and line
						// navigation
						fileMenuMnemonic = 'I';
						newActionMnemonic = 'T';
						newActionShortcut = KeyStroke.getKeyStroke("ctrl T");
						exitActionTxt = "Exit";
						exitActionMnemonic = 'X';
						exitActionShortcut = KeyStroke.getKeyStroke("ctrl Q");
						selectAllActionMnemonic = 'L';
						selectAllActionShortcut = KeyStroke
								.getKeyStroke("ctrl L");
						printActionShortcut = KeyStroke
								.getKeyStroke("ctrl shift P");
					} else if (prefs.isEmacsKeybindings()) {
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
					if (menuBar != null) {
						contentPane.remove(menuBar);
						fileMenu = new JMenu("File");
						contentPane.remove(toolBar);
					}

					// make menu bar and menus
					menuBar = new JMenuBar();
					fileMenu.setMnemonic(fileMenuMnemonic);
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
//					toolBar.addMouseListener(new PopupListener());
					toolBar.setBorderPainted(false);

					// create pop-up menu for right-mouse-clicking
					popup = new JPopupMenu();
					tabsPopup = new JPopupMenu();
					getGroupTabbedPane().addMouseListener(new PopupListener(tabsPopup));

					/* File menu items */

					// make new tab and text area
					Action newAction = new AbstractAction("New") {
						public void actionPerformed(ActionEvent evt) {
							addTextArea(getSelectedTabbedPane(), makeNewFile());
						}
					};
					LibTTx.setAcceleratedAction(newAction, "New",
							newActionMnemonic, newActionShortcut);
					fileMenu.add(newAction);

					// make new tab group
					Action newGroupAction = new AbstractAction("New group") {
						public void actionPerformed(ActionEvent evt) {
							addTabbedPane(getGroupTabbedPane(), "");
						}
					};
					LibTTx.setAcceleratedAction(newGroupAction, "New group",
							newGroupActionMnemonic, newGroupActionShortcut);
					fileMenu.add(newGroupAction);

					// (ctrl-o) open file; use selected tab if empty
					Action openAction = new FileOpenAction(TextTrix.this,
							"Open", LibTTx
									.makeIcon("images/openicon-roll-16x16.png"));
					LibTTx.setAcceleratedAction(openAction, "Open", 'O',
							KeyStroke.getKeyStroke("ctrl O"));
					fileMenu.add(openAction);

					Action openActionForBtn = new FileOpenAction(TextTrix.this,
							"Open", LibTTx
									.makeIcon("images/openicon-16x16.png"));
					LibTTx.setAction(openActionForBtn, "Open file(s)", 'O');
					JButton openButton = toolBar.add(openActionForBtn);
					openButton.setBorderPainted(false);
					LibTTx.setRollover(openButton,
							"images/openicon-roll-16x16.png");

					// close file; check if saved
					Action closeAction = new FileCloseAction("Close", LibTTx
							.makeIcon("images/closeicon-16x16.png"));
					LibTTx.setAcceleratedAction(closeAction, "Close",
							closeActionMnemonic, closeActionShortcut);
					fileMenu.add(closeAction);

					Action closeActionForBtn = new FileCloseAction("Close",
							LibTTx.makeIcon("images/door-60deg-16x16.png"));
					LibTTx.setAction(closeActionForBtn, "Close file",
							closeActionMnemonic);
					JButton closeButton = toolBar.add(closeActionForBtn);
					closeButton.setBorderPainted(false);
					LibTTx.setRollover(closeButton,
							"images/closeicon-16x16.png");


					// close tab group
					Action closeGroupAction = new AbstractAction("Close tab group") {
						public void actionPerformed(ActionEvent evt) {
							removeTabbedPane(getGroupTabbedPane());
						}
					};
					LibTTx.setAcceleratedAction(closeGroupAction, "Close tab group",
							closeGroupActionMnemonic, closeGroupActionShortcut);
					fileMenu.add(closeGroupAction);



					// (ctrl-s) save file; no dialog if file already created
					Action saveAction = new AbstractAction("Save", LibTTx
							.makeIcon("images/saveicon-16x16.png")) {
						public void actionPerformed(ActionEvent evt) {
							TextPad t = getSelectedTextPad(); //null;
							// can't use getSelectedTabbedPane().getSelectedComponent() b/c
							// returns JScrollPane
							if (t != null) {
								if (t.fileExists()) {
									if (!saveFile(t.getPath())) {
										String msg = t.getPath()
												+ " couldn't be written.\n"
												+ "Would you like to try saving it somewhere else?";
										String title = "Couldn't write";
										if (yesNoDialog(TextTrix.this, msg,
												title))
											fileSaveDialog(t, TextTrix.this);
									}
									// otherwise, request filename for new file
								} else {
									fileSaveDialog(t, TextTrix.this);
								}
							}
						}
					};
					LibTTx.setAcceleratedAction(saveAction, "Save file", 'S',
							KeyStroke.getKeyStroke("ctrl S"));
					fileMenu.add(saveAction);
					JButton saveButton = toolBar.add(saveAction);
					saveButton.setBorderPainted(false);
					LibTTx.setRollover(saveButton,
							"images/saveicon-roll-16x16.png");

					// Tool Bar: begin plug-ins
					toolBar.addSeparator();

					// save w/ file save dialog
					Action saveAsAction = new FileSaveAction(TextTrix.this,
							"Save as...", LibTTx
									.makeIcon("images/saveasicon-16x16.png"));
					LibTTx.setAction(saveAsAction, "Save as...", '.');
					fileMenu.add(saveAsAction);

					// Menu: begin print entries
					fileMenu.addSeparator();

					Action printAction = new AbstractAction("Print...") {
						public void actionPerformed(ActionEvent e) {
							printTextPad();
						}
					};
					LibTTx.setAcceleratedAction(printAction, "Print...",
							printActionMnemonic, printActionShortcut);
					fileMenu.add(printAction);

					Action printPreviewAction = new AbstractAction(
							"Print preview...") {
						public void actionPerformed(ActionEvent e) {
							printPreview();
						}
					};
					LibTTx.setAction(printAction, "Print...",
							printPreviewActionMnemonic);
					fileMenu.add(printPreviewAction);

					Action printSettingsAction = new AbstractAction(
							"Print settings...") {
						public void actionPerformed(ActionEvent e) {
							printTextPadSettings();
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
							exitTextTrix();
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
							((TextPad) getSelectedTextPad()).undo();
						}
					};
					LibTTx.setAcceleratedAction(undoAction, "Undo", 'U',
							KeyStroke.getKeyStroke("ctrl Z"));
					editMenu.add(undoAction);

					// redo; multiple redos available
					Action redoAction = new AbstractAction("Redo") {
						public void actionPerformed(ActionEvent evt) {
							((TextPad) getSelectedTextPad()).redo();
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
							((TextPad) getSelectedTextPad()).cut();
						}
					};
					LibTTx.setAcceleratedAction(cutAction, "Cut",
							cutActionMnemonic, cutActionShortcut);
					editMenu.add(cutAction);
					popup.add(cutAction);

					// copy
					Action copyAction = new AbstractAction("Copy") {
						public void actionPerformed(ActionEvent evt) {
							((TextPad) getSelectedTextPad()).copy();
						}
					};
					LibTTx.setAcceleratedAction(copyAction, "Copy",
							copyActionMnemonic, copyActionShortcut);
					editMenu.add(copyAction);
					popup.add(copyAction);

					// paste
					Action pasteAction = new AbstractAction("Paste") {
						public void actionPerformed(ActionEvent evt) {
							TextPad t = getSelectedTextPad();
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
							((TextPad) getSelectedTextPad()).selectAll();
						}
					};
					LibTTx.setAcceleratedAction(selectAllAction, "Select all",
							selectAllActionMnemonic, selectAllActionShortcut);
					editMenu.add(selectAllAction);
					popup.add(selectAllAction);

					// edit menu preferences separator
					editMenu.addSeparator();

					// group tab title
					Action chgGrpTabTitleAction = new AbstractAction("Change group tab title...") {
						public void actionPerformed(ActionEvent evt) {
							String title = JOptionPane.showInputDialog(
								getThis(), 
								"What would you like to name the tab group?");
							if (title != null) {
								getGroupTabbedPane().setTitleAt(getGroupTabbedPane().getSelectedIndex(), title);
							}
						}
					};
					LibTTx.setAcceleratedAction(chgGrpTabTitleAction, "Change group tab title...", 'H',
							KeyStroke.getKeyStroke("ctrl shift G"));
					editMenu.add(chgGrpTabTitleAction);
					tabsPopup.add(chgGrpTabTitleAction);

					// auto-indent
					// apply the selection to the current TextPad
					Action autoIndentAction = new AbstractAction(
							"Auto-wrap-indent the selected file") {
						public void actionPerformed(ActionEvent evt) {
							TextPad t = getSelectedTextPad();
							if (t != null)
								t.setAutoIndent(autoIndent.isSelected());
						}
					};
					LibTTx
							.setAcceleratedAction(
									autoIndentAction,
									"<html>Automatically repeat tabs on the next line and "
											+ "<br>graphically wrap the indentations</html>",
									'I', KeyStroke.getKeyStroke("alt shift I"));
					autoIndent = new JCheckBoxMenuItem(autoIndentAction);
					editMenu.add(autoIndent);

					// Preferences panel starter;
					// also reloads the plug-ins
					Action prefsAction = new AbstractAction(
							"It's your preference...") {
						public void actionPerformed(ActionEvent evt) {
							refreshPlugInsPanel();
							getPrefs().setVisible(true); //show();
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
					Action backTabAction = new AbstractAction("Back") {
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
							MotherTabbedPane pane = getSelectedTabbedPane();
							pane.decrementTabIndexHistoryIndex();
							int tabIndexHistoryIndex = pane.getTabIndexHistoryIndex();
							if (tabIndexHistoryIndex >= 1) {
								// uncouple the tab index history while
								// switching
								// to the past tabs -- leave the tabs as a
								// trail until a new tab is chosesn
								updateTabIndexHistory = false;
								pane.setSelectedIndex(pane.getTabIndexHistoryAt(tabIndexHistoryIndex - 1));
							} else { // reset the index to its orig val, -1
								pane.incrementTabIndexHistoryIndex();
							}
						}
					};
					LibTTx.setAcceleratedAction(backTabAction, "Back", 'B',
							KeyStroke.getKeyStroke("ctrl shift OPEN_BRACKET"));
					viewMenu.add(backTabAction);

					// (ctrl-shift-]) switch forwared in the tab history
					Action forwardTabAction = new AbstractAction("Forward") {
						public void actionPerformed(ActionEvent evt) {
							MotherTabbedPane pane = getSelectedTabbedPane();
							int i = 0;
							// switch only through the the last recorded
							// selected tab;
							// i == -1 signifies that no tab has been recorded
							// for that index;
							// keep from updating the history with past
							// selections
							pane.incrementTabIndexHistoryIndex();
							int tabIndexHistoryIndex = pane.getTabIndexHistoryIndex();
							if (tabIndexHistoryIndex < pane.getTabIndexHistoryCount()
									&& (i = pane.getTabIndexHistoryAt(tabIndexHistoryIndex - 1)) != -1) {
								// uncouple the history, preserving it as a
								// trail of selections past and future from
								// the current position
								updateTabIndexHistory = false;
								pane.setSelectedIndex(i);
								//updateTabIndexHistory = true;
							} else {
								pane.decrementTabIndexHistoryIndex();
							}
						}
					};
					LibTTx.setAcceleratedAction(forwardTabAction, "Forward",
							'F', KeyStroke.getKeyStroke("ctrl shift "
									+ "CLOSE_BRACKET"));
					viewMenu.add(forwardTabAction);

					// (ctrl-[) switch to the preceding tab
					Action prevTabAction = new AbstractAction("Preceeding tab") {
						public void actionPerformed(ActionEvent evt) {
							int tab = getSelectedTabbedPane().getSelectedIndex();
							if (tab > 0) {
								getSelectedTabbedPane().setSelectedIndex(tab - 1);
							} else if (tab == 0) {
								getSelectedTabbedPane().setSelectedIndex(getSelectedTabbedPane()
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
							int tab = getSelectedTabbedPane().getSelectedIndex();
							if ((tab != -1)
									&& (tab == getSelectedTabbedPane().getTabCount() - 1)) {
								getSelectedTabbedPane().setSelectedIndex(0);
							} else if (tab >= 0) {
								getSelectedTabbedPane().setSelectedIndex(tab + 1);
							}
						}
					};
					LibTTx.setAcceleratedAction(nextTabAction, "Next tab", 'N',
							KeyStroke.getKeyStroke("ctrl CLOSE_BRACKET"));
					viewMenu.add(nextTabAction);

					// (ctrl-]) switch to the next tab
					Action refreshTabAction = new AbstractAction("Refresh tab") {
						public void actionPerformed(ActionEvent evt) {
							refreshTab();
						}
					};
					LibTTx.setAcceleratedAction(refreshTabAction, "Refresh tab", 'R',
							KeyStroke.getKeyStroke("F5"));
					viewMenu.add(refreshTabAction);

					viewMenu.addSeparator();

					// view as plain text
					Action togglePlainViewAction = new AbstractAction(
							"Toggle plain text view") {
						public void actionPerformed(ActionEvent evt) {
							viewPlain();
						}
					};
					LibTTx.setAction(togglePlainViewAction,
							"View as plain text", 'A');
					viewMenu.add(togglePlainViewAction);

					// view as HTML formatted text
					Action toggleHTMLViewAction = new AbstractAction(
							"Toggle HTML view") {
						public void actionPerformed(ActionEvent evt) {
							viewHTML();
						}
					};
					LibTTx.setAction(toggleHTMLViewAction, "View as HTML", 'H');
					viewMenu.add(toggleHTMLViewAction);

					// view as RTF formatted text
					Action toggleRTFViewAction = new AbstractAction(
							"Toggle RTF view") {
						public void actionPerformed(ActionEvent evt) {
							viewRTF();
						}
					};
					LibTTx.setAction(toggleRTFViewAction, "View as RTF", 'T');
					viewMenu.add(toggleRTFViewAction);

					/* Help menu items */

					// about Text Trix, incl copyright notice and version number
					Action aboutAction = new AbstractAction("About...", LibTTx
							.makeIcon("images/minicon-16x16.png")) {
						public void actionPerformed(ActionEvent evt) {
							String path = "about.txt";
							String text = readText(path);
							if (text == "") {
								text = "Text Trix" + "\nthe text tinker"
										+ "\nCopyright (c) 2002-4, Text Flex"
										+ "\nhttp://textflex.com/texttrix";
								displayMissingResourceDialog(path);
							}
							String iconPath = "images/texttrixsignature.png";
							JOptionPane.showMessageDialog(getThis(), text,
									"About Text Trix",
									JOptionPane.PLAIN_MESSAGE, LibTTx
											.makeIcon(iconPath));
						}
					};
					LibTTx.setAction(aboutAction, "About...", 'A');
					helpMenu.add(aboutAction);

					// shortcuts description; opens new tab;
					// reads from "shortcuts.txt" in same dir as this class
					Action shortcutsAction = new AbstractAction("Shortcuts") {
						public void actionPerformed(ActionEvent evt) {
							String path = "shortcuts.txt";
							// ArrayIndexOutOfBoundsException while opening file
							// from
							// from menu is an JVM 1.5.0-beta1 bug (#4962642)
							if (!openFile(new File(path), false, true)) {
								displayMissingResourceDialog(path);
							}
						}
					};
					LibTTx.setAction(shortcutsAction, "Shortcuts", 'S');
					helpMenu.add(shortcutsAction);

					// license; opens new tab;
					// reads from "license.txt" in same directory as this class
					Action licenseAction = new AbstractAction("License") {
						public void actionPerformed(ActionEvent evt) {
							String path = "license.txt";
							// ArrayIndexOutOfBoundsException while opening file
							// from
							// from menu is an JVM 1.5.0-beta1 bug (#4962642)
							if (!openFile(new File(path), false, true)) {
								displayMissingResourceDialog(path);
							}
						}
					};
					LibTTx.setAction(licenseAction, "License", 'L');
					helpMenu.add(licenseAction);

					/* Trix and Tools menus */

					// Load plugins; add to appropriate menu
					setupPlugIns();

					/* Place menus and other UI components */

					// must add tool bar before set menu bar lest tool bar
					// shortcuts
					// take precedence
					contentPane.add(toolBar, BorderLayout.NORTH);

					// add menu bar and menus
					setJMenuBar(menuBar);
					menuBar.add(fileMenu);
					menuBar.add(editMenu);
					menuBar.add(viewMenu);
					menuBar.add(trixMenu);
					menuBar.add(toolsMenu);
					menuBar.add(helpMenu);

					// prepare the file history menu entries
					fileHistStart = fileMenu.getItemCount();
					syncMenus();
					//System.out.println("Validating the menu bar...");
					validate();
				}
			});
		}
	}
	
	
	
	
	/**Creates the status bar in a worker thread.
	*/
	private class StatusBarCreator implements Runnable {
		
		private int lastLine = 0;

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
					
					// Prime the layout
					SpringLayout layout = new SpringLayout();
					statusBarPanel.setLayout(layout);
					
					// Make the status bar components;
					// statusBar already created so that could accept line updates
					
					// Line Find
					JLabel lineNumLbl = new JLabel("Line Find:");
					lineNumLbl.setToolTipText("GoTo the line number as it's typed");
					lineNumFld = new JTextField(5);
					// caret listener to find-as-you-type the line number into the text box
					lineNumFld.addCaretListener(new CaretListener() {
						public void caretUpdate(CaretEvent e) {
							String lineStr = lineNumFld.getText();
							int line = 0;
							// do nothing if empty box
							if (!lineStr.equals("")) {
								// otherwise, parse string, assuming key listener has
								// filtered out non-digits
								line = Integer.parseInt(lineStr);
								if (line != lastLine) {
									selectLine(line);
								}
							}
						}
					});
					// filter input to only accept digits
					lineNumFld.addKeyListener(new KeyAdapter() {
						public void keyTyped(KeyEvent evt) {
							char keyChar = evt.getKeyChar();
							if (!Character.isDigit(keyChar)) {
								evt.consume();
								if (keyChar == KeyEvent.VK_ENTER) {
									selectLine(lastLine);
								}
							}
						}
					});
					
					// Add the components
					statusBarPanel.add(statusBar);
					statusBarPanel.add(lineNumLbl);
					statusBarPanel.add(lineNumFld);
					
					// Lay out the components using the all-new (as of JVM v.1.4)
					// SpringLayout for graphical glee
					/* Figuring out the SpringLayout
					 * 
					 * All positioning indices apparently point to the right and down;
					 * negative values go in the opposite directions.  The given side of
					 * the first component in the putConstraint argument list is set relative 
					 * to the given side of the second component.  Eg for the Line Find
					 * label below, the label's East (right) border is positioned 2 points
					 * to the left (-2 to the right) of the West (left) border of the text field.
					 *
					*/
					
					// position the statusBar line indicator
					layout.putConstraint(SpringLayout.WEST, statusBar,
						5,
						SpringLayout.WEST, statusBarPanel);
					layout.putConstraint(SpringLayout.NORTH, statusBar,
						2,
						SpringLayout.NORTH, statusBarPanel);
					layout.putConstraint(SpringLayout.SOUTH, statusBarPanel,
						2,
						SpringLayout.SOUTH, statusBar);
					
					// position the Line Find label relative to the Line Find
					// text field, which is in turn relative to the right side of 
					// the panel
					layout.putConstraint(SpringLayout.NORTH, lineNumLbl,
						2,
						SpringLayout.NORTH, statusBarPanel);
					layout.putConstraint(SpringLayout.SOUTH, statusBarPanel,
						2,
						SpringLayout.SOUTH, lineNumFld);
					layout.putConstraint(SpringLayout.EAST, lineNumLbl,
						-2,
						SpringLayout.WEST, lineNumFld);
					
					// position the Lind Find text field
					layout.putConstraint(SpringLayout.EAST, lineNumFld,
						5,
						SpringLayout.EAST, statusBarPanel);
					layout.putConstraint(SpringLayout.NORTH, lineNumFld,
						0,
						SpringLayout.NORTH, statusBarPanel);
					layout.putConstraint(SpringLayout.SOUTH, statusBarPanel,
						0,
						SpringLayout.SOUTH, lineNumFld);
					
					validate();
				}
			});
		}
		
		public void selectLine(int line) {
			TextPad t = getSelectedTextPad();
			if (t != null) {
				// highlight the appropriate line
				Point p = getPositionFromLineNumber(t, line);
				textSelectionReverse(t, (int) p.getX(), 0, 
					(int) p.getY() - (int) p.getX());
				lastLine = line;
			}
		}
	}

	/**
	 * Worker thread class to update the file history entries.
	 * 
	 * @author davit
	 *  
	 */
	private class FileHist extends Thread {
		JMenu menu = null;

		/**
		 * Starts creating the entries within the given menu.
		 * 
		 * @param aMenu
		 *            menu to add file history entries
		 */
		public void start(JMenu aMenu) {
			menu = aMenu;
			(new Thread(this, "thread")).start();
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
			String[] files = getPrefs().retrieveFileHist();
			for (int i = 0; i < files.length; i++) {
				String file = files[i];
				Action fileAction = createFileHistAction(file);
				menu.add(fileAction);
			}
		}

		/**
		 * Creates the actions to add to the history menu.
		 * 
		 * @param file
		 *            file to open when invoking the action
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
					openFile(new File(file));
				}
			};
			LibTTx.setAction(act, file); // tool tip displays full file path
			return act;
		}

		/**
		 * Updates the file history menu by deleting old entries and replacing
		 * them with the current ones. Assumes that <code>fileHistStart</code>
		 * in <code>TextTrix</code> has been set.
		 *  
		 */
		public void updateFileHist() {
			for (int i = menu.getItemCount() - 1; i >= fileHistStart; i--) {
				menu.remove(i);
			}
			createFileHist();
			menu.revalidate();

			/*
			 * Attempt to delete specific menu entries rather than updating the
			 * whole history. Not yet working.
			 */
			/*
			 * String[] files = getPrefs().retrieveFileHist(); if (files.length ==
			 * 0) { fileHistCount = 0; return; } String file = files[0];
			 * System.out.println("Adding " + file + " to the menu"); JMenuItem
			 * item = null; int countDiff = fileHistCount - files.length; if
			 * (countDiff > 0) { for (int i = 0; i < countDiff; i++) {
			 * menu.remove(menu.getItemCount() - 1); } } else { int i = 0; int
			 * totItems = menu.getItemCount(); int newPos = totItems -
			 * fileHistCount; if (fileHistCount != 0) { while (++i <=
			 * fileHistCount && !((item = menu.getItem(totItems -
			 * i)).getText().equals(file))); if (i <= fileHistCount) {
			 * menu.remove(totItems - i); menu.insert(item, newPos); } else if
			 * (countDiff == 0) { menu.remove(totItems - 1);
			 * menu.insert(createFileHistAction(file), newPos); } } else {
			 * menu.insert(createFileHistAction(file), newPos); } }
			 * fileHistCount = files.length;
			 */

			// assumes that the file history entries are at the entries in the
			// menu
			//int i = 0;
			/*
			 * for (int i = menu.getItemCount() - 1; i < fileHistCount; i++) {
			 * menu.remove(menu.getItemCount() - 1); } createFileHist(menu);
			 */
		}

	}
	
	
	private class TextPadChangeListener implements ChangeListener {
	
		private MotherTabbedPane pane = null;
		
		public TextPadChangeListener(MotherTabbedPane aPane) {
			pane = aPane;
		}
		
		public void stateChanged(ChangeEvent evt) {
			final TextPad t = getSelectedTextPad();
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					if (t != null) {
						setAutoIndent();
						// update the tab index record;
						// addTabIndexHistory increments the record index;
						// add the current tab selection now to ensure that
						// all selections are recorded
						int i = pane.getSelectedIndex();
						if (updateTabIndexHistory) {
							//System.out.println("Updating tab index
							// history...");
							pane.addTabIndexHistory(i);
						} else {
							updateTabIndexHistory = true;
						}
						updateTitle(t.getFilename());
						// doesn't work when creating new tabs via
						// the keyboard accelerator;
						// only works when changing between already created
						// tabs or creating new ones via the menu item
						t.requestFocusInWindow();
						updateStatusBarLineNumbers(t);
					}
				}
			});
			// this second call is necessary for unknown reasons;
			// perhaps some events still follow the call in invokeLater
			// (above)
			if (t != null)
				t.requestFocusInWindow();
		}
	}

	
	
}

/**
 * Filters for files with specific extensions.
 */

class ExtensionFileFilter extends FileFilter {
	private String description = "";

	private ArrayList extensions = new ArrayList();

	/**
	 * Add extension to include for file display. May need to modify to check
	 * whether extension has already been added.
	 * 
	 * @param file
	 *            extension, such as <code>.txt</code>, though the period is
	 *            optional
	 */
	public void addExtension(String extension) {
		if (!extension.startsWith("."))
			extension = "." + extension;
		extensions.add(extension.toLowerCase());
	}

	/**
	 * Sets the file type description.
	 * 
	 * @param aDescription
	 *            file description, such as <code>text
	 * files</code> for
	 *            <code>.txt</code> files
	 */
	public void setDescription(String aDescription) {
		description = aDescription;
	}

	/**
	 * Gets file type's description.
	 * 
	 * @return file type description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Accept a given file to display if it has the extension currently being
	 * filtered for.
	 * 
	 * @param f
	 *            file whose extension need to check
	 * @return <code>true</code> if accepts file, <code>false</code> if
	 *         don't
	 */
	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		String name = f.getName().toLowerCase();

		for (int i = 0; i < extensions.size(); i++)
			if (name.endsWith((String) extensions.get(i)))
				return true;
		return false;
	}
}
