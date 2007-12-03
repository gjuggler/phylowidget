package org.andrewberman.ui.menu;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.ifaces.Positionable;
import org.andrewberman.ui.ifaces.Sizable;

import processing.core.PApplet;
import processing.core.PFont;

/**
 * A <code>Toolbar</code> is a menu that acts more or less like a
 * Windows-style toolbar. The two main options for the Toolbar class are:
 * <code>fullWidth</code>, which if set to true will draw itself across the
 * entire width of the screen, and <code>isModal</code>, which if set to true
 * will make the toolbar grab modal focus from the FocusManager when one of its
 * menus is opened.
 * 
 * @author Greg
 */
public class Toolbar extends Menu
{
	RoundRectangle2D.Float roundRect = new RoundRectangle2D.Float(0, 0, 0, 0,
			0, 0);
	Rectangle2D.Float buffRect = new Rectangle2D.Float(0, 0, 0, 0);

	/**
	 * If set to true, the toolbar will take up the entire width of the PApplet.
	 */
	public boolean fullWidth;
	/**
	 * If true, this option will cause the menu to grab modal focus from the
	 * FocusManager upon opening.
	 */
	public boolean isModal;

	public Toolbar(PApplet app)
	{
		super(app);
		layout();
		open();
	}

	protected boolean isActive()
	{
		return hasOpenChildren();
	}

	protected void setOptions()
	{
		super.setOptions();
		/*
		 * Override some default options from the Menu class.
		 */
		useCameraCoordinates = false;
		clickToggles = true;
		hoverNavigable = true;
		clickAwayBehavior = Menu.CLICKAWAY_COLLAPSES;
		// actionOnMouseDown = true;
		useHandCursor = true;
		autoDim = true;

		fullWidth = false;
		// isModal = true;
		/*
		 * Do some automatic positioning.
		 */
		if (fullWidth)
		{
			x = style.strokeWidth;
			y = style.strokeWidth;
		} else if (!useCameraCoordinates)
		{
			x = style.strokeWidth + style.margin;
			y = style.strokeWidth + style.margin;
		}
	}

	public void setX(float x)
	{
		this.x = x + style.strokeWidth;
	}

	public void setY(float y)
	{
		this.y = y + style.strokeWidth;
	}

	public void draw()
	{
		if (fullWidth)
			setFullWidth();
		super.draw();
	}

	public void drawBefore()
	{
		MenuUtils.drawDoubleGradientRect(this, x, y, width, height);
	}

	public MenuItem create(String name)
	{
		ToolbarItem ti = new ToolbarItem();
		ti.setLayoutMode(ToolbarItem.LAYOUT_BELOW);
		ti.drawChildrenTriangle = false;
		ti.setName(name);
		return ti;
	}

	protected void clickaway()
	{
		super.clickaway();
		if (isModal)
		{
			FocusManager.instance.removeFromFocus(this);
		}
	}

	public void layout()
	{
		float xOffset = style.padX;
		float yOffset = style.padY;

		for (int i = 0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem) items.get(i);
			item.calcPreferredSize();
			float itemWidth = item.width;
			if (item instanceof Positionable)
			{
				Positionable pos = (Positionable) item;
				pos.setPosition(x + xOffset, y + yOffset);
			}
			xOffset += itemWidth;
			/*
			 * I had been using the following line for adding padding between
			 * toolbar items, but it looks better without any space, so for now
			 * I am adding zero.
			 */
			if (i < items.size() - 1)
				xOffset += 0;
		}
		/*
		 * Set this Toolbar's width and height.
		 */
		if (fullWidth)
			setFullWidth();
		else
			width = xOffset + style.padX;
		float maxHeight = getMaxHeight();
		height = maxHeight + style.padY * 2;
		/*
		 * Trigger the recursive layout.
		 */
		super.layout();
	}

	void setFullWidth()
	{
		width = canvas.width - style.strokeWidth * 2;
	}

	protected void getRect(Rectangle2D.Float rect, Rectangle2D.Float buff)
	{
		super.getRect(rect, buff);
		if (isOpen())
		{
			buff.setRect(x, y, width, height);
			Rectangle2D.union(rect, buff, rect);
		}
	}

	protected void itemMouseEvent(MouseEvent e, Point pt)
	{
		super.itemMouseEvent(e, pt);
		if (isActive())
			aTween.continueTo(fullAlpha);
		// if (e.getID() == MouseEvent.MOUSE_PRESSED)
		// {
		// if (clickedInside)
		// System.out.println("HEY");
		// }
		// if (isActive() && isModal)
		// {
		// FocusManager.instance.setModalFocus(this);
		// }
	}

	@Override
	public void keyEvent(KeyEvent e)
	{
		super.keyEvent(e);
//		System.out.println("event");
//		if (hovered == null)
//		{
//			items.get(0).setState(MenuItem.OVER);
//		}
//		int code = e.getKeyCode();
//		int type = e.getID();
//		switch (code)
//		{
//			case (KeyEvent.VK_ENTER):
//				if (hovered.hasChildren())
//				{
//					hovered.open();
//					MenuItem i = hovered.items.get(0);
//					i.setState(MenuItem.OVER);
//					hovered.setState(MenuItem.UP);
//				} else
//				{
//					hovered.performAction();
//				}
//				break;
//			case (KeyEvent.VK_RIGHT):
////				System.out.println("RIGHT");
//				if (hovered.hasChildren())
//				{
//					hovered.open();
//					MenuItem i = hovered.items.get(0);
//					i.setState(MenuItem.OVER);
//					hovered.setState(MenuItem.UP);
//				} else
//				{
//					/*
//					 * TODO: Find currently open top-level menuitem, and move
//					 * one to the right. Shoudl wrap around to the left if
//					 * necessary.
//					 */
//					MenuItem p = hovered.parent;
//					int index = p.items.indexOf(hovered);
//					p.items.get(index+1).setState(MenuItem.OVER);
//					hovered.setState(MenuItem.UP);
//				}
//				break;
//		}
	}

	protected boolean containsPoint(Point pt)
	{
		buffRect.setRect(x, y, width, height);
		return buffRect.contains(pt);
	}
}
