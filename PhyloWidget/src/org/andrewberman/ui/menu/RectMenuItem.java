package org.andrewberman.ui.menu;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;

import org.andrewberman.ui.Point;

public class RectMenuItem extends MenuItem
{
	String label;
	float x,y,width,height;
	
	int position;
	public static final int MIDDLE = 0;
	public static final int TOP = 1;
	public static final int BOTTOM = 2;
	
	static Rectangle2D.Float rect = new Rectangle2D.Float(0,0,0,0);
	
	public RectMenuItem(String label)
	{
		this.label = label;
	}

	public void draw()
	{
		super.draw();
		if (isVisible())
		{
			rect.setFrame(x,y,width,height);
			
			menu.g2.setPaint(colors.stateColors[state]);
			menu.g2.fill(rect);
		}
			if (items.size() > 0 && showingChildren)
			{
				menu.g2.setPaint(colors.strokeColor);
				menu.g2.setStroke(colors.stroke);
				rect.setFrame(x+width,y,width, height*items.size());
				menu.g2.draw(rect);
			}
	}
	
	public void layout()
	{
		
		
		for (int i=0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem)items.get(i);
			if (item instanceof RectMenuItem)
			{
				RectMenuItem rectItem = (RectMenuItem) item;
				if (menu instanceof VerticalMenu)
				{
					rectItem.x = x + width;
					rectItem.y = y + i*height;
					rectItem.width = width;
					rectItem.height = height;
				} //else if (menu instanceof HorizontalMenu)
//				{
//					rectItem.x = x + i*width;
//					rectItem.y = y + height;
//					rectItem.width = width;
//					rectItem.height = height;
//				}
			}
		}
		super.layout();
	}

	protected boolean containsPoint(Point p)
	{
		if (p.x < x || p.y < y ||
				p.x >= x+width || p.y >= y+height)
		{
			return false;
		} else
		{
			return true;
		}
	}

	protected void getRect(Float rect, Float buff)
	{
		buff.setFrame(x,y,width,height);
		Rectangle2D.union(rect, buff, rect);
		super.getRect(rect, buff);
	}
}
