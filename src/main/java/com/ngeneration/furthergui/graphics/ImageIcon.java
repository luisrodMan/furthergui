package com.ngeneration.furthergui.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class ImageIcon implements Icon {

	private Texture texture;

	public ImageIcon(InputStream stream) {
		try {
			BufferedImage data = ImageIO.read(stream);
			var buffer = FFont.imageToBuffer(data);
			texture = new Texture(buffer, data.getWidth(), data.getHeight(), Texture.CHANNELS_RGBA);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

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
