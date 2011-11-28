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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

import java.lang.Runnable;
import java.lang.Thread;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyledEditorKit;
import java.awt.EventQueue;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.Point;
import java.awt.Toolkit;
import javax.swing.KeyStroke;

/**Creates the status bar in a worker thread.
*/
class StatusBarCreator implements Runnable {
	
	private TextTrix ttx;
	private int lastLine = 0; // most recent line highlighted
	private String lastWord = ""; // most recent word found
	private JTextField wordFindFld = null;
	
	public StatusBarCreator(TextTrix aTtx) {
		ttx = aTtx;
	}

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
				JPanel statusBarPanel = ttx.getStatusBarPanel();
				statusBarPanel.setLayout(layout);
				
				// Make the status bar components;
				// statusBar already created so that could accept line updates
				
				
				
				// progres bar for showing document loading status;
				// object should have already been created
				JProgressBar statusProgress = ttx.getStatusProgress();
				statusProgress.setString("");
				statusProgress.setStringPainted(true);
				
				
				
				// Line Find
				JLabel lineNumLbl = new JLabel("Line Find:");
				lineNumLbl.setToolTipText("GoTo the line number as it's typed");
				final JTextField lineNumFld = ttx.getLineNumFld();
				// caret listener to find-as-you-type the line number into the 
				// text box
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
				lineNumFld.addFocusListener(new FocusAdapter() {
					public void focusGained(FocusEvent e) {
						lineNumFld.selectAll();
					}
				});
				
				
				
				// Word Find
				JLabel wordFindLbl = new JLabel("Word Find:");
				wordFindLbl.setToolTipText("GoTo the word as it's typed");
				wordFindFld = ttx.getWordFindFld();
				// caret listener to find-as-you-type into the text box
				wordFindFld.addCaretListener(new CaretListener() {
					public void caretUpdate(CaretEvent e) {
						String lineStr = wordFindFld.getText();
						// do nothing if empty box
						if (!lineStr.equals("") 
								&& !lastWord.equalsIgnoreCase(lineStr)) {
							// otherwise, parse string, assuming key listener 
							// has filtered out non-digits
							findSeq(lineStr, -1);
						}
					}
				});
				// find word when Enter is pressed
				wordFindFld.addKeyListener(new KeyAdapter() {
					public void keyTyped(KeyEvent evt) {
						char keyChar = evt.getKeyChar();
						if (keyChar == KeyEvent.VK_ENTER) {
							evt.consume();
							String lineStr = wordFindFld.getText();
//						System.out.println("here");
							findSeq(lineStr, -1);
						}
					}
					
					// Advance to next ocurrance with F3 or Meta-G;
					// move to previous occurrance with Shift-F3 or Shift-Meta-G
					public void keyPressed(KeyEvent evt) {
//						System.out.println(evt.getKeyCode() + "");
						int code = evt.getKeyCode();
						if (code == KeyEvent.VK_F3
								|| (code == KeyEvent.VK_G 
										&& evt.isMetaDown())) {
							evt.consume();
							String lineStr = wordFindFld.getText();
							if (evt.isShiftDown()) {
								findSeqReverse(lineStr, 
									ttx.getSelectedTextPad().getSelectionStart());
							} else {
								findSeq(lineStr, 
									ttx.getSelectedTextPad().getSelectionEnd());
							}
						}
					}
				});
				wordFindFld.addFocusListener(new FocusAdapter() {
					public void focusGained(FocusEvent e) {
						wordFindFld.selectAll();
					}
				});
				
				// Filter input to ensure <=256 characters
				AbstractDocument doc = null;
				final int MAX_CHARS = 256;
				Document fldDoc = wordFindFld.getDocument();
				if (fldDoc instanceof AbstractDocument) {
					doc = (AbstractDocument) fldDoc;
					doc.setDocumentFilter(new DocumentSearchFilter(MAX_CHARS));
				}
				
				// tool tip for the input field
				wordFindFld.setToolTipText(
					"<html>Press F3 (or Cmd-G) to find the next occurrence"
					+ "<br />or Shift+F3 (or Sh-Cmd-G) for the previous occurrence.</hmlm>");
				
				
				
				// status bar popup for the line saver
				JPopupMenu statusBarPopup = ttx.getStatusBarPopup();
				JLabel statusBar = ttx.getStatusBar();
				statusBar.addMouseListener(
						new StatusBarPopupListener(statusBarPopup));
				
				// line saver
				statusBarPopup.add(ttx.getLineSaverAction());
				
				
				
				
				
				
				// Add the components
				statusBarPanel.add(statusBar);
				//statusBarPanel.add(statusMsg);
				statusBarPanel.add(statusProgress);
				statusBarPanel.add(wordFindLbl);
				statusBarPanel.add(wordFindFld);
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
				
