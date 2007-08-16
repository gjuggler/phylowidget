package org.phylowidget.render;

import org.andrewberman.ui.Color;

public class RenderStyleSet
{

	public Color regColor;
	public float regStroke;
	
	public Color hoverColor;
	public float hoverStroke;
	
	public Color dimColor;
	public float dimStroke;

	public Color copyColor;
	public float copyStroke;
	
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
		
		dimColor = regColor.brighter(200);
		dimStroke = 4f;
		
		hoverColor = new Color(100,150,255);
		hoverStroke = 3f;
		
		copyColor = new Color(255,0,0);
		copyStroke = 4f;
	}
	
}
