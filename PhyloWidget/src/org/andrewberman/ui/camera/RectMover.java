/**************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PhyloWidget.  If not, see <http://www.gnu.org/licenses/>.
 */
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
	
	float border = 100;
	
	public RectMover(PApplet app, SettableRect r)
	{
		super(app);
		this.r = r;
		
		wTween = new Tween(null, TweenQuad.tween, Tween.OUT, 1f, 1f, FRAMES);
		hTween = new Tween(null, TweenQuad.tween, Tween.OUT, 1f, 1f, FRAMES);

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
		return w / (float)p.width;
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
			r.setRect(-cx * getZ() - w/2.0f, -cy * getZ() - h/2.0f, w, h);
	}

	private void updateConvenienceVariables()
	{	
		/*
		 * Set the convenience variables.
		 */
		cx = xTween.getPosition();
		cy = yTween.getPosition();
		w = wTween.getPosition();
		h = hTween.getPosition();
	}
	
	private void constrainToScreen()
	{
		if (!this.constrainToScreen) return;
		
		border = Math.min(p.width*.9f,p.height*.9f);
		
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
		
		float minZoom = 0.2f;
		if (getZ() < minZoom)
		{
//			fillScreen();
			zoomTo(minZoom);
			xTween.fforward();
			yTween.fforward();
			wTween.fforward();
			hTween.fforward();
		}
//		System.out.println(getZ());
		
		updateConvenienceVariables();
	}
}
