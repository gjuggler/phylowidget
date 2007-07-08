package org.andrewberman.ui;

import java.awt.color.ColorSpace;

public final class Color extends java.awt.Color
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Color(int r, int g, int b)
	{
		super(r, g, b);
	}

	public Color(int r, int g, int b, int a)
	{
		super(r,g,b,a);
	}
	
	public Color brighter(double multiplier)
	{
		int r = getRed();
        int g = getGreen();
        int b = getBlue();
        return new Color(Math.min((int)(r+multiplier), 255),
                Math.min((int)(g+multiplier), 255),
                Math.min((int)(b+multiplier), 255));
	}
	
	public Color darker(double multiplier)
	{
		int r= getRed();
		int g = getGreen();
		int b = getBlue();
		return new Color(Math.max((int)(r-multiplier), 0),
                Math.max((int)(g-multiplier), 0),
                Math.max((int)(b-multiplier), 0));
	}
	
}
