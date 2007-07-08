package org.andrewberman.ui.menu;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.andrewberman.ui.Point;

public abstract class MenuItem
{
	public static final int UP = 0;
	public static final int OVER = 1;
	public static final int DOWN = 2;

	static MenuColorSet colors = MenuColorSet.defaultSet;
	static MenuTimer timer = MenuTimer.instance;
	
	public Menu menu;
	public MenuItem parent;
	Object o;
	Method m;
	ArrayList items;
	boolean clickedInside;
	boolean mouseInside;
	boolean hidden = true;
	boolean showingChildren;
	boolean hoverToggle = true;
	int state = UP;	
	
	public MenuItem()
	{
		items = new ArrayList(2);
	}
	
	public void setAction(Object object, String method)
	{
		this.o = object;
		if (method != null && !method.equals("") && o != null)
		{
			try
			{
				m = o.getClass().getMethod(method, null);
			} catch (SecurityException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean isVisible()
	{
		if (hidden)
			return false;
		return true;
	}
	
	public void hide()
	{
		hidden = true;
	}
	
	protected void hideChildren()
	{
		showingChildren = true;
		toggleChildren();
	}
	
	protected void hideAllChildren()
	{
		System.out.println("Hide all!");
		hideChildren();
		for (int i=0; i < items.size(); i++)
		{
			final MenuItem item = (MenuItem)items.get(i);
			item.hideAllChildren();
		}
	}
	
	protected void show()
	{
		hidden = false;
	}

	protected void showChildren()
	{
		showingChildren = false;
		toggleChildren();
	}
	
	protected void setOpenItem(MenuItem openMe)
	{
		for (int i=0; i < items.size(); i++)
		{
			final MenuItem item = (MenuItem)items.get(i);
			if (item == openMe)
				item.showChildren();
			else
				item.hideAllChildren();
		}
	}
	
	protected void toggle()
	{
		if (isVisible())
			hide();
		else
			show();
	}
	
	protected void toggleChildren()
	{
		for (int i=0; i < items.size(); i++)
		{
			MenuItem seg = (MenuItem)items.get(i);
			if (showingChildren)
			{
				seg.hide();
			} else
			{
				seg.show();
			}
		}	
		showingChildren = !showingChildren;
	}
	
	public void draw()
	{
		for (int i=0; i < items.size(); i++)
		{
			MenuItem seg = (MenuItem)items.get(i);
			seg.draw();
		}
	}
	
	protected void layout()
	{
		for (int i=0; i < items.size(); i++)
		{
			MenuItem seg = (MenuItem)items.get(i);
			seg.layout();
		}
	}
	
	public void add(MenuItem seg)
	{
		items.add(seg);
		seg.setParent(this);
		seg.setMenu(menu);
		if (menu != null) menu.layout();
	}
	
	protected void setMenu(Menu menu)
	{
		this.menu = menu;
		for (int i=0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem)items.get(i);
			item.setMenu(menu);
		}
	}
	
	protected void setParent(MenuItem item)
	{
		parent = item;
	}
	
	protected void performAction()
	{
		if (items.size() > 0)
		{
			toggleChildren();
		} else
		{
			if (m == null || o == null) return;
			try
			{
				m.invoke(o, null);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Subclasses should return true if the point is contained within their
	 * mouse-responsive area.
	 * @param p a Point (in model coordinates) representing the mouse.
	 * @return true if this MenuItem contains the point, false if not.
	 */
	abstract protected boolean containsPoint(Point p);
	
	/**
	 * Subclasses should union their bounding rectangle with the Rectangle
	 * passed in as the rect parameter.
	 * 
	 * @param rect The rectangle with which to union this MenuItem's rectangle.
	 * @param buff A buffer Rectangle2D object, to be used for anything.
	 */
	protected void getRect(Rectangle2D.Float rect, Rectangle2D.Float buff)
	{
		for (int i=0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem)items.get(i);
			item.getRect(rect, buff);
		}
	}
	
	protected void mouseEvent(MouseEvent e, Point tempPt)
	{
		mouseInside = false;
		
		if (this.isVisible())
			visibleMouseEvent(e,tempPt);
		
		for (int i=0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem)items.get(i);
			item.mouseEvent(e,tempPt);
			if (item.mouseInside)
				mouseInside = true;
		}
	}

	protected void setState(int state)
	{
		if (this.state == state) return;
		this.state = state;
		
		if (hoverToggle)
		{
			if (state == MenuItem.OVER)
				timer.setMenuItem(this);
			else if (state == MenuItem.UP)
				timer.unsetMenuItem(this);
		}
	}
	
	protected void visibleMouseEvent(MouseEvent e, Point tempPt)
	{
		boolean containsPoint = containsPoint(tempPt);
		if (containsPoint)
			mouseInside = true;
		switch (e.getID())
		{
			case MouseEvent.MOUSE_MOVED:
				if (containsPoint)
				{
					setState(MenuItem.OVER);
				} else
				{
					setState(MenuItem.UP);
				}
				break;
			case MouseEvent.MOUSE_PRESSED:
				if (containsPoint) clickedInside = true;
				else clickedInside = false;
			case MouseEvent.MOUSE_DRAGGED:
				if (/*clickedInside && */containsPoint) setState(MenuItem.DOWN);
//				else if (containsPoint) this.state = MenuItem.OVER;
				else setState(MenuItem.UP);
				break;
			case MouseEvent.MOUSE_RELEASED:
				if (containsPoint)
				{
//					if (clickedInside)
						performAction();
					setState(MenuItem.OVER);
				} else setState(MenuItem.UP);
			default:
				break;
		}
	}
		
	protected void keyEvent(KeyEvent e)
	{
		for (int i=0; i < items.size(); i++)
		{
			MenuItem seg = (MenuItem)items.get(i);
			seg.keyEvent(e);
		}
	}
}