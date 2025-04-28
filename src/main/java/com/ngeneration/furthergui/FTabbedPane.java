package com.ngeneration.furthergui;

import java.util.ArrayList;
import java.util.List;

import com.ngeneration.furthergui.event.ChangeEvent;
import com.ngeneration.furthergui.event.ChangeListener;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.layout.OneVisibleLayout;
import com.ngeneration.furthergui.math.Padding;

public class FTabbedPane extends FPanel {

	public static final int BOTTOM = 2;
	private final Color hintColor = new Color(0, 115, 245);
	private int hintSize = 1;
	private final Color borderColor = Color.GRAY;

	private FPanel headerPanel = new HeaderPanel(new FlowLayout(FlowLayout.LEFT, 0));
	private FPanel containerPanel = new FPanel(new OneVisibleLayout());
	private TabComponent selectedTab;

	private List<ChangeListener> changeListeners = new ArrayList<>(1);
	public int borderSize = 1;

	public FTabbedPane() {
		super(new BorderLayout());
		containerPanel.setPadding(new Padding());
		headerPanel.setBackground(new Color(10, 20, 20));
		add(headerPanel, BorderLayout.NORTH);
		add(containerPanel, BorderLayout.CENTER);
	}

	@Override
	public boolean isInvalidable() {
		return false;
	}

	public void addChangeListener(ChangeListener changeListener) {
		changeListeners.add(changeListener);
	}

	public void removeChangeListener(ChangeListener changeListener) {
		changeListeners.remove(changeListener);
	}

	public void addTab(String title, FComponent component) {
		if (component == null)
			throw new RuntimeException("Can not add null component: " + component);
		var header = new TabComponent(component, new FLabel(title));
		headerPanel.add(header);
		containerPanel.add(component);
		revalidate();
		if (getTabCount() == 1) {
			setSelectedIndex(0);
		} else
			fireChangeListener();
	}

	public FComponent getSelectedComponent() {
		return selectedTab == null ? null : selectedTab.userComponent;
	}

	public FComponent getComponentAt(int i) {
		validateIndex(i);
		return ((TabComponent) headerPanel.getComponent(i)).userComponent;
	}

	private void validateIndex(int i) {
		if (i < 0 || i >= headerPanel.getComponentCount())
			throw new RuntimeException("invalid tab index: " + i + " size: " + headerPanel.getComponentCount());
	}

	public int getTabCount() {
		return headerPanel.getComponentCount();
	}

	@Override
	public void validate() {
		super.validate();
		if (getTabCount() > 0 && getSelectedIndex() > -1) {
			selectedTab = (TabComponent) headerPanel.getComponent(getSelectedIndex());
			((OneVisibleLayout) containerPanel.getLayout()).setVisible(containerPanel, selectedTab.userComponent);
		}
	}

	public void setSelectedIndex(int index) {
		validateIndex(index);
		if (getSelectedIndex() == index)
			return;
		selectedTab = (TabComponent) headerPanel.getComponent(index);
		((OneVisibleLayout) containerPanel.getLayout()).setVisible(containerPanel, selectedTab.userComponent);
//		System.out.println("user component - " + selectedTab.userComponent);
		selectedTab.userComponent.requestFocus();
		if (isComponentOnScreen()) {
			validate();
			FTabbedPane.this.repaint();
		}
		fireChangeListener();
	}

	public void removeTab(int viewIndex) {
		validateIndex(viewIndex);
		removeTab(getComponentAt(viewIndex));
	}

	public void removeTab(FComponent component) {
		TabComponent found = null;
		for (var f : headerPanel.getComponents()) {
			if (((TabComponent) f).userComponent == component) {
				found = (TabComponent) f;
				break;
			}
		}
		if (found == null)
			throw new RuntimeException("Component not added: " + component);
		headerPanel.remove(found);
		containerPanel.remove(found.userComponent);
		if (getTabCount() == 0) {
			selectedTab = null;
			revalidate();
			fireChangeListener();
		} else {
			setSelectedIndex(getTabCount() - 1);
			revalidate();
		}
	}

	private void fireChangeListener() {
		ChangeEvent event = new ChangeEvent(this);
		changeListeners.forEach(l -> l.stateChanged(event));
		if (isComponentOnScreen()) {
			validate();
		}
	}

	/**
	 * 
	 * @param i
	 * @return the visual tab component in tab
	 */
	public FComponent getTabComponentAt(int i) {
		validateIndex(i);
		return ((TabComponent) headerPanel.getComponent(i)).tabComponent;
	}

	public int getSelectedIndex() {
		return headerPanel == null ? -1 : headerPanel.getComponentIndex(selectedTab);
	}

	public void setTabPlacement(int placement) {

	}

	private class TabComponent extends FPanel {

		private FComponent userComponent;
		private FComponent tabComponent;
		private final Color originalBackground = Color.DARK_GRAY;

		public TabComponent(FComponent userComponent, FComponent tabComponent) {
			add(tabComponent);
			setPadding(new Padding(6, 4, 5, 4));
			this.userComponent = userComponent;
		}

		@Override
		protected void processMouseEvent(MouseEvent event) {
			super.processMouseEvent(event);
			if (!event.isConsumed() && event.getEventType() == MouseEvent.MOUSE_PRESSED)
				setSelectedIndex(headerPanel.getComponentIndex(this));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (selectedTab == this)
				g.setColor(hintColor);
			else
				g.setColor(originalBackground);
			int p = 3;
			// top hint
			g.fillRect(0, 0, getWidth(), hintSize);
			// border
			g.setColor(borderColor);
			// fill just for the first xd??
			// g.fillRect(0, size, borderSize, getHeight());
			g.fillRect(getWidth() - borderSize, hintSize, borderSize, getHeight());
		}

	}

	private class HeaderPanel extends FPanel {

		public HeaderPanel(FlowLayout flowLayout) {
			super(flowLayout);
		}

		@Override
		protected void paintComponents(Graphics g) {
			super.paintComponents(g);
			int selIndex = getSelectedIndex();
			if (selIndex < 0)
				return;
			var selected = getComponent(selIndex);
			int x = 0;
			g.setColor(borderColor);
			if (selIndex > 0) {
				g.fillRect(0, getHeight() - borderSize, selected.getX(), borderSize);
			}
			g.fillRect(selected.getX() + selected.getWidth(), getHeight() - borderSize, getWidth(), borderSize);
		}

	}

}
