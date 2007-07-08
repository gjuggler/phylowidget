package org.andrewberman.ui.menu;

import java.awt.geom.Rectangle2D;

import org.phylowidget.ui.FontLoader;

import processing.core.PFont;

public class VerticalMenu extends Menu
{
	final static int INNER_PAD = 4;
	
	public int layoutRule;
	public float itemWidth = 80;
	public float itemHeight = 15;
	
	PFont font = FontLoader.f64;
	float fontSize = 12;
	
	protected void drawAbove()
	{
		if (items.size() > 0)
		{
			g2.setStroke(colors.stroke);
			g2.setPaint(colors.strokeColor);
			Rectangle2D.Float rect = new Rectangle2D.Float(PAD,PAD,
					itemWidth, itemHeight*items.size());
			g2.draw(rect);
		}
	}
	
	public void layout()
	{
		final float height = itemHeight - INNER_PAD * 2;
		fontSize = height / (font.descent() * 2 + font.ascent());
		
//		System.out.println("Menu layout!"+items.size());
		for (int i=0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem)items.get(i);
			if (item instanceof RectMenuItem)
			{
				RectMenuItem rItem = (RectMenuItem) item;
				rItem.x = PAD;
				rItem.y = PAD + i*itemHeight;
				rItem.width = itemWidth;
				rItem.height = itemHeight;
				if (i == 0)
					rItem.position = RectMenuItem.TOP;
				else if (i == items.size()-1)
					rItem.position = RectMenuItem.BOTTOM;
				else
					rItem.position = RectMenuItem.MIDDLE;
			}
		}
		// Trigger the recursive layout for the rest of the menu.
		super.layout();
	}

}
