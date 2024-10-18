package com.ngeneration.furthergui;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_DOUBLEBUFFER;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import com.ngeneration.furthergui.event.Action;
import com.ngeneration.furthergui.event.ActionEvent;
import com.ngeneration.furthergui.event.FocusEvent;
import com.ngeneration.furthergui.event.KeyEvent;
import com.ngeneration.furthergui.event.KeyStroke;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.FFont;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Point;
import com.ngeneration.furthergui.math.Rectangle;

import lombok.Data;

@Data
public class FurtherApp {

	private static FurtherApp instance;

	private static Map<FWindow, Map<FComponent, Map<KeyStroke, String>>> inputMap = new HashMap<>();
	private static List<Integer> pressedKeys = new ArrayList<>();

	public static Map<FComponent, Map<KeyStroke, String>> getInputMap(FWindow window) {
		return inputMap.computeIfAbsent(window, w -> new HashMap<>());
	}

	public static List<Integer> getPressedKeys() {
		return new ArrayList<>(pressedKeys);
	}

	// The window handle
	private long window;

	private int width = 500;
	private int height = 500;

	private Graphics graphics;

	private List<FWindow> windows = new ArrayList<>();
	private FComponent focusedComponent;

	private FFont defaultFont;

	private int metaModifiers;
	private Point mouseLocation = new Point();
	private Point pressedLocation = new Point();
	private int clickCount = 0;

	private long lastTimeClick;

	private List<Runnable> toInvokeList = new LinkedList<>();

	private boolean mousePressed;

	public FurtherApp() {
		instance = this;
	}

	public FFont getDefaultFont() {
		if (defaultFont == null) {
			defaultFont = new FFont(new java.awt.Font("xxx", java.awt.Font.PLAIN, 15));
		}
		return defaultFont;
	}

	public void run(Consumer<Void> consumer) {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		init();

		// estaba en loop
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		graphics = new Graphics(width, height, 10);
		glViewport(0, 0, width, height);

		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_BLEND);
		glEnable(GL_SCISSOR_TEST);

		consumer.accept(null);
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_DOUBLEBUFFER, GL_FALSE);// double buffer disabled - use framebuffer instead
		// Create the window
		window = glfwCreateWindow(width, height, "Hello World!", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated
		// or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});

		// Get the thread stack and push a new frame
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);

		// Enable v-sync
		// By default, the swap interval is zero, meaning buffer swapping will occur
		// immediately.
