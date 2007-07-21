package org.andrewberman.ui.menu;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.ui.Action;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.Shortcut;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.ifaces.Positionable;
import org.andrewberman.ui.ifaces.Sizable;

import processing.core.PFont;

/**
 * The <code>MenuItem</code> class is the base class for ALL objects in the
 * <code>Menu</code> package. Its main purpose is to provide the recursive
 * functions necessary for managing a tree-like menu structure.
 * <p>
 * If you are interested in designing a new type of menu based on this
 * structure, then you need to (a) create a <code>Menu</code> subclass and (b)
 * create a <code>MenuItem</code> subclass. Your new <code>Menu</code>
 * subclass will handle the root-level layout and logic handling, while your
 * <code>MenuItem</code> subclass should draw itself and layout any sub-items
 * it may have. See the examples within this package, all of which were designed
 * in this way.
 * <p>
 * 
 * @author Greg
 */
public abstract class MenuItem implements Positionable, Sizable
{
	public static final int UP = 0;
	public static final int OVER = 1;
	public static final int DOWN = 2;

	public static final int LAYOUT_BELOW = 0;
	public static final int LAYOUT_RIGHT = 1;
	public static final int LAYOUT_LEFT = 2;
	public static final int LAYOUT_ABOVE = 3;
	int layoutRule;

	static MenuTimer timer;

	public Menu menu;
	public Menu nearestMenu;
	public MenuItem parent;

	Action action;
	Shortcut shortcut;
	public String label;
	ArrayList items;

	protected float x, y;
	protected float width, height;

	int state = UP;
	boolean clickedInside;
	boolean mouseInside;
	boolean hidden = true;

	MenuItem()
	{
		this("[Unnamed MenuItem]");
	}

	MenuItem(String label)
	{
		timer = MenuTimer.instance();
		items = new ArrayList(2);
		this.label = label;
	}

	public void setAction(Object object, String method)
	{
		action = new Action(object, method);
		if (shortcut != null)
			shortcut.action = action;
	}

	public void setShortcut(String s)
	{
		shortcut = new Shortcut(s);
		if (action != null)
			shortcut.action = action;
		menu.layout();
	}

	public void setPosition(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public float getX()
	{
		return x;
	}

	public float getY()
	{
		return y;
	}

	public void setSize(float w, float h)
	{
		width = w;
		height = h;
	}

	public float getWidth()
	{
		return width;
	}

	public float getHeight()
	{
		return height;
	}

	public MenuItem add(MenuItem seg)
	{
		items.add(seg);
		seg.setParent(this);
		seg.setMenu(menu);
		if (menu != null)
			menu.layout();
		return seg;
	}

	public MenuItem add(String newLabel)
	{
		if (nearestMenu != null)
		{
			MenuItem item = nearestMenu.create(newLabel);
			add(item);
			return item;
		} else if (menu != null)
		{
			MenuItem item = menu.create(newLabel);
			add(item);
			return item;
		} else
		{
			throw new RuntimeException(
					"Error in MenuItem.add(String): This MenuItem is not associated with any menu!");
		}
	}

	public MenuItem get(String search)
	{
		if (label.equals(search))
			return this;
		else
		{
			for (int i = 0; i < items.size(); i++)
			{
				MenuItem mightBeNull = ((MenuItem) items.get(i)).get(search);
				if (mightBeNull != null)
					return mightBeNull;
			}
		}
		return null;
		// throw new RuntimeException("Unable to find MenuItem (label
		// \""+search+"\") within the specified MenuItem (label
		// \""+label+"\")");
	}

	/**
	 * Returns whether this MenuItem is visible or not. For example, an
	 * 
	 * @return
	 */
	public boolean isVisible()
	{
		if (hidden)
			return false;
		return true;
	}

	/**
	 * Returns whether this MenuItem is showing its children or not.
	 * 
	 * @return true if any of this MenuItem's children are showing.
	 */
	public boolean showingChildren()
	{
		for (int i = 0; i < items.size(); i++)
		{
			if (((MenuItem) items.get(i)).isVisible())
				return true;
		}
		return false;
	}

	/**
	 * Draws this MenuItem to the current root menu's PGraphics object.
	 */
	public void draw()
	{
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem seg = (MenuItem) items.get(i);
			seg.draw();
		}
	}

