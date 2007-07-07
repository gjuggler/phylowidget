package org.andrewberman.ui;

import org.phylowidget.PhyloWidget;

import processing.core.PApplet;

public class ColorSwatch
{	
	private static PApplet p = PhyloWidget.p;
	
	public static final int blue = p.color(0,0,255); 
	public static final int lightBlue = lighten(blue, 100); 
	public static final int lighterBlue = lighten(lightBlue, 100);
	public static final int lightestBlue = lighten(lighterBlue, 25);
	
	public static final int green = p.color(0,255,0);
	public static final int lightGreen = lighten(green, 100);
	public static final int lighterGreen = lighten(lightGreen, 100);
	public static final int lightestGreen = lighten(lighterGreen, 25);
	
	public static int lighten(int color, float amount)
	{
		float red = p.red(color);
		float green = p.green(color);
		float blue = p.blue(color);
		float alpha = p.alpha(color);
		
		red = PApplet.constrain(red+amount,0,255);
		green = PApplet.constrain(green+amount,0,255);
		blue = PApplet.constrain(blue+amount,0,255);
		
		return p.color(red,green,blue,alpha);
	}
}
