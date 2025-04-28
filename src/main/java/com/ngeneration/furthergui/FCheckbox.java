package com.ngeneration.furthergui;

import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.graphics.Icon;

public class FCheckbox extends FAbstractButton {

	public FCheckbox(String text) {
		this(text, false);
	}

	public FCheckbox(String text, Icon icon) {
		this(text, icon, false);
	}

	public FCheckbox(boolean checked) {
		this("", checked);
	}

	public FCheckbox(String text, boolean checked) {
		this(text, new RadioIcon(false), checked);
	}

	public FCheckbox(String string, Icon icon, boolean checked) {
		super(string, icon);
		setSelected(checked);
		setSelectedIcon(new RadioIcon(true));
		setOpaque(false);
	}

//	public void setSelected(boolean b) {
//		boolean repaint = b != isSelected();
//		selected = b;
//		if (repaint)
//			repaint();
//	}

	static class RadioIcon implements Icon {

		private int size = 12;
		private boolean filled;

		public RadioIcon(boolean filled) {
			this.filled = filled;
		}

		@Override
		public int getWidth() {
			return size;
		}

		@Override
		public int getHeight() {
			return size;
		}

		@Override
		public void dispose() {

		}

		@Override
		public void paint(int x, int y, Graphics g) {
			g.setPenSize(1);
			g.setColor(filled ? Color.LIGTH_GRAY : Color.LIGTH_GRAY);
			g.drawRect(x, y, getWidth(), getHeight());

			if (filled) {
				g.setPenSize(4);
				g.setColor(Color.LIGTH_GRAY.lighter().lighter().lighter());
				g.drawCircle(x + getWidth() / 2, y + getWidth() / 2, getWidth() / 8, 2);
			}
		}

	}

}
