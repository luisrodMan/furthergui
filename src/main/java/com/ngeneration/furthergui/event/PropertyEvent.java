package com.ngeneration.furthergui.event;

import com.ngeneration.furthergui.FComponent;

public class PropertyEvent extends Event {

	private String propertyName;
	private Object oldValue;

	public PropertyEvent(FComponent source, String propertyName, Object oldValue) {
		super(source);
		this.propertyName = propertyName;
		this.oldValue = oldValue;
	}
	
	public Object getOldValue() {
		return oldValue;
	}

	public String getProperty() {
		return propertyName;
	}

}
