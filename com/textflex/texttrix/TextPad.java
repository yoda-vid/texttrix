/* TextPad.java
 * Text Trix
 * a goofy gui editor
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
import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.text.*;
import javax.swing.Action.*;
import java.io.*;

/**The writing pad and text manipulator.
   Consists of standard text editing methods
   as well as special, silly ones for true text fun.
*/
public class TextPad extends JEditorPane implements StateEditable{
	private File file;
	private boolean changed = false;
	private String path;
	private UndoManager undoManager = new UndoManager();
	private Hashtable actions;
//	private Hashtable state;
	private int tabSize = 0;

	/**Constructs a <code>TextPad</code> that includes a file
	 * for the text area.
	 * @param w width in text columns; only approximate
	 * @param h height in text rows; only approximate
	 * @param aFile file to which the <code>TextPad</code> will
	 * save its text area contents
	 */
	public TextPad(int w, int h, File aFile) {
//		super(w, h);
		file = aFile;
		// to listen for keypad events
		// to allow multiple undos
		tabSize = 4;
		applyDocumentSettings();
//		setTabSize(4); // probably add to preferences later

		// for new key-bindings
		createActionTable(this);
		InputMap imap = getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap amap = getActionMap();
		
		// (ctrl-f) advance a character
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK), 
				"forwardChar");
		amap.put("forwardChar", getActionByName(
					DefaultEditorKit.forwardAction));

		// (ctrl-b) go back a character
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK), 
				"backwardChar");
		amap.put("backwardChar", getActionByName(
					DefaultEditorKit.backwardAction));

		// (ctrl-p) go up a line
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK), 
				"upChar");
		amap.put("upChar", getActionByName(
					DefaultEditorKit.upAction));

		// (ctrl-n) advance a line
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK), 
				"downChar");
		amap.put("downChar", getActionByName(
					DefaultEditorKit.downAction));

		// (ctrl-a) go to the beginning of the line
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK), 
				"beginLine");
		amap.put("beginLine", getActionByName(
					DefaultEditorKit.beginLineAction));

		// (ctrl-e) go to the end of the line
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK), 
				"endLineChar");
		amap.put("endLineChar", getActionByName(
					DefaultEditorKit.endLineAction));
		
		// (alt-b) go to beginning of current word
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.ALT_MASK), 
				"wordStart");
		amap.put("wordStart", new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				setCaretPosition(getWordPosition());
			}
		});

		// (alt-f) go to beginning of next word
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.ALT_MASK), 
				"nextWord");
		amap.put("nextWord", new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				setCaretPosition(getNextWordPosition());
			}
		});

		// (ctrl-backspace) delete from caret to current word start
		// First discard JTextComponent's usual dealing with ctrl-backspace.
		// Auto-indent if Text Trix's option selected.
		addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent event) {
				char keyChar = event.getKeyChar();
				if (event.isControlDown() && keyChar 
					== KeyEvent.VK_BACK_SPACE) {
					event.consume();
				} else if (TextTrix.getAutoIndent() 
					&& keyChar == KeyEvent.VK_ENTER) {
					autoIndent();
				}
			}
		});
		
		// Next apply own action
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Event.CTRL_MASK),
				"deleteWord");
		amap.put("deleteWord", new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				int wordPos = getWordPosition();
				// delete via the document methods rather than building
				// string manually, a slow task.  Uses document rather than
				// AccessibleJTextComponent in case used for serialization:
				// serialized objects of this class won't be compatible w/
				// future releases
				try {
					getDocument().remove(wordPos, getCaretPosition() - wordPos);
					setCaretPosition(wordPos);
				} catch(BadLocationException b) {
					System.out.println("Deletion out of range.");
				}
				/*
				// alternate method, essentially same as document except
				// using the AccessibleJTextComponent class, whose serializable
				// objects may not be compatible w/ future releases
				// access the text component itself to tell it to perform
				// the deletions rather than building string manually, a slow task
				(new JTextComponent.AccessibleJTextComponent())
					.delete(wordPos, getCaretPosition());
				*/
			}
		});
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
		} catch(IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**Gets the file's name.
	 * @return filename
	 */
	public String getName() {
		return file.getName();
	}

	public String getDir() {
		String dir = "";
		return ((dir = file.getParent()) != null) ? dir : "";
	}

	public int getTabSize() {
		return tabSize;
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

	public void setUndoableText(String text) {
		StateEdit stateEdit = new StateEdit(this);
		setText(text);
		stateEdit.end();
		undoManager.addEdit((UndoableEdit)stateEdit);
	}
	
	public void setTabSize(int size) {
		tabSize = size;
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

	public void applyDocumentSettings() {
		Document doc = getDocument();
		doc.addUndoableEditListener(undoManager);
		if (doc.getClass() == PlainDocument.class) {
			doc.putProperty(PlainDocument.tabSizeAttribute, new Integer(tabSize));
//			System.out.println("That's me");
		}
	}

	public boolean isEmpty() {
		String text = getText();
		return ((text == null) || !(new StringTokenizer(text)).hasMoreTokens()) ? true : false;
	}

	public void viewPlain() {
		String text = getText();
		StateEdit stateEdit = new StateEdit(this);
		setDocument(getEditorKit().createDefaultDocument());
		setContentType("text/plain");
		applyDocumentSettings();
		setText(text);
		stateEdit.end();
		undoManager.addEdit((UndoableEdit)stateEdit);
	}

	public void viewHTML() {
		String text = getText();
		StateEdit stateEdit = new StateEdit(this);
		setDocument(getEditorKit().createDefaultDocument());
		setContentType("text/html");
		applyDocumentSettings();
		setText(text);
		stateEdit.end();
		undoManager.addEdit((UndoableEdit)stateEdit);
	}

	public void viewRTF() {
		String text = getText();
		StateEdit stateEdit = new StateEdit(this);
		setDocument(getEditorKit().createDefaultDocument());
		setContentType("text/rtf");
		applyDocumentSettings();
		setText(text);
		stateEdit.end();
		undoManager.addEdit((UndoableEdit)stateEdit);
		if (getText() == null) {
			undo();
			/*
			setDocument(getEditorKit().createDefaultDocument());
			setContentType("text/plain");
			applyDocumentSettings();
			setText(text);
			*/
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
		
		// check that new caret not at start of string; 
		// skip backward as long as delimiters
		while (newCaretPos > 0
				&& delimiters.indexOf(getText().charAt(newCaretPos)) != -1) {
			newCaretPos--;
		}
		// check that new caret not at start of string;
		// now skip backward as long as not delimiters; bring caret to next space, etc
		while (newCaretPos > 0 
				&& (delimiters.indexOf(getText().charAt(newCaretPos - 1)) == -1)) {
			newCaretPos--;
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

		return newCaretPos;
	}

	/**Auto-indent to the previous line's tab position.
	 */
	public void autoIndent() {
		int tabs = 0;
		String text = getText();
		char c;
		for (int n = getCaretPosition() - 2; 
				n >= 0 && (c = text.charAt(n)) != '\n'; n--) {
			if (c == '\t') {
				tabs++;
			}
		}
		StringBuffer tabStr = new StringBuffer(tabs);
		for (int i = 0; i < tabs; i++) {
			tabStr.append('\t');
		}
		try {
			getDocument().insertString(getCaretPosition(), tabStr.toString(), null);
		} catch(BadLocationException e) {
			System.out.println("Insert location " + getCaretPosition() + " does not exist");
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
		return (Action)(actions.get(name));
	}

	public void storeState(Hashtable state) {
		state.put("contenttype", getContentType());
//		System.out.println("Stored:" + (String)state.get("contenttype"));
		String text = getText();
		if (text == null) 
			text = "";
		state.put("text", text);
		// store the current document so can reapply undos later
		state.put("doc", getDocument());
/*		Document d = (Document)state.get("doc");
		String s = "";
		try { s = d.getText(0, d.getLength()); } catch(BadLocationException e) {}
		System.out.println("Stored:" + s);
*/
	}

	public void restoreState(Hashtable state) {
		String contentType = (String)state.get("contenttype");
		String text = (String)state.get("text");
//		setDocument(getEditorKit().createDefaultDocument());
		if (contentType != null) 
			setContentType(contentType);
		// set document after setting the editor kit so can apply the doc's settings
		setDocument((Document)state.get("doc"));
		if (text != null) 
			setText(text);
//		applyDocumentSettings();
/*			String s = "";
			try { s = doc.getText(0, doc.getLength()); } catch(BadLocationException e) {}
			System.out.println("Stored:" + s);
		}
		System.out.println("Restored:" + contentType);
		if (doc == null) 
			System.out.println("The doc's not with us");
*/
	}
}
