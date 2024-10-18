package com.ngeneration.furthergui.layout;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Padding;

public class BorderLayout implements Layout {

	public static final int WEST = 1;
	public static final int EAST = 2;
	public static final int NORTH = 3;
	public static final int SOUTH = 4;
	public static final int CENTER = 5;

	private FComponent east, weast, north, center, south;

	@Override
	public void addComponent(FComponent component, Object constraints) {
		int target = constraints instanceof Integer ? (int) constraints : CENTER;
		switch (target) {
		case WEST:
			weast = component;
			break;
		case EAST:
			east = component;
			break;
		case NORTH:
			north = component;
			break;
		case SOUTH:
			south = component;
			break;
		default:
			center = component;
			break;
		}
	}

	@Override
	public Dimension getPrefferedDimension(FComponent component) {
		Padding padding = component.getPadding();
		Dimension dimension = new Dimension();
		Dimension weastDimension = null, eastDimension = null, northDimension = null, southDimension = null,
				centerDimension = null;
		if (weast != null)
			weastDimension = weast.getPrefferedSize();
		if (east != null)
			eastDimension = east.getPrefferedSize();
		if (north != null)
			northDimension = north.getPrefferedSize();
		if (south != null)
			southDimension = south.getPrefferedSize();
		if (center != null)
			centerDimension = center.getPrefferedSize();

		int maxHeight = 0;

		if (weast != null) {
			dimension.width += weastDimension.width;
			maxHeight = Math.max(maxHeight, weastDimension.height);
		}
		if (east != null) {
			dimension.width += eastDimension.width;
			maxHeight = Math.max(maxHeight, eastDimension.height);
		}
		if (center != null) {
			dimension.width += centerDimension.width;
			maxHeight = Math.max(maxHeight, centerDimension.height);
		}
		if (north != null) {
			maxHeight += northDimension.height;
			dimension.width = Math.max(northDimension.width, dimension.width);
		}
		if (south != null) {
			maxHeight += southDimension.height;
			dimension.width = Math.max(southDimension.width, dimension.width);
		}
		dimension.width = dimension.width + padding.getHorizontal();
		dimension.height = maxHeight + dimension.height + padding.getVertical();
		return dimension;
	}

	@Override
	public void layout(FComponent component) {
		Padding padding = component.getPadding();
		int left = padding.left;
		int right = component.getWidth() - padding.right;
		int top = padding.top;
		int bottom = component.getHeight() - padding.bottom;

		if (north != null) {
			int l = north.getPrefferedSize().height;
			north.setBounds(left, top, right - left, l);
			top += l;
		}
		if (south != null) {
			int h = south.getPrefferedSize().height;
			south.setBounds(left, bottom - h, right - left, h);
			bottom -= h;
		}

		if (weast != null) {
			int l = weast.getPrefferedSize().width;
			weast.setBounds(left, top, l, bottom - top);
			left += l;
		}
		if (east != null) {
			int l = east.getPrefferedSize().width;
			east.setBounds(right - l, top, l, bottom - top);
			right -= l;
		}
		if (center != null) {
			center.setBounds(left, top, right - left, bottom - top);
		}
	}

	@Override
	public void remove(FComponent component) {
		if (center == component)
			center = null;
		if (weast == component)
			weast = null;
		if (east == component)
			east = null;
		if (north == component)
			north = null;
		if (south == component)
			south = null;
	}

}
