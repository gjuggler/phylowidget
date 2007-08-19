package org.andrewberman.ui.tools;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.Shortcut;
import org.andrewberman.ui.camera.Camera;

import processing.core.PApplet;

public abstract class ScrollTool extends Tool
{
	Cursor cursor;
	
	public ScrollTool(PApplet p)
	{
		super(p);
		
		shortcut = new Shortcut("s");
	}
	
	public abstract Camera getCamera();
	
	public void draw()
	{
		if (mouseDragging)
		{
			float factor = 20;
			float dx = curPoint.x - downPoint.x;
			dx /= factor;
			float dy = curPoint.y - downPoint.y;
			dy /= factor;
			getCamera().nudge(dx,dy);
		}
	}
	
	public Cursor getCursor()
	{
		if (cursor == null)
		{
			cursor = createCursor(p,"cursors/move.png",6,6);
		}
		return cursor;
	}
	
}
