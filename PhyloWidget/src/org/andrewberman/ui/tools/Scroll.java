package org.andrewberman.ui.tools;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.Shortcut;
import org.andrewberman.ui.camera.Camera;

import processing.core.PApplet;

public class Scroll extends Tool
{
	Cursor cursor;
	
	public Scroll(PApplet p)
	{
		super(p);
		
		shortcut = new Shortcut("s");
	}
	
	public void draw()
	{
		if (mouseDragging)
		{
			float factor = 10;
			float dx = curPoint.x - downPoint.x;
			dx /= factor;
			float dy = curPoint.y - downPoint.y;
			dy /= factor;
			dx /= getCamera().getZ();
			dy /= getCamera().getZ();
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
