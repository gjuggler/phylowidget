package org.phylowidget.render;

import org.andrewberman.ui.Color;
import org.andrewberman.ui.menu.StyleSet;

public class RenderStyleSet extends StyleSet
{

	public Color hiliteColor;
	public Color dimColor;
	public Color regColor;
	
	public float hiliteStroke;
	public float dimStroke;
	public float regStroke;

	public void loadDefaults()
	{
		super.loadDefaults();
		
		regColor = new Color(Color.black);
		dimColor = regColor.brighter(50);
		hiliteColor = new Color(100,150,255);
		
		hiliteStroke = 2f;
		dimStroke = .5f;
		regStroke = 1f;
		
	}
	
}
