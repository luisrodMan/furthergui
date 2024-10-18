package com.ngeneration.furthergui.event;

public interface WindowListener {

	/**
	 * Invoked first time window is become visible.
	 * 
	 * @param event
	 */
	void windowOpenned(WindowEvent event);

	/**
	 * Invoked when the user attempts to close the window from the window's system
	 * menu.
	 * 
	 * @param e the event to be processed
	 */
	void windowClossin(WindowEvent event);

	void windowClosed(WindowEvent event);

	void windowIconified(WindowEvent event);

	void windowDeiconified(WindowEvent event);

	void windowActivated(WindowEvent event);

	void windowDeActivated(WindowEvent event);

}
