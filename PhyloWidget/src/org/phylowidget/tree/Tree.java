package org.phylowidget.tree;

import java.util.ArrayList;

public class Tree
{
	private TreeNode root;

	private NodeFactory factory;
	
	public boolean editable = true;
	public int modCount = -1;

	public Tree(NodeFactory f)
	{
		this.factory = f;
		this.root = factory.createNode();
	}

	public Tree(NodeFactory f, String s)
	{
		this(f);
		root.name = s;
	}
	
	public void addSisterNode(TreeNode orig, TreeNode sis)
	{
		if (orig.parent == TreeNode.NULL_PARENT)
		{
			// Special case: if this is the root node.
			root = factory.createNode();
			root.addChild(orig);
			root.addChild(sis);
		} else
		{
			// Adds a sister node by creating a new "inner" parent node.
			TreeNode parent = orig.parent;
			parent.removeChild(orig);
			TreeNode newBranch = factory.createNode();
			newBranch.addChild(orig);
			newBranch.addChild(sis);
			parent.addChild(newBranch);
		}
		modCount++;
	}
	
	public void deleteNode(TreeNode node)
	{
		if (node.parent == TreeNode.NULL_PARENT)
			return;
		
		TreeNode parent = node.parent;
		parent.removeChild(node);
		for (int i=0; i < node.children.size(); i++)
		{
			TreeNode n = (TreeNode)node.children.get(i);
			parent.addChild(n);
		}
		parent.sortChildren();
		pruneTree();
		modCount++;
	}

	public void pruneTree()
	{
		root.prune();
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
	
	public NodeFactory getFactory()
	{
		return factory;
	}
	
	/*
	 * We shouldn't be allowed to switch node factories during a tree's existence.
	 */
//	public void setFactory(NodeFactory fac)
//	{
//		System.out.println("Setting factory!");
//		this.factory = fac;
//	}
}
