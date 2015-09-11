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
 * Portions created by the Initial Developer are Copyright (C) 2002-15
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

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.awt.font.TextLayout;
import java.io.*;
import javax.swing.filechooser.FileFilter;
import java.net.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.print.*;
import javax.print.attribute.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import java.lang.reflect.InvocationTargetException;

import com.inet.jortho.FileUserDictionary;
import com.inet.jortho.SpellChecker;

import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.SyntaxViewWrapped;
import jsyntaxpane.SyntaxDocument;

import com.Ostermiller.Syntax.TokenStyles;
import com.Ostermiller.Syntax.HighlightedDocument;

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
	private static final String FILE_WINDOW_SPLITTER = "{w}";
	private static final String FILE_WINDOW_SPLITTER_REGEX = "\\{w\\}";
	private static final String NEWLINE 
		= System.getProperty("line.separator"); // newlines
	private static final String GROUP_START = "Start";
	
	// command-line arguments
	private static final String ARG_FRESH = "--fresh";
	private static final String ARG_FILES = "--files";
	private static final String ARG_NO_HIGHLIGHTING = "--nohigh";
	private static final String ARG_VERBOSE = "--verbose";
	private static final String ARG_CLEAR_TABS = "--cleartabs";
	
	/* Storage variables */
	private static String openDir = ""; // most recently path opened to
	private static String saveDir = ""; // most recently path saved to
	private static int fileIndex = 0; // for giving each TextPad a unique name
	private String toolsCharsUnavailable = ""; // chars for shorcuts
	private String trixCharsUnavailable = ""; // chars for shorcuts
	private int[] tabIndexHistory = new int[10]; // records for back/forward
	private int tabIndexHistoryIndex = 0; // index of next record
	private boolean updateTabIndexHistory = true; // flag to update the record
	private boolean updateForTextPad = true; // flag to update UI and Hx for pad
	// flag to update file history menu entries
	private static boolean updateFileHist = false;
	// starting position of file history in file menu
	private static int fileHistStart = -1;
	private static boolean fresh = false; // temporarily don't reopen tabs
	private static boolean highlighting = true; // syntax highlighting flag
	private static boolean verbose = false; // verbose command-line output
	private static boolean clearTabs = false;
	
	/* General GUI components */
	private static ArrayList ttxWindows = new ArrayList();
	private MotherTabbedPane groupTabbedPane = null; // multiple tabbed panes
	private Container contentPane = getContentPane(); // main frame content pane
	private static JPopupMenu popup = null; // make popup menu
	private static JPopupMenu tabsPopup = null; // make popup menu
	private static JFileChooser chooser = null; // file dialog
	private LineDanceDialog lineDanceDialog = null;
	
	/* Menu bar controls */
	private JMenuBar menuBar = null; // menu bar
	private static JCheckBoxMenuItem autoIndent = null; // auto-wrap-indent
	private JMenu viewMenu = null; // view menu
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
	private JDialog[] plugInDiags = null; // plugin dialog windows
	private int plugInDiagsIdx = 0; // num of plugin windows
	private static PlugIn[] plugIns = null; // plugins from jar archives
	private static Action[] plugInActions = null; // plugin invokers
	
	/* Printer controls */
	// print request attributes
	private HashPrintRequestAttributeSet printAttributes = 
			new HashPrintRequestAttributeSet();
	private PageFormat pageFormat = null; // page formatting
	
	/* Status bar */
	private StatusBarCreator statusBarCreator = null; // worker thread
	private JPanel statusBarPanel = null; // the panel
	private JLabel statusBar = null; // the status label; not really a "bar"
	private JProgressBar statusProgress = null;
	private JTextField lineNumFld = new JTextField(5); // Line Find
	private JTextField wordFindFld = new JTextField(10); // Word Find
	private JPopupMenu statusBarPopup = null; // status bar popup menu
	
	
	/* Actions */
	private Action lineSaverAction = null; // Line Find saver action

	/**
	 * Constructs a new <code>TextTrix</code> frame and a 
	 * {@link TextPad} for each of the specified paths or at least one
	 * TextPad.
	 * @param paths file paths to be opened at launch
	 */
	public TextTrix(final String[] paths) {
		final String[] filteredPaths = filterArgs(paths);
	
		// create file menu in constructor rather than when defining class
		// variables b/c would otherwise uWindowListenerse bold font for this 
		// menu alone
		resetMenus(); // resets file and view menus

		// adds a window listener that responds to closure of the main
		// window by calling the exit function
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				boolean closedTabs = false;
				// check whether only one window is open
				if (ttxWindows.size() <= 1) {
					closedTabs = exitTextTrix();
				} else {
					closedTabs = closeAllTabs();
				}
				// remove the window from the window list only if all
				// tabs were successfully closed in it
				if (closedTabs) {
					setVisible(false);
					ttxWindows.remove(getThis());
				}
			}
		});
		
		
		
		
		/* Load preferences to create prefs panel */
		// The prefs must be loaded as early as possible since
		// many of the preference settings are used to setup
		// the rest of the GUI

		// create the accept action
		prefsOkayAction = new AbstractAction("Okay", null) {
			public void actionPerformed(ActionEvent evt) {
				// check whether to continue the update before proceeding
				if (continuePrefsUpdate()) {
					// store and apply the prefs before exiting them
					getPrefs().storePrefs();
					applyPrefs();
					getPrefs().dispose();
				}
			}
		};
		
		// Set the action shortcuts
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
				// check whether to continue the update before proceeding
				if (continuePrefsUpdate()) {
					// store and apply the prefs, then continue
					getPrefs().storePrefs();
					applyPrefs();
				}
			}
		};
		// Set the action shortcuts
		LibTTx.setAcceleratedAction(prefsApplyAction,
				"Apply the current tabs settings immediately", 'A', KeyStroke
						.getKeyStroke("alt A"));

		// creates the reject action, something I'm all too familiar with
		prefsCancelAction = new AbstractAction("No way", null) {
			public void actionPerformed(ActionEvent evt) {
				// Exit the prefs without saving or applying any changes
				prefs.dispose();
				prefs = null;
			}
		};
		// Set the action shortcuts
		LibTTx.setAcceleratedAction(prefsCancelAction, "Cancel", 'N', KeyStroke
				.getKeyStroke("alt C"));
				
		// Create the preferences and apply specific prefs
		getPrefs();
		
		
		/* Setup the main Text Trix window */

		setTitle("Text Trix"); // frame title

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
				//TextPad pad = getSelectedTextPad();
				Iterator it = getTextPads().iterator();
				while (it.hasNext()) {
//					System.out.println("here");
					TextPad pad = (TextPad)it.next();
					if (pad != null && pad.getWrappedView() != null) {
						JScrollPane scrollPane = pad.getScrollPane();
						scrollPane.getViewport().setViewSize(new Dimension(
								scrollPane.getWidth() - 10, scrollPane.getHeight()));
					}
					/*
					//SyntaxViewWrapped view = (SyntaxViewWrapped)pad.getWrappedView();
					if (view != null) {
						//view.setSize(getWidth(), getHeight());
						//System.out.println("reset size; getMinSpan: " + view.getMinimumSpan(View.X_AXIS));
						int viewCount = view.getViewCount();
						for (int i = 0; i < viewCount; i++) {
							View childView = view.getView(i);
							//view.childAllocation(i, new Rectangle(0, 0, -10, 10));
							//view.layoutChanged(View.X_AXIS);
							//childView.setSize(getWidth(), getHeight());
							//SimpleAttributeSet attr = childView.getAttributes();
							//StyleConstants.setLeftIndent(attr, 1 * 7);
							//view.preferenceChanged(childView, true, true);
							System.out.println("reset view " + i);
						}
						
						short n = 50;
						short rightInset = (short)(getWidth() - view.getPreferredSpan(View.X_AXIS));
						System.out.println("prefspan: " + view.getPreferredSpan(View.X_AXIS) + ", minSpan" + view.getMinimumSpan(View.X_AXIS) + ", rightInset: " + rightInset);
						//view.setInsets(n, n, n, n);
						//SimpleAttributeSet attribs = new SimpleAttributeSet();
						//StyleConstants.setLeftIndent(attribs, 1 * 7);
						//view.setParagraphInsets(attribs);
					}
					*/
				}
			}
			

			public void componentShown(ComponentEvent evt) {
			}

			public void componentHidden(ComponentEvent evt) {
			}
		});

		// set frame icon
		ImageIcon im = LibTTx.makeIcon("images/minicon-32x32.png");
		if (im != null) {
	    		setIconImage(im.getImage());
		}
		
		
		
		
		
		/* Prep jortho spell checker */
		
		// Create user dictionary in the current working directory of your application
		SpellChecker.setUserDictionaryProvider( new FileUserDictionary() );
		
		// Load the configuration from the file dictionaries.cnf and 
		// use the current locale or the first language as default 
		SpellChecker.registerDictionaries( null, "en" );
		
		
		
		
		/* Start the jsyntaxpane highlighter */
		int fontSize = getPrefs().getFontSize();
		DefaultSyntaxKit.setFontSize(fontSize);
		DefaultSyntaxKit.initKit();
		
		TokenStyles.setFontSize(fontSize);
		TokenStyles.initialize();
		
		
		

		/* Create the main Text Trix frame components */
		
		// taken out of invokeAndWait because TextTrix windows also need
		// to be launchable from another TextTrix window
		groupTabbedPane = new MotherTabbedPane(
			JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
			
		// adds a listener to update the title and status bars when
		// switching among group tabs
		groupTabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				final TextPad t = getSelectedTextPad();
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						// Ensure that a tab is selected before updating
						// the bars with its information
						if (t != null) {
							updateTitle(t.getFilename());
							updateStatusBarLineNumbers(t);
							// updates the Line Dance table only if visible;
							// otherwise table will update when the panel
							// becomes visible
							if (lineDanceDialog != null 
									&& lineDanceDialog.isVisible()) {
								lineDanceDialog.updatePadPanel();
							}
							
						}
					}
				});
			}
		});
		
		// Add the group pane to the frame
		addTabbedPane(groupTabbedPane, "");
		

		// display tool tips for up to 100s
		ToolTipManager.sharedInstance().setDismissDelay(100000);

		// set text and web file filters for open/save dialog boxes
		chooser = getChooser();
		
		// line saver
		lineSaverAction = new AbstractAction("Save current line number") {
			public void actionPerformed(ActionEvent evt) {
				lineNumFld.setText("" + getSelectedTextPad().getLineNumber());
			}
		};
		LibTTx.setAcceleratedAction(lineSaverAction, 
				"Saves this line number in Line Find", 'L',
				KeyStroke.getKeyStroke("ctrl shift L"));
		
		// invoke the worker thread to create the initial menu bar;
		(new MenuBarCreator(this)).start();
		
		// invoke worker thread to create status bar
		statusBarPanel = new JPanel(); // worker builds on the panel
		// other thread interacts with statusBar label, so need to create early
		statusBar = new JLabel();
		statusProgress = new JProgressBar();
		statusBarPopup = new JPopupMenu();
		(statusBarCreator = new StatusBarCreator(this)).start();
		
		
		

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

				// Load files specified at start from command-line
				
				// opens paths given as cmd-line args, placing them in their own 
				// tab group and preparing to create a new tab group for other 
				// startup files
				// TODO: keep track of cmd-line files so don't reopen them 
				// automatically on next startup
				boolean cmdLineFiles = filteredPaths != null 
						&& filteredPaths.length > 0;
				if (cmdLineFiles) {
					openFiles(filteredPaths, 0, true);
					// distinguishes the tab group with files given as args,
					// labled specially with the GROUP_START title, which allows
					// this tab group to be identified later and not included in
					// tab memory and reopening
					getGroupTabbedPane().setTitleAt(0, GROUP_START);
				}

				// load files left open at the close of the last session
				String reopenPaths = getPrefs().getReopenTabsList();
				if (!getFresh() && getPrefs().getReopenTabs()) {
					// gets the group tokens for the current window
					String[] windowTokens = 
							reopenPaths.split(FILE_WINDOW_SPLITTER_REGEX);
					int windowNum = ttxWindows.indexOf(getThis()) + 1;
					if (windowNum < windowTokens.length) {
						// the list consists of a comma-delimited string of
						// filenames
						String[] grpTokens = windowTokens[windowNum].split(
								FILE_GROUP_SPLITTER_REGEX);
						
						// Start at 1 b/c first token is empty group splitter
						for (int i = 1; i < grpTokens.length; i++) {
							String[] tokens = grpTokens[i].split(FILE_SPLITTER);
							// Blank group already open initally, so simply 
							// update title and start adding files, unless 
							// cmd-line files were opened
							if (i == 1 && !cmdLineFiles) {
								setSelectedTabbedPaneTitle(tokens[0]);
							} else {
								// adds a new tab group for other startup files
								// if cmd-line files already occupy a group
								addTabbedPane(getGroupTabbedPane(), tokens[0]);
							}
							openFiles(tokens, 1, true);
						}
						// "recursively" opens a new window for each remaining
						// window token
						if (windowNum < windowTokens.length - 1) {
							openTTXWindow(null);
						}
					}
				}
				
				// selects the first tab group and updates the UI for the 
				// currently selected tab
				getGroupTabbedPane().setSelectedIndex(0);
				
				
				// drag-n-drop files to open using FileDrop (public domain)
				// http://www.iharder.net/current/java/filedrop/
				// TODO: replace System.out with null to avoid debugging
				new FileDrop(null, getGroupTabbedPane(), 
						new FileDrop.Listener() {
					public void filesDropped( java.io.File[] files ) {
						for( int i = 0; i < files.length; i++ ) {
								openFile(files[i], true, false, true);
						}
					}
				});

				
				// Synchronizes the menus with the current text pad settings. 
				// Creates the file history menu entries in the File menu and 
				// flags the auto-indent check box according to the current 
				// text pad's setting.
				//syncMenus();
				Thread fileHistThread = new Thread(new FileHist(getThis()));
				fileHistThread.start();
				setAutoIndent();
			}
		});

		/*
		 * Something apparently grabs focus after he tabbed pane ChangeListener
		 * focuses on the selected TextPad. Calling focus after displaying the
		 * window seems to restore this focus at least most of the time.
		 */
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				focuser();
			}
		});
	}

	/**
	 * Publically executable starter method. Creates the (@ link TextTrix)
	 * object, displays it, an makes sure that it will still undergo its exit
	 * routine when closed manually.  Also sets up the Look & Feel for
	 * Text Trix, which is the native look in Windows systems, and the
	 * default Java Ocean look on all other platforms.
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		
		// Set the look and feel: native for Windows systems, default Java Ocean
		// for all other platforms to provide a more consistent look, since 
		// Windows and Ocean themes aren't all that different from one another
		String errorMsg = "Defaulting to the Java Ocean Look & Feel.";
		String osName = System.getProperty("os.name").toLowerCase();
		try {
			if (osName.startsWith("mac os x")) {
				System.setProperty("apple.laf.useScreenMenuBar", "true");
			}
			/* 
			// not yet defaulting to Nimbus as the group tabbed pane doesn't 
			// appear to integrate well
			UIManager.LookAndFeelInfo[] feels = UIManager.getInstalledLookAndFeels();
			int feelsLen = feels.length;
			boolean found = false;
			for (int i = 0; i < feelsLen; i++) {
				UIManager.LookAndFeelInfo info = (UIManager.LookAndFeelInfo)feels[i];
				if (info.getName().equals("Nimbus")) {
					UIManager.setLookAndFeel(info.getClassName());
					System.out.println("found Nimbus!");
					found = true;
					break;
				}
			}
			if (!found)
			*/
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			System.out.println(errorMsg);
		} catch (ClassNotFoundException e1) {
			// Java 6 without Nimbus throws this exception rather than 
			// the unsupported L&F one, so error message will suffice
//			errorMsg = "Class not found exception in main..." + NEWLINE + errorMsg;
			System.out.println(errorMsg);
		} catch (InstantiationException e2) {
			errorMsg = "Instantiation exception in main..." 
					+ NEWLINE + errorMsg;
			System.out.println(errorMsg);
		} catch (IllegalAccessException e3) {
			errorMsg = "Illegal access exception exception in main..." 
					+ NEWLINE + errorMsg;
			System.out.println(errorMsg);
		}
		
		// Create Text Trix!
		// and add it to the window list
		final TextTrix textTrix = openTTXWindow(args); //new TextTrix(args);
		

	}
	
	/** Filters command-line arguments to apply settings and
	 * return a list of file paths for opening.
	 * @param args array of command-line arguments, assumed to 
	 * be completely filled
	 * @return array of paths to open, assumed to be completely filled
	 */
	public String[] filterArgs(String[] args) {
		// file paths to open
		if (args == null) return null;
		String[] filteredArgs = new String[args.length];
		int filteredArgsi = 0;
		// true if next element is a file path
		boolean files = false;
		for (int i = 0; i < args.length; i++) {
			// identify switches
			if (args[i].indexOf("--") == 0) {
				files = false;
				// "--fresh" flag to start session without reopening tabs
				if (args[i].equals(ARG_FRESH)) {
					setFresh(true);
				} else if (args[i].equals(ARG_FILES)) {
					// "--files" to store command-line specified files
					files = true;
				} else if (args[i].equals(ARG_NO_HIGHLIGHTING)) {
					// "--nohigh" to turn off highlighting
					setHighlighting(false);
				} else if (args[i].equals(ARG_VERBOSE)) {
					// "--verbose" to turn on verbose command-line output
					setVerbose(true);
				} else if (args[i].equals(ARG_CLEAR_TABS)) {
					// "--cleartabs" clears memory of previously opened tabs
					setClearTabs(true);
					getPrefs().storeReopenTabsList("");
				}
			} else if (files || i == 0) {
				// if files flag set to true, or first arg is not a switch,
				// then treat the args as file paths until a switch is 
				// identified
				files = true;
				filteredArgs[filteredArgsi++] = args[i];
			}
		}
		// returns the array of paths equal in length to the number of paths
		return (String[])LibTTx.truncateArray(filteredArgs, filteredArgsi);
	}
	
	/** Sets the flag for opening a fresh session, where previous
	 * tabs are not reopening, but the most recent history of
	 * tabs is preserved for when this flag is not used.
	 * @param b true if files should not be reopened
	 */
	public void setFresh(boolean b) {
		fresh = b;
	}
	
	/** Gets the flag for opening a fresh session, where previous
	 * tabs are not reopening, but the most recent history of
	 * tabs is preserved for when this flag is not used.
	 * @return true if files should not be reopened
	 */
	public boolean getFresh() {
		return fresh;
	}
	
	/** Sets syntax highlighting on or off.
	 * @param b true if highlighting should be turned on
	 */
	public void setHighlighting(boolean b) {
		highlighting = b;
	}
	
	/** Gets syntax highlighting on or off.
	 * @return true if highlighting should be turned on
	 */
	public boolean getHighlighting() {
		return highlighting;
	}
	
	/** Gets the verbose output flag.
	 * @return true for verbose output
	 */
	public boolean getVerbose() {
		return verbose;
	}
	
	JMenu getFileMenu() { return fileMenu; }
	JMenu getViewMenu() { return viewMenu; }
	int getFileHistStart() { return fileHistStart; }
	LineDanceDialog getLineDanceDialog() { return lineDanceDialog; }
	JToolBar getToolBar() { return toolBar; }
	Action getLineSaverAction() { return lineSaverAction; }
	JCheckBoxMenuItem getAutoIndentCheckBox() { return autoIndent; }
	JPanel getStatusBarPanel() { return statusBarPanel; }
	JLabel getStatusBar() { return statusBar; }
	JProgressBar getStatusProgress() { return statusProgress; }
	JPopupMenu getStatusBarPopup() { return statusBarPopup; }
	JTextField getLineNumFld() { return lineNumFld; }
	JTextField getWordFindFld() { return wordFindFld; }
	
	
	
	
	/** Turns verbose command-line output on or off.
	 * @param b true to turn on verbose output
	 */
	public void setVerbose(boolean b) {
		verbose = b;
	}
	
	public void setClearTabs(boolean b) {
		clearTabs = b;
	}
	
	void setTrixMenu(JMenu val) { trixMenu = val; }
	void setToolsMenu(JMenu val) { toolsMenu = val; }
	void setToolBar(JToolBar val) { toolBar = val; }
	void setPopup(JPopupMenu val) { popup = val; }
	void setTabsPopup(JPopupMenu val) { tabsPopup = val; }
	void setUpdateTabIndexHistory(boolean b) { updateTabIndexHistory = b; }
	void setLineDanceDialog(LineDanceDialog val) { lineDanceDialog = val; }
	void setFileHistStart(int i) { fileHistStart = i; }
	void setAutoIndentJCheckBox(JCheckBoxMenuItem val) { autoIndent = val; }
	
	
	
	/** Opens a new Text Trix window.
	 * Creates a new instance of the TextTrix frame and
	 * registers the frame with the ttxWindows ArrayList.
	 * @param args command-line arguments
	 */
	public static TextTrix openTTXWindow(String[] args) {
		final TextTrix textTrix = new TextTrix(args);
		ttxWindows.add(textTrix);
		// Close the program with customized exit operation when
		// user hits close so can apply exit operations, such as remembering
		// open file paths
		textTrix.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		/* Workaround for bug #4841881, where Alt-Tabs are captured as
		 * menu shortcuts
		 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4841881
		 * This bug existed in Java v.1.4.x, disappeared in v.1.5, but
		 * reappeared in snapshots of v.1.6 (tested in b94).  The workaround
		 * has potentially undesirable side effects, as mentioned by the 
		 * submitter, but is a temporary solution until the Java regression
		 * is fixed.
		 */
		textTrix.addWindowFocusListener(new WindowFocusListener() {
			public void windowGainedFocus(WindowEvent e) {
			}
			
			public void windowLostFocus(WindowEvent e) {
				MenuSelectionManager.defaultManager().clearSelectedPath();
			}
		});
		
		// Make the frame visible from the event dispatch thread
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textTrix.setVisible(true);
			}
		});
		return textTrix;
	}
	
	
	
	
	
	
	
	/** Resets the file and view menus.
	 */
	void resetMenus() {
		fileMenu = new JMenu("File");
		viewMenu = new JMenu("View");
	}

	/**
	 * Opens the given file. Useful for opening files at program start-up
	 * because only gives command-line feedback if the file cannot be opened;
	 * the user may not have expected the opening in the first place and thus
	 * does not have to be needlessly concerned.
	 * 
	 * @param file file to open
	 */
	private boolean openInitialFile(File file, boolean reuseTab) {
		// command-line feedback
		String path = file.getPath();
		boolean verb = getVerbose();
		if (verb) {
			System.out.print("Opening file from path " + path + "...");
		}
		
		// Open the file if possible, or supply explanation if not
		if (!openFile(file, true, false, reuseTab)) {
			String msg = newline + "Sorry, but " + path + " can't be read."
					+ newline + "Is it a directory?  Does it have the right "
					+ "permsissions for reading?";
			System.out.println(msg);
			return false;
		} else {
			if (verb) {
				System.out.println("got it!");
			}
			return true;
		}
	}
	
	/** Opens a single intitial file.
	 * @param file file to open
	 * @return true if file opens successfully
	 */
	private boolean openInitialFile(File file) {
		return openInitialFile(file, false);
	}
	
	/**
	 * Opens the given file from the given path. 
	 * Useful for opening files at program start-up
	 * because only gives command-line feedback if the file cannot be opened;
	 * the user may not have expected the opening in the first place and thus
	 * does not have to be needlessly concerned.
	 * 
	 * @param path path of file to open
	 */
	private boolean openInitialFile(String path) {
		return openInitialFile(new File(path));
	}

	/**
	 * Switches focus synchronously to the selected <code>TextPad</code>, if
	 * one exists.
	 */
	public void focuser() {
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
	 * @param frame the Text Trix window frame
	 * @param filename name of given file, such as the currently displayed one
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
	 * @param filename name of given file, such as the currently displayed one
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
	 * @see #applyGeneralPrefs
	 * @see #applyShortsPrefs
	 * @see #reloadPlugIns
	 */
	public void applyPrefs() {
		reloadPlugIns();
		// no applyPlugInsPrefs b/c CreateMenuPanel takes care of GUI updates
		applyGeneralPrefs();
		applyShortsPrefs();
		(new MenuBarCreator(this)).start();
	}

	/**
	 * Applies preferences from the General tab in the preferences panel
	 * to each and every tab open.
	 *  
	 */
	public void applyGeneralPrefs() {
		// Update the file history based on whether should save or not
		Thread fileHistThread = new Thread(new FileHist(this));
		fileHistThread.start();
		
		// Update all the tabs in each tab group
		MotherTabbedPane pane = getGroupTabbedPane();
		int origPaneIndex = pane.getSelectedIndex();
		// access each tab by selecting it and applying settings
		// TODO: access text pad directly, rather than through selection
		boolean autoSave = getPrefs().getAutoSave();
		boolean spellCheck = getPrefs().getSpellChecker();
		int tabCount = pane.getTabCount();
		int fontSize = -1;
		for (int h = 0; h < tabCount; h++) {
			pane.setSelectedIndex(h);
			for (int i = 0; i < getSelectedTabbedPane().getTabCount(); i++) {
				TextPad pad = getTextPadAt(i);
				// restarts the save timer
				if (autoSave) {
					if (pad.getChanged()) startTextPadAutoSaveTimer(pad);
				} else {
					stopTextPadAutoSaveTimer(pad);
				}
				
				// resets spell-checker
				pad.spellChecker(spellCheck);
				
				// adjusts font size
				if (h == 0 && i == 0) {
					// checks font size on first tab to see if different
					// from prefs setting
					int currFontSize = pad.getFont().getSize();
					fontSize = getPrefs().getFontSize();
					if (fontSize == currFontSize) fontSize = -1;
					DefaultSyntaxKit.setFontSize(fontSize); // jsyntaxpane
					TokenStyles.setFontSize(fontSize); // oster
					TokenStyles.initialize();
				}
				if (fontSize != -1) {
					// adjusts font size if different
					Document doc = pad.getDocument();
					if (doc instanceof HighlightedDocument) {
						// recolors Ostermillier-highlighted document
						HighlightedDocument highDoc = (HighlightedDocument)doc;
						highDoc.colorAll();
					}
					// adjusts font setting, which will automatically update
					// size on JSyntaxPane-highlighted text but not 
					// Ostermiller-highlighted documents, but will still
					// update to remember font setting
					pad.setFont(pad.getFont().deriveFont((float)fontSize));
				}
			}
		}
		
		// Re-select the originally selected tab
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
		// Cycle through each tab in each tab group
		MotherTabbedPane pane = getGroupTabbedPane();
		int origPaneIndex = pane.getSelectedIndex();
		
		// applies shortcuts according to user choice in preferences
		// TODO: access text pad directly, rather than through selection
		int groupTabCount = pane.getTabCount();
		for (int h = 0; h < groupTabCount; h++) {
			pane.setSelectedIndex(h);
			int tabCount = getSelectedTabbedPane().getTabCount();
			for (int i = 0; i < tabCount; i++) {
				if (prefs.isHybridKeybindings()) {
					// Hybrid shortcuts, similar to that of Pico
					getTextPadAt(i).hybridKeybindings();
				} else if (prefs.isEmacsKeybindings()) {
					// Emacs-style shortcuts
					getTextPadAt(i).emacsKeybindings();
				} else {
					// Standard shortcuts
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
	 * @param pl plugin from which to make an action
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
							// reverse selection to prevent selected area from
							// scrolling off screen if area > 1 line
							textSelectionReverse(t, 0, selectionStart, selectionEnd);
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
// 			viewPlain();
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
				
				// By default, only sends the selected text;
				// if no region is selected, or if plug-in flags to works on the text pad's entire
				// text, sends the plugin all the text
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
				
					// Send plugin the selected text only
					
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
		if (t.isAutoIndent() ) t.indentRegion(start, start + outcome.getText().length());
	}

	/**
	 * Selects the given region of text. Works on the given text pad.
	 * @param t text pad from which to select text
	 * @param offset starting point from which to measure <code>start</code> 
	 * and <cdoe>end</code>
	 * @param start beginning point of selection, relative to baseline
	 * @param end end point of selection, relative to baseline
	 */
	public void textSelection(TextPad t, int offset, int start, int end) {
		if (end != -1 && start != end) {
			t.setCaretPositionTop(offset + start);
			t.moveCaretPosition(offset + end);
			t.getCaret().setSelectionVisible(true);
			// to ensure selection visibility
		} else {
			t.setCaretPositionTop(offset + start);
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
	 * @param t text pad from which to select text
	 * @param baseline starting point from which to measure <code>start</code> 
	 * and <cdoe>end</code>
	 * @param start beginning point of selection, relative to baseline
	 * @param end end point of selection, relative to baseline
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
		File plugInsFile = getPlugInsFile();
		// WORKAROUND: JNLP cannot find the plugins folder,
		// so the current workaround is to return empty-handed if no
		// plugins folder is found
		if (!plugInsFile.exists()) return;

		// load the plugins and create actions for them
		plugIns = LibTTx.loadPlugIns(plugInsFile);
		plugInDiags = new JDialog[plugIns.length];
		plugInDiagsIdx = 0;
		getPrefs().updatePlugInsPanel(getPlugInPaths());
		if (plugIns != null) {
			for (int i = 0; i < plugIns.length; i++) {
				makePlugInAction(plugIns[i]);
			}
		}
	}

	
	/** Gets the <code>plugins</code> file for accessing Text Trix plugins.
	 * Notifies the user if the folder could not be located.
	 *
	 * <p>TODO: Add preferences mechanism to specify alternative or additional
	 * <code>plugins</code> folder location, such as a permanent storage place
	 * to reuse plug-ins after installing a new version of Text Trix.
	 * 
	 * @return the plugins folder
	 */
	public File getPlugInsFile() {
//		File plugInsFile = new File(LibTTx.getBaseFile(), "plugins");
		File plugInsFile = new File(LibTTx.getBaseURI("plugins"));
		if (!plugInsFile.exists()) {
			System.out.println("I haven't been able to locate"
				+ newline + plugInsFile.toString()
				+ newline + "for your plug-ins.");
		}
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
	
	/** Gets the tabbed pane for tab groups.
	 * @return the "mother" pane of all other tabs
	 */
	public MotherTabbedPane getGroupTabbedPane() {
		return groupTabbedPane;
	}
	
	/** Gets the selected tab group.
	 * @return the tab group tabbed pane
	 */
	public  MotherTabbedPane getSelectedTabbedPane() {
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
	public  TextPad getSelectedTextPad() {
		MotherTabbedPane pane = getSelectedTabbedPane();
		if (pane != null) {
			int i = pane.getSelectedIndex();
			if (i != -1) {
				return (TextPad) ((JScrollPane) pane.getComponentAt(i)).getViewport().getView();
			}
		}
		return null;
	}

	/**
	 * Gets the <code>TextPad</code> at a given index in the tabbed pane.
	 * 
	 * @param i
	 *            index of <code>TextPad</code>
	 * @return the given Text Pad; <code>null</code> if the tabbed pane lacks
	 *         the index value
	 */
	public  TextPad getTextPadAt(int i) {
		return getTextPadAt(getSelectedTabbedPane(), i);
	}
	
	/** Gets the {@link TextPad} at the given index in the given tab group.
	 * @param pane the tab group
	 * @param i the tab index
	 * @return the Text Pad
	 */
	public  TextPad getTextPadAt(JTabbedPane pane, int i) {
		if (i < -1 || i >= pane.getTabCount())
			return null;
		return (TextPad) ((JScrollPane) pane.getComponentAt(i)).getViewport().getView();
		
	}
	
	/** Gets the tab group tabbed pane at the given index.
	 * @param i the index of the group tab to retrieve
	 * @return the tab group tabbed pane
	 */
	public  MotherTabbedPane getTabbedPaneAt(int i) {
		return (MotherTabbedPane) getGroupTabbedPane().getComponentAt(i);
	}
	
	/** Gets the mother tabbed pane that houses the given Text Pad.
	 * @param textPad the text pad
	 * @return the tabbed pane of the text pad's tabbed pane
	 */
	public  MotherTabbedPane getTabbedPane(TextPad textPad) {
		int lenGroup = getGroupTabbedPane().getTabCount();
		MotherTabbedPane pane = null;
		// finds the Text Pad by searching for the index of the component
		// in each tab group
		for (int i = 0; i < lenGroup; i++) {
			pane = getTabbedPaneAt(i);
			if (pane.indexOfComponent(textPad.getScrollPane()) != -1) {
				return pane;
			}
		}
		return null;
	}
	
	/** Gets all of the TextPads.
	 * @return ArrayList of all the TextPads
	 */
	public ArrayList getTextPads() {
		ArrayList pads = new ArrayList();
		int lenGroup = getGroupTabbedPane().getTabCount();
		for (int i = 0; i < lenGroup; i++) {
			MotherTabbedPane pane = getTabbedPaneAt(i);
			int padCount = pane.getTabCount();
			for (int j = 0; j < padCount; j++) {
				pads.add(((JScrollPane) pane.getComponentAt(j))
						.getViewport().getView());
			}
		}
		return pads;
	}
	
	/** Gets the index of the given Text Pad in the given tabbed pane.
	 * @param pane the tabbed pane that holds the Text Pad
	 * @return the index of the Text Pad
	 */
	public  int getTextPadIndex(JTabbedPane pane, TextPad textPad) {
		return pane.indexOfComponent(textPad.getScrollPane());
	}

	
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
	public  void setAutoIndent() {
		TextPad t = getSelectedTextPad();
		if (autoIndent != null && t != null) {
			autoIndent.setSelected(t.getAutoIndent());
		}
	}

	/** Sets the selection to the given Text Pad index in
	 * the given tab group.
	 * @param motherIdx index of the tab group
	 * @param padIdx index of the tab within the tab group
	 */
	public void setSelectedTextPad(int motherIdx, int padIdx) {
		getGroupTabbedPane().setSelectedIndex(motherIdx);
		getSelectedTabbedPane().setSelectedIndex(padIdx);
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
	 * Exits <code>TextTrix</code> by closing each tabs and saving their
	 * names for subsequent reopening, as well as calling System.exit.
	 * @return true if Text Trix exits successfully
	 * @see #closeAllTabs
	 */
	public boolean exitTextTrix() {
		String reopenPaths = "";
		boolean b = true;
		for (int i = 0; i < ttxWindows.size(); i++) {
			b = b && ((TextTrix)ttxWindows.get(i)).closeAllTabs();
			reopenPaths += FILE_WINDOW_SPLITTER + getPrefs().getReopenTabsList();
		}
		// store the file list and exit Text Trix if all the files closed
		// successfully, set to reopen tabs, and not set for a fresh session
		if (b == true) {
			if (!getFresh() && getPrefs().getReopenTabs()) {
				getPrefs().storeReopenTabsList(reopenPaths);
			}
			System.exit(0);
		}
		return b;
	}
	
	/**	Closes each tab individually, checking
	 * for unsaved text areas in the meantime.
	 * The tab filenames are stored as one continuous string, separated by the
	 * names of their tab groups.
	 * TODO: restore open tabs if close canceled
	 * @return true if all tabs are successfully closed
	 */
	public boolean closeAllTabs() {
		// Get the open file history and prep for new entries
		String openedPaths = "";
		boolean reopenTabs = getPrefs().getReopenTabs();
		// resets open tabs history
		boolean b = true; // flags whether ok to close tab
		boolean newGrp = false; // flag for new tab group
		
		// Close the tabs
		MotherTabbedPane pane = getGroupTabbedPane();
		// cycles through the tab groups
		for (int i = 0; i < pane.getTabCount() && b; i++) {
			// appends the tab group name
			String groupTabTitle = getGroupTabbedPane().getTitleAt(i);
			// don't save "Start" group, which contains files
			// specified on the command line
			if (!groupTabTitle.equals(GROUP_START)) {
				openedPaths = openedPaths 
					+ FILE_GROUP_SPLITTER 
					+ groupTabTitle;
				pane.setSelectedIndex(i);
				// identifies the number of tabs in the tab group
				int totTabs = getSelectedTabbedPane().getTabCount();
		
				// closes the files and prepares to store their paths in the list
				// of files left open at the end of the session
				while (totTabs > 0 && b) {
					// only stores if told to in prefs
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
		}
		// preserves most recently saved tabs if set to fresh session
		if (b == true) {
			if (!getFresh() && reopenTabs) {
				getPrefs().storeReopenTabsList(openedPaths);
			}
		}
		return b;
	}
	
	/** Gets the title of the selected tab group.
	 * @return the tab group name
	 */
	public String getSelectedTabbedPaneTitle() {
		JTabbedPane pane = getGroupTabbedPane();
		return pane.getTitleAt(pane.getSelectedIndex());
	}
	
	/** Sets the name of the selected tab group.
	 * @param title the new name of the tab group
	 */
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
	 * @param tabIndex index of tab to close
	 * @param tabbedPane pane holding a tab to be closed
	 * @return <code>true</code> if the tab successfully closes
	 */
	public boolean closeTextArea(int tabIndex, MotherTabbedPane tabbedPane) {
		boolean successfulClose = false;
		
		// Get the given Text Pad
		TextPad t = getTextPadAt(tabbedPane, tabIndex);
		// checks if unsaved text area
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

	/** Adds a new tab group to the given tabbed pane.
	 * @param tabbedPane the tabbed pane to receive the new tab group tabbed pane
	 * @param title the title for the new tab group
	 */
	public void addTabbedPane(MotherTabbedPane tabbedPane, String title) {
		final MotherTabbedPane newTabbedPane = new MotherTabbedPane(JTabbedPane.TOP);
		
		// keep the tabs the same width when substituting chars
		newTabbedPane.setFont(new Font("Monospaced", Font.PLAIN, 11));
		
		// Appends a new tab group and selects it
		int i = tabbedPane.getTabCount();
		if (title.equals("")) {
			title = "Group " + i;
		}
		tabbedPane.addTab(title, newTabbedPane);
		tabbedPane.setSelectedIndex(i);
		
		// TODO: focus in newly created tab
		addTextArea(newTabbedPane, makeNewFile());
		updateUIForTextPad(newTabbedPane, getSelectedTextPad());
		updateTabHistory(getSelectedTabbedPane());
		
		// adds a change listener to listen for tab switches and display the
		// options of the tab's TextPad
		newTabbedPane.addChangeListener(new TextPadChangeListener(newTabbedPane));
		
	}
	
	/**
	 * Creates a new <code>TextPad</code> object, a text area for writing, and
	 * gives it a new tab. Can call for each new file; names the tab,
	 * <code>File[n].txt</code>, where <code>n</code> is the tab number.
	 * @param tabbedPane the tabbed pane to receive the new Text Pad
	 * @param file the file to open in the new Text Pad
	 */
	public void addTextArea(MotherTabbedPane tabbedPane,
		File file) {
		
		if (tabbedPane == null) {
			addTabbedPane(getGroupTabbedPane(), "");
			return;
		}
	
		// Create the new Text Pad		
		updateTabIndexHistory = true;
		final TextPad textPad = new TextPad(file, getPrefs());
		
		// Add the pad to a scroll pane;
		// uses SCROLLBAR_AS_NEEDED to conform to native behavior
		// as much as possible, including showing scrollbars
		// on Mac platforms
		final JScrollPane scrollPane = new JScrollPane(textPad,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		textPad.setScrollPane(scrollPane);
		textPad.spellChecker(getPrefs().getSpellChecker());
// 		textPad.setHighlightStyle();

		// must add to array list before adding scroll pane to tabbed pane
		// or else get IndexOutOfBoundsException from the array list
		// 1 more than highest tab index since will add tab
		int i = tabbedPane.getTabCount();
		tabbedPane.addTab(file.getName() + " ", scrollPane);
		textPad.addDocListener(new TextPadDocListener(textPad));
		textPad.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				updateStatusBarLineNumbers(textPad);
			}
		});
		// show " *" in tab title when text changed
		tabbedPane.setSelectedIndex(i);
		tabbedPane.setToolTipTextAt(i, textPad.getPath());
	}

	/** Updates the user interface for the give Text Pad.
	 * The menu entries, including the auto-wrap-indent check box,
	 * and status bar line numbers are updates.  The main window
	 * title takes on the name of the file in the tab.  The Line Dance
	 * panel corresponding to the pad is added.  The pad
	 * also requests focus.
	 * @see #updateTabHistory
	 */
	public void updateUIForTextPad(MotherTabbedPane pane, TextPad t) {
		if (t != null) {
			setAutoIndent();
			updateTitle(t.getFilename());
			
			if (lineDanceDialog != null && lineDanceDialog.isVisible()) {
				lineDanceDialog.updatePadPanel();
			}
			
			// doesn't work when creating new tabs via
			// the keyboard accelerator;
			// only works when changing between already created
			// tabs or creating new ones via the menu item
			t.requestFocusInWindow();
			updateStatusBarLineNumbers(t);
		}
	}
	
	/** Updates the tab history for the given Text Pad.
	 * If the <code>updateTabIndexHistory</code> flag is set
	 * to false, no history will be added, but the flag will be
	 * reset to true.
	 * @param pane the tab group
	 * @param i the index of the tab to add to the history; if
	 * -1, then the currently selected tab index will be stored
	 */
	public void updateTabHistory(MotherTabbedPane pane, int i) {
		// update the tab index record;
		// addTabIndexHistory increments the record index;
		// add the current tab selection now to ensure that
		// all selections are recorded
		if (i == -1) i = pane.getSelectedIndex();
		if (updateTabIndexHistory) {
			pane.addTabHistory(i);
		} else {
			updateTabIndexHistory = true;
		}
	}
	
	/** Updates the tab history for the currently selected Text Pad.
	 * If the <code>updateTabIndexHistory</code> flag is set
	 * to false, no history will be added, but the flag will be
	 * reset to true.
	 * @param pane the tab group
	 */
	public void updateTabHistory(MotherTabbedPane pane) {
		updateTabHistory(pane, -1);
	}
	
	/**
	 * Changes given tab's title in the tabbed pane title to indicate whether
	 * the file has unsaved changes. Appends "<code> *</code>" if the file
	 * has unsaved changes; appends "<code>  </code>" otherwise.
	 * 
	 * @param textPad
	 *            Text Pad to update
	 */
	public  void updateTabTitle(TextPad textPad) {
		MotherTabbedPane pane = getTabbedPane(textPad);
		int tabIndex = getTextPadIndex(pane, textPad);

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
	 * Adds additional listeners and other settings to a <code>TextPad</code>.
	 * Useful to apply on top of the <code>TextPad</code>'s
	 * <code>applyDocumentSettings</code> function.
	 * 
	 * @param textPad pad requiring applied settings
	 */
	public void addExtraTextPadDocumentSettings(TextPad textPad) {
		textPad.addDocListener(new TextPadDocListener(textPad));
		textPad.setChanged(true);
		updateTabTitle(textPad);
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

	
	/** Switches the given or current TextPad to HTML
	 * view, prompting the user if necessary.
	 * @param pad the TextPad to change views; if null,
	 * the currently selected pad will be changed
	 * @param warning if true, the user will be prompted
	 * before proceeding with the view change
	 */
	public boolean switchToHTMLView(TextPad pad, boolean warning) {
		boolean changeView = true;
		if (pad == null) pad = getSelectedTextPad();
		if (pad == null) return false;
// 		System.out.println("html");
		if (!pad.isHTMLView()) {
			if (warning) {
				changeView = LibTTx.yesNoDialog(
					this,
					"To apply text formatting, the text pad "
					+ NEWLINE + "will be switched to HTML view, and some "
					+ NEWLINE + "current formmating may be lost.  Proceed?",
					"Switch to HTML view?");
			}
			if (changeView) {
				pad.viewHTML();
				addExtraTextPadDocumentSettings(pad);
			}
		}
		return changeView;
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
	 * @param textPad pad to read a file into
	 * @param in file reader
	 * @param desc reader stream description
	 */
	public void read(final TextPad textPad, Reader in, Object desc)
			throws IOException {
		final String text = LibTTx.readText(new BufferedReader(in));
		// reads in text and sets it manually, assuming newly created
		// HighlightedDocument in TextPad;
		// runs in EDT to prevent GUI lock-up during loading
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				loadText(textPad, text);
				if (getPrefs().getHighlighting() && getHighlighting()) {
					// need to set highlight style after loading text
					// for the Ostermiller highlighter to continue dynamically 
					// updating syntax coloration
					applyHighlighting(textPad);
				}
				autoAutoIndent(textPad);
				textPad.setCaretPosition(0);
			}
		});
	}
	
	/**
	 * Loads text into a TextPad and sets teh caret position.
	 * The associated documents managers will also be applied, and the text's
	 * end-of-line style will be stored for future reference.
	 * Loading process will be displayed in the progress bar.
	 */
	private void loadText(final TextPad textPad, final String text) {
		// reads and saves the end-of-line style to reapply when saving
		String eol = LibTTx.getEOL(text);
		System.out.println("loading eol..." + LibTTx.getEOLName(eol));
		textPad.setEOL(eol);
		
		// sets the text, applies filters and managers, repositions the
		// caret to the original position, and sets the file modification flags
// 		textPad.replaceAllText(text);
		textPad.setText(text);
		textPad.setChanged(false);
		updateTabTitle(textPad);
		
		// provides feedback on document loading status
		Thread loadCheck = 
				new Thread(new CheckDocLoad(textPad, statusProgress));
		loadCheck.start();
	}
	
	private void applyHighlighting(TextPad textPad) {
		textPad.removeDocListener();
		textPad.setHighlightStyle();
		textPad.addDocListener(new TextPadDocListener(textPad));
		textPad.applyDocumentSettings();
	}
	
	/** Removes the currently selected tabbed pane from the
	 * given group tabbed pane.
	 * @param tp the group tabbed pane that contains other
	 * tabbed panes
	 */
	public static void removeTabbedPane(MotherTabbedPane tp) {
		int i = tp.getSelectedIndex();
		if (i >= 0 && i < tp.getTabCount()) {
			tp.remove(i);
		}
	}
	/**
	 * Removes a tab containing a text area.
	 * 
	 * @param i tab index
	 * @param tp tabbed pane from which to remove a tab
	 */
	public  void removeTextArea(int i, MotherTabbedPane tp) {
		TextPad t = getSelectedTextPad();
		// stops the pad's save timer and removes the pad
		if (t != null) {
			stopTextPadAutoSaveTimer(t);
			t.stopFileModifiedThread();
			tp.remove(i);
		}
	}

	/**
	 * Saves text area contents to a given path.
	 * 
	 * @param path file path in which to save
	 * @param t the pad to save; if <code>null</code>, defaults to the
	 *            currently selected pad if null
	 * @return true for a successful save, false if otherwise
	 * @see #saveFile
	 */
	public boolean saveFile(String path, TextPad t) {
		if (t == null)
			t = getSelectedTextPad();
		try {
			if (t != null) {
				/*
				 * if don't use canWrite(), work instead by catching exception
				 * and either handling it there or returning signal of the
				 * failure
				 */
				LibTTx.writeText(path, t.getText(), t.getEOL());
				System.out.println("saving eol..." + LibTTx.getEOLName(t.getEOL()));
				// keeps track of orig filename to compare file extensions
				// for syntax highlighting;
				// assumes that path points to a valid file
				String origName = t.getFile().getName();
				t.setFile(path);

				// stops any auto-save timer attached to the pad
				// since the file has just been saved;
				// relies on TextPadDocListener to restart the timer
				stopTextPadAutoSaveTimer(t);
				
				getPrefs().storeFileHist(path);
				// sets the style according to extension, but only if 
				// the next extension is different from the previous one
				if (!origName.equals(t.getFile().getName())) {
					if (getPrefs().getHighlighting() && getHighlighting()
								&& !LibTTx.getFileExtension(origName)
										.equalsIgnoreCase(t.getFileExtension())) {
						String text = t.getAllText();
// 						t.removeAllText();
						int caretPos = t.getCaretPosition();
						loadText(t, text);
						applyHighlighting(t);
// 						t.setHighlightStyle();
// 						t.addDocListener(new TextPadDocListener(t));
						// reattach undo manager and listeners;
						// note that prevents undos from before the save
// 						t.applyDocumentSettings();
						t.setCaretPosition(caretPos);
					}
					// automatically starts indenting, if applicable, after
					// rather than before applying the syntax highlighting 
					// so that the indentations are applied to the new styled doc
					autoAutoIndent(t); 
				}
				t.setChanged(false);
				updateTabTitle(t);//textAreas.indexOf(t));
				return true;
			}
		} finally { // release system resources from stream
			t.setupFileModifiedThread();
		}
		return false;
	}
	
	/**
	 * Saves the file to the given path. Similar to
	 * <code>saveFile(String)</code>, but tailored for the program exit by
	 * ignoring updates to the graphical user interface.
	 * 
	 * @param path the path to the modified file
	 * @return true if the file saves successfully
	 */
	public boolean saveFileOnExit(String path) {
		//	System.out.println("printing");
		TextPad t = getSelectedTextPad();
		return LibTTx.writeText(path, t.getText(), t.getEOL());
	}
	

	/**
	 * Opens a file into a text pad. Calls the file open dialog. Opens the file
	 * into a new pad unless the currently selected one is empty. Sets the
	 * file's name as a the tab's title and the path as the tab's tool tip.
	 * Assumes that the file is readable as text.  Anytime multiple files are
	 * opened at once, the <code>updateForTextPad</code> flag should
	 * be set to false to prevent the change listener from responding
	 * after the fact, since the listener is on an EvokeLater.  The tab history
	 * should be updated manually for each file, and the UI should be
	 * updated after opening the last file, just before resetting the flag 
	 * to true.
	 * 
	 * @param file file to open
	 * @param editable <code>true</code> if the resulting text pad should be
	 *            editable
	 * @param resource <code>true</code> if the file should be accessed as a
	 *            resource, via <code>TextTrix.class.getResourceAsStream(path)</code>
	 * @param reuseTab <code>true</code> if a tab should be reused, even
	 * if it is isn't empty or is empty but with unsaved changes
	 * @see #openFile
	 * @return true if the file is successfully opened or already open
	 */
	public boolean openFile(File file, boolean editable, boolean resource, 
			boolean reuseTab) {
		String path = file.getPath();
		
		// Check to see if the file is already open before creating new tab
		if (!path.equals("")) {
			int len = getGroupTabbedPane().getTabCount();
			// checks through each tab group
			for (int i = 0; i < len; i++) {
				MotherTabbedPane pane = getTabbedPaneAt(i);
				// checks through each tab in each group
				if (pane.getTabCount() > 0) {// && !reuseTab) {
					int idPath = getIdenticalTextPadIndex(path, i);
					if (idPath != -1) {
						setSelectedTextPad(i, idPath);
						return true;
					}
				}
			}
		}
		// ensures that the file exists and is not a directory
		if (file.canRead() || resource) { // readable file
			TextPad t = getSelectedTextPad();
			BufferedReader reader = null;
			try {
				// Resources are read as input streams, while
				// non-resources are read with file readers
				if (resource) {
					// Attempt to get the resource as a stream
					InputStream in = TextTrix.class.getResourceAsStream(path);
					if (in != null) {
						// if unable, read the resource with a buffered reader
						reader = new BufferedReader(new InputStreamReader(in));
					} else {
						return false;
					}
				} else {
					// Read non-resources through a buffered reader
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
				if (t != null && (reuseTab && (t.isEmpty() && !t.getChanged()))) {
					read(t, reader, path);
				} else {
					addTextArea(getSelectedTabbedPane(), file);
					t = getSelectedTextPad();
					read(t, reader, path);
				}
				t.setFile(path); // sets file pointer
				t.setupFileModifiedThread(); // sets last modified time
				// TODO: check whether thread safe
				getSelectedTabbedPane().setToolTipTextAt(
						getSelectedTabbedPane().getSelectedIndex(), 
						t.getPath());
				// set the path to the last opened directory
				setOpenDir(file.getParent());
				// file.getParent() returns null when opening file
				// from the command-line and passing in a relative path
				if (getOpenDir() == null) {
					setOpenDir(System.getProperty("user.dir"));
				}
				getPrefs().storeFileHist(path);
				//updateFileHist(fileMenu);
// 				if (getPrefs().getHighlighting() && getHighlighting()) {
// 					t.setHighlightStyle(getPrefs().getSpellChecker());
// 				}
// 				Thread loadCheck = new Thread(
// 						new CheckDocLoad(t, statusProgress));
// 				loadCheck.start();
// 				autoAutoIndent(t);
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
	public int getIdenticalTextPadIndex(String path, int paneIdx) {
		MotherTabbedPane pane = getTabbedPaneAt(paneIdx);
		int len = pane.getTabCount(); // the number of tabs
		// checks each tab to see if any have the given path
		for (int i = 0; i < len; i++) {
			if (getTextPadAt(pane, i).getPath().equals(path)) {
				return i;
			}
		}
		return -1;
	}

	/**Refreshes a tab without the user having to close and reopen it.
	 * Useful when an open file is externally changed.
	*/
	public void refreshTab() {
		// refreshes the currently selected TextPad
		TextPad t = getSelectedTextPad();
		if (t != null) {
			t.refresh();
		}
	}

	/**
	 * Automatically auto-indents the given Text Pad. Determines whether the
	 * Text Pad's filename extension matches the user-defined list of files to
	 * automatically auto-indent.
	 * 
	 * @param t pad whose file is to be checked
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
	 * @param path file to check
	 * @return <code>true</code> if the file's extension is in the list
	 * @see #autoAutoIndent(TextPad)
	 */
	public boolean isAutoIndentExt(String path) {
		// get the file extension index
		int extIndex = path.lastIndexOf(".") + 1;
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
			if (tokenizer.nextElement().equals(ext)) return true;
		}
		return false;
	}

	/**
	 * Evokes a save dialog to save a file just before exiting the program, when
	 * the text pad will no longer exist.
	 * 
	 * @param owner parent frame; can be null
	 * @return true if the approve button is chosen, false if otherwise
	 */
	public  boolean fileSaveDialogOnExit(JFrame owner) {
		if (!prepFileSaveDialog(null))
			return false;
		return getSavePathOnExit(owner);
	}

	/**
	 * Evokes a save dialog to save the given pad's file. Sets the tabbed pane
	 * tab to the saved file name.
	 * 
	 * @param pad the text pad with the file to save
	 * @param owner parent frame; can be null
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
	 * @param t the pad from which to gather the path defaults; if
	 *            <code>null</code> the pad defaults to the currently selected
	 *            pad
	 * @return <code>true</code> if a Text Pad is selected, necessary to save
	 *         a file
	 */
	public boolean prepFileSaveDialog(TextPad t) {
		//	int tabIndex = getSelectedTabbedPane().getSelectedIndex();
		if (t == null)
			t = getSelectedTextPad();
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
	 * Helper function to <code>fileSaveDialog</code> when exiting Text Trix.
	 * Unlike <code>getSavePath(JFrame)</code>, this method does not attempt
	 * to update the graphical components, currently in the process of closing.
	 * Opens the file save dialog to retrieve the file's new name. If the file
	 * will overwrite another file, prompts the user with a dialog box to
	 * determine whether to continue with the overwrite, get another name, or
	 * cancel the whole operation.
	 * 
	 * @param owner the frame to which the dialog will serve; can be null
	 * @return true if the file is saved successfully
	 * @see #getSavePath(TextPad, JFrame)
	 */
	public  boolean getSavePathOnExit(JFrame owner) {
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
						repeat = LibTTx.yesNoDialog(owner, msg, title);
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
	 * @param pad the <code>TextPad</code> whose file will be saved
	 * @param owner the frame to which the dialog will serve; can be null
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
					int len = getGroupTabbedPane().getTabCount();
					int idPath = -1; // a pad with the same file
					int paneIdx = -1; // the pad's tab group
					while (idPath == -1&& ++paneIdx < len) {
						if (getTabbedPaneAt(paneIdx).getTabCount() > 0) {
							idPath = getIdenticalTextPadIndex(path, paneIdx);
						}
					}
					// Warn user that another pad has the same file as
					// the file that is being written to from the currently
					// selected tab.  This should only happen when the
					// selected tab has not been saved previously.
					// Ask if the identical pad should be closed before
					// saving the selected tab.
					if (idPath != -1) {
						String closeIDTabWarning = 
							"A text pad saved to the path you have chosen"
							+ NEWLINE + "is already open.  Should I close it?  It may have"
							+ NEWLINE + "unsaved changes.";
						String[] closeIDTabOptions = { "Close it anyway", "Show me", "Cancel" };
						// retrieves the user's choice
						int closeIDTabChoice = JOptionPane.showOptionDialog(
							owner, closeIDTabWarning,
							"Close?", JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE, null, closeIDTabOptions,
							closeIDTabOptions[1]);
						switch (closeIDTabChoice) {
							case 0:
								// Close the duplicate pad in deference to the current one
								removeTextArea(idPath, getTabbedPaneAt(paneIdx));
								break;
							case 1:
								// Shows the other, duplicate file and exit
								setSelectedTextPad(paneIdx, idPath);
								return false;
							case 2:
								// Cancel the save
								return false;
						}
					}
					// Aks user whether to overwrite the original file
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
					
					if (success) {
						setSaveDir(chooser.getSelectedFile().getParent());
						// update graphical components
						MotherTabbedPane pane = getTabbedPane(pad);
						pane.setToolTipTextAt(getTextPadIndex(pane, pad), path);
						updateTitle(owner, f.getName());
						getPrefs().storeFileHist(path);
						//fileHist.start(fileMenu);
						Thread fileHistThread = new Thread(new FileHist(this));
						fileHistThread.start();

						return true;

					} else { // fail; request another try at saving
						String msg = path + " couldn't be written to "
								+ "that location.\nWould you like to try "
								+ "another directory or filename?";
						String title = "Couldn't save";
						repeat = LibTTx.yesNoDialog(owner, msg, title);
					}
				}
			} else { // cancel button chosen
				return false;
			}
		} while (repeat); // repeat if retrying save after failure
		return false;
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
	 * @param pad the pad containing the document to save automatically
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
	 * @param pad the pad with the auto-save timer to stop
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
	private TextTrix getThis() {
		return this;
	}

	/**
	 * Updates the status bar with the latest line number information.
	 * 
	 * @param pad the pad
	 */
	public void updateStatusBarLineNumbers(TextPad pad) {
		// TODO: make one component of a larger status-bar update operation
		if (statusBar != null) {
			int lineNum = pad.getLineNumber();
			int totLines = pad.getTotalLineNumber();
			// cast to float for float division rather than int division
			int percentage = (int) ((float) lineNum / (float) totLines * 100);
			statusBar.setText(lineNum + ", " + totLines + " " + "(" + percentage
					+ "%)");
		}
	}

	/**
	 * Displays a message dialog to inform the user that a resource, such as a
	 * documentation or icon file, is missing. Points the user to the Text Trix
	 * website to find the resource and notify its maintainers to include the
	 * file in the next release.
	 * 
	 * @param path the path to the missing resource
	 */
	void displayMissingResourceDialog(String path) {
		JOptionPane.showMessageDialog(getThis(), "Hm, I can't seem to find \""
				+ path + "\""
				+ "\n...really sorry 'bout that.  You might find it at"
				+ "\nhttp://textflex.com/texttrix, and in the meantime,"
				+ "\nwe invite you to drop us a line so that we can "
				+ "\ninclude the file in the next release.  Thanks!",
				"Really sorry, but I couldn't find that",
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	
	
	
	
	
	
	/** Sets the <code>updateForTextPad</code> flag to the given value
	 * to let the <code>TextPadChangeListener</code> know whether
	 * or not to update the UI and tab history based during a change event.
	 * @param aUpdateForTextPad true if the listener should respond
	 * events; false if the UI and tab updates will be taken care of
	 * independently
	 */
	public void setUpdateForTextPad(boolean aUpdateForTextPad) {
		updateForTextPad = aUpdateForTextPad;
	}
	
	
	
	
	
	
	
	/** Gets the main file chooser, which includes file filters.
	 * @return the main file chooser
	 */
	public JFileChooser getChooser() {
		if (chooser == null) {
			/** WORKAROUND:
			 *	For Sun Java bug #6210674:
			 *	"FileChooser fails to load custom harddrive icon and gets NullPointerException"
			 *	(http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6210674)
			 *	Workaround from Sun Java bug #4711700
			 *	(http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4711700)
			 *
			 * This bug causes Text Trix to crash inconsistently during loading.
			 * Apparently the problem is a timing issue in loading an image icon
			 * for the file chooser.  The problem has occurred sporadically during
			 * tests in Windows XP SP2, but more conistently in Windows Vista.
			 * An alternative workaround is to repeatedly catch the exception and
			 * try again to produce the chooser until it works.
			 */
		    class JF extends JFileChooser {
		        protected void setUI(ComponentUI newUI) {
		            try {
		                super.setUI(newUI);
		            } catch (NullPointerException e) {
		                if (newUI.getClass().getName().equals(
								"com.sun.java.swing.plaf.windows.WindowsFileChooserUI")) {
		                    try {
		                        super.setUI(null);
		                    } catch (NullPointerException ee) {
		                        ui = null;
		                    }
		                    super.setUI(new MetalFileChooserUI(this));
		                } else {
		                    throw e;
		                }
		            }
		        }
		    }
			chooser = new JF();
			FileFilter allFilter = chooser.getFileFilter();
			
			// web file filters
			final ExtensionFileFilter webFilter = new ExtensionFileFilter();
			webFilter.addExtension("html");
			webFilter.addExtension("htm");
			webFilter.addExtension("xhtml");
			webFilter.addExtension("shtml");
			webFilter.addExtension("css");
			webFilter.addExtension("js");
			webFilter.setDescription(
					"Web files (.html, .htm, .xhtml, .shtml, .css, .js)");
			chooser.setFileFilter(webFilter);
	
			// RTF file filters
			final ExtensionFileFilter rtfFilter = new ExtensionFileFilter();
			rtfFilter.addExtension("rtf");
			rtfFilter.setDescription("RTF files (.rtf)");
			chooser.setFileFilter(rtfFilter);
	
			// source code filters
			final ExtensionFileFilter prgmFilter = new ExtensionFileFilter();
			prgmFilter.addExtension("java");
			prgmFilter.addExtension("cpp");
			prgmFilter.addExtension("c");
			prgmFilter.addExtension("sh");
			prgmFilter.addExtension("js");
			prgmFilter.addExtension("py");
			prgmFilter.setDescription(
					"Programming source code (" 
						+ TextPad.collectHighlightExtDotList() + ")");
			chooser.setFileFilter(prgmFilter);
	
			// Text! filters
			final ExtensionFileFilter txtFilter = new ExtensionFileFilter();
			txtFilter.addExtension("txt");
			txtFilter.setDescription("Text files (*.txt)");
			chooser.setFileFilter(txtFilter);
	
			chooser.setFileFilter(allFilter);

		}
		return chooser;
	}
	
	/** Gets the <code>updateForTextPad</code> flag, which tells
	 * whether the <code>TextPadChangeListener</code> should 
	 * update the UI and tab history based during a change event.
	 * @return true if the listener should respond
	 * events; false if the UI and tab updates will be taken care of
	 * independently
	 */
	public boolean getUpdateForTextPad() {
		return updateForTextPad;
	}
	
	
	/** Opens multiple files, waiting until the final file has been
	 * opened before updating the UI, but updating the tab
	 * history continuously.
	 * The <code>TextPadChangeListener</code> is uncoupled
	 * during this operation to prevent it from updating the
	 * UI and tab history after the fact, since the listener is
	 * on an <code>EvokeLater</code>.  The Text Pad will
	 * still be added, but the title and menu settings won't be
	 * updated until the last file, since the other files' updates will
	 * be replaced by each successively opened file, and they will 
	 * each update the UI whenever they are brought to focus.
	 * @param files the files to open
	 * @param offset starting index in the files array
	 * @param initialFiles if true, the files will be opened by 
	 * the openInitialFile method
	 * @see #openInitialFile
	 */
	public String openFiles(File[] files, int offset, boolean initialFiles) {
		String msg = "";
		
		// first uncouples the change listener from responding to
		// the Text Pad additions; need to uncouple when opening
		// multiple files at once because change listener is on an
		// EvokeLater and can't respond to text pads as they're added
		setUpdateForTextPad(false);
		
		// opens the files, starting from the offset
		boolean reuseTab = true;
		for (int i = offset; i < files.length; i++) {
			// opens the file if it exists
			boolean success = false;
			if (files[i].exists()) {
				// opens files according to whether the file is given 
				// on the command line/reopening or through 
				// user-interaction, eg the file dialog
				success = 
					initialFiles 
					? openInitialFile(files[i], reuseTab) 
					: openFile(files[i], true, false, reuseTab);
			}
			reuseTab = false;
			if (success) {
				// if successful, updates the tab history
				updateTabHistory(getSelectedTabbedPane());
			} else {
				// record unopened files
				msg = msg + files[i] + "\n";
			}
		}
		// updates the UI to reflect the last opened tab
		updateUIForTextPad(getSelectedTabbedPane(), getSelectedTextPad());
		
		// recouples the listener
		setUpdateForTextPad(true);
		
		return msg;
	}
	
	/** Opens multiple files from the given paths, waiting until the final file 
	 * has been opened before updating the UI, but updating the tab history 
	 * continuously.
	 * The <code>TextPadChangeListener</code> is uncoupled
	 * during this operation to prevent it from updating the
	 * UI and tab history after the fact, since the listener is
	 * on an <code>EvokeLater</code>.  The Text Pad will
	 * still be added, but the title and menu settings won't be
	 * updated until the last file, since the other files' updates will
	 * be replaced by each successively opened file, and they will 
	 * each update the UI whenever they are brought to focus.
	 * @param paths the paths of the files to open
	 * @param offset starting index in the files array
	 * @param initialFiles if true, the files will be opened by 
	 * the openInitialFile method
	 * @see #openInitialFile
	 */
	public String openFiles(String[] paths, int offset, boolean initialFiles) {
		File[] files = new File[paths.length];
		// converts paths to files, starting from the offset
		for (int i = offset; i < paths.length && paths[i] != null; i++) {
			files[i] = new File(paths[i]);
		}
		return openFiles(files, offset, initialFiles);
	}

	/**
	 * Responds to changes in the <code>TextPad</code> text areas. Updates the
	 * titles to reflect text alterations.
	 */
	private class TextPadDocListener implements DocumentListener {
		private TextPad pad = null;
		
		public TextPadDocListener(TextPad aPad) {
			pad = aPad;
		}
		/**
		 * Flags a text insertion.
		 * 
		 * @param e insertion event
		 */
		public void insertUpdate(DocumentEvent e) {
			setChanged();
		}

		/**
		 * Flags a text removal.
		 * 
		 * @param e removal event
		 */
		public void removeUpdate(DocumentEvent e) {
			setChanged();
		}

		/**
		 * Flags any sort of text change.
		 * 
		 * @param e any text change event
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
			if (!pad.getChanged()) {
				pad.setChanged(true);
				updateTabTitle(pad);
				if (getPrefs().getAutoSave()) {
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
								int choice = JOptionPane.showConfirmDialog(
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
							
							// saves the pad directly if it already exists;
							// otherwise, asks for a file path
							if (textPad.fileExists()) {
								saveFile(textPad.getPath(), textPad);
							} else {
								// asks users whether they would like to supply
								// a file
								// path rather than diving immediately and
								// cryptically
								// into the file save dialog
								int choice = JOptionPane.showConfirmDialog(
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
	
	/** Change listener for events in the {@link Text Pad}.
	 */
	private class TextPadChangeListener implements ChangeListener {
	
		private MotherTabbedPane pane = null; // tab group
		
		/** Constructs a listener. 
		 * @param aPane the tab group 
		 */
		public TextPadChangeListener(MotherTabbedPane aPane) {
			pane = aPane;
		}
		
		/** Updates the tab history, auto-wrap-indent, Line Dance dialog,
		 * and the focus of the Text Pad.
		 * @param evt the change event
		 */
		public void stateChanged(ChangeEvent evt) {
			final TextPad t = getSelectedTextPad();
//			System.out.println("TextPadChangeListener stateChanged");
			if (getUpdateForTextPad()) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						updateUIForTextPad(pane, t);
						updateTabHistory(pane);
					}
				});
			}
			// this second call is necessary for unknown reasons;
			// perhaps some events still follow the call in invokeLater
			// (above)
			if (t != null)
				t.requestFocusInWindow();
		}
	}
	
	
	
	
}
