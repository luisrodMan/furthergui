package com.ngeneration.furthergui;

public class TreePath {

	private TreeNode[] path;

	public TreePath(TreeNode[] path) {
		this.path = path;
	}

	public TreeNode getLastPathComponent() {
		return getEnd();
	}

	@Deprecated
	public TreeNode getEnd() {
		return path[path.length - 1];
	}

}
