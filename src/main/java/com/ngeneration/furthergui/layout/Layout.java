package com.ngeneration.furthergui.layout;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.math.Dimension;

public interface Layout {

	void addComponent(FComponent component, Object constraints);
	
	Dimension getPrefferedDimension(FComponent container);
	
	void layout(FComponent container);

	void remove(FComponent component);
	
}
