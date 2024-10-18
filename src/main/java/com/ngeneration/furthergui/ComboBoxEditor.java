package com.ngeneration.furthergui;

import java.awt.event.ActionListener;

public interface ComboBoxEditor {

	/**
	 * Returns the component that should be added to the tree hierarchy for this
	 * editor
	 *
	 * @return the component
	 */
	public FComponent getEditorComponent();

	/**
	 * Set the item that should be edited. Cancel any editing if necessary
	 *
	 * @param anObject an item
	 */
	public void setItem(Object anObject);

	/**
	 * Returns the edited item
	 *
	 * @return the edited item
	 */
	public Object getItem();

	/**
	 * Ask the editor to start editing and to select everything
	 */
	public void selectAll();

	/**
	 * Add an ActionListener. An action event is generated when the edited item
	 * changes
	 *
	 * @param l an {@code ActionListener}
	 */
	public void addActionListener(ActionListener l);

	/**
	 * Remove an ActionListener
	 *
	 * @param l an {@code ActionListener}
	 */
	public void removeActionListener(ActionListener l);
}