	/**
	 * Lays out this MenuItem and all of its sub-items.
	 */
	public void layout()
	{
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem seg = (MenuItem) items.get(i);
			seg.layout();
		}
	}

	boolean isAncestorOfSelected()
	{
		if (menu == null)
			return false;
		if (this == menu.lastHovered)
			return true;
		else if (isAncestorOf(menu.lastHovered))
			return true;
		return false;
	}

	protected boolean isAncestorOf(MenuItem child)
	{
		if (child == null)
			return false;
		else if (child.parent == this)
			return true;
		else
		{
			boolean found = false;
			for (int i = 0; i < items.size(); i++)
			{
				MenuItem item = (MenuItem) items.get(i);
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
		for (int i = 0; i < items.size(); i++)
		{
			((MenuItem) items.get(i)).hide();
		}
	}

	protected void hideAllChildren()
	{
		hideChildren();
		for (int i = 0; i < items.size(); i++)
		{
			final MenuItem item = (MenuItem) items.get(i);
			item.hideAllChildren();
		}
	}

	protected void show()
	{
		hidden = false;
	}

	protected void showChildren()
	{
		// We hide all children first to make sure any sub-submenus aren't
		// showing.
		hideAllChildren();
		for (int i = 0; i < items.size(); i++)
		{
			((MenuItem) items.get(i)).show();
		}
	}

	protected void setOpenItem(MenuItem openMe)
	{
		for (int i = 0; i < items.size(); i++)
		{
			final MenuItem item = (MenuItem) items.get(i);
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
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem seg = (MenuItem) items.get(i);
			if (showingChildren)
			{
				seg.hide();
			} else
			{
				seg.show();
			}
		}
	}

	protected void setMenu(Menu menu)
	{
		this.menu = menu;
		getNearestMenu();
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem) items.get(i);
			item.setMenu(menu);
		}
	}

	protected Menu getNearestMenu()
	{
		// if (nearestMenu != null) return nearestMenu;
		MenuItem item = this;
		while (item != null)
		{
			if (item instanceof Menu)
			{
				nearestMenu = (Menu) item;
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
			if (menu.hideOnAction)
				menu.hide();
			if (action != null)
				action.performAction();
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
				if (parent != null)
				{
					if (showingChildren())
						parent.setOpenItem(null);
					else
						parent.setOpenItem(this);
				}
			} else
				toggleChildren();
		}
	}

	/**
	 * Subclasses should return true if the point is contained within their
	 * mouse-responsive area.
	 * 
	 * @param p
	 *            a Point (in model coordinates) representing the mouse.
	 * @return true if this MenuItem contains the point, false if not.
	 */
	abstract protected boolean containsPoint(Point p);

	/**
	 * Subclasses should union their bounding rectangle with the Rectangle
	 * passed in as the rect parameter.
	 * 
	 * @param rect
	 *            The rectangle with which to union this MenuItem's rectangle.
	 * @param buff
	 *            A buffer Rectangle2D object, to be used for anything.
	 */
	protected void getRect(Rectangle2D.Float rect, Rectangle2D.Float buff)
	{
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem) items.get(i);
			item.getRect(rect, buff);
		}
	}

	/**
	 * Calculates the maximum width among this MenuItem's sub-items.
	 * 
	 * @return the maximum width of the MenuItems in the "items" arraylist.
	 */
	protected float getMaxWidth()
	{
		float max = 0;
		for (int i = 0; i < items.size(); i++)
		{
			float curWidth = ((MenuItem) items.get(i)).getTextWidth();
			if (curWidth > max)
				max = curWidth;
		}
		return max;
	}

	/**
	 * Determines the max width of this MenuItem's "content". Currently just
	 * returns the max width based on the width of the label text and the
	 * current Palette's padding, but subclasses can override this default
	 * behavior.
	 * 
	 * @return the maximum width of this MenuItem.
	 */
	protected float getTextWidth()
	{
		PFont font = menu.style.font;
		float fontSize = menu.style.fontSize;
		float width = UIUtils.getTextWidth(menu.buff, font, fontSize, label,
				true);
		return width + menu.style.padX * 2;
	}

	protected float getTextHeight()
	{
		PFont font = menu.style.font;
		float fontSize = menu.style.fontSize;
		return UIUtils.getTextHeight(menu.buff, font, fontSize, label, true)
				+ menu.style.padY * 2;
	}

	protected void itemMouseEvent(MouseEvent e, Point tempPt)
	{
		mouseInside = false;
		if (this.isVisible())
			visibleMouseEvent(e, tempPt);
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem) items.get(i);
			item.itemMouseEvent(e, tempPt);
			if (item.mouseInside)
				mouseInside = true;
		}
	}

	protected void setState(int state)
	{
		if (this.state == state)
			return;
		this.state = state;
		if (nearestMenu.hoverNavigable)
		{
			if (state == MenuItem.OVER || state == MenuItem.DOWN)
				timer.setMenuItem(this);
			else if (state == MenuItem.UP)
				timer.unsetMenuItem(this);
		}
		if (state == MenuItem.DOWN)
		{
			menu.lastPressed = this;
			menu.currentlyHovered = this;
			menu.lastHovered = this;
		} else if (state == MenuItem.OVER)
		{
			menu.lastHovered = this;
			menu.currentlyHovered = this;
		} else if (state == MenuItem.UP && menu.currentlyHovered == this)
		{
			menu.currentlyHovered = null;
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
				if (containsPoint)
				{
					clickedInside = true;
					if (nearestMenu.actionOnMouseDown)
						performAction();
				} else
					clickedInside = false;
			case MouseEvent.MOUSE_DRAGGED:
				if (/* clickedInside && */containsPoint)
					setState(MenuItem.DOWN);
				// else if (containsPoint) this.state = MenuItem.OVER;
				else
					setState(MenuItem.UP);
				break;
			case MouseEvent.MOUSE_RELEASED:
				if (containsPoint)
				{
					if (!nearestMenu.actionOnMouseDown)
						performAction();
					// setState(MenuItem.OVER);
				} else
					setState(MenuItem.UP);
			default:
				break;
		}
	}

	protected void keyEvent(KeyEvent e)
	{
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem seg = (MenuItem) items.get(i);
			seg.keyEvent(e);
		}
	}
}