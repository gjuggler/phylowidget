package org.phylowidget.ui;

import org.andrewberman.ui.tween.Tween;
import org.andrewberman.ui.tween.TweenFriction;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.CachedVertex;

public class PhyloNode extends CachedVertex implements Comparable
{
	public double unscaledX,unscaledY;
	public float x,y;
	public double unitTextWidth;
	public boolean drawMe;
	
	private int state = 0;
	public static final int NONE = 0;
	public static final int CUT = 1;
	public static final int COPY = 2;
	
	static TweenFriction fric = TweenFriction.tween(0.3f * PhyloWidget.TWEEN_FACTOR);
	
	static final float mult = 10000f;
	
	public Tween xTween;
	public Tween yTween;
	
	public PhyloNode(Object o)
	{
		super(o);
		xTween = new Tween(null,fric,Tween.OUT,0f,0f,0f);
		yTween = new Tween(null,fric,Tween.OUT,0f,0f,0f);
	}
	
	public void update()
	{
		xTween.update();
		yTween.update();
		unscaledX = xTween.getPosition()/mult;
		unscaledY = yTween.getPosition()/mult;
	}
	
	public void setUnscaledPosition(float x, float y)
	{
		xTween.continueTo(x*mult);
		yTween.continueTo(y*mult);
	}
	
	public float getTargetX()
	{
		return xTween.getFinish()/mult;
	}
	
	public float getTargetY()
	{
		return yTween.getFinish()/mult;
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
}
