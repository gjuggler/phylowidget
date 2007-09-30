package org.andrewberman.ui.menu;

import java.lang.reflect.Constructor;

import org.andrewberman.ui.tools.Tool;

import processing.core.PApplet;

public class ToolDockItem extends DockItem
{
	private String toolString;
	private Tool tool;

	public void setTool(String s)
	{
		toolString = s;
		if (menu != null)
		{
			PApplet p = menu.canvas;
			try
			{
				String packageName = Tool.class.getPackage().getName();
				Class toolClass = Class.forName(packageName+"."+s);
				Constructor c = toolClass
						.getConstructor(new Class[] { PApplet.class });
				Object instance = c.newInstance(new Object[] { p });
				this.tool = (Tool) instance;
			} catch (Exception e)
			{
				e.printStackTrace();
				return;
			}
		}
	}

	public Tool getTool()
	{
		return tool;
	}
	
	public void setMenu(Menu menu)
	{
		super.setMenu(menu);
		if (tool == null)
		{
			setTool(toolString);
		}
	}
	
	public MenuItem setShortcut(String s)
	{
		super.setShortcut(s);
		if (tool != null)
			tool.setShortcut(s);
		return this;
	}
	
	protected void performAction()
	{
		super.performAction();
		if (nearestMenu instanceof ToolDock)
		{
			ToolDock td = (ToolDock) nearestMenu;
			td.toolManager.switchTool(tool);
		}
	}
}
