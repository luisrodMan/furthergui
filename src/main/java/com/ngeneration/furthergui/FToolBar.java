package com.ngeneration.furthergui;

import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.layout.Layout;
import com.ngeneration.furthergui.math.Padding;

public class FToolBar extends FComponent {

	public FToolBar() {
		this(new FlowLayout(FlowLayout.LEFT));
	}

	public FToolBar(Layout layout) {
		super.setLayout(layout);
		setPadding(new Padding(10));
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

}
