package org.phylowidget.tree;

import org.andrewberman.ui.ifaces.Positionable;

public final class RenderNode extends TreeNode
{
	public float unscaledX,unscaledY;
	public float x,y;
	public float unitTextWidth;
	
	public boolean hovered;
	
	public RenderNode(String taxonName)
	{
		super(taxonName);
	}

	public RenderNode()
	{
		super();
	}
}
