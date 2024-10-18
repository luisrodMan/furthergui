package com.ngeneration.furthergui;

import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.layout.Layout;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.MathUtil;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.furthergui.math.Point;

import lombok.Data;

public class FScrollPane extends FPanel {

	private FComponent component;
	private FComponent northComponent;
	private FScroll vscroll;
	private FScroll hscroll;
	private FComponent viewport = new FPanel((Layout)null);

	public FScrollPane(FComponent component) {
		viewport.setPadding(new Padding(0));
		setComponent(component);
		setLayout(new ScrollLayout());
		add(viewport);
		vscroll = new FScroll(FScroll.VERTICAL);
		hscroll = new FScroll(FScroll.HORIZONTAL);
		add(vscroll);
		add(hscroll);
	}

	public void setComponent(FComponent component) {
		if (this.component != null) {
			viewport.remove(this.component);
			if (this.component instanceof FTable && ((FTable) this.component).getHeaderComponent() == northComponent)
				setNorthComponent(null);
		}
		this.component = component;
		if (component != null) {
			viewport.add(component);
			if (component instanceof FTable)
				setNorthComponent(((FTable) component).getHeaderComponent());
		}
	}

	public void setNorthComponent(FComponent component) {
		if (this.northComponent != null)
			remove(northComponent);
		this.northComponent = component;
		if (component != null)
			add(component);
	}

	@Data
	public class FScroll extends FPanel {
		public static int VERTICAL = 1;
		public static int HORIZONTAL = 2;
		private static final int BAR_SIZE = 16;
		private static final int NORMAL_KNOT_SIZE = 8;
		private static final int EXPANDED_KNOT_SIZE = 14;
		private int orientation = VERTICAL;
		private Point lastLocation = null;
		private float knotDiff = 0;
		private Color knotColor = new Color(80, 80, 80);

		private boolean selected;
		private boolean draggin;

		public FScroll(int orientation) {
			this.orientation = orientation;
			setBackground(new Color(60, 60, 60));
			setPrefferedSize(new Dimension(BAR_SIZE, BAR_SIZE));
		}

