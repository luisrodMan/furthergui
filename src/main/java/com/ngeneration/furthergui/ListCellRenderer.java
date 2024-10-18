package com.ngeneration.furthergui;

public interface ListCellRenderer<T2> {

	FComponent getRendererComponent(FList<T2> list, T2 value, boolean isSelected, boolean cellHasFocus, int row);
}
