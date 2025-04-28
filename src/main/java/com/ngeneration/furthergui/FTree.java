package com.ngeneration.furthergui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.ngeneration.furthergui.drag.DragInterface;
import com.ngeneration.furthergui.drag.DropEvent;
import com.ngeneration.furthergui.drag.Flavor;
import com.ngeneration.furthergui.drag.Transferable;
import com.ngeneration.furthergui.event.KeyStroke;
import com.ngeneration.furthergui.event.ListSelectionEvent;
import com.ngeneration.furthergui.event.ListSelectionListener;
import com.ngeneration.furthergui.event.MouseEvent;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.graphics.Graphics;
import com.ngeneration.furthergui.math.Dimension;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.furthergui.math.Point;
import com.ngeneration.furthergui.math.Rectangle;

import lombok.AllArgsConstructor;
import lombok.Data;

public class FTree extends FComponent implements DragInterface {

	private static TreeCellRenderer defaultCellRenderer = new DefaultTreeCellRenderer();

	private boolean showsRootHandles = true;
	private boolean rootVisible = true;
	private TreeNode rootNode;
	private final Set<ListSelectionListener> selectionListeners = new LinkedHashSet<>();
	private final Set<TreeNode> selection = new LinkedHashSet<>();
	private final Set<TreeNode> opened = new LinkedHashSet<>();
	private TreeCellRenderer treeCellRenderer;
	private TreeNode hoveredNode;
	private int lastHoverType = -1;
	private UI ui;

	private Color selectionColor = new Color(50, 60, 240);
	private Color hoverColor = new Color(70, 80, 250);

	private Dimension prefferedSize = null;

	private boolean dragEnabled;
	private boolean dropEnabled;
	private TreeNode dropTarget;
	private int dropRowInTarget;
	private int dropTargetY;
	private boolean dropInRowsEnabled;
	private Color dropColor = new Color(0, 0, 255, 50);

	private int dropBorder = 6;

	public int selectionMode = 2;

	public void setDropInRowsEnabled(boolean dropInRowsEnabled) {
		this.dropInRowsEnabled = dropInRowsEnabled;
	}

