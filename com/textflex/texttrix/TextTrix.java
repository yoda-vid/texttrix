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
 * Portions created by the Initial Developer are Copyright (C) 2002-4
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

//import java.lang.reflect.InvocationTargetException;
import java.net.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.print.*;
import javax.print.attribute.*;

/**The main Text Trix class.
 * Takes care of all basic graphical user interface operations, such as 
 * setting up and responding to changes in the <code>Text Pad</code>s, 
 * tool bar, menus, and dialogs.  Manages and mediates the plug-ins' actions.
 */
public class TextTrix extends JFrame {
	private static ArrayList textAreas = new ArrayList(); // all the TextPads
	private Container contentPane = getContentPane();
	private JMenuBar menuBar = null;
	private static JTabbedPane tabbedPane = null; // multiple TextPads
	private static JPopupMenu popup = null; // make popup menu
	private static JFileChooser chooser = null; // file dialog
	private static FileFilter allFilter = null; // TODO: may be unnecessary
	private static JCheckBoxMenuItem autoIndent = null;
	private static String openDir = ""; // most recently path opened to
	private static String saveDir = ""; // most recently path saved to
	private static int fileIndex = 0; // for giving each TextPad a unique name
	private static PlugIn[] plugIns = null; // plugins from jar archives
	private static Action[] plugInActions = null; // plugin invokers
	private JMenu trixMenu = null; // trix plugins
	private JMenu toolsMenu = null; // tools plugins
	private JToolBar toolBar = null; // icons
	private String toolsCharsUnavailable = ""; // chars for shorcuts
	private String trixCharsUnavailable = ""; // chars for shorcuts
	private int[] tabIndexHistory = new int[10]; // records for back/forward
	private int tabIndexHistoryIndex = 0; // index of next record
	private boolean updateTabIndexHistory = true; // flag to update the record
	private static Prefs prefs = null; // preferences
	private static Action prefsOkayAction = null; // prefs action signaling to accept
	private static Action prefsApplyAction = null; // prefs action signaling to immediately accept
	private static Action prefsCancelAction = null; // prefs action to reject
	private static boolean updateFileHist = false; // flag to update file history menu entries
	private static JMenu fileMenu = new JMenu("File"); // file menu, which incl file history
	private static int fileHistStart = -1; // starting position of file history in file menu
	private MenuBarCreator menuBarCreator = null; // menu and tool bar worker thread
	private FileHist fileHist = null; // file history
	private boolean tmpActivated = false;
	private HashPrintRequestAttributeSet printAttributes = 
		new HashPrintRequestAttributeSet();
	private PageFormat pageFormat = null;

	/** Constructs a new <code>TextTrix</code> frame and with
	<code>TextPad</code>s for each of the specified paths or at least
	one <code>TextPad</code>.
	 */
	public TextTrix(final String[] paths) {
		
		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
//				System.out.println("tmpActivated: " + tmpActivated);
				if (!getPrefs().getActivateWindowsTogether()) {
				} else if (isTmpActivated()) {
//					setTmpActivated(false);
/*
					Thread runner = new Thread() {
						public void run() {
							try {
								Thread.sleep(100);
								setTmpActivated(false);
							} catch(InterruptedException e) {
							}
						}
					};
					runner.start();
*/
				} else {
//					System.out.println("focusing");
					focusAllWindows();
				}
			}
			
