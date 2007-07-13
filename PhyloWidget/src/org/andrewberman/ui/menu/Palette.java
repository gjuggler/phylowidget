package org.andrewberman.ui.menu;

import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

import org.andrewberman.ui.Color;
import org.phylowidget.ui.FontLoader;

import processing.core.PFont;

public final class Palette
{	
	/*
	 * Basic colors and strokes.
	 */
	public Stroke stroke;
	public Stroke noStroke;
	public float strokeWidth;
	public Paint strokeColor;
	/*
	 * Menu-specific stuff.
	 */
	public Color[] stateColors;
	public Color menuGradLo;
	public Color menuGradHi;
	/*
	 * Text stuff.
	 */
	public PFont font;
	public float fontSize;
	public Paint textColor;
	public Paint selectedTextColor;
	/*
	 * "Layout" stuff 
	 */
	public float padY;
	public float padX;
	public float margin;
	/*
	 * Shape stuff.
	 */
	public Area subTriangle;
	public float roundOff;
	
	public static Palette defaultSet = new Palette();
	
	public Palette()
	{
		setDefaults();
	}
	
	public void setDefaults()
	{
		/*
		 * Basic colors and strokes.
		 */
		strokeWidth = .5f;
		stroke = new BasicStroke(strokeWidth,BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND);
		noStroke = new BasicStroke(0,BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		strokeColor = Color.black;
		/*
		 * Menu-specific colors.
		 */
		Color color = new Color(245,245,255);
		stateColors = new Color[3];
		stateColors[MenuItem.UP] = color;
		stateColors[MenuItem.OVER] = color.brighter(20);
		stateColors[MenuItem.DOWN] = color.darker(20);
		menuGradLo = new Color(255,255,255);
		menuGradHi = new Color(180,200,245);
		/*
		 * Text stuff.
		 */
		fontSize = 12;
		font = FontLoader.v12;	
		textColor = new Color(0,0,0);
		selectedTextColor = textColor;
		/*
		 * "Layout" stuff 
		 */
		padY = Math.round(fontSize/3);
		padX = padY * 2;
		margin = Math.min(padX,padY);
		/*
		 * Shape stuff.
		 */
		GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD,3);
		p.moveTo(0f,-.5f);
		p.lineTo(.5f, 0f);
		p.lineTo(0f, .5f);
		p.closePath();
		subTriangle = new Area(p);
		roundOff = .2f;
	}
	
	public Paint getGradient(float lo, float hi)
	{
		return new GradientPaint(0,lo,menuGradHi.brighter(60),0,hi,menuGradHi,true);
	}
	
	public Paint getGradient(int state, float lo, float hi)
	{
		switch (state)
		{
			case (MenuItem.UP):
				return new GradientPaint(0,lo,menuGradLo,0,hi,menuGradHi.brighter(15),true);
			case (MenuItem.OVER):
				return new GradientPaint(0,lo,menuGradLo,0,hi,menuGradHi.brighter(0),true); 
			case (MenuItem.DOWN):
				return new GradientPaint(0,lo,menuGradLo,0,hi,menuGradHi.darker(30),true);
			default:
				return Color.black;
		}
	}
}