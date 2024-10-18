package com.ngeneration.furthergui;

import java.util.ArrayList;
import java.util.List;

import com.ngeneration.furthergui.event.PopupMenuEvent;
import com.ngeneration.furthergui.event.PopupMenuListener;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Padding;

public class FPopupMenu extends FPopupWindow {

	public static final Color POPUP_COLOR = Color.DARK_GRAY.darker().darker().darker();

	private List<PopupMenuListener> listeners = new ArrayList<>(1);
	private FComponent lastComponent = null;

	public FPopupMenu() {
		setLayout(new FlowLayout(FlowLayout.TOP_TO_BOTTOM, 0, true));
		setPadding(new Padding(7, 3));
		setBackground(POPUP_COLOR);
	}

	public FMenuItem add(FMenuItem item) {
		if (lastComponent == null) {
			lastComponent = new FPanel(new FlowLayout(FlowLayout.TOP_TO_BOTTOM, 0, true));
			add(lastComponent);
		}
		lastComponent.add(item);
		return item;
	}

	public List<FMenuItem> getItems() {
		return getComponents().stream().filter(c -> c.getComponentCount() > 0).flatMap(c -> c.getComponents().stream())
				.filter(FMenuItem.class::isInstance).map(FMenuItem.class::cast).toList();
	}

	public void clearAll() {
		removeAll();
		lastComponent = null;
	}

	public void addSeparator() {
		lastComponent = null;
		var separator = new FPanel();
		separator.setBackground(Color.GRAY.darker().darker());
		separator.setPrefferedSize(new Dimension(1, 1));
		add(separator);
	}

	public void addPopupMenuListener(PopupMenuListener popupMeuListener) {
		listeners.add(popupMeuListener);
	}

	public void removePopupMenuListener(PopupMenuListener popupMeuListener) {
		listeners.remove(popupMeuListener);
	}

	@Override
	public void setVisible(boolean value) {
		PopupMenuEvent event = new PopupMenuEvent(this);
		if (value && !isVisible())
			listeners.forEach(e -> e.popupMenuWillBecomeVisible(event));
		else if (!value && isVisible())
			listeners.forEach(e -> e.popupMenuWillBecomeInvisible(event));
		if (event.isCanceled()) {
			listeners.forEach(e -> e.popupMenuCanceled(event));
			return;
		}
		super.setVisible(value);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.GRAY);
		g.drawRect(1, 1, getWidth() - 1, getHeight() - 1);
	}

}
