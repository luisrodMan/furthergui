package com.ngeneration.furthergui;

import java.util.HashMap;
import java.util.Map;

public class Cursor {

	private static Map<Integer, Cursor> cursors = new HashMap<>();

	/**
	 * The default cursor type (gets set if no cursor is defined).
	 */
	public static final int ARROW_CURSOR = 0x00036001;

	/**
	 * The default cursor type (gets set if no cursor is defined).
	 */
	public static final Cursor DEFAULT_CURSOR = getStandardCursor(ARROW_CURSOR);

	/**
	 * The crosshair cursor type.
	 */
	public static final int CROSSHAIR_CURSOR = 0x00036003;

	/**
	 * The text cursor type.
	 */
	public static final int TEXT_CURSOR = 0x00036002;

	/**
	 * The wait cursor type.
	 */
	public static final int WAIT_CURSOR = 3;

	/**
	 * The south-west-resize cursor type.
	 */
	public static final int SW_RESIZE_CURSOR = 4;

	/**
	 * The south-east-resize cursor type.
	 */
	public static final int SE_RESIZE_CURSOR = 5;

	/**
	 * The north-west-resize cursor type.
	 */
	public static final int NW_RESIZE_CURSOR = 6;

	/**
	 * The north-east-resize cursor type.
	 */
	public static final int NE_RESIZE_CURSOR = 7;

	/**
	 * The north-resize cursor type.
	 */
	public static final int N_RESIZE_CURSOR = 0x00036006;

	/**
	 * The south-resize cursor type.
	 */
	public static final int S_RESIZE_CURSOR = 0x00036006;

	/**
	 * The west-resize cursor type.
	 */
	public static final int W_RESIZE_CURSOR = 0x00036005;

	/**
	 * The east-resize cursor type.
	 */
	public static final int E_RESIZE_CURSOR = 0x00036005;

	/**
	 * The hand cursor type.
	 */
	public static final int HAND_CURSOR = 221188;
	public static final int NOT_ALLOWED_CURSOR = 221194;

	/**
	 * The move cursor type.
	 */
	public static final int MOVE_CURSOR = 13;

	private int id;

	private Cursor(int cursor) {
		this.id = cursor;
	}
//	private Cursor(Image image) {
//		this.id = < 1000;
//	}

	public int getId() {
		return id;
	}

	public static Cursor getStandardCursor(int cursor) {
		if (ARROW_CURSOR == cursor || CROSSHAIR_CURSOR == cursor || TEXT_CURSOR == cursor || N_RESIZE_CURSOR == cursor
				|| S_RESIZE_CURSOR == cursor || W_RESIZE_CURSOR == cursor || E_RESIZE_CURSOR == cursor
				|| HAND_CURSOR == cursor || cursor == NOT_ALLOWED_CURSOR) {
			cursors.put(cursor, new Cursor(cursor));
			return cursors.get(cursor);
		}
		throw new RuntimeException("Not a standard cursor: " + cursor);
	}

}
