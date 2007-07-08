package org.andrewberman.ui.menu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;

public class ColorSet
{
	public Stroke stroke;
	public float strokeWidth;
	public Paint strokeColor;
	public Paint fillColor;
	public Paint textColor;

	public static ColorSet defaultSet;
	
	static {
		defaultSet = setDefaults(new ColorSet());
	}
	
	public static ColorSet setDefaults(ColorSet defaultSet)
	{
		defaultSet.strokeWidth = 1;
		defaultSet.stroke = new BasicStroke(defaultSet.strokeWidth,BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		defaultSet.strokeColor = Color.black;
		defaultSet.fillColor = Color.white;
		defaultSet.textColor = Color.black;
		return defaultSet;
	}
}
