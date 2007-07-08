package org.andrewberman.ui.menu;

import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Stroke;

import org.andrewberman.ui.Color;

public class MenuColorSet extends ColorSet
{
	public Paint[] stateColors;
	
	public Stroke bubbleStroke;
	public Paint bubbleStrokeColor;
	public Paint bubbleFill;
	public Paint bubbleText;
	
	public static MenuColorSet defaultSet;
	
	static
	{
		defaultSet = setDefaults(new MenuColorSet());
	}
	
	public static MenuColorSet setDefaults(MenuColorSet defaultSet)
	{
		ColorSet.setDefaults(defaultSet); // Grab defaults from our superior.
		
		defaultSet.stroke = new BasicStroke(2,BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		defaultSet.strokeWidth = 2;
		defaultSet.bubbleStroke = defaultSet.stroke;
		
		defaultSet.strokeColor = Color.black;
		defaultSet.fillColor = Color.white;
		defaultSet.textColor = Color.black;
		
		defaultSet.bubbleStrokeColor = Color.black;
		defaultSet.bubbleFill = Color.white;
		defaultSet.bubbleText = Color.black;
		
		Color color = new Color(230,230,255);
		defaultSet.stateColors = new Paint[3];
		defaultSet.stateColors[0] = color;
		defaultSet.stateColors[1] = color.brighter(15);
		defaultSet.stateColors[2] = color.darker(30);
		return defaultSet;
	}
}