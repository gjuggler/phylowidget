package org.andrewberman.ui.tools;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.Shortcut;
import org.andrewberman.ui.camera.Camera;

import processing.core.PApplet;

public class Zoom extends Tool
{
	Cursor zoomCursor;
	
	float targetX, targetY;
	float downZoom, zoomFactor;
	float downCameraX, downCameraY;

	public Zoom(PApplet p)
	{
		super(p);
		
		shortcut = new Shortcut("z");
	}

	public void draw()
	{
		if (mouseDragging)
		{
			Camera cam = getCamera();
			/*
			 * Update the zoom factor and set the new zoom level.
			 */
			float zoomDist = downPoint.y - curPoint.y;
//			System.out.println(zoomDist + "  " + zoomFactor);
			zoomFactor *= (1 + zoomDist / 100 / 10);
//			cam.zoomTo(zoomFactor);
			/*
			 * Update the new center point and set it. This calculation gets a
			 * little annoying; just trust me here.
			 */
			// original distances from down point to center.
			float dx = targetX / downZoom;
			float dy = targetY / downZoom;
			// ratio of current zoom to the original zoom.
			float zoomRatio = downZoom / zoomFactor;
//			System.out.println(zoomRatio);
			// new distances from down point to center.
			float newX = downCameraX + dx - dx * zoomRatio;
			float newY = downCameraY + dy - dy * zoomRatio;
			cam.zoomTo(zoomFactor);
			cam.nudgeTo(newX, newY);
			
			cam.fforward();
		}
	}

	void pressReset(MouseEvent e, Point screen, Point model)
	{
		targetX = downPoint.x - p.width / 2;
		targetY = downPoint.y - p.height / 2;
		downCameraX = getCamera().getX();
		downCameraY = getCamera().getY();
		downZoom = getCamera().getZ();
		zoomFactor = getCamera().getZ();
	}

	public Cursor getCursor()
	{
		if (zoomCursor == null)
		{
			zoomCursor = createCursor(p, "cursors/zoom.png", 6, 6);
		}
		return zoomCursor;
	}
}
