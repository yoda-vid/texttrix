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
 * Portions created by the Initial Developer are Copyright (C) 2002-11
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
import javax.swing.undo.*;
import javax.swing.text.*;
import javax.swing.JPopupMenu;
import java.io.*;
import javax.swing.event.*;
import java.beans.*;

import com.inet.jortho.FileUserDictionary;
import com.inet.jortho.SpellChecker;

import jsyntaxpane.DefaultSyntaxKit;

/**The writing pad, complete with keyboard shortcuts, auto-wrap indent
 * functions, and text sytle changes.
 * 
 * <p>The auto-wrap indent graphically indents text according to tabs at the
 * beginning of each newline.  Wrapped lines graphically indent along
 * with the first, tabbed line, but without any alteration to the underlying
 * text.  Auto-wrap indent mode consists of two distinct components&#151;
 * the auto- and wrap-indents, but remain integrated as a single feature
 * because of their complementary function.  While wrap-indent graphically
 * aligns word-wrapped text, auto-indent automatically adds tabs to the start
 * of new lines.  Both functions are useful in coding, such as Java 
 * programming, but especially in tagging, such as HTML, where each
 * newline may contain several screenlines of text.  It would usually
 * be useful to have auto-indented code also automatically wrap-indented
 * to perpetuate the tab structure graphically.
 * 
 * <p>By keeping shortcuts within the pad, they need not affect behavior
 * program-wide.  For example, if a program integrates a spreadsheet as well
 * as the pad, the pad's shortcuts keep out of the way of the spreadsheet.
 */
public class TextPad extends JTextPane implements StateEditable {
	private File file; // the file that the pad displays
	private boolean changed = false; // flag that text changed
	private boolean ignoreChanged = false;
	private String path; // file's path
	// allows for multiple and ignored undo operations
	private UndoManagerTTx undoManager = new UndoManagerTTx();
	private Hashtable actions; // table of shortcut actions
	private InputMap imap = null; // map of keyboard inputs
	private ActionMap amap = null; // map of actions
	private boolean autoIndent = false; // flag to auto-indent the text
	private int tabSize = 4; // default tab display size
	private StoppableThread autoSaveTimer = null; // timer to auto save the text
	private CompoundEdit compoundEdit = null; // group editing tasks for single undo
	private boolean compoundEditing = false; // flags whether editing as group
	private JScrollPane scrollPane = null; // the scroll pane that houses this pad
	private LineDancePanel lineDancePanel = null; // the Line Dance panel
	private FileModifiedThread fileModifiedThread = null;
	private DocumentListener docListener = null;
	private DocumentFilter docFilter = null;
	private WrappedPlainView wrappedView = null;
	private String eol = null;
	
	/**Constructs a <code>TextPad</code> that includes a file
	 * for the text area.
	 * @param aFile file to which the <code>TextPad</code> will
	 * save its text area contents
	 */
	// TODO: doesn't really need prefs from calling function;
	// should instead create simple Prefs object to access all the saved values
	public TextPad(File aFile, Prefs prefs) {
		// TODO: decide whether to check JVM within each TextPad or only once,
		// within TextTrix, with ways to check TextTrix or pass as a parameter 
		
		file = aFile;
		setFileModifiedThread(new FileModifiedThread(this));
		getFileModifiedThread().start();
		
		// Create Line Dance panel
		
		// Run the plug-in if the user hits "Enter" in components with this adapter
		KeyAdapter takeMeEnter = new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
					lineDance();
					evt.consume();
				}
			}
		};
		// makes the panel
		lineDancePanel = new LineDancePanel(takeMeEnter);
		// adds thh mouse listener
		lineDancePanel.addTableMouseListener(new LineDanceMouseListener());
		
		// adds dran-n-drop support for text
		setDragEnabled(true);
		
		// to allow multiple undos and listen for events
		// for new key-bindings
		imap = getInputMap(JComponent.WHEN_FOCUSED);
		amap = getActionMap();
		createActionTable(this);
		
		/* WORKAROUND:
		 * The paragraph that follows a word-wrapped line may overlap it.  With each
		 * line that wraps, the following paragraph for some reason gets less and less
		 * space between it and the previous paragraph.  Apparently this problem
		 * exists solely in the Windows L&F at certain font size and apparently for 
		 * specific font(s).  See
		 *  http://forum.java.sun.com/thread.jspa?forumID=57&threadID=613396
		 * for the proposed workaround.
		*/
