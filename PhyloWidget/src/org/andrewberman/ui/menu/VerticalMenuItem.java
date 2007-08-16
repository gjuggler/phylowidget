package org.andrewberman.ui.menu;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;

import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.ifaces.Positionable;
import org.andrewberman.ui.ifaces.Sizable;

import processing.core.PFont;

/**
 * A <code>VerticalMenuItem</code> is the corresponding MenuItem to go along
 * with a <code>VerticalMenu</code>. Not much else to say about that.
 * 
 * @author Greg
 */
public class VerticalMenuItem extends MenuItem implements Sizable, Positionable
{
	static final float shortcutSize = .75f;

	float width, height;
	float labelWidth, shortcutWidth;
	float labelOffsetY;// , shortcutOffsetY;

	static Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 0, 0);
	static AffineTransform at = AffineTransform.getTranslateInstance(0, 0);
	static Area tri;
	static float triWidth;

	public VerticalMenuItem(String label)
	{
		super(label);
	}

	static void drawChildrenRect(MenuItem item)
	{
		if (item.items.size() == 0 || !item.isShowingChildren())
			return;
		MenuItem firstItem = (MenuItem) item.items.get(0);
		if (item.isShowingChildren() && firstItem instanceof VerticalMenuItem)
		{
			VerticalMenuItem firstChild = (VerticalMenuItem) item.items.get(0);
			item.menu.buff.g2.setPaint(item.menu.style.strokeColor);
			Rectangle2D.Float rect = new Rectangle2D.Float(firstChild.x,
					firstChild.y, firstChild.width, firstChild.height
							* item.items.size());
			item.menu.buff.g2.draw(rect);
		}
	}

	public void draw()
	{
		if (isVisible())
		{
			Graphics2D g2 = menu.buff.g2;
			/*
			 * Draw the filled-in rectangle.
			 */
			rect.setFrame(x, y, width, height);
			if (isAncestorOfSelected())
				g2.setPaint(menu.style.getGradient(y, y + height));
			else
				g2.setPaint(menu.style.menuBackground);
			g2.fill(rect);
			/*
			 * Draw the text.
			 */
			g2.setFont(menu.style.font.font.deriveFont(menu.style.fontSize));
			g2.setPaint(menu.style.textColor);
			g2.drawString(label, x + menu.style.padX, y + labelOffsetY);
			/*
			 * Draw the shortcut text if necessary.
			 */
			if (shortcut != null)
			{
				g2.setFont(menu.style.font.font.deriveFont(menu.style.fontSize
						* shortcutSize));
				g2.setPaint(menu.style.textColor.brighter(100));
				g2.drawString(shortcut.label, x + menu.style.padX * 2
						+ labelWidth, y + labelOffsetY);
			}
			/*
			 * Draw the "subMenu" triangle if necessary.
			 */
			if (items.size() > 0)
			{
				float triXPos = Math.round(x + width - menu.style.padX
						- triWidth);
				at.setToIdentity();
				at.translate(triXPos, y + height / 2);
				Area a2 = tri.createTransformedArea(at);
				g2.setPaint(menu.style.strokeColor);
				g2.fill(a2);
			}
		}
		super.draw();
		VerticalMenuItem.drawChildrenRect(this);
	}

	public void layout()
	{
		/*
		 * Position our text correctly.
		 */
		getTextWidth();
		float labelAscent = UIUtils.getTextAscent(menu.buff, menu.style.font,
				menu.style.fontSize, true);
		float labelDescent = UIUtils.getTextDescent(menu.buff, menu.style.font,
				menu.style.fontSize, true);
		float labelHeight = labelAscent + labelDescent;
		labelOffsetY = (height / 2 - labelHeight / 2) + labelAscent;
		// float shortcutAscent = PUtils.getTextAscent(menu.g, menu.style.font,
		// menu.style.fontSize*shortcutSize, true);
		// float shortcutDescent = PUtils.getTextDescent(menu.g,
		// menu.style.font, menu.style.fontSize*shortcutSize, true);
		// float shortcutHeight = shortcutAscent+shortcutDescent;
		// shortcutOffsetY = (height/2 - shortcutHeight/2) + shortcutAscent;
		/*
		 * Layout my sub-items.
		 */
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
				pos.setPosition(x + width, y + i * height);
			}
		}
		super.layout();
	}

	protected float getTextWidth()
	{
		int numElements = 0;
		/*
		 * Text width.
		 */
		PFont font = menu.style.font;
		float fontSize = menu.style.fontSize;
		labelWidth = UIUtils.getTextWidth(menu.buff, font, fontSize, label,
				true);
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
		if (shortcut != null)
		{
			shortcutWidth = UIUtils.getTextWidth(menu.buff, font, fontSize
					* shortcutSize, shortcut.label, true);
			numElements++;
		}

		return labelWidth + myTriWidth + shortcutWidth + (numElements + 1)
				* menu.style.padX;
	}

	protected boolean containsPoint(Point p)
	{
		if (!isVisible()) return false;
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
		if (isVisible())
		{
			buff.setFrame(x, y, width, height);
			Rectangle2D.union(rect, buff, rect);
		}
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

	public float getX()
	{
		return x;
	}

	public float getY()
	{
		return y;
	}

	public float getHeight()
	{
		return height;
	}

	public float getWidth()
	{
		return width;
	}
}
