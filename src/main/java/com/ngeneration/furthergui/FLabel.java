package com.ngeneration.furthergui;

import com.ngeneration.furthergui.graphics.FFont;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.math.Dimension;

import lombok.Data;

@Data
public class FLabel extends FComponent {

	public static final int LEFT = 1;
	public static final int CENTER = 2;
	private FFont font;
	private String text;
	private int textAlign = LEFT;

	public FLabel() {
		this(null);
	}

	public FLabel(String text) {
		setOpaque(false);
		setText(text);
		setFont(FurtherApp.getInstance().getDefaultFont());
	}

	public void setTextAlign(int align) {
		textAlign = align;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public Dimension getPrefferedSize() {
		return getPadding().toDimension().add(text == null ? new Dimension() : getFont().getStringBounds(text));
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), g.getHeight());
		}
//		var padding = text != null ? getPadding() : null;
		float x = getPadding().left;
		if (text != null) {
			if (textAlign == CENTER)
				x = (getWidth() - getFont().getStringWidth(text, 0, text.length())) * 0.5f;
			float y = (getHeight() - getFont().getFontHeight()) * 0.5f;
			g.setFont(font);
			g.setColor(getForeground());
			g.drawString(x, y, text);
		}
	}

}