//		glfwSwapInterval(1); -- 

		// events
		GLFW.glfwSetCursorPosCallback(window, (long window, double mx, double my) -> {
			mouseLocation.set((int) mx, (int) my);
			processMouseEvent(mousePressed ? MouseEvent.MOUSE_DRAGGED : MouseEvent.MOUSE_MOVED, MouseEvent.BUTTON1,
					metaModifiers);
		});
		GLFW.glfwSetMouseButtonCallback(window, (long window, int button, int action, int mods) -> {
//			System.out.println("action: " + action + "   b" + button);
			metaModifiers = mods;
			processMouseEvent(action == 1 ? MouseEvent.MOUSE_PRESSED : MouseEvent.MOUSE_RELEASED, button, mods);
		});

		GLFW.glfwSetKeyCallback(window, new GLFWKeyCallbackI() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				metaModifiers = mods;
				if (action == KeyEvent.KEY_PRESSED)
					pressedKeys.add(key);
				else if (action == KeyEvent.KEY_RELEASED)
					pressedKeys.remove((Object) key);
				if (focusedComponent != null) {
					var event = new KeyEvent(focusedComponent, key, action, mods);
					focusedComponent.processKeyEvent(event);
//				System.out.println("°key: " + scancode + " " + key);
				}
			}
		});
		GLFW.glfwSetCharCallback(window, new GLFWCharCallbackI() {
			@Override
			public void invoke(long window, int codepoint) {
				if (focusedComponent != null)
					focusedComponent.processKeyEvent(new KeyEvent(focusedComponent, codepoint, KeyEvent.KEY_TYPED, 0));
			}
		});

		// Make the window visible
		glfwShowWindow(window);
	}

	private void processMouseEvent(int eventType, int button, int mods) {
		if (eventType == MouseEvent.MOUSE_PRESSED) {
			var pressedLocation = new Point(mouseLocation);
			// click
			long newTimeClick = System.currentTimeMillis();
			if ((newTimeClick - lastTimeClick) > 300 || !pressedLocation.equals(this.pressedLocation))
				clickCount = 0;
			clickCount++;
			lastTimeClick = newTimeClick;
			this.pressedLocation = pressedLocation;
			mousePressed = true;
		} else if (eventType == MouseEvent.MOUSE_RELEASED) {
			if (mouseLocation.equals(pressedLocation)) {
				lastTimeClick = System.currentTimeMillis();
			} else
				clickCount = 0;
			mousePressed = false;
		} else {
			long newTimeClick = System.currentTimeMillis();
			if (newTimeClick - lastTimeClick > 300)
				clickCount = 0;
		}
		var windows = new ArrayList<>(this.windows);
		for (int i = 0; i < windows.size(); i++) {
			var window = windows.get(i);
			Point point = new Point(mouseLocation).sub(window.getLocation());
			boolean containsPoint = window.contains(mouseLocation);
			boolean stop = false;

			if (containsPoint
					|| (eventType == MouseEvent.MOUSE_RELEASED && window.hasStatus(FComponent.MOUSE_PRESSED))) {
				if (stop = (containsPoint && !(window instanceof FPopupWindow) && eventType == MouseEvent.MOUSE_PRESSED)
						|| (!containsPoint && eventType == MouseEvent.MOUSE_RELEASED
								&& window instanceof FPopupWindow)) {
					clearPopupWindows();
				}
				if (window instanceof FPopupWindow)
					stop = true;

				// clear events on other windows
				if ((eventType == MouseEvent.MOUSE_MOVED && containsPoint)
						|| (eventType == MouseEvent.MOUSE_RELEASED && stop)) {
					for (i += 1; i < windows.size(); i++) {
						var w = windows.get(i);
						if (eventType != MouseEvent.MOUSE_RELEASED || window.hasStatus(FComponent.MOUSE_PRESSED))
							w.processMouseEvent(new MouseEvent(window,
									eventType == MouseEvent.MOUSE_MOVED ? MouseEvent.MOUSE_EXITED : eventType, button,
									clickCount, new Point(mouseLocation).sub(window.getLocation()), mods));
					}
					stop = true;
				}

				window.processMouseEvent(new MouseEvent(window, eventType, button, clickCount, point, mods));
				if (eventType == MouseEvent.MOUSE_RELEASED && clickCount > 0)
					window.processMouseEvent(
							new MouseEvent(window, MouseEvent.MOUSE_CLICKED, button, clickCount, point, mods));

				if (eventType == MouseEvent.MOUSE_MOVED && containsPoint) {
					stop = true;
				}
			}

			if (stop || window.isAlwaysOnTop()) {
				break;
			}
		}
	}

	public void clearPopupWindows() {
		new LinkedList<>(windows).stream().filter(FPopupWindow.class::isInstance).forEach(w -> w.setVisible(false));
	}

	private void loop() {

		// Set the clear color

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		glClearColor(1.15f, 0.1f, 0.45f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer
//		glfwSwapBuffers(window); // swap the color buffers
		while (!glfwWindowShouldClose(window)) {
			internalLoop();
		}
		new ArrayList<>(windows).forEach(w -> w.dispose());
	}

	private void internalLoop() {
		var list = new ArrayList<>(toInvokeList);
		toInvokeList.clear();
		list.forEach(c -> c.run());

		// Poll for window events. The key callback above will only be
		// invoked during this call.
		glfwPollEvents();
	}

	public static FurtherApp getInstance() {
		return instance;
	}

	public void addWindow(FWindow fWindow) {
		int index = 0;
		if (!fWindow.isAlwaysOnTop() && !(fWindow instanceof FPopupWindow)) {
			for (int i = 0; i < windows.size(); i++) {
				var existingWindow = windows.get(i);
				if (!existingWindow.isAlwaysOnTop() && !(existingWindow instanceof FPopupWindow))
					break;
				index++;
			}
		}
		windows.add(index, fWindow);
	}

	public void removeWindow(FWindow fWindow) {
		windows.remove(fWindow);
		paintScreen(fWindow.getScreenVisibleRectangle());
	}

	public void paintScreen(int sx, int sy, int swidth, int sheight) {
		paintScreen(new Rectangle(sx, sy, swidth, sheight));
	}

	public void paint(FComponent fComponent, Rectangle rectangle) {
//		paintComponents(Arrays.asList(fComponent), rectangle);
//		paintComponents(windows, rectangle);
		paintScreen(rectangle);
	}

	public void paintScreen(Rectangle rectangle) {
		// paint from back N to front 0
		var reversedList = new LinkedList<FComponent>();
		windows.stream().forEach(w -> reversedList.add(0, w));
		paintComponents(reversedList, rectangle);
	}

	private void paintComponents(List<FComponent> components, Rectangle target) {
		invokeLater(() -> {
			var screenRectangle = new Rectangle(0, 0, getWidth(), getHeight());
			target.clamp(screenRectangle);
			components.forEach(c -> {
				var componentRect = c.getScreenVisibleRectangle();
				componentRect.clamp(target);
				if (c.isVisible() && componentRect.width > 0 && componentRect.height > 0) {
					graphics.setScissor(target);
					graphics.setTranslation(c.getScreenLocation());
//					System.out.println("paintingxd1: " + c + ": " + target);
//					System.out.println("paintingxd2: " + c + ": " + componentRect);
					c.paint(graphics);
				}
			});
//			glfwSwapBuffers(window); // swap the color buffers
//			glFlush();
			glFinish(); // instead of glfwSwapBuffers(double buffer)
		});
	}

	public void invokeLater(Runnable runnable) {
		toInvokeList.add(runnable);
		var size = toInvokeList.size();
		if (size > 2)// leave enter
			System.out.println("to invoke size: " + size);
		if (toInvokeList.size() == 40)
			throw new RuntimeException();
	}

	public void blockByDialog(FDialog fDialog) {
		while (fDialog.isVisible()) {
			internalLoop();
		}
	}

	public Dimension getDimension() {
		return new Dimension(width, height);
	}

	private Cursor lastCursor = null;
	private Map<Integer, Long> createdCursors = new HashMap<>();

	private boolean runningLoop;

	public void setCursor(Cursor cursor) {
		if (lastCursor != null && lastCursor.getId() == cursor.getId())
			return;
		var createdCursor = createdCursors.get(cursor.getId());
		if (createdCursor == null) {
			if (cursor.getId() < 1000)
				throw new RuntimeException("Not implemented");
//			unsigned char pixels[16 * 16 * 4];
//			memset(pixels, 0xff, sizeof(pixels));
//			 
//			GLFWimage image;
//			image.width = 16;
//			image.height = 16;
//			image.pixels = pixels;
//			 
//			GLFWcursor* cursor = glfwCreateCursor(&image, 0, 0);
			else {
				createdCursor = GLFW.glfwCreateStandardCursor(cursor.getId());
			}
			createdCursors.put(cursor.getId(), createdCursor);
		}
//		glfwDestroyCursor(cursor);
		GLFW.glfwSetCursor(window, createdCursor);

		lastCursor = cursor;
	}

	public void setFocusedControl(FComponent fComponent) {
		if (fComponent != this.focusedComponent) {
			if (this.focusedComponent != null)
				this.focusedComponent.processFocusEventInternal(new FocusEvent(this.focusedComponent, false));
			this.focusedComponent = fComponent;
			if (this.focusedComponent != null)
				this.focusedComponent.processFocusEventInternal(new FocusEvent(fComponent, true));
		}
	}

	static void handleKeyStroke(FComponent fComponent, KeyEvent keyEvent, Map<KeyStroke, String> inputMap,
			Map<String, Action> actionMap) {
		if (inputMap != null && actionMap != null && !keyEvent.isConsumed()
				&& (keyEvent.getAction() == KeyEvent.KEY_PRESSED || keyEvent.getAction() == KeyEvent.KEY_REPEATED)) {
			var pressedKeys = FurtherApp.getPressedKeys();
			pressedKeys.remove((Object) KeyEvent.VK_CONTROL);
			pressedKeys.remove((Object) KeyEvent.VK_SHIFT);
			pressedKeys.remove((Object) KeyEvent.VK_ALT);
			var keys = new int[pressedKeys.size()];
			for (int i = 0; i < keys.length; i++)
				keys[i] = pressedKeys.get(i);
			var targetKeyStroke = new KeyStroke(keyEvent.getMods(), keys);
			var keyStroke = inputMap.keySet().stream().filter(st -> st.equals(targetKeyStroke)).findAny();
			if (keyStroke.isPresent())
				fireKeyStroke(fComponent, inputMap, keyStroke.get());
			else {
				for (var map : getInputMap(getWindow(fComponent)).entrySet()) {
					keyStroke = map.getValue().keySet().stream().filter(st -> st.equals(targetKeyStroke)).findAny();
					if (keyStroke.isPresent()) {
						fireKeyStroke(map.getKey(), map.getValue(), keyStroke.get());
						break;
					}
				}
			}
		}

	}

	private static void fireKeyStroke(FComponent fComponent, Map<KeyStroke, String> inputMap2, KeyStroke keyStroke) {
		var actionName = inputMap2.get(keyStroke);
		var action = fComponent.getActionMap().get(actionName);
		if (action != null)
			action.actionPerformed(new ActionEvent(fComponent));
		else
			throw new RuntimeException("Action not found: " + actionName + " on object: " + fComponent);
	}

	public static FWindow getWindow(FComponent component) {
		FComponent f = component;
		while (f.getParent() != null) {
			f = f.getParent();
		}
		return f instanceof FWindow ? (FWindow) f : null;
	}

}