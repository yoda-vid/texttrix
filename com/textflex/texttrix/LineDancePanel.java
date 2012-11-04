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
 * Portions created by the Initial Developer are Copyright (C) 2006-7
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
import java.io.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.table.*;

/** A panel that contains the table for the Line Dance function.
 * The table records the line numbers and 
 * user-defined names to help record multiple locations in a
 * given file.  The assumption is that each {@link TextPad}
 * contains its own Line Dance panel, which is incorporated
 * into the Line Dance dialog from {@link TextTrix} each
 * time a new Text Pad is selected.
 */
public class LineDancePanel extends JPanel {

	/* Constants */
	private static final int COL_LINE = 0; // index of line column
// 	private static final int COL_POSITION = 1; // index of position column
	private static final int COL_NAME = 1; // index of name column
	
	/* GUI components */
	DefaultTableModel tableModel = null; // table model
	LineDanceTable table = null; // the table
	JScrollPane scrollPane = null; // scroll pane holding the table
	private boolean editName = false;
	
	/** Creates the Line Dance panel to be included in the Line
	 * Dance dialog.
	 * @param aKeyAdapter an adapter that can launch
	 * Line Dance functionality
	 */
	public LineDancePanel(KeyAdapter aKeyAdapter) {
		super();
		
		// preps the graphics layout
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.NORTH;
		
		// the table column names
		String[] cols = {
			"Line",
// 			"Position",
			"Name"
		};
		
		// an empty data set for constructing the table model
		Object[][] data = new Object[0][3];
		tableModel = new DefaultTableModel(data, cols) {
			// prevents any cell from being editable to ease
			// double-clicking anywhere on the entry to jump to that line
			public boolean isCellEditable(int row, int column) {
				if (column == COL_NAME && editName) {
 					// only the name column is editable
					return true;
				}
				return false;
			}
		};
		
		
		// creates the table and places it in a scroll pane
		table = new LineDanceTable(tableModel, aKeyAdapter);
		scrollPane = new JScrollPane(table);
		table.setPreferredScrollableViewportSize(new Dimension(300, 150));
		
		// set columns width preferences
		TableColumnModel colModel = table.getColumnModel();
		colModel.getColumn(0).setPreferredWidth(20);
		colModel.getColumn(1).setPreferredWidth(80);
			
		// adds the scroll pane and table
		LibTTx.addGridBagComponent(
			scrollPane,
			constraints,
			0,
			0,
			1,
			1,
			100,
			0,
			this);
		
		// validates the layout
		validate();
	}
	
	/** Adds a new row to the table.
	 * The new roll is selected, and if the new entry is beyond
	 * the fold, the scroll pane is scrolled to the new addition.
	 * @param rowData the data to add, usually a String of
	 * line number, position, name
	 */
	public void addRow(Object[] rowData) {
		// adds the row
		tableModel.addRow(rowData);
		// selects the row
		int rowCount = table.getRowCount();
		table.setRowSelectionInterval(rowCount - 1, rowCount -1);
		// scrolls to the row
		Rectangle rect = table.getCellRect(rowCount - 1, 0, true);
		table.scrollRectToVisible(rect);
	}
	
	/** Removes the given row.
	 * The previous entry is selected, or if none exist, the next
	 * row will be selected, if possible.
	 * @param row the number of the row to remove
	 */
	public void removeRow(int row) {
		tableModel.removeRow(row);
		if (--row >= 0) {
			table.setRowSelectionInterval(row, row);
		} else if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}
	}
	
	/** Removes the selected rows.
	 * @see #removeRow
	 */
	public void removeSelectedRows() {
		int[] selectedRows = table.getSelectedRows();
		for (int i = 0; i < selectedRows.length; i++) {
			removeRow(selectedRows[i]);
		}
	}
	
	/** Allows the user to edit the name cell of the currently 
	 * selected row.
	 */
	public void editLineName() {
		int row = table.getSelectedRow();
//		System.out.println("row: " + row);
		if (row != -1) {
			// opens the cell for editing
			editName = true;
			table.editCellAt(row, COL_NAME);
			table.requestFocusInWindow();
			editName = false;
		}
	}
	
	/**
	 * Updates the line numbers in the table based on changes to the
	 * documents's line numbering.
	 * For example, if a line was added in the middle of the documents, all
	 * subsequent recorded line numbers should be incremented by one.
	 * If two lines were added, the subsequent lines should be incremented 
	 * by two. If three lines were deleted, any line numbers recorded in
	 * the deleted section should be removed from the recorded lines, and 
	 * all subsequent lines should be decremented by three. This function
	 * should usually be called after making text changes that change
	 * the number of lines in the text.
	 * 
	 * @param origNum the line number of the caret prior to text changes
	 * @param newNum the line number of the caret after text changes.
	 */
	public void updateLineNumber(int origNum, int newNum) {
		int rows = tableModel.getRowCount();
		int diff = newNum - origNum; // line change
		int[] removeRows = new int[rows]; // table rows to remove
		Arrays.fill(removeRows, 0); // 0 = keep, 1 = remove
		
		for (int i = 0; i < rows; i++) {
			// the line number in given row of the table
			int num = Integer.parseInt(
				(String) tableModel.getValueAt(i, COL_LINE));
			if (diff > 0 && num > origNum
					|| diff < 0 && num >= origNum) {
				// change recorded line numbers above the original caret
				// position when lines have been added
				table.setValueAt((num + diff) + "", i, COL_LINE);
			} else if (diff < 0 && (num > newNum && num < origNum)) {
				// flag to remove recorded line numbers for lines that have 
				// been deleted from the document
				removeRows[i] = 1;
			}
		}
		
		for (int i = rows - 1; i >= 0; i--) {
			// remove rows flagged to be removed, working from last to first
			// to avoid having to adjust for prior row removals
			if (removeRows[i] == 1) removeRow(i);
		}
	}
	
	/** Adds a mouse listener to the table.
	 * @param listener a listener for mouse events
	 */
	public void addTableMouseListener(MouseAdapter listener) {
		table.addMouseListener(listener);
	}
	
	
	/** Gets the position number from the currently selected entry.
	 */
	public int getLineNum() {
		int row = table.getSelectedRow();
		if (row == -1) return -1; // if none selected
		// gets the recorded value
		int line = Integer.parseInt(
			(String) tableModel.getValueAt(row, COL_LINE));
		return line;
	}
	
	
	
}

/** A table to record line memory entries.
 */
class LineDanceTable extends JTable {
	
	/** Constructs a new table.
	 * @param tableModel a table model
	 * @param aKeyAdapter an adapter for responding to key events
	 */
	public LineDanceTable(TableModel tableModel, KeyAdapter aKeyAdapter) {
		super(tableModel);
		addKeyListener(aKeyAdapter);
	}
}
