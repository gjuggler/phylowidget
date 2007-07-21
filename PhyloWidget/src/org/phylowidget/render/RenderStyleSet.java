package org.phylowidget.render;

import org.andrewberman.ui.Color;
import org.andrewberman.ui.menu.StyleSet;

public class RenderStyleSet
{

	public Color regColor;
	public float regStroke;
	
	public Color hoverColor;
	public float hoverStroke;
	
	public Color dimColor;
	public float dimStroke;

	private static RenderStyleSet defaultSet;
	
	public static RenderStyleSet defaultStyle()
	{
		if (defaultSet == null)
		{
			defaultSet = new RenderStyleSet();
			defaultSet.loadDefaults();
		}
		return defaultSet;
	}
	
	public void loadDefaults()
	{
		
		regColor = new Color(Color.black);
		regStroke = 1f;
		
		dimColor = regColor.brighter(100);
		dimStroke = .5f;
		
		hoverColor = new Color(100,150,255);
		hoverStroke = 3f;
	}
	
}
