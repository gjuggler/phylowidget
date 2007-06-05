package org.andrewberman.phyloinfo.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Tree
{

	private TreeNode root;
	
	public Tree(TreeNode root) {
		this.root = root;
	}
	
	public Tree() {
		this.root = new TreeNode();
	}
	
	public void addSisterNode(TreeNode orig,TreeNode sis) {
		// Special case: if this is the root node.
		if (orig.parent == TreeNode.NULL_PARENT)
		{
			TreeNode newParent = new TreeNode();
			newParent.addChild(orig);
			newParent.addChild(sis);
			this.root = newParent;
		} else
		{
			// Adds a sister node by creating a new "inner" parent node.
			TreeNode parent = orig.parent;
			parent.removeChild(orig);
			TreeNode newBranch = new TreeNode(parent);
			newBranch.addChild(orig);
			newBranch.addChild(sis);
		}
	}
	
	public void sortAllChildren() {
		ArrayList all = root.getAllNodes();
		for (int i=0; i < all.size(); i++)
		{
			TreeNode n = (TreeNode) all.get(i);
			n.sortChildren();
		}
	}
	
	public ArrayList getAllNodes() {
		return root.getAllNodes();
	}
	
	public TreeNode getRoot() {
		return root;
	}
	
	public void setRoot(TreeNode t) {
		root = t;
	}
}
