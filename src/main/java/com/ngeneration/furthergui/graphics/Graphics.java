package com.ngeneration.furthergui.graphics;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glScissor;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgramiv;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderiv;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;

import com.ngeneration.furthergui.FurtherApp;
import com.ngeneration.furthergui.math.Point;
import com.ngeneration.furthergui.math.Rectangle;

import lombok.Data;

@Data
public class Graphics {

	private static final int FLOATS_BY_TEXTURE = 4 * 2 + 4 * 2 + 4 * 4;// vertices, uv, color

	private Color color = new Color(255, 255, 255);
	private Image pixel = new Image("..\\furthergui\\src\\main\\resources\\pixel.png");

	private float[] vertices;
	private int verticesIndex = 0;

	private FloatBuffer floatBuffer;

	private int VAO;
	private int VBO;
	private int EBO;

	private int shaderProgram;

	private int tx;

	private int ty;

	private FFont font;

	private int activeTexture = -1;

	private Rectangle scissor;

	private float penSize = 1;

	public Color getColor() {
		return new Color(color);
	}

	public void setFont(FFont font) {
		this.font = font;
		if (font == null) {
			// setToDefault
			this.font = FurtherApp.getInstance().getDefaultFont();
		}
	}

	public Graphics(int width, int height, int maxTextures) {
		this.height = height;

		int vertexShader;
		vertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShader, vertexShaderSource);
		glCompileShader(vertexShader);

		int[] status = new int[1];
		glGetShaderiv(vertexShader, GL_COMPILE_STATUS, status);

		System.out.println("vertext: " + status[0]);
		if (status[0] < 1) {
			String msg = glGetShaderInfoLog(vertexShader);
			System.out.println(msg);
		}

		int fragmentShader;
		fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShader, fragmentShaderSource);
		glCompileShader(fragmentShader);

		glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, status);

		System.out.println("fragment: " + status[0]);
		if (status[0] < 1) {
			String msg = glGetShaderInfoLog(fragmentShader);
			System.out.println("fragment: " + msg);
		}

		shaderProgram = glCreateProgram();

		glAttachShader(shaderProgram, vertexShader);
		glAttachShader(shaderProgram, fragmentShader);
		glLinkProgram(shaderProgram);

		glGetProgramiv(shaderProgram, GL_LINK_STATUS, status);
		System.out.println("program: " + status[0]);
		if (status[0] < 1) {
			String msg = glGetProgramInfoLog(shaderProgram);
			System.out.println("program: " + msg);
		}

		vertices = new float[maxTextures * FLOATS_BY_TEXTURE];
		floatBuffer = ByteBuffer.allocate(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		short[] indices = new short[maxTextures * 6];
		for (int i = 0, j = 0; i < maxTextures * 4; i += 4) {
			indices[j++] = (short) i;
			indices[j++] = (short) (i + 1);
			indices[j++] = (short) (i + 2);
			indices[j++] = (short) (i + 2);
			indices[j++] = (short) (i + 3);
			indices[j++] = (short) (i);
		}

		glUseProgram(shaderProgram);
		glUniform1f(0, width / 2);
		glUniform1f(1, height / 2);
		glUseProgram(0);

		EBO = glGenBuffers();
		VBO = glGenBuffers();
		VAO = glGenVertexArrays();

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

		glBindVertexArray(VAO);

		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 8 * 4, 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 8 * 4, 2 * 4);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(2, 4, GL_FLOAT, false, 8 * 4, 4 * 4);
		glEnableVertexAttribArray(2);

