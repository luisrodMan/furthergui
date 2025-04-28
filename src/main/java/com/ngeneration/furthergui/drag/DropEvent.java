package com.ngeneration.furthergui.drag;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.event.Event;
import com.ngeneration.furthergui.math.Point;

public class DropEvent extends Event {

	public static final int ACCEPTED_EVENT = 1;
	public static final int TEST_EVENT = 2;
	public static final int CANCEL_EVENT = 3;

	public static final int COPY_MODE = 1;
	public static final int MOVE_MODE = 2;
	public static final int ANY_MODE = 3;
	private int eventType;
	private int mode;
	private FComponent other;
	private Transferable transferable;
	private Point location;

	public DropEvent(int eventType, Point location, int mods, FComponent source, FComponent other,
			Transferable transferable, int mode) {
		super(source, mods);
		this.eventType = eventType;
		this.location = location;
		this.other = other;
		this.mode = mode;
		this.transferable = transferable;
	}

	public int getEventType() {
		return eventType;
	}

	public Point getLocation() {
		return location;
	}

	public FComponent getOther() {
		return other;
	}

	public int getMode() {
		return mode;
	}

	public Transferable getTransferable() {
		return transferable;
	}

	public void acept(int mode) {
		consume();
		this.mode = mode;
	}

	public void acept(Transferable transfer, int mode) {
		consume();
		this.transferable = transfer;
		this.mode = mode;
	}

}
