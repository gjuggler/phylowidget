package org.andrewberman.ui;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.ifaces.Malleable;
import org.andrewberman.ui.ifaces.Positionable;

import processing.core.PApplet;
import processing.core.PGraphicsJava2D;

public class Label extends AbstractUIObject implements Malleable
{
	PApplet app;

	String label;
	
	Color color;
	float fontSize;
	float x, y;

	public Label(PApplet p)
	{
		this.app = p;
		UIUtils.loadUISinglets(p);
		EventManager.instance.add(this);

		color = new Color(Color.black);
		label = "";
		fontSize = 16;
		x = 0;
		y = 0;
	}

	public Label(PApplet p, String s)
	{
		this(p);
		setLabel(s);
	}

	public void setLabel(String s)
	{
		label = s;
	}

	public String getLabel()
	{
		return label;
	}

	public void draw()
	{
		app.smooth();
		app.fill(color.getRGB());
		app.textFont(FontLoader.instance.vera);
		app.textSize(fontSize);
		app.textAlign(PApplet.LEFT);
		app.text(label, x, y);
	}

	public float getX()
	{
		return x;
	}

	public float getY()
	{
		return y;
	}

	public void setFontSize(float f)
	{
		fontSize = f;
	}
	
	public void setPosition(float x, float y)
	{
		setPositionByCornerNW(x,y);
	}

	public void setPositionByBaseline(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	public void setPositionByCornerNW(float west, float north)
	{
		cache();
		x = west; // Nothing fancy here
		y = north + cacheA;
	}
	
//	public void setPositionByCornerSW(float west, float south)
//	{
//		cache();
//		x = west;
//		y = south - cacheD;
//	}
	
	public void setX(float f)
	{
		x = f;
	}

	public void setY(float f)
	{
		cache();
		y = f + cacheA;
	}

	public void setYBaseline(float f)
	{
		y = f;
	}
	
	public float getHeight()
	{
		cache();
		return cacheH;
	}

	void cache()
	{
		if (cacheS == label && cacheFS == fontSize)
			return;
		cacheS = label;
		cacheFS = fontSize;
		cacheH = UIUtils.getTextHeight(app.g, FontLoader.instance.vera, fontSize, label, true);
		cacheW = UIUtils.getTextWidth(app.g, FontLoader.instance.vera, fontSize, label, true);
		cacheA = UIUtils.getTextAscent(app.g, FontLoader.instance.vera, fontSize, true);
		cacheD = UIUtils.getTextDescent(app.g, FontLoader.instance.vera, fontSize, true);
	}
	
	float cacheFS;
	String cacheS;
	float cacheW;
	float cacheH;
	float cacheA;
	float cacheD;
	public float getWidth()
	{
		cache();
		return cacheW;
	}

	public void setHeight(float h)
	{
		setFontSize(getFontSize() * h/getHeight());
	}

	public void setWidth(float w)
	{
		setFontSize(getFontSize() * w/getWidth());
	}
	
	private float getFontSize()
	{
		return fontSize;
	}

	public void setColor(int r, int g, int b)
	{
		color = new Color(r,g,b);
	}
	
	public void setSize(float w, float h)
	{
	}

}
