package org.andrewberman.ui.tools;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.Shortcut;
import org.andrewberman.ui.camera.Camera;
import org.andrewberman.ui.tween.Tween;
import org.andrewberman.ui.tween.TweenFriction;

import processing.core.PApplet;

public class Zoom extends Tool
{
	Cursor zoomCursor;
	
	float targetX, targetY;
	double downZoom, zoomFactor;
	float downCameraX, downCameraY;

	Tween zoomTween;
	
	boolean isScrolling;
	
	public Zoom(PApplet p)
	{
		super(p);
		
		shortcut = new Shortcut("z");
		zoomTween = new Tween(null,TweenFriction.tween(0.3f),Tween.OUT,1,1,30);
	}

	public void draw()
	{
		zoomTween.update();
		if (mouseDragging)
		{
			float zoomDist = downPoint.y - curPoint.y;
			zoomFactor = downZoom * Math.exp(zoomDist/100f);
			zoomTween.continueTo((float) zoomFactor);
		}
		if (zoomTween.isTweening())
		{
			/*
			 * Update the new center point and set it. This calculation gets a
			 * little annoying; just trust me here.
			 */
			double dx = targetX / downZoom;
			double dy = targetY / downZoom;
			// ratio of current zoom to the original zoom.
			double zoomRatio = downZoom / zoomTween.getPosition();
			// new distances from down point to center.
			float newX = (float) (downCameraX + dx - dx * zoomRatio);
			float newY = (float) (downCameraY + dy - dy * zoomRatio);
			Camera cam = getCamera();
			cam.zoomTo(zoomTween.getPosition());
			cam.nudgeTo(newX, newY);
			cam.fforward();
		}
	}
	
	@Override
	public void enter()
	{
		super.enter();
		reset();
	}
	
	void pressReset(MouseEvent e, Point screen, Point model)
	{
		reset();
	}
	
	void reset()
	{
		targetX = downPoint.x - p.width / 2;
		targetY = downPoint.y - p.height / 2;
		downCameraX = getCamera().getX();
		downCameraY = getCamera().getY();
		downZoom = getCamera().getZ();
		zoomFactor = getCamera().getZ();
		zoomTween.continueTo((float) zoomFactor);
		zoomTween.fforward();
	}

	public Cursor getCursor()
	{
		if (zoomCursor == null)
		{
			zoomCursor = createCursor("cursors/zoom2.png", 6, 6);
		}
		return zoomCursor;
	}

	public boolean respondToOtherEvents()
	{
		return false;
	}
	
	public void keyEvent(KeyEvent e)
	{
		int code = e.getKeyCode();
	}
}
