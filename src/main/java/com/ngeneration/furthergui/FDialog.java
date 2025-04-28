package com.ngeneration.furthergui;

import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;

public class FDialog extends FFrame {

	public FDialog() {
		this(null);
	}

	public FDialog(String title) {
		super(title);
		setAlwaysOnTop(true);
		setBackground(Color.RED);
	}

	@Override
	protected FComponent createHeader() {
		return new FFrame.DefaultFrameHeader(this, FFrame.DefaultFrameHeader.BUTTON_CLOSE);
	}
//
//	public void setContentPane(FComponent contentPanel2) {
//		add(contentPanel2);
//	}

	@Override
	public void setVisible(boolean value) {
		super.setVisible(value);
		if (value) {
			FurtherApp.getInstance().blockByDialog(this);
		}
	}

	@Override
	protected void paintComponents(Graphics g) {
		super.paintComponents(g);
		g.setColor(new Color(55, 65, 65));
		g.setPenSize(1);
		g.drawRect(0, 0, getWidth(), getHeight());
	}

}
