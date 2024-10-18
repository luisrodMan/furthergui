package com.ngeneration.furthergui.event;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.math.Point;

import lombok.Getter;

@Getter
public class MouseEvent extends Event {

	public static final int MOUSE_EVENT = 1;
	public static final int MOUSE_MOTION_EVENT = 0;

	public static final int MOUSE_PRESSED = 0;
	public static final int MOUSE_RELEASED = 1;
	public static final int MOUSE_CLICKED = 2;
	public static final int MOUSE_ENTERED = 3;
	public static final int MOUSE_EXITED = 4;
	public static final int MOUSE_MOVED = 5;
	public static final int MOUSE_DRAGGED = 6;
	public static final int BUTTON1 = 0;
	/**Window right button.*/
	public static final int BUTTON2 = 1;
	/**Window middle button.*/
	public static final int BUTTON3 = 2;

	private int eventType;
	private int button;
	private int clickCount;
	private int x;
	private int y;

	public MouseEvent(FComponent component, int eventType, int button, int clickCount, Point p, int modifiers) {
		super(component, modifiers);
		this.eventType = eventType;
		this.button = button;
		this.clickCount = clickCount;
		this.x = p.getX();
		this.y = p.getY();
	}

	public Point getLocation() {
		return new Point(x, y);
	}

	@Deprecated
	public Point getPoint() {
		return getLocation();
	}

}
