/*******************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * PhyloWidget. If not, see <http://www.gnu.org/licenses/>.
 */
package org.andrewberman.ui;

import java.awt.AlphaComposite;
import java.awt.Composite;

import org.andrewberman.ui.ifaces.Malleable;

import processing.core.PApplet;
import processing.core.PGraphicsJava2D;

public class Label extends AbstractUIObject implements Malleable
{
	PApplet p;
	protected UIContext c;

	String label;

	Color color;
	float fontSize;
	float x, y;
	public float alpha = 1f;

	public Label(PApplet p)
	{
		this.p = p;
		c = UIPlatform.getInstance().getAppContext(p);
		c.event().add(this);

		color = new Color(Color.black);
		label = "";
		fontSize = 16;
		x = 0;
		y = 0;
	}

	public void dispose()
	{
		if (c != null && c.event() != null)
		{
			c.event().remove(this);
		}
		p = null;
		label = null;
		color = null;
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
		PGraphicsJava2D pg = (PGraphicsJava2D) p.g;
		Composite origComp = pg.g2.getComposite();
		pg.g2.setComposite(AlphaComposite.getInstance(
			AlphaComposite.SRC_OVER, alpha));
		
		if (UIUtils.isJava2D(p))
			p.smooth();
		p.fill(color.getRGB());
		p.textFont(c.getPFont());
		p.textSize(fontSize);
		p.textAlign(PApplet.LEFT);
		p.text(label, x, y);
		
		pg.g2.setComposite(origComp);
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
		setPositionByCornerNW(x, y);
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
		cacheH = UIUtils.getTextHeight(p.g, c.getPFont(),
				fontSize, label, true);
		cacheW = UIUtils.getTextWidth(p.g, c.getPFont(),
				fontSize, label, true);
		cacheA = UIUtils.getTextAscent(p.g, c.getPFont(),
				fontSize, true);
		cacheD = UIUtils.getTextDescent(p.g, c.getPFont(),
				fontSize, true);
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
		setFontSize(getFontSize() * h / getHeight());
	}

	public void setWidth(float w)
	{
		setFontSize(getFontSize() * w / getWidth());
	}

	private float getFontSize()
	{
		return fontSize;
	}

	public void setColor(int r, int g, int b)
	{
		color = new Color(r, g, b);
	}

	public void setSize(float w, float h)
	{
	}

}
