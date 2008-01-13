package org.andrewberman.ui.camera;

import org.andrewberman.ui.tween.Tween;
import org.andrewberman.ui.tween.TweenFriction;
import org.andrewberman.ui.tween.TweenListener;
import org.andrewberman.ui.tween.TweenQuad;

import processing.core.PApplet;

public class Camera
{
	protected Tween xTween;
	protected Tween yTween;
	protected Tween zTween;

	protected int FRAMES = 15;

	protected PApplet p;
	
	public Camera(PApplet p)
	{
		this.p = p;
		xTween = new Tween(null, TweenFriction.tween(.2f), Tween.OUT, 0, 0, 0);
		yTween = new Tween(null, TweenFriction.tween(.2f), Tween.OUT, 0, 0, 0);
		zTween = new Tween(null, TweenFriction.tween(.2f), Tween.OUT, 1f, 1f, 0);
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

	protected void applyTransformations()
	{
		/*
		 * Translate by half the stage width and height to re-center the stage
		 * at (0,0).
		 */
		p.translate(getStageWidth() / 2.0f, getStageHeight() / 2.0f);
		/*
		 * Now scale.
		 */
		p.scale(getZ());
		/*
		 * Then translate.
		 */
		p.translate(-getX(), -getY());
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
		return p.width;
	}

	public float getStageHeight()
	{
		return p.height;
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
		applyTransformations();
	}
}