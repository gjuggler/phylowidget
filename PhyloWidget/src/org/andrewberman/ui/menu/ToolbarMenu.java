package org.andrewberman.ui.menu;

import org.andrewberman.ui.ProcessingUtils;

import processing.core.PFont;

public class ToolbarMenu extends Menu
{
	float itemHeight;
	float fontOffset;
	boolean isActive;

	public ToolbarMenu()
	{
		clickToggles = true;
		hoverNavigable = false;
		clickAwayBehavior = Menu.CLICKAWAY_COLLAPSES;
		actionOnMouseDown = true;
	}
	
	protected void setOpenItem(MenuItem item)
	{
		super.setOpenItem(item);
		if (item == null)
			isActive = false;
		else
			isActive = true;
	}
	
	public MenuItem create(String label)
	{
		return new ToolbarMenuItem(label);
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
		itemHeight = textHeight + menu.style.pad*2;
		fontOffset = itemHeight/2 + textHeight/2 - descent;
		/*
		 * Set the width, height and position for the top-level items.
		 */
		float curPos = 0;
		for (int i=0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem)items.get(i);
			float textWidth = item.getWidth();
			float fullWidth = textWidth + 4*menu.style.pad;
			if (item instanceof Sizable)
			{
				Sizable size = (Sizable) item;
				size.setSize(fullWidth, itemHeight);
			}
			if (item instanceof Positionable)
			{
				Positionable pos = (Positionable) item;
				if (menu == this)
					pos.setPosition(OFFSET + curPos, OFFSET);
				else
					pos.setPosition(x + curPos, y);
			}
			if (item instanceof ToolbarMenuItem)
			{
				ToolbarMenuItem tbi = (ToolbarMenuItem) item;
				tbi.textWidth = textWidth;
				tbi.textOffsetY = fontOffset;
			}
			curPos += fullWidth + menu.style.pad/2;
		}
		// Trigger the recursive layout for the rest of the menu.
		super.layout();
	}
}
