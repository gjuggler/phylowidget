package org.andrewberman.ui.menu;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Rectangle2D.Float;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.ifaces.Positionable;

/**
 * The <code>ToolbarItem</code> class is a MenuItem that belongs to a Toolbar
 * object. It represents the "base" MenuItem of a Toolbar, i.e. the actual item
 * that says "File", "Edit", and so on.
 * 
 * @author Greg
 */
public class ToolbarItem extends MenuItem
{
	static final float roundOff = .2f;
	static RoundRectangle2D.Float roundRect = new RoundRectangle2D.Float(0, 0,
			0, 0, 0, 0);
	static RoundRectangle2D.Float buffRoundRect = new RoundRectangle2D.Float(0,
			0, 0, 0, 0, 0);

	float width, height;
	float textOffsetX;
	float textOffsetY;

	public void draw()
	{
		if (isVisible())
		{
			roundRect.setRoundRect(x, y, width, height, roundOff * height,
					roundOff * height);
			Graphics2D g2 = menu.buff.g2;
			/*
			 * Set the correct fill gradient
			 */
			if (isShowingChildren())
			{
				g2.setPaint(menu.style
						.getGradient(MenuItem.DOWN, y, y + height));
			} else
			{
				g2.setPaint(menu.style
						.getGradient(MenuItem.OVER, y, y + height));
			}
			/*
			 * Only perform the fill if the mood is right.
			 */
			if (getState() != MenuItem.UP || isShowingChildren())
			{
				g2.fill(roundRect);
			}
			/*
			 * Draw the rounded rectangle outline.
			 */
			if (getState() != MenuItem.UP || isShowingChildren())
			{
				RenderingHints rh = menu.buff.g2.getRenderingHints();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setPaint(menu.style.strokeColor);
				g2.setStroke(menu.style.stroke);
				g2.draw(roundRect);
				g2.setRenderingHints(rh);
			}
			/*
			 * Draw the text.
			 */
			g2.setFont(menu.style.font.font.deriveFont(menu.style.fontSize));
			g2.setPaint(menu.style.textColor);
			g2.drawString(getLabel(), x + textOffsetX, y + textOffsetY);
		}
		super.draw();
	}

	public MenuItem add(String s)
	{
		/*
		 * Let's modify this method slightly so that by default we create items
		 * within our underlying VerticalMenu.
		 */
		if (items.size() != 0 && items.get(0) instanceof VerticalMenu)
		{
			VerticalMenu vm = (VerticalMenu) items.get(0);
			return vm.add(s);
		} else
		{
			return add(new VerticalMenu(menu.canvas)).add(s);
		}
	}

	public void layout()
	{
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem) items.get(i);
			if (item instanceof Positionable)
			{
				Positionable pos = (Positionable) item;
				pos.setPosition(x, (int) y + height + menu.style.margin / 2);
			}
		}
		super.layout();
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

	protected void setState(int state)
	{
		super.setState(state);
		if (menu instanceof Toolbar)
		{
			Toolbar tb = (Toolbar) menu;
			if (tb.isActive && state == MenuItem.OVER)
			{
				menu.setOpenItem(this);
				setState(MenuItem.DOWN);
			}
		}
	}

	protected boolean containsPoint(Point p)
	{
		if (!isVisible()) return false;
		buffRoundRect.setRoundRect(x, y, width, height, roundOff, roundOff);
		return buffRoundRect.contains(p);
	}

	public void setSize(float w, float h)
	{
		this.width = w;
		this.height = h;
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
