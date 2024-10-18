package com.ngeneration.furthergui.layout;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.math.Dimension;

public class OneVisibleLayout implements Layout {

	public void setVisible(FComponent container, FComponent component) {
		boolean found = false;
		for (var c : container.getComponents()) {
			if (c == component)
				found = true;
			else
				c.setVisible(false);
		}
		if (!found)
			throw new RuntimeException("The container does not contains supplied component");
		component.setVisible(true);
	}

	@Override
	public void addComponent(FComponent component, Object constraints) {

	}

	@Override
	public Dimension getPrefferedDimension(FComponent container) {
		int maxWidth = 0;
		int maxHeight = 0;
		for (var c : container.getComponents()) {
			var preffered = c.getPrefferedSize();
			maxWidth = Math.max(maxWidth, preffered.width);
			maxHeight = Math.max(maxHeight, preffered.height);
		}
		var p = container.getPadding();
		return new Dimension(maxWidth + p.getHorizontal(), maxHeight + p.getVertical());
	}

	@Override
	public void layout(FComponent container) {
		var padding = container.getPadding();
		int x = padding.left;
		int y = padding.top;
		int width = container.getWidth() - padding.getHorizontal();
		int height = container.getHeight() - padding.getVertical();
		container.getComponents().forEach(c -> c.setBounds(x, y, width, height));
	}

	@Override
	public void remove(FComponent component) {

	}

}
