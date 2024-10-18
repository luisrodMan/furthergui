package com.ngeneration.furthergui;

import com.ngeneration.furthergui.graphics.Icon;

public class FMenuItem extends FAbstractButton {

	public FMenuItem(String text) {
		this(text, null);
	}

	public FMenuItem(String text, Icon icon) {
		super(text, icon);
		setBackground(FPopupMenu.POPUP_COLOR);
	}

	@Override
	protected void fireActionEvent() {
		if (!(this instanceof FMenu))
			FurtherApp.getInstance().clearPopupWindows();
		super.fireActionEvent();
	}

}
