/**************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PhyloWidget.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.andrewberman.ui.menu;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Rectangle2D.Float;

import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.ifaces.Positionable;
import org.andrewberman.ui.ifaces.Sizable;

import processing.core.PApplet;

/**
 * The <code>ToolbarItem</code> class is a MenuItem that belongs to a Toolbar
 * object. It represents the "base" MenuItem of a Toolbar, i.e. the actual item
 * that says "File", "Edit", and so on.
 * 
 * @author Greg
 */
public class ToolbarItem extends MenuItem
{
	static final float shortcutTextSize = .75f;
	static RoundRectangle2D.Float roundRect = new RoundRectangle2D.Float(0, 0,
			0, 0, 0, 0);
	static RoundRectangle2D.Float buffRoundRect = new RoundRectangle2D.Float(0,
			0, 0, 0, 0, 0);
	static AffineTransform at = new AffineTransform();

	static Area tri;
	static float triWidth;

	Rectangle2D.Float subItemRect = new Rectangle2D.Float();

	float tWidth, shortcutWidth;

	boolean drawChildrenTriangle;

	public ToolbarItem()
	{
		super();
	}

	protected void drawMyself()
	{
		roundRect.setRoundRect(x, y, width, height, menu.style.roundOff,
				menu.style.roundOff);
		Graphics2D g2 = menu.buff.g2;

		/*
		 * Set the correct fill gradient
		 */
		if (isOpen() && parent == menu)
		{
			g2.setPaint(menu.style.getGradient(MenuItem.DOWN, y, y + height));
		} else if (!hasChildren() && getState() == MenuItem.DOWN)
		{
			g2.setPaint(menu.style.getGradient(MenuItem.DOWN, y, y + height));
		} else
			g2.setPaint(menu.style.getGradient(getState(), y, y + height));

		/*
		 * Only perform the fill if the mood is right.
		 */
		if (getState() != MenuItem.UP || isOpen())
		{
			if (getState() == MenuItem.DISABLED)
			{
				g2.fill(roundRect);
				g2.setPaint(menu.style.strokeColor);
				g2.setStroke(menu.style.stroke);
				g2.draw(roundRect);
			} else if (menu.hovered != null && menu.hovered != this
					&& !isAncestorOfHovered())
			{
				;
			} else
			{
				g2.fill(roundRect);
				g2.setPaint(menu.style.strokeColor);
				g2.setStroke(menu.style.stroke);
				g2.draw(roundRect);
			}
		}

		/*
		 * Draw the text, triangle, and shortcut.
		 */
		float curX = x + menu.style.padX;
		MenuUtils.drawLeftText(this, getName(), curX);
		curX += tWidth;
		if (shortcut != null)
		{
			float rightX = getX() + getWidth();
			curX = rightX - shortcutWidth;
			// curX += menu.style.padX;
			float shortSize = menu.style.fontSize * shortcutTextSize;
			float descent = UIUtils.getTextDescent(menu.buff, menu.style.font,
					shortSize, true);
			g2.setFont(menu.style.font.font.deriveFont(shortSize));
			g2.setPaint(menu.style.textColor.brighter(100));
			float ht = UIUtils.getTextHeight(menu.canvas.g, menu.style.font,
					shortSize, shortcut.label, true);
			float yOffset = (height - ht) / 2f + descent;
			yOffset += ht / 2;
			g2.drawString(shortcut.label, curX, y + yOffset);
		}
		curX += shortcutWidth;
		if (drawChildrenTriangle && items.size() > 0)
		{
			if (layoutMode == LAYOUT_BELOW && getState() != MenuItem.UP
					&& !isOpen())
			{
				curX = x + width / 2;
				at.setToIdentity();
				at.translate(curX, y + height + menu.style.padY / 2);
				at.rotate(PApplet.HALF_PI);
				Area a2 = tri.createTransformedArea(at);
				g2.setPaint(menu.style.strokeColor);
				g2.fill(a2);
			} else if (layoutMode != LAYOUT_BELOW)
			{
				curX = x + width - triWidth - menu.style.padX;
				at.setToIdentity();
				at.translate(curX, y + height / 2);
				Area a2 = tri.createTransformedArea(at);
				g2.setPaint(menu.style.strokeColor);
				g2.fill(a2);
			}
		}
	}

	protected void drawBefore()
	{
		if (isOpen() && items.size() > 0)
			MenuUtils.drawBackgroundRoundRect(this, subItemRect.x,
					subItemRect.y, subItemRect.width, subItemRect.height);
	}

	/**
	 * Normally, the MenuItem's create() method just defers back to the nearest
	 * Menu it can use to create an item, but here we want to change some
	 * options, so let's override it.
	 */
	public MenuItem create(String label)
	{
		ToolbarItem ti = new ToolbarItem();
		ti.setLayoutMode(ToolbarItem.LAYOUT_RIGHT);
		ti.drawChildrenTriangle = true;
		ti.setName(label);
		return ti;
	}

