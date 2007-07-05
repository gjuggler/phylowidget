package org.andrewberman.ui;

import org.phylowidget.PhyloWidget;

import processing.core.PApplet;
import processing.core.PFont;

public class FontLoader
{
	private static PApplet p = PhyloWidget.p;
	
	private static final String base = "TimesNewRoman";
	
	public static PFont f64 = p.loadFont(base + "-64.vlw");
	public static PFont f32 = p.loadFont(base + "-32.vlw");
	public static PFont f16 = p.loadFont(base + "-16.vlw");
	public static PFont f8 = p.loadFont(base + "-8.vlw");
	
	private FontLoader()
	{
		
	}
}
