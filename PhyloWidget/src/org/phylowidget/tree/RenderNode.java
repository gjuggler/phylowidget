package org.phylowidget.tree;

import org.andrewberman.ui.ifaces.Positionable;

public final class RenderNode extends TreeNode
{
	public float unscaledX,unscaledY;
	public float x,y;
	public float unitTextWidth;
	
	public boolean hovered;
	
	/**
	 *  If this node is a leaf, this represents the displayed index of the leaf.
	 */
	public int leafIndex;
	
	public RenderNode(String taxonName)
	{
		super(taxonName);
	}

	public RenderNode()
	{
		super();
	}
	
	public void recalculateStuff()
	{
		super.recalculateStuff();
		calcLowestLeaf();
		calcHighestLeaf();
	}
	
	void calcLowestLeaf()
	{
		
	}
	
	void calcHighestLeaf()
	{
		
	}
}
