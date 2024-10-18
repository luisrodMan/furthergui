package com.ngeneration.furthergui;

import java.util.LinkedList;
import java.util.List;

import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.furthergui.math.Point;

import lombok.AllArgsConstructor;
import lombok.Data;

public class FTree extends FComponent {

	private static TreeCellRenderer defaultCellRenderer = new DefaultTreeCellRenderer();

	private boolean showsRootHandles = true;
	private boolean rootVisible = true;
	private TreeNode rootNode;
	private final List<TreeNode> selection = new LinkedList<>();
	private final List<TreeNode> opened = new LinkedList<>();
	private TreeCellRenderer treeCellRenderer;
	private TreeNode hoveredNode;
	private int lastHoverType = -1;
	private UI ui;

	private Color selectionColor = new Color(50, 60, 240);
	private Color hoverColor = new Color(70, 80, 250);

	public FTree(TreeNode rootNode) {
		this.rootNode = rootNode;
		setCellRenderer(defaultCellRenderer);
	}

	@Override
	protected void processMouseEvent(MouseEvent event) {
		super.processMouseEvent(event);
		if (!event.isConsumed()) {
			Point point = event.getLocation();
			if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
				var model = viewToModel(event.getLocation());
				var newNode = model != null ? model.node : null;
				var newHoverType = newNode == null ? -1
						: ((model.handleContains(point) ? 1 : (model.componentContains(point) ? 2 : -1)));
				boolean repaint = (newNode != hoveredNode) || (lastHoverType != newHoverType);
				lastHoverType = newHoverType;
				hoveredNode = newNode;
				if (repaint)
					repaint();
			} else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
				var model = viewToModel(point);
				if (model != null) {
					if (model.handleContains(point)) {
						if (opened.contains(model.node))
							opened.remove(model.node);
						else
							opened.add(model.node);
						repaint();
					} else if (model.componentContains(point)) {
						if (!selection.contains(model.node)) {
							clearSelection();
							selection.add(model.node);
						}
						repaint();
					}

				}
			}
		}
	}

	private Model viewToModel(Point point) {
		Padding padding = getPadding();
		int[] data = new int[] { padding.top, 0 };
		return viewToModel(rootNode, point, 0, data);
	}

	private Model viewToModel(TreeNode node, Point point, int deep, int[] data) {
		if (node != rootNode || rootVisible) {
			FComponent component = treeCellRenderer.getTreeCellRendererComponent(this, node, selection.contains(node),
					opened.contains(node), node.isLeaf(), data[1], node == hoveredNode && lastHoverType == 2);
			var dim = component.getPrefferedSize();
			component.setBounds(0, 0, dim.width, dim.height);
			if (point.getY() >= data[0] && point.getY() <= data[0] + component.getHeight())
				return new Model(node, component, deep, data[1]);
			data[0] += component.getHeight();
			data[1]++;
		}
		if (node.allowsChildren() && !node.isLeaf() && node.getChildCount() > 0 && opened.contains(node)) {
			for (TreeNode child : node.getChildren()) {
				var model = viewToModel(child, point, deep + 1, data);
				if (model != null)
					return model;
			}
		}
		return null;
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (ui == null) {
			ui = new UI();
			ui.handleSize = g.getFont().getStringWidth("+");
		}
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), g.getHeight());

		Padding padding = getPadding();
		int[] x = new int[] { g.getTranslationX() + padding.left };
		int[] y = new int[] { g.getTranslationY() + padding.top };
		int[] row = new int[1];

		if (rootNode == null)
			return;
		if (rootVisible)
			drawNode(g, rootNode, x, y, row[0], 0);
		else if (rootNode.allowsChildren())
			rootNode.getChildren().forEach(n -> row[0] += drawNode(g, n, x, y, row[0], 0));
	}

	private int drawNode(Graphics g, TreeNode node, int[] x, int[] y, int row, int deep) {
		int DEEP_SIZE = ui.getDeepSize();
		int HANDLE_SIZE = !showsRootHandles ? 0 : ui.getHandleSize();
		int HANDLE_PADDING = HANDLE_SIZE == 0 ? 0 : ui.getPadding();
		Padding padding = getPadding();

		boolean hasHandle = showsRootHandles && node.allowsChildren() && node.getChildCount() > 0;
		boolean expanded = hasHandle && opened.contains(node);

		FComponent component = treeCellRenderer.getTreeCellRendererComponent(this, node, selection.contains(node),
				expanded, node.isLeaf(), row, node == hoveredNode && lastHoverType == 2);
		int height = component.getPrefferedSize().height;
		var scissor = g.getScissor();
		g.setTranslation(x[0] + deep * DEEP_SIZE + HANDLE_SIZE + HANDLE_PADDING * 2, y[0]);
		component.setBounds(g.getTranslationX(), g.getTranslationY(),
				component.getPrefferedSize().width - padding.right, height);
		component.validate();
		component.paint(g);

		if (hasHandle) {
			g.setScissor(scissor);
			g.setColor(Color.RED);
			g.flush();
			g.setTranslation(g.getTranslationX() - HANDLE_SIZE - HANDLE_PADDING, y[0]);
			g.drawString(0, (height - g.getFont().getFontHeight()) * 0.5f, expanded ? "-" : "+");
			g.flush();
		}

		y[0] += height;

		int[] row2 = new int[] { row + 1 };
		if (node.allowsChildren() && node.getChildCount() > 0 && opened.contains(node)) {
			node.getChildren().forEach(child -> row2[0] += drawNode(g, child, x, y, row2[0], deep + 1));
		}

		return row2[0] - row;
	}

	public TreeNode viewToModel(int x, int y) {
		var path = getPathForLocation(x, y);
		return path == null ? null : path.getLastPathComponent();
	}

	public TreePath getPathForLocation(int x, int y) {
		var model = viewToModel(new Point(x, y));
		return model == null ? null : new TreePath(model.node.getPath());
	}

	public void setRooVisible(boolean b) {
		rootVisible = b;
	}

	public void setCellRenderer(TreeCellRenderer treeCellRenderer) {
		if (treeCellRenderer == null)
			treeCellRenderer = defaultCellRenderer;
		this.treeCellRenderer = treeCellRenderer;
	}

	public TreeCellRenderer getCellRenderer() {
		return treeCellRenderer;
	}

	public void clearSelection() {
		if (selection.isEmpty())
			return;
		selection.clear();
//		fireListeners(SELECTION_LISTENER, new Event(), (l, e) -> l.fireListener(l));
	}

	public void scrollPathToVisible(TreePath treePath) {

	}

	public void expandPath(TreePath treePath) {

	}

	public void addSelectionPath(TreePath treePath) {

	}

	public TreePath[] getSelectionPaths() {
		return selection.stream().map(s -> new TreePath(s.getPath())).toArray(size -> new TreePath[size]);
	}

	public void reload(TreeNode node) {

	}

	public void setShowsRootHandles(boolean b) {
		showsRootHandles = b;
	}

	@Data
	@AllArgsConstructor
	private class Model {

		private TreeNode node;
		private FComponent component;
		private int deep;
		private int row;

		public int getHandleStart() {
			return getPadding().left + deep * ui.getDeepSize();
		}

		public int getHandleEnd() {
			return getHandleStart() + ui.getPadding() * 2 + ui.getHandleSize();
		}

		public boolean componentContains(Point point) {
			var end = getHandleEnd();
			return point.getX() >= end && point.getX() <= end + component.getWidth();
		}

		public boolean handleContains(Point point) {
			return showsRootHandles && node.allowsChildren() && !node.isLeaf() && point.getX() >= getHandleStart()
					&& point.getX() <= getHandleEnd();
		}

	}

	@Data
	private class UI {
		private int deepSize = 20;
		private int handleSize = 0;
		private int padding = 6;
	}

	public Color getSelectionColor() {
		return selectionColor;
	}

	public void setSelectionColor(Color selectionColor) {
		this.selectionColor = selectionColor;
	}

	public Color getHoverColor() {
		return hoverColor;
	}

	public void setHoverColor(Color hoverColor) {
		this.hoverColor = hoverColor;
	}

}
