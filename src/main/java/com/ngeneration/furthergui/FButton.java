package com.ngeneration.furthergui;

import com.ngeneration.furthergui.graphics.Icon;

public class FButton extends FAbstractButton {

	public FButton(Icon icon) {
		this(null, icon);
	}

	public FButton(String text) {
		this(text, null);
	}

	public FButton(String text, Icon icon) {
		super(text, icon);
	}

}
