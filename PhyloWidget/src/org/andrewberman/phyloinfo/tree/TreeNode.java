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

	private static int serialSeed = 0;
	
	protected String name;
	public int serial;
	
	/*
	 * Bread-and-butter fields for the TreeNode class.
	 */
	public static final TreeNode NULL_PARENT = null;
	protected TreeNode parent = NULL_PARENT;
	protected ArrayList children = new ArrayList(2);
	protected float height = 0; // Height to the parent node.
	
	/*
	 * Cached values. Each of these needs to be percolated upwards when a part
	 * of the tree below this node is altered.
	 */
	protected int numLeaves=1;
	protected int numDescendants=1;
	protected int maxDepth=1;
	protected float maxHeight=0;
	
	/*
	 * Static integers for the switch statement in percolate().
	 */
	public static final int NUM_LEAVES=0;
	public static final int NUM_DESCENDANTS=1;
	public static final int MAX_DEPTH=2;
	
	/*
	 * TODO: Implement a "percolate" method that allows us to percolate up, from leaf to root,
	 * any important variable state updates. This will be an easy way to manage "cached" values,
	 * such as numLeaves, maxHeight, and numDescendants.
	 */
	
	private TreeNode()
	{
		serial = serialSeed++;
	}

	public TreeNode(TreeNode rent)
	{
		this();
		parent = rent;
	}

	public TreeNode(String s)
	{
		this();
		name = s;
	}
	
	public TreeNode(TreeNode rent,String s) {
		this(rent);
		name = s;
	}

	public int getNumDescendants()
	{
		return numDescendants;
	}
	
	public int getNumLeaves()
	{
		return numLeaves;
	}
	
	public float getMaxHeight()
	{
		return maxHeight;
	}
	
	public int getMaxDepth()
	{
		return maxDepth;
	}
	
	public synchronized void percolateUp()
	{
		if (parent == NULL_PARENT) return;
		parent.percolate(NUM_LEAVES,numLeaves);
		parent.percolate(NUM_DESCENDANTS,numDescendants);
		parent.percolate(MAX_DEPTH, maxDepth);
	}
	
	public synchronized void percolate(int type, int val)
	{
		switch (type)
		{
			case (NUM_LEAVES):
				numLeaves += val;
				if (parent != NULL_PARENT)parent.percolate(type,val);
				break;
			case (NUM_DESCENDANTS):
				numDescendants += val;
				if (parent != NULL_PARENT)parent.percolate(type,val);
				break;
			case (MAX_DEPTH):
				maxDepth = Math.max(maxDepth, val+1);
				if (parent != NULL_PARENT)parent.percolate(type,maxDepth);
				break;
		}
	}
	
	public synchronized void addChild(TreeNode child) {	
		children.add(child);
		if (this != child.parent)
		{
			child.parent = this;
		}
		child.percolateUp();
		sortChildren();
	}
	
	public synchronized void removeChild(TreeNode child) {
		children.remove(child);
		percolate(NUM_LEAVES,-child.numLeaves);
		percolate(NUM_DESCENDANTS,-child.numDescendants);
		// Reset and re-calculate the max height and depth.
		maxDepth = 0;
		maxHeight = 0;
		for (int i=0; i < children.size(); i++)
		{
			TreeNode c = (TreeNode)children.get(i);
			maxDepth = Math.max(c.maxDepth+1, maxDepth);
			maxHeight = Math.max(c.maxHeight+c.height,maxHeight);
		}
	}
	
	public synchronized void sortChildren() {
		Collections.sort(children);
	}
	
	public boolean isLeaf()
	{
		return (getNumChildren() == 0);
	}

	public ArrayList getChildren()
	{
		return children;
	}

	public int getNumChildren()
	{
		return children.size();
	}
	
	public synchronized void getAllDescendants(ArrayList toAdd)
	{
		int size = children.size();
		for (int i = 0; i < size; ++i)
		{
			TreeNode child = (TreeNode) children.get(i);
			toAdd.add(child);
			child.getAllDescendants(toAdd);
		}
	}
	
	public synchronized void getAllNodes(ArrayList toAdd)
	{
		getAllDescendants(toAdd);
		toAdd.add(this);
	}
	
	public synchronized void getAllLeaves(ArrayList toAdd)
	{
		if (children.size() == 0)
		{
			toAdd.add(this);
			return;
		}
		int size = children.size();
		for (int i = size-1; i >= 0; i--)
		{
			TreeNode child = (TreeNode) children.get(i);
			if (!child.isLeaf())
				child.getAllLeaves(toAdd);
			else
				toAdd.add(child);
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

	public TreeNode getParent()
	{
		return parent;
	}
	
	public boolean equals(Object o) {
		if (o instanceof TreeNode) {
			TreeNode b = (TreeNode)o;
			if (this.serial == b.serial) return true;
		}
		return false;
	}
	
	public int compareTo(Object o) {
		if (o == null) throw new NullPointerException();
		TreeNode b = (TreeNode)o;
		int mySize = this.numDescendants;
		int hisSize = b.numDescendants;
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
