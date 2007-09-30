package org.phylowidget.tree;

public class CachedVertex extends DefaultVertex
{

	private Object parent;

	/**
	 * Calculated during root-to-tip recursion.
	 */
	private int depthToRoot;
	private double heightToRoot;
	
	/**
	 * Calculated during leaf-to-root recursion.
	 */
	private int numLeaves;
	private int numEnclosed;
	private int maxDepthToLeaf;
	private double maxHeightToLeaf;

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

	
	
	
}
