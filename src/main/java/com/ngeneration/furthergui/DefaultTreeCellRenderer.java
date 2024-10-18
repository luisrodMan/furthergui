package com.ngeneration.furthergui;

import com.ngeneration.furthergui.math.Padding;

public class DefaultTreeCellRenderer extends FLabel implements TreeCellRenderer {

	public DefaultTreeCellRenderer() {
		setPadding(new Padding(5));
	}

	@Override
	public FComponent getTreeCellRendererComponent(FTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		setOpaque(selected || hasFocus);
		setBackground(selected ? tree.getSelectionColor() : tree.getHoverColor());

		setFont(tree.getFont());
		setForeground(tree.getForeground());
		setText(String.valueOf(value));
		return this;
	}

}
