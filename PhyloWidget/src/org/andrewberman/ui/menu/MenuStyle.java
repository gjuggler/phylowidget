package org.andrewberman.ui.menu;

import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

import org.andrewberman.ui.Color;
import org.andrewberman.ui.Style;
import org.andrewberman.ui.UIPlatform;

import processing.core.PFont;

public class MenuStyle extends Style
{

	public MenuStyle()
	{
		super();
		
		loadDefaults(this);
	}
	
	protected void loadDefaults(MenuStyle defaults)
	{
//		MenuStyle defaults = new MenuStyle();

		defaults.set("c.background", new Color(245, 245, 255));
		defaults.set("c.foreground", new Color(0,0,0));
		
		defaults.set("c.foregroundDisabled", new Color(0,0,0).brighter(100));
		
		defaults.set("c.disabled", new Color(245, 245, 255).darker(30));
		defaults.set("c.gradientLo", new Color(245, 245, 255));
		defaults.set("c.gradientHi", new Color(190, 210, 245));

		defaults.set("f.fontSize", 11f);
		defaults.set("font", UIPlatform.getInstance().getThisAppContext().getPFont());
		defaults.set("c.font", new Color(0, 0, 0));

		defaults.set("f.padY", 3.5f);
		defaults.set("f.padX", 3.5f);
		defaults.set("f.roundOff", 6f);
		
		defaults.set("f.strokeWeight",.5f);

		GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3);
		p.moveTo(0f, -.5f);
		p.lineTo(.5f, 0f);
		p.lineTo(0f, .5f);
		p.closePath();
		defaults.set("subTriangle", new Area(p));
	}

	public PFont getFont(Object key)
	{
		return (PFont)get(key);
	}
	
	public Paint getGradient(int state, float loY, float hiY)
	{
		return getGradient(state,0,loY,0,hiY);
	}
	
	public Paint getGradient(int state, float loX, float loY, float hiX,
			float hiY)
	{
		Color gradLo = getC("c.gradientLo");
		Color gradHi = getC("c.gradientHi");
		switch (state)
		{
			case (MenuItem.UP):
				return new GradientPaint(loX, loY, gradLo, hiX, hiY, gradHi,
						true);
			case (MenuItem.OVER):
				return new GradientPaint(loX, loY, gradLo.brighter(15), hiX,
						hiY, gradHi.brighter(15), true);
			case (MenuItem.DOWN):
			default:
				return new GradientPaint(loX, loY, gradLo.darker(15), hiX, hiY,
						gradHi.darker(15), true);
		}
	}

}
