package org.andrewberman.ui.menu;

import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import org.andrewberman.tween.PropertyTween;
import org.andrewberman.tween.Tween;
import org.andrewberman.tween.TweenFriction;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIUtils;

import processing.core.PApplet;
import processing.core.PConstants;

public class RadialMenu extends Menu
{
	public float thetaLo = 0;
	public float thetaHi = PConstants.TWO_PI;
	float innerRadius = 10;
	float radius = 30;

	Ellipse2D.Float inner = new Ellipse2D.Float(0, 0, 0, 0);
	Ellipse2D.Float outer = new Ellipse2D.Float(0, 0, 0, 0);
	Ellipse2D.Float max = new Ellipse2D.Float(0, 0, 0, 0);

	AffineTransform buffTransform, mouseTransform;

	public RadialMenu(PApplet p)
	{
		super(p);
	}

	protected void setOptions()
	{
		clickAwayBehavior = Menu.CLICKAWAY_HIDES;
		hoverNavigable = false;
		clickToggles = true;
		autoDim = true;
		useCameraCoordinates = true;
	}

	public void setPosition(float x, float y)
	{
		super.setPosition(x, y);
		layout();
	}

	public void setRadius(float r)
	{
		this.radius = r;
	}

	public void setRadii(float inner, float outer)
	{
		this.innerRadius = inner;
		this.radius = outer;
		layout();
	}

	public MenuItem create(String s)
	{
		/*
		 * Attempt to automatically find a good hint character to use with the
		 * new RadialMenuItem
		 */
		boolean foundGoodChar = false;
		int charInd = 0;
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (!alreadyContainsChar(c) && Character.isLetter(c))
			{
				foundGoodChar = true;
				charInd = i;
				break;
			}
		}
		if (!foundGoodChar)
		{
			charInd = 0; // Oh well, let's just use the first char again.
		}
		return create(s, s.charAt(charInd));
	}

	protected boolean alreadyContainsChar(char c)
	{
		for (int i = 0; i < items.size(); i++)
		{
			RadialMenuItem rmi = (RadialMenuItem) items.get(i);
			if (rmi.alreadyContainsChar(c))
				return true;
		}
		return false;
	}

	public RadialMenuItem create(String s, char c)
	{
		return new RadialMenuItem(s, c);
	}

	public void setArc(float thetaLo, float thetaHi)
	{
		this.thetaLo = thetaLo;
		this.thetaHi = thetaHi;
		layout();
	}

	public void layout()
	{
		if (items.size() == 0)
			return;

		float dTheta = thetaHi - thetaLo;
		float thetaStep = dTheta / items.size();
		float start = thetaLo; // -PConstants.HALF_PI;
		for (int i = 0; i < items.size(); i++)
		{
			RadialMenuItem seg = (RadialMenuItem) items.get(i);
			seg.setPosition(x, y);
			float curTheta = start + i * thetaStep;
			seg.layout(innerRadius, radius, curTheta, curTheta + thetaStep);
		}

	}

	float getMaxVisibleRadius()
	{
		/*
		 * Set the maxRadius.
		 */
		float maxRadius = radius;
		for (int i=0; i < items.size(); i++)
		{
			RadialMenuItem rmi = (RadialMenuItem) items.get(i);
			float cur = rmi.getMaxRadius();
			if (cur > maxRadius)
				maxRadius = cur;
		}
		return maxRadius;
	}
	
	// public boolean containsPoint(Point pt)
	// {
	// outer.setFrameFromCenter(x,y,x-radius,y-radius);
	// return outer.contains(pt);
	// }

	public void itemMouseEvent(MouseEvent e, Point pt)
	{
		super.itemMouseEvent(e, pt);

		float maxRadius = getMaxVisibleRadius();
		
		inner.setFrameFromCenter(x, y, x - innerRadius, y - innerRadius);
		outer.setFrameFromCenter(x, y, x - maxRadius, y - maxRadius);
		float outerLimit = Math.max(5*radius,maxRadius+25);
		max.setFrameFromCenter(x, y, x - outerLimit, y - outerLimit);
		boolean in = inner.contains(pt.x, pt.y);
		boolean out = outer.contains(pt.x, pt.y);
		boolean inMax = max.contains(pt.x, pt.y);
		if (mouseInside)
		{
			aTween.continueTo(1f);
			aTween.fforward();
			return;
		}
		if (out)
		{
			aTween.continueTo(1f);
			aTween.fforward();
		} else if (inMax) // if we're outside the visible boundary.
		{
			// Fade out as we move further away.
			float diff = (float) max.getWidth() / 2 - (float) pt.distance(x, y);
			float ratio = diff
					/ ((float) max.getWidth() / 2 - (float) outer.getWidth() / 2);
			float intDst = (float) Math.min((ratio), 1f);
			aTween.continueTo(intDst);
			aTween.fforward();
		} else
		{
			hide();
		}
	}
}
