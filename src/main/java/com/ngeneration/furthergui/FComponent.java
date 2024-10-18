package com.ngeneration.furthergui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.ngeneration.furthergui.event.Action;
import com.ngeneration.furthergui.event.Event;
import com.ngeneration.furthergui.event.FocusEvent;
import com.ngeneration.furthergui.event.FocusListener;
import com.ngeneration.furthergui.event.KeyEvent;
import com.ngeneration.furthergui.event.KeyListener;
import com.ngeneration.furthergui.event.KeyStroke;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.event.MouseListener;
import com.ngeneration.furthergui.event.MouseMotionListener;
import com.ngeneration.furthergui.event.PropertyEvent;
import com.ngeneration.furthergui.event.PropertyListener;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.FFont;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.layout.Layout;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.furthergui.math.Point;
import com.ngeneration.furthergui.math.Rectangle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class FComponent {

	public static final String DIMENSION_PROPERTY = "p_dimension";
	public static final String LOCATION_PROPERTY = "p_location";
	public static final String BOUNDS_PROPERTY = "p_bounds";

	public static final int WHEN_IN_FOCUSED_WINDOWS = 1;
	public static final int WHEN_FOCUSED = 2;

	private static int _status = 1;
	protected static final int VISIBLE = _status;
	protected static final int MOUSE_ENTERED = (_status *= 2);
	protected static final int MOUSE_PRESSED = (_status *= 2);

	private static final Color defBackground = new Color(35, 35, 35);
	private transient Rectangle bounds = new Rectangle(10, 10);
	private transient Rectangle visibleRectangle = new Rectangle(10, 10);
	private FFont font = FurtherApp.getInstance().getDefaultFont();
	private Color foreground = Color.WHITE;
	private Color background = defBackground;

	private Cursor cursor = Cursor.DEFAULT_CURSOR;
	private Padding padding = new Padding();
	private Dimension prefferedSize;
	private Layout layout;
	private FComponent parent;
	private List<FComponent> components;
	private boolean visible = true;
	private boolean enabled = true;
	private boolean opaque = true;
	private boolean focusable = true;
	private boolean hasFocus = false;
	private boolean validated;
	private List<Listener> listeners = new LinkedList<>();
	private int currentStatus;
	private String name;
	private Map<String, Action> actionMap = new HashMap<>();
	private Map<KeyStroke, String> inputMap;
	private Map<KeyStroke, String> windowInputMap;
	
	private boolean validateForDown = false;
	private FPopupMenu popupMenu;

	public void setPadding(Padding p) {
		padding.set(p);
	}

	public Padding getPadding() {
		return new Padding(padding);
	}

	public void setVisible(boolean value) {
		visible = value;
//		System.out.println("xxxxhere: " + this);
		if (isValidated() && value) {
//			System.out.println("here setVisible: " + this + ": " + getScreenRectangle());
			getGraphicsApplicationManager().paint(this, getScreenRectangle());
		}
//		else if (!(this instanceof FWindow) && value)
//			throw new RuntimeException("sduoifs");
	}

	public int getX() {
		return bounds.x;
	}

	public int getY() {
		return bounds.y;
	}

	public Point getLocation() {
		return bounds.getLocation();
	}

	public int getWidth() {
		return bounds.width;
	}

	public int getHeight() {
		return bounds.height;
	}

	public final void setLocation(Point location) {
		setLocation(location.getX(), location.getY());
	}

	public void setLocation(int x, int y) {
		setBounds(x, y, bounds.width, bounds.height);
	}

	public void setDimension(Dimension dim) {
		setDimension(dim.width, dim.height);

	}

	public void setDimension(int width, int height) {
		setBounds(bounds.x, bounds.y, width, height);
	}

	public Dimension getDimension() {
		return new Dimension(bounds.width, bounds.height);
	}

	public void setBounds(Rectangle bounds) {
		this.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	public void setBounds(int x, int y, int width, int height) {
		boolean dimensionChanged = bounds.width != width || bounds.height != height;
		boolean locationChanged = bounds.x != x || bounds.y != y;
		if (!dimensionChanged && !locationChanged)
			return;
		var old = new Rectangle(bounds);
		validated = false;
		bounds.set(x, y, width, height);
		visibleRectangle.set(0, 0, bounds.width, bounds.height);
		if (dimensionChanged)
			firePropertyListeners(DIMENSION_PROPERTY, old.getDimension());
		if (locationChanged)
			firePropertyListeners(LOCATION_PROPERTY, old.getLocation());
		firePropertyListeners(BOUNDS_PROPERTY, old);
	}

	public boolean contains(int x, int y) {
		return bounds.contains(x, y);
	}

	/**
	 * This method performs a repaint operation
	 */
	public void revalidate() {
		invalidate();
		validate();
	}

	public void invalidate() {
		if (!isValidated())
			return;
		var parent = this;
		while (parent != null) {
			parent.validated = false;
			if (!parent.isInvalidable())
				break;
			parent = parent.getParent();
		}
	}

	/**
	 * This method performs a repaint operation
	 */
	public void validate() {
		if (!validateForDown) {
			// validate from top to bottom
			var topMostInvalidated = this;
			var parent = topMostInvalidated;
			while (parent != null) {
				if (!parent.isValidated())
					topMostInvalidated = parent;
				parent = parent.getParent();
			}
			topMostInvalidated.validateDown();

			// could be dangerous xddxdx
			var win = FurtherApp.getWindow(topMostInvalidated);
			if (win != null && win.isVisible())
				topMostInvalidated.repaint();
		} else {
			visibleRectangle.set(getScreenRectangle());
			if (getParent() != null) {
				visibleRectangle.clamp(getParent().getScreenVisibleRectangle());
			}
			var loc = getScreenLocation();
			visibleRectangle.x -= loc.getX();
			visibleRectangle.y -= loc.getY();
			validated = true;
			doLayout();
			if (components != null)
				components.forEach(c -> c.validateDown());
		}
	}

	private void validateDown() {
		this.validateForDown = true;
		validate();
		this.validateForDown = false;
	}

	protected void doLayout() {
		if (layout != null && getComponentCount() > 0)
			layout.layout(this);
	}

	public Dimension getPrefferedSize() {
		return prefferedSize != null ? prefferedSize
				: (/* UI?? */ layout != null ? layout.getPrefferedDimension(this) : getDimension());
	}

	public void add(FComponent component) {
		add(component, null);
	}

	public void add(FComponent component, Object constraints) {
		add(components != null ? components.size() : 0, component, constraints);
	}

	public void add(int index, FComponent component, Object constraints) {
		if (component.getParent() != null)
			throw new RuntimeException("Component has already a parent: " + component);
		component.parent = this;
		if (components == null)
			components = new LinkedList<>();
		components.add(index, component);

		var window = FurtherApp.getWindow(this);
		if (window != null)
			setWindow(window, component, true);

		if (layout != null)
			layout.addComponent(component, constraints);
		invalidate();
	}

	private void setWindow(FWindow window, FComponent component, boolean add) {
		if (component.windowInputMap != null) {
			if (add)
				FurtherApp.getInputMap(window).put(component, component.getWindowInputMap());
			else
				FurtherApp.getInputMap(window).remove(component);
		}
		if (components != null)
			components.forEach(c -> c.setWindow(window, c, add));
	}

	public void remove(int index) {

	}

	public boolean remove(FComponent component) {
		if (components != null && components.remove(component)) {
			component.parent = null;
			var window = FurtherApp.getWindow(this);
			if (window != null)
				setWindow(window, component, false);
		} else
			return false;
		if (layout != null)
			layout.remove(component);
		return true;
	}

	public int getComponentCount() {
		return components == null ? 0 : components.size();
	}

	public List<FComponent> getComponents() {
		return components == null || components.isEmpty() ? new ArrayList<>(0) : new ArrayList<>(components);
	}

	final void paint(Graphics g) {
		if (!isVisible())
			return;
		if (!isValidated())
			throw new RuntimeException("Invalid state component not validated: " + this);
		int tx = g.getTranslationX();
		int ty = g.getTranslationY();
		Rectangle scissor = g.getScissor();
//		System.out.println("screeb: " + getScreenVisibleRectangle());
		var sr = getScreenVisibleRectangle();
//		System.out.println("sr: " + sr);
//		if (getParent() == null && !(this instanceof FWindow))// to pass clamp on screen cords
//			sr.setLocation(sr.x + tx, sr.y + ty);
//		System.out.println("sr1: " + sr);
		var xd = sr.clamp(scissor);
		g.setScissor(xd);
//		System.out.println("scisso: " + this + "  ~  " + xd);
		paintComponent(g);
		g.flush();
		g.setTranslation(tx, ty);
		g.setScissor(scissor);
		paintComponents(g);
		g.flush();
		g.setScissor(scissor);
	}

	protected FurtherApp getGraphicsApplicationManager() {
		return FurtherApp.getInstance();
	}

	public void repaint() {
		repaint(0, 0, getWidth(), getHeight());
	}

	public void repaint(Rectangle rect) {
		repaint(rect.x, rect.y, rect.width, rect.height);
	}

	public void repaint(int x, int y, int width, int height) {
		FComponent window = null;
		if (!isVisible() || (window = FurtherApp.getWindow(this)) == null || !window.isVisible())
			return;
		var loc = getScreenLocation();
		var screen = new Rectangle(loc.getX() + x, loc.getY() + y, width, height);
//		System.out.println("screen1 " + this + ": " + screen);
		screen.clamp(getScreenVisibleRectangle());
//		System.out.println("screen2 " + this + ": " + screen);
		getGraphicsApplicationManager().paint(this, screen);
	}

	protected abstract void paintComponent(Graphics g);

	protected void paintComponents(Graphics g) {
		int tx = g.getTranslationX();
		int ty = g.getTranslationY();
		if (components != null)
			for (FComponent component : components) {
				if (component.isVisible()) {
					g.setTranslation(tx + component.getX(), ty + component.getY());
					component.paint(g);
				}
			}
		g.setTranslation(tx, ty);
	}

	// status
	private final void addStatus(int status) {
		currentStatus |= status;
	}

	protected boolean hasStatus(int status) {
		return (currentStatus & status) == status;
	}

	private void removeStatus(int status) {
		currentStatus &= ~status;
	}

	// events

	public void addMouseListener(MouseListener mouseListener) {
		listeners.add(new Listener(mouseListener, Listener.MOUSE_EVENT));
	}

	public void addMouseMotionListener(MouseMotionListener mouseMotionListener) {
		listeners.add(new Listener(mouseMotionListener, Listener.MOUSE_MOTION_EVENT));
	}

	protected void processMouseEvent(MouseEvent event) {
		if (event.isConsumed() || !isVisible())
			return;

		var eventType = event.getEventType();
		if (eventType == MouseEvent.MOUSE_MOVED && !hasStatus(MOUSE_ENTERED)) {
			processMouseEvent(fireNewEvent(MouseEvent.MOUSE_ENTERED, event));
//			if (eventType != MouseEvent.MOUSE_DRAGGED)
			FurtherApp.getInstance().setCursor(getCursor());
		} else if (eventType == MouseEvent.MOUSE_PRESSED) {
			addStatus(MOUSE_PRESSED);
		} else if (eventType == MouseEvent.MOUSE_RELEASED) {
			removeStatus(MOUSE_PRESSED);
		} else if (eventType == MouseEvent.MOUSE_ENTERED || eventType == MouseEvent.MOUSE_EXITED) {
			if (eventType == MouseEvent.MOUSE_ENTERED)
				addStatus(MOUSE_ENTERED);
			else
				removeStatus(MOUSE_ENTERED);
			fireEvent(event);
			return;
		}
		var components = getComponents();
		Collections.reverse(components);
		boolean anyComponentContains = false;
		for (FComponent comp : components) {
			if (comp.isVisible()) {
				var compPoint = event.getLocation().sub(comp.getLocation());
				boolean compContains = comp.containsOnVisible(compPoint);
				if (compContains)
					anyComponentContains = true;
				if ((eventType == MouseEvent.MOUSE_PRESSED && compContains)
						|| (eventType == MouseEvent.MOUSE_RELEASED && comp.hasStatus(MOUSE_PRESSED))
						|| (eventType == MouseEvent.MOUSE_DRAGGED && comp.hasStatus(MOUSE_PRESSED))
						|| (eventType == MouseEvent.MOUSE_MOVED && (compContains || comp.hasStatus(MOUSE_ENTERED)))) {
					var newEvent = new MouseEvent(comp, eventType, event.getButton(), event.getClickCount(), compPoint,
							event.getMods());
					comp.processMouseEvent(newEvent);
					if (newEvent.isConsumed()) {
						event.consume();
						break;
					}
				}
			}
		}
		if (!event.isConsumed()) {
			if (eventType == MouseEvent.MOUSE_PRESSED) {
				requestFocus();
			}
			fireEvent(event);
			if (eventType == MouseEvent.MOUSE_RELEASED && !event.isConsumed()) {
				if (event.getButton() == MouseEvent.BUTTON2 && getPopupMenu() != null) {
					getPopupMenu().showVisible(this, event.getX(), event.getY());
				}
				if (event.getClickCount() > 0) {
					processMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_CLICKED, event.getButton(),
							event.getClickCount(), event.getLocation(), event.getMods()));
				}
			}
		}

		boolean mouseExited = false;
		if ((eventType == MouseEvent.MOUSE_MOVED || eventType == MouseEvent.MOUSE_DRAGGED)
				&& !containsOnVisible(event.getLocation())) {
			processMouseEvent(fireNewEvent(MouseEvent.MOUSE_EXITED, event));
			mouseExited = true;
		}
		if (!mouseExited && !anyComponentContains && eventType != MouseEvent.MOUSE_DRAGGED) {
			FurtherApp.getInstance().setCursor(getCursor());
		}
	}

	private MouseEvent fireNewEvent(int type, MouseEvent event) {
		return getEvent(type, event, event.getLocation());
	}

	private MouseEvent getEvent(int type, MouseEvent event, Point point) {
		MouseEvent newEvent = new MouseEvent(this, type, event.getButton(), event.getClickCount(), point,
				event.getMods());
		return newEvent;
	}

	private void fireEvent(MouseEvent event) {
		if (event.getEventType() <= MouseEvent.MOUSE_EXITED) {
			var stream = listeners.stream().filter(l -> l.type == Listener.MOUSE_EVENT).map(l -> l.listener)
					.map(MouseListener.class::cast);
			if (event.getEventType() == MouseEvent.MOUSE_PRESSED)
				stream.filter((v) -> !event.isConsumed()).peek((l) -> l.mousePressed(event)).count();
			else if (event.getEventType() == MouseEvent.MOUSE_RELEASED)
				stream.filter((v) -> !event.isConsumed()).peek((l) -> l.mouseReleased(event)).count();
			else if (event.getEventType() == MouseEvent.MOUSE_CLICKED)
				stream.filter((v) -> !event.isConsumed()).peek((l) -> l.mouseClicked(event)).count();
			else if (event.getEventType() == MouseEvent.MOUSE_ENTERED)
				stream.filter((v) -> !event.isConsumed()).peek((l) -> l.mouseEntered(event)).count();
			else if (event.getEventType() == MouseEvent.MOUSE_EXITED)
				stream.filter((v) -> !event.isConsumed()).peek((l) -> l.mouseExited(event)).count();
		} else {
			var stream = listeners.stream().filter(l -> l.type == Listener.MOUSE_MOTION_EVENT).map(l -> l.listener)
					.map(MouseMotionListener.class::cast);
			if (event.getEventType() == MouseEvent.MOUSE_MOVED)
				stream.filter((v) -> !event.isConsumed()).peek((l) -> l.mouseMoved(event)).count();
			else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED)
				stream.filter((v) -> !event.isConsumed()).peek((l) -> l.mouseDragged(event)).count();
		}
	}

	/**
	 * 
	 * @param point in parent coordinates
	 * @return true if this component contains the specified point
	 */
	public final boolean contains(Point point) {
		return bounds.contains(point);
	}

	public final boolean containsOnLocal(Point point) {
		return bounds.contains(point.getX() + getX(), point.getY() + getY());
	}

	public final boolean containsOnVisible(Point point) {
		return visibleRectangle.contains(point);
	}

	protected Rectangle getVisibleRectangle() {
		return new Rectangle(visibleRectangle);
	}

	@AllArgsConstructor
	private class Listener {
		private static int eventIdx;
		private static final int FOCUS_EVENT = eventIdx++;
		private static final int MOUSE_EVENT = eventIdx++;
		private static final int MOUSE_MOTION_EVENT = eventIdx++;
		private static final int KEY_EVENT = eventIdx++;
		private static final int PROPERTY_EVENT = eventIdx++;
		private Object listener;
		private int type;
	}

	public Point getScreenLocation() {
		Point location = getLocation();
		return getParent() == null ? location : location.add(getParent().getScreenLocation());
	}

	public Rectangle getScreenRectangle() {
		Point location = getScreenLocation();
		return new Rectangle(location.getX(), location.getY(), getWidth(), getHeight());
	}

	public Rectangle getScreenVisibleRectangle() {
		var loc = getScreenLocation();
		var vis = getVisibleRectangle();
		vis.setLocation(vis.x + loc.getX(), vis.y + loc.getY());
		return vis;
	}

	@Override
	public String toString() {
		String value = super.toString();
		return name == null ? value : value + " : " + name;
	}

	public void setComponentPopupMenu(FPopupMenu popupMenu) {
		this.popupMenu = popupMenu;
	}

	public FPopupMenu getPopupMenu() {
		return popupMenu;
	}

	public Map<String, Action> getActionMap() {
		return this.actionMap;
	}

	public Map<KeyStroke, String> getInputMap(int condition) {
		if (condition == WHEN_IN_FOCUSED_WINDOWS) {
			if (windowInputMap == null) {
				windowInputMap = new HashMap<>();
				var window = FurtherApp.getWindow(this);
				if (window != null)
					FurtherApp.getInputMap(window).put(this, windowInputMap);
			}
			return windowInputMap;
		} else
			return inputMap != null ? inputMap : (inputMap = new HashMap<>());
	}

	public void addKeyListener(KeyListener keyListener) {
		listeners.add(new Listener(keyListener, Listener.KEY_EVENT));
	}

	public boolean isInvalidable() {
		return true;
	}

	public void removeAll() {
		if (getComponentCount() > 0) {
			getComponents().forEach(this::remove);
			invalidate();
		}
	}

	public boolean hasFocus() {
		return hasFocus;
	}

	public boolean isFocusable() {
		return focusable;
	}

	public void requestFocus() {
		if (!hasFocus() && isFocusable())
			FurtherApp.getInstance().setFocusedControl(this);
	}

	public boolean isComponentOnScreen() {
		var parent = this;
		FComponent found = null;
		while (parent != null) {
			found = parent;
			parent = parent.getParent();
		}
		return found instanceof FWindow && ((FWindow) found).isVisible();
	}

	public FComponent getComponent(int i) {
		return components.get(i);
	}

	public int getComponentIndex(FComponent selectedTab) {
		if (getComponentCount() > 0) {
			int i = 0;
			for (var c : components) {
				if (c == selectedTab)
					return i;
				i++;
			}
		}
		return -1;
	}

	void processFocusEventInternal(FocusEvent focusEvent) {
		if ((focusEvent.isFocusGained() && !focusable) || (!focusEvent.isFocusGained() && !hasFocus))
			return;
		var comp = this;
		while (comp != null) {
			comp.processFocusEvent(new FocusEvent(comp, focusEvent.isFocusGained()));
			comp = comp.getParent();
		}
	}

	protected void processFocusEvent(FocusEvent focusEvent) {
		if (!focusEvent.isConsumed()) {
			var event = new FocusEvent(this, focusEvent.isFocusGained());
			listeners.stream().filter(l -> l.type == Listener.FOCUS_EVENT).map(l -> l.listener)
					.map(FocusListener.class::cast).filter((v) -> !event.isConsumed()).peek((l) -> {
						if (focusEvent.isFocusGained())
							l.focusGained(event);
						else
							l.focusLost(event);
					}).count();
			if (!event.isConsumed()) {
				hasFocus = focusEvent.isFocusGained();
			}
		}
	}

	protected void processKeyEvent(KeyEvent keyEvent) {
		if (keyEvent.isConsumed())
			return;
		List<Listener> list = listeners.stream().filter(l -> l.type == Listener.KEY_EVENT).toList();
		for (var l : list) {
			KeyListener listener = (KeyListener) l.listener;
			if (keyEvent.getAction() == KeyEvent.KEY_PRESSED || keyEvent.getAction() == KeyEvent.KEY_REPEATED)
				listener.keyPressed(keyEvent);
			else if (keyEvent.getAction() == KeyEvent.KEY_RELEASED)
				listener.keyReleased(keyEvent);
			else if (keyEvent.getAction() == KeyEvent.KEY_TYPED)
				listener.keyTyped(keyEvent);
			if (keyEvent.isConsumed())
				break;
		}
		FurtherApp.handleKeyStroke(this, keyEvent, inputMap, actionMap);
	}

	private void removeListener(int type, Object listener) {
		listeners.stream().filter(l -> l.type == type && l.listener == listener).findAny().ifPresent(listeners::remove);
	}

	public void addPropertyListener(PropertyListener propertyListener) {
		listeners.add(new Listener(propertyListener, Listener.PROPERTY_EVENT));
	}

	public void removePropertyListener(PropertyListener propertyListener) {
		removeListener(Listener.PROPERTY_EVENT, propertyListener);
	}

	private void fireEvent(int type, Event event, Consumer<Object> listener) {
		listeners.stream().filter(l -> l.type == type && !event.isConsumed()).peek(l -> listener.accept(l.listener))
				.count();
	}

	protected void firePropertyListeners(String propertyName, Object oldValue) {
		var event = new PropertyEvent(this, propertyName, oldValue);
		fireEvent(Listener.PROPERTY_EVENT, event, l -> ((PropertyListener) l).onPropertyChanged(event));
	}

}
