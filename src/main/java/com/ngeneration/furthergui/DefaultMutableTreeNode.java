package com.ngeneration.furthergui;

import java.util.LinkedList;
import java.util.List;

public class DefaultMutableTreeNode implements TreeNode {

	private Object userObject;
	private TreeNode parent;
	private List<TreeNode> children = new LinkedList<>();

	public DefaultMutableTreeNode() {
		this(null);
	}

	public DefaultMutableTreeNode(Object object) {
		this.userObject = object;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void add(int index, TreeNode underLayingNode) {
		if (underLayingNode.getParent() != null)
			throw new RuntimeException("Node already has a parent: " + underLayingNode);
		if (underLayingNode instanceof DefaultMutableTreeNode)
			((DefaultMutableTreeNode) underLayingNode).parent = this;
		children.add(index, underLayingNode);
	}

	public TreeNode getParent() {
		return parent;
	}

	public void setUserObject(Object object) {
		this.userObject = object;
	}

	public void add(TreeNode underLayingNode) {
		add(getChildCount(), underLayingNode);
	}

	@Override
	public boolean isLeaf() {
		return children.isEmpty();
	}

	@Override
	public int getChildCount() {
		return children.size();
	}

	@Override
	public TreeNode getChildAt(int i) {
		return children.get(i);
	}

	@Override
	public List<TreeNode> getChildren() {
		return new LinkedList<>(children);
	}

	public void clear() {
		children.forEach(c -> {
			if (c instanceof DefaultMutableTreeNode)
				((DefaultMutableTreeNode) c).parent = null;
		});
		children.clear();
	}

	@Override
	public boolean allowsChildren() {
		return true;
	}

	@Override
	public String toString() {
		return String.valueOf(userObject);
	}

	public void remove(DefaultMutableTreeNode node) {
		if (node.getParent() != this)
			throw new RuntimeException("invalid state");
		node.parent = null;
		children.remove(node);
	}

}
