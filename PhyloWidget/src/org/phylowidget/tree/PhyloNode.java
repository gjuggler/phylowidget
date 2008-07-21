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

import java.util.HashMap;

import org.andrewberman.ui.tween.Tween;
import org.andrewberman.ui.tween.TweenFriction;
import org.andrewberman.ui.tween.TweenQuad;
import org.phylowidget.PhyloWidget;
import org.phylowidget.UsefulConstants;
import org.phylowidget.render.images.ImageSearcher;

public class PhyloNode extends CachedVertex implements Comparable, UsefulConstants
{
	// public double unscaledX,unscaledY;
	private double x, y;
	private float realX, realY;
	public double aspectRatio; // Almost ready to get rid of this one...
	public double unitTextWidth;
	public boolean drawMe, isWithinScreen;

	public float bulgeFactor = 1;

	private int sorting = RootedTree.FORWARD.intValue();

	private int state = 0;
	public static final int NONE = 0;
	public static final int CUT = 1;
	public static final int COPY = 2;

	public boolean found = false;

	static TweenFriction fric = TweenFriction
			.tween(0.3f * PhyloWidget.TWEEN_FACTOR);
	static TweenQuad quad = TweenQuad.tween;
	static final float mult = 10000f;
	
	
	
	HashMap<String, String> annotations;
	
	private Tween xTween;
	private Tween yTween;
	public boolean labelWasDrawn;
	public float lastTextSize;

	private ImageSearcher searchResults;

	public PhyloNode()
	{
		super();
		xTween = new Tween(null, quad, Tween.OUT, (float) x, (float) x, 30f);
		yTween = new Tween(null, quad, Tween.OUT, (float) y, (float) y, 30f);
	}

	public void loadImage()
	{
		if (searchResults == null)
			searchResults = new ImageSearcher(this);
		else
			searchResults.next();

		searchResults.getThumbnailURL();
//						searchResults.getImageURL();
	}

	public void setPosition(PhyloNode n)
	{
		if (n == null)
			return;
		setPosition(n.getX(), n.getY());
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
		if (searchResults != null)
			searchResults.clear();
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

	public boolean isNHX()
	{
		return (annotations != null);
	}

	public void clearAnnotations()
	{
		if (annotations != null)
			annotations.clear();
	}

	public void clearAnnotation(String key)
	{
		if (annotations == null)
			return;
		annotations.remove(key);
	}

	public void setAnnotation(String key, String value)
	{
		if (annotations == null)
			annotations = new HashMap<String, String>();
		if (value == null)
			annotations.remove(key);
		else
			annotations.put(key.toLowerCase(), value);
	}

	/**
	 * Warning: MAY RETURN NULL 
	 * @param key
	 * @return
	 */
	public String getAnnotation(String key)
	{
		if (PhyloWidget.cfg.ignoreAnnotations)
			return null;
		if (annotations == null)
			return null;
		else
			return annotations.get(key.toLowerCase());
	}

	/**
	 * May return null!
	 * @return
	 */
	public HashMap<String, String> getAnnotations()
	{
		if (PhyloWidget.cfg.ignoreAnnotations)
			return null;
		return annotations;
	}
}
