package com.ngeneration.furthergui;

import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.layout.Layout;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.MathUtil;
import com.ngeneration.furthergui.math.Padding;

public class FSplitPane extends FPanel {

	public static final int HORIZONTAL = 1;
	public static final int VERTICAL = 2;
	private FComponent leftComponent;
	private FComponent rightComponent;
	private int orientation;

	private int pivotSize = 6;

	private float pivotValue = 0.5f;

	private boolean pressedToDrag = false;

	public FSplitPane() {
		this(HORIZONTAL);
	}

	public FSplitPane(int orientation) {
		this(null, null, orientation);
	}

	public FSplitPane(FComponent component1, FComponent component2) {
		this(component1, component2, HORIZONTAL);
	}

	public FSplitPane(FComponent component1, FComponent component2, int orientation) {
		setOrientation(orientation);
		setPadding(new Padding());
//		setBackground(new Color(30, 30, 30));
		setLayout(new SplitLayout());
		setLeftComponent(component1);
		setRightComponent(component2);
	}

	@Override
	protected void processMouseEvent(MouseEvent event) {
		super.processMouseEvent(event);
		if (!event.isConsumed()) {
			if (event.getEventType() == MouseEvent.MOUSE_PRESSED && leftComponent != null && rightComponent != null
					&& !leftComponent.contains(event.getLocation()) && !rightComponent.contains(event.getLocation()))
				pressedToDrag = true;
			else if (event.getEventType() == MouseEvent.MOUSE_RELEASED)
				pressedToDrag = false;
			else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && pressedToDrag)
				setDividerLocation(orientation == HORIZONTAL ? event.getX() : event.getY());
		}

	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
		setCursor(
				Cursor.getStandardCursor(orientation == HORIZONTAL ? Cursor.W_RESIZE_CURSOR : Cursor.N_RESIZE_CURSOR));
	}

	public void setLeftComponent(FComponent leftComponent) {
		this.leftComponent = leftComponent;
		if (leftComponent != null)
			add(leftComponent);
	}

	public void setRightComponent(FComponent rightComponent) {
		this.rightComponent = rightComponent;
		if (rightComponent != null)
			add(rightComponent);
	}

	public int getMaxLocation() {
		Padding p = getPadding();
		return orientation == HORIZONTAL ? (getWidth() - p.left - p.right - pivotSize)
				: (getHeight() - p.top - p.bottom - pivotSize);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.GRAY);
		g.setPenSize(1);
		if (orientation == VERTICAL)
			g.drawLine(0, getDividerLocation() + pivotSize * 0.5f, getWidth(), getDividerLocation() + pivotSize * 0.5f);
		else
			g.drawLine(getDividerLocation() + pivotSize * 0.5f, 0, getDividerLocation() + pivotSize * 0.5f,
					getHeight());
	}

	private class SplitLayout implements Layout {

		@Override
		public void addComponent(FComponent component, Object constraints) {
		}

		@Override
		public Dimension getPrefferedDimension(FComponent component) {
			Dimension dimension = component.getPadding().toDimension();
			Dimension c1 = null;
			Dimension c2 = null;
			if (leftComponent != null)
				c1 = leftComponent.getPrefferedSize();
			if (rightComponent != null)
				c2 = rightComponent.getPrefferedSize();
			if (orientation == HORIZONTAL) {
				if (c1 != null) {
					dimension.width += c1.width;
					dimension.height = Math.max(dimension.height, c1.height);
				}
				if (c2 != null) {
					dimension.width += c2.width;
					dimension.height = Math.max(dimension.height, c2.height);
				}
				if (c1 != null && c2 != null)
					dimension.width += pivotSize;
			} else {
				if (c1 != null) {
					dimension.height += c1.height;
					dimension.width = Math.max(dimension.width, c1.width);
				}
				if (c2 != null) {
					dimension.height += c2.height;
					dimension.width = Math.max(dimension.width, c2.width);
				}
				if (c1 != null && c2 != null)
					dimension.height += pivotSize;
			}
			return dimension;
		}

		@Override
		public void layout(FComponent component) {
			Padding p = component.getPadding();
			boolean both = leftComponent != null && rightComponent != null;
			int x = p.left;
			int y = p.top;
			int max = getMaxLocation();
			if (!both) {
				FComponent component1 = leftComponent != null ? leftComponent : rightComponent;
				if (component1 != null)
					component1.setBounds(x, y, component.getWidth() - x - p.right,
							component.getHeight() - y - p.bottom);
			} else {
				if (orientation == HORIZONTAL) {
					int d = (int) (pivotValue * max);
					int maxHeight = component.getHeight() - p.top - p.bottom;
					leftComponent.setBounds(x, y, d, maxHeight);
					x += d + pivotSize;
					rightComponent.setBounds(x, y, Math.max(component.getWidth() - p.right - x, 0), maxHeight);
				} else {
					int d = (int) (pivotValue * max);
					int maxWidth = component.getWidth() - p.left - p.right;
					leftComponent.setBounds(x, y, maxWidth, d);
					y += d + pivotSize;
					rightComponent.setBounds(x, y, maxWidth, Math.max(component.getHeight() - p.bottom - y, 0));
				}
			}
		}

		@Override
		public void remove(FComponent component) {

		}

	}

	public void setDividerLocation(int value) {
		value = MathUtil.clamp(value, 0, getMaxLocation());
		float np = (float) value / getMaxLocation();
		if (np == pivotValue)
			return;
		pivotValue = np;
		if (isComponentOnScreen()) {
			doLayout();
			validate();
			repaint();
		}
	}

	public int getDividerLocation() {
		return (int) (pivotValue * getMaxLocation());
	}

}
