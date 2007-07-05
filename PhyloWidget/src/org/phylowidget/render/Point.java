package org.phylowidget.render;

import java.awt.geom.Point2D;


public final class Point extends Point2D.Float
{
	private static final long serialVersionUID = 1L;

	public Point()
	{
		super();
	}
	
	public Point(float x, float y)
	{
		super(x,y);
	}
	
	public Point translate(float dx, float dy)
	{
		this.setLocation(dx+x,dy+y);
		return this;
	}
	
	public Point scale(float scaleX, float scaleY)
	{
		this.setLocation(x*scaleX,y*scaleY);
		return this;
	}
	
}
