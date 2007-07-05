package org.andrewberman.sortedlist;

public class XYRange implements ItemI
{
	/**
	 * A reference back to an object. For convenience.
	 */
	public Object parent;
	/**
	 * An ID, also for convenience. Could be used to signify what "type" of
	 * range this is. i.e. a node or a label.
	 */
	public int id;
	
	public float loX, hiX, loY, hiY = 0;
	
	public static final int LO_X = 0;
	public static final int HI_X = 1;
	public static final int LO_Y = 2;
	public static final int HI_Y = 3;
	
	public XYRange(Object parent)
	{
		this(parent,0,0,0,0);
	}
	
	public XYRange(Object parent, float lox, float hix, float loy, float hiy)
	{
		this.parent = parent;
		this.loX = lox;
		this.hiX = hix;
		this.loY = loy;
		this.hiY = hiy;
	}
	
	public float get(int what)
	{
		switch (what)
		{
			case LO_X:
				return loX;
			case HI_X:
				return hiX;
			case LO_Y:
				return loY;
			case HI_Y:
				return hiY;
			default:
				return -1;
		}
	}
}
