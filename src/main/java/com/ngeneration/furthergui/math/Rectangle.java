package com.ngeneration.furthergui.math;

import lombok.ToString;

@ToString
public class Rectangle {

	public int x;
	public int y;
	public int width;
	public int height;

	public Rectangle() {

	}

	public Rectangle(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public Rectangle(Rectangle other) {
		set(other.x, other.y, other.width, other.height);
	}

	public Rectangle(int x, int y, int width, int height) {
		set(x, y, width, height);
	}

	public Rectangle set(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		return this;
	}

	public boolean contains(Point point) {
		return contains(point.getX(), point.getY());
	}

	public boolean contains(int x, int y) {
		return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
	}

	public Point getLocation() {
		return new Point(x, y);
	}

	public boolean intersects(Rectangle other) {
		return Math.max(x + width, other.x + other.width) - Math.min(x, other.x) < width + other.width
				&& Math.max(y + height, other.y + other.height) - Math.min(y, other.y) < height + other.height;
	}

	public Rectangle setSize(int width, int height) {
		set(x, y, width, height);
		return this;
	}

	public Rectangle set(Rectangle other) {
		return set(other.x, other.y, other.width, other.height);
	}

	public Rectangle setLocation(Point location) {
		return setLocation(location.getX(), location.getY());
	}

	public Rectangle setLocation(int x2, int y2) {
		return set(x2, y2, width, height);
	}

	public Rectangle clamp(Rectangle other) {
		int x2 = MathUtil.clamp(x + width, other.x, other.x + other.width);
		int y2 = MathUtil.clamp(y + height, other.y, other.y + other.height);
		x = MathUtil.clamp(x, other.x, other.x + other.width);
		y = MathUtil.clamp(y, other.y, other.y + other.height);
		width = x2 - x;
		height = y2 - y;
		return this;
	}

	public Dimension getDimension() {
		return new Dimension(width, height);
	}

	public boolean contains(Rectangle localRegion) {
		return contains(localRegion.x, localRegion.y) && contains(localRegion.x + localRegion.width, localRegion.y)
				&& contains(localRegion.x, localRegion.y + localRegion.height)
				&& contains(localRegion.x + localRegion.width, localRegion.y + localRegion.height);
	}

}
