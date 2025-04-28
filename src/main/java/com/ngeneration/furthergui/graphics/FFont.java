package com.ngeneration.furthergui.graphics;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import com.ngeneration.furthergui.math.Dimension;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class FFont {

	private int IMAGE_WIDTH = 256;
	private int IMAGE_HEIGHT = 256;
	private BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
	private Graphics2D graphics = (Graphics2D) image.getGraphics();

	private Font font;
	private Texture texture;
	private int fontHeight;
	private Map<Integer, CharDef> chars = new HashMap<>();

	public FFont(Font font) {
		this.font = font;
		createFont();
		var buffer = imageToBuffer(image);
		texture = new Texture(buffer, IMAGE_WIDTH, IMAGE_HEIGHT, Texture.CHANNELS_RGBA);
	}

	public Texture getTexture() {
		return texture;
	}

	public Font getFont() {
		return font;
	}

	private void createFont() {
		graphics.setFont(font);
		graphics.setColor(new java.awt.Color(255, 255, 255, 0));
		graphics.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(java.awt.Color.WHITE);
		var fm = graphics.getFontMetrics();
		fontHeight = fm.getHeight();
		int x = 0;
		int y = fontHeight;
		// horizontal padding 2 1-1
		for (int i = 32; i < 256; i++) {
			String c = "" + ((char) i);
			var sb = fm.getStringBounds(c, graphics);
			if (x + sb.getWidth() + 2 > IMAGE_WIDTH) {
				x = 0;
				y += fontHeight;
			}
			graphics.drawString(c, x + 1, y - fm.getDescent());
			chars.put(i, new CharDef(i, x + 1, y - fontHeight, (int) sb.getWidth(), (int) sb.getHeight()));
			x += sb.getWidth() + 2;
		}

	}

	public static ByteBuffer imageToBuffer(BufferedImage image) {
		int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length * 4).order(ByteOrder.nativeOrder());
		for (int pixel : pixels) {
			buffer.put((byte) ((pixel >> 16) & 0xFF));
			buffer.put((byte) ((pixel >> 8) & 0xFF));
			buffer.put((byte) (pixel & 0xFF));
			buffer.put((byte) ((pixel >> 24) & 0xFF));
		}
		buffer.flip();
		return buffer;
	}

	@Getter
	@AllArgsConstructor
	public static class CharDef {
		private int value;
		private int x;
		private int y;
		private int width;
		private int height;
	}

	public int getFontHeight() {
		return fontHeight;
	}

	public int getStringWidth(String text) {
		return getStringWidth(text, 0, text.length());
	}

	public int getStringWidth(String text, int i, int length) {
		int w = 0;
		for (int j = 0; j < length; j++) {
			int code = (int) text.charAt(i + j);
			var cData = chars.get(code);

			// tab size
			if (code == '\t') {
				w += chars.get((int) " ".charAt(0)).width * 4;
				continue;
			}
			if (code == '\r')
				continue;
			else if (code == '\n') {
				continue;
			}

			if (cData == null) {

				// generate new character
				System.out.println("unknown character: [" + (char) code + "]");
				cData = chars.get('*');

			}

			w += cData.getWidth();
		}
		return w;
	}

	public Dimension getStringBounds(String text) {
		return new Dimension(getStringWidth(text, 0, text.length()), getFontHeight());
	}

	public void drawString(Graphics g, float x, float y, String string) {
		int length = string.length();
		for (int i = 0; i < length; i++) {
			int code = string.charAt(i);

			// tab size
			if (code == '\t') {
				x += chars.get((int) " ".charAt(0)).width * 4;
				continue;
			}

			var charDef = chars.get(code);
			if (charDef == null) {

				// generate new character xdxxdxdxdx
				System.out.println("unknown character: [" + (char) code + "]");
				charDef = chars.get((int) '*');
			}
			g.drawTexture(texture, x, y, charDef.getX(), charDef.getY(), charDef.getWidth(), fontHeight,
					charDef.getWidth(), fontHeight);
			x += charDef.getWidth();
		}
	}

	public int getStringHeight(String object) {
		int max = 0;
		for (int i = 0; i < object.length(); i++) {
			max = Math.max(max, chars.get((int) object.charAt(i)).height);
		}
		return max;
	}

}
