package com.ngeneration.furthergui.math;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Dimension {

	public int width;
	public int height;

	public Dimension(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public Dimension() {
	}

	public Dimension(Point location) {
		this(location.getX(), location.getY());
	}
	

	public Dimension add(int horizontal, int vertical) {
		this.width += horizontal;
		this.height += vertical;
		return this;
	}

	public Dimension add(Point point) {
		return add(point.getX(), point.getY());
	}

	public Dimension add(Dimension dimension) {
		return add(dimension.width, dimension.height);
	}

	public Dimension substract(Dimension size) {
		width -= size.width;
		height -= size.height;
		return this;
	}

	public Dimension divide(int div) {
		width /= div;
		height /= div;
		return this;
	}

	public void set(int width, int height) {
		this.width = width;
		this.height = height;
	}

}
