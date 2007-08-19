package org.andrewberman.camera;

import org.andrewberman.ui.tween.Tween;
import org.andrewberman.ui.tween.TweenListener;
import org.andrewberman.ui.tween.TweenQuad;

public abstract class Camera implements TweenListener
{
	protected Tween xTween;
	protected Tween yTween;
	protected Tween zTween;

	protected int FRAMES = 15;

	public Camera()
	{
		xTween = new Tween(this, TweenQuad.tween, Tween.OUT, 0, 0, FRAMES);
		yTween = new Tween(this, TweenQuad.tween, Tween.OUT, 0, 0, FRAMES);
		zTween = new Tween(this, TweenQuad.tween, Tween.OUT, 1f, 1f, FRAMES * 2);
	}

	/*
	 * Zoom and center to the rectangle provided by the coordinates.
	 * X and Y represent the center of the rectangle.
	 */
	public void zoomCenterTo(float centerX, float centerY, float w, float h)
	{
		float xAspect = w / getStageWidth();
		float yAspect = h / getStageHeight();
		if (xAspect > yAspect)
		{
			zTween.continueTo(1.0f / xAspect);
			xTween.continueTo(centerX / xAspect);
			yTween.continueTo(centerY / xAspect);
		} else
		{
			zTween.continueTo(1 / yAspect);
			yTween.continueTo(centerY / yAspect);
			xTween.continueTo(centerX / yAspect);
		}
	}

	public void zoomBy(float factor)
	{
		zTween.continueTo(zTween.getFinish() * factor);
	}

	public void zoomTo(float z)
	{
		zTween.continueTo(z);
	}
	
	public void nudge(float dx, float dy)
	{
		nudgeTo(xTween.getFinish() + dx, yTween.getFinish() + dy);
	}

	public void nudgeTo(float x, float y)
	{
		xTween.continueTo(x);
		yTween.continueTo(y);
	}

	public void skipTo(float x, float y)
	{
		nudgeTo(x,y);
		xTween.fforward();
		yTween.fforward();
	}
	
	public void fforward()
	{
		xTween.fforward();
		yTween.fforward();
		zTween.fforward();
	}
	
	/*
	 * These methods should be overridden with something that makes sense.
	 */
	public float getStageWidth()
	{
		return 100;
	}

	public float getStageHeight()
	{
		return 100;
	}

	public float getX()
	{
		return xTween.getPosition();
	}

	public float getY()
	{
		return yTween.getPosition();
	}

	/**
	 * Float value ranging from 0.0 (all the way out) to infinity (all the way in).
	 * Standard zoom is 1.0.
	 * @return
	 */
	public float getZ()
	{
		return zTween.getPosition();
	}

	public void update()
	{
		xTween.update();
		yTween.update();
		zTween.update();
	}
	
	public void tweenEvent(Tween t, int type)
	{
		// Do nothing.
	}
}