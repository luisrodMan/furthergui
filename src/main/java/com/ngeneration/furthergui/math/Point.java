package com.ngeneration.furthergui.math;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Point {

	private int x, y;

	public Point(Point point) {
		this(point.x, point.y);
	}

	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Point sub(Point point) {
		x -= point.x;
		y -= point.y;
		return this;
	}

	public Point add(Point location) {
		return add(location.x, location.y);
	}

	public Point add(int x2, int y2) {
		x += x2;
		y += y2;
		return this;
	}

}