			public void windowDeactivated(WindowEvent e) {
				setTmpActivated(true);
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
		LibTTx.setAcceleratedAction(
			prefsOkayAction,
			"Okay",
			'O',
			KeyStroke.getKeyStroke("alt O"));

		// creates an action that could store and apply preferences without closing the window;
		// the class, not the calling function, creates the action b/c no need to report
		// back to the calling function;
		// contrast "cancelAction", which requires the calling function to both dispose of
		// and destroy the object 
		prefsApplyAction = new AbstractAction("Apply now", null) {
			public void actionPerformed(ActionEvent evt) {
				if (continuePrefsUpdate()) {
					getPrefs().storePrefs();
					applyPrefs();
				}
			}
		};
		LibTTx.setAcceleratedAction(
			prefsApplyAction,
			"Apply the current tabs settings immediately",
			'A',
			KeyStroke.getKeyStroke("alt A"));

		// creates the reject action, something I'm all too familiar with
		prefsCancelAction = new AbstractAction("No way", null) {
			public void actionPerformed(ActionEvent evt) {
				prefs.dispose();
				prefs = null;
			}
		};
		LibTTx.setAcceleratedAction(
			prefsCancelAction,
			"Cancel",
			'N',
			KeyStroke.getKeyStroke("alt C"));
		getPrefs();

		/* Setup the main Text Trix window */

		setTitle("Text Trix");

		// restore window size and location
		setSize(getPrefs().getPrgmWidth(), getPrefs().getPrgmHeight());
		setLocation(
			new Point(getPrefs().getPrgmXLoc(), getPrefs().getPrgmYLoc()));

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
		ImageIcon im = LibTTx.makeIcon("images/minicon-32x32.png");
		if (im != null) {
			setIconImage(im.getImage());
		}

		/* Create the main Text Trix frame components */

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		// keep the tabs the same width when substituting chars
		tabbedPane.setFont(new Font("Monospaced", Font.PLAIN, 11));
		// initialize tabIndexHistory
		for (int i = 0; i < tabIndexHistory.length; i++)
			tabIndexHistory[i] = -1;

		/* adds a change listener to listen for tab switches and display
		 * the options of the tab's TextPad
		 */
		tabbedPane.addChangeListener(new ChangeListener() {
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
							int i = tabbedPane.getSelectedIndex();
							if (updateTabIndexHistory) {
								//System.out.println("Updating tab index history...");
								addTabIndexHistory(i);
							} else {
								updateTabIndexHistory = true;
							}
							updateTitle(t.getFilename());
							// doesn't work when creating new tabs via
							// the keyboard accelerator;
							// only works when changing between already created
							// tabs or creating new ones via the menu item
							t.requestFocusInWindow();
						}
					}
				});
				// this second call is necessary for unknown reasons;
				// perhaps some events still follow the call in invokeLater (above) 
				if (t != null)
					t.requestFocusInWindow();
			}
		});

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
		webFilter.setDescription(
			"Web files (*.html, *.htm, " + "*.xhtml, *.shtml, *.css, *.js)");
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
		prgmFilter.setDescription(
			"Programming source code (*.java, *.cpp, *.c)");
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

		// open the initial files;
		// must make sure that all of the operations do not require anything from
		// the menu or tool bars, which MenuBarCreator is the process of making
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				contentPane.add(tabbedPane, BorderLayout.CENTER);

				// make first tab and text area;
				// can only create after making several other user interface
				// components, such as the autoIndent check menu item
				addTextArea(textAreas, tabbedPane, makeNewFile());

				// load files specified at start from command-line
				if (paths != null) {
					for (int i = 0; i < paths.length; i++) {
						openInitialFile(paths[i]);
					}
				}

				// load files left open at the close of the last session
				if (getPrefs().getReopenTabs()) {
					// the list consists of a comma-delimited string of filenames
					StringTokenizer tokenizer =
						new StringTokenizer(
							getPrefs().getReopenTabsList(),
							",");
					while (tokenizer.hasMoreElements()) {
						openInitialFile(tokenizer.nextToken());
					}
				}
				//System.out.println("Adding files just opened");

				// make the file history menu entries and set the auto-indent check box
				syncMenus();
			}
		});
	}

	/**Publically executable starter method.
	 * Creates the <code>TextTrix</code> object, displays it,
	 * an makes sure that it will still undergo its
	 * exit routine when closed manually.
	 * @param args command-line arguments; not yet used
	 */
	public static void main(String[] args) {
		// set the look and feel;
		try {
			//	    System.out.println("java ver num: " + System.getProperty("java.vm.version") + ", " + System.getProperty("os.name"));

			/* JRE Bug 4275928, "RFE: Mnemonics for non-character keys are
			 * displayed incorectly in the tooltip"
			 * (http://developer.java.sun.com/developer/bugParade/bugs/435928.html)
			 * notes that tool tips often append "Alt-" to the shortcut key, even if the
			 * shortcut should be for "Ctrl-" or another modifier key.  In particular,
			 * Actions in JMenus display the correct accelerator key in both the tool tip
			 * and the on the right side of the menu item, but JToolBars show
			 * the accelerator key as using "Alt-" instead of the true modifier.
			 * To avoid confusing the user, the pseudo-workaround is to simply
			 * prevent the tool tips from showing the accelerator keys.  More extensive
			 * workarounds subclass several swing and java package classes.
			 */
			UIManager.getDefaults().put(
				"ToolTipUI",
				"javax.swing.plaf.basic.BasicToolTipUI");

			/* GTK+ look and feel for Java v.1.4.2 running on Linux,
			 * otherwise the default look and feel, such as the new XP 
			 * interface for Microsoft Windows XP systems with Java v.1.4.2.
			 * According to http://java.sun.com/j2se/1.4.2/docs/guide/swing/1.4/Post1.4.html, 
			 * UIManager.getSystemLookAndFeelClassName() will return GTK+ by default
			 * in Java v.1.5.
			 */
			if (System.getProperty("os.name").equals("Linux")
				&& System.getProperty("java.vm.version").indexOf("1.4.2") != -1) {
				// GTK+ only for available systems
				/*
				UIManager.setLookAndFeel("com.sun.java.swing.plaf"
							 + ".gtk.GTKLookAndFeel");
				*/
				//	    } else if (System.getProperty("mrj.version") != null) {
				/*
			} else if (System.getProperty("java.vm.version").indexOf("1.5") 
				!= -1) {
				// the new Java v.1.5.0, Tiger release, contains a revamped
				// Swing look-and-feel called "Ocean," implemented by default
				 */
			} else { // default interface
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			//	    SwingUtilities.updateComponentTreeUI(TextTrix.this);
		} catch (Exception e) {
			//	    e.printStackTrace();
			String msg =
				"Sorry, couldn't find that look-and-feel."
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
		});
		textTrix.setTmpActivated(true);
		//textTrix.show(); DEPRECATED as of JVM v.1.5.0
		textTrix.setVisible(true);

		/* Something apparently grabs focus after he tabbed pane ChangeListener
		 * focuses on the selected TextPad.  Calling focus after displaying the 
		 * window seems to restore this focus at least most of the time.
		 */
		focuser();
	}
	

	/** Synchronizes the menus with the current text pad settings.
	 * Creates the file history menu entries in the File menu
	 * and flags the auto-indent check box according to the 
	 * current text pad's setting.
	 *
	 */
	public void syncMenus() {
		if (fileHistStart != -1) {
			fileHist.start(fileMenu); // assumes fileHistStart is up-to-date
		}
		setAutoIndent();
	}

	/** Opens the given file.
	 * Useful for opening files at program start-up because only gives 
	 * command-line feedback if the file cannot be opened; the user
	 * may not have expected the opening in the first place and
	 * thus does not have to be needlessly concerned.
	 * @param path path to file
	 */
	private void openInitialFile(String path) {
		if (!openFile(new File(path))) {
			String msg =
				"Sorry, but "
					+ path
					+ " can't be read.\n"
					+ "Is it a directory?  Does it have the right "
					+ "permsissions for reading?";
			System.out.println(msg);
		}
	}

	/**Switches focus synchronously to the selected <code>TextPad</code>,
	 * if one exists. 
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

	/** Updates the main Text Trix frame's heading.
	 * Window managers often use the title to display in taskbar icons.  The updater is
	 * useful to display the name of the currently selected file, for example.  This name
	 * is automatically appended to the front of the text, " - Text Trix".
	 * @param frame the Text Trix window frame
	 * @param filename name of given file, such as the currently displayed one
	 */
	public static void updateTitle(JFrame frame, String filename) {
		String titleSuffix = " - Text Trix";
		frame.setTitle(filename + titleSuffix);
	}

	/** Updates the main Text Trix frame's heading.
	 * Window managers often use the title to display in taskbar icons.  The updater is
	 * useful to display the name of the currently selected file, for example.  This name
	 * is automatically appended to the front of the text, " - Text Trix".
	 * @param filename name of given file, such as the currently displayed one
	 */
	public void updateTitle(String filename) {
		String titleSuffix = " - Text Trix";
		setTitle(filename + titleSuffix);
	}

	/** Gets the preferences panel object.
	 * Creates a new object if necessary, such as after the old panel has
	 * been cancelled and thus set to <code>null</code>.
	 * Assumes that the necessary "okay," "apply," and "cancel"
	 * actions have already been created.
	 * @return the preferences panel object
	 */
	public static Prefs getPrefs() {
		return (prefs == null) ? prefs =
			new Prefs(
				prefsOkayAction,
				prefsApplyAction,
				prefsCancelAction) : prefs;
	}

	/** Applies the settings from the preferences panel.
	 * Reloads the plug-ins, applies the preferences from the General and Shorts tabs,
	 * and creates new menu and tool bars.  Front end for several functions that
	 * update the program to match the user's preferences settings.
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
//		getPrefs().setPlugInsPrefsChanged(false);
	}

	/** Applies preferences from the General tab in the preferences panel.
	 * 
	 *
	 */
	public void applyGeneralPrefs() {
		fileHist.start(fileMenu);
	}

	/** Applies preferences from the Shorts tab in the preferences panel.
	 * Relies on a separate call to <code>#menuBarCreator.start()</code>
	 * to complete the shortcuts update.  For example, <ocde>applyPrefs()</code>
	 * calls the method to create the menu bar.
	 *
	 */
	public void applyShortsPrefs() {
		//System.out.println("Applying shorts...");
		if (prefs.isHybridKeybindings()) {
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				getTextPadAt(i).hybridKeybindings();
			}
		} else if (prefs.isEmacsKeybindings()) {
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				getTextPadAt(i).emacsKeybindings();
			}
		} else {
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				getTextPadAt(i).standardKeybindings();
			}
		}
	}

	public boolean continuePrefsUpdate() {

//		if (getPrefs().getPlugInsPrefsChanged()) {
		for (int i = 0; i < plugIns.length; i++) {
			if (plugIns[i].isWindowVisible()) {
				int choice = JOptionPane.showConfirmDialog(
					getPrefs(),
					"Some plug-in windows may be closed.  Keep on going?",
					"Electricity...",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
				if (choice == JOptionPane.YES_OPTION) {
					return true;
				} else {
					return false;
				}
			}
		}
		return true;
	}

	/** Creates a plugin action.
	 * Allows the plugin to be invoked from a button or other action-capable
	 * interface.
	 * @param pl plugin from which to make an action
	*/
	public void makePlugInAction(final PlugIn pl) {
		// assumes prefs' includes is udpated
/*
		String[] includes = getPrefs().getIncludePlugInsNames();
		if (!getPrefs().getAllPlugIns()
			&& !LibTTx.inUnsortedList(pl.getName(), includes))
			return;
*/
		String[] includes = getPrefs().getIncludePlugInsList();
/*
		for (int i = 0; i < includes.length; i++) {
			System.out.println("includes[" + i + "]: " + includes[i]);
		}
*/
		if (!getPrefs().getAllPlugIns()
			&& !LibTTx.inUnsortedList(pl.getPath(), includes)) {
			System.out.println(pl.getPath());
			return;
		}
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
		
		if (pl instanceof PlugInWindow) {
			WindowAdapter winAdapter = new WindowAdapter() {
				public void windowActivated(WindowEvent e) {
					if (!prefs.getActivateWindowsTogether()) {
					} else if (pl.isTmpActivated()) {
/*
//						pl.setTmpActivated(false);
						Thread runner = new Thread() {
							public void run() {
								try {
//									System.out.println("or here");
									Thread.sleep(100);
									pl.setTmpActivated(false);
								} catch(InterruptedException e) {
								}
							}
						};
						runner.start();
*/
					} else {
//						System.out.println("here");
						focusAllWindows(pl);
					}
				}
			};
			pl.setWindowAdapter(winAdapter);
//			System.out.println(pl.getName());
			pl.addWindowAdapter();
		}

		// action to start the plug in, such as invoking its options
		// panel if it has one;
		// invokes the plugin's text manipulation on the current TextPad's text
		Action startAction = new AbstractAction(name, icon) {
			public void actionPerformed(ActionEvent evt) {
				pl.startPlugIn();
			}
		};

		// add the action to the appropriate menu
		if (category.equalsIgnoreCase("tools")) {
			toolsCharsUnavailable =
				LibTTx.setAction(
					startAction,
					name,
					description,
					toolsCharsUnavailable);
			toolsMenu.add(startAction);
		} else {
			trixCharsUnavailable =
				LibTTx.setAction(
					startAction,
					name,
					description,
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

	/** Run a text-manipulating plug-in on the selected text pad's text.
	 * If a given region is selected, the plug-in will only work on that
	 * area, unless the plug-in's <code>alwaysEntireText</code> variable
	 * is <code>true</code>.  If so, the plug-in will receive the entire
	 * body of text as well as the positions of selected text.  The plug-in
	 * receives the entire body but only the caret position when no
	 * text is selected.
	 * @param pl plugin to invoke
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

			// only modify the selected text, and make 
			// the action undoable
			int start = t.getSelectionStart();
			int end = t.getSelectionEnd(); // at the first unselected character
			//System.out.println("selectionStart: " + start + ", selectionEnd: " + end);
			PlugInOutcome outcome = null;
			try {
				// determines whether a region is selected or not;
				// if not, works on the text pad's entire text
				if (start == end || pl.getAlwaysEntireText()) { // no selection
					text = doc.getText(0, doc.getLength()); // all the text

					// invokes the plugin: start and ending selection positions
					// when "alwaysEntireText" b/c want to both get all of 
					// the text and show its highlighted portion,
					// rather than only getting that part of the text
					//System.out.println(text);
					outcome =
						pl.getAlwaysEntireText()
							? pl.run(text, start, end)
							: pl.run(text, end);
					//System.out.println("I'm there");

					// if the plug-in flags that it has not changed the text, don't even try to do so
					if (!outcome.getNoTextChange()) {
						doc.remove(0, doc.getLength()); // remove all the text
						doc.insertString(0, outcome.getText(), null);
						// insert text
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
					outcome = pl.run(text);// invoke the plugin

					// if the plug-in flags that it has not changed the text, don't even try to do so
					if (!outcome.getNoTextChange()) {
						// remove only the region
						doc.remove(start, len);
						// insert text
						doc.insertString(start, outcome.getText(), null);
					}
					// caret automatically returns to end of selected region
					int i = -1;
					if ((i = outcome.getSelectionStart()) != -1)
						textSelection(t, start, i, outcome.getSelectionEnd());
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	/** Selects the given region of text.
	 * Works on the given text pad.
	 * @param t given text pad, not necessarily the selected one, though probably only
	 * relevant if so
	 * @param baseline starting point from which to measure <code>start</code>
	 * and <cdoe>end</code>
	 * @param start beginning point for selection
	 * @param end end point for selection
	 */
	public void textSelection(TextPad t, int baseline, int start, int end) {
		if (end != -1) {
			t.setCaretPosition(baseline + start);
			t.moveCaretPosition(baseline + end);
			t.getCaret().setSelectionVisible(true);
			// to ensure selection visibility
		} else {
			t.setCaretPosition(baseline + start);
		}
	}
	
	/*
	public void activatePlugInWindow(final PlugIn pl) {
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					pl.activateWindow();
				} 
			});
		} catch(InterruptedException e) {
		} catch(java.lang.reflect.InvocationTargetException e) {
		}
		
	}
	*/
	
	public void focusAllWindows(PlugIn pl) {
//		if (pl.isActivated()) return;
		for (int i = 0; i < plugIns.length; i++) {
			if (plugIns[i] != pl && plugIns[i] instanceof PlugInWindow) {
				plugIns[i].setTmpActivated(true);
//				activatePlugInWindow(pl);
				plugIns[i].activateWindow();
//				plugIns[i].setActivated(false);
			} 
		}
		setTmpActivated(true);
		toFront();
		pl.setTmpActivated(true);
		pl.activateWindow();
//		pl.setActivated(false);
//		System.out.println("hey there");
	}
	
	public void focusAllWindows() {
//		System.out.println("plugIns.length = " + plugIns.length);
		for (int i = 0; i < plugIns.length; i++) {
//			System.out.println("plugIn[" + i + "]");
			if (plugIns[i] instanceof PlugInWindow) {
				plugIns[i].setTmpActivated(true);
			/*
			try {
				System.out.println("sleeping...");
				Thread.sleep(500);
			} catch (Exception e) {
			}
			*/
				plugIns[i].activateWindow();
			}
		}
		setTmpActivated(true);
		toFront();
//		System.out.println("hi there");
	}
	
	public void setTmpActivated(boolean b) {
		if (tmpActivated = b) {
			Thread runner = new Thread() {
				public void run() {
					try {
//						System.out.println("main waiting");
						Thread.sleep(500);
						tmpActivated = false;
					} catch(InterruptedException e) {
					}
				}
			};
			runner.start();
		}
	}
	
	public boolean isTmpActivated() {
		return tmpActivated;
	}
	
	/** Reloads all the plug-ins in the current <code>plugins</code> folder.
	 * Prepares the preferences panel to offer all available plug-ins.
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
					//System.out.println("Couldn't find " + list[i]);
					extraPlugs[extraPlugsInd++] = LibTTx.loadPlugIn(list[i]);
				}
			}
			// check for loaded but now missing plug-in files
			for (int i = 0; i < plugIns.length; i++) {
				if (LibTTx.inUnsortedList(plugIns[i].getPath(), list)) {
					extraPlugs[extraPlugsInd++] = plugIns[i];
				}
			}
			/* TODO: May need to create a temporary collection of all loaded plug-ins
			 * rather than adding the extraPlugs to the official, active plugIns group.
			 */
			plugIns = (PlugIn[])LibTTx.truncateArray(extraPlugs, extraPlugsInd);
			for (int i = 0; i < plugIns.length; i++) {
				plugIns[i].closeWindow();
			}
//			getPrefs().updatePlugInsPanel(getPlugInNames());
		}
	}
	
	public void refreshPlugInsPanel() {

		// determine the available plug-ins
		File file = getPlugInsFile();
		String[] list = LibTTx.getPlugInPaths(file);
		getPrefs().updatePlugInsPanel(list);
	}

	/* For some reason this fn sporadically doesn't work;
	 * eg stats in Search plug-in doesn't give the correct
	 * selectionEnd value from TextTrix, and find/replace
	 * calls runPlugIn twice and entered some sort of
	 * infinite loop once.  My guess is that makePlugInAction
	 * somehow creates duplicate actions that get called twice.
	 * As usual, the first call in Search removes the highlighting,
	 * causing the second call to occasionally treat the text
	 * as if it were never highlighted.  The actions run in
	 * parallel, meaning that some actions' second call will
	 * miss the highlighting while others are fast enough to
	 * still see it.
	 */
	/*
	public void refreshPlugIns() {
		reloadPlugIns();
		if (plugIns != null) {
			for (int i = 0; i < plugIns.length; i++) {
				makePlugInAction(plugIns[i]);
			}
		}
	}
	*/
	
	/** Gets the names of the currently loaded plug-ins.
	 * Each plug-in has a descriptive name, usually different from the filename.
	 * @return array of all the loaded plug-ins' descriptive name; the array's length
	 * is equal to the number off names
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

	/** Gets the paths of all currently loaded plug-ins.
	 * 
	 * @return array of all the loaded plug-in's paths and length equal to the number
	 * of these paths
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

	/** Loads and set up the plugins.
	Retrieves them from the "plugins" directory, located in the same
	directory as the executable JAR for TextTrix.class or the 
	"com" directory in the "com/textflex/texttrix" sequence holding
	TextTrix.class.
	TODO: also search the user's home directory or user determined
	locations, such as ones a user specifies via a preferences panel.
	*/
	public void setupPlugIns() {
		File plugInsFile = getPlugInsFile(); //refreshPlugIns();

		// load the plugins and create actions for them
		plugIns = LibTTx.loadPlugIns(plugInsFile);
//		getPrefs().updatePlugInsPanel(getPlugInNames());
		getPrefs().updatePlugInsPanel(getPlugInPaths());
		if (plugIns != null) {
			for (int i = 0; i < plugIns.length; i++) {
//				plugIns[i].removeWindowAdapter();
				makePlugInAction(plugIns[i]);
			}
		}
	}
	
	/** Gets the <code>plugins</code> folder file.
	 * TODO: Add preferences mechanism to specify alternative or additional
	 * <code>plugins</code> folder location, such as a permanent storage place
	 * to reuse plug-ins after installing a new version of Text Trix.
	 * @return <code>plugins</code> folder
	 */
	public File getPlugInsFile() {
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

		// this class's location
		String relClassLoc = "com/textflex/texttrix/TextTrix.class";
		URL urlClassDir = ClassLoader.getSystemResource(relClassLoc);
		String strClassDir = urlClassDir.getPath();
		// to check whether JAR
		File fileClassDir = new File(urlClassDir.getPath());
		File baseDir = null;
		// move into JAR's parent directory only if launched from a JAR
		if (strClassDir.indexOf(".jar!/" + relClassLoc) != -1) {
			baseDir =
				fileClassDir
					.getParentFile()
					.getParentFile()
					.getParentFile()
					.getParentFile()
					.getParentFile();
		} else { // not from JAR; one less parent directory
			baseDir =
				fileClassDir
					.getParentFile()
					.getParentFile()
					.getParentFile()
					.getParentFile();
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
				strBaseDir =
					strBaseDir.substring(0, space)
						+ " "
						+ strBaseDir.substring(space + 3);
			} else {
				strBaseDir = strBaseDir.substring(0, space) + " ";
			}
		}
		/* Though simpler, this alternative solution crashes 
		   after a NoSuchMethodError under JRE <= 1.3.
		*/
		/*
		baseDir = new File(baseDir.toString().replaceAll("%20", " "));
		File plugInsFile = new File(baseDir, "plugins");
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

		/* A possible workaround for an apparent JRE v.1.4 bug that
		   fails to open files with spaces in their paths.
		   This workaround converts any file or directory names
		   with their "8.3" formatted equivalents.
		   For example, "Program Files" is converted to 
		   "PROGRA~1", which some systems might map to the intended file.
		*/
		/*
		if (!plugInsFile.exists()) {
			String seg = "";
			StringTokenizer tok = new StringTokenizer(plugInsPath, "/\\");
			StringBuffer buf = new StringBuffer(plugInsPath.length());
			for (int i = 0; tok.hasMoreTokens(); i++) {
			seg = tok.nextToken();
			if (seg.length() > 8) 
				seg = seg.substring(0, 6).toUpperCase() + "~1";
			buf.append(File.separator + seg);
			}
			plugInsPath = buf.toString();
			plugInsFile = new File(plugInsPath); // the actual file
			//	    System.out.println(plugInsPath);
		}
		*/
		return plugInsFile;
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
	
	/** Gets the <code>TextPad</code> at a given index in the tabbed pane.
	 * 
	 * @param i index of <code>TextPad</code>
	 * @return the given Text Pad; <code>null</code> if the tabbed pane
	 * lacks the index value
	 */
	public static TextPad getTextPadAt(int i) {
		if (i < -1 || i >= tabbedPane.getTabCount())
			return null;
		return (TextPad) (textAreas.get(i));
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
		   that it continues to point to the next available position.
		   "tabIndexHistoryIndex - 1" now refers to the current tab,
		   while "tabIndexHistoryIndex - 2" refers to the last tab, the 
		   one to return to while going "Back".  The "Back" method must
		   therefore not only decrement tabIndexHistoryIndex, but also
		   refer to the position preceding the new index.
		*/
		boolean repeat = true;
		boolean shift = false;
		//	System.out.println("mostRecent: " + mostRecent + "; tabIndexHistoryIndex: " + tabIndexHistoryIndex);
		for (int i = 0; i < tabIndexHistoryIndex && repeat; i++) {
			// shift the records as necessary to move a potential
			// duplicate to the front of the history
			if (shift) { // shift the records
				if (tabIndexHistory[i] == -1) {
					repeat = false;
				} else {
					tabIndexHistory[i - 1] = tabIndexHistory[i];
				}
			} else { // find where to start shifting, if necessary
				if (tabIndexHistory[i] == mostRecent) {
					shift = true;
				} else if (tabIndexHistory[i] == -1) {
					repeat = false;
				}
			}
		}
		// add the tab selection
		if (shift) { // add the potential duplicate to the front of the record
			tabIndexHistory[--tabIndexHistoryIndex] = mostRecent;
		} else if (tabIndexHistoryIndex >= tabIndexHistory.length) {
			// increase the array size if necessary
			tabIndexHistory = (int[])LibTTx.growArray(tabIndexHistory);
			tabIndexHistory[tabIndexHistoryIndex] = mostRecent;
		} else if (tabIndexHistoryIndex >= 0) {
			// ensure that the tab during TextTrix's startup has no entry;
			// otherwise, the 0 tab selection index duplicates
			tabIndexHistory[tabIndexHistoryIndex] = mostRecent;
		}
		for (int i = ++tabIndexHistoryIndex;
			i < tabIndexHistory.length;
			i++) {
				tabIndexHistory[i] = -1;
		}
		//tabIndexHistoryIndex++;
		/*
		for (int i = 0; i < tabIndexHistoryIndex; i++) {
			System.out.println("tabIndexHistory[" + i + "]: " + tabIndexHistory[i]);
		}
		*/
		//System.out.println("tabIndexHistoryIndex: " + tabIndexHistoryIndex);
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
				if (i <= tabIndexHistoryIndex)
					--tabIndexHistoryIndex;
				shift = true;
			}
			// shift the tab record
			if (shift) {
				tabIndexHistory[i] =
					(i < tabIndexHistory.length - 1)
						? tabIndexHistory[i + 1]
						: -1;
			}
			// decrease tab indices for those above the that of the removed tab
			if (tabIndexHistory[i] > removed) {
				tabIndexHistory[i] = --tabIndexHistory[i];
			}
			//	    System.out.print(tabIndexHistory[i] + ",");
		}
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
	public static void setSaveDir(String aSaveDir) {
		saveDir = aSaveDir;
	}
	
	/** Flags the menu check box to indicate whether the current
	 * Text Pad is in auto-indent mode.
	 *
	 */
	public static void setAutoIndent() {
		TextPad t = getSelectedTextPad();
		if (autoIndent != null && t != null) {
			autoIndent.setSelected(t.getAutoIndent());
		}
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
			fileIndex++;
			// ensures that no repeat of name in current session
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
			if (in == null)
				return false;
			reader = new BufferedReader(new InputStreamReader(in));
			String text = readText(reader); // retrieve the text
			addTextArea(textAreas, tabbedPane, new File(path));
			TextPad t = getSelectedTextPad();
			t.setEditable(false); // so appropriate for read-only
			t.setText(text);
			t.setChanged(false);
			updateTabTitle(textAreas, tabbedPane);
			t.setCaretPosition(0);
			return true;
		} finally { // stream closure operations
			try {
				if (reader != null)
					reader.close();
				if (in != null)
					in.close();
			} catch (IOException e) {
				return false;
			}
		}
	}

	/**Exits <code>TextTrix</code> by closing each tab individually,
	 * checking for unsaved text areas in the meantime.
	 */
	public static boolean exitTextTrix() {
		// closes each tab individually, using the function that checks
		// for unsaved changes
		String openedPaths = "";
		boolean reopenTabs = getPrefs().getReopenTabs();
		//if (i <= 0 && reopenTabs) {
		getPrefs().storeReopenTabsList("");
		//}
		//int i = 0;
		boolean b = true;
		int totTabs = tabbedPane.getTabCount();
		
		// close the files and prepare to store their paths in the list
		// of files left open at the end of the session
		while (totTabs > 0 && b) {
			if (reopenTabs) {
				TextPad t = getTextPadAt(0);
				if (t.fileExists()) {
					if (!openedPaths.equals("")) {
						openedPaths = openedPaths + "," + t.getPath();
					} else {
						openedPaths = t.getPath();
					}
				}
			}
			b = closeTextArea(0, textAreas, tabbedPane);
			totTabs = tabbedPane.getTabCount();
		}
		
		// store the file list and exit Text Trix all the files closed successfully
		if (b == true) {
			if (reopenTabs) {
				getPrefs().storeReopenTabsList(openedPaths);
			}
			System.exit(0);
		}
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
	public static boolean closeTextArea(
		int tabIndex,
		ArrayList textAreas,
		JTabbedPane tabbedPane) {
		boolean successfulClose = false;

		TextPad t = (TextPad)textAreas.get(tabIndex);
		// check if unsaved text area
		if (t.getChanged()) {
			String s = "Please save first.";
			tabbedPane.setSelectedIndex(tabIndex);
			// dialog with 3 choices: save, discard, cancel
			String msg =
				"This file has not yet been saved."
					+ "\nWhat would you like me to do with this new version?";
			int choice =
				JOptionPane.showOptionDialog(
					null,
					msg,
					"Save before close",
					JOptionPane.WARNING_MESSAGE,
					JOptionPane.DEFAULT_OPTION,
					null,
					new String[] { "Save", "Toss it out", "Cancel" },
					"Save");
			switch (choice) {
				// save the text area's contents
				case 0 :
					// bring up "Save as..." dialog if never saved file before
					if (t.fileExists()) {
						successfulClose = saveFileOnExit(t.getPath());
					} else {
						// still closes tab if cancel "Save as..." dialog
						// may need to change in future releases
						successfulClose = fileSaveDialogOnExit(null);
					}
					if (successfulClose) {
						removeTextArea(tabIndex, textAreas, tabbedPane);
					}
					break;
					// discard the text area's contents
				case 1 :
					removeTextArea(tabIndex, textAreas, tabbedPane);
					successfulClose = true;
					break;
					// cancel the dialog and return unsuccessful closure;
					// could likely remove default case as well as case 2's break
				case 2 :
					successfulClose = false;
					break;
				default :
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

	/**Read in text from a file and return the text as a string.
	 * Differs from <code>displayFile(String)</code> because
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
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		// no stream closure operations b/c assume calling function takes care of them
		return text;
	}

	/**Creates a new <code>TextPad</code> object, a text area 
	 * for writing, and gives it a new tab.  Can call for
	 * each new file; names the tab, <code>Filen.txt</code>,
	 * where <code>n</code> is the tab number.
	 */
	public void addTextArea(
		ArrayList arrayList,
		JTabbedPane tabbedPane,
		File file) {
		TextPad textPad = new TextPad(file, getPrefs());

		JScrollPane scrollPane =
			new JScrollPane(
				textPad,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		DocumentListener listener = new TextPadDocListener();

		// must add to array list before adding scroll pane to tabbed pane
		// or else get IndexOutOfBoundsException from the array list
		arrayList.add(textPad);
		// 1 more than highest tab index since will add tab
		int i = tabbedPane.getTabCount();
		tabbedPane.addTab(file.getName() + " ", scrollPane);
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
	public static void updateTabTitle(
		ArrayList arrayList,
		JTabbedPane tabbedPane) {
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
		updateTabTitle(textAreas, tabbedPane);
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
	public void read(TextPad textPad, Reader in, Object desc)
		throws IOException {
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

	/**Saves text area contents to a given path.
	 * @param path file path in which to save
	 * @return true for a successful save, false if otherwise
	 */
	public boolean saveFile(String path) {
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
				// open the stream to write to
				out = new PrintWriter(new FileWriter(path), true);
				// write to it
				out.print(t.getText());
				t.setChanged(false);
				t.setFile(path);
				updateTabTitle(textAreas, tabbedPane);
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

	public static boolean saveFileOnExit(String path) {
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
				/* t.getText() != null, even if have typed nothing in it. Add tab and set its text 
				 * if no tabs exist or if current tab has tokens; set current tab's text otherwise.
				*/
				if (t == null || !t.isEmpty()) { // open file in new pad
					addTextArea(textAreas, tabbedPane, file);
					t = (TextPad)textAreas.get(tabbedPane.getSelectedIndex());
					read(t, reader, path);
				} else { // open file in current, empty pad
					read(t, reader, path);
				}
				t.setCaretPosition(0);
				t.setChanged(false);
				t.setFile(path);
				tabbedPane.setToolTipTextAt(
					tabbedPane.getSelectedIndex(),
					t.getPath());
				updateTabTitle(textAreas, tabbedPane);
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

	/** Automatically auto-indents the given Text Pad.
	 * Determines whether the Text Pad's filename extension matches the 
	 * user-defined list of files to automatically auto-indent.
	 * @param t Text Pad whose file is to be checked
	 */
	public void autoAutoIndent(TextPad t) {
		String path = t.getPath();
		if (getPrefs().getAutoIndent() && isAutoIndentExt(path)) {
			t.setAutoIndent(true);
		}
	}
	
	/** Checks if the given file extension is in the user-defined list of files
	 * to automatically auto-indent.
	 * @param path file to check
	 * @return <code>true</code> if the file's extension is in the list
	 * @see #autoAutoIndent(TextPad)
	 */
	public boolean isAutoIndentExt(String path) {
		int extIndex = LibTTx.reverseIndexOf(path, ".", path.length()) + 1;
		if (extIndex < 0 || extIndex >= path.length())
			return false;
		String ext = path.substring(extIndex);
		//System.out.println("Ext: " + ext);
		StringTokenizer tokenizer =
			new StringTokenizer(getPrefs().getAutoIndentExt(), " ,.");
		String token = "";
		while (tokenizer.hasMoreTokens()) {
			if (tokenizer.nextElement().equals(ext))
				return true;
		}
		return false;
	}

	/** Evokes a save dialog, with a filter for text files.
	Sets the tabbed pane tab to the saved file name.
	@param owner parent frame; can be null
	@return true if the approve button is chosen, false if otherwise
	 */
	public static boolean fileSaveDialogOnExit(JFrame owner) {
		if (!prepFileSaveDialog())
			return false;
		return getSavePathOnExit(owner);
	}

	/** Evokes a save dialog, with a filter for text files.
	Sets the tabbed pane tab to the saved file name.
	@param owner parent frame; can be null
	@return true if the approve button is chosen, false if otherwise
	 */
	public boolean fileSaveDialog(JFrame owner) {
		if (!prepFileSaveDialog())
			return false;
		return getSavePath(owner);
	}

	/** Prepares the file save dialog.
	 * Finds the current directory if the file and selects it, if the file has already 
	 * been saved.  If not, returns to the most recent directory in the current session
	 * and selects no file.
	 * @return <code>true</code> if a Text Pad is selected, necessary to save a file 
	 */
	public static boolean prepFileSaveDialog() {
		//	int tabIndex = tabbedPane.getSelectedIndex();
		TextPad t = getSelectedTextPad();
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

	/** Helper function to <code>fileSaveDialog</code> when exiting Text Trix.
	 * Unlike <code>getSavePath(JFrame)</code>, this method does not
	 * attempt to update the graphical components, currently in
	 * the process of closing.
	Opens the file save dialog to retrieve the file's new name.
	If the file will overwrite another file, prompts the user
	with a dialog box to determine whether to continue with the 
	overwrite, get another name, or cancel the whole operation.
	@param owner the frame to which the dialog will serve; can be null
	@return true if the file is saved successfully
	@see #getSavePath(JFrame)
	 */
	private static boolean getSavePathOnExit(JFrame owner) {
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
					String overwrite =
						path + "\nalready exists.  Should I overwrite it?";
					String[] options =
						{ "But of course", "Please, no!", "Cancel" };
					// dialog warning of a possible overwrite
					choice =
						JOptionPane.showOptionDialog(
							owner,
							overwrite,
							"Overwrite?",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null,
							options,
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
						String msg =
							path
								+ " couldn't be written to "
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

	/** Helper function to <code>fileSaveDialog</code>.
	Unlike <code>getSavePathOnExit(JFrame)</code>, this method
	attempts to update the graphical components.
	Opens the file save dialog to retrieve the file's new name.
	If the file will overwrite another file, prompts the user
	with a dialog box to determine whether to continue with the 
	overwrite, get another name, or cancel the whole operation.
	@param owner the frame to which the dialog will serve; can be null
	@return true if the file is saved successfully
	@see #getSavePathOnExit(JFrame)
	*/
	private boolean getSavePath(JFrame owner) {
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
					String overwrite =
						path + "\nalready exists.  Should I overwrite it?";
					String[] options =
						{ "But of course", "Please, no!", "Cancel" };
					// dialog warning of a possible overwrite
					choice =
						JOptionPane.showOptionDialog(
							owner,
							overwrite,
							"Overwrite?",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null,
							options,
							options[1]);
				}
				if (choice == 1) {
					// don't overwrite, but choose another name
					repeat = true;
				} else if (choice == 2) { // don't overwrite.
					return false;
				} else { // write, even if overwriting
					// try to save the file and check if successful
					if (saveFile(path)) { // success
						setSaveDir(chooser.getSelectedFile().getParent());
						// update graphical components
						tabbedPane.setToolTipTextAt(
							tabbedPane.getSelectedIndex(),
							path);
						updateTitle(owner, f.getName());
						getPrefs().storeFileHist(path);
						fileHist.start(fileMenu);

						return true;
					} else { // fail; request another try at saving
						String msg =
							path
								+ " couldn't be written to "
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
	private static boolean yesNoDialog(
		JFrame owner,
		String msg,
		String title) {
		int choice =
			JOptionPane.showConfirmDialog(
				owner,
				msg,
				title,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		// true for Yes, false for No
		if (choice == JOptionPane.YES_OPTION) {
			return true;
		} else {
			return false;
		}
	}
	
	public void printTextPad() {
		try {
			PrinterJob job = PrinterJob.getPrinterJob();
			Book bk = createBook();
			if (bk == null) return;
			job.setPageable(bk);
			if (job.printDialog(printAttributes)) {
				job.print(printAttributes);
//				System.out.println("printed!");
//				pad.setPrintText(null);
			}
		} catch (PrinterException e) {
			JOptionPane.showMessageDialog(this, e);
		}
	}
	
	public Book createBook() {
		TextPad textPad = getSelectedTextPad();
		if (textPad == null) return null;
/*
		System.out.println("font fam: " + textPad.getFont().getFamily()
			+ ", font name: " + textPad.getFont().getName()
			+ ", font size: " + textPad.getFont().getSize());
*/
		if (pageFormat == null) {
			PrinterJob job = PrinterJob.getPrinterJob();
			pageFormat = job.defaultPage();
		}
		PrintPad pad = textPad.createPrintPad();
		Book bk = new Book();
		int pp = pad.getPageCount((Graphics2D)getGraphics(), pageFormat);
		bk.append(pad, pageFormat, pp);
		return bk;
	}
	
	public void printPreview() {
		Book bk = createBook();
		if (bk == null) return;
		PrintPadPreview preview = new PrintPadPreview(this, bk);
		preview.setVisible(true);
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
			if (t != null && (dir = t.getDir()).equals(""))
				dir = openDir;
			//	    System.out.println("dir: " + dir);
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
						msg =
							"The following files couldn't be opened:\n"
								+ msg
								+ "Would you like to try again?";
						// request another chance to open them or other files
						repeat = yesNoDialog(owner, msg, title);
					}
					fileHist.start(fileMenu);
					setAutoIndent();
					/* Original workaround.
					   Utilizes the fact that getSelectedFiles() returns an 
					   array of length 0, which getSelectedFile() returns the
					   intended file object.
					*/
					/*
					if (files.length == 0) {
						File f1 = chooser.getSelectedFile();
						System.out.println(f1.getPath());
						// OLDDO: dialog informing that the file doesn't exist
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
			} while (repeat);
			// repeat if failed opens for user to retry
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
	
	private class FileCloseAction extends AbstractAction {
		
		public FileCloseAction(String name, Icon icon) {
			putValue(Action.NAME, name);
			putValue(Action.SMALL_ICON, icon);
		}
		
		public void actionPerformed(ActionEvent evt) {
			int i = tabbedPane.getSelectedIndex();
			if (i >= 0) {
				updateTabIndexHistory = false;
				removeTabIndexHistory(i);
				updateTabIndexHistory = true;
				closeTextArea(i, textAreas, tabbedPane);
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
			((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).setChanged(
				true);
			updateTabTitle(textAreas, tabbedPane);
		}

		/**Flags a text removal.
		 * @param e removal event
		 */
		public void removeUpdate(DocumentEvent e) {
			((TextPad)textAreas.get(tabbedPane.getSelectedIndex())).setChanged(
				true);
			updateTabTitle(textAreas, tabbedPane);
		}

		/**Flags any sort of text change.
		 * @param e any text change event
		 */
		public void changedUpdate(DocumentEvent e) {
		}
	}

	/** Listener to pop up a context menu when right-clicking.
	 * 
	 * @author davit
	 */
	private class PopupListener extends MouseAdapter {
		/** Press right mouse button.
		 * 
		 */
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		/** Release right mouse button.
		 * 
		 */
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	/** Creates the menu bar and its associated tool bar through a worker
	 * thread to build concurrently with other processes.
	 * No other method should rely upon the components that this
	 * class creates to be available until sufficient time after the
	 * thread starts.
	 * @author davit
	 */
	private class MenuBarCreator extends Thread {
		
		/** Begins creating the bars.
		 * 
		 */
		public void start() {
			(new Thread(this, "thread")).start();
		}

		/** Performs the menu and associated bars' creation.
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
					KeyStroke newActionShortcut = KeyStroke.getKeyStroke("ctrl N");
					char closeActionMnemonic = 'C';
					KeyStroke closeActionShortcut = KeyStroke.getKeyStroke("ctrl W");
					String exitActionTxt = "Exit";
					char exitActionMnemonic = 'X';
					KeyStroke exitActionShortcut = KeyStroke.getKeyStroke("ctrl Q");
					char cutActionMnemonic = 'T';
					KeyStroke cutActionShortcut = KeyStroke.getKeyStroke("ctrl X");
					KeyStroke redoActionShortcut = KeyStroke.getKeyStroke("ctrl Y");
					char copyActionMnemonic = 'C';
					KeyStroke copyActionShortcut = KeyStroke.getKeyStroke("ctrl C");
					char pasteActionMnemonic = 'P';
					KeyStroke pasteActionShortcut = KeyStroke.getKeyStroke("ctrl V");
					char selectAllActionMnemonic = 'A';
					KeyStroke selectAllActionShortcut = KeyStroke.getKeyStroke("ctrl A");
					char printActionMnemonic = 'P';
					KeyStroke printActionShortcut = KeyStroke.getKeyStroke("ctrl P");
					char printPreviewActionMnemonic = 'R';

					// Alternate keybindings: shortcuts added to and with
					// preference over the standard shortcuts
					// TODO: add Mac-sytle shortcuts
					if (prefs.isHybridKeybindings()) {
						// Hybrid: standard + Emacs for single char and line navigation
						fileMenuMnemonic = 'I';
						newActionMnemonic = 'T';
						newActionShortcut = KeyStroke.getKeyStroke("ctrl T");
						exitActionTxt = "Exit";
						exitActionMnemonic = 'X';
						exitActionShortcut = KeyStroke.getKeyStroke("ctrl Q");
						selectAllActionMnemonic = 'L';
						selectAllActionShortcut =
							KeyStroke.getKeyStroke("ctrl L");
						printActionShortcut = KeyStroke.getKeyStroke("ctrl shift P");
					} else if (prefs.isEmacsKeybindings()) {
						// Emacs: Hybrid + Emacs single-key shortcuts
						// TODO: create double-key shortcuts, such as ctrl-x, ctrl-s for saving
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
						selectAllActionShortcut =
							KeyStroke.getKeyStroke("ctrl L");			
						printActionShortcut = KeyStroke.getKeyStroke("ctrl shift P");				
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
					toolBar.addMouseListener(new PopupListener());
					toolBar.setBorderPainted(false);
					
					// create pop-up menu for right-mouse-clicking
					popup = new JPopupMenu();






					/* File menu items */

					// make new tab and text area
					Action newAction = new AbstractAction("New") {
						public void actionPerformed(ActionEvent evt) {
							addTextArea(textAreas, tabbedPane, makeNewFile());
						}
					};
					LibTTx.setAcceleratedAction(
						newAction,
						"New",
						newActionMnemonic,
						newActionShortcut);
					fileMenu.add(newAction);

					// (ctrl-o) open file; use selected tab if empty
					Action openAction =
						new FileOpenAction(
							TextTrix.this,
							"Open",
							LibTTx.makeIcon("images/openicon-roll-16x16.png"));
					LibTTx.setAcceleratedAction(
						openAction,
						"Open",
						'O',
						KeyStroke.getKeyStroke("ctrl O"));
					fileMenu.add(openAction);

					Action openActionForBtn =
						new FileOpenAction(
							TextTrix.this,
							"Open",
							LibTTx.makeIcon("images/openicon-16x16.png"));
					LibTTx.setAction(openActionForBtn, "Open file(s)", 'O');
					JButton openButton = toolBar.add(openActionForBtn);
					openButton.setBorderPainted(false);
					LibTTx.setRollover(
						openButton,
						"images/openicon-roll-16x16.png");

					// close file; check if saved
					Action closeAction =
						new FileCloseAction(
							"Close",
							LibTTx.makeIcon("images/closeicon-16x16.png"));
					/*
						new AbstractAction(
							"Close",
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
					*/
					LibTTx.setAcceleratedAction(
						closeAction,
						"Close",
						closeActionMnemonic,
						closeActionShortcut);
					fileMenu.add(closeAction);

					Action closeActionForBtn = 
						new FileCloseAction(
							"Close",
							LibTTx.makeIcon("images/door-60deg-16x16.png"));
					LibTTx.setAction(closeActionForBtn, "Close file", 'W');
					JButton closeButton = toolBar.add(closeActionForBtn);
					closeButton.setBorderPainted(false);
					LibTTx.setRollover(
						closeButton,
						"images/closeicon-16x16.png");

					// (ctrl-s) save file; no dialog if file already created
					Action saveAction =
						new AbstractAction(
							"Save",
							LibTTx.makeIcon("images/saveicon-16x16.png")) {
						public void actionPerformed(ActionEvent evt) {
							TextPad t = getSelectedTextPad(); //null;
							// can't use tabbedPane.getSelectedComponent() b/c returns JScrollPane
							if (t != null) {
								if (t.fileExists()) {
									if (!saveFile(t.getPath())) {
										String msg =
											t.getPath()
												+ " couldn't be written.\n"
												+ "Would you like to try saving it somewhere else?";
										String title = "Couldn't write";
										if (yesNoDialog(TextTrix.this,
											msg,
											title))
											fileSaveDialog(TextTrix.this);
									}
									// otherwise, request filename for new file
								} else {
									fileSaveDialog(TextTrix.this);
								}
							}
						}
					};
					LibTTx.setAcceleratedAction(
						saveAction,
						"Save file",
						'S',
						KeyStroke.getKeyStroke("ctrl S"));
					fileMenu.add(saveAction);
					JButton saveButton = toolBar.add(saveAction);
					saveButton.setBorderPainted(false);
					LibTTx.setRollover(
						saveButton,
						"images/saveicon-roll-16x16.png");
						
					// Tool Bar: begin plug-ins
					toolBar.addSeparator();

					// save w/ file save dialog
					Action saveAsAction =
						new FileSaveAction(
							TextTrix.this,
							"Save as...",
							LibTTx.makeIcon("images/saveasicon-16x16.png"));
					LibTTx.setAction(saveAsAction, "Save as...", '.');
					fileMenu.add(saveAsAction);
					
					// Menu: begin print entries
					fileMenu.addSeparator();
					
					Action printAction = new AbstractAction("Print...") {
						public void actionPerformed(ActionEvent e) {
							printTextPad();
						}
					};
					LibTTx.setAcceleratedAction(
						printAction,
						"Print...",
						printActionMnemonic,
						printActionShortcut);
					fileMenu.add(printAction);


					Action printPreviewAction = new AbstractAction("Print preview...") {
						public void actionPerformed(ActionEvent e) {
							printPreview();
						}
					};
					LibTTx.setAction(
						printAction,
						"Print...",
						printPreviewActionMnemonic);
					fileMenu.add(printPreviewAction);
					
					
					// Menu: begin exit entries
					fileMenu.addSeparator();

					// exit file; close each tab separately, checking for saves
					Action exitAction = new AbstractAction(exitActionTxt) {
						public void actionPerformed(ActionEvent evt) {
							exitTextTrix();
						}
					};
					// Doesn't work if close all tabs unless click ensure window focused, 
					// such as clicking on menu
					LibTTx.setAcceleratedAction(
						exitAction,
						exitActionTxt,
						exitActionMnemonic,
						exitActionShortcut);
					fileMenu.add(exitAction);

					fileMenu.addSeparator();
					//System.out.println("About to create the menu entries");
					
					
					
					
					
					

					/* Edit menu items */

					// (ctrl-z) undo; multiple undos available
					Action undoAction = new AbstractAction("Undo") {
						public void actionPerformed(ActionEvent evt) {
							((TextPad)textAreas
								.get(tabbedPane.getSelectedIndex()))
								.undo();
						}
					};
					LibTTx.setAcceleratedAction(
						undoAction,
						"Undo",
						'U',
						KeyStroke.getKeyStroke("ctrl Z"));
					editMenu.add(undoAction);

					// redo; multiple redos available
					Action redoAction = new AbstractAction("Redo") {
						public void actionPerformed(ActionEvent evt) {
							((TextPad)textAreas
								.get(tabbedPane.getSelectedIndex()))
								.redo();
						}
					};
					LibTTx.setAcceleratedAction(
						redoAction,
						"Redo",
						'R',
						redoActionShortcut);
					editMenu.add(redoAction);

					// Begin Cut, Copy, Paste entries;
					// create here instead of within TextPad so can use as menu entries
					editMenu.addSeparator();

					// cut
					Action cutAction = new AbstractAction("Cut") {
						public void actionPerformed(ActionEvent evt) {
							((TextPad)textAreas
								.get(tabbedPane.getSelectedIndex()))
								.cut();
						}
					};
					LibTTx.setAcceleratedAction(
						cutAction,
						"Cut",
						cutActionMnemonic,
						cutActionShortcut);
					editMenu.add(cutAction);
					popup.add(cutAction);

					// copy
					Action copyAction = new AbstractAction("Copy") {
						public void actionPerformed(ActionEvent evt) {
							((TextPad)textAreas
								.get(tabbedPane.getSelectedIndex()))
								.copy();
						}
					};
					LibTTx.setAcceleratedAction(
						copyAction,
						"Copy",
						copyActionMnemonic,
						copyActionShortcut);
					editMenu.add(copyAction);
					popup.add(copyAction);

					// paste
					Action pasteAction = new AbstractAction("Paste") {
						public void actionPerformed(ActionEvent evt) {
							((TextPad)textAreas
								.get(tabbedPane.getSelectedIndex()))
								.paste();
						}
					};
					LibTTx.setAcceleratedAction(
						pasteAction,
						"Paste",
						pasteActionMnemonic,
						pasteActionShortcut);
					editMenu.add(pasteAction);
					popup.add(pasteAction);

					// Start selection items
					editMenu.addSeparator();

					// select all text in current text area
					Action selectAllAction = new AbstractAction("Select all") {
						public void actionPerformed(ActionEvent evt) {
							((TextPad)textAreas
								.get(tabbedPane.getSelectedIndex()))
								.selectAll();
						}
					};
					LibTTx.setAcceleratedAction(
						selectAllAction,
						"Select all",
						selectAllActionMnemonic,
						selectAllActionShortcut);
					editMenu.add(selectAllAction);
					popup.add(selectAllAction);

					// edit menu preferences separator
					editMenu.addSeparator();

					// auto-indent
					// apply the selection to the current TextPad
					Action autoIndentAction =
						new AbstractAction("Auto indent the selected file") {
						public void actionPerformed(ActionEvent evt) {
							TextPad t = getSelectedTextPad();
							if (t != null)
								t.setAutoIndent(autoIndent.isSelected());
						}
					};
					LibTTx.setAcceleratedAction(
						autoIndentAction,
						"Automatically repeat tabs with the next line",
						'I',
						KeyStroke.getKeyStroke("alt shift I"));
					autoIndent = new JCheckBoxMenuItem(autoIndentAction);
					editMenu.add(autoIndent);
					
					// Preferences panel starter;
					// also reloads the plug-ins
					Action prefsAction =
						new AbstractAction("It's your preference...") {
						public void actionPerformed(ActionEvent evt) {
//							reloadPlugIns();
							refreshPlugInsPanel();
							getPrefs().setVisible(true);//show();
						}
					};
					LibTTx.setAction(
						prefsAction,
						"It's your preference...",
						'Y');
					editMenu.add(prefsAction);

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
					
					   The default Java key-bindings for tab switchin no longer apply since 
					   the focus switches automatically to the newly selected tab.
					*/

					// (ctrl-shift-[) switch back in the tab history
					Action backTabAction = new AbstractAction("Back") {
						/* Switch back only up through the first record and  keep from recording
						 * the past selected tabs as newly selected one.  The current index always 
						 * refers to the next available position to add selections, while the previous 
						 * index refers to the current selection.  To go back, the value at two 
						 * positions back must be checked.
						 */
						public void actionPerformed(ActionEvent evt) {
							if (--tabIndexHistoryIndex >= 1) {
								// uncouple the tab index history while switching
								// to the past tabs -- leave the tabs as a
								// trail until a new tab is chosesn
								updateTabIndexHistory = false;
								//System.out.println("tabIndexHistoryIndex: " + tabIndexHistoryIndex + "; tabIndexHistory[]: " + tabIndexHistory[tabIndexHistoryIndex]);
								tabbedPane.setSelectedIndex(
									tabIndexHistory[tabIndexHistoryIndex - 1]);
								//System.out.println("Re-allowing tab history updates...");
							} else { // reset the index to its orig val, -1
								++tabIndexHistoryIndex;
							}
						}
					};
					LibTTx.setAcceleratedAction(
						backTabAction,
						"Back",
						'B',
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
								&& (i = tabIndexHistory[tabIndexHistoryIndex - 1])
									!= -1) {
								// uncouple the history, preserving it as a 
								// trail of selections past and future from 
								// the current position
								updateTabIndexHistory = false;
								//System.out.println("tabIndexHistoryIndex: " + tabIndexHistoryIndex + "; tabIndexHistory[]: " + i);
								tabbedPane.setSelectedIndex(i);
								//updateTabIndexHistory = true;
							} else {
								--tabIndexHistoryIndex;
							}
						}
					};
					LibTTx.setAcceleratedAction(
						forwardTabAction,
						"Forward",
						'F',
						KeyStroke.getKeyStroke(
							"ctrl shift " + "CLOSE_BRACKET"));
					viewMenu.add(forwardTabAction);

					// (ctrl-[) switch to the preceding tab
					Action prevTabAction =
						new AbstractAction("Preceeding tab") {
						public void actionPerformed(ActionEvent evt) {
							int tab = tabbedPane.getSelectedIndex();
							if (tab > 0) {
								tabbedPane.setSelectedIndex(tab - 1);
							} else if (tab == 0) {
								tabbedPane.setSelectedIndex(
									tabbedPane.getTabCount() - 1);
							}
						}
					};
					LibTTx.setAcceleratedAction(
						prevTabAction,
						"Preeceding tab",
						'P',
						KeyStroke.getKeyStroke("ctrl OPEN_BRACKET"));
					viewMenu.add(prevTabAction);

					// (ctrl-]) switch to the next tab
					Action nextTabAction = new AbstractAction("Next tab") {
						public void actionPerformed(ActionEvent evt) {
							int tab = tabbedPane.getSelectedIndex();
							if ((tab != -1)
								&& (tab == tabbedPane.getTabCount() - 1)) {
								tabbedPane.setSelectedIndex(0);
							} else if (tab >= 0) {
								tabbedPane.setSelectedIndex(tab + 1);
							}
						}
					};
					LibTTx.setAcceleratedAction(
						nextTabAction,
						"Next tab",
						'N',
						KeyStroke.getKeyStroke("ctrl CLOSE_BRACKET"));
					viewMenu.add(nextTabAction);

					viewMenu.addSeparator();

					// view as plain text
					Action togglePlainViewAction =
						new AbstractAction("Toggle plain text view") {
						public void actionPerformed(ActionEvent evt) {
							viewPlain();
						}
					};
					LibTTx.setAction(
						togglePlainViewAction,
						"View as plain text",
						'A');
					viewMenu.add(togglePlainViewAction);

					// view as HTML formatted text
					Action toggleHTMLViewAction =
						new AbstractAction("Toggle HTML view") {
						public void actionPerformed(ActionEvent evt) {
							viewHTML();
						}
					};
					LibTTx.setAction(toggleHTMLViewAction, "View as HTML", 'H');
					viewMenu.add(toggleHTMLViewAction);

					// view as RTF formatted text
					Action toggleRTFViewAction =
						new AbstractAction("Toggle RTF view") {
						public void actionPerformed(ActionEvent evt) {
							viewRTF();
						}
					};
					LibTTx.setAction(toggleRTFViewAction, "View as RTF", 'R');
					viewMenu.add(toggleRTFViewAction);
					
					
					
					
					
					

					/* Help menu items */

					// about Text Trix, incl copyright notice and version number
					Action aboutAction =
						new AbstractAction(
							"About...",
							LibTTx.makeIcon("images/minicon-16x16.png")) {
						public void actionPerformed(ActionEvent evt) {
							String text = readText("about.txt");
							String iconPath = "images/texttrixsignature.png";
							JOptionPane.showMessageDialog(
								null,
								text,
								"About Text Trix",
								JOptionPane.PLAIN_MESSAGE,
								LibTTx.makeIcon(iconPath));
						}
					};
					LibTTx.setAction(aboutAction, "About...", 'A');
					helpMenu.add(aboutAction);

					// TODO: add dialog to point to web doc files if local ones missing

					// shortcuts description; opens new tab;
					// reads from "shortcuts.txt" in same dir as this class
					Action shortcutsAction = new AbstractAction("Shortcuts") {
						public void actionPerformed(ActionEvent evt) {
							String path = "shortcuts.txt";
							displayFile(path);
						}
					};
					LibTTx.setAction(shortcutsAction, "Shortcuts", 'S');
					helpMenu.add(shortcutsAction);

					// features descriptions; opens new tab;
					// reads from "features.txt" in same dir as this class
					Action featuresAction =
						new AbstractAction("Features descriptions") {
						public void actionPerformed(ActionEvent evt) {
							String path = "features.txt";
							displayFile(path);
						}
					};
					LibTTx.setAction(
						featuresAction,
						"Features descriptions",
						'F');
					helpMenu.add(featuresAction);

					// license; opens new tab;
					// reads from "license.txt" in same directory as this class
					Action licenseAction = new AbstractAction("License") {
						public void actionPerformed(ActionEvent evt) {
							String path = "license.txt";
							displayFile(path);
						}
					};
					LibTTx.setAction(licenseAction, "License", 'L');
					helpMenu.add(licenseAction);
					
					
					
					
					
					
					

					/* Trix and Tools menus */

					// Load plugins; add to appropriate menu
					setupPlugIns();
/*
*/					
					
					
					
					

					/* Place menus and other UI components */
					
					// must add tool bar before set menu bar lest tool bar shortcuts 
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
					/*
					for (int i = 0; i < plugIns.length; i++) {
						System.out.println(": " + i);
						final PlugIn pl = plugIns[i];
						try {
						EventQueue.invokeAndWait(new Runnable() {
							public void run() {

									if (pl.isWindowVisible()) {
										pl.reloadWindow();
									}
							}
							
						});
						} catch(InvocationTargetException e) {
							
						} catch(InterruptedException e) {
						}
					}
					*/
				}
			});
		}
	}
	
	/** Worker thread class to update the file history entries.
	 * @author davit
	 *
	 */
	private class FileHist extends Thread {
		JMenu menu = null;

		/** Starts creating the entries within the given menu.
		 * 
		 * @param aMenu menu to add file history entries
		 */
		public void start(JMenu aMenu) {
			menu = aMenu;
			 (new Thread(this, "thread")).start();
		}

		/** Updates the file history record and menu entries.
		 * 
		 */
		public void run() {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					updateFileHist();
				}
			});
		}

		/** Creates the file history menu entries.
		 * 
		 *
		 */
		public void createFileHist() {
			// assumes that the file history entries are at the entries in the menu
			String[] files = getPrefs().retrieveFileHist();
			for (int i = 0; i < files.length; i++) {
				String file = files[i];
				Action fileAction = createFileHistAction(file);
				menu.add(fileAction);
			}
		}

		/** Creates the actions to add to the history menu.
		 * 
		 * @param file file to open when invoking the action
		 * @return action to open the given file
		 */
		public Action createFileHistAction(final String file) {
			String fileDisp = file;
			int pathLen = file.length();
			if (pathLen > 30) {
				fileDisp =
					file.substring(0, 10)
						+ "..."
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
		
		/** Updates the file history menu by deleting old entries and
		 * replacing them with the current ones.
		 * Assumes that <code>fileHistStart</code> in <code>TextTrix</code>
		 * has been set.
		 *
		 */
		public void updateFileHist() {
			for (int i = menu.getItemCount() - 1; i >= fileHistStart; i--) {
				menu.remove(i);
			}
			createFileHist();
			menu.revalidate();

			/* Attempt to delete specific menu entries rather than updating
			 * the whole history.  Not yet working.
			 */
			/*
			String[] files = getPrefs().retrieveFileHist();
			if (files.length == 0) {
				fileHistCount = 0;
				return;
			}
			String file = files[0];
			System.out.println("Adding " + file + " to the menu");
			JMenuItem item = null;
			int countDiff = fileHistCount - files.length;
			if (countDiff > 0) {
				for (int i = 0; i < countDiff; i++) {
					menu.remove(menu.getItemCount() - 1);
				}
			} else {
				int i = 0;
				int totItems = menu.getItemCount();
				int newPos = totItems - fileHistCount;
				if (fileHistCount != 0) {
					while (++i <= fileHistCount 
						&& !((item = menu.getItem(totItems - i)).getText().equals(file)));
					if (i <= fileHistCount) {
						menu.remove(totItems - i);
						menu.insert(item, newPos);
					} else if (countDiff == 0) {
						menu.remove(totItems - 1);
						menu.insert(createFileHistAction(file), newPos);
					}
				} else {
					menu.insert(createFileHistAction(file), newPos);
				}
			}
			fileHistCount = files.length;
			*/

			// assumes that the file history entries are at the entries in the menu
			//int i = 0;
			/*
			for (int i = menu.getItemCount() - 1; i < fileHistCount; i++) {
				menu.remove(menu.getItemCount() - 1);
			}
			createFileHist(menu);
			*/
		}

	}
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
