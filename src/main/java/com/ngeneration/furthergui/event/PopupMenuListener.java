package com.ngeneration.furthergui.event;

public interface PopupMenuListener {

	void popupMenuWillBecomeVisible(PopupMenuEvent e);

	void popupMenuWillBecomeInvisible(PopupMenuEvent e);

	void popupMenuCanceled(PopupMenuEvent e);

}
