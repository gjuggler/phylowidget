package org.phylowidget.tree;

import org.andrewberman.ui.ifaces.Positionable;

public final class PhyloNode extends TreeNode implements Positionable
{
	float x,y;
	
	public float unitTextWidth;
	
	public int state;
	public static final int NORMAL = 0;
	public static final int BRIGHT = 1;
	public static final int DIM = 2;
	public static final int INVIS = 3;
	
	public PhyloNode(String taxonName)
	{
		super(taxonName);
	}

	public PhyloNode()
	{
		super();
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
