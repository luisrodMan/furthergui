package com.ngeneration.furthergui.math;

public class Padding {

	public int top, right, bottom, left;

	public Padding(int v) {
		this(v, v);
	}

	public Padding(Padding p) {
		set(p);
	}

	public Padding(int horizontal, int vertical) {
		right = left = horizontal;
		top = bottom = vertical;
	}

	public Padding() {
	}

	public Padding(int left, int bottom, int right, int top) {
		set(left, bottom, right, top);
	}

	public void set(int left, int bottom, int right, int top) {
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	public Dimension toDimension() {
		return new Dimension(left + right, top + bottom);
	}

	public void set(Padding p) {
		set(p.left, p.bottom, p.right, p.top);
	}

	public int getHorizontal() {
		return left + right;
	}

	public int getVertical() {
		return top + bottom;
	}

}
