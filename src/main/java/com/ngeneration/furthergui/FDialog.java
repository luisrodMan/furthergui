package com.ngeneration.furthergui;

import com.ngeneration.furthergui.graphics.Color;

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

}
