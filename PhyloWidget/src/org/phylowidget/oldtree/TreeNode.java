package org.phylowidget.oldtree;

import java.util.ArrayList;
import java.util.Collections;

public class TreeNode implements Comparable
{
	private static int serialSeed = 0;

	protected String name;
	public int serial;

	/**
	 * Bread-and-butter fields for the TreeNode class.
	 */
	public static final TreeNode NULL_PARENT = null;
	protected TreeNode parent = NULL_PARENT;
	protected ArrayList children = new ArrayList(2);
	protected float height = 0; // Height to the parent node.

	/**
	 * Cached values. Each of these needs to be percolated upwards when a part
	 * of the tree below this node is altered.
	 */
	protected int numLeaves = 1;
	protected int numDescendants = 0;
	protected int maxDepth = 0;
	protected float maxHeight = 0;

	/**
	 * Static integers for the switch statement in percolate().
	 */
	public static final int NUM_LEAVES = 0;
	public static final int NUM_DESCENDANTS = 1;
	public static final int MAX_DEPTH = 2;

	/**
	 * If true, then the TreeNode libraries will use the percolate() function to
	 * calculate cached variables, as opposed to running through the whole tree
	 * to calculate them when needed.
	 */
//	public static boolean usePercolate = false;

	public static boolean autoLadderize = true;
	
	public TreeNode()
	{
		serial = serialSeed++;
	}

	public TreeNode(String s)
	{
		this();
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
	
	public void recalculateStuff()
	{
		calcMaxDepth();
		calcMaxHeight();
		calcNumDescendants();
		calcNumLeaves();
	}
	
	int calcMaxDepth()
	{
		if (isLeaf())
		{
			maxDepth = 0;
			return maxDepth;
		} else
		{
			int max = 0;
			for (int i=0; i < children.size(); i++)
			{
				TreeNode n = (TreeNode) children.get(i);
				int cur = n.calcMaxDepth();
				if (cur > max)
					max = cur;
			}
			maxDepth = max + 1;
			return maxDepth;
		}
	}
	
	float calcMaxHeight()
	{
		if (children.size() == 0)
		{
			maxHeight = height;
			return maxHeight;
		} else
		{
			float max = 1;
			for (int i=0; i < children.size(); i++)
			{
				TreeNode n = (TreeNode) children.get(i);
				float cur = n.calcMaxHeight();
				if (cur > max)
					max = cur;
			}
			maxHeight = max + height;
			return maxHeight;
		}
	}
	
	int calcNumDescendants()
	{
		int sum = 0;
		for (int i=0; i < children.size(); i++)
		{
			TreeNode n = (TreeNode)children.get(i);
			sum += n.calcNumDescendants();
		}
		numDescendants = sum + children.size();
		return numDescendants;
	}
	
	int calcNumLeaves()
	{
		if (isLeaf()) return 1;
		int sum = 0;
		for (int i=0; i < children.size(); i++)
		{
			TreeNode n = (TreeNode)children.get(i);
			sum += n.calcNumLeaves();
		}
		numLeaves = sum;
		return sum;
	}

	public synchronized void addChild(TreeNode child)
	{
		children.add(child);
		if (this != child.parent)
		{
			child.parent = this;
		}
		if (autoLadderize)
			sortChildren();
	}

	public synchronized void removeChild(TreeNode child)
	{
		children.remove(child);
	}

	public synchronized void prune()
	{
		if (children.size() == 1)
		{ // Only child.
			TreeNode n = (TreeNode) children.get(0);
			if (!n.isLeaf())
			{ // Child has children.
				removeChild(n);
				for (int i = 0; i < n.children.size(); i++)
				{
					TreeNode child = (TreeNode) n.children.get(i);
					addChild(child);
				}
			} else
			{ // Child is a leaf... so we just remove ourselves.
				if (this.parent != TreeNode.NULL_PARENT)
				{
					this.parent.removeChild(this);
					this.parent.addChild(n);
					return;
				}
			}
		}
		for (int i = 0; i < children.size(); i++)
		{
			((TreeNode) children.get(i)).prune();
		}
	}
	
	public synchronized void sortChildren()
	{
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

	public synchronized void getAll(ArrayList leaves, ArrayList nodes)
	{
		int size = children.size();
		for (int i = 0; i < size; ++i)
		{
			TreeNode child = (TreeNode) children.get(i);
			child.getAll(leaves, nodes);
		}
		if (nodes != null)
			nodes.add(this);
		if (leaves != null && this.children.size() == 0)
			leaves.add(this);
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

	public boolean equals(Object o)
	{
		if (o instanceof TreeNode)
		{
			TreeNode b = (TreeNode) o;
			if (this.serial == b.serial)
				return true;
		}
		return false;
	}

	public int compareTo(Object o)
	{
		if (o == null)
			throw new NullPointerException();
		TreeNode b = (TreeNode) o;
		int mySize = this.numDescendants;
		int hisSize = b.numDescendants;
//		int mySize = this.numLeaves;
//		int hisSize = b.numLeaves;
		if (mySize < hisSize)
			return -1;
		else if (mySize > hisSize)
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
