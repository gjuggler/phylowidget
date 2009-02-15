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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import org.andrewberman.ui.tween.Tween;
import org.andrewberman.ui.tween.TweenQuad;
import org.phylowidget.PWContext;
import org.phylowidget.PWPlatform;
import org.phylowidget.PhyloTree;
import org.phylowidget.PhyloWidget;
import org.phylowidget.UsefulConstants;
import org.phylowidget.render.NodeRange;
import org.phylowidget.render.images.ImageSearcher;

final public class PhyloNode extends CachedVertex implements Comparable, UsefulConstants
{
	private double layoutX, layoutY; // Layout position.
	private float realX, realY; // Real-world (i.e. screen) position, after scaling and translation of the layout.
	private float angle; // Angle (in radians) at which the node should be drawn. Clockwise from horizontal.

	public Point2D[] corners =
			new Point2D.Float[] { new Point2D.Float(), new Point2D.Float(), new Point2D.Float(), new Point2D.Float() };
	public Rectangle2D.Float rect = new Rectangle2D.Float();

	private byte textAlign = ALIGN_LEFT;
	public static final byte ALIGN_LEFT = 0;
	public static final byte ALIGN_RIGHT = 1;

	public float textMult;
	public float unitTextWidth;
	//	public float aspectRatio; // Almost ready to get rid of this one...
	public boolean drawMe, isWithinScreen;

	public float bulgeFactor = 1;
	public boolean found = false;

	private int sorting = RootedTree.FORWARD.intValue();

	private int state = 0;
	public static final int NONE = 0;
	public static final int CUT = 1;
	public static final int COPY = 2;

	//	static TweenFriction fric = TweenFriction
	//			.tween(0.3f * PhyloWidget.TWEEN_FACTOR);
	static TweenQuad quad = TweenQuad.tween;
	static final float mult = 10000f;

	HashMap<String, String> annotations;

	private Tween xTween;
	private Tween yTween;
	public boolean labelWasDrawn;
	public boolean drawLineAndNode;
	public boolean drawLabel;
	public float lastTextSize;

	private ImageSearcher searchResults;
	public NodeRange range;

	PWContext context;
	
	public PhyloNode()
	{
		super();
		this.context = PWPlatform.getInstance().getThisAppContext();
		xTween = new Tween(null, quad, Tween.OUT, (float) layoutX, (float) layoutX, 30f);
		yTween = new Tween(null, quad, Tween.OUT, (float) layoutY, (float) layoutY, 30f);
		range = new NodeRange();
		range.node = this;
	}

	public void loadThumbImage()
	{
		if (searchResults == null)
			searchResults = new ImageSearcher(this);
		else
			searchResults.next();

		searchResults.loadThumbnailURL();
	}

	public void loadFullImage()
	{
		if (searchResults != null)
		{
			searchResults.loadFullImageURL();
		}
	}

	public String getFullImageURL()
	{
		return searchResults.getFullImageURL();
	}

	@Override
	public double getBranchLength()
	{
		if (!context.config().useBranchLengths)
			return 1;
		return super.getBranchLength();
	}

	public void setPosition(PhyloNode n)
	{
		if (n == null)
			return;
		setPosition(n.getLayoutX(), n.getLayoutY());
		fforward();
	}

	public void update()
	{
		//		zoomTextSize *= 0.9f;
		if (context.config().useAnimations)
		{
		xTween.update();
		yTween.update();
		} else
		{
			xTween.fforward();
			yTween.fforward();
		}
		layoutX = xTween.getPosition() / mult;
		layoutY = yTween.getPosition() / mult;
	}

	public void setPosition(float x, float y)
	{
		setLayoutX(x);
		setLayoutY(y);
	}

	public void fforward()
	{
		xTween.fforward();
		yTween.fforward();
		update();
	}

	public void setLayoutX(float x)
	{
		xTween.continueTo(x * mult, context.config().animationFrames);
		this.layoutX = x;
	}

	public void setLayoutY(float y)
	{
		yTween.continueTo(y * mult, context.config().animationFrames);
		this.layoutY = y;
	}

	public float getLayoutX()
	{
		return (float) layoutX;
	}

	public float getLayoutY()
	{
		return (float) layoutY;
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

	public float getX()
	{
		return realX;
	}

	public void setX(float realX)
	{
		this.realX = realX;
	}

	public float getY()
	{
		return realY;
	}

	public void setY(float realY)
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
			if (key.length() <= 3)
				annotations.put(key.toLowerCase(),value);
			else
				annotations.put(key, value); // GJ 2009-02-15 : stop lower-casing annotations for longer keys.
	}

	/**
	 * Warning: MAY RETURN NULL
	 * 
	 * @param key
	 * @return
	 */
	public String getAnnotation(String key)
	{
		if (context.config().ignoreAnnotations)
			return null;
		if (annotations == null)
			return null;
		else
			return annotations.get(key.toLowerCase());
	}

	/**
	 * May return null!
	 * 
	 * @return
	 */
	public HashMap<String, String> getAnnotations()
	{
		if (context.config().ignoreAnnotations)
			return null;
		return annotations;
	}

	public void setAngle(float angle)
	{
		this.angle = angle;
	}

	public float getAngle()
	{
		return angle;
	}

	public int getTextAlign()
	{
		return textAlign;
	}

	public void setTextAlign(int textAlign)
	{
		this.textAlign = (byte) textAlign;
	}

	public static boolean parseTruth(String s)
	{
		if (s.startsWith("T") || s.startsWith("t") || s.startsWith("y") || s.startsWith("Y") || s.equals("1"))
			return true;
		else
			return false;
	}
	
	public synchronized PhyloTree getTree()
	{
		if (range != null)
		{
			if (range.render != null)
			{
				return (PhyloTree) range.render.getTree();
			}
			System.out.println("Render null!");
		}
		System.out.println("Range null!");
		return null;
	}
	
	//	public float getTrueAngle()
	//	{
	//		return trueAngle;
	//	}
	//
	//	public void setTrueAngle(float trueAngle)
	//	{
	//		this.trueAngle = trueAngle;
	//	}
}
