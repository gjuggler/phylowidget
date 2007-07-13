package org.andrewberman.ui.menu;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import org.andrewberman.ui.Positionable;
import org.andrewberman.ui.ProcessingUtils;
import org.andrewberman.ui.Sizable;

import processing.core.PApplet;
import processing.core.PFont;

public class VerticalMenu extends Menu
{	
	public VerticalMenu(PApplet app)
	{
		super(app);
		// TODO Auto-generated constructor stub
	}

	/*
	 * Values to cache and send off as static to VerticalItemMenu to avoid
	 * creating objects during each draw cycle.
	 */
	float fontOffset;
	float triWidth;
	Area tri;
	float iconSize;
	
	protected void drawBefore()
	{
//		pg.textFont(menu.style.font);
//		pg.textSize(menu.style.fontSize);
//		pg.fill(0);
	}
	
	public MenuItem create(String label)
	{
		return new VerticalMenuItem(label);
	}
	
	public void draw()
	{
		VerticalMenuItem.fontOffset = fontOffset;
		VerticalMenuItem.triWidth = triWidth;
		VerticalMenuItem.tri = tri;
		VerticalMenuItem.iconSize = iconSize;
		super.draw();
	}
	
	protected void drawAfter()
	{
		VerticalMenuItem.drawChildrenRect(this);
	}
	
	public void layout()
	{
		/*
		 * Calculate the height of each row in this menu.
		 */
		PFont font = style.font;
		float fontSize = style.fontSize;
		float descent = ProcessingUtils.getTextDescent(menu.g,font,fontSize,true);
		float ascent = ProcessingUtils.getTextAscent(menu.g,font,fontSize,true);
		float textHeight = descent+ascent;
		float itemHeight = textHeight + 2*style.padY;
		fontOffset = style.padY + textHeight - descent;
		/*
		 * Calculate the width of the "submenu" triangle shape.
		 */
		float innerHeight = textHeight;
		AffineTransform at = AffineTransform.getScaleInstance(innerHeight/2, innerHeight/2);
		Area a = style.subTriangle.createTransformedArea(at);
		VerticalMenuItem.tri = tri = a;
		VerticalMenuItem.triWidth = triWidth = (float) a.getBounds2D().getWidth();
		/*
		 * Calculate the max width of text in the menu items.
		 */
		float itemWidth = getMaxWidth();
		/*
		 * Set the width, height and position for the top-level items.
		 */
		for (int i=0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem)items.get(i);
			if (item instanceof Sizable)
			{
				Sizable size = (Sizable) item;
				size.setSize(itemWidth, itemHeight);
			}
			if (item instanceof Positionable)
			{
				Positionable pos = (Positionable) item;
				if (menu == this)
					pos.setPosition(OFFSET, OFFSET + i*itemHeight);
				else
					pos.setPosition(x, y + i*itemHeight);
			}
		}
		// Trigger the recursive layout for the rest of the menu.
		super.layout();
	}
}
