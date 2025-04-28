package com.ngeneration.furthergui;

import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.layout.Layout;
import com.ngeneration.furthergui.math.Padding;

public class FPanel extends FComponent {

	public FPanel() {
		this(new FlowLayout(FlowLayout.CENTER));
	}

	public FPanel(Layout layout) {
		this(layout, new Padding(0));
	}

	public FPanel(Padding padding) {
		this(null, padding);
	}

	public FPanel(Layout layout, Padding padding) {
		super.setLayout(layout);
		setPadding(padding);
		setFocusable(false);
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
	}

}
