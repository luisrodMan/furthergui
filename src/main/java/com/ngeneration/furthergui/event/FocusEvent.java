package com.ngeneration.furthergui.event;

import com.ngeneration.furthergui.FComponent;

public class FocusEvent extends Event {

	private boolean focusGained;

	public FocusEvent(FComponent source, boolean focusGained) {
		super(source);
		this.focusGained = focusGained;
	}

	public boolean isFocusGained() {
		return focusGained;
	}

}
