package org.phylowidget.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Tree
{

	private TreeNode root;

	public boolean editable = true;
	public int modCount = -1;

	public Tree()
	{
		this.root = new TreeNode("");
	}

	public Tree(String s)
	{
		this.root = new TreeNode(s);
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
		modCount++;
	}

	public void sortAllChildren()
	{
		ArrayList all = new ArrayList();
		root.getAll(null,all);
		for (int i = 0; i < all.size(); i++)
		{
			TreeNode n = (TreeNode) all.get(i);
			n.sortChildren();
		}
	}

	public void getAll(ArrayList leaves, ArrayList nodes)
	{
		root.getAll(leaves,nodes);
	}
	
	public void getAllNodes(ArrayList nodes)
	{
		root.getAll(null,nodes);
	}

	public void getAllLeaves(ArrayList leaves)
	{
		root.getAll(leaves,null);
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
