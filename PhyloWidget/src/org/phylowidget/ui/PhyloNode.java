package org.phylowidget.ui;

import org.andrewberman.ui.tween.Tween;
import org.andrewberman.ui.tween.TweenFriction;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.CachedVertex;

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

	static TweenFriction fric = TweenFriction
			.tween(0.3f * PhyloWidget.TWEEN_FACTOR);

	static final float mult = 10000f;

	private Tween xTween;
	private Tween yTween;
	public boolean labelWasDrawn;

	public PhyloNode(Object o)
	{
		super(o);
		xTween = new Tween(null, fric, Tween.OUT, 0f, 0f, 0f);
		yTween = new Tween(null, fric, Tween.OUT, 0f, 0f, 0f);
	}

	public boolean isStationaryX()
	{
//		return (!xTween.isTweening() && !yTween.isTweening());
		float ratio = (xTween.getPosition() - xTween.getBegin()) / xTween.getChange();
		return (ratio > .8);
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
