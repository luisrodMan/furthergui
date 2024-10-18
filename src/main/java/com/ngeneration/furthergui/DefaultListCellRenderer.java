package com.ngeneration.furthergui;

public class DefaultListCellRenderer<T2> extends DefaultItemCellRenderer<T2> implements ListCellRenderer<T2> {

	@Override
	public FComponent getRendererComponent(FList<T2> list, T2 value, boolean isSelected, boolean cellHasFocus,
			int row) {
		return super.getRendererComponent(list, value, isSelected, cellHasFocus);
	}

}
