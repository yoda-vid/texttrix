/*TextPad.java
 *Text Trix
 *a goofy gui editor

 *Copyright (c) 2002, David Young
 *All rights reserved.

 *Redistribution and use in source and binary forms, with or without
 *modification, are permitted provided that the following conditions 
 *are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Text Trix nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 *OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

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

/**The writing pad and text manipulator.
   Consists of standard text editing methods
   as well as special, silly ones for true text fun.
*/
public class TextPad extends JTextArea {
	boolean changed = false;
	String path;
	DocumentListener docListener = new TextPadDocListener();	
	UndoManager undoManager = new UndoManager();
	Hashtable actions;
	
    public TextPad(int w, int h, String aPath) {
		super(w, h);
		path = aPath;
		getDocument().addDocumentListener(docListener);
		getDocument().addUndoableEditListener(undoManager);
		setTabSize(4); // probably add to preferences later

		

//		Keymap keymap = addKeymap("MyKeymap", getKeymap());

		createActionTable(this);
		InputMap imap = getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap amap = getActionMap();
		
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK), 
				"forwardChar");
		amap.put("forwardChar", getActionByName(
					DefaultEditorKit.forwardAction));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK), 
				"backwardChar");
		amap.put("backwardChar", getActionByName(
					DefaultEditorKit.backwardAction));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK), 
				"upChar");
		amap.put("upChar", getActionByName(
					DefaultEditorKit.upAction));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK), 
				"downChar");
		amap.put("downChar", getActionByName(
					DefaultEditorKit.downAction));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK), 
				"beginLine");
		amap.put("beginLine", getActionByName(
					DefaultEditorKit.beginLineAction));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK), 
				"endLineChar");
		amap.put("endLineChar", getActionByName(
					DefaultEditorKit.endLineAction));
/*
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.ALT_MASK), 
				"beginWord");
		amap.put("beginWord", getActionByName(
					DefaultEditorKit.beginWordAction));

		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.ALT_MASK), 
				"nextWord");
		amap.put("nextWord", getActionByName(
					DefaultEditorKit.nextWordAction));
*/
		/*
		keymap.addActionForKeyStroke(key, DefaultEditorKit.backwardAction);

		action = getActionByName(DefaultEditorKit.forwardAction);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
		keymap.addActionForKeyStroke(key, action);
				
		action = getActionByName(DefaultEditorKit.upwardAction);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
		keymap.addActionForKeyStroke(key, action);
		
		action = getActionByName(DefaultEditorKit.downwardAction);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
		keymap.addActionForKeyStroke(key, action);

		action = getActionByName(DefaultEditorKit.nextWordAction);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.META_MASK);
		keymap.addActionForKeyStroke(key, action);
		
		action = getActionByName(DefaultEditorKit.beginWordAction);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.META_MASK);
		keymap.addActionForKeyStroke(key, action);
		
		action = getActionByName(DefaultEditorKit.beginLineAction);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK);
		keymap.addActionForKeyStroke(key, action);
		
		action = getActionByName(DefaultEditorKit.endLineAction);
		key = KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK);
		keymap.addActionForKeyStroke(key, action);
		*/
	}

	public boolean getChanged() {
		return changed;
	}

	public void setChanged(boolean b) {
		changed = b;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String aPath) {
		path = aPath;
	}

	public void undo() {
		if (undoManager.canUndo())
			undoManager.undo();
	}

	public void redo() {
		if (undoManager.canRedo())
			undoManager.redo();
	}

	private void createActionTable(JTextComponent txtComp) {
		actions = new Hashtable();
		Action actionsArray[] = txtComp.getActions();
		for (int i = 0; i < actionsArray.length; i++) {
			Action a = actionsArray[i];
			actions.put(a.getValue(Action.NAME), a);
		}
	}

	private Action getActionByName(String name) {
		return (Action)(actions.get(name));
	}

    /** Strips inserted, extra hard returns.  For example,
	unformatted email arrives with hard returns
	inserted after every line; this method
	strips all but the paragraph, double-spaced
	hard returns.  Text within <code>&#60pre&#62</code>
	and <code>&#60/pre&#62</code> tags are
	left untouched.  Additionally, each line whose first
	non-space character is a dash or an asterisk
	gets its own line.
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
	    // see if first character is a space
//	    if (space == singleReturn + 1) {
//			spaces++;
			// check each subsequent character to see if space
			while (s.substring(singleReturn + spaces + 1,
					   singleReturn + spaces + 2).equals(" ")) {
		    	spaces++;
			}
//	    }

	    if (startPre != -1 && startPre < singleReturn) {
			if (endPre != -1) {
		    	stripped = stripped 
					+ s.substring(0, endPre + 6);
		    	s = s.substring(endPre + 6);
			} else {
		    	// if user forgets closing pre tag, goes to end
		    	stripped = stripped + s;
		    	s = "";
			}
	    } else if (singleReturn == -1) {
			stripped = stripped + s;
			s = "";
	    } else if (singleReturn == doubleReturn) {
			stripped = stripped 
		    	+ s.substring(0, doubleReturn + 2);
			s = s.substring(doubleReturn + 2);
	    } else if (dash == singleReturn + 1 + spaces
		    	   || asterisk == singleReturn + 1 + spaces) {
			// + 2 to pick up the dash
			stripped = stripped 
		    	+ s.substring(0, singleReturn + 2 + spaces);
			s = s.substring(singleReturn + 2 + spaces);
		} else if (tab == singleReturn + 1) {
			stripped = stripped + s.substring(0, singleReturn + 1);
			s = s.substring(singleReturn + 1);
	    } else {
			stripped = stripped 
		    	+ s.substring(0, singleReturn) + " ";
			s = s.substring(singleReturn + 1);
	    }
	}
	return stripped;
    }

	private class TextPadDocListener implements DocumentListener {
		
		public void insertUpdate(DocumentEvent e) {
			changed = true;
		}

		public void removeUpdate(DocumentEvent e) {
			changed = true;
		}

		public void changedUpdate(DocumentEvent e) {
		}
	}
}
