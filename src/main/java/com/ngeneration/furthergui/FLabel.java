package com.ngeneration.furthergui;

import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.graphics.Icon;
import com.ngeneration.furthergui.math.Dimension;

public class FLabel extends FComponent {

	public static final int LEFT = 1;
	public static final int CENTER = 2;
	public static final int RIGHT = 3;
	private String text;
	private int textAlign = LEFT;
	private Icon icon;
	private int gap = 5;

	public FLabel() {
		this(null);
	}

	public FLabel(String text) {
		setOpaque(false);
		setFocusable(false);
		setText(text);
		setFont(FurtherApp.getInstance().getDefaultFont());
		setForeground(new Color(190, 190, 190));
	}

	public void setTextAlign(int align) {
		textAlign = align;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public Icon getIcon(String string) {
		return icon;
	}

	@Override
	public Dimension getPrefferedSize() {
		var dim = text == null ? new Dimension() : getFont().getStringBounds(text);
		if (icon != null) {
			dim.set(dim.getWidth() + icon.getWidth(), Math.max(dim.height, icon.getHeight()));
			if (text != null)
				dim.width += gap;
		}
		dim.add(getPadding().toDimension());
		return dim;
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), g.getHeight());
		}
//		var padding = text != null ? getPadding() : null;
		float x = getPadding().left;

		if (icon != null) {
			g.setColor(Color.WHITE);
			icon.paint((int) x, (getHeight() - icon.getHeight()) / 2, g);
			x += icon.getWidth() + gap;
		}
		if (text != null) {
			if (textAlign == CENTER)
				x = (getWidth() - getFont().getStringWidth(text, 0, text.length())) * 0.5f;
			else if (textAlign == RIGHT)
				x = (getWidth() - getFont().getStringWidth(text, 0, text.length()));
			float y = (getHeight() - getFont().getFontHeight()) * 0.5f;
			g.setFont(getFont());
			g.setColor(getForeground());
			g.drawString(x, y, text);
		}
	}

}
