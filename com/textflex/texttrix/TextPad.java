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
public class TextPad extends JTextArea {
	File file;
	boolean changed = false;
	String path;
	UndoManager undoManager = new UndoManager();
	Hashtable actions;

	/**Constructs a <code>TextPad</code> that includes a file
	 * for the text area.
	 * @param w width in text columns; only approximate
	 * @param h height in text rows; only approximate
	 * @param aFile file to which the <code>TextPad</code> will
	 * save its text area contents
	 */
    public TextPad(int w, int h, File aFile) {
		super(w, h);
		file = aFile;
		// to listen for keypad events
		// to allow multiple undos
		getDocument().addUndoableEditListener(undoManager);
		setTabSize(4); // probably add to preferences later

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
		// First need to discard JTextComponent's usual dealing with ctrl-backspace
		addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent event) {
				char keyChar = event.getKeyChar();
				if (event.isControlDown() && keyChar == KeyEvent.VK_BACK_SPACE)
					event.consume();
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
				
				// alternate method, using StringBuffer; essentially same as
				// building string manually
				setText((new StringBuffer(getText())).delete(wordPos, getCaretPosition()).toString());

				// alternate method, building string manually
				String text = getText();
				setText(text.substring(0, wordPos)
					+ text.substring(getCaretPosition()));
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

	/**Gets the index of the current word's first character.
	 * @return string index of either the current word or the previous
	 * word if only delimiters intervene between the word and
	 * the caret position.
	 */
	public int getWordPosition() {
		int caretPos = getCaretPosition(); // current caret position
		int newCaretPos = caretPos - 1; // moving caret position
		String delimiters = " .,;:-\\\'\"/_\t\n";
		
		// check that new caret not at start of string
		while (newCaretPos > 0
				&& delimiters.indexOf(getText().charAt(newCaretPos)) != -1) {
			newCaretPos--;
		}
		while (newCaretPos > 0 
				&& (delimiters.indexOf(getText().charAt(newCaretPos - 1)) == -1)) {
			newCaretPos--;
		}

		return (newCaretPos <= 0) ? 0 : newCaretPos;
	}
		/*
		 * This method fails to skip over multiple delimiters.
		while (newCaretPos > 0 && 
				// check for space, comma, semicolon, etc
				(((checkChar = getText().charAt(--newCaretPos)) != ' ')
				 && checkChar != ','
				 && checkChar != ';'
				 && checkChar != ':'
				 && checkChar != '-'
				 && checkChar != '\\'
				 && checkChar != '/'
				 && checkChar != '\"'
				 && checkChar != '\'')
				// if space, comma, etc, check before exiting to ensure that not
				// directly before orig caret position
				|| newCaretPos == caretPos - 1);
				
		// if caret didn't move or reached start of string,
		// set new caret to one position before string since will increment
		if (newCaretPos == caretPos - 1 || newCaretPos == 0) 
			newCaretPos = -1;
		// place cursor after space, comma, etc, or at start of string
		return ++newCaretPos;
	}
	*/

	/**Gets the index of the next word's first character.
	 * @return string index of the next word's beginning, past
	 * any delimiters
	 */
	public int getNextWordPosition() {
		int caretPos = getCaretPosition(); // current caret position
		int newCaretPos = caretPos; // moving caret position
		int textLen = getText().length(); // end of text
		String delimiters = " .,;:-\\\'\"/_\t\n";
		
		// check that new caret not at start of string
		while (newCaretPos < textLen
				&& delimiters.indexOf(getText().charAt(newCaretPos)) == -1) {
			newCaretPos++;
		}
		while (newCaretPos < textLen
				&& (delimiters.indexOf(getText().charAt(newCaretPos)) != -1)) {
			newCaretPos++;
		}

		return newCaretPos;
	}
		/* This method does not skip over multiple delimiters.
		int caretPos; // current caret position
		int newCaretPos; // caret position progressing to desired position
		caretPos = newCaretPos = getCaretPosition();
		char checkChar;
		int textLen = getText().length();
		// check that new caret not at end of string
		while (newCaretPos < textLen && 
				// check for space, comma, semicolon, etc
				((checkChar = getText().charAt(newCaretPos++)) != ' ')
				&& checkChar != ','
				&& checkChar != ';'
				&& checkChar != ':'
				&& checkChar != '-'
				&& checkChar != '\\'
				&& checkChar != '/'
				&& checkChar != '\"'
				&& checkChar != '\'');
		return newCaretPos;
	}
	*/
	
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
}
