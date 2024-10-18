package com.ngeneration.furthergui.event;

import com.ngeneration.furthergui.FPopupMenu;

public class PopupMenuEvent extends Event {

	private boolean cancel;

	public PopupMenuEvent(FPopupMenu fPopupMenu) {
		super(fPopupMenu);
	}

	public void cancel() {
		cancel = true;
	}

	public boolean isCanceled() {
		return cancel;
	}

}
