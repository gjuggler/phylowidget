/**************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PhyloWidget.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.andrewberman.ui.menu;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.Shortcut;
import org.andrewberman.ui.UIGlobals;
import org.andrewberman.ui.ifaces.UIObject;
import org.andrewberman.ui.tools.Tool;

import processing.core.PApplet;

public class ToolDock extends Dock implements UIObject
{
//	public Tool curTool;
	
	public ToolDock(PApplet app)
	{
		super(app);
		UIGlobals.g.tools().setToolDock(this);
	}

	public MenuItem create(String s)
	{
		ToolDockItem tdi = new ToolDockItem();
		tdi.setName(s);
			
		return tdi;
	}

	public void keyEvent(KeyEvent e)
	{
		UIGlobals.g.getToolManager().checkToolShortcuts(e);
	}

	public void selectItem(MenuItem item, boolean performAction)
	{
		setState(item,MenuItem.DOWN);
		hovered = null;
		for (MenuItem i : items)
		{
//			if (i == item)
//			{
//				ToolDockItem tdi = (ToolDockItem) item;
//				curTool = tdi.tool;
//			}
			if (i != item)
				setState(i,MenuItem.UP);
		}
		if (performAction)
			item.performAction();
	}
	
	/*
	 * Convenience method for xml menu definitions. Don't remove!!
	 */
	public void selectTool(String toolName)
	{
		MenuItem tool = get(toolName);
		selectItem(tool,true);
	}
	
	public void updateActiveTool(String toolName)
	{
		MenuItem tool = get(toolName);
		selectItem(tool,false);
	}
	
	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		super.mouseEvent(e, screen, model);
	}


}