package com.ngeneration.furthergui;

import java.util.ArrayList;
import java.util.List;

import com.ngeneration.furthergui.event.ChangeEvent;
import com.ngeneration.furthergui.event.ChangeListener;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.graphics.Icon;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.MathUtil;

public class FSlider extends FComponent {

	public static final int HORIZONTAL = 1, VERTICAL = 2;
	private List<ChangeListener> itemListeners = new ArrayList<>(1);
	private int orientation;
	private float value;
	private Icon icon;
	private FComponent sliderComponent;
	private int size = 20;

	public FSlider() {
		this(HORIZONTAL);
	}

	public FSlider(int orientation) {
		setIcon(new RadioIcon(true));
		this.sliderComponent = createSliderComponent();
		add(this.sliderComponent);
		setOrientation(orientation);
		setBackground(Color.GRAY);
		setForeground(Color.WHITE);
	}

	public void addChangeListener(ChangeListener changeListener) {
		itemListeners.add(changeListener);
	}

	public void removeChangeListener(ChangeListener changeListener) {
		itemListeners.remove(changeListener);
	}

	@Override
	protected void processMouseEvent(MouseEvent event) {
		super.processMouseEvent(event);
		if ((event.getEventType() == MouseEvent.MOUSE_PRESSED || event.getEventType() == MouseEvent.MOUSE_DRAGGED)
				&& !event.isConsumed()) {
			setValue(event);
		}
	}

	private void setValue(MouseEvent event) {
		setValue(orientation == VERTICAL ? (event.getY() - icon.getHeight() / 2) / (float) getMaxLocationInt()
				: (event.getX() - icon.getWidth() / 2) / (float) getMaxLocationInt());
		event.consume();
	}

	public int getOrientation() {
		return orientation;
	}

	public float getValue() {
		return value;
	}

	private int getMaxLocationInt() {
		return orientation == VERTICAL ? sliderComponent.getHeight() : sliderComponent.getWidth();
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public void setSize(int size) {
		this.size = size;
		sliderComponent.setPrefferedSize(new Dimension(size, size));
	}

	public void setOrientation(int orientation) {
		int old = this.orientation;
		this.orientation = orientation;
		if (orientation != old) {
			validate();
		}
	}

	@Override
	public void setBackground(Color background) {
		super.setBackground(background);
	}

	@Override
	public void setForeground(Color background) {
		super.setForeground(background);
		sliderComponent.setBackground(background);
	}

	protected FComponent createSliderComponent() {
		var panel = new FPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				g.setColor(getBackground());
				g.fillRect(0, 0, (int) (getWidth() * (getOrientation() == FSlider.HORIZONTAL ? getValue() : 1)),
						(int) (getHeight() * (getOrientation() == FSlider.HORIZONTAL ? 1 : getValue())));
			}
		};
		panel.setPrefferedSize(new Dimension(size, size));
		return panel;
	}

	public void setValue(float value) {
		float old = this.value;
		this.value = MathUtil.clamp(value, 0.0f, 1.0f);
		if (this.value == old)
			return;
		var event1 = new ChangeEvent(this);
		itemListeners.stream().filter((v) -> !event1.isConsumed()).peek((l) -> l.stateChanged(event1)).count();
		repaint();
	}

	@Override
	public Dimension getPrefferedSize() {
		var psize = super.getPrefferedSize();
		if (psize != null)
			return psize;
		var min = sliderComponent.getPrefferedSize();
		var padding = sliderComponent.getPadding();
		if (orientation == VERTICAL) {
			if (padding.top < icon.getHeight() / 2)
				min.height += icon.getHeight() / 2 - padding.top;
			else if (padding.bottom < icon.getHeight() / 2)
				min.height += icon.getHeight() / 2 - padding.bottom;
		} else {
			if (padding.left < icon.getWidth() / 2)
				min.width += icon.getWidth() / 2 - padding.left;
			else if (padding.right < icon.getWidth() / 2)
				min.width += icon.getWidth() / 2 - padding.right;
		}
		return min;
	}

	@Override
	protected void doLayout() {
		super.doLayout();
		var min = sliderComponent.getPrefferedSize();
		if (orientation == VERTICAL) {
			sliderComponent.setBounds((getWidth() - min.getWidth()) / 2, icon.getHeight() / 2, min.getWidth(),
					getHeight() - icon.getHeight());
		} else {
			sliderComponent.setBounds(icon.getWidth() / 2, (getHeight() - min.getHeight()) / 2,
					getWidth() - icon.getWidth(), min.getHeight());
		}
	}

	@Override
	protected void paintComponents(Graphics g) {
		super.paintComponents(g);
		if (orientation == VERTICAL)
			icon.paint((getWidth() - icon.getWidth()) / 2, (int) ((getHeight() - icon.getHeight()) * value), g);
		else
			icon.paint((int) (getMaxLocationInt() * value), (getHeight() - icon.getHeight()) / 2, g);
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(getBackground());
		if (getOrientation() == FSlider.VERTICAL)
			g.fillRect(0, icon.getHeight() / 2, getWidth(), getHeight() - icon.getHeight());
		else
			g.fillRect(icon.getWidth() / 2, 0, getWidth() - icon.getWidth(), getHeight());
	}

	static class RadioIcon implements Icon {

		private int size = 12;
		private boolean filled;

		public RadioIcon(boolean filled) {
			this.filled = filled;
		}

		@Override
		public int getWidth() {
			return size;
		}

		@Override
		public int getHeight() {
			return size;
		}

		@Override
		public void dispose() {

		}

		@Override
		public void paint(int x, int y, Graphics g) {
			g.setPenSize(1);
			g.setColor(filled ? Color.CYAN : Color.LIGTH_GRAY);
			g.drawCircle(x + getWidth() / 2 + 1, y + getWidth() / 2 + 1, getWidth() / 2, 2);

			if (filled) {
				g.setPenSize(4);
				g.setColor(Color.GREEN);
				g.drawCircle(x + getWidth() / 2 + 1, y + getWidth() / 2 + 1, getWidth() / 8, 1);
			}
		}

	}

}