//		glBindVertexArray(VAO);
//		glEnableVertexAttribArray(0);
//		glBindBuffer(GL_ARRAY_BUFFER, VBO);
//		glVertexAttribPointer(0, 2, GL_FLOAT, false, 3*4, 0);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);

		glBindVertexArray(0);
		glActiveTexture(GL_TEXTURE0); // activate the texture unit first before binding texture
	}

	public void setTranslation(Point location) {
		setTranslation(location.getX(), location.getY());
	}

	/**
	 * Top left screen cords.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void setScissor(int x, int y, int width, int height) {
		scissor = new Rectangle(x, y, width, height);
		glScissor(x, this.height - y - height, width, height);
	}

	public void setScissor(Rectangle rect) {
		setScissor(rect.x, rect.y, rect.width, rect.height);
	}

	public Rectangle getScissor() {
		return scissor == null ? null : new Rectangle(scissor);
	}

	public void setTranslation(int x, int y) {
		this.tx = x;
		this.ty = y;
	}

	public int getTranslationX() {
		return tx;
	}

	public int getTranslationY() {
		return ty;
	}

	public void drawString(float x, float y, String text) {
		if (font != null) {
			font.drawString(this, x, y, text);
		}
	}

	public void fillRect(float x, float y, int width, int height) {
		drawTexture(pixel, x, y, width, height);
	}

	public void setPenSize(float size) {
		if (size < 0)
			throw new RuntimeException("invalid pen size: " + size);
		this.penSize = size;
	}

	public void drawTexture(Texture image, float x, float y, int width, int height) {
		drawTexture(image, x, y, 0, 0, image.getWidth(), image.getHeight(), width, height);
	}

	public void drawLine(float x1, float y1, float x2, float y2) {
		float a = x2 - x1;
		float b = y2 - y1;
		float length = (float) Math.sqrt(a * a + b * b);
		float angle = (float) (Math.atan2(b, a) * 180 / Math.PI);
		drawTexture(pixel, x1, y1, 0, 0, 1, 1, 0, 0, length, penSize, angle);
	}

	public void drawCircle(float x, float y, float radius, int fidelity) {
		fidelity *= 3;
		var circleArray = getArrayCircle(fidelity);
		for (int j = 0; j < 4; j++) {
			for (int i = 1; i < circleArray.length; i++) {
				int v = (j % 3 == 0 ? 1 : -1);
				drawLine(x + circleArray[i - 1][0] * radius * v, y + circleArray[i - 1][1] * radius * (j > 1 ? -1 : 1),
						x + circleArray[i][0] * radius * v, y + circleArray[i][1] * radius * (j > 1 ? -1 : 1));
			}
		}
	}

	private float[][] getArrayCircle(int fidelity) {
		float[][] array = new float[fidelity + 1][2];
		float step = (float) (Math.PI / 2 / fidelity);
		for (int i = 1; i < array.length - 1; i++) {
			float cos = (float) Math.cos(step * (i));
			float sin = (float) Math.sin(step * (i));
			array[i][0] = (1) * cos + (0) * -sin;
			array[i][1] = (1) * sin + (0) * cos;
		}
		array[0][0] = 1;
		array[0][1] = 0;
		array[array.length - 1][0] = 0;
		array[array.length - 1][1] = 1;
		return array;
	}

	public void drawTexture(Texture image, float x, float y, int sx, int sy, int sw, int sh, int width, int height) {
		drawTexture(image, x, y, sx, sy, sw, sh, 0, 0, (float) width / sw, (float) height / sh, 0);
	}

	public void drawTexture(Texture image, float x, float y, int sx, int sy, int sw, int sh, float originx,
			float originy, float scalex, float scaley, float rotation) {
		bindTexture(image);
		x += tx;
		y += ty;
		// image from left top to right bottom
		float tw = image.getWidth();
		float th = image.getHeight();
		float uvL = sx / tw;
		float uvR = uvL + sw / tw;
		float uvB = sy / th;
		float uvT = uvB + sh / th;

		// x cos sin
		// y -sin cos
		// 90 cos 0 sin 1
		// 0,100 -> 0 + 100, 0 + 0 -> 100,0
		float radians = (float) (rotation * Math.PI / 180);
		float cos = (float) Math.cos(radians);
		float sin = (float) Math.sin(radians);

		float rt = (sw - originx) * scalex;
		float yt = -originy * scaley;
		float ll = -originx * scalex;
		float rb = (sh - originy) * scaley;

		vertices[verticesIndex++] = x + (rt * cos + yt * -sin);
		vertices[verticesIndex++] = y + (rt * sin + yt * cos);
		vertices[verticesIndex++] = uvR;
		vertices[verticesIndex++] = uvB;
		vertices[verticesIndex++] = color.getRed();
		vertices[verticesIndex++] = color.getGreen();
		vertices[verticesIndex++] = color.getBlue();
		vertices[verticesIndex++] = color.getAlpha();

		vertices[verticesIndex++] = x + (rt * cos + rb * -sin);
		vertices[verticesIndex++] = y + (rt * sin + rb * cos);
		vertices[verticesIndex++] = uvR;
		vertices[verticesIndex++] = uvT;
		vertices[verticesIndex++] = color.getRed();
		vertices[verticesIndex++] = color.getGreen();
		vertices[verticesIndex++] = color.getBlue();
		vertices[verticesIndex++] = color.getAlpha();

		vertices[verticesIndex++] = x + (ll * cos + rb * -sin);
		vertices[verticesIndex++] = y + (ll * sin + rb * cos);
		vertices[verticesIndex++] = uvL;
		vertices[verticesIndex++] = uvT;
		vertices[verticesIndex++] = color.getRed();
		vertices[verticesIndex++] = color.getGreen();
		vertices[verticesIndex++] = color.getBlue();
		vertices[verticesIndex++] = color.getAlpha();

		vertices[verticesIndex++] = x + (ll * cos + yt * -sin);
		vertices[verticesIndex++] = y + (ll * sin + yt * cos);
		vertices[verticesIndex++] = uvL;
		vertices[verticesIndex++] = uvB;
		vertices[verticesIndex++] = color.getRed();
		vertices[verticesIndex++] = color.getGreen();
		vertices[verticesIndex++] = color.getBlue();
		vertices[verticesIndex++] = color.getAlpha();

		if (verticesIndex >= vertices.length) {
			flush();
		}
	}

	public void fillRect(int x1, int y1, Color color1, int x2, int y2, Color color2, int x3, int y3, Color color3,
			int x4, int y4, Color color4) {
		bindTexture(pixel);
		vertices[verticesIndex++] = tx + x1;
		vertices[verticesIndex++] = ty + y1;
		vertices[verticesIndex++] = 0;
		vertices[verticesIndex++] = 0;
		vertices[verticesIndex++] = color1.getRed();
		vertices[verticesIndex++] = color1.getGreen();
		vertices[verticesIndex++] = color1.getBlue();
		vertices[verticesIndex++] = color1.getAlpha();
		vertices[verticesIndex++] = tx + x2;
		vertices[verticesIndex++] = ty + y2;
		vertices[verticesIndex++] = 0;
		vertices[verticesIndex++] = 1;
		vertices[verticesIndex++] = color2.getRed();
		vertices[verticesIndex++] = color2.getGreen();
		vertices[verticesIndex++] = color2.getBlue();
		vertices[verticesIndex++] = color2.getAlpha();
		vertices[verticesIndex++] = tx + x3;
		vertices[verticesIndex++] = ty + y3;
		vertices[verticesIndex++] = 1;
		vertices[verticesIndex++] = 1;
		vertices[verticesIndex++] = color3.getRed();
		vertices[verticesIndex++] = color3.getGreen();
		vertices[verticesIndex++] = color3.getBlue();
		vertices[verticesIndex++] = color3.getAlpha();
		vertices[verticesIndex++] = tx + x4;
		vertices[verticesIndex++] = ty + y4;
		vertices[verticesIndex++] = 1;
		vertices[verticesIndex++] = 0;
		vertices[verticesIndex++] = color4.getRed();
		vertices[verticesIndex++] = color4.getGreen();
		vertices[verticesIndex++] = color4.getBlue();
		vertices[verticesIndex++] = color4.getAlpha();
		if (verticesIndex >= vertices.length)
			flush();
	}

	public void fillTriangle(int x1, int y1, Color color1, int x2, int y2, Color color2, int x3, int y3, Color color3) {
		bindTexture(pixel);
		vertices[verticesIndex++] = tx + x1;
		vertices[verticesIndex++] = ty + y1;
		vertices[verticesIndex++] = 0;
		vertices[verticesIndex++] = 0;
		vertices[verticesIndex++] = color1.getRed();
		vertices[verticesIndex++] = color1.getGreen();
		vertices[verticesIndex++] = color1.getBlue();
		vertices[verticesIndex++] = color1.getAlpha();
		vertices[verticesIndex++] = tx + x2;
		vertices[verticesIndex++] = ty + y2;
		vertices[verticesIndex++] = 0;
		vertices[verticesIndex++] = 1;
		vertices[verticesIndex++] = color2.getRed();
		vertices[verticesIndex++] = color2.getGreen();
		vertices[verticesIndex++] = color2.getBlue();
		vertices[verticesIndex++] = color2.getAlpha();
		vertices[verticesIndex++] = tx + x3;
		vertices[verticesIndex++] = ty + y3;
		vertices[verticesIndex++] = 1;
		vertices[verticesIndex++] = 1;
		vertices[verticesIndex++] = color3.getRed();
		vertices[verticesIndex++] = color3.getGreen();
		vertices[verticesIndex++] = color3.getBlue();
		vertices[verticesIndex++] = color3.getAlpha();
		vertices[verticesIndex++] = tx + x3;
		vertices[verticesIndex++] = ty + y3;
		vertices[verticesIndex++] = 1;
		vertices[verticesIndex++] = 1;
		vertices[verticesIndex++] = color3.getRed();
		vertices[verticesIndex++] = color3.getGreen();
		vertices[verticesIndex++] = color3.getBlue();
		vertices[verticesIndex++] = color3.getAlpha();
		if (verticesIndex >= vertices.length)
			flush();
	}

	private void bindTexture(Texture texture) {
		if (activeTexture != texture.getTexID())
			flush();
		glBindTexture(GL_TEXTURE_2D, texture.getTexID());
		activeTexture = texture.getTexID();
	}

	public void flush() {

		if (verticesIndex < 1)
			return;

		glEnable(GL_BLEND);
		glBlendFunc(GL11.GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
//		
//		floatBuffer.clear();
////		floatBuffer.put(vertices);
////		floatBuffer.flip();
//		
//		// 2. copy our vertices array in a vertex buffer for OpenGL to use
		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		glUseProgram(shaderProgram);
		glUniform1i(glGetUniformLocation(shaderProgram, "texture1"), 0); // set it manually
		glBindVertexArray(VAO);
		int count = 6 * (verticesIndex / FLOATS_BY_TEXTURE);
		glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_SHORT, 0);

		glBindVertexArray(0);
		verticesIndex = 0;
	}

	public void begin() {
		glUseProgram(shaderProgram);
	}

	public void end() {
		flush();
		glUseProgram(0);
	}

	private String vertexShaderSource = "#version 330 core\n" + "layout (location = 0) in vec2 aPos;\n"
			+ "layout (location = 1) in vec2 aUV;\n" + "layout (location = 2) in vec4 aColor;\n"

			+ "out vec4 finalColor;\n" + "out vec2 TexCord;\n" +

			"uniform float hwidth;\n" + "uniform float hheight;\n" + "void main()\n" + "{\n" + "   TexCord = aUV;"
			+ "	  finalColor = aColor;\n"
			+ "   gl_Position = vec4(-1.0f + aPos.x/hwidth, 1.0f - aPos.y/hheight, 0.0f, 1.0f);\n" + "}";
	private String fragmentShaderSource = "#version 330 core\r\n" + "in vec4 finalColor;\r\n" + "in vec2 TexCord;\n"
			+ "uniform sampler2D texture1;\r\n"

			+ "out vec4 frag_color;\r\n" + "void main()\r\n" + "{\r\n "
			+ "    frag_color = texture(texture1, TexCord) * finalColor;\r\n" + "}";

	private int height;

	public void drawRect(int x, int y, int width, int height) {
		drawLine(x, y, x + width, y);
		drawLine(x + penSize, y, x + penSize, y + height);
		drawLine(x + width, y, x + width, y + height);
		drawLine(x, y + height - penSize, x + width, y + height - penSize);
	}

	public Point getTranslation() {
		return new Point(tx, ty);
	}

}
