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
package org.phylowidget.tree;

import org.andrewberman.ui.tween.Tween;
import org.andrewberman.ui.tween.TweenFriction;
import org.andrewberman.ui.tween.TweenQuad;
import org.phylowidget.PhyloWidget;

public class PhyloNode extends CachedVertex implements Comparable
{
	// public double unscaledX,unscaledY;
	private double x, y;
	private float realX, realY;
	public double unitTextWidth;
	public boolean drawMe, isWithinScreen;

	public float zoomTextSize = 1;

	private int state = 0;
	public static final int NONE = 0;
	public static final int CUT = 1;
	public static final int COPY = 2;

	public boolean found = false;

	static TweenFriction fric = TweenFriction
			.tween(0.3f * PhyloWidget.TWEEN_FACTOR);
	static TweenQuad quad = TweenQuad.tween;
	static final float mult = 10000f;

	private Tween xTween;
	private Tween yTween;
	public boolean labelWasDrawn;

	public PhyloNode()
	{
		super();
		xTween = new Tween(null, quad, Tween.OUT, (float) x, (float) x, 20f);
		yTween = new Tween(null, quad, Tween.OUT, (float) y, (float) y, 20f);
		
//		if (o instanceof PhyloNode)
//		{
//			PhyloNode n = (PhyloNode) o;
//			setPosition(n);
//		}
		
		//		xTween = new Tween(null, fric, Tween.OUT, (float)x, (float)x, 0f);
		//		yTween = new Tween(null, fric, Tween.OUT, (float)y, (float)y, 0f);
	}

	@Override
	protected Object clone()
	{
		Object clone = super.clone();
		PhyloNode n = (PhyloNode) clone;
		n.xTween.continueTo(xTween.getPosition());
		n.yTween.continueTo(yTween.getPosition());
		n.xTween.fforward();
		n.yTween.fforward();
		return n;
	}
	
	public void setPosition(PhyloNode n)
	{
		if (n == null)
			return;
		setPosition(n.getX(),n.getY());
		fforward();
	}

	public void update()
	{
		//		zoomTextSize *= 0.9f;

		xTween.update();
		yTween.update();
		x = xTween.getPosition() / mult;
		y = yTween.getPosition() / mult;
	}

	public void setPosition(float x, float y)
	{
		setX(x);
		setY(y);
	}

	public void fforward()
	{
		xTween.fforward();
		yTween.fforward();
		update();
	}

	public void setX(float x)
	{
		xTween.continueTo(x * mult);
		this.x = x;
	}

	public void setY(float y)
	{
		yTween.continueTo(y * mult);
		this.y = y;
	}

	public float getX()
	{
		return (float) x;
	}

	public float getY()
	{
		return (float) y;
	}

	public float getTargetX()
	{
		return xTween.getFinish() / mult;
	}

	public float getTargetY()
	{
		return yTween.getFinish() / mult;
	}

	public String toString()
	{
		return label;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String s)
	{
		label = s;
	}

	public int compareTo(Object o)
	{
		if (o instanceof PhyloNode)
		{
			PhyloNode that = (PhyloNode) o;
			float a = this.getTargetY();
			float b = that.getTargetY();
			if (a < b)
				return -1;
			else if (a > b)
				return 1;
		}
		return 0;
	}

	public void setState(int state)
	{
		this.state = state;
	}

	public int getState()
	{
		return state;
	}

	public float getRealX()
	{
		return realX;
	}

	public void setRealX(float realX)
	{
		this.realX = realX;
	}

	public float getRealY()
	{
		return realY;
	}

	public void setRealY(float realY)
	{
		this.realY = realY;
	}
}
