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
import java.awt.*;
import java.util.*;
import java.awt.event.*;
//import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.text.*;
//import javax.swing.Action.*;
import java.io.*;

/**The writing pad and text manipulator.
 * Consists of practical text editing tools
 * as well as trix for true text fun.
 */
public class TextPad extends JTextPane implements StateEditable {
	private File file;
	private boolean changed = false;
	private String path;
	private UndoManager undoManager = new UndoManager();
	private Hashtable actions;
	//	private int tabSize = 0;
	private boolean autoIndent = false;
	private int tabSize = 4; // default tab size

	/**Constructs a <code>TextPad</code> that includes a file
	 * for the text area.
	 * @param aFile file to which the <code>TextPad</code> will
	 * save its text area contents
	 */
	public TextPad(File aFile) {
		file = aFile;
		//		tabSize = 4; // set the displayed tab size; add to preferences in the future
		applyDocumentSettings();
		// to allow multiple undos and listen for events

		// for new key-bindings
		createActionTable(this);
		InputMap imap = getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap amap = getActionMap();

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

		// (ctrl-p) go up a line
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK),
			"upChar");
		amap.put("upChar", getActionByName(DefaultEditorKit.upAction));

		// (ctrl-n) advance a line
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK),
			"downChar");
		amap.put("downChar", getActionByName(DefaultEditorKit.downAction));

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

		// (ctrl-d) delete next char		
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK),
			"deleteNextChar");
		amap.put(
			"deleteNextChar",
			getActionByName(DefaultEditorKit.deleteNextCharAction));

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

		// (ctrl-backspace) delete from caret to current word start
		// First discard JTextComponent's usual dealing with ctrl-backspace.
		// (enter) Auto-indent if Text Trix's option selected.
		addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent event) {
				char keyChar = event.getKeyChar();
				if (event.isControlDown()
					&& keyChar == KeyEvent.VK_BACK_SPACE) {
					event.consume();
				} else if (autoIndent && keyChar == KeyEvent.VK_ENTER) {
					autoIndent();
				} else if (
					autoIndent
						&& keyChar == KeyEvent.VK_TAB
						&& isLeadingTab()) {
					// performs the action after adding the tab
					//			System.out.println("autoIndent: " + autoIndent + ", char: " + keyChar + ", leading tab: " + isLeadingTab());
					indentCurrentParagraph(getTabSize());
				} else if (
					autoIndent
						&& keyChar == KeyEvent.VK_BACK_SPACE
						&& isLeadingTab()) {
					// performs the action before deleting the char
					unindentCurrentParagraph(getTabSize());
				}
			}
		});

		// Next apply own action
		imap.put(
			KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Event.CTRL_MASK),
			"deleteWord");
		amap.put("deleteWord", new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
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
				/*
				// alternate method, essentially same as document except
				// using the AccessibleJTextComponent class, 
				// whose serializable
				// objects may not be compatible w/ future releases
				// access the text component itself to tell it to perform
				// the deletions rather than building string manually, 
				// a slow task
				(new JTextComponent.AccessibleJTextComponent())
				.delete(wordPos, getCaretPosition());
				*/
			}
		});

		//       	setDefaultTabs(4);
		//	System.out.println(getFont().getFontName());

	}

	public void setDefaultTabs(int tabChars) {
		//	setFont(new Font("Dialog", Font.PLAIN, 12));
		//String[] fontFams = 
		//	for (j = 0; 
		//	System.out.println(getFont().getFontName());
		int charWidth = getFontMetrics(getFont()).charWidth(' ');
		int tabWidth = charWidth * tabChars;

		TabStop[] tabs = new TabStop[30]; // just enough to fit default frame
		for (int i = 0; i < tabs.length; i++)
			tabs[i] = new TabStop((i + 1) * tabWidth);
		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attribs = new SimpleAttributeSet();
		StyleConstants.setTabSet(attribs, tabSet);
		StyleConstants.setLeftIndent(attribs, 0);
		getStyledDocument()
			.setParagraphAttributes(0, getDocument().getLength() + 1,
		// next char
		attribs, false); // false to preserve default font
		//	setFont(new Font("Dialog", Font.PLAIN, 12));
		//	return true;
	}

	public void setNoTabs() { //int offset, int length) {
		//	System.out.println("set no tabs at position " + offset + " for " + length + " chars");
		TabStop[] tabs = new TabStop[1];
		tabs[0] = new TabStop(0);
		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attribs = new SimpleAttributeSet();
		StyleConstants.setTabSet(attribs, tabSet);
		/*
		getStyledDocument()
		    .setCharacterAttributes(offset,
					    length, // next char
					    attribs,
					    true); // false to preserve default font
		*/
		getStyledDocument()
			.setParagraphAttributes(0, getDocument().getLength() + 1,
		// next char
		attribs, false); // false to preserve default font
		/*
		StyleConstants.setBold(attribs, true);
		getStyledDocument()
		    .setCharacterAttributes(offset + 1,
					    length, // next char
					    attribs,
					    true); // false to preserve default font
		*/
	}

	public void setIndentTabs(int tabChars) {
		int i = 0;
		int j = 0;
		String s = getAllText();
		while (i < s.length() && (j = s.indexOf("\n", i + 1)) != -1) {
			//	    System.out.println("i: " + i + ", j: " + j);
			int tabs = leadingTabsCount(s, i);
			if (tabs > 0)
				indent(tabChars, tabs, i, j - i + 1);
			i = j + 1;
		}
		int tabs = leadingTabsCount(s, i);
		indent(tabChars, tabs, i, s.length() + 1);
		//       	setDefaultTabs(4);
		//	System.out.println(getFont().getFontName());

	}
	/*
	public boolean setDefaultTabs(int tabChars) {
	//	setFont(new Font("Dialog", Font.PLAIN, 12));
	//String[] fontFams = 
	//	for (j = 0; 
	//	System.out.println(getFont().getFontName());
	int charWidth = getFontMetrics(getFont()).charWidth(' ');
	int tabWidth = charWidth * tabChars;
	
	TabStop[] tabs = new TabStop[30]; // just enough to fit default frame
	for (int i = 0; i < tabs.length; i++) 
	    tabs[i] = new TabStop((i + 1) * tabWidth);
	TabSet tabSet = new TabSet(tabs);
	SimpleAttributeSet attribs = new SimpleAttributeSet();
	StyleConstants.setTabSet(attribs, tabSet);
	getStyledDocument()
	    .setParagraphAttributes(0,
				    getDocument().getLength() + 1,
				    attribs,
				    false); // false to preserve default font
	//	setFont(new Font("Dialog", Font.PLAIN, 12));
	return true;
	}
	*/

	/*
	public void clearIndentTabs() {
	SimpleAttributeSet attribs = new SimpleAttributeSet();
	StyleConstants.setLeftIndent(attribs, 0);
	getStyledDocument()
	    .setParagraphAttributes(0,
				    getDocument().getLength() + 1, // next char
				    attribs,
				    false); // false to preserve default font
	}
	*/

	public int leadingTabsCount(String s, int offset) {
		int tabs = 0;
		for (int i = offset; i < s.length() && s.charAt(i) == '\t'; i++)
			++tabs;
		//	System.out.println("I found " + tabs + " tabs");
		return tabs;
	}

	public void indent(int tabChars, int tabs, int offset, int length) {
		int charWidth = getFontMetrics(getFont()).charWidth(' ');
		int tabWidth = charWidth * tabChars;

		SimpleAttributeSet attribs = new SimpleAttributeSet();
		StyleConstants.setLeftIndent(attribs, tabs * tabWidth);
		//	StyleConstants.setFirstLineIndent(attribs, -1 * tabs * tabWidth);
		getStyledDocument().setParagraphAttributes(offset, length, // next char
		attribs, false); // false to preserve default font
	}

	public void indentCurrentParagraph(int tabChars) {
		String s = getAllText();
		//	int caretPos = getCaretPosition();
		//	System.out.println("caret pos: " + caretPos);
		int start = reverseIndexOf(s, "\n", getCaretPosition()) + 1;
		int tabs = leadingTabsCount(s, start);
		indent(tabChars, tabs, start, s.indexOf("\n", start) - start);
		//	setNoTabs(start, 2);
	}

	public void unindentCurrentParagraph(int tabChars) {
		String s = getAllText();
		//	int caretPos = getCaretPosition();
		//	System.out.println("caret pos: " + caretPos);
		int start = reverseIndexOf(s, "\n", getCaretPosition()) + 1;
		int tabs = leadingTabsCount(s, start) - 1;
		indent(tabChars, tabs, start, s.indexOf("\n", start) - start);
		//	setNoTabs(start, tabs - 1);
	}

	public int reverseIndexOf(String str, String searchStr, int offset) {
		int i = offset - 1;
		//	System.out.println("len: " + searchStr.length());
		while (i >= 0
			&& !str.substring(i, i + searchStr.length()).equals(searchStr)) {
			i--;
		}
		return i;
	}

	public boolean isLeadingTab() {
		int i = getCaretPosition() - 1;
		try {
			while (i > 0 && getDocument().getText(i, 1).equals("\t"))
				i--;
			return i == 0 || getDocument().getText(i, 1).equals("\n");
		} catch (BadLocationException e) {
			System.out.println("Can't find no tabs, dude.");
			return false;
		}
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

	/**Gets the current tab display size.
	 * @return tab dispaly size
	 *
	 public int getTabSize() {
	 return tabSize;
	 }
	*/
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
	}

	/**Sets the file to a path.
	 * @param path path of file to save to
	 */
	public void setFile(String path) {
		file = new File(path);
	}

	/**Execute an edit, capture the state changes, and make the changes
	 * undoable.
	 * @param text text to edit
	 */
	public void setUndoableText(String text) {
		StateEdit stateEdit = new StateEdit(this);
		setText(text);
		stateEdit.end();
		undoManager.addEdit((UndoableEdit) stateEdit);
	}

	/**Set the tab display size.
	 * @param size tab display size, in average character spaces
	 *
	 public void setTabSize(int size) {
	 tabSize = size;
	 }
	*/
	/**Sets the auto-indent selection.
	 * @param b <code>true</code> to auto-indent
	 */
	public void setAutoIndent(boolean b) {
		if (autoIndent = b) {
			setNoTabs(); //0, 0);
			setIndentTabs(getTabSize());
		} else {
			setDefaultTabs(getTabSize());
		}
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
		if (undoManager.canUndo())
			undoManager.undo();
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
		Document doc = getDocument();
		doc.addUndoableEditListener(undoManager);
		if (autoIndent) {
			setIndentTabs(getTabSize());
			setNoTabs();
		} else {
			setDefaultTabs(getTabSize());
			//	    setDefaultTabs(4);
		}
		/* no plain docs in JTextPane
		   if (doc.getClass() == PlainDocument.class) {
		   doc.putProperty(PlainDocument.tabSizeAttribute, new Integer(tabSize));
		   }
		*/
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

	public void replaceAllText(String s) {
		try {
			//	    System.out.println(s);
			Document doc = getDocument();
			int len = doc.getLength();
			doc.remove(0, len);
			doc.insertString(0, s, null);
		} catch (BadLocationException e) {
			System.out.println("Couldn't replace it all.");
		}
	}

	/**Converts the pad to a plain text view.
	 * Undoable.
	 */
	public void viewPlain() {
		StateEdit stateEdit = new StateEdit(this);
		//	String text = getAllText();
		String text = getText();
		setEditorKit(createDefaultEditorKit()); // revert to default editor kit
		// use the default editor kit's <code>DefaultStyledDocument</code>
		setDocument(getEditorKit().createDefaultDocument());
		//	replaceAllText(text);
		setText(text);
		applyDocumentSettings();
		stateEdit.end();
		undoManager.addEdit((UndoableEdit) stateEdit);
		//       	setDefaultTabs(4);
	}

	/**Converts the pad to an HTML text view.
	 * The underlying text may have HTML tags added.
	 */
	public void viewHTML() {
		StateEdit stateEdit = new StateEdit(this);
		String text = getText();
		setDocument(getEditorKit().createDefaultDocument());
		setContentType("text/html");
		setText(text);
		applyDocumentSettings();
		//	replaceAllText(text);
		stateEdit.end();
		undoManager.addEdit((UndoableEdit) stateEdit);
		//       	setDefaultTabs(4);
	}

	/**Converts the pad to an RTF tex view, if possible.
	* If the document is not in RTF format, the pad reverts to its original setting.
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
			//	} else {
			// if placed before the undo, it would undo this command and 
			// not restore the text w/o another undo
			//	    setDefaultTabs(4);
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

		//	System.out.println(getText());
		// check that new caret not at start of string; 
		// skip backward as long as delimiters
		/*
		while (newCaretPos > 0
		       && delimiters.indexOf(getText().charAt(newCaretPos)) != -1) {
		    newCaretPos--;
		}
		*/
		try {
			while (newCaretPos > 0
				&& delimiters.indexOf(getDocument().getText(newCaretPos, 1))
					!= -1) {
				newCaretPos--;
			}

			// check that new caret not at start of string;
			// now skip backward as long as not delimiters; bring caret to next space, etc
			/*
			  while (newCaretPos > 0 
			  && (delimiters.indexOf(getText().charAt(newCaretPos - 1)) == -1)) {
			  newCaretPos--;
			  }
			*/
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

		/*		
		// check that new caret not at end of string; 
		// skip forward as long as not delimiters
		while (newCaretPos < textLen
		       && delimiters.indexOf(getText().charAt(newCaretPos)) == -1) {
		    newCaretPos++;
		}
		// check that new caret not at end of string; 
		// now skip forward as long as delimiters; bring to next letter, etc
		while (newCaretPos < textLen
		       && (delimiters.indexOf(getText().charAt(newCaretPos)) != -1)) {
		    newCaretPos++;
		}
		*/
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
		/*
		String text = getText();
		char c;
		// go back 2: one for the hard return, one to check the previous character
		for (int n = getCaretPosition() - 2; n >= 0 && (c = text.charAt(n)) != '\n'; n--) {
		    if (c == '\t') {
			tabs++;
		    }
		}
		*/
		// go back 2 to skip the hard return that makes the new line
		// requiring indentation
		try {
			int n = 0;
			int tabs = 0;
			for (n = getCaretPosition() - 2;
				n >= 0 && !getDocument().getText(n, 1).equals("\n");
				n--);
			if (n == 0)
				n = -1;
			for (tabs = 0; getDocument().getText(++n, 1).equals("\t"); tabs++);

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

	public void setTabSize(int aTabSize) {
		tabSize = aTabSize;
	}

	public int getTabSize() {
		return tabSize;
	}
}
