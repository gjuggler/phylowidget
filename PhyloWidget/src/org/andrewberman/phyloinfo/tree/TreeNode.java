package org.andrewberman.phyloinfo.tree;

import java.util.ArrayList;
import java.util.Stack;

public class TreeNode
{

	public static TreeNode NULL_PARENT = null;

	public ArrayList children;

	public TreeNode parent;

	private String name;

	public TreeNode()
	{
		parent = NULL_PARENT;
		children = new ArrayList();
	}

	public TreeNode(TreeNode rent)
	{
		parent = rent;
		if (parent != NULL_PARENT)
		{
			parent.addChild(this);
		}
		children = new ArrayList();
	}

	public void addChild(TreeNode child)
	{
		children.add(child);
	}

	public boolean isLeaf()
	{
		if (children.size() == 0)
		{
			return true;
		} else
		{
			return false;
		}
	}

	public void setName(String s)
	{
		name = s;
	}
	public String getName()
	{
		return this.toString();
	}

	public String toString()
	{
		if (name == null)
		{
			return "[Unnamed node]";
		} else
		{
			return name;
		}
	}

}
