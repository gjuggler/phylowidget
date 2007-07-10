package org.andrewberman.ui.menu;

import org.andrewberman.ui.ProcessingUtils;

import processing.core.PFont;

public class ToolbarMenu extends Menu
{
	float fontOffset;
	boolean isActive;
	
	public void draw()
	{
		ToolbarMenuItem.fontOffset = fontOffset;
		super.draw();
		clickToggles = true;
		hoverNavigable = false;
		clickAwayBehavior = Menu.CLICKAWAY_COLLAPSES;
	}
	
	protected void setOpenItem(MenuItem item)
	{
		super.setOpenItem(item);
		if (item == null)
			isActive = false;
		else
			isActive = true;
	}
	
	public void layout()
	{
		/*
		 * Calculate the height of each row in this menu.
		 */
		PFont font = menu.style.font;
		float fontSize = menu.style.fontSize;
		float descent = ProcessingUtils.getTextDescent(pg,font,fontSize,true);
		float ascent = ProcessingUtils.getTextAscent(pg,font,fontSize,true);
		float textHeight = descent+ascent;
		float itemHeight = textHeight + 2*menu.style.pad;
		fontOffset = menu.style.pad + textHeight - descent;
		/*
		 * Set the width, height and position for the top-level items.
		 */
		float curPos = x;
		for (int i=0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem)items.get(i);
			float curWidth = item.getWidth();
			if (item instanceof Sizable)
			{
				Sizable size = (Sizable) item;
				size.setSize(curWidth, itemHeight);
			}
			if (item instanceof Positionable)
			{
				Positionable pos = (Positionable) item;
				if (menu == this)
					pos.setPosition(OFFSET + curPos, OFFSET);
				else
					pos.setPosition(x + curPos, y);
			}
			curPos += curWidth + menu.style.pad;
		}
		// Trigger the recursive layout for the rest of the menu.
		super.layout();
	}
}
