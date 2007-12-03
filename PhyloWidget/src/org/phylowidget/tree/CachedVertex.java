package org.phylowidget.tree;

public class CachedVertex extends DefaultVertex
{

	private Object parent;

	/**
	 * Calculated during root-to-tip recursion.
	 */
	private int depthToRoot;
	private double heightToRoot;
	private double branchLength;
	
	/**
	 * Calculated during leaf-to-root recursion.
	 */
	private int numLeaves;
	private int numEnclosed;
	private int maxDepthToLeaf;
	private double maxHeightToLeaf;
	private int maxChildEnclosed;

	private Object firstChild;
	private Object lastChild;
	
	public int getMaxChildEnclosed()
	{
		return maxChildEnclosed;
	}

	public void setMaxChildEnclosed(int maxChildEnclosed)
	{
		this.maxChildEnclosed = maxChildEnclosed;
	}
	
	public Object getFirstChild()
	{
		return firstChild;
	}

	public void setFirstChild(Object firstChild)
	{
		this.firstChild = firstChild;
	}

	public Object getLastChild()
	{
		return lastChild;
	}

	public void setLastChild(Object lastChild)
	{
		this.lastChild = lastChild;
	}

	public CachedVertex(Object o)
	{
		super(o);
	}

	public Object getParent()
	{
		return parent;
	}

	public void setParent(Object parent)
	{
		this.parent = parent;
	}

	public int getNumLeaves()
	{
		return numLeaves;
	}

	public void setNumLeaves(int numLeaves)
	{
		this.numLeaves = numLeaves;
	}

	public int getNumEnclosed()
	{
		return numEnclosed;
	}

	public void setNumEnclosed(int numEnclosed)
	{
		this.numEnclosed = numEnclosed;
	}

	public int getDepthToRoot()
	{
		return depthToRoot;
	}

	public void setDepthToRoot(int depthToRoot)
	{
		this.depthToRoot = depthToRoot;
	}

	public double getHeightToRoot()
	{
		return heightToRoot;
	}

	public void setHeightToRoot(double heightToRoot)
	{
		this.heightToRoot = heightToRoot;
	}

	public int getMaxDepthToLeaf()
	{
		return maxDepthToLeaf;
	}

	public void setMaxDepthToLeaf(int maxDepthToLeaf)
	{
		this.maxDepthToLeaf = maxDepthToLeaf;
	}

	public double getMaxHeightToLeaf()
	{
		return maxHeightToLeaf;
	}

	public void setMaxHeightToLeaf(double maxHeightToLeaf)
	{
		this.maxHeightToLeaf = maxHeightToLeaf;
	}

	public double getBranchLength()
	{
		return branchLength;
	}

	public void setBranchLength(double branchLength)
	{
		this.branchLength = branchLength;
	}

	
	
	
}
