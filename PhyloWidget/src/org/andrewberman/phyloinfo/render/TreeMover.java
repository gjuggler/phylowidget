package org.andrewberman.phyloinfo.render;

import java.awt.geom.Rectangle2D;

import org.andrewberman.camera.Camera;
import org.andrewberman.tween.Tween;
import org.andrewberman.tween.TweenQuad;

import processing.core.PApplet;

/*
 * A tweaked camera, for use by the TreeRenderer class and subclasses, in order to allow a renderer to smoothly move
 * around the drawing area, scaling by width and height as necessary.
 */
public class TreeMover extends Camera
{
	protected PApplet p;
	protected TreeRenderer r;

	protected Tween wTween;
	protected Tween hTween;

	protected boolean forceAspectRatio = true;
	
	public TreeMover(PApplet app, TreeRenderer r)
	{
		super();
		this.p = app;
		this.r = r;

		wTween = new Tween(this, TweenQuad.instance, "out", 1f, 1f, FRAMES,
				false);
		hTween = new Tween(this, TweenQuad.instance, "out", 1f, 1f, FRAMES,
				false);
	}

	public void zoomCenterTo(float x, float y, float w, float h)
	{
		xTween.continueTo((float) x, FRAMES);
		yTween.continueTo((float) y, FRAMES);
		wTween.continueTo((float) w, FRAMES);
		hTween.continueTo((float) h, FRAMES);
	}

	public void fillScreen()
	{
		zoomCenterTo(-p.width/2,-p.height/2,p.width,p.height);
	}
	
	public void updatePosition()
	{
		/*
		 * We won't call super.updatePosition() here, because we don't want to 
		 * update the zTween if we don't have to...
		 */
		xTween.update();
		yTween.update();
		wTween.update();
		hTween.update();
		
		// Set our associated renderer's rectangle.
		r.setRect(getX(), getY(), wTween.position, hTween.position);
	}

}
