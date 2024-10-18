package com.ngeneration.furthergui;

public interface TreeCellRenderer {

	FComponent getTreeCellRendererComponent(FTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus);
	
}
