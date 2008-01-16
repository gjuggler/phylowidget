package org.andrewberman.ui;

import java.awt.geom.Point2D;

/**
 * The <code>Point</code> class is, like the <code>Color</code> class, a
 * crappy extension of Java's built-in AWT functionality. The most important
 * change is that this <code>Point</code> class is set to <code>final</code>,
 * meaning that if the JVM is smart enough, it will run significantly faster
 * than a non-final class.
 * <p>
 * Also included are a couple of convenient methods, such as <code>translate()</code>
 * and <code>scale</code>.  
 * 
 * @author Greg
 * @see		java.awt.Point
 * @see		org.andrewberman.ui.UIUtils
 * @see		org.andrewberman.ui.Color
 */
public final class Point extends Point2D.Float
{
	private static final long serialVersionUID = 1L;

	public Point()
	{
		super();
	}

	public Point(float x, float y)
	{
		super(x, y);
	}

	public Object clone()
	{
		return new Point(x,y);
	}
	
	public Point translate(float dx, float dy)
	{
		this.setLocation(dx + x, dy + y);
		return this;
	}
	
	public Point scale(float scaleX, float scaleY)
	{
		this.setLocation(x * scaleX, y * scaleY);
		return this;
	}

	public float dotProd(Point that)
	{
		return this.x * that.x + this.y * that.y;
	}
	
	public float length()
	{
		return (float) Math.sqrt(this.x * this.x + this.y * this.y);
	}
	
	public float lengthSqr()
	{
		return this.x * this.x + this.y * this.y;
	}
	
}
