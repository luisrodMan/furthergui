package com.ngeneration.furthergui;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.ngeneration.furthergui.event.ActionEvent;
import com.ngeneration.furthergui.event.ActionListener;
import com.ngeneration.furthergui.event.MouseAdapter;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.graphics.Icon;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Padding;

public class FComboBox<T> extends FComponent {

	private List<T> items = new LinkedList<>();
	private ListCellRenderer<T> renderer;
	private ComboBoxEditor editor;
	private int selectedIndex = -1;
	private FButton button = new FButton(new ArrowIcon());
	private FComponent separator = new FPanel();

	private Color hoverColor = Color.LIGTH_GRAY.darker().darker().darker().darker();
	private int mode = -1;
	private FPopupWindow menuWindow;
	private FList<T> list;

	private Set<ActionListener> listeners = new LinkedHashSet<>();

	public FComboBox() {
		setBackground(Color.GRAY);
		setRenderer(new DefaultListCellRenderer<T>());
		setEditor(new DefaultComboboxEditor());
		setPadding(new Padding(0));
		button.setHoverBackground(hoverColor);
		button.addActionListener(x -> setPopupVisible(true));
		separator.setBackground(Color.DARK_GRAY.darker().darker());
		separator.setPrefferedSize(new Dimension(1, 1));

		setMode(0);
		menuWindow = new FPopupWindow();
		menuWindow.setLayout(new BorderLayout());
		list = new FList<>();
		list.setPadding(new Padding(10));
		list.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent event) {
				var selected = list.viewToModel(event.getLocation());
				if (selected != null) {
					setSelectedItem(selected);
				}
				menuWindow.setVisible(false);
			}
		});
		menuWindow.add(list);
	}

	public void setEditor(ComboBoxEditor defaultComboboxEditor) {
		this.editor = defaultComboboxEditor;
	}

	public ComboBoxEditor getEditor() {
		return editor;
	}

	@Override
	protected void processMouseEvent(MouseEvent event) {
		super.processMouseEvent(event);
		if (event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == MouseEvent.MOUSE_EXITED)
			repaint();
		else if (!isEditable() && event.getEventType() == MouseEvent.MOUSE_RELEASED
				&& containsOnVisible(event.getLocation())) {
			if (menuWindow.isVisible())
				menuWindow.setVisible(false);
			else
				setPopupVisible(true);
		}
	}

	public void setPopupVisible(boolean b) {
		list.clearList();
		list.setCellRenderer(getRenderer());
		items.forEach(i -> list.addItem(i));
		menuWindow.showVisible(this, 0, getHeight());
	}

	public void setEditable(boolean b) {
		setMode(b ? 1 : (mode));
		getEditor().setItem(getSelectedItem());
	}

	public boolean isEditable() {
		return mode == 1;
	}

	private void setMode(int mode) {
		boolean change = this.mode != mode;
		this.mode = mode;
		if (change) {
			removeAll();
			setOpaque(mode != 1);
			if (mode == 1) {
				setLayout(new BorderLayout());
				add(button, BorderLayout.EAST);
			} else {
				var layout = new FlowLayout(FlowLayout.RIGHT);
				layout.setGap(0);
				layout.setFilled(true);
				setLayout(layout);
				add(button);
			}
			button.setPadding(mode == 0 ? new Padding(0, 5, 5, 5) : new Padding(5));
			if (mode == 1)
				add(getEditor().getEditorComponent());
			if (mode == 2)
				add(separator);
			button.setOpaque(mode == 2);
		}
	}

	@Override
	public Dimension getPrefferedSize() {
		var size = super.getPrefferedSize();
		var selection = getSelectedItem();

		var content = new Dimension();
		if (mode != 1) {
			content = (selection == null ? new Dimension(10, getFont().getFontHeight())
					: getRenderer().getRendererComponent(list, selection, true, hasFocus(), getSelectedIndex())
							.getPrefferedSize());
		}
		size.set(size.width + content.width, Math.max(size.height, content.height + getPadding().getVertical()));
		return size;
	}

	public ListCellRenderer<T> getRenderer() {
		return renderer;
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (isOpaque()) {
			g.setColor(hasStatus(MOUSE_ENTERED) ? hoverColor : getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		if (!isEditable()) {
			var selected = getSelectedItem();
			if (selected != null) {
				var itemomponent = renderer.getRendererComponent(list, selected, true, false, getSelectedIndex());
				itemomponent.setBounds(g.getTranslationX(), g.getTranslationY(), getWidth(), getHeight());
				itemomponent.validate();
				itemomponent.paint(g);
			}
		}
	}

	public void addItem(T item) {
		addItem(items.size(), item);
	}

	public void addItem(int index, T item) {
		items.add(index, item);
		if (selectedIndex == -1)
			setSelectedIndex(0);
	}

	public void setMaximumSize(Dimension prefferedSize) {

	}

	public T getSelectedItem() {
		return getSelectedIndex() == -1 ? null : getItemAt(getSelectedIndex());
	}

	public void setRenderer(ListCellRenderer<T> defaultListCellRenderer) {
		this.renderer = defaultListCellRenderer;
	}

	public void setSelectedIndex(int selectedMethodIndex) {
		setSelectedIndexInternal(selectedMethodIndex);
	}

	public void setSelectedItem(T environment) {
		setSelectedIndex(items.indexOf(environment));
	}

	public void setSelectedIndexInternal(int selectedMethodIndex) {
		boolean change = this.selectedIndex != selectedMethodIndex;
		selectedIndex = selectedMethodIndex > -1 ? selectedMethodIndex : -1;
		if (change) {
			if (isEditable())
				getEditor().setItem(getSelectedItem());
			else {
				revalidate();
				var event = new ActionEvent(this);
				listeners.forEach(l -> l.actionPerformed(event));
			}
		}
	}

	public int getItemCount() {
		return items.size();
	}

	public T getItemAt(int i) {
		return items.get(i);
	}

	public List<T> getItems() {
		return new LinkedList<>(items);
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void addActionListener(ActionListener object) {
		listeners.add(object);
	}

	public void removeActionListener(ActionListener object) {
		listeners.remove(object);
	}

	public static class ArrowIcon implements Icon {

		private int size = 20;
		private int height = 5;
		private int width = 10;
		private Color color = Color.LIGTH_GRAY.lighter().lighter();

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
			float xx = x + ((getWidth() - width) * 0.5f);
			float yy = y + ((getHeight() - height) * 0.5f);
			g.setColor(color);
			g.setPenSize(1);
			g.drawLine(xx, yy, xx + width * 0.5f, yy + height);
			g.drawLine(xx + width, yy, xx + width * 0.5f, yy + height);
		}

	}

	public static class DefaultComboboxEditor implements ComboBoxEditor {

		private Object item;
		private FTextField field = new FTextField();

		public DefaultComboboxEditor() {
			field.setOpaque(false);
		}

		@Override
		public FComponent getEditorComponent() {
			return field;
		}

		@Override
		public void setItem(Object anObject) {
			item = anObject;
			field.setText(item == null ? "" : item.toString());
		}

		@Override
		public Object getItem() {
			return item;
		}

		@Override
		public void selectAll() {
			field.setSelection(0, field.getLength());
		}

		@Override
		public void addActionListener(java.awt.event.ActionListener l) {

		}

		@Override
		public void removeActionListener(java.awt.event.ActionListener l) {

		}

	}

	public boolean containsItem(T item) {
		return items.contains(item);
	}

	public void removeAllItems() {
		int count = items.size();
		items.clear();
		if (count > 0)
			setSelectedIndexInternal(-1);
		revalidate();
	}

}
