package org.andrewberman.ui.menu;

import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.ProcessingUtils;

import processing.core.PFont;

public abstract class MenuItem
{
	public static final int UP = 0;
	public static final int OVER = 1;
	public static final int DOWN = 2;

	static MenuTimer timer = MenuTimer.instance;
	
	public Menu menu;
	public Menu nearestMenu;
	public MenuItem parent;
	
	Object o;
	Method m;
	public String label;
	ArrayList items;
	
	int state = UP;	
	boolean clickedInside;
	boolean mouseInside;
	boolean hidden = true;

	
	public MenuItem()
	{
		items = new ArrayList(2);
		label = "";
	}
	
	public MenuItem(String label)
	{
		this();
		this.label = label;
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
				e.printStackTrace();
			} catch (NoSuchMethodException e)
			{
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
	
	public boolean showingChildren()
	{
		for (int i=0; i < items.size(); i++)
		{
			if (((MenuItem)items.get(i)).isVisible())
				return true;
		}
		return false;
	}
	
	boolean isAncestorOfSelected()
	{
		if (menu == null) return false;
		if (this == menu.currentlySelected)
			return true;
		else if (isAncestorOf(menu.currentlySelected))
			return true;
		return false;
	}
	
	public boolean isAncestorOf(MenuItem child)
	{
		if (child == null) return false;
		else if (child.parent == this) return true;
		else {
			boolean found = false;
			for (int i=0; i < items.size(); i++)
			{
				MenuItem item = (MenuItem)items.get(i);
				if (item.isAncestorOf(child))
					found = true;
			}
			return found;
		}
	}
	
	protected void hide()
	{
		hidden = true;
	}
	
	protected void hideChildren()
	{
		for (int i=0; i < items.size(); i++)
		{
			((MenuItem)items.get(i)).hide();
		}
	}
	
	protected void hideAllChildren()
	{
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
		// We hide all children first to make sure any sub-submenus aren't showing.
		hideAllChildren();
		for (int i=0; i < items.size(); i++)
		{
			((MenuItem)items.get(i)).show();
		}
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
		final boolean showingChildren = showingChildren();
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
		getNearestMenu();
		for (int i=0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem)items.get(i);
			item.setMenu(menu);
		}
	}
	
	protected Menu getNearestMenu()
	{
//		if (nearestMenu != null) return nearestMenu;
		MenuItem item = this;
		while (item != null)
		{
			if (item instanceof Menu)
			{
				nearestMenu = (Menu)item;
				return nearestMenu;
			} else
				item = item.parent;
		}
		return null;
	}
	
	protected void setParent(MenuItem item)
	{
		parent = item;
	}
	
	protected void performAction()
	{
		if (items.size() > 0)
		{
			menuTriggerLogic();
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
	
	protected void menuTriggerLogic()
	{
		if (timer.item == this || !nearestMenu.clickToggles)
		{ 
			if (nearestMenu.singletNavigation)
				parent.setOpenItem(this);
			else
				showChildren();
		} else if (nearestMenu.clickToggles)
		{
			if (nearestMenu.singletNavigation)
			{
				if (showingChildren())
					parent.setOpenItem(null);
				else
					parent.setOpenItem(this);
			} else
				toggleChildren();
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
	
	/**
	 * Calculates the maximum width among this MenuItem's sub-items.
	 * @return the maximum width of the MenuItems in the "items" arraylist.
	 */
	protected float getMaxWidth()
	{
		float max = 0;
		for (int i=0; i < items.size(); i++)
		{
			float curWidth = ((MenuItem)items.get(i)).getWidth();
			if (curWidth > max)
				max = curWidth;
		}
		return max;
	}
	
	/**
	 * Determines the max width of this MenuItem's "content".
	 * Currently just returns the max width based on the width of the label
	 * text and the current Palette's padding, but subclassers should override
	 * this default behavior.
	 * @return the maximum width of this MenuItem.
	 */
	protected float getWidth()
	{
		PFont font = menu.style.font;
		float fontSize = menu.style.fontSize;
		float width = ProcessingUtils.getTextWidth(menu.pg,font, fontSize, label,true);
		return width + 2*menu.style.pad;
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
		if (nearestMenu.hoverNavigable)
		{
			if (state == MenuItem.OVER)
				timer.setMenuItem(this);
			else if (state == MenuItem.UP)
				timer.unsetMenuItem(this);
		}
		if (state == MenuItem.OVER || state == MenuItem.DOWN)
			menu.currentlySelected = this;
//		else if (state == MenuItem.UP && menu.currentlySelected == this)
//			menu.currentlySelected = null;
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