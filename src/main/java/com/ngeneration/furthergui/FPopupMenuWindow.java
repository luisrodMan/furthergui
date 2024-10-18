package com.ngeneration.furthergui;

import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Padding;

public class FPopupMenuWindow extends FPopupWindow {

	private FList<MenuItem> lastList;

	public FPopupMenuWindow() {
		setPadding(new Padding(0));
		FlowLayout layout = new FlowLayout(FlowLayout.TOP_TO_BOTTOM);
		layout.setFilled(true);
		setLayout(layout);
	}

	public void addItem(MenuItem item) {
		if (lastList == null) {
			lastList = new FList<FPopupMenuWindow.MenuItem>();
			add(lastList);
		}
		lastList.addItem(item);
	}

	public void addSeparator() {
		lastList = null;
		FPanel p = new FPanel();
		p.setPadding(new Padding());
		p.setPrefferedSize(new Dimension(1, 1));
		p.setBackground(Color.LIGTH_GRAY);
		add(p);
	}

	public static class MenuItem {
		private String text;

		public MenuItem(String text) {
			setText(text);
		}

		public void setText(String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return String.valueOf(text);
		}
	}

}
