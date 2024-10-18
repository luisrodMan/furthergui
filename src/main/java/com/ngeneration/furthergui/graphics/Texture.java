package com.ngeneration.furthergui.graphics;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

import org.lwjgl.stb.STBImage;

import lombok.Data;

@Data
public class Texture {

	public static final int CHANNELS_RGB = 3;
	public static final int CHANNELS_RGBA = 4;

	private ByteBuffer image;

	private int texID;
	private int width;
	private int height;
	private int comp;

	public Texture(String imagePath) {
		// IntBuffer width =
		// ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer(),
		// height =
		// ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer(),
		// nrChannels =
		// ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
		int[] width = new int[1], height = new int[1], nrChannels = new int[1];
		STBImage.stbi_set_flip_vertically_on_load(true);
		image = STBImage.stbi_load(imagePath, width, height, nrChannels, CHANNELS_RGBA);
		if (image == null) {
			if (!new File(imagePath).exists())
				throw new RuntimeException(new FileNotFoundException(imagePath));
			else
				throw new RuntimeException("failure: " + STBImage.stbi_failure_reason());
		}
		setBuffer(image, width[0], height[0], nrChannels[0]);
//		STBImage.stbi_image_free(image);
	}

	public Texture(ByteBuffer buffer, int width, int height, int channels) {
		setBuffer(buffer, width, height, channels);
	}

	private void setBuffer(ByteBuffer buffer, int width, int height, int channels) {
		image = buffer;
		this.width = width;
		this.height = height;
		this.comp = channels;

		createTexture();
	}

	private int createTexture() {
		texID = glGenTextures();

		glBindTexture(GL_TEXTURE_2D, texID);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		int format;
		if (comp == 3) {
			if ((width & 3) != 0) {
				glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (width & 1));
			}
			format = GL_RGB;
		} else {
			premultiplyAlpha();

			glEnable(GL_BLEND);
			glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

			format = GL_RGBA;
		}

		glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, image);

//		ByteBuffer input_pixels = image;
//		int input_w = w;
//		int input_h = h;
//		int mipmapLevel = 0;
//		while (1 < input_w || 1 < input_h) {
//			int output_w = Math.max(1, input_w >> 1);
//			int output_h = Math.max(1, input_h >> 1);
//
//			ByteBuffer output_pixels = memAlloc(output_w * output_h * comp);
//			stbir_resize_uint8_generic(input_pixels, input_w, input_h, input_w * comp, output_pixels, output_w,
//					output_h, output_w * comp, comp, comp == 4 ? 3 : STBIR_ALPHA_CHANNEL_NONE,
//					STBIR_FLAG_ALPHA_PREMULTIPLIED, STBIR_EDGE_CLAMP, STBIR_FILTER_MITCHELL, STBIR_COLORSPACE_SRGB);
//
//			if (mipmapLevel == 0) {
//				stbi_image_free(image);
//			} else {
//				memFree(input_pixels);
//			}
//
//			glTexImage2D(GL_TEXTURE_2D, ++mipmapLevel, format, output_w, output_h, 0, format, GL_UNSIGNED_BYTE,
//					output_pixels);
//
//			input_pixels = output_pixels;
//			input_w = output_w;
//			input_h = output_h;
//		}
//		if (mipmapLevel == 0) {
//			stbi_image_free(image);
//		} else {
//			memFree(input_pixels);
//		}

		return texID;
	}

	private void premultiplyAlpha() {
		int stride = width * 4;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int i = y * stride + x * 4;

				float alpha = (image.get(i + 3) & 0xFF) / 255.0f;
				image.put(i + 0, (byte) Math.round(((image.get(i + 0) & 0xFF) * alpha)));
				image.put(i + 1, (byte) Math.round(((image.get(i + 1) & 0xFF) * alpha)));
				image.put(i + 2, (byte) Math.round(((image.get(i + 2) & 0xFF) * alpha)));
			}
		}
	}

}