		@Override
		protected void processMouseEvent(MouseEvent event) {
			super.processMouseEvent(event);
			if (!event.isConsumed()) {
				if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
					lastLocation = event.getLocation();
					knotDiff = getKnotOffset() - (isVertical() ? event.getY() : event.getX());
					event.consume();
				}

				if (event.getEventType() == MouseEvent.MOUSE_DRAGGED)
					draggin = true;

				if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
					selected = true;
					repaint();
				} else if (event.getEventType() == MouseEvent.MOUSE_EXITED && !draggin) {
					selected = false;
					repaint();
				} else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
					draggin = false;
					if (!containsOnLocal(event.getLocation())) {
						selected = false;
						repaint();
					}
				}
				if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
//					Point offset = event.getLocation().sub(lastLocation);
//					lastLocation = event.getLocation();
//					setOffset(getOffset() + (!isVertical() ? offset.getX() : offset.getY()));
					// by follow mouse
					event.consume();
					float v = (isVertical() ? event.getY() : event.getX()) + knotDiff;
					setOffset(viewToModel(v));
				}
			}
		}

		public int viewToModel(float value) {
			value = MathUtil.clamp(value, 0, getKnotMaxOffset());
			value /= getKnotMaxOffset();
			return (int) (getMaxOffset() * value);
		}

		private boolean isVertical() {
			return orientation == VERTICAL;
		}

		public int getOffset() {
			var padding = FScrollPane.this.getPadding();
			return (isVertical() ? (component.getY() - padding.top) : (component.getX() - padding.left)) * -1;
		}

		/**
		 * 
		 * @return component max offset
		 */
		public int getMaxOffset() {
			return Math.max(0,
					isVertical() ? (component.getHeight() - getHeight()) : (component.getWidth() - getWidth()));
		}

		public void setOffset(int offset) {
			int currentOffset = getOffset();
			offset = MathUtil.clamp(offset, 0, getMaxOffset());
			if (offset == currentOffset)
				return;
			var padding = FScrollPane.this.getPadding();
			if (isVertical())
				component.setLocation(component.getX(), offset * -1 + padding.top);
			else
				component.setLocation(offset * -1 + padding.left, component.getY());
			getParent().revalidate();
			getParent().repaint();
		}

		private int getKnotMaxOffset() {
			return (isVertical() ? getHeight() : getWidth()) - getKnotSize();
		}

		private float getVisiblePercentage() {
			return isVertical() ? (getHeight() / (float) component.getHeight())
					: (getWidth() / (float) component.getWidth());
		}

		private int getKnotOffset() {
			return (int) ((float) getOffset() / getMaxOffset() * getKnotMaxOffset());
		}

		private int getKnotSize() {
			float percentage = getVisiblePercentage();
			return (int) (isVertical() ? percentage * getHeight() : percentage * getWidth());
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			// paint knot
			g.setColor(knotColor);
			int knotOffset = getKnotOffset();
			int size = selected ? EXPANDED_KNOT_SIZE : NORMAL_KNOT_SIZE;
			int v = (BAR_SIZE - size) / 2;
			if (isVertical())
				g.fillRect(v, knotOffset, size, getKnotSize());
			else
				g.fillRect(knotOffset, v, getKnotSize(), size);
		}

	}

	@Override
	public boolean isInvalidable() {
		return false;
	}

	public class ScrollLayout implements Layout {

		@Override
		public void addComponent(FComponent component, Object constraints) {
		}

		@Override
		public Dimension getPrefferedDimension(FComponent container) {
			var dim = container.getPadding().toDimension()
					.add(component != null ? component.getPrefferedSize() : new Dimension());
			if (northComponent != null)
				dim.height += northComponent.getPrefferedSize().height;
			return dim;
		}

		@Override
		public void layout(FComponent container) {
			boolean isVerticalScrollActive = true;
			boolean isHorizontalScrollActive = true;
			var padding = container.getPadding();
			int left = padding.left;
			int right = container.getWidth() - padding.right;
			int northSize = (northComponent != null ? northComponent.getPrefferedSize().height : 0);
			int top = padding.top + northSize;
			int bottom = container.getHeight() - padding.bottom;
			hscroll.setVisible(false);
			vscroll.setVisible(false);
			boolean wasActiveH = hscroll.isVisible();
			boolean wasActiveV = vscroll.isVisible();
			if (component != null) {
				var psize = component.getPrefferedSize();
				boolean hscrollActive = vscroll != null && isHorizontalScrollActive;
				boolean vscrollActive = hscroll != null && isVerticalScrollActive;

				if (psize.width <= right - left)
					hscrollActive = false;
				if (psize.height <= bottom - top)
					vscrollActive = false;
				if (vscrollActive && psize.width > right - left - vscroll.getPrefferedSize().width) {
					hscrollActive = true;
//					right = Math.max(left, right - vscroll.getPrefferedSize().width);
				}
				if (hscrollActive && psize.height > bottom - top - hscroll.getPrefferedSize().height) {
					vscrollActive = true;
//					bottom = Math.max(top, bottom - hscroll.getPrefferedSize().height);
				}

				if (vscrollActive) {
					var ps = vscroll.getPrefferedSize();
					vscroll.setVisible(true);
					int scrollHeight = bottom - top;
					right -= ps.width;
					vscroll.setBounds(right, top, ps.width, scrollHeight);
				} else
					vscroll.setVisible(false);
				if (hscrollActive) {
					hscroll.setVisible(true);
					var ps = hscroll.getPrefferedSize();
					bottom -= ps.height;
					hscroll.setBounds(left, bottom, right - left, ps.height);
				} else
					hscroll.setVisible(false);

				int w = Math.max(psize.width, right - left);
				int h = Math.max(psize.height, bottom - top);
				int x = component.getX();
				int y = component.getY();
				if (hscrollActive) {
					x = MathUtil.clamp(component.getX(), (right - left) - w + padding.left, 0);
				}
				if (vscrollActive) {
					y = MathUtil.clamp(component.getY(), (bottom - top) - h + padding.top, 0);
				}
				viewport.setBounds(left, top, right - left, bottom - top);
				component.setBounds(x, y, w, h);
			}
			if (northComponent != null) {
				northComponent.setBounds(padding.left, padding.top, viewport.getWidth(), northSize);
			}
		}

		@Override
		public void remove(FComponent component) {
		}

	}

	public void setRowHeaderView(FPanel fPanel) {
		// ruler left ?? xdxdxdxdx rows
	}

}