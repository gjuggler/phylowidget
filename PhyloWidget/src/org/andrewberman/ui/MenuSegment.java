package org.andrewberman.ui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.phylowidget.render.Point;

public abstract class MenuSegment
{
	protected Object o;
	protected String f;
	
	protected ArrayList subSegments;
	
	protected int state = UP;
	public static final int HIDDEN = -1;
	public static final int UP = 0;
	public static final int OVER = 1;
	public static final int DOWN = 2;
	protected boolean clickedInside;
	
	public MenuSegment(Object object, String function)
	{
		this.o = object;
		this.f = function;
		
		subSegments = new ArrayList(2);
	}
	
	public void draw()
	{
		for (int i=0; i < subSegments.size(); i++)
		{
			MenuSegment seg = (MenuSegment)subSegments.get(i);
			seg.draw();
		}
	}
	
	public void drawUnder()
	{
		for (int i=0; i < subSegments.size(); i++)
		{
			MenuSegment seg = (MenuSegment)subSegments.get(i);
			seg.draw();
		}
	}
	
	public void layout()
	{
		for (int i=0; i < subSegments.size(); i++)
		{
			MenuSegment seg = (MenuSegment)subSegments.get(i);
			seg.layout();
		}
	}
	
	public abstract void getRect(Rectangle2D.Float rect, Rectangle2D.Float buff);
	
	public void performAction()
	{
		if (this.subSegments.size() > 0)
		{
			
		}
		
		try
		{
			Method m = o.getClass().getDeclaredMethod(f, null);
			m.invoke(o, null);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public abstract boolean containsPoint(Point p);
	
	protected static Point segPt = new Point(0,0);
	public void mouseEvent(MouseEvent e)
	{
		segPt.setLocation(e.getX(),e.getY());
		ProcessingUtils.screenToModel(segPt);
		
		boolean containsPoint = this.containsPoint(segPt); 
		switch (e.getID())
		{
			case MouseEvent.MOUSE_MOVED:
				if (containsPoint)
				{
					this.state = MenuSegment.OVER;
				} else
				{
					this.state = MenuSegment.UP;
				}
				break;
			case MouseEvent.MOUSE_PRESSED:
				if (containsPoint) clickedInside = true;
				else clickedInside = false;
			case MouseEvent.MOUSE_DRAGGED:
				if (clickedInside && containsPoint) this.state = MenuSegment.DOWN;
				else if (containsPoint) this.state = MenuSegment.OVER;
				else this.state = MenuSegment.UP;
				break;
			case MouseEvent.MOUSE_RELEASED:
				if (containsPoint)
				{
					performAction();
					this.state = MenuSegment.OVER;
				} else this.state = MenuSegment.UP;
			default:
				break;
		}

		for (int i=0; i < subSegments.size(); i++)
		{
			MenuSegment seg = (MenuSegment)subSegments.get(i);
			seg.mouseEvent(e);
		}
	}

	public void keyEvent(KeyEvent e)
	{
		for (int i=0; i < subSegments.size(); i++)
		{
			MenuSegment seg = (MenuSegment)subSegments.get(i);
			seg.keyEvent(e);
		}
	}
}
