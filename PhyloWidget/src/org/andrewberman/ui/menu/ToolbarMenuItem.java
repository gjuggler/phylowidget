package org.andrewberman.ui.menu;

import java.awt.RenderingHints;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Rectangle2D.Float;

import org.andrewberman.ui.Color;
import org.andrewberman.ui.Point;

public class ToolbarMenuItem extends MenuItem implements Sizable, Positionable
{
	static final float roundOff = .2f;
	static RoundRectangle2D.Float roundRect = new RoundRectangle2D.Float(0,0,0,0,0,0);
	static RoundRectangle2D.Float buffRoundRect = new RoundRectangle2D.Float(0,0,0,0,0,0);
	
	float x,y,width,height;
	float textWidth;
	float textOffsetY;
	
	public ToolbarMenuItem(String label)
	{
		super(label);
		add(new VerticalMenu());
	}
	
	public void draw()
	{
		if (isVisible())
		{
			roundRect.setRoundRect(Math.round(x), Math.round(y),
					Math.round(width), Math.round(height), Math.round(roundOff*width), Math.round(roundOff*width));
			/*
			 * Set the correct fill gradient
			 */
			if (showingChildren())
			{
				menu.g2.setPaint(menu.style.getGradient(MenuItem.DOWN,y, y + height));
			} else
			{
				menu.g2.setPaint(menu.style.getGradient(MenuItem.OVER,y, y + height));
			}
			/*
			 * Only perform the fill if the conditions are right.
			 */
			if (state != MenuItem.UP || showingChildren())
			{
				menu.g2.fill(roundRect);
			}
			/*
			 * Draw the rounded rectangle outline.
			 */
			if (state != MenuItem.UP || showingChildren())
			{
				RenderingHints rh = menu.g2.getRenderingHints();
				menu.g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); 
				menu.g2.setPaint(menu.style.strokeColor);
				menu.g2.draw(roundRect);
				menu.g2.setRenderingHints(rh);
			}
			/*
			 * Draw the text.
			 */
			menu.g2.setFont(menu.style.font.font
					.deriveFont(menu.style.fontSize));
			menu.g2.setPaint(menu.style.textColor);
			menu.g2.drawString(label, x + width/2 - textWidth/2, y + textOffsetY);
		}
		super.draw();
	}
	
	public MenuItem add(String s)
	{
		/*
		 * Let's modify this method slightly so that by default we create
		 * items within our underlying VerticalMenu. 
		 */
		if (items.size() != 0 && items.get(0) instanceof VerticalMenu)
		{
			VerticalMenu vm = (VerticalMenu) items.get(0);
			return vm.add(s);
		} else
		{
			return add(new VerticalMenu()).add(s);
		}
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

	public float getX()
	{
		return x;
	}
	
	public float getY()
	{
		return y;
	}
}
