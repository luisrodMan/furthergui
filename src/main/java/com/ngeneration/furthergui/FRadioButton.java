package com.ngeneration.furthergui;

import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.graphics.Icon;

public class FRadioButton extends FAbstractButton {

	public FRadioButton(String text) {
		this(text, new RadioIcon(false));
		setSelectedIcon(new RadioIcon(true));
		setOpaque(false);
	}

	public FRadioButton(String text, Icon icon) {
		super(text, icon);
	}

	@Override
	protected void fireActionEvent() {
		setSelected(!isSelected());
		super.fireActionEvent();
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
			g.setColor(filled ? Color.CYAN : Color.LIGTH_GRAY);
			g.drawCircle(x + getWidth() / 2 + 1, y + getWidth() / 2 + 1, getWidth() / 2, 2);

			if (filled) {
				g.setPenSize(4);
				g.setColor(Color.GREEN);
				g.drawCircle(x + getWidth() / 2 + 1, y + getWidth() / 2 + 1, getWidth() / 8, 1);
			}
		}

	}

}
