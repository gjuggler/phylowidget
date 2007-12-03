package org.andrewberman.ui.tools;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.Shortcut;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.camera.Camera;
import org.andrewberman.ui.tween.Tween;
import org.andrewberman.ui.tween.TweenFriction;

import processing.core.PApplet;

public class Scroll extends Tool
{
	Cursor cursor;
	Cursor draggingCursor;
	
	float camDownX,camDownY;
	Tween xTween,yTween;
	
	public Scroll(PApplet p)
	{
		super(p);
		
		shortcut = new Shortcut("s");
		xTween = new Tween(null,TweenFriction.tween(0.8f),Tween.OUT,0,0,30);
		yTween = new Tween(null,TweenFriction.tween(0.8f),Tween.OUT,0,0,30);
	}
	
	public void draw()
	{
		xTween.update();
		yTween.update();
		if (xTween.isTweening() || yTween.isTweening())
		{
			getCamera().nudgeTo(camDownX - xTween.getPosition(),camDownY - yTween.getPosition());
			getCamera().fforward();
		}
		if (mouseDragging)
		{
			float dx = curPoint.x - downPoint.x;
			float dy = curPoint.y - downPoint.y;
			dx /= getCamera().getZ();
			dy /= getCamera().getZ();
			xTween.continueTo(dx);
			yTween.continueTo(dy);
		}
	}
	
	@Override
	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		super.mouseEvent(e, screen, model);
		switch(e.getID())
		{
			case (MouseEvent.MOUSE_PRESSED):
				UIUtils.setBaseCursor(draggingCursor);
			
				break;
			case (MouseEvent.MOUSE_RELEASED):
				UIUtils.setBaseCursor(cursor);
				break;
		}
	}
	
	@Override
	void pressReset(MouseEvent e, Point screen, Point model)
	{
		camDownX = getCamera().getX();
		camDownY = getCamera().getY();
		xTween.continueTo(0);
		yTween.continueTo(0);
		xTween.fforward();
		yTween.fforward();
	}
	
	@Override
	public void enter()
	{
		super.enter();
		xTween.continueTo(0);
		yTween.continueTo(0);
		xTween.fforward();
		yTween.fforward();
	}
	
	public Cursor getCursor()
	{
		if (cursor == null)
		{
			cursor = createCursor("cursors/grab.png",6,6);
			draggingCursor = createCursor("cursors/grabbing.png",6,6);
		}
		return cursor;
	}

	public boolean respondToOtherEvents()
	{
		return false;
	}
	
}
