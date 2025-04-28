package com.ngeneration.furthergui;

import java.util.List;

public interface TreeNode {

	default TreeNode[] getPath() {
		TreeNode parent = this;
		int count = 0;
		while (parent != null) {
			parent = parent.getParent();
			count++;
		}
		var path = new TreeNode[count];
		parent = this;
		while (parent != null) {
			path[--count] = parent;
			parent = parent.getParent();
		}
		return path;
	}

	TreeNode getParent();

	default boolean isLeaf() {
		return getChildCount() == 0;
	}

	int getChildCount();

	TreeNode getChildAt(int i);

	List<TreeNode> getChildren();

	boolean allowsChildren();

}
