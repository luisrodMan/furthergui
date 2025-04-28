package com.ngeneration.furthergui.event;

import com.ngeneration.furthergui.FComponent;

public class MouseWheelEvent extends Event {

	private int amount;

	public MouseWheelEvent(FComponent source, int modifiers, int amount) {
		super(source, modifiers);
		this.amount = amount;
	}

	public int getAmount() {
		return amount;
	}

}
