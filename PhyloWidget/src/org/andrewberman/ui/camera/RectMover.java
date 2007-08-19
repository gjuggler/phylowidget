package org.andrewberman.ui.camera;

import org.andrewberman.ui.tween.Tween;
import org.andrewberman.ui.tween.TweenQuad;

import processing.core.PApplet;

/*
 * A tweaked camera, for use by the TreeRenderer class and subclasses, in order to allow a renderer to smoothly move
 * around the drawing area, scaling by width and height as necessary.
 */
public class RectMover extends MovableCamera
{
	protected SettableRect r;

	protected Tween wTween;
	protected Tween hTween;

	/**
	 * Convenience variables, storing the current position of each of the tweens.
	 */
	float cx, cy, w, h = 0;

	protected boolean constrainToScreen = true;
	
	final float border = 200;
	
	public RectMover(PApplet app, SettableRect r)
	{
		super(app);
		this.r = r;
		
		wTween = new Tween(this, TweenQuad.tween, Tween.OUT, 1f, 1f, FRAMES);
		hTween = new Tween(this, TweenQuad.tween, Tween.OUT, 1f, 1f, FRAMES);

		/**
		 * Kind of important: call update() to make sure nothing here is null
		 * in case some mouse events happen before stuff is finished loading.
		 */
		update();
	}

	public void zoomBy(float factor)
	{
		float newW = wTween.getFinish() * factor;
		float newH = hTween.getFinish() * factor;
		zoomCenterTo(cx, cy, newW, newH);
	}

	public void zoomTo(float z)
	{
		wTween.continueTo(p.width * z);
		hTween.continueTo(p.height * z);
	}
	
	/**
	 * cx and cy are the CENTER coordinates of this TreeMover, in order to make
	 * it more closely resemble a camera.
	 */
	public void zoomCenterTo(float cx, float cy, float w, float h)
	{
		xTween.continueTo((float) cx);
		yTween.continueTo((float) cy);
		wTween.continueTo((float) w);
		hTween.continueTo((float) h);
	}

	public void fforward()
	{
		super.fforward();
		wTween.fforward();
		hTween.fforward();
	}
	
	public void fillScreen()
	{
		zoomCenterTo(0, 0, p.width, p.height);
	}
	
	public float getZ()
	{
		return w / p.width;
	}

	public void update()
	{
		/*
		 * No super.update() because we're updating all the necessary tweens on our own.
		 */

		super.scroll();

		xTween.update();
		yTween.update();
		wTween.update();
		hTween.update();
		
		updateConvenienceVariables();
		constrainToScreen();
		
		// Set our associated object's rectangle.
		if (r != null)
			r.setRect(-cx * getZ() - w/2, -cy * getZ() - h/2, w, h);
	}

	private void updateConvenienceVariables()
	{	
		/*
		 * Set the convenience variables.
		 */
		cx = xTween.position;
		cy = yTween.position;
		w = wTween.position;
		h = hTween.position;
	}
	
	private void constrainToScreen()
	{
		if (!this.constrainToScreen) return;
		
		float effectiveWidth = Math.max(10,p.width - border);
		float effectiveHeight = Math.max(10,p.height - border);
		
		float oX = (w - effectiveWidth)/2 - cx*getZ();
		if (oX < 0)
		{
			xTween.continueTo((w - effectiveWidth)/2 / getZ());
			xTween.fforward();
		} else if (oX > (w - effectiveWidth))
		{
			xTween.continueTo(-(w - effectiveWidth)/2 / getZ());
			xTween.fforward();
		}
		
		float oY = (h - effectiveHeight)/2 - cy*getZ();
		if (oY < 0)
		{
			yTween.continueTo((h - effectiveHeight)/2 / getZ());
			yTween.fforward();
		} else if (oY > (h - effectiveHeight))
		{
			yTween.continueTo(-(h - effectiveHeight)/2 / getZ());
			yTween.fforward();
		}
		
		
		/**
		 * Make the rectangle never shrink below the stage size.
		 */
		if (w < effectiveWidth || h < effectiveHeight)
		{
			xTween.continueTo(0);
			yTween.continueTo(0);
			wTween.continueTo(effectiveWidth);
			hTween.continueTo(effectiveHeight);
			xTween.fforward();
			yTween.fforward();
			wTween.fforward();
			hTween.fforward();
		}
		
		updateConvenienceVariables();
	}
}
