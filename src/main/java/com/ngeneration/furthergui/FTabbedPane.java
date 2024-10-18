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

import lombok.Data;

public class FTabbedPane extends FPanel {

	public static final int BOTTOM = 2;

	private FPanel headerPanel = new FPanel(new FlowLayout(FlowLayout.LEFT, 1));
	private FPanel containerPanel = new FPanel(new OneVisibleLayout());
	private TabComponent selectedTab;

	private List<ChangeListener> changeListeners = new ArrayList<>(1);

	public FTabbedPane() {
		super(new BorderLayout());
		containerPanel.setPadding(new Padding());
		add(headerPanel, BorderLayout.NORTH);
		add(containerPanel, BorderLayout.CENTER);
//		headerPanel.setBackground(new Color(50, 50, 40));
		headerPanel.setPadding(new Padding(1));
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
		if (getTabCount() == 1) {
			setSelectedIndex(0);
		}
		fireChangeListener();
	}

	public FComponent getSelectedComponent() {
		return selectedTab == null ? null : selectedTab.userComponent;
	}

	public FComponent getComponentAt(int i) {
		validateIndex(i);
		return ((TabComponent) headerPanel.getComponent(i)).getUserComponent();
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
		if (getTabCount() > 0) {
			selectedTab = (TabComponent) headerPanel.getComponent(getSelectedIndex());
			((OneVisibleLayout) containerPanel.getLayout()).setVisible(containerPanel, selectedTab.getUserComponent());
		}
	}

	public void setSelectedIndex(int index) {
		validateIndex(index);
		var cur = getSelectedIndex();
		if (cur == index)
			return;
		selectedTab = (TabComponent) headerPanel.getComponent(index);
		((OneVisibleLayout) containerPanel.getLayout()).setVisible(containerPanel, selectedTab.getUserComponent());
		fireChangeListener();
	}

	public void removeTab(int viewIndex) {
		validateIndex(viewIndex);
		removeTab(getComponentAt(viewIndex));
	}

	public void removeTab(FComponent component) {
		TabComponent found = null;
		for (var f : headerPanel.getComponents()) {
			if (((TabComponent) f).getUserComponent() == component) {
				found = (TabComponent) f;
				break;
			}
		}
		if (found == null)
			throw new RuntimeException("Component not added: " + component);
		headerPanel.remove(found);
		containerPanel.remove(found.userComponent);
		if (getTabCount() == 0)
			selectedTab = null;
		fireChangeListener();
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
		return ((TabComponent) headerPanel.getComponent(i)).getTabComponent();
	}

	public int getSelectedIndex() {
		return headerPanel == null ? -1 : headerPanel.getComponentIndex(selectedTab);
	}

	public void setTabPlacement(int placement) {

	}

	@Data
	private class TabComponent extends FPanel {

		private FComponent userComponent;
		private FComponent tabComponent;
		private final Color selectedBackground = Color.MAGENTA;
		private final Color originalBackground = Color.DARK_GRAY;

		public TabComponent(FComponent userComponent, FComponent tabComponent) {
			add(tabComponent);
			setPadding(new Padding(4));
			this.userComponent = userComponent;
		}

		@Override
		protected void processMouseEvent(MouseEvent event) {
			super.processMouseEvent(event);
			if (!event.isConsumed() && event.getEventType() == MouseEvent.MOUSE_PRESSED) {
				setSelectedIndex(headerPanel.getComponentIndex(this));
				if (isComponentOnScreen())
					FTabbedPane.this.repaint();
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (selectedTab == this)
				g.setColor(selectedBackground);
			else
				g.setColor(originalBackground);
			int size = 1;
			int p = 3;
			g.fillRect(p, getHeight() - size, getWidth() - p * 2, size);
		}

	}

}
