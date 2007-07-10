package org.andrewberman.ui.menu;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.ProcessingUtils;

import processing.core.PFont;
import processing.core.PImage;

public class VerticalMenuItem extends MenuItem implements Sizable, Positionable
{
	float x, y, width, height;
	PImage icon;
	
	static Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 0, 0);
	static AffineTransform at = AffineTransform.getTranslateInstance(0, 0);
	static Area tri;
	static float triWidth;
	static float fontOffset;
	static float iconSize;

	public VerticalMenuItem(String label)
	{
		super(label);
	}

	static void drawChildrenRect(MenuItem item)
	{
		if (item.showingChildren())
		{
			VerticalMenuItem firstChild = (VerticalMenuItem) item.items.get(0);
			item.menu.g2.setStroke(item.menu.style.stroke);
			item.menu.g2.setPaint(item.menu.style.strokeColor);
			Rectangle2D.Float rect = new Rectangle2D.Float(firstChild.x,
					firstChild.y, firstChild.width, firstChild.height
							* item.items.size());
			item.menu.g2.draw(rect);
		}
	}

	public void draw()
	{
		if (isVisible())
		{
			/*
			 * Draw the filled-in rectangle.
			 */
			rect.setFrame(x, y, width, height);
			if (isAncestorOfSelected())
				menu.g2.setPaint(menu.style.getGradient(y, y + height));
			else
				menu.g2.setPaint(menu.style.stateColors[state]);
			menu.g2.fill(rect);
			/*
			 * Draw the text.
			 */
			menu.g2.setFont(menu.style.font.font
					.deriveFont(menu.style.fontSize));
			if (isAncestorOfSelected())
				menu.g2.setPaint(menu.style.selectedTextColor);
			else
				menu.g2.setPaint(menu.style.textColor);
			menu.g2.drawString(label, x+menu.style.pad, y+fontOffset);
//			menu.pg.text(label, x + menu.style.pad, y + fontOffset);
			/*
			 * Draw the "subMenu" triangle if necessary.
			 */
			if (items.size() > 0)
			{
				float triXPos = Math.round(x + width - menu.style.pad - triWidth);
				at.setToIdentity();
				at.translate(triXPos, y + height / 2);
				tri.transform(at);
				menu.g2.setPaint(menu.style.strokeColor);
				menu.g2.fill(tri);
				try
				{
					at.invert();
				} catch (NoninvertibleTransformException e)
				{
					// Should never get here.
					System.err.println("AAAAGGHHH!!! Gasp... I'm dying... Save me!!!");
				}
				tri.transform(at);
			}
		}
		super.draw();
		VerticalMenuItem.drawChildrenRect(this);
	}

	public void layout()
	{
		float maxWidth = getMaxWidth();

		for (int i = 0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem) items.get(i);
			if (item instanceof Sizable)
			{
				Sizable size = (Sizable) item;
				size.setSize(maxWidth, height);
			}
			if (item instanceof Positionable)
			{
				Positionable pos = (Positionable) item;
				pos.setPosition(x+width, y + i*height);
			}
		}
		super.layout();
	}

	protected float getWidth()
	{
		int numElements = 0;
		/*
		 * Text width.
		 */
		PFont font = menu.style.font;
		float fontSize = menu.style.fontSize;
		float textWidth = ProcessingUtils.getTextWidth(menu.pg,font, fontSize, label,true);
		numElements++;
		/*
		 * Triangle width (if any).
		 */
		float myTriWidth = 0;
		if (items.size() > 0)
		{
			myTriWidth = triWidth;
			numElements++;
		}
		/*
		 * Icon width (if any).
		 */
		float iconWidth = 0;
		if (icon != null)
		{
			iconWidth = icon.width;
			numElements++;
		}
		
		return textWidth + myTriWidth + iconWidth + (numElements+1)*menu.style.pad;
	}
	
	protected boolean containsPoint(Point p)
	{
		if (p.x < x || p.y < y || p.x >= x + width || p.y >= y + height)
		{
			return false;
		} else
		{
			return true;
		}
	}

	protected void getRect(Float rect, Float buff)
	{
		buff.setFrame(x, y, width, height);
		Rectangle2D.union(rect, buff, rect);
		super.getRect(rect, buff);
	}

	public void setSize(float w, float h)
	{
		this.width = w;
		this.height = h;
	}

	public void setPosition(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
}
