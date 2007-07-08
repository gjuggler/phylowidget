package org.andrewberman.ui;

import java.awt.Color;

public class Java2DUtils
{

	public static final Color lightenColor(Color c, int lighten)
	{
		int red = constrain(c.getRed() + lighten,0,255);
		int green = constrain(c.getGreen() + lighten,0,255);
		int blue = constrain(c.getBlue() + lighten,0,255);
		int alpha = c.getAlpha();
		return new Color(red,green,blue,alpha);
	}
	
	public static final int constrain(int val, int lo, int hi)
	{
		if (val < lo) val = lo;
		else if (val > hi) val = hi;
		return val;
	}
	
}
