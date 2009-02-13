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
package org.andrewberman.ui.tools;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Set;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.Shortcut;
import org.andrewberman.ui.UIContext;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.menu.ToolDock;
import org.andrewberman.ui.menu.ToolDockItem;

import processing.core.PApplet;

public class ToolManager
{
	PApplet p;
	UIContext context;
	Tool curTool;
	ToolDock toolDock;
	HashMap<String, Tool> tools;

	Tool scrollTool;
	
	public ToolManager(UIContext context)
	{
		this.context = context;
		this.p = context.getApplet();
		tools = new HashMap<String, Tool>();
		context.event().setToolManager(this);
	}

	public void setToolDock(ToolDock td)
	{
		this.toolDock = td;
	}

	public Tool createTool(String toolName, String toolClassS)
	{
		try
		{
			String packageName = Tool.class.getPackage().getName();
			Class toolClass = Class.forName(packageName + "." + toolClassS);
			Constructor c = toolClass
					.getConstructor(new Class[] { PApplet.class });
			Object instance = c.newInstance(new Object[] { p });
			Tool tool = (Tool) instance;
			if (tool instanceof Scroll)
			{
				scrollTool = tool;
			}
			tools.put(toolName, tool);
			return tool;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public Tool getTool(String toolName)
	{
		return tools.get(toolName);
	}

	void switchTool(Tool t)
	{
		if (t == curTool)
			return;
		
		if (curTool != null)
			curTool.exit();
		curTool = t;
		curTool.setCamera(context.event().toolCamera);
		curTool.enter();
		UIUtils.setBaseCursor(p, curTool.getCursor());
	}
	
	public void switchTool(String switchToMe)
	{
		if (toolDock != null)
		{
			toolDock.updateActiveTool(switchToMe);
		}
		switchTool(tools.get(switchToMe));
	}

	public Tool getCurrentTool()
	{
		return curTool;
	}

	public void draw()
	{
		if (curTool != null)
			curTool.draw();
	}

	public void focusEvent(FocusEvent e)
	{
		if (curTool != null)
			curTool.focusEvent(e);
	}

	public void keyEvent(KeyEvent e)
	{
		checkToolShortcuts(e);
		if (curTool != null)
			curTool.keyEvent(e);
//		if (context.focus().getFocusedObject() != null)
//		{
			if (e.getKeyCode() == KeyEvent.VK_SPACE)
			{
				if (e.getID() == KeyEvent.KEY_PRESSED)
				{
					tempScroll(true);
				} else if (e.getID() == KeyEvent.KEY_RELEASED)
				{
					tempScroll(false);
				}
			}
//		}
	}

	void tempScroll(boolean on)
	{
		if (on)
		{
			if (curTool != scrollTool && scrollTool != null)
			{
				baseTool = curTool;
				switchTool(scrollTool);
			}
		} else
		{
			if (curTool == scrollTool && scrollTool != null)
			{
				if (baseTool != null)
					switchTool(baseTool);
			}
		}
	}
	
	Tool baseTool;
	
	public void checkToolShortcuts(KeyEvent e)
	{
		Object o = context.focus().getFocusedObject();
		if (o != null && o != this)
		{
			return;
		}
		if (e.getID() != KeyEvent.KEY_PRESSED)
			return;
		ToolDockItem activeItem = null;
		Set<String> set = tools.keySet();
		for (String toolS : set)
		{
			Tool t = tools.get(toolS);
			if (t.getShortcut() != null)
			{
				Shortcut s = t.getShortcut();
				if (s.matchesKeyEvent(e))
				{
					switchTool(toolS);
					break;
				}
			}
		}
	}

	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		if (curTool != null)
			curTool.mouseEvent(e, screen, model);
		if (e.getButton() == MouseEvent.BUTTON3)
		{
			if (e.getID() == MouseEvent.MOUSE_PRESSED)
			{
				tempScroll(true);
			} else if (e.getID() == MouseEvent.MOUSE_RELEASED)
			{
				tempScroll(false);
			}
		}
//		if (curTool != null)
//			curTool.mouseEvent(e, screen, model);
	}

}