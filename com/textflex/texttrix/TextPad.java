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
	DocumentListener docListener = new TextPadDocListener();	
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
		getDocument().addDocumentListener(docListener);
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
/*
 * need some decisions to go to the appropriate position of each word
 * 
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.ALT_MASK), 
				"beginWord");
		amap.put("beginWord", getActionByName(
					DefaultEditorKit.beginWordAction));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.ALT_MASK), 
				"nextWord");
		amap.put("nextWord", getActionByName(
					DefaultEditorKit.nextWordAction));
*/
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

    /** Strips inserted, extra hard returns.  For example,
	unformatted email arrives with hard returns inserted after 
	every line; this method strips all but the paragraph, double-spaced
	hard returns.
	Text within <code>&#60pre&#62</code>
	and <code>&#60/pre&#62</code> tags are
	left untouched.  Additionally, each line whose first
	non-space character is a dash, asterisk, or tab
	gets its own line.  The line above such lines also gets to remain
	by itself.
	@param s the full text from which to strip
	extra hard returns
	@return stripped text
    */
    public String stripExtraHardReturns(String s) {
	String stripped = "";

	while (!s.equals("")) {
	    int singleReturn = s.indexOf("\n");
	    int doubleReturn = s.indexOf("\n\n");
	    int dash = s.indexOf("-");
	    int asterisk = s.indexOf("*");
//	    int space = s.indexOf(" ", singleReturn + 1);
		int tab = s.indexOf("\t", singleReturn + 1);
	    int spaces = 0;
	    int startPre = s.indexOf("<pre>");
	    int endPre = s.indexOf("</pre>");

	    // only catch dashes and asterisks after newline
	    while (dash != -1 && dash < singleReturn) {
		dash = s.indexOf("-", dash + 1);
	    }
	    while (asterisk != -1 && asterisk < singleReturn) {
		asterisk = s.indexOf("*", asterisk + 1);
	    }

	    // find all leading spaces
		int oneAfterSingleReturn = singleReturn + 1;
		// check whether have exceeded length of text
		// add one to spaces if next leading character has a space.
		while (s.length() > oneAfterSingleReturn && 
				(String.valueOf(s.charAt(oneAfterSingleReturn))).equals(" ")) {
		    spaces++;
		}

		// skip "pre"-delimited sections
	    if (startPre != -1 && startPre < singleReturn) {
			// go to the end of the "pre" section
			if (endPre != -1) {
		    	stripped = stripped 
					+ s.substring(0, endPre + 6);
		    	s = s.substring(endPre + 6);
		    // if user forgets closing "pre" tag, goes to end
			} else {
		    	stripped = stripped + s;
		    	s = "";
			}
		// join singly-returned lines
	    } else if (singleReturn == -1) {
			stripped = stripped + s;
			s = "";
		// preserve doubly-returned lines, as between paragraphs
	    } else if (singleReturn == doubleReturn) {
			stripped = stripped 
		    	+ s.substring(0, doubleReturn + 2);
			s = s.substring(doubleReturn + 2);
		// preserve separate lines for lines starting w/
		// dashes or asterisks or spaces before them
	    } else if (dash == singleReturn + 1 + spaces
		    	   || asterisk == singleReturn + 1 + spaces) {
			// + 2 to pick up the dash
			stripped = stripped 
		    	+ s.substring(0, singleReturn + 2 + spaces);
			s = s.substring(singleReturn + 2 + spaces);
		// preserve separate lines for ones starting with tabs
		} else if (tab == singleReturn + 1) {
			stripped = stripped + s.substring(0, singleReturn + 1);
			s = s.substring(singleReturn + 1);
		// join the tail-end of the text
	    } else {
			stripped = stripped 
		    	+ s.substring(0, singleReturn) + " ";
			s = s.substring(singleReturn + 1);
	    }
	}
	return stripped;
    }

	/**Listens for text changes in the text area.
	 */
	private class TextPadDocListener implements DocumentListener {
	
		/**Flags a text insertion.
		 * @param e insertion event
		 */
		public void insertUpdate(DocumentEvent e) {
			changed = true;
		}

		/**Flags a text removal.
		 * @param e removal event
		 */
		public void removeUpdate(DocumentEvent e) {
			changed = true;
		}

		/**Flags any sort of text change.
		 * @param e any text change event
		 */
		public void changedUpdate(DocumentEvent e) {
		}
	}
}
