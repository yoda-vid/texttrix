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
	JTable table = null;
	JScrollPane scrollPane = null;
	
	public LineDancePanel() {
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
				if (column == COL_LINE || column == COL_POSITION) {
					return false;
				}
				return true;
			}
		};
		table = new JTable(tableModel);
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