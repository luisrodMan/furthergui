package com.ngeneration.furthergui.event;

public interface DocumentListener {

	void removeUpdate(DocumentEvent event);

	void insertUpdate(DocumentEvent event);

	void chanedUpdate(DocumentEvent event);

}
