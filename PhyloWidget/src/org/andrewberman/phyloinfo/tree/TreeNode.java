package org.andrewberman.phyloinfo.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import org.andrewberman.phyloinfo.PhyloWidget;

public class TreeNode implements Comparable
{

	public static TreeNode NULL_PARENT = null;
	private static int serialNumber = 0;
	protected ArrayList children;
	protected TreeNode parent;
	protected String name;
	protected String serial;

	public TreeNode()
	{
		parent = NULL_PARENT;

		init();
	}

	public TreeNode(TreeNode rent)
	{
		parent = rent;
		if (parent != NULL_PARENT)
		{
			parent.addChild(this);
		}

		init();
	}

	public TreeNode(String s) {
		parent = NULL_PARENT;
		name = s;
		
		init();
	}
	
	private void init()
	{
		children = new ArrayList();
		serial = String.valueOf(serialNumber++);
	}

	public void addChild(TreeNode child) {
		children.add(child);
		child.setParent(this);
	}
	
	public void addChildren(Collection children) {
		Iterator it = children.iterator();
		while (it.hasNext())
		{
			TreeNode child = (TreeNode) it.next();
			addChild(child);
		}
	}
	
	public void removeChild(TreeNode child) {
		children.remove(child);
		if (child.parent == this)
			child.setParent(TreeNode.NULL_PARENT);
	}
	
	public void sortChildren() {
		// Sort the children ArrayList by the number of descendants.
		Collections.sort(children);
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

	public ArrayList getChildren()
	{
		return children;
	}

	public ArrayList getAllDescendants()
	{
		if (isLeaf())
		{
			return new ArrayList();
		}
		ArrayList desc = (ArrayList) children.clone();
		for (int i = 0; i < children.size(); i++)
		{
			TreeNode child = (TreeNode) children.get(i);
			desc.addAll(child.getAllDescendants());
		}
		return desc;
	}

	public ArrayList getAllNodes()
	{
		ArrayList desc = getAllDescendants();
		desc.add(this);
		return desc;
	}

	public TreeNode getParent()
	{
		return parent;
	}
	
	public void setParent(TreeNode p) {
		parent = p;
	}

	public ArrayList getAllLeaves()
	{
		ArrayList leaves = new ArrayList();
		Stack stack = new Stack();
		stack.push(this);
		while (!stack.isEmpty())
		{
			TreeNode curNode = (TreeNode) stack.pop();
			if (curNode.isLeaf())
			{
				leaves.add(curNode);
			} else
			{
				stack.addAll(curNode.getChildren());
			}
		}
		return leaves;
	}

	public int getMaxHeight()
	{
		if (children.size() == 0)
		{
			return 0;
		} else
		{
			int maxHeight = 0;
			for (int i = 0; i < children.size(); i++)
			{
				TreeNode n = (TreeNode) children.get(i);
				int height = n.getMaxHeight();
				if (height > maxHeight)
					maxHeight = height;
			}
			return maxHeight + 1;
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

	public String getSerial()
	{
		return serial;
	}
	
	public boolean equals(Object o) {
		if (o instanceof TreeNode) {
			TreeNode b = (TreeNode)o;
			if (this.getSerial().equals(b.getSerial())) return true;
		}
		return false;
	}
	
	public int compareTo(Object o) {
		if (o == null) throw new NullPointerException();
		TreeNode b = (TreeNode)o;
		int mySize = this.getAllDescendants().size();
		int hisSize = b.getAllDescendants().size();
		if (mySize > hisSize)
			return -1;
		else if (mySize < hisSize)
			return 1;
		else
		{
			return 0;
		}
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
