package com.ngeneration.furthergui;

import java.util.LinkedList;
import java.util.List;

public class DefaultTableModel {

	private String[] header;
	private List<Object[]> rows = new LinkedList<>();

	public DefaultTableModel(String[] headers, int i) {
		this.header = headers;
	}

	public void addRow(Object[] objects) {
		rows.add(objects);
	}

	public int getRowCount() {
		return rows.size();
	}

	public Object getValueAt(int row, int col) {
		return rows.get(row)[col];
	}

	public int getColumnsCount() {
		return header.length;
	}

	public List<Object[]> getData() {
		return rows;
	}

	public String getColumnName(int i) {
		return header[i];
	}

	public boolean isEditable(int row, int col) {
		return true;
	}

	public void setValue(int row, int col, Object value) {
		rows.get(row)[col] = value;
	}

}
