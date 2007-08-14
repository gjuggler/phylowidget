package org.phylowidget.ui;

import org.andrewberman.tween.Tween;
import org.andrewberman.tween.TweenFriction;
import org.andrewberman.tween.TweenListener;


public class PhyloNode implements Comparable
{
	PhyloNode parent;
	String label;
	public float unscaledX,unscaledY;
	public float x,y;
	public float unitTextWidth;
	
	public boolean hovered;
	private int state = 0;
	public static final int NONE = 0;
	public static final int CUT = 1;
	public static final int COPY = 2;
	
	static TweenFriction fric = TweenFriction.tween(0.2f);
	
	static final float MULT = 10000f;
	
	public Tween xTween;
	public Tween yTween;
	
	public PhyloNode(String label)
	{
		this.label = label;
		xTween = new Tween(null,fric,Tween.OUT,0f,0f,0f);
		yTween = new Tween(null,fric,Tween.OUT,0f,0f,0f);
	}
	
	public void update()
	{
		xTween.update();
		yTween.update();
		unscaledX = xTween.getPosition()/MULT;
		unscaledY = yTween.getPosition()/MULT;
	}
	
	public void setUnscaledPosition(float x, float y)
	{
		xTween.continueTo(x*MULT);
		yTween.continueTo(y*MULT);
	}
	
	public float getTargetX()
	{
		return xTween.getFinish()/MULT;
	}
	
	public float getTargetY()
	{
		return yTween.getFinish()/MULT;
	}
	
	public String toString()
	{
		return label;
	}
	
	public String getName()
	{
		return label;
	}
	
	public void setName(String s)
	{
		label = s;
	}
	
	public PhyloNode getParent()
	{
		return parent;
	}
	
	public int compareTo(Object o)
	{
		if (o instanceof PhyloNode)
		{
			PhyloNode that = (PhyloNode) o;
			float a = this.unscaledY;
			float b = that.unscaledY;
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
