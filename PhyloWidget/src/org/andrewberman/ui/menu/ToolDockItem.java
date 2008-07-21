/*******************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * PhyloWidget. If not, see <http://www.gnu.org/licenses/>.
 */
package org.andrewberman.ui.menu;

import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;

import org.andrewberman.ui.UIGlobals;
import org.andrewberman.ui.tools.Tool;

import processing.core.PApplet;

public class ToolDockItem extends DockItem
{
	protected Tool tool;
	private String toolString;
	private String shortcutString;

	public void setMenu(Menu menu)
	{
		super.setMenu(menu);
	}

	public MenuItem setShortcut(String s)
	{
		// Tools have "global" shortcuts, so we don't add a menu-specific one here.
		// Instead, we store a separate string and use that to build the tool's shortcut.
		shortcutString = s;
		if (tool != null)
			tool.setShortcut(s);
		return this;
	}

	public void setTool(String toolClass)
	{
		tool = UIGlobals.g.getToolManager().createTool(getName(), toolClass);
		if (shortcutString != null)
			tool.setShortcut(shortcutString);
	}

	@Override
	public void keyEvent(KeyEvent e)
	{
		super.keyEvent(e);
	}

	public String getLabel()
	{
		return getName() + " (" + tool.getShortcut().label + ")";
	}

	public void performAction()
	{
		super.performAction();
//		System.out.println(tool);
//		System.out.println(UIGlobals.g.tools().getCurrentTool());
//		if (tool != UIGlobals.g.tools().getCurrentTool())
		UIGlobals.g.tools().switchTool(getName());
	}
}
