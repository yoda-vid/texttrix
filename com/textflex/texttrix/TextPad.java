/* Tools.java    
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
import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.text.*;
import javax.swing.Action.*;
import java.io.*;

/**The writing pad and text manipulator.
 * Consists of practical text editing tools
 * as well as trix for true text fun.
 */
public class TextPad extends JTextPane implements StateEditable{
    private File file;
    private boolean changed = false;
    private String path;
    private UndoManager undoManager = new UndoManager();
    private Hashtable actions;
    //	private int tabSize = 0;
    private boolean autoIndent = false;

    /**Constructs a <code>TextPad</code> that includes a file
     * for the text area.
     * @param aFile file to which the <code>TextPad</code> will
     * save its text area contents
     */
    public TextPad(File aFile) {
	file = aFile;
	//		tabSize = 4; // set the displayed tab size; add to preferences in the future
	applyDocumentSettings(); // to allow multiple undos and listen for events

	// for new key-bindings
	createActionTable(this);
	InputMap imap = getInputMap(JComponent.WHEN_FOCUSED);
	ActionMap amap = getActionMap();
		
	// (ctrl-f) advance a character
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK), 
		 "forwardChar");
	amap.put("forwardChar", 
		 getActionByName(DefaultEditorKit.forwardAction));

	// (ctrl-b) go back a character
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK), 
		 "backwardChar");
	amap.put("backwardChar", 
		 getActionByName(DefaultEditorKit.backwardAction));

	// (ctrl-p) go up a line
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK), 
		 "upChar");
	amap.put("upChar", 
		 getActionByName(DefaultEditorKit.upAction));

	// (ctrl-n) advance a line
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK), 
		 "downChar");
	amap.put("downChar", getActionByName(DefaultEditorKit.downAction));

	// (ctrl-a) go to the beginning of the line
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK), 
		 "beginLine");
	amap.put("beginLine", 
		 getActionByName(DefaultEditorKit.beginLineAction));

	// (ctrl-e) go to the end of the line
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK), 
		 "endLineChar");
	amap.put("endLineChar", 
		 getActionByName(DefaultEditorKit.endLineAction));

	// (ctrl-d) delete next char		
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK), 
		 "deleteNextChar");
	amap.put("deleteNextChar", 
		 getActionByName(DefaultEditorKit.deleteNextCharAction));
		
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
		    } else if (autoIndent && keyChar == KeyEvent.VK_ENTER) {
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
			// call getDocument() each time since document may change
			// with call to viewPlain, viewHTML, or viewRTF
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
	undoManager.addEdit((UndoableEdit)stateEdit);
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
	autoIndent = b;
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
	return ((text == null) || !(new StringTokenizer(text)).hasMoreTokens()) ? true : false;
    }

    /**Converts the pad to a plain text view.
     * Undoable.
     */
    public void viewPlain() {
	String text = getText();
	StateEdit stateEdit = new StateEdit(this);
	setEditorKit(createDefaultEditorKit()); // revert to default editor kit
	// use the default editor kit's <code>DefaultStyledDocument</code>
	setDocument(getEditorKit().createDefaultDocument());
	applyDocumentSettings();
	setText(text);
	stateEdit.end();
	undoManager.addEdit((UndoableEdit)stateEdit);
    }

    /**Converts the pad to an HTML text view.
     * The underlying text may have HTML tags added.
     */
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

    /**Converts the pad to an RTF tex view, if possible.
     * If the document is not in RTF format, the pad reverts to its original setting.
     */
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
	// go back 2: one for the hard return, one to check the previous character
	for (int n = getCaretPosition() - 2; n >= 0 && (c = text.charAt(n)) != '\n'; n--) {
	    if (c == '\t') {
		tabs++;
	    }
	}
	// construct a string of all the tabs
	StringBuffer tabStr = new StringBuffer(tabs);
	for (int i = 0; i < tabs; i++) {
	    tabStr.append('\t');
	}
	// add the tabs
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
	EditorKit editorKit= (EditorKit)state.get("editorkit");
	StyledDocument doc = (StyledDocument)state.get("doc");
	if (editorKit != null) 
	    setEditorKit(editorKit);
	// set document after setting the editor kit so can apply the doc's settings
	if (doc !=null) 
	    setDocument(doc);
    }
}