				// position the statusMsg label
				layout.putConstraint(SpringLayout.WEST, statusProgress,
					5,
					SpringLayout.EAST, statusBar);
				layout.putConstraint(SpringLayout.NORTH, statusProgress,
					2,
					SpringLayout.NORTH, statusBarPanel);
				layout.putConstraint(SpringLayout.SOUTH, statusBarPanel,
					2,
					SpringLayout.SOUTH, statusProgress);
				
				
				
				
				
				// position the Word Find label relative to the Word Find
				// text field, which is in turn relative to the right side of 
				// the panel
				layout.putConstraint(SpringLayout.NORTH, wordFindLbl,
					2,
					SpringLayout.NORTH, statusBarPanel);
				layout.putConstraint(SpringLayout.SOUTH, statusBarPanel,
					2,
					SpringLayout.SOUTH, wordFindFld);
				layout.putConstraint(SpringLayout.EAST, wordFindLbl,
					-2,
					SpringLayout.WEST, wordFindFld);
				
				// position the Word Find text field
				layout.putConstraint(SpringLayout.EAST, wordFindFld,
					-5,
					SpringLayout.WEST, lineNumLbl);
				layout.putConstraint(SpringLayout.NORTH, wordFindFld,
					0,
					SpringLayout.NORTH, statusBarPanel);
				layout.putConstraint(SpringLayout.SOUTH, statusBarPanel,
					0,
					SpringLayout.SOUTH, wordFindFld);
				
				
				
				
				
				
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
				
				ttx.validate();
			}
		});
	}
	
	/** Selects the given line.
	 * Highlights the entire line.
	 * @param line the line to highlight
	 */
	public void selectLine(int line) {
		TextPad t = ttx.getSelectedTextPad();
		if (t != null) {
			// highlight the appropriate line
			Point p = t.getPositionFromLineNumber(line);
			ttx.textSelectionReverse(t, (int) p.getX(), 0, 
				(int) p.getY() - (int) p.getX());
			lastLine = line;
		}
	}
	
	/** Finds the first occurrence of a sequence from the
	 * given starting point, ignoring case.
	 * If the given sequence has already been selected, the next
	 * occurrance of the sequence will be found.
	 * @param seq the sequence to find
	 * @param start the position number from which to start 
	 * searching; if -1, the search will begin from the current 
	 * caret posiion.
	 */
	public void findSeq(String seq, int start) {
		// Prepare the search
		TextPad t = ttx.getSelectedTextPad();
		if (t == null) return;
		// shifts text and quarry to lower case
		String text = t.getAllText().toLowerCase();
		seq = seq.toLowerCase();
		// saves the caret position
		int origCaretPosition = t.getCaretPosition();
		// starts from 0 if flagged not to start at caret position
		if (start == -1) start = origCaretPosition - seq.length();
		String currentSelection = t.getSelectedText();
		if (currentSelection != null 
				&& currentSelection.equalsIgnoreCase(seq)) start++;
		
		// Find the quarry
		int i = text.indexOf(seq, start);
		// if can't find, wraps to the beginning
		if (i == -1) {
			i = text.indexOf(seq, 0);
		}
		// if still can't find, turns field pink and sounds an audible
		// warning; otherwise, highlights the word
		if (i != -1) {
			wordFindFld.setBackground(Color.white);
			ttx.textSelection(t, 0, i, i + seq.length());
		} else {
			Toolkit.getDefaultToolkit().beep();
			wordFindFld.setBackground(Color.pink);
			t.setCaretPositionTop(origCaretPosition);
		}
		
		// Save the quarry
		lastWord = seq;
	}
	
	/** Finds the first occurrence of a sequence from the
	 * given starting point, ignoring case.
	 * @param seq the sequence to find
	 * @param start the position number from which to start 
	 * searching; if -1, the search will begin from the current 
	 * caret posiion.
	 */
	public void findSeqReverse(String seq, int start) {
		// Prepare the search
		TextPad t = ttx.getSelectedTextPad();
		if (t == null) return;
		// shifts text and quarry to lower case
		String text = t.getAllText().toLowerCase();
		seq = seq.toLowerCase();
		// saves the caret position
		int origCaretPosition = t.getCaretPosition();
		// starts from 0 flagged not to start at caret position
		if (start == -1) start = text.length() - 1;
		
		// Find the quarry
		int i = LibTTx.reverseIndexOf(text, seq, start);
		// if can't find, wraps to the beginning
		if (i == -1) {
			i =  LibTTx.reverseIndexOf(text, seq, text.length() - 1);
		}
		// if still can't find, turns field pink and sounds an audible
		// warning; otherwise, highlights the word
		if (i != -1) {
			wordFindFld.setBackground(Color.white);
			t.setCaretPosition(i);
			ttx.textSelection(t, 0, i, i + seq.length());
		} else {
			Toolkit.getDefaultToolkit().beep();
			wordFindFld.setBackground(Color.pink);
			t.setCaretPositionTop(origCaretPosition);
		}
		
		// Save the quarry
		lastWord = seq;
	}
	
}
