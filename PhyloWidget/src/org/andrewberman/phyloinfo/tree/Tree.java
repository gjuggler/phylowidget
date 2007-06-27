package org.andrewberman.phyloinfo.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Tree
{

	private TreeNode root;

	public boolean needsUpdating = true;

	public Tree(TreeNode root)
	{
		this.root = root;
	}

	public Tree()
	{
		this.root = new TreeNode("");
	}

	public void addSisterNode(TreeNode orig, TreeNode sis)
	{
		if (orig.parent == TreeNode.NULL_PARENT)
		{
			// Special case: if this is the root node.
			root = new TreeNode("");
			root.addChild(orig);
			root.addChild(sis);
		} else
		{
			// Adds a sister node by creating a new "inner" parent node.
			TreeNode parent = orig.parent;
			parent.removeChild(orig);
			TreeNode newBranch = new TreeNode("");
			newBranch.addChild(orig);
			newBranch.addChild(sis);
			parent.addChild(newBranch);
		}
		needsUpdating = true;
	}

	public void sortAllChildren()
	{
		ArrayList all = new ArrayList();
		root.getAllNodes(all);
		for (int i = 0; i < all.size(); i++)
		{
			TreeNode n = (TreeNode) all.get(i);
			n.sortChildren();
		}
	}

	public void getAllNodes(ArrayList nodes)
	{
		root.getAllNodes(nodes);
	}

	public void getAllLeaves(ArrayList leaves)
	{
		root.getAllLeaves(leaves);
	}

	public TreeNode getRoot()
	{
		return root;
	}

	public void setRoot(TreeNode t)
	{
		root = t;
	}
}
