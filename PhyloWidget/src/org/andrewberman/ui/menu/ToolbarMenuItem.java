package org.andrewberman.ui.menu;

import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Rectangle2D.Float;

import org.andrewberman.ui.Color;
import org.andrewberman.ui.Point;

public class ToolbarMenuItem extends MenuItem implements Sizable, Positionable
{
	static final float roundOff = 15;
	static RoundRectangle2D.Float roundRect = new RoundRectangle2D.Float(0,0,0,0,0,0);
	static RoundRectangle2D.Float buffRoundRect = new RoundRectangle2D.Float(0,0,0,0,0,0);
	static float fontOffset;
	
	float x,y,width,height;
	
	public ToolbarMenuItem(String label)
	{
		super(label);
	}
	
	public void draw()
	{
		if (isVisible())
		{
			roundRect.setRoundRect(Math.round(x), Math.round(y),
					Math.round(width), Math.round(height), roundOff, roundOff);
			/*
			 * Fill in.
			 */
			if (menu instanceof ToolbarMenu)
			{
				ToolbarMenu tb = (ToolbarMenu) menu;
				if (tb.isActive)
				{
					if (state == MenuItem.DOWN || showingChildren())
					{
						menu.g2.setPaint(menu.style.getGradient(MenuItem.DOWN,y, y + height));
					} else
						menu.g2.setPaint(menu.style.getGradient(state,y, y + height));
				} else {
					switch (state)
					{
						case (MenuItem.DOWN):
							menu.g2.setPaint(menu.style.getGradient(MenuItem.OVER, y, y+height));
							break;
						case (MenuItem.UP):
						case (MenuItem.OVER):
						default:
							menu.g2.setPaint(menu.style.getGradient(state, y, y + height));
							break;
					}
				}
				menu.g2.fill(roundRect);
			}
			menu.g2.setPaint(menu.style.strokeColor);
			menu.g2.draw(roundRect);
			/*
			 * Draw the text.
			 */
			menu.g2.setFont(menu.style.font.font
					.deriveFont(menu.style.fontSize));
			menu.g2.setPaint(menu.style.textColor);
			menu.g2.drawString(label, x+menu.style.pad, y+fontOffset);
		}
		super.draw();
	}
	
	public void layout()
	{
		for (int i=0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem) items.get(i);
			if (item instanceof Positionable)
			{
				Positionable pos = (Positionable) item;
				pos.setPosition(x,y+height+menu.style.pad/2);
			}
		}
		super.layout();
	}
	
	protected void getRect(Float rect, Float buff)
	{
		buff.setFrame(x, y, width, height);
		Rectangle2D.union(rect, buff, rect);
		super.getRect(rect, buff);
	}
	
	protected void setState(int state)
	{
		super.setState(state);
		
		if (menu instanceof ToolbarMenu)
		{
			ToolbarMenu tb = (ToolbarMenu) menu;
			if (tb.isActive && state == MenuItem.OVER)
			{
				menu.setOpenItem(this);
				setState(MenuItem.DOWN);
			}
		}
	}
	
	protected boolean containsPoint(Point p)
	{
		buffRoundRect.setRoundRect(x, y, width, height, roundOff, roundOff);
		return buffRoundRect.contains(p);
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