//		setFont(getFont().deriveFont(11.5f));
		// TODO: set font face and size in prefs
		setFont(new Font("Arial,SansSerif", Font.PLAIN, 11));
		
		
		// (ctrl-backspace) delete from caret to current word start
		// First discard JTextComponent's usual dealing with ctrl-backspace.
		// (enter) Auto-indent if Text Trix's option selected.
		addKeyListener(new KeyAdapter() {
			
			/** Responds to whatever the current key combination maps to.
			 * Backspaces have already been processed in JVM >= v.1.5.
			*/
			public void keyTyped(KeyEvent event) {
				char keyChar = event.getKeyChar();
				if (event.isControlDown()
					&& keyChar == KeyEvent.VK_BACK_SPACE) {
					// TODO: may not be necessary, at least w/ JVM >= v.1.4.2
					event.consume();
				} else if (autoIndent && keyChar == KeyEvent.VK_ENTER) {
					autoIndent();
				} else if (
					autoIndent
						&& keyChar == KeyEvent.VK_TAB
						&& isLeadingTab()) {
					// performs the action after adding the tab
					indentCurrentParagraph(getTabSize());
				}
			}
			
			/**Responds to key events right after the key is pressed.
			 * Unlike keyTyped(KeyEvent), backspaces have not yet
			 * been processed, for both JVM < v.1.5 and == v.1.5.
			 * 
			 */
			public void keyPressed(KeyEvent evt) {
				int keyCode = evt.getKeyChar();
				// respond to key presses, which have not yet
				// processed the event
				if (autoIndent
					&& keyCode == KeyEvent.VK_BACK_SPACE
					&& isLeadingTab()) {
					// no longer should JVM_15 b/c the behavior also applies
					// to < JVM v.1.4.2 in keyPressed
					indentCurrentParagraph(getTabSize(), true);
				} else if (autoIndent 
					&& keyCode == KeyEvent.VK_TAB
					&& evt.isShiftDown()) {
					// Unindent action has been shifted to the universal shortcuts
					// action map.  Even though keyPressed doesn't invoke the action,
					// still need to grab the keystroke so that it won't be passed on
					// to the TAB action.
					
				} else if (autoIndent 
					&& keyCode == KeyEvent.VK_TAB
					&& getSelectionStart() != getSelectionEnd()) {
					// tab entire region
					// Unindent code needs to be placed in the action map
					// because shift+TAB is normally captured by the 
					// focusing mechanism
					
//					System.out.println("tab");
					evt.consume();
					try {
						tabRegion();
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			}

		});
		// applies the user specified set of keybindings
		applyKeybindings(prefs);
		
		// creates a styled document only for certain file extensions
		applyDocumentSettings();
	}
	
	/**
	 * Creates actions for popup menus, such as copy, cut, and paste.
	 */
	public ArrayList createPopupActions() {
		ArrayList list = new ArrayList();
		
		// cut
		Action cutAction = new AbstractAction("Cut") {
			public void actionPerformed(ActionEvent evt) {
				cut();
			}
		};
		list.add(cutAction);
		
		// copy
		Action copyAction = new AbstractAction("Copy") {
			public void actionPerformed(ActionEvent evt) {
				copy();
			}
		};
		list.add(copyAction);
		// paste
		Action pasteAction = new AbstractAction("Paste") {
			public void actionPerformed(ActionEvent evt) {
				paste();
			}
		};
		list.add(pasteAction);
		// select all text in current text area
		Action selectAllAction = new AbstractAction("Select all") {
			public void actionPerformed(ActionEvent evt) {
				selectAll();
			}
		};
		list.add(selectAllAction);
		return list;
	}
	
    /**
     * Enable or disable the popup menu with the menu item "Orthography" and "Languages". 
     * @param text the JTextComponent that should change
     * @param enable true, enable the feature.
     */
    public void enablePopup(boolean enable ){
        if( enable ){
            final JPopupMenu menu = new JPopupMenu();

			// adds popup menu items from the TextPad
			ArrayList list = createPopupActions();
			for (int i = 0; i < list.size(); i++) {
				menu.add((Action)list.get(i));
			}
							
			// adds the spell checker menu items
            addMouseListener( new TextPadPopupListener(menu) );
        } else {
			MouseListener[] listeners = getMouseListeners();
            for (int i = 0; i < listeners.length; i++){
                if(listeners[i] instanceof TextPadPopupListener){
                    removeMouseListener( listeners[i] );
                }
            }
        }
    }
    
	/** Sets the syntax highlighting style for the current file
	 * according to the file's extension, if possible.
	 * If the file has no extension, a ".txt" extension, or 
	 * a content type other than plain text, no highlight
	 * styling is applied.  Assumes that {@link #applyDocumentSettings}
	 * and any other document settings, such as TextPad
	 * document listeners, will be re-applied to the new
	 * styled document.
	 * @return the styled, highlighted document
	 */
	public void setHighlightStyle(boolean spellChecker) {
		// detects the file extension and returns if the document
		// either requires no styling or has a content type that
		// requires its own formatting
		String ext = getFileExtension();
		ext = ext.toLowerCase();
		if (ext.equals("") || ext.equals("txt") ) {
		
      		// enable the spell checking on the text component with all 
			// features; turn on spell checker only for plain text documents 
			// since most other code will have numerous non-detected words;
			// TODO: apply spell-checker more broadly if add more
			// varied dictionaries
			if (spellChecker) SpellChecker.register(this);
			return;
		}
		
		// prepares to transfer text into new styled document, which
		// will automatically style the text
		String text = getAllText();
		SpellChecker.unregister(this);

		
		// sets the appropriate style
		DefaultSyntaxKit.setWrapped(false);
		if (ext.equals("java")) {
			setContentType("text/java");
		} else if (ext.equals("c")) {
			setContentType("text/c");
		} else if (ext.equals("cpp")) {
			setContentType("text/cpp");
		} else if (ext.equals("html") || ext.equals("htm") 
				|| ext.equals("xhtml")) {
			DefaultSyntaxKit.setWrapped(true);
			setContentType("text/xhtml");
			DefaultSyntaxKit kit = (DefaultSyntaxKit)getEditorKit();
			setWrappedView((WrappedPlainView)kit.getView());
			kit.deinstallComponent(this, 
					"jsyntaxpane.components.LineNumbersRuler");
		} else if (ext.equals("js")) {
			setContentType("text/js");
		} else if (ext.equals("groovy")) {
			setContentType("text/groovy");
		} else if (ext.equals("bash") || ext.equals("sh")) {
			setContentType("text/bash");
		} else if (ext.equals("json")) {
			setContentType("text/json");
		} else if (ext.equals("xml")) {
			setContentType("text/xml");
		} else if (ext.equals("sql")) {
			setContentType("text/sql");
		} else if (ext.equals("properties")) {
			setContentType("text/properties");
		} else if (ext.equals("py")) {
			setContentType("text/python");
		} else if (ext.equals("tal")) {
			setContentType("text/tal");
		} else if (ext.equals("jflex")) {
			setContentType("text/jflex");
		} else if (ext.equals("ruby")) {
			setContentType("text/ruby");
		} else if (ext.equals("scala")) {
			setContentType("text/scala");
		} else if (ext.equals("clojure")) {
			setContentType("text/clojure");
		} else if (ext.equals("bat")) {
			setContentType("text/dosbatch");
		} else if (ext.equals("xpath")) {
			setContentType("text/xpath");
		} else if (ext.equals("lua")) {
			setContentType("text/lua");
		} else {
			setContentType("text/plain");
			// defaults to Java style
		}
		// transfers the text into the appropriately styled document
		setText(text);
		enablePopup(true);
		
		// still need to add undo manager to the new document
		// via applyDocumentSettings;
		// note that it will cause all previous edits to be unavailable
		
	}
	
	/**
	 * Gets or makes a DocumentFilter for detecting changes in numbers of 
	 * lines and making the corresponding updates to recorded line numbers
	 * in the LineDancePanel.
	 */
	private DocumentFilter getOrMakeDocFilter() {
		return (docFilter != null) ? docFilter : new DocumentFilter() {
			public void insertString(DocumentFilter.FilterBypass fb, int offset,
					String s, AttributeSet attr) {
				try {
					int origNum = getLineNumber();
					super.insertString(fb, offset, s, attr);
					// updates line numbers if the one or more lines have been
					// added to the document
					int newNum = getLineNumber();
					if (newNum > origNum) {
// 						System.out.println("new line added");
						lineDancePanel.updateLineNumber(origNum, newNum);
					}
				} catch(BadLocationException e) {
					e.printStackTrace();
				}
			}
			
			public void remove(DocumentFilter.FilterBypass fb, int offset,
					int length) {
				try {
					int origNum = getLineNumber();
					super.remove(fb, offset, length);
					int newNum = getLineNumber();
					// updates line numbers if the one or more lines have been
					// removed from the document
					if (newNum < origNum) {
// 						System.out.println("line removed");
						lineDancePanel.updateLineNumber(origNum, newNum);
					}
				} catch(BadLocationException e) {
					e.printStackTrace();
				}
				
			}
			
			public void replace(DocumentFilter.FilterBypass fb, int offset,
					int length, String s, AttributeSet attr) {
				try {
					int origNum = getLineNumber();
					super.replace(fb, offset, length, s, attr);
					int newNum = getLineNumber();
					// updates line numbers if the one or more lines have been
					// added or removed from the documents
					if (newNum != origNum) {
// 						System.out.println("line replaced");
						lineDancePanel.updateLineNumber(origNum, newNum);
					}
				} catch(BadLocationException e) {
					e.printStackTrace();
				}
				
			}
		};
	}
	
	/*
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}
	
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}
	
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
	*/
	
	/** Gets the file extension of the current file.
	 * @return the file extension, which is the part of
	 * the file after the final period (".")
	 * @see LibTTx#getFileExtension
	 */
	public String getFileExtension() {
		String path = getFile().getPath();
		return LibTTx.getFileExtension(path);
	}

	/** Sets the keybindings to the preferred value.
	 * 
	 * @param prefs preferences storage
	 */
	public void applyKeybindings(Prefs prefs) {
		if (prefs.isHybridKeybindings()) {
			hybridKeybindings();
		} else if (prefs.isEmacsKeybindings()) {
			emacsKeybindings();
		} else {
			standardKeybindings();
		}
	}

	/** Sets the keybindings to standard shortcuts.
	 * These shortcuts consist of those typically found on most desktop
	 * systems.  The old set of keybindings are replaced with this set.
	 *
	 */
	public void standardKeybindings() {
		createActionTable(this);
		universalShortcuts();
	}

	/** Sets the keybindings to a mesh of standard and Emacs shortcuts.
	 * The keybinding uses Emacs shortcuts are those for most single line or 
	 * character navigation, but standard shortcuts for everything else.  In case
	 * of a conflict, the Emacs shortcuts take precedence.
	 *
	 */
	public void hybridKeybindings() {
		createActionTable(this);
		universalShortcuts();
		partialEmacsShortcuts();
	}

	/** Sets the keybindings to Emacs shortcuts for most typical single-key
	 * combinations.  
	 * The keybindings essentially uses a hierarchy of standard, hybrid,
	 * and Emacs combinations, where set of shortcuts takes precedence
	 * over the previous set in case of conflict.  The limit of the shortcuts is
	 * a set of common single-key Emacs combinations.
	 */
	public void emacsKeybindings() {
		createActionTable(this);
		universalShortcuts();
		partialEmacsShortcuts();
		emacsShortcuts();
	}

	/** Creates the universal shortcuts, common to standard, partial-Emacs,
	 * and Emacs keybindings.
	 * Currently the standard set consists of only the universal shortcuts.
	 *
	 */
	private void universalShortcuts() {
		// (Ctrl-BKSPC): deletes previous word, conforming with 
		// normal Windows and Linux shortcut behavior
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Event.CTRL_MASK),
			"deleteWord");
		amap.put("deleteWord", new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				deletePrevWord();
			}
		});
		
		// (Alt+BKSPC): also deletes previous word, for compatibility 
		// with Mac systems
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Event.ALT_MASK),
			"deleteWord");
		amap.put("deleteWord", new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				deletePrevWord();
			}
		});
		
		// (Shfit+BKSPC): deletes the previous character, as if BKSPC alone
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Event.SHIFT_MASK),
			"deletePrevChar");
		amap.put(
			"deletePrevChar",
			getActionByName(DefaultEditorKit.deletePrevCharAction));
			
		// (Shfit+TAB): unindents the highlighted region
		// This action could be placed in the general keyPressed
		// method in Windows, but tests on Linux (Fedora 7, GNOME)
		// indicate that standard practice is to place shift+TAB code
		// in the action map to override the focus mechanism from
		// taking over the shortcut.
		Action unidentTabsAction = new AbstractAction() {
			/** Unindents the entire selected region.
			 * Each line to be unindented should be selected
			 * in its entirety.
			 * TODO: Check the start of any line even partially
			 * highlighted rather than just checking up the start
			 * of the selected area.
			 * @param evt the action event
			 */
			public void actionPerformed(ActionEvent evt) {
				if (autoIndent) {
//					System.out.println("shift tab");
					try {
						if (getSelectionStart() != getSelectionEnd()) {
							tabRegionReverse();
						} else {
							unindentLeadingTab();
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			}
		};
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.SHIFT_MASK),
			"unindentTabs");
		amap.put(
			"unindentTabs",
			unidentTabsAction);
		
		
		// (ctrl-n) advance a line
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
			"downChar");
		amap.put("downChar", getActionByName(DefaultEditorKit.downAction));

		// (ctrl-p) go up a line
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
			"upChar");
		amap.put("upChar", getActionByName(DefaultEditorKit.upAction));

	}
	
	/** Creates the partial-Emacs shortcuts, consisting of single character and
	 * line navigation.
	 *
	 */
	private void partialEmacsShortcuts() {
		// (ctrl-f) advance a character
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK),
			"forwardChar");
		amap.put(
			"forwardChar",
			getActionByName(DefaultEditorKit.forwardAction));

		// (ctrl-b) go back a character
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK),
			"backwardChar");
		amap.put(
			"backwardChar",
			getActionByName(DefaultEditorKit.backwardAction));

		// (ctrl-n) advance a line
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK),
			"downChar");
		amap.put("downChar", getActionByName(DefaultEditorKit.downAction));

		// (ctrl-p) go up a line
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK),
			"upChar");
		amap.put("upChar", getActionByName(DefaultEditorKit.upAction));

		// (ctrl-a) go to the beginning of the line
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK),
			"beginLine");
		amap.put(
			"beginLine",
			getActionByName(DefaultEditorKit.beginLineAction));

		// (ctrl-e) go to the end of the line
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK),
			"endLineChar");
		amap.put(
			"endLineChar",
			getActionByName(DefaultEditorKit.endLineAction));

		// (ctrl-d) delete next char, including indent updates
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK),
			"deleteNextChar");
		amap.put(
			"deleteNextChar",
			new DeleteNextCharAction());
			
		// (DEL) delete previous character, including indent updates
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
			"deleteNextChar");
		amap.put(
			"deleteNextChar",
			new DeleteNextCharAction());
		
		// (alt-b) go to beginning of current word
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.ALT_MASK),
			"wordStart");
		amap.put("wordStart", new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				setCaretPosition(getWordPosition());
			}
		});

		// (alt-f) go to beginning of next word
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.ALT_MASK),
			"nextWord");
		amap.put("nextWord", new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				setCaretPosition(getNextWordPosition());
			}
		});

	}

	/** Sets the Emacs shortcuts, consisting of common single-key Emacs keybindings.
	 * These shortcuts should be applied on top of those from
	 * <code>partialEmacsShortcuts()</code>; <code>emacsShortcuts()</code>
	 * only applies additional key combinations.
	 *
	 */
	private void emacsShortcuts() {
		
		// (ctrl-V) page down
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK),
			"pageDown");
		amap.put("pageDown", getActionByName(DefaultEditorKit.pageDownAction));
		
		// (alt-V) page up
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.ALT_MASK),
			"pageUp");
		amap.put("pageUp", getActionByName(DefaultEditorKit.pageUpAction));
		
		// (alt-shift-<) go home (of the document, that is)
		imap.put(
			KeyStroke.getKeyStroke(
				KeyEvent.VK_COMMA,
				Event.ALT_MASK | Event.SHIFT_MASK),
			"begin");
		amap.put("begin", getActionByName(DefaultEditorKit.beginAction));
		
		// (alt-shift->) go to the end
		imap.put(
			KeyStroke.getKeyStroke(
				KeyEvent.VK_PERIOD,
				Event.ALT_MASK | Event.SHIFT_MASK),
			"end");
		amap.put("end", getActionByName(DefaultEditorKit.endAction));

	}
	
	/** Deletes the word or rest of the word to the left
	 * of the cursor
	 */
	public void deletePrevWord() {
		int wordPos = getWordPosition();
		// delete via the document methods rather than building
		// string manually, a slow task.  Uses document rather than
		// AccessibleJTextComponent in case used for serialization:
		// Serialized objects of this class won't be compatible w/
		// future releases
		try {
			// call getDocument() each time since doc may change
			// with call to viewPlain, viewHTML, or viewRTF
			getDocument().remove(wordPos, getCaretPosition() - wordPos);
			setCaretPosition(wordPos);
		} catch (BadLocationException b) {
			System.out.println("Deletion out of range.");
		}
		/* Alternate method, essentially same as document except
		 * using the AccessibleJTextComponent class, whose serializable
		 * objects may not be compatible w/ future releases access the 
		 * text component itself to tell it to perform the deletions rather 
		 * than building string manually, a slow task
		 */
		/*
		(new JTextComponent.AccessibleJTextComponent())
		.delete(wordPos, getCaretPosition());
		*/
	}

	/** Sets the default displayed tab sizes.
	 * Only affects the displayed size, since the text itself represents the 
	 * tab simply as "\t".
	 * @param tabChars number of spaces for the tab to represent
	 * @see #setNoTabs()
	 * @see #setIndentTabs(int) 
	 */
	public void setDefaultTabs(int tabChars) {
		int charWidth = getFontMetrics(getFont()).charWidth(' ');
		int tabWidth = charWidth * tabChars;
		
		// Creates the tabs and fills them with the default indent
		TabStop[] tabs = new TabStop[30]; // just enough to fit default frame
		for (int i = 0; i < tabs.length; i++)
			tabs[i] = new TabStop((i + 1) * tabWidth);
		TabSet tabSet = new TabSet(tabs);
		
		// resets any other indents or styles to their default settings
		SimpleAttributeSet attribs = new SimpleAttributeSet();
		StyleConstants.setTabSet(attribs, tabSet);
		StyleConstants.setLeftIndent(attribs, 0);
		StyleConstants.setFirstLineIndent(attribs, 0);
		
		// apply the change without recording an undo
		undoManager.setIgnoreNextStyleChange(true);
		getStyledDocument()
			.setParagraphAttributes(0, getDocument().getLength() + 1,
		// next char
		attribs, false); // false to preserve default font
	}

	/** Sets the displayed tab size to 0.
	 * Only affects the displayed size, since the text still represents the tab simply as "\t".
	 * Useful when representing tabs through other means, such as styled indents.
	 * @see #setDefaultTabs(int)
	 * @see #setIndentTabs(int)
	 * @see #indent(int, int, int, int)
	 */
	public void setNoTabs() { //int offset, int length) {
		//	System.out.println("set no tabs at position " + offset + " for " + length + " chars");
		TabStop[] tabs = new TabStop[30];
		//tabs[0] = new TabStop(0, TabStop.ALIGN_RIGHT, TabStop.LEAD_NONE);
		
		/* Provide a whole array of TabStops for positions many tabs deep;
		 * now setFirstLineIndent, set to a neg number, can provide the neg
		 * region into which these tabs can pull the first line of text in a paragraph
		 * (see indent(int, int, int, int).
		*/
		int charWidth = getFontMetrics(getFont()).charWidth(' ');
		int tabWidth = charWidth * getTabSize();
		for (int i = 0; i < 30; i++) {
			tabs[i] = new TabStop(i * tabWidth * -1);
		}
		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attribs = new SimpleAttributeSet();
		StyleConstants.setTabSet(attribs, tabSet);
		
		// apply the change without recording an undo
		undoManager.setIgnoreNextStyleChange(true);
		getStyledDocument()
			.setParagraphAttributes(0, getDocument().getLength() + 1,
		// next char
		attribs, false); // false to preserve default font
	}

	/** Sets the displayed indentation for the entire text. 
	 * Only affects the displayed size.  
	 * Useful to represent tabs as styled indents.
	 * @param tabChars the number of characters that each tab represents
	 * @see #setDefaultTabs(int)
	 * @see #setIndentTabs(int, int, int)
	 */
	public void setIndentTabs(int tabChars) {
		setIndentTabs(tabChars, 0, getAllText().length());
	}

	/** Sets the displayed indentation for the region beginning at 
	 * <code>start</code> through any paragraph that begins at <code>end</code>.
	 * By only affecting the displayed size, this method is useful 
	 * to represent tabs as styled indents.  The underlying text remains untouched.
	 * @param tabChars the number of characters that each tab represents
	 * @param start the position from which to start indenting
	 * @param end the first position from which to no longer indent
	 * @see #setDefaultTabs(int)
	 * @see #setIndentTabs(int)
	 */
	public void setIndentTabs(int tabChars, int start, int end) {
		//		System.out.println("start: " + start + ", end: " + end);
		int i = start;
		int j = 0;
		String s = getAllText();
		
		// indent every paragraph from "start" to "end" acc
		// to each paragraph's number of tabs
		while (i < end) {
			// find next newline
			j = s.indexOf("\n", i);
			if (j == -1) j = end - 1; // if found none, go to end
			
			// count and graphically indent tabs at head of curr line
			int tabs = leadingTabsCount(s, i);
			if (tabs >= 0)
				indent(tabChars, tabs, i, j - i + 1);
			i = j + 1;
		}
	}

	/** Counts the number of continuous tabs from a given position.
	 * Useful when determining the number of tabs in the current line to auto-indent
	 * the same number for the next line, for example.
	 * @param s text to count for tabs
	 * @param offset position to start counting
	 * @return number of continuous tabs from a given position
	 * @see #leadingTabsCount(Document, int)
	 */
	public int leadingTabsCount(String s, int offset) {
		int tabs = 0;
		for (int i = offset; i < s.length() && s.charAt(i) == '\t'; i++)
			++tabs;
		return tabs;
	}

	/** Counts the number of continuous tabs from a given position.
	 * Useful when determining the number of tabs in the current line to auto-indent
	 * the same number for the next line, for example.
	 * @param doc the document with the text to count for tabs
	 * @param offset position to start counting
	 * @return number of continuous tabs from a given position
	 * @see #leadingTabsCount(String, int)
	 */
	public int leadingTabsCount(Document doc, int offset) {
		int tabs = 0;
		int len = doc.getLength();
		try {
			for (int i = offset; i < len && doc.getText(i, 1).equals("\t"); i++)
				++tabs;
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return tabs;
	}

	/** Indents a paragraph by a given number of tabs and size per tab.
	 * Indents the entire region, not just the first line, though each tab remains the size of
	 * one space.
	 * @param tabChars number of spaces to represent for each tab
	 * @param tabs number of tabs
	 * @param offset position in text at which to start indenting
	 * @param length position in text at which to stop indenting
	 */
	public void indent(int tabChars, int tabs, int offset, int length) {
		int charWidth = getFontMetrics(getFont()).charWidth(' ');
		int tabWidth = charWidth * tabChars;
		//System.out.println("tabWidth: " + tabWidth + ", charWidth: " + charWidth + ", tabs: " + tabs + ", offset: " + offset + ", length: " + length + ", styledDoc: " + getStyledDocument().toString());

		SimpleAttributeSet attribs = new SimpleAttributeSet();
		
		StyleContext sc = new StyleContext();
		StyleConstants.setLeftIndent(attribs, tabs * tabWidth);
		/*
		Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
		final Style mainStyle = sc.addStyle("MainStyle", defaultStyle);
		StyleConstants.setLeftIndent(mainStyle, tabs * tabWidth);
		getStyledDocument().setLogicalStyle(offset, mainStyle);
		*/
		
		// waiting until fix (Java Bug ID#5073988 )?  But first line does indent,
		// though the first tab merely increases in size to fill up the firstLineIndent
		//System.out.println("firstLineIndent: " + tabs * charWidth * -1);
		
		/* By itself, the following command merely extends the negative 
		 * indentation area for the first line--which the first tab expands
		 * to occupy, voiding the entire effort to shift the first line back.
		 * Coupled with an array of negatively posititioned tabs, however,
		 * the command provides an area into which these tabs can drag
		 * the text from the first line.
		*/
		StyleConstants.setFirstLineIndent(attribs, tabs * charWidth * -1.7f);
		undoManager.setIgnoreNextStyleChange(true);
		getStyledDocument().setParagraphAttributes(offset, length, // next char
		attribs, false); // false to preserve default font
	}

	/** Indents the current paragraph, no matter where the caret is within it.
	 * Renders tabs as spaces, but indents the entire region a given number of spaces
	 * per tab.
	 * @param tabChars number of spaces for each tab to represent
	 * @param decrementTab an unindent has occurred within a Java Virtual
	 * Machine whose version precedes 1.5
	 * @see #indentCurrentParagraph(int)
	 */
	/* JVM v.1.5 simply processes tabs according to the
	 * number currently present, and indentCurrentParagraph methods operate 
	 * accordingly:
	 * both methods assume that the tab character change has already been 
	 * accomplished.  Tab deletions in JVM < v.1.5 appear not to be processed 
	 * until the KeyListener methods finish, requiring a the tab count
	 * in these methods to be decremented.
	 * Rather than create two separate indentation methods, a single indent
	 * method now assumes that the tabs have been processed but also allows
	 * for a parameter to flag when a tab deletion takes place in JVM < v.1.5. 
	 */
	public void indentCurrentParagraph(
		int tabChars,
		boolean decrementTab) {
		String s = getAllText();
		int start = LibTTx.reverseIndexOf(s, "\n", getCaretPosition()) + 1;
		int tabs = leadingTabsCount(s, start);
		// the tab has already been deleted in JVM >= v.1.5;
		// tabs should equal the final number of tab characters, so tabs
		// must be decremented in JVM < v.1.5
		if (decrementTab) {
			--tabs;
		}
//		System.out.println("tabs: " + tabs);
		int end = s.indexOf("\n", start);
		indent(tabChars, tabs, start, (end == -1 ? s.length() : end + 1) - start);
	}

	/** Indents the current paragraph, no matter where the caret is within it.
	 * Renders tabs as spaces, but indents the entire region according to the 
	 * currently set number of spaces per tab.  
	 * Assumes that either an indent, as opposed to an unindent,
	 * has occurred, or that the Java Virtual Machine is >= v.1.5, or both. 
	 * @see #indentCurrentParagraph(int, boolean)
	 * @see #indentCurrentParagraph(int)
	 */
	public void indentCurrentParagraph() {
		indentCurrentParagraph(getTabSize(), false);
	}

	/** Indents the current paragraph, no matter where the caret is within it.
	 * Renders tabs as spaces, but indents the entire region a given number of spaces
	 * per tab.  Assumes that either an indent, as opposed to an unindent,
	 * has occurred, or that the Java Virtual Machine is >= v.1.5, or both. 
	 * @param tabChars number of spaces for each tab to represent
	 * @see #indentCurrentParagraph(int, boolean)
	 */
	public void indentCurrentParagraph(int tabChars) {
		indentCurrentParagraph(tabChars, false);
	}

	/** Determines whether the tab is at the start of a given line.
	 * The tab must be either at the head of the line or connected to it by a continuous string
	 * of tabs.  Useful to prevent inner tabs from influencing indents.
	 * @return <code>true</code> if the tab is a leading tab
	 */
	public boolean isLeadingTab() {
		//System.out.println("caret: " + getCaretPosition() + ", selectionEnd: " + getSelectionEnd());
		int i = getLeadingCharIndex();
		try {
			if (i >= 0 && !getDocument().getText(i, 1).equals("\t")) return false;
			while (i > 0 && getDocument().getText(i, 1).equals("\t"))
				i--;
			return i == 0 || i >= 0 && getDocument().getText(i, 1).equals("\n");
		} catch (BadLocationException e) {
			System.out.println("Can't find no tabs, dude.");
			return false;
		}
	}
	
	/**Gets the character immediately preceding the caret or,
	 * if the text is selected, the first character in the selection.
	 * @return indext of the character preceding the caret in
	 * unselected text or, in selected text, the first selected
	 * character.
	*/
	private int getLeadingCharIndex() {
		int start = getSelectionStart();
		int end = getSelectionEnd();
		return (start == end) ? --end : start;
	}
		
	/**Unindents the leading tab on the current line.
	 * The current line is the line with the caret.  The line is
	 * first checked for a tab immediately following the preceding
	 * line.  If a tab exists there, it will be removed, and the current
	 * paragraph will be graphically re-indented.
	 * @throws BadLocationException if the document cannot
	 * access text in the current line
	*/
	public void unindentLeadingTab() throws BadLocationException {
		Document doc = getDocument();
		// the current line is the line with the caret
		int i = getCaretPosition();
		// finds the start of the line, either immediately following
		// the previous newline, or if none exists, the first character
		// of the document
		int lineLeadingChar = LibTTx.reverseIndexOf(doc, "\n", i) + 1;
		System.out.println("lineLeadingChar: " + lineLeadingChar);
		// removes the first tab
		if (doc.getText(lineLeadingChar, 1).equals("\t")) {
			doc.remove(lineLeadingChar, 1);
			indentCurrentParagraph();
		}
	}
	
	/**Tabs each line in an entire selected region.
	 * Any line that that starts or has a leading tab within the selected
	 * region receives a new leading tab.
	*/
	public void tabRegion() throws BadLocationException {
		Document doc = getDocument();
		int start = getSelectionStart();
		int end = getSelectionEnd();
		int len = end - start;
		// buffer slightly larger than the text to accommodate
		// any new tabs
		StringBuffer buf = new StringBuffer((int) (len + len * .1));
		String currChar = ""; // currently evaluated character
		int charsAdded = 0; // char count for re-highlighting
		boolean addTab = false; // flag to add a tab
		
		// starts from the character just before the selected region
		// to the last character that the selection encompasses
		for (int i = start - 1; i < end; i++) {
			// get each character after the first char in the document
			if (i != -1) currChar = doc.getText(i, 1);
			// append every character but the one preceding the
			// selected region
			if (i != start - 1) buf.append(currChar);
			// add a tab after any newline or at the start of 
			// the document; the tab follows the newline
			if (i == -1|| currChar.equals("\n")) {
				buf.append("\t");
				charsAdded++;
			}
		}
		// allow one undo to undo the entire edit
		startCompoundEdit();
		doc.remove(start, len);
		doc.insertString(start, buf.toString(), null);
		stopCompoundEdit();
		
		//System.out.println("start: " + start + ", end: " + end + ", len: " + len + ", charsAdded: " + charsAdded + ", getSelectionStart: " + getSelectionStart() + ", getSelectionEnd: " + getSelectionEnd());
		
		// re-indent graphically, if necessary
		if (autoIndent) indentRegion(start, end + charsAdded);
		
		// re-highlight the text
		setSelectionEnd(end + charsAdded);
		moveCaretPosition(start);
	}
	
	/**Removes a leading tab from each line in an entire selected region.
	 * Any line that that has a leading tab within the selected
	 * region loses a leading tab.
	*/
	public void tabRegionReverse() throws BadLocationException {
		Document doc = getDocument();
		int start = getSelectionStart();
		int end = getSelectionEnd();
		int len = end - start;
		// buffer slightly larger than the text to accommodate
		// any new tabs
		StringBuffer buf = new StringBuffer((int) (len + len * .1));
		String currChar = ""; // currently evaluated character
		int charsAdded = 0; // char count for re-highlighting
		boolean checkTab = false; // flag to check for a tab
		
		// starts from the character just before the selected region
		// to the last character that the selection encompasses
		for (int i = start - 1; i < end; i++) {
			// gets each char except the char at the beginning of the doc
			if (i != -1) currChar = doc.getText(i, 1);
			// adds each char except the one immediately preceding the
			// selected region and the first tab encountered in the line
			if (i != start - 1 && !(checkTab && currChar.equals("\t"))) {
				buf.append(currChar);
				charsAdded++;
			} else {
				// resets the tab flag after skipping a char
				checkTab = false;
			}
			// flag tab check if newline or start of doc
			if (i == -1|| currChar.equals("\n")) {
				checkTab = true;
			}
			
		}
		
		// allow one undo to undo the entire edit
		startCompoundEdit();
		doc.remove(start, len);
		doc.insertString(start, buf.toString(), null);
		end = start + charsAdded;
		
		// remove one final tab if the last selected character is a newline,
		// and the tab immediately follows the selection
		if (doc.getLength() > end && checkTab && doc.getText(end, 1).equals("\t"))
			doc.remove(end, 1);
		stopCompoundEdit();
		//System.out.println("start: " + start + ", end: " + end + ", len: " + len + ", charsAdded: " + charsAdded + ", getSelectionStart: " + getSelectionStart() + ", getSelectionEnd: " + getSelectionEnd());
		
		// re-indent graphically and re-highlight
		if (autoIndent) indentRegion(start, end);
		setSelectionEnd(end);
		moveCaretPosition(start);
	}



















	/**Gets the value showing whether the text in the text area
	 * has changed.
	 * @return <code>true</code> if the text has changed;
	 * <code>false</code> if otherwise
	 */
	public boolean getChanged() {
		return changed;
	}

	/**Sets the value showing whether the text in the text
	 * area has changed.
	 * @param b <code>true</code> if the text has changed;
	 * <code>false</code> if otherwise
	 */
	public void setChanged(boolean b) {
		changed = b;
	}

	/**Gets the file's path.
	 * @return path, formatted for the system and streamlined
	 */
	public String getPath() {
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**Gets the file in the text pad.
	 * 
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**Gets the file's name.
	 * @return filename
	 */
	public String getFilename() {
		return file.getName();
	}

	/**Gets the path to the file's directory.
	 * @return path to directory
	 */
	public String getDir() {
		String dir = "";
		return ((dir = file.getParent()) != null) ? dir : "";
	}

	/**Gets the auto-indent selection.
	 * @return <code>true</code> if auto-indent is selected.
	 */
	public boolean getAutoIndent() {
		return autoIndent;
	}
	
	
	
	
	
	

	/**Sets the file to a file object.
	 * @param aFile file to hold the text area's contents.
	 */
	public void setFile(File aFile) {
		file = aFile;
//		setupFileModifiedThread();
	}

	/**Sets the file to a path.
	 * @param path path of file to save to
	 */
	public void setFile(String path) {
		file = new File(path);
//		setupFileModifiedThread();
	}
	
	public void setupFileModifiedThread() {
		FileModifiedThread thread = getFileModifiedThread();
		if (thread != null) {
//			thread.setFile(file);
//System.out.println("just saved: " + getFile().lastModified());
			thread.setLastModifiedWithTTx(getFile().lastModified());
		}
	}
	
	public void stopFileModifiedThread() {
		FileModifiedThread thread = getFileModifiedThread();
		if (thread != null) {
			thread.requestStop();
		}
	}

	/**Sets the auto-indent selection.
	 * @param b <code>true</code> to auto-indent
	 */
	public void setAutoIndent(boolean b) {
		autoIndent = b;
		applyAutoIndent();
	}
	
	public void setFileModifiedThread(FileModifiedThread thread) {
		fileModifiedThread = thread;
	}
	
	
	
	
	
	

	/**Check whether the file has been created.
	 * @return <code>true</code> if the file has been created;
	 * <code>false</code> if otherwise
	 */
	public boolean fileExists() {
		return file.exists();
	}

	/**Undoes the last text change.
	 * May need to make include the goofy features' text manipulations.
	 */
	public void undo() {
		if (undoManager.canUndo()) {
			undoManager.undo();
//			System.out.println("here");
			if (autoIndent) {
				indentCurrentParagraph();
			}
		}
	}

	/**Redoes the last undone text change.
	 * May need to make include the goofy features' text manipulations
	 */
	public void redo() {
		if (undoManager.canRedo())
			undoManager.redo();
	}

	/**Applies the current <code>UndoManager</code> as well as the tab size, 
	 * if appropriate.
	 */
	public void applyDocumentSettings() {
		StyledDocument doc = getStyledDocument();
		// need to remove and re-add the undo manager to properly
		// undo events after reading in new text
		doc.removeUndoableEditListener(undoManager);	
		undoManager.discardAllEdits();
		doc.addUndoableEditListener(undoManager);
		applyAutoIndent();
		
		if (doc instanceof AbstractDocument) {
			AbstractDocument abstracDoc = (AbstractDocument)doc;
			abstracDoc.setDocumentFilter(getOrMakeDocFilter());
		}
	}
	
	/** Visually indents the tabs and word-wrapped lines
	 * according to the auto-indent flag.
	 * If the auto-indent flag is set to true, each word-wrapped 
	 * line will be visually indented to match the indentation
	 * of the entire line paragraph.  If the flag is set to false,
	 * all tabs will be reset to their default size, and visual
	 * indentation of word-wrapped lines will be turned off.
	 */
	public void applyAutoIndent() {
		// TODO: commenting out for now as visual indentation not currently
		// working with WrappedPlainView, and calls to paragraph attributes
		// causing crash on loading
// 		if (autoIndent) {
// 			setIndentTabs(getTabSize());
// 			setNoTabs();
// 		} else {
// 			setDefaultTabs(getTabSize());
// 		}
	}

	/**Tells whether the pad has any characters in it.
	 * @return boolean <code>true</code> if the pad is empty
	 */
	public boolean isEmpty() {
		String text = getText();
		return ((text == null) || !(new StringTokenizer(text)).hasMoreTokens())
			? true
			: false;
	}

	/** Gets all the current text.
	 * 
	 * @return the text
	 */
	public String getAllText() {
		try {
			Document doc = getDocument();
			int len = doc.getLength();
			return doc.getText(0, len);
		} catch (BadLocationException e) {
			System.out.println("Couldn't get it all.");
			return "";
		}
	}
	
	public void removeAllText() {
		try {
			Document doc = getDocument();
			int len = doc.getLength();
			doc.remove(0, len);
		} catch (BadLocationException e) {
			System.out.println("Couldn't remove all text.");
		}
	}
	
	/**Replaces the entire body of text with the given string.
	 * @param s the string to replace the document's text
	*/
	public void replaceAllText(String s) {
		try {
			//	    System.out.println(s);
			removeAllText();
			getDocument().insertString(0, s, null);
		} catch (BadLocationException e) {
			System.out.println("Couldn't replace it all.");
		}
	}

	/**Converts the pad to a plain text view.
	 * Undoable.
	 */
	public void viewPlain() {
	
		// store current selection indices
		int selectionStart = getSelectionStart();
		int selectionEnd = getSelectionEnd();
		
		// records the edit event for undoing and gets the text
		StateEdit stateEdit = new StateEdit(this);
		String text = getText();
		setEditorKit(createDefaultEditorKit()); // revert to default editor kit
		
		// sets the style
		// use the default editor kit's <code>DefaultStyledDocument</code>
		setDocument(getEditorKit().createDefaultDocument());
		
		// reapplies the text and registers the edit event
		setText(text);
		applyDocumentSettings();
		stateEdit.end();
		undoManager.addEdit((UndoableEdit) stateEdit);
		
		// reaapplies text selection;
		// for some reason, text gets shifted forward by one,
		// so selection needs to add 1 as well
		if (selectionStart != selectionEnd) {
			setSelectionStart(selectionStart + 1);
			setSelectionEnd(selectionEnd + 1);
		}
	}

	/**Converts the pad to an HTML text view.
	 * The underlying text may have HTML tags added.
	 */
	public void viewHTML() {
		
		// store current selection indices
		int selectionStart = getSelectionStart();
		int selectionEnd = getSelectionEnd();
		
		// records the edit event for undoing and gets the text
		StateEdit stateEdit = new StateEdit(this);
		String text = getText();
		text = text.replaceAll("\n", "<p>");
//		System.out.println(text);
		
		// sets the style
		setDocument(getEditorKit().createDefaultDocument());
		setContentType("text/html");
		
		// reapplies the text and registers the edit event
		setText(text);
		applyDocumentSettings();
		stateEdit.end();
		undoManager.addEdit((UndoableEdit) stateEdit);
		
		// reaapplies text selection;
		// for some reason, text gets shifted forward by one,
		// so selection needs to add 1 as well
		if (selectionStart != selectionEnd) {
			setSelectionStart(selectionStart + 1);
			setSelectionEnd(selectionEnd + 1);
		}
	}
	
	/**Turns on HTML mode, without converting any text
	 * to HTML tags.
	 * The document will be read in directly as an HTML file.
	 */
	public void turnOnHTML() {
		String text = getText();
		// sets the style
		setDocument(getEditorKit().createDefaultDocument());
		setContentType("text/html");
		
		// reapplies the text and registers the edit event
		setText(text);
	}
	
	public boolean isHTMLView() {
		return getContentType().equals("text/html");
	}

	/**Converts the pad to an RTF tex view, if possible.
	 * If the document is not in RTF format, the pad reverts to its original 
	 * setting.
	*/
	public void viewRTF() {
		StateEdit stateEdit = new StateEdit(this);
		String text = getText();
		setDocument(getEditorKit().createDefaultDocument());
		setContentType("text/rtf");
		setText(text);
		applyDocumentSettings();
		stateEdit.end();
		undoManager.addEdit((UndoableEdit) stateEdit);
		if (getText() == null) {
			undo();
		}
	}

	/**Gets the index of the current word's first character.
	 * @return string index of either the current word or the previous
	 * word if only delimiters intervene between the word and
	 * the caret position.
	 */
	public int getWordPosition() {
		int caretPos = getCaretPosition(); // current caret position
		int newCaretPos = caretPos - 1; // moving caret position
		String delimiters = " .,;:-\\\'\"/_\t\n";

		try {
			while (newCaretPos > 0
				&& delimiters.indexOf(getDocument().getText(newCaretPos, 1))
					!= -1) {
				newCaretPos--;
			}
			while (newCaretPos > 0
				&& (delimiters.indexOf(getDocument().getText(newCaretPos - 1, 1))
					== -1)) {
				newCaretPos--;
			}
		} catch (BadLocationException e) {
			System.out.println("Ack!  Can't find the beginning of the word.");
		}

		return (newCaretPos <= 0) ? 0 : newCaretPos;
	}

	/**Gets the index of the next word's first character.
	 * @return string index of the next word's beginning, past
	 * any delimiters
	 */
	public int getNextWordPosition() {
		int caretPos = getCaretPosition(); // current caret position
		int newCaretPos = caretPos; // moving caret position
		int textLen = getText().length(); // end of text
		String delimiters = " .,;:-\\\'\"/_\t\n";

		// check that new caret not at end of string; 
		// skip forward as long as not delimiters
		try {
			while (newCaretPos < textLen
				&& delimiters.indexOf(getDocument().getText(newCaretPos, 1))
					== -1) {
				newCaretPos++;
			}
			// check that new caret not at end of string; 
			// now skip forward as long as delimiters; bring to next letter, etc
			while (newCaretPos < textLen
				&& (delimiters.indexOf(getDocument().getText(newCaretPos, 1))
					!= -1)) {
				newCaretPos++;
			}
		} catch (BadLocationException e) {
			System.out.println("Arrgh!  Can't find the beginning of the word.");
		}

		return newCaretPos;
	}

	/**Auto-indent to the previous line's tab position.
	 */
	public void autoIndent() {
		// go back 2 to skip the hard return that makes the new line
		// requiring indentation
		try {
			int n = 0;
			int tabs = 0;
			
			// finds the previous newline that created the previous paragraph
			for (n = getCaretPosition() - 2;
				n >= 0 && !getDocument().getText(n, 1).equals("\n");
				n--);
//				System.out.println("n char: " + getDocument().getText(n, 1)); }
			/*
			if (n == 0)
				n = -1;
			*/
			// counts the number of tabs at the start of the previous paragraph
			for (tabs = 0; getDocument().getText(++n, 1).equals("\t"); tabs++);
//			System.out.println("tab count: " + tabs);

			// construct a string of all the tabs
			StringBuffer tabStr = new StringBuffer(tabs);
			for (int i = 0; i < tabs; i++) {
				tabStr.append('\t');
			}
			// add the tabs
			getDocument().insertString(
				getCaretPosition(),
				tabStr.toString(),
				null);
		} catch (BadLocationException e) {
			System.out.println(
				"Insert location " + getCaretPosition() + " does not exist");
		}
	}

	/**Fills an array with the component's possible actions.
	 * @param txtComp <code>Java Swing</code> text component
	 */
	private void createActionTable(JTextComponent txtComp) {
		imap.clear();
		amap.clear();
		actions = new Hashtable();
		Action actionsArray[] = txtComp.getActions();
		for (int i = 0; i < actionsArray.length; i++) {
			Action a = actionsArray[i];
			actions.put(a.getValue(Action.NAME), a);
		}
	}

	/**Gets an action from a hash table when give the name of the action.
	 * @param name name of action
	 * @return action
	 */
	private Action getActionByName(String name) {
		return (Action) (actions.get(name));
	}

	/**Stores the editor kit and document during a state change.
	 * Called at both the start and end of the state change for separate hashtables.
	 * @param state table storing the object's current state
	 */
	public void storeState(Hashtable state) {
		state.put("editorkit", getEditorKit());
		state.put("doc", getStyledDocument());
	}

	/**Restores the editor kit and document from the table's stored settings.
	 * Called from an undo event.
	 * @param state tabled of stored settings
	 */
	public void restoreState(Hashtable state) {
		EditorKit editorKit = (EditorKit) state.get("editorkit");
		StyledDocument doc = (StyledDocument) state.get("doc");
		if (editorKit != null)
			setEditorKit(editorKit);
		// set document after setting the editor kit so can apply the doc's settings
		if (doc != null)
			setDocument(doc);
	}

	/**Pastes in text, auto-indenting it as necessary.
	 * If the text pad is in auto-indent mode, the pasted text is graphically
	 * indented to match each of its tabbed paragraphs.
	 * 
	 */
	/* The JVM automatically captures ctrl-V paste events, overriding
	 * any ctrl-V paste shortcut.  To add events to a paste action,
	 * the actual paste method in the text pane needs to be overridden.
	 * Here the new method checks for auto-indentation and graphically
	 * indents the newly spliced in paragraphs as necessary.
	 * 
	 */
	public void paste() {
		int start = getCaretPosition(); // to mark beginning of pasted region
		super.paste();
		
		// refreshes the graphical indents in the pasted region
		if (autoIndent) {
			int end = getCaretPosition(); // marks end of region
			indentRegion(start, end);
		}
	}
	
	/**Wrap-indents a given region.
	 * @param start the character at which to begin indenting
	 * @param end the last character to indent, non-inclusive
	*/
	public void indentRegion(int start, int end) {
		int prevBreak = end; // position of next preceding "\n"
		String text = getAllText(); // pad's text
		// indents each paragraph in pasted region individually;
		// stops once finds a hard return outside the region
		do {
			// indentCurrentParagraph relies on caret position
			setCaretPosition(prevBreak); 
			indentCurrentParagraph();
		} while (
			start
				< (prevBreak = LibTTx.reverseIndexOf(text, "\n", prevBreak)));
		setCaretPosition(end); // returns caret position to end of region
	}
	
	/**Deletes the following character or, if text is selected, the
	 * selected region, re-wrap-indenting if necessary.
	*/
	public void deleteNextChar() {
		Document doc = getDocument();
		//int caretPos = getCaretPosition();
		int start = getSelectionStart();
		int end = getSelectionEnd();
		//System.out.println("i've been called!");
		try {
			// Delete region if selected
			if (start != end) {
				doc.remove(start, end - start);
			} else if (end < doc.getLength()) {
				// Delete following character
				doc.remove(end, 1);
			}
			
			// only need to re-indent current paragraph because 
			// deletions either occur within a line or join two lines,
			// however disparate, into one
			if (isAutoIndent()) {
				indentCurrentParagraph();
			}
		} catch (BadLocationException badE) {
			System.out.println("having trouble deleting next char...");
		}
	}
	
	/**Starts a compound edit sequence, which tracks multiple edits to
	 * allow them to be undone in one fell swoop.
	 * Does nothing if a compound edit is already in progress.
	 * 
	 * @see #stopCompoundEdit()
	 */
	public void startCompoundEdit() {
		if (!isCompoundEditing()) {
			compoundEdit = new CompoundEdit();
			compoundEditing = true;
		}
	}
	
	/**Stores a compound edit sequence in the undo manager.
	 * 
	 * @see #startCompoundEdit()
	 */
	public void stopCompoundEdit() {
		compoundEdit.end();
		compoundEditing = false;
		undoManager.addEdit(compoundEdit);
	}
	
	
	/**
	 * Gets the number of the current newline in the given pad. Word-wrapped
	 * lines are not counted, but only lines with hard breaks.
	 * 
	 * @return the line number, relative to 1 as the first line of the document
	 */
	public int getLineNumber() {
		int offset = getCaretPosition();
		return getDocument().getDefaultRootElement()
				.getElementIndex(offset) + 1;
	}

	/**
	 * Gets the number of newlines in the given pad. Word-wrapped lines are not
	 * counted, but only lines with hard breaks.
	 * 
	 * @return the number of lines with hard breaks
	 */
	public int getTotalLineNumber() {
		return getDocument().getDefaultRootElement().getElementCount();
	}
	
	public int getLineOffset(int line) {
		return getDocument().getDefaultRootElement().getElement(line - 2)
				.getEndOffset();
	}
	
	/**Gets the index position within the document, given the line number.
	 * @param line the number of the line, starting at 1; add 1 to the
	 * document element number
	 * @return a <code>Point</code> object whose X value corresponds
	 * to the start of the line and whose Y value corresponds to the end,
	 * the index of the last character - 1, dropping the last char to avoid
	 * including a newline char or exceeding the document length;
	 * both values relative to the start
	 * of the document
	*/
	public Point getPositionFromLineNumber(int line) {
		// adjusts the line number from the user-friendly, 1 to n+1 numbering
		// system, to the standard 0 to n system
		line--;
		Element elt = getDocument().getDefaultRootElement();
		int len = getDocument().getLength();
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
		return new Point(lineElt.getStartOffset(), end);
	}
	
	/** Remembers a line number and caret position in the 
	 * Line Dance table.
	 */
	public void remLineNum(String name) {
		if (name == null) name = "";
		// adds a new entry in the table
		lineDancePanel.addRow(new String[] {
			"" + getLineNumber(),
// 			"" + getCaretPosition(),
			name
		});
	}
	
	/** Removes Line Dance table entry(ies).
	 */
	public void forgetSelectedLines() {
		lineDancePanel.removeSelectedRows();
	}
	
	/** Edits the name of a the selected entry in Line Dance.
	 */
	public void editLineName() {
		lineDancePanel.editLineName();
	}
	
	/** Jumps the cursor to the caret position recorded in the
	 * selected row of the Line Dance table.
	 */
	public void lineDance() {
		// gets the position from the table
		int line = lineDancePanel.getLineNum();
		int position = getLineOffset(line);
		// checks the position against the length of the document,
		// in case enough characters have been deleted from the
		// document that the caret position would exceed the length
		int len = getDocument().getLength();
		// sets the position
		if (position > len) {
			setCaretPosition(len);
		} else if (position != -1) {
			setCaretPositionTop(position);
		}
		// shifts focus from the Line Dance panel to this pad
		requestFocus();
		requestFocusInWindow();
	}
	

	/**Creates a print pad for this <code>TextPad</code> object.
	 * The text pad needs to create its own print pad so that the 
	 * text pad can pass its contents array of each visible line
	 * and also pass the current font. 
	 * @return the print pad, including the current text, broken
	 * up according to the visible, soft breaks in the 
	 * <code>TextPad</code>, and the current font
	 */
	public PrintPad createPrintPad() {
		return new PrintPad(
			//LibTTx.getVisibleLines(this),
			LibTTx.getPrintableLines(this),
			new Font(getFont().getAttributes()));
	}
	
	
	/**Refreshes a tab without the user having to close and reopen it.
	 * Useful when an open file is externally changed.
	*/
	public void refresh() {
		// Ensure that has a saved file to refresh
		if (!fileExists()) {
			String title = "Refreshing ain't always easy";
			String msg = "This is all we've got.  There's no saved file yet"
				+ "\nfor us to refresh.  Sorry about that.";
			JOptionPane.showMessageDialog(this, msg, title,
				JOptionPane.INFORMATION_MESSAGE, null);
			return;
		}
		
		// Confirms with user that willing to override any unsaved changes
		if (getChanged()) {
			
			String s = "Refresh request";
			// dialog with 2 choices: discard, cancel
			String msg = "This file has not yet been saved."
					+ "\nShould I still refresh it with the currently saved version?";
			int choice = JOptionPane.showOptionDialog(this, msg,
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
		int pos = getCaretPosition();
		String path = getPath();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			final String text = LibTTx.readText(new BufferedReader(reader));
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// flagging the change prior to the change prevents UI updates
					// for the change from TextPadDocumentListener in TextTrix
					setChanged(true);
					setText(text);
//					setHighlightStyle(new Prefs().getSpellChecker(), text);
					applyAutoIndent();
					setChanged(false);
				}
			});
//			read(t, reader, path);
		} catch(FileNotFoundException e) {
			// This message will most likely not be reached since
			// the non-existant file would be detected earlier.
			String msg = "The original file appears to have been moved, "
				+ "\ndeleted, or set to be unreadable.";
			JOptionPane.showMessageDialog(
				this, 
				msg, 
				"File missing",
				JOptionPane.ERROR_MESSAGE);
		} catch(IOException e) {
			String msg = "The original file could not be accessed.";
			JOptionPane.showMessageDialog(
				this, 
				msg, 
				"File inaccessible",
				JOptionPane.ERROR_MESSAGE);
		}
//		openFile(t.getFile(), t.isEditable(), false, true);
		// prevent caret from exceeding length of newly refreshed file
		if (pos <= getDocument().getLength()) {
			setCaretPosition(pos);
		} else {
			setCaretPosition(getDocument().getLength());
		}
	}
	
	public void addDocListener(DocumentListener aDocListener) {
		docListener = aDocListener;
		getDocument().addDocumentListener(docListener);
	}
	
	public void removeDocListener() {
		getDocument().removeDocumentListener(docListener);
	}
	
	
	
	
	
	
	
	
	
	
	/**Checks the value of the auto-indent flag.
	 * 
	 * @return <code>true</code> if the pad is flagged to auto-indent
	 */
	public boolean isAutoIndent() {
		return autoIndent;
	}
	/**Checks the value of the compound edit flag.
	 * 
	 * @return <code>true</code> if the text pad is currently tracking
	 * a compound edit
	 */
	public boolean isCompoundEditing() {
		return compoundEditing;
	}
	
	
	
	
	

	/** Sets the number of characters that each tab represents.
	 * The tab is still represented by a '/t' rather than the given number
	 * of characters; this setting is merely for display purposes.
	 * 
	 * @param aTabSize number of characters
	 */
	public void setTabSize(int aTabSize) {
		tabSize = aTabSize;
	}

	
	
	
	
	/** Sets the caret position and scrolls the scroll pane that 
	 * contains this Text Pad
	 * so that the top of the caret is at the middle of the 
	 * pad's viewable area.
	 * @param position the new caret position
	 */
	public void setCaretPositionTop(int position) {
		int origCaretPosition = getCaretPosition();
		setCaretPosition(position); // if cursor out of view, need to bring it back in
		// if cursor didn't move, don't want screen to move any farther
		if (origCaretPosition == position) return;
		try {
//			int selectionStart = getSelectionStart();
			// moves caret to start of selection, which is equal to the new position
			
			// if no selection, in case selection spans > 1 line
			// TODO: still seems to scroll to end of selection
//			System.out.println("selection start: " + getAllText().substring(getSelectionStart(), getSelectionStart() + 3) + ", position: " + position);
			// the position of the selection start
			Rectangle rect = modelToView(getSelectionStart());//position);
			// the viewport and current position
			JViewport viewport = getScrollPane().getViewport();
			Rectangle viewportRect = viewport.getViewRect();
			
			// scrollRectToVisibible will normally scroll the view
			// so that the caret is at the top of the viewport.
			// The rect height is adjusted here to position the caret
			// to the middle of the viewport, and setViewPosition is
			// used instead to manually set the viewport position.
			if (rect != null && rect.y > viewportRect.height) {
				// calculates the position of where the caret would
				// be in the middle of the viewport
				int recty = rect.y - viewportRect.height / 2;
				if (recty >= 0) {
//					System.out.println("x: " + rect.x + ", y: " + rect.y + ", width: " + rect.width + ", height: " + rect.height + ", recty: " + recty);
					rect.setLocation(rect.x, recty);
				}
//				System.out.println("rect: " + rect.x + ", " + rect.y);
//				viewport.scrollRectToVisible(rect);
				// ignores the x-val and sets to 0 to ensure that the view
				// is not horizontally shifted
				viewport.setViewPosition(new Point(0, rect.y));
			}
//			getScrollPane().getViewport().setViewPosition(new Point(rect.x, rect.y));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	/**Sets the current auto-save timer.
	 * <code>TextTrix</code> attaches a timer to each <code>TextPad</code>
	 * with changed content if the user has enabled the feature.
	 */
	public void setAutoSaveTimer(StoppableThread aAutoSaveTimer) {
		autoSaveTimer = aAutoSaveTimer;
	}
	
	/** Sets the scroll pane that houses this pad.
	 * @param aScrollPane the pane that houses this pad
	 */
	public void setScrollPane(JScrollPane aScrollPane) {
		scrollPane = aScrollPane;
	}
	
	public void setEOL(String val) { eol = val; }
	
	
	
	
	
	
	
	
	
	
	
	
	/** Gets the number of characters that each tab represents.
	 * 
	 * @return the number of characters
	 * @see #setTabSize(int)
	 */
	public int getTabSize() {
		return tabSize;
	}
	
	
	/** Gets the scroll pane that houses this pad.
	 * @return the scroll pane
	 */
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	/** Gets the Line Dance panel associated with this pad.
	 * @return the Line Dance panel, which includes a table
	 * of saved line numbers for editing in this pad
	 */
	public LineDancePanel getLineDancePanel() {
		return lineDancePanel;
	}
	
	public FileModifiedThread getFileModifiedThread() {
		return fileModifiedThread;
	}
	
	/**Gets the current auto-save timer.
	 * <code>TextTrix</code> attaches a timer to each <code>TextPad</code>
	 * with changed content if the user has enabled the feature.
	 * @return the auto-save timer
	 */
	public StoppableThread getAutoSaveTimer() {
		return autoSaveTimer;
	}
	
	public WrappedPlainView getWrappedView() { return wrappedView; }
	private void setWrappedView(WrappedPlainView val) { wrappedView = val; }
	
	public String getEOL() { return eol; }
	
	
	
	
	
	
	
	/** Subclass of the <code>UndoManager</code>.
	 * Allows the code to ignore undos.
	 * @author davit
	 */
	private class UndoManagerTTx extends UndoManager {
		private boolean ignoreNextStyleChange = false;

		/** Sublcassed method to add ability to ignore undos when flagged.
		 * If <code>ignoreNextStyleChange</code> is set to <code>true</code>,
		 * the method doesn't call the superclass' corresponding method.
		 */
		public synchronized boolean addEdit(UndoableEdit anEdit) {
			// check if undoable
			if (anEdit instanceof AbstractDocument.DefaultDocumentEvent) {
				AbstractDocument.DefaultDocumentEvent de =
					(AbstractDocument.DefaultDocumentEvent) anEdit;
				// ignore style attribute changes by ignoring if change event
				// or flagged specifically as a style change
				if (de.getType() == DocumentEvent.EventType.CHANGE
					|| ignoreNextStyleChange) {
					ignoreNextStyleChange = false; // reset the ignore flag
					return false;
				}
			}
			// call superclass' method
			return super.addEdit(anEdit);
		}
		
		/**Subclassed method to check for compound edits.
		 * If a compound edit is in progress, the edit is added to the 
		 * compound edit object; otherwise, the method operates as normally.
		 * 
		 * @param evt the edit event
		 */
		public void undoableEditHappened(UndoableEditEvent evt) {
			if (isCompoundEditing()) {
				compoundEdit.addEdit(evt.getEdit());
			} else {
				super.undoableEditHappened(evt);
			}
		}

		/** Flags the manager to ignore the next change in style.
		 * For example, stylistic auto-indentation indents should maybe be ignored
		 * @param b <code>true</code> to ignore the next style change
		 */
		public void setIgnoreNextStyleChange(boolean b) {
			ignoreNextStyleChange = b;
		}

		/** Gets the state of the flag for ignoring the next style change.
		 * 
		 * @return <code>true</code> if the next style will be ignored
		 */
		public boolean getIgnoreNextStyleChange() {
			return ignoreNextStyleChange;
		}
	}
	
	/**An action to delete the next character.
	*/
	private class DeleteNextCharAction implements Action {
		
		/**Constructs an empty action.
		*/
		public DeleteNextCharAction() {
		}
		
		/**Deletes the previous character or, if the text is selected,
		 * the selected region
		 * @param e
		 * @see TextPad#deleteNextChar()
		*/
		public void actionPerformed(ActionEvent e) {
			deleteNextChar();
		}
		
		public void addPropertyChangeListener(PropertyChangeListener listener) { }
		public Object getValue(String key) { return null; }
		public boolean isEnabled() { return true; }
		public void putValue(String key, Object value) { }
		public void removePropertyChangeListener(PropertyChangeListener listener) { }
		public void setEnabled(boolean b) { }
	}
	
	
	/** A mouse listener for detecting double clicks in the Line Dance panel.
	 */
	private class LineDanceMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				lineDance();
			}
		}
	}
}

