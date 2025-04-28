package com.ngeneration.furthergui.layout;

import com.ngeneration.furthergui.math.Padding;

public class GridBagConstraints {

	static final int CENTER_H = 1;
	static final int CENTER_V = 2;
	public static final int LEFT = 4 | CENTER_V, BOTTOM_LEFT = 8, RIGHT = 16, TOP_LEFT = 32, TOP = TOP_LEFT | CENTER_H,
			TOP_RIGHT = TOP_LEFT | RIGHT, BOTTOM = BOTTOM_LEFT | CENTER_H, BOTTOM_RIGHT = BOTTOM_LEFT | RIGHT,
			CENTER = CENTER_H | CENTER_V;

	public int row, col;
	public Padding margin;
	public float weightH = 0;
	public float weightV = 0;
	public int cols = 1;
	public int rows = 1;
	public int anchor = TOP_LEFT;
	public boolean fillHorizontal, fillVertical;

	public GridBagConstraints() {
	}

	public GridBagConstraints(int r, int c, int rows, int cols, float w, float h) {
		this.row = r;
		this.col = c;
		this.rows = rows;
		this.cols = cols;
		this.weightH = w;
		this.weightV = h;
	}

}
