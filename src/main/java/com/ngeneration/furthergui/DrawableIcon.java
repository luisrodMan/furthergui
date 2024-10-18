package com.ngeneration.furthergui;

import com.ngeneration.furthergui.graphics.Icon;

public abstract class DrawableIcon implements Icon {
	private int width = 0;
	private int height = 0;

	public DrawableIcon(int size) {
		this(size, size);
	}

	public DrawableIcon(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void dispose() {

	}
}
