package org.andrewberman.camera;

import org.andrewberman.tween.Tween;
import org.andrewberman.tween.TweenListener;
import org.andrewberman.tween.TweenQuad;

public abstract class Camera implements TweenListener
{

	protected Tween xTween;
	protected Tween yTween;
	protected Tween zTween;

	protected int FRAMES = 15;

	public Camera()
	{
		xTween = new Tween(this, TweenQuad.instance, "out", 0, 0, FRAMES, false);
		yTween = new Tween(this, TweenQuad.instance, "out", 0, 0, FRAMES, false);
		zTween = new Tween(this, TweenQuad.instance, "out", 1f, 1f, FRAMES * 2,
				false);
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

	public void nudge(float dx, float dy)
	{
		nudgeTo(xTween.getFinish() + dx, yTween.getFinish() + dy);
	}

	public void nudgeTo(float x, float y)
	{
		xTween.continueTo(x);
		yTween.continueTo(y);
	}

	/*
	 * These methods should be overrideen with something that makes sense.
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