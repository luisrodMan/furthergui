package com.ngeneration.furthergui.event;

import com.ngeneration.furthergui.FComponent;

import lombok.Getter;

@Getter
public class Event {

	public static final int SHIFT_MASK = 1;
	public static final int CTRL_MASK = 2;
	public static final int ALT_MASK = 4;
	public static final int WINDOW_MASK = 8;

	private FComponent source;
	private boolean consumed;
	private int mods;

	public Event(FComponent source) {
		this(source, 0);
	}

	public Event(FComponent source, int mods) {
		this.source = source;
		this.mods = mods;
	}

	public void consume() {
		consumed = true;
	}

	public int getMods() {
		return mods;
	}

	public boolean isControlDown() {
		return hasMods(CTRL_MASK);
	}

	public boolean isShiftDown() {
		return hasMods(SHIFT_MASK);
	}

	public boolean isAltDown() {
		return hasMods(ALT_MASK);
	}

	public boolean hasMods(int mods) {
		return (this.mods & mods) == mods;
	}

}