	// @Override
	// public void open()
	// {
	// super.open();
	// if (parent == menu)
	// {
	// FocusManager.instance.setFocus(menu);
	// // System.out.println("FOCUS");
	// }
	//
	// }

	// @Override
	// public void close()
	// {
	// super.close();
	// if (parent == menu)
	// {
	// FocusManager.instance.removeFromFocus(menu);
	// }
	// }

	protected int layoutMode;
	protected static final int LAYOUT_BELOW = 0;
	protected static final int LAYOUT_RIGHT = 1;
	protected static final int LAYOUT_LEFT = 2;

	protected void setLayoutMode(int layoutMode)
	{
		this.layoutMode = layoutMode;
	}

	public void layout()
	{
		if (menu == null)
			return;
		float curX = 0, curY = 0;
		switch (layoutMode)
		{
			case (LAYOUT_BELOW):
				curX = x - menu.style.padY;
				curY = y + height;
				break;
			case (LAYOUT_RIGHT):
			default:
				curX = x + width;
				curY = y - menu.style.padY;
				break;
		}
		subItemRect.x = curX;
		subItemRect.y = curY;
		curX += menu.style.padY;
		curY += menu.style.padY;
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem) items.get(i);
			item.calcPreferredSize();
		}
		float maxWidth = getMaxWidth();
		float maxHeight = getMaxHeight();
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem) items.get(i);
			item.setPosition(curX, curY);
			item.setSize(maxWidth, maxHeight);
			curY += item.getHeight();
		}
		curY += menu.style.padY;

		subItemRect.width = maxWidth + menu.style.padY * 2;
		subItemRect.height = curY - subItemRect.y;
		super.layout();
	}

	protected void calcPreferredSize()
	{
		super.calcPreferredSize();

		/*
		 * Calculate the text rectangle size.
		 */
		tWidth = UIUtils.getTextWidth(menu.buff, menu.style.font,
				menu.style.fontSize, getName(), true);
		/*
		 * For the height, let's use the height of some capital letters.
		 */
		float tHeight = UIUtils.getTextHeight(menu.buff, menu.style.font,
				menu.style.fontSize, "XYZ", true);

		float triangleWidth = 0;
		if (drawChildrenTriangle && items.size() > 0
				&& layoutMode != LAYOUT_BELOW)
		{
			/*
			 * Calculate the width of the "submenu" triangle shape.
			 */
			at = AffineTransform.getScaleInstance(tHeight / 2f, tHeight / 2f);
			Area a = menu.style.subTriangle.createTransformedArea(at);
			ToolbarItem.tri = a;
			ToolbarItem.triWidth = (float) a.getBounds2D().getWidth();
			triangleWidth = triWidth + menu.style.padX;
		}
		shortcutWidth = 0;
		if (shortcut != null)
		{
			shortcutWidth = menu.style.padX
					+ UIUtils.getTextWidth(menu.buff, menu.style.font,
							menu.style.fontSize * shortcutTextSize,
							shortcut.label, true);
		}

		setWidth(tWidth + triangleWidth + shortcutWidth + 2 * menu.style.padX);
		setHeight(tHeight + 2 * menu.style.padY);
	}

	protected void getRect(Rectangle2D.Float rect, Rectangle2D.Float buff)
	{
		buff.setFrame(x, y, width, height);
		Rectangle2D.union(rect, buff, rect);
		super.getRect(rect, buff);
	}

	// protected void setState(int state)
	// {
	// super.setState(state);
	// if (this.state == state)
	// return;
	// if (menu == parent && menu instanceof Toolbar)
	// {
	// boolean oldHov = menu.hoverNavigable;
	// menu.hoverNavigable = false;
	// super.setState(state);
	// Toolbar tb = (Toolbar) menu;
	// if (state != MenuItem.UP)
	// {
	// if (tb.isActive())
	// {
	// tb.closeMyChildren();
	// open();
	// }
	// }
	// menu.hoverNavigable = oldHov;
	// } else
	// super.setState(state);
	// }

	protected void itemMouseEvent(MouseEvent e, Point pt)
	{
		/*
		 * I'm doing this actionOnMouseDown stuff so that the top-level menus
		 * are activated on a mouse press, to be more toolbar-like (I'm looking
		 * to match Eclipse-like functionality). Basically, I'm overriding the
		 * default values if we're in a top-level menu.
		 */
		super.itemMouseEvent(e, pt);
		// if (parent == menu && mouseInside)
		// {
		// if (e.getID() == MouseEvent.MOUSE_RELEASED)
		// {
		// if (!isOpen())
		// {
		// menuTriggerLogic();
		// } else
		// {
		// close();
		// }
		// }
		// // System.out.println("Hey!");
		// }
	}

	protected boolean containsPoint(Point p)
	{
		buffRoundRect.setRoundRect(x, y, width, height, menu.style.roundOff,
				menu.style.roundOff);
		return buffRoundRect.contains(p);
	}
}
