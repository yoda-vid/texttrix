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
 * Portions created by the Initial Developer are Copyright (C) 2004-6
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
import java.io.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.table.*;

public class LineDancePanel extends JPanel {

	private static final int COL_LINE = 0;
	private static final int COL_POSITION = 1;
	private static final int COL_NAME = 2;
	
	DefaultTableModel tableModel = null;
	LineDanceTable table = null;
	JScrollPane scrollPane = null;
	
	public LineDancePanel(KeyAdapter aKeyAdapter) {
		super();
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.NORTH;
		
		String[] cols = {
			"Line",
			"Position",
			"Name"
		};
		
		Object[][] data = new Object[0][3];
		
		tableModel = new DefaultTableModel(data, cols) {
			public boolean isCellEditable(int row, int column) {
				if (column == COL_NAME) {
					return true;
				}
				return false;
				/*
				if (column == COL_LINE || column == COL_POSITION) {
					return false;
				}
				return true;
				*/
			}
		};
		table = new LineDanceTable(tableModel, aKeyAdapter);
		scrollPane = new JScrollPane(table);
		table.setPreferredScrollableViewportSize(new Dimension(300, 150));
		
		
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
		
		validate();
	}
	
	public void addRow(Object[] rowData) {
		tableModel.addRow(rowData);
		int rowCount = table.getRowCount();
		table.setRowSelectionInterval(rowCount - 1, rowCount -1);
	}
	
	public void removeRow(int row) {
		tableModel.removeRow(row);
	}
	
	public void removeSelectedRows() {
		int[] selectedRows = table.getSelectedRows();
		for (int i = 0; i < selectedRows.length; i++) {
			tableModel.removeRow(selectedRows[i]);
		}
	}
	
	public void editLineName() {
		int row = table.getSelectedRow();
//		System.out.println("row: " + row);
		if (row != -1) {
			table.editCellAt(row, COL_NAME);
			table.requestFocusInWindow();
		}
	}
	
	public void addTableMouseListener(MouseAdapter listener) {
		table.addMouseListener(listener);
	}
	
	public int getPosition() {
		int row = table.getSelectedRow();
		if (row == -1) return -1;
		int position = Integer.parseInt((String) tableModel.getValueAt(row, COL_POSITION));
		return position;
	}
	
	
	
}


class LineDanceTable extends JTable {
	
	public LineDanceTable(TableModel tableModel, KeyAdapter aKeyAdapter) {
		super(tableModel);
		addKeyListener(aKeyAdapter);
		/*
		addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				int row = getSelectedRow();
				if (row != -1) {
					clearSelection();
					setRowSelectionInterval(row, row);
				}
			}
			
		});
		*/
	}
/*
	public boolean editCellAt(int row, int col, EventObject e) {
		if (e != null) System.out.println(e.toString());
		return super.editCellAt(row, col, e);
	}
	public boolean editCellAt(int row, int col, EventObject e) {
//		System.out.println(e.toString());
		if (e instanceof KeyEvent) {
		} else {
			return super.editCellAt(row, col, e);
		}
		if (e instanceof MouseEvent) {
			if (((MouseEvent)e).getClickCount() == 1) {
				System.out.println("here");
//				return super.editCellAt(row, col, e);
				((MouseEvent)e).consume();
			} else {
				return super.editCellAt(row, col, e);
			}
		} else {
				System.out.println("also here");
			return super.editCellAt(row, col, e);
		}
		return false;
	}
*/
}