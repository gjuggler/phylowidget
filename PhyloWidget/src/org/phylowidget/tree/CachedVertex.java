/**************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PhyloWidget.  If not, see <http://www.gnu.org/licenses/>.
 */
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

	private CachedVertex firstChild;
	private CachedVertex lastChild;
	
	public int getMaxChildEnclosed()
	{
		return maxChildEnclosed;
	}

	public void setMaxChildEnclosed(int maxChildEnclosed)
	{
		this.maxChildEnclosed = maxChildEnclosed;
	}
	
	public CachedVertex getFirstChild()
	{
		return firstChild;
	}

	public void setFirstChild(CachedVertex firstChild)
	{
		this.firstChild = firstChild;
	}

	public Object getLastChild()
	{
		return lastChild;
	}

	public void setLastChild(CachedVertex lastChild)
	{
		this.lastChild = lastChild;
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
