package org.phylowidget.render;

import org.andrewberman.ui.ifaces.Positionable;
import org.phylowidget.tree.TreeNode;

public final class PhyloNode extends TreeNode implements Positionable
{
	float x,y;
	
	public PhyloNode(String taxonName)
	{
		super(taxonName);
	}

	public void setPosition(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	public float getX()
	{
		return x;
	}
	
	public float getY()
	{
		return y;
	}
}
