package com.ngeneration.furthergui;

public interface ItemCellRenderer<T2> {

	FComponent getRendererComponent(FComponent component, T2 value, boolean isSelected, boolean cellHasFocus);
}
