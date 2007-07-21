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

/**
 * A <code>StyleSet</code> is a simple definition of color, stroke, and font
 * constants to be used by UI objects as needed.
 * <p>
 * If someone wishes to use the "default" style, then simply call the static
 * method <code>StyleSet.defaultStyle()</code>.
 * <p>
 * If you wish, on the other hand, to use a non-default set, you can create your
 * own StyleSet using an empty constructor: <code>new StyleSet()</code>. You
 * can then load the default styles using <code>loadDefaults()</code> and
 * modify individual settings at your will.
 * 
 * @author Greg
 */
public class StyleSet
{
	/*
	 * Basic colors and strokes.
	 */
	public Stroke stroke;
	public Stroke noStroke;
	public float strokeWidth;
	public Color strokeColor;
	/*
	 * Menu-specific stuff.
	 */
	public Color[] stateColors;
	public Color menuBackground;
	public Color menuGradLo;
	public Color menuGradHi;
	/*
	 * Text stuff.
	 */
	public PFont font;
	public float fontSize;
	public Color textColor;
	public Color selectionColor;
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

	private static StyleSet defaultSet;

	public StyleSet()
	{
	}

	public static StyleSet defaultStyle()
	{
		if (defaultSet == null)
		{
			defaultSet = new StyleSet();
			defaultSet.loadDefaults();
		}
		return defaultSet;
	}

	public void loadDefaults()
	{
		/*
		 * Basic colors and strokes.
		 */
		strokeWidth = .5f;
		stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		noStroke = new BasicStroke(0, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		strokeColor = new Color(Color.black);
		/*
		 * Menu-specific colors.
		 */
		menuBackground = new Color(245, 245, 255);
		stateColors = new Color[3];
		Color baseState = new Color(220, 230, 255);
		stateColors[MenuItem.UP] = baseState;
		stateColors[MenuItem.OVER] = baseState.brighter(20);
		stateColors[MenuItem.DOWN] = baseState.darker(20);
		menuGradLo = new Color(245, 245, 255);
		menuGradHi = new Color(190, 210, 245);
		/*
		 * Text stuff.
		 */
		fontSize = 12;
		font = FontLoader.vera;
		font.font = font.font.deriveFont(fontSize);
		textColor = new Color(0, 0, 0);
		selectionColor = new Color(40, 40, 255);
		/*
		 * "Layout" stuff
		 */
		padY = Math.round(fontSize / 3);
		padX = padY * 1.5f;
		margin = Math.min(padX, padY);
		/*
		 * Shape stuff.
		 */
		GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
		p.moveTo(0f, -.5f);
		p.lineTo(.5f, 0f);
		p.lineTo(0f, .5f);
		p.closePath();
		subTriangle = new Area(p);
		roundOff = .2f;
	}

	public Paint getGradient(float lo, float hi)
	{
		return new GradientPaint(0, lo, menuGradLo, 0, hi,
				menuGradHi, true);
	}

	public Paint getGradient(float loX, float loY, float hiX, float hiY)
	{
		return new GradientPaint(loX,loY,menuGradHi.brighter(60),hiX,hiY,menuGradHi,true);
	}
	
	public Paint getGradient(int state, float loX, float loY, float hiX, float hiY)
	{
		switch (state)
		{
			case (MenuItem.UP):
				return new GradientPaint(loX,loY, menuGradLo, hiX,hiY, menuGradHi
						, true);
			case (MenuItem.OVER):
				return new GradientPaint(loX,loY, menuGradLo.brighter(15), hiX,hiY, menuGradHi
						.brighter(15), true);
			case (MenuItem.DOWN):
				return new GradientPaint(loX,loY, menuGradLo.darker(15), hiX,hiY, menuGradHi
						.darker(15), true);
			default:
				return Color.black;
		}
	}
	
	public Paint getGradient(int state, float lo, float hi)
	{
		return getGradient(state,0,lo,0,hi);
//		switch (state)
//		{
//			case (MenuItem.UP):
//				return new GradientPaint(0, lo, menuGradLo, 0, hi, menuGradHi
//						, true);
//			case (MenuItem.OVER):
//				return new GradientPaint(0, lo, menuGradLo.brighter(15), 0, hi, menuGradHi
//						.brighter(15), true);
//			case (MenuItem.DOWN):
//				return new GradientPaint(0, lo, menuGradLo.darker(30), 0, hi, menuGradHi
//						.darker(30), true);
//			default:
//				return Color.black;
//		}
	}
}