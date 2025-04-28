package com.ngeneration.furthergui.layout;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.math.Dimension;

public class FlowLayout implements Layout {

	public static final int LEFT = 0;
	public static final int CENTER = 1;
	public static final int RIGHT = 2;
	public static final int TOP_TO_BOTTOM = 3;
	public static final int BOTTOM = 4;
	public static final int CENTER_VERTICAL = 5;
	private int direction;
	private int gap = 10;
	private boolean fill;

	public FlowLayout() {
		this(LEFT);
	}

	public FlowLayout(int direction) {
		this(direction, false);
	}

	public FlowLayout(int direction, boolean fill) {
		this(direction, 10, fill);
	}

	public FlowLayout(int direction, int gap) {
		this(direction, gap, false);
	}

	public FlowLayout(int direction, int gap, boolean fill) {
		this.direction = direction;
		this.gap = gap;
		this.fill = fill;
	}

	public void setGap(int gap) {
		this.gap = gap;
	}

	@Override
	public void addComponent(FComponent component, Object constraints) {
	}

	@Override
	public Dimension getPrefferedDimension(FComponent component) {
		Dimension dimension = component.getPadding().toDimension();
		if (component.getComponentCount() > 0) {
			int maxWidth = 0;
			int maxHeight = 0;
			for (FComponent comp : component.getComponents()) {
				var dim = comp.getPrefferedSize();
				if (direction <= RIGHT)
					dimension.width += dim.width;
				else
					dimension.height += dim.height;
				maxWidth = Math.max(maxWidth, dim.width);
				maxHeight = Math.max(maxHeight, dim.height);
			}
			if (direction <= RIGHT) {
				dimension.height += maxHeight;
				dimension.width += (component.getComponentCount() - 1) * gap;
			} else {
				dimension.width += maxWidth;
				dimension.height += (component.getComponentCount() - 1) * gap;
			}
		}
		return dimension;
	}

	@Override
	public void layout(FComponent container) {
		boolean horizontal = direction <= RIGHT;
		var padding = container.getPadding();
		int filled = horizontal ? container.getHeight() - padding.getVertical()
				: container.getWidth() - padding.getHorizontal();

		int x = padding.left;
		int y = padding.top;
		if (direction == RIGHT)
			x = container.getWidth() - padding.right;
		else if (direction == BOTTOM)
			y = container.getHeight() - padding.bottom;
		else if (direction == CENTER || direction == CENTER_VERTICAL) {
			var components = container.getComponents();
			int maxWidth = 0;
			int maxHeight = 0;
			for (FComponent c : components) {
				var dim = c.getPrefferedSize();
				maxWidth += dim.width;
				maxHeight += dim.height;
			}
			if (direction == CENTER) {
				x += (container.getWidth() - padding.getHorizontal() - maxWidth - (container.getComponentCount() - 1) * gap) / 2;
			} else
				y = (container.getHeight() - maxHeight - (container.getComponentCount() - 1) * gap) / 2;
		}

		for (var component : container.getComponents()) {
			var preffered = component.getPrefferedSize();
			component.setBounds(x - (direction == RIGHT ? preffered.width : 0),
					y - (direction == BOTTOM ? preffered.height : 0), !horizontal && fill ? filled : preffered.width,
					horizontal && fill ? filled : preffered.height);
			x += horizontal ? ((preffered.width + gap) * (direction == RIGHT ? -1 : 1)) : 0;
			y += !horizontal ? ((preffered.height + gap) * (direction == BOTTOM ? -1 : 1)) : 0;
		}
	}

	@Override
	public void remove(FComponent component) {
	}

	public void setFilled(boolean fill) {
		this.fill = fill;
	}

}
