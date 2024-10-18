package com.ngeneration.furthergui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.ListSelectionListener;

import com.ngeneration.furthergui.event.Event;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.layout.Layout;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.furthergui.math.Point;
import com.ngeneration.furthergui.math.Rectangle;

import lombok.Getter;
import lombok.Setter;

public class FList<T> extends FPanel {

	@Getter
	@Setter
	private Color selectionColor = Color.GRAY;
	private Color hoverColor = selectionColor.lighter().lighter();
	private List<Item> items = new LinkedList<>();
	private List<T> selection = new LinkedList<>();
	private List<SelectionChangeListener> listeners = new ArrayList<>(1);

	@Getter
	@Setter
	private ListCellRenderer<T> itemRenderer;
	private T hoveredItem;

	public FList() {
		this(null);
	}

	public FList(ListCellRenderer<T> renderer) {
		setBackground(Color.DARK_GRAY.darker().darker());
		setLayout(new ListLayout());
		setPadding(new Padding());
		setItemRenderer(renderer != null ? renderer : new DefaultListCellRenderer<>());
	}

	@Override
	protected void processMouseEvent(MouseEvent event) {
		super.processMouseEvent(event);
		if (!event.isConsumed()) {
			if (event.getEventType() == MouseEvent.MOUSE_MOVED || event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
				var hoveredItem = viewToModel(event.getLocation());
				if (hoveredItem != this.hoveredItem) {
					this.hoveredItem = hoveredItem;
					repaint();
				}
			} else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
				var item = viewToModel(event.getLocation());
				if (item != null && !selection.contains(item)) {
					selection.clear();
					selection.add(item);
					fireSelectionListeners();
				}
			}
		}
	}

	public List<SelectionChangeListener> getSelectionListeners() {
		return listeners == null ? new LinkedList<>() : new ArrayList<>(listeners);
	}

	public void addSelectionChangeListener(SelectionChangeListener listener) {
		listeners.add(listener);
	}

	public void removeSelectionChangeListener(SelectionChangeListener listener) {
		listeners.remove(listener);
	}

	public T getSelectedItem() {
		return selection.isEmpty() ? null : selection.get(0);
	}

	public List<T> getSelection() {
		return new ArrayList<>(selection);
	}

	public void removeAllItems() {
		int count = getItemCount();
		items.clear();
		if (count > 0) {
			selection.clear();
			fireSelectionListeners();
		}
	}

	private void fireSelectionListeners() {
		repaint();
		SelectionChangeEvent event = new SelectionChangeEvent(this);
		getSelectionListeners().stream().filter(l -> !event.isConsumed()).peek(l -> l.onSelectionChanged(event))
				.count();
	}

	public static class SelectionChangeEvent extends Event {

		public SelectionChangeEvent(FComponent source) {
			super(source);
		}

	}

	public static interface SelectionChangeListener {

		void onSelectionChanged(SelectionChangeEvent event);

	}

	public void addItem(T item) {
		addItem(items.size(), item);
	}

	public void addItem(int index, T item) {
		items.add(index, new Item(item));
	}

	public T viewToModel(Point location) {
		int y = location.getY() - getPadding().top;
		int yy = 0;
		for (Item item : items) {
			if (y >= yy && y <= yy + item.height) {
				return item.item;
			}
			yy += item.height;
		}
		return null;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.GRAY);
		g.setPenSize(1);
		g.drawRect(1, 1, getWidth() - 1, getHeight() - 1);
		g.flush();
		var padding = getPadding();
		int y = g.getTranslationY() + padding.top;
		int x = g.getTranslationX() + padding.left;
		int i = 0;
		for (Item item : items) {
			var selected = selection.contains(item.item);
			boolean hovered = item.item == hoveredItem;
			var rendererComponent = itemRenderer.getRendererComponent(this, item.item, selected, false, i++);
			rendererComponent.setBounds(x, y, getWidth() - padding.left - padding.right, item.height);
			g.setTranslation(x, y);
			g.setColor(hovered ? hoverColor : (selected ? selectionColor : getBackground()));
			g.fillRect(0, 0, getWidth() - padding.getHorizontal(), item.height);
			rendererComponent.validate();
			rendererComponent.paint(g);
			y += item.height;
		}
	}

	private class ListLayout implements Layout {

		@Override
		public void addComponent(FComponent component, Object constraints) {
		}

		@Override
		public Dimension getPrefferedDimension(FComponent container) {
			return dimension(container);
		}

		private Dimension dimension(FComponent container) {
			var dimension = container.getPadding().toDimension();
			int i = 0;
			int maxWidth = 0;
			for (Item item : items) {
				var com = itemRenderer.getRendererComponent((FList<T>) container, item.item, false, false, i++);
				var dim = com.getPrefferedSize();
				item.height = dim.height;
				dimension.height += dim.height;
				maxWidth = Math.max(maxWidth, dim.width);
			}
			dimension.width += maxWidth;
			return dimension;
		}

		@Override
		public void layout(FComponent container) {
			dimension(container);
		}

		@Override
		public void remove(FComponent component) {

		}
	}

	public int getItemCount() {
		return items.size();
	}

	public T getItem(int index) {
		return items.get(index).item;
	}

	private class Item {
		private T item;
		private int height;

		public Item(T item) {
			this.item = item;
		}
	}

	public void addElement(T iterationElement) {

	}

	public void setCellRenderer(ListCellRenderer<T> defaultListCellRenderer) {
		this.itemRenderer = defaultListCellRenderer;
	}

	public int[] getSelectedIndices() {
		return null;
	}

	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	public T getElementAt(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSelectedIndex(int i) {
		// TODO Auto-generated method stub

	}

	public void clearSelection() {
		// TODO Auto-generated method stub

	}

	public void setSelectionInterval(int i, int j) {

	}

	public void removeElement(T item) {
		// TODO Auto-generated method stub

	}

	public void insertElement(T item, int i) {
		// TODO Auto-generated method stub

	}

	public void addSelectionInterval(int firstSelection, int i) {

	}

	public void insertElementAt(int index, T item) {
		// TODO Auto-generated method stub
	}

	public void addListSelectionListener(ListSelectionListener listSelectionListener) {

	}

	public List<T> getSelectedValuesList() {
		return null;
	}

	public int locationToIndex(Point point) {
		return 0;
	}

	public Rectangle getCellBounds(int index, int index2) {
		return null;
	}

	public void clearList() {
		items.clear();
	}
}
