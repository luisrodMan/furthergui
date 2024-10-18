package com.ngeneration.furthergui;

import com.ngeneration.furthergui.math.Padding;

public class DefaultItemCellRenderer<T2> extends FLabel implements ItemCellRenderer<T2> {

	public DefaultItemCellRenderer() {
		setPadding(new Padding(6, 4));
	}

	@Override
	public FComponent getRendererComponent(FComponent component, T2 value, boolean isSelected, boolean cellHasFocus) {
		setFont(component.getFont());
		setForeground(component.getForeground());
		setTextAlign(FLabel.LEFT);
		setText(String.valueOf(value));
		return this;
	}

}