	public FTree(TreeNode rootNode) {
		this.rootNode = rootNode;
		setForeground(new Color(240, 240, 240));
		setCellRenderer(defaultCellRenderer);
		if (ui == null) {
			ui = new UI();
			ui.handleSize = getFont().getStringWidth("+");
		}
		setFocusable(true);
		getActionMap().put("left", ((e) -> {
			var node = getSelection().getLast();
			if (node != null) {
				if (isExpanded(node))
					collapse(node);
				else if (node.getParent() != null)
					setSelection(node.getParent());
			}
		}));
		getActionMap().put("right", ((e) -> {
			var node = getSelection().getLast();
			if (node != null) {
				if (isExpanded(node) && node.getChildCount() > 0)
					setSelection(node.getChildAt(0));
				else if (!node.isLeaf() && !isExpanded(node))
					expand(node);
			}
		}));
		getActionMap().put("up", ((e) -> {
			var node = getSelection().getLast();
			if (node != null) {
				var upper = getUpperNode(node);
				if (upper != null)
					setSelection(upper);
			}
		}));

		getActionMap().put("down", ((e) -> {
			var node = getSelection().getLast();
			if (node != null) {
				var upper = getLowerNode(node);
				if (upper != null)
					setSelection(upper);
			}
		}));
		getActionMap().put("selUp", ((e) -> {
			var node = getSelection().getLast();
			if (node != null) {
				var lower = getUpperNode(node);
				if (lower != null) {
					if (selection.contains(lower))
						removeFromSelection(node);
					else
						addToSelection(lower);
					repaint();
				}
			}
		}));
		getActionMap().put("selDown", ((e) -> {
			var node = getSelection().getLast();
			if (node != null) {
				var lower = getLowerNode(node);
				if (lower != null) {
					if (selection.contains(lower))
						removeFromSelection(node);
					else
						addToSelection(lower);
					repaint();
				}
			}
		}));
		getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("LEFT"), "left");
		getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("RIGHT"), "right");
		getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("UP"), "up");
		getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DOWN"), "down");
		getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SHIFT DOWN"), "selDown");
		getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SHIFT UP"), "selUp");
	}

	public Dimension getPrefferedSize() {
		var preffered = super.getPrefferedSize();
		if (preffered == null && prefferedSize == null)
			prefferedSize = calculatePrefferedSize(rootNode).add(getPadding().toDimension());
		return preffered != null ? preffered : new Dimension(prefferedSize.getWidth(), prefferedSize.getHeight());
	}

	private void expand(TreeNode node) {
		opened.add(node);
		revalidate();
	}

	private void collapse(TreeNode node) {
		opened.remove(node);
		revalidate();
	}

	@Override
	public void revalidate() {
		prefferedSize = null;
		super.revalidate();
	}

	public Color getSelectionColor() {
		return selectionColor;
	}

	public void setSelectionColor(Color selectionColor) {
		this.selectionColor = selectionColor;
	}

	public boolean addSelectionPath(TreePath treePath) {
		return addToSelection(treePath.getLastPathComponent());
	}

	public boolean addToSelection(TreeNode node) {
		return addToSelection(List.of(node));
	}

	public boolean addToSelection(Collection<TreeNode> nodes) {
		if (selection.addAll(nodes)) {
			fireSelectionListeners();
			return true;
		}
		return false;
	}

	public void removeFromSelection(TreeNode xx) {
		removeFromSelection(List.of(xx));
	}

	public void removeFromSelection(Collection<? extends TreeNode> of) {
		if (selection.removeAll(of))
			fireSelectionListeners();
	}

	public TreePath[] getSelectionPaths() {
		return selection.stream().map(s -> new TreePath(s.getPath())).toArray(size -> new TreePath[size]);
	}

	public List<? extends TreeNode> getSelection() {
		return new ArrayList<>(selection);
	}

	public void addSelectionListener(ListSelectionListener listener) {
		selectionListeners.add(listener);
	}

	public void removeSelectionListener(ListSelectionListener listener) {
		selectionListeners.remove(listener);
	}

	public void clearSelection() {
		setSelection(Set.of());
	}

	private void setSelection(TreeNode node) {
		setSelection(Set.of(node));
	}

	public int getSelectionCount() {
		return selection.size();
	}

	public void setSelection(Collection<? extends TreeNode> node) {
		var modified = getSelectionCount() != node.size() || new HashSet<TreeNode>(node).addAll(getSelection());
		if (modified) {
			selection.clear();
			selection.addAll(node);
			repaint();
			fireSelectionListeners();
		}
	}

	private void fireSelectionListeners() {
		ListSelectionEvent event = new ListSelectionEvent(this);
		selectionListeners.forEach(e -> e.valueChanged(event));
	}

	@Override
	protected void processMouseEvent(MouseEvent event) {
		if (!event.isConsumed()) {
			Point point = event.getLocation();
			if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
				var model = viewToModel2(event.getLocation());
				var newNode = model != null ? model.node : null;
				var newHoverType = newNode == null ? -1
						: ((model.handleContains(point) ? 1 : (model.componentContains(point) ? 2 : -1)));
				boolean repaint = (newNode != hoveredNode) || (lastHoverType != newHoverType);
				lastHoverType = newHoverType;
				hoveredNode = newNode;
				if (repaint)
					repaint();
			} else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
				var model = viewToModel2(point);
				if (model != null) {
					if (model.handleContains(point)) {
						if (opened.contains(model.node))
							collapse(model.node);
						else
							expand(model.node);
					} else if (selectionMode == 1) {
						if (!event.isControlDown() && !selection.contains(model.node)) {
							setSelection(model.node);
						}
					} else if (selectionMode == 2 && event.getButton() == MouseEvent.BUTTON2) {
						if (!event.isControlDown())
							setSelection(model.node);
					}
				}
			} else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
				var model = viewToModel2(point);
				if (model != null) {
					if (model.componentContains(point)) {
						if (event.isControlDown()) {
							if (!selection.contains(model.node))
								addToSelection(model.node);
							else
								removeFromSelection(model.node);
						} else {
							if (selectionMode == 2) {
								if (!selection.contains(model.node)) {
									setSelection(model.node);
								}
							} else
								setSelection(model.node);
						}
					}
				}
			}
		}
		super.processMouseEvent(event);
	}

	public TreeNode viewToModel(Point p) {
		return viewToModel(p.getX(), p.getY());
	}

	public TreeNode viewToModel(int x, int y) {
		var path = getPathForLocation(x, y);
		return path == null ? null : path.getLastPathComponent();
	}

	private Model viewToModel2(Point point) {
		var padding = getPadding();
		point = new Point().add(point).add(-padding.left, -padding.top);
		int[] data = new int[] { 0, 0, 0, 0 };
		return viewToModel(rootNode, point, 0, data);
	}

	private Dimension calculatePrefferedSize(TreeNode node) {
		int[] data = new int[] { 0, 0, 0, 0 };
		viewToModel(false, rootNode, new Point(Integer.MAX_VALUE, Integer.MAX_VALUE), 0, data, null);
		return new Dimension(data[2], data[3]).add(getPadding().toDimension());
	}

	private Model viewToModel(TreeNode node, Point point, int deep, int[] data) {
		return viewToModel(true, node, point, deep, data, null);
	}

	public Rectangle modelToView(TreeNode node) {
		int[] data = new int[] { 0, 0, 0, 0 };
		var x = viewToModel(false, rootNode, new Point(Integer.MAX_VALUE, Integer.MAX_VALUE), 0, data, node);
		return x == null ? null : new Rectangle(0, data[1], data[2], data[3] - data[1]);
	}

	/**
	 * 
	 * @param node
	 * @param point
	 * @param deep
	 * @param data  4 values{row, cury, maxwidth, height}
	 * @return
	 */
	private Model viewToModel(boolean checkbound, TreeNode node, Point point, int deep, int[] data, TreeNode target) {
		// checkbounds -- calculate preferred size true
		if (checkbound && !getBounds().contains(point))
			return null;
		// datas: row, curY, maxWidth, maxHeight
		if (node != rootNode || rootVisible) {
			FComponent component = treeCellRenderer.getTreeCellRendererComponent(this, node, selection.contains(node),
					opened.contains(node), node.isLeaf(), data[1], node == hoveredNode && lastHoverType == 2);
			var dim = component.getPrefferedSize();
			component.setBounds(0, 0, dim.width, dim.height);
			int offset = deep * ui.getDeepSize() + ui.getHandleSize() + ui.getPadding() * 2;
			data[2] = Math.max(data[2], dim.width + offset);
			data[3] += dim.height;
			if (point.getY() >= data[1] && point.getY() <= data[1] + component.getHeight())
				return new Model(node, component, deep, data[0]);

			if (node == target)
				return new Model(node, component, deep, data[0]);

			data[1] += component.getHeight();
			data[0]++;
		}
		if (node.allowsChildren() && !node.isLeaf() && node.getChildCount() > 0 && opened.contains(node)) {
			for (TreeNode child : node.getChildren()) {
				var model = viewToModel(checkbound, child, point, deep + 1, data, target);
				if (model != null)
					return model;
			}
		}
		return null;
	}

	@Override
	protected void paintComponent(Graphics g) {
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

	@Override
	protected void paintComponents(Graphics g) {
		super.paintComponents(g);
		if (dropTarget != null) {
			g.setColor(dropColor);
			g.setPenSize(2);
			int initialY = getPadding().top;
			int dropHeight = this.dropBorder;
			if (dropRowInTarget == -1) {
				FComponent component = treeCellRenderer.getTreeCellRendererComponent(this, dropTarget,
						selection.contains(dropTarget), opened.contains(dropTarget), dropTarget.isLeaf(), 1,
						dropTarget == hoveredNode && lastHoverType == 2);
				dropHeight = component.getPrefferedSize().height;
			} else {
				initialY -= dropHeight;
				dropHeight *= 2;
			}
			g.fillRect(0, initialY + dropTargetY, getWidth(), dropHeight);
		}
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
			g.setFont(getFont());
			g.drawString(0, (height - g.getFont().getStringHeight(expanded ? "-" : "+")) * 0.5f, expanded ? "-" : "+");
			g.flush();
		}

		y[0] += height;

		int[] row2 = new int[] { row + 1 };
		if (node.allowsChildren() && node.getChildCount() > 0 && opened.contains(node)) {
			node.getChildren().forEach(child -> row2[0] += drawNode(g, child, x, y, row2[0], deep + 1));
		}
		return row2[0] - row;
	}

	public TreePath getPathForLocation(int x, int y) {
		var model = viewToModel2(new Point(x, y));
		return model == null ? null : new TreePath(model.node.getPath());
	}

	public TreeNode getRootNode() {
		return rootNode;
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

	public void scrollPathToVisible(TreePath treePath) {
		var data = modelToView(treePath.getLastPathComponent());
		makeVisible(data);
	}

	/**
	 * Expand the end of the path
	 * 
	 * @param treePath
	 */
	public void expandPath(TreePath treePath) {
		var node = treePath.getLastPathComponent();
		if (!opened.contains(node))
			opened.add(node);
	}

	public void reload(TreeNode node) {
		throw new RuntimeException();
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

	public Color getHoverColor() {
		return hoverColor;
	}

	public void setHoverColor(Color hoverColor) {
		this.hoverColor = hoverColor;
	}

	public void setDragAndDropEnabled(boolean drag, boolean drop) {
		this.dragEnabled = drag;
		this.dropEnabled = drop;
	}

	public boolean isDragEnabled() {
		return dragEnabled;
	}

	public boolean isDropEnabled() {
		return dropEnabled;
	}

	public static class TreeSelectioFlavor implements Flavor {

		private List<? extends TreeNode> nodes;
		private FTree tree;

		public TreeSelectioFlavor(FTree tree, List<? extends TreeNode> nodes) {
			this.tree = tree;
			this.nodes = nodes;
		}

		public FTree getValue() {
			return tree;
		}

		public List<? extends TreeNode> getNodes() {
			return nodes;
		}

	}

	protected Transferable getTransferable(DropEvent event) {
		var selection = getSelection();
		List<? extends TreeNode> list = null;
		if (selectionMode == 2) {
			var model = viewToModel2(event.getLocation());
			if (model != null && model.componentContains(event.getLocation())) {
				list = List.of(model.node);
			}
		} else if (!selection.isEmpty() && !selection.contains(rootNode)) {
			list = getSelection();
		}

		final var lista2 = list;
		return list == null ? null : new Transferable() {

			@Override
			public boolean isFlavorSupported(Class<? extends Flavor> flavor) {
				return TreeSelectioFlavor.class.isAssignableFrom(flavor);
			}

			@Override
			public <T extends Flavor> T getFlavor(Class<T> objectFlavor) {
				return objectFlavor.cast(new TreeSelectioFlavor(FTree.this, lista2));
			}
		};
	}

	@Override
	public void onDrag(DropEvent event) {
		if (dragEnabled) {
			if (event.getEventType() == DropEvent.ACCEPTED_EVENT) {
				removeFromSelection(event.getTransferable().getFlavor(TreeSelectioFlavor.class).getNodes());
			} else {
				var transferable = getTransferable(event);
				if (transferable != null)
					event.acept(transferable, DropEvent.ANY_MODE);
			}
		}
	}

	@Override
	public void onDrop(DropEvent event) {
		if (isDropEnabled() && event.getEventType() == DropEvent.TEST_EVENT
				&& event.getTransferable().isFlavorSupported(TreeSelectioFlavor.class)) {
			int[] data = new int[4];
			var model = viewToModel(rootNode, event.getLocation(), 0, data);
			if (model != null) {
				int nodeY = data[1];
				int totalHeight = data[3];

				var flavor = event.getTransferable().getFlavor(TreeSelectioFlavor.class);
				var selection = getRootSelection(flavor.getNodes());

				// find drop target
				dropTarget = model.node;
				dropRowInTarget = -1;
				dropTargetY = nodeY;
				if (dropInRowsEnabled && dropTarget.getParent() != null) {
					int y = event.getLocation().getY() - getPadding().top;
					if (y - nodeY < dropBorder) {
						dropTarget = dropTarget.getParent();
						dropRowInTarget = dropTarget.getChildren().indexOf(model.node);
					} else if (y > totalHeight - dropBorder) {
						if (isExpanded(dropTarget)) {
							dropRowInTarget = 0;
						} else {
							dropTarget = dropTarget.getParent();
							dropRowInTarget = dropTarget.getChildren().indexOf(model.node) + 1;
						}
						dropTargetY = totalHeight;
					}
				}

//				if ()

				// filter target from dragged nodes
				var targetInSelection = selection.stream().anyMatch(selected -> {
					var parent = dropTarget;
					for (int i = 0; i < 2 && parent != null; i++) {
						while (parent != null) {
							if (parent == selected)
								return true;
							parent = parent.getParent();
						}
						parent = parent != model.node ? model.node : null;
					}
					return false;
				});
				if (targetInSelection) {
					dropTarget = null;
				}

				if (dropTarget != null) {
					if (!testDropOnTarget(dropTarget, toAfterIndex(dropRowInTarget, dropTarget, selection), event)) {
						dropTarget = null;
						dropRowInTarget = -1;
						repaint();
					} else {
						event.acept(DropEvent.ANY_MODE);
					}
				}
			} else {
				boolean changed = dropTarget != null;
				dropTarget = null;
				dropRowInTarget = getDefaultDropTarget() == null ? -1 : getDefaultDropTarget().getChildCount();
				if (testDropOnTarget(getDefaultDropTarget(), dropRowInTarget, event)) {
					changed = true;
					event.acept(DropEvent.ANY_MODE);
				}
				if (changed)
					repaint();
			}
		} else if (event.getEventType() == DropEvent.CANCEL_EVENT) {
			dropTarget = null;
		} else if (event.getEventType() == DropEvent.ACCEPTED_EVENT) {
			int idx = dropRowInTarget;
			if (dropTarget != null && event.getTransferable().isFlavorSupported(TreeSelectioFlavor.class)) {
				var flavor = event.getTransferable().getFlavor(TreeSelectioFlavor.class);
				var selection = getRootSelection(flavor.getNodes());
				idx = toAfterIndex(dropRowInTarget, dropTarget, selection);
			}
			onDropAccepted(dropTarget, idx, event);
			dropTarget = null;
		}
	}

	protected TreeNode getDefaultDropTarget() {
		return null;
	}

	private int toAfterIndex(int index, TreeNode dropTarget, List<? extends TreeNode> nodes) {
		if (index > 0) {
			var children = dropTarget.getChildren();
			int max = index;
			int i = 0;
			for (Iterator<TreeNode> iterator = children.iterator(); iterator.hasNext() && i < max; i++) {
				TreeNode treeNode = (TreeNode) iterator.next();
				if (nodes.contains(treeNode))
					index--;
			}
		} else {
			index = (int) (dropTarget.getChildCount()
					- dropTarget.getChildren().stream().filter(nodes::contains).count());
		}
		return index;
	}

	private List<? extends TreeNode> getRootSelection(List<? extends TreeNode> selection) {
		var copy = new ArrayList<>(selection);
		return selection.stream().filter(selected -> {
			var parent = selected.getParent();
			while (parent != null) {
				if (copy.contains(parent))
					return false;
				parent = parent.getParent();
			}
			return true;
		}).toList();
	}

	public boolean isExpanded(TreeNode node) {
		return opened.contains(node);
	}

	private TreeNode getUpperNode(TreeNode node) {
		var parent = node.getParent();
		if (parent != null) {
			int idx = parent.getChildren().indexOf(node);
			if (idx > 0) {
				var top = parent.getChildAt(idx - 1);
				while (!top.isLeaf() && top.getChildCount() > 0 && isExpanded(top))
					top = top.getChildAt(top.getChildCount() - 1);
				parent = top;
			}
		}
		return parent;
	}

	private TreeNode getLowerNode(TreeNode node) {
		if (!node.isLeaf() && isExpanded(node) && node.getChildCount() > 0)
			return node.getChildAt(0);
		while (node.getParent() != null) {
			var parent = node.getParent();
			int idx = parent.getChildren().indexOf(node);
			if (idx < parent.getChildCount() - 1)
				return parent.getChildAt(idx + 1);
			else
				node = node.getParent();
		}
		return null;
	}

	protected void onDropAccepted(TreeNode target, int targetRow, DropEvent event) {
		if (testDrop(target, targetRow, event)) {
			var flavor = event.getTransferable().getFlavor(TreeSelectioFlavor.class);
			flavor.getValue().getSelection().forEach(selected -> {
				var parent = (DefaultMutableTreeNode) selected.getParent();
				parent.remove((DefaultMutableTreeNode) selected);

				((DefaultMutableTreeNode) target).add(targetRow, selected);
				addToSelection(selected);
			});
		}
	}

	protected boolean testDropOnTarget(TreeNode target, int index, DropEvent event) {
		return testDrop(target, index, event);
	}

	private boolean testDrop(TreeNode target, int index, DropEvent event) {
		var flavor = event.getTransferable().isFlavorSupported(TreeSelectioFlavor.class);
		return target != null && flavor && event.getTransferable().getFlavor(TreeSelectioFlavor.class).getValue()
				.getSelection().stream().allMatch(DefaultMutableTreeNode.class::isInstance);
	}

}
