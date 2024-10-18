package com.ngeneration.furthergui.graphics;

public class ImageIcon implements Icon {

	private Texture texture;

	public ImageIcon(String filepath) {
		texture = new Texture(filepath);
	}

	@Override
	public int getWidth() {
		return texture.getWidth();
	}

	@Override
	public int getHeight() {
		return texture.getHeight();
	}

	@Override
	public void paint(int x, int y, Graphics g) {
		g.drawTexture(texture, x, y, texture.getWidth(), texture.getHeight());
	}

	@Override
	public void dispose() {
		System.err.println("Dispose Not implemented: " + this);
	}

}
