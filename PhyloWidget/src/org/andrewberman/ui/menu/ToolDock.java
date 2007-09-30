package org.andrewberman.ui.menu;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.Shortcut;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.ifaces.UIObject;
import org.andrewberman.ui.tools.Tool;

import processing.core.PApplet;

public class ToolDock extends Dock
{
	protected ToolManager toolManager;

	public ToolDock(PApplet app)
	{
		super(app);
		toolManager = new ToolManager();
		EventManager.instance.setToolManager(toolManager);
	}

	public MenuItem create(String s)
	{
		ToolDockItem tdi = new ToolDockItem();
		tdi.setName(s);
		return tdi;
	}

	public ToolDockItem create(String name, String toolClassName, String icon)
	{
		ToolDockItem tdi = new ToolDockItem();
		tdi.setName(name);
		tdi.setTool(toolClassName);
		tdi.setIcon(icon);
		return tdi;
	}

	public void keyEvent(KeyEvent e)
	{
		super.keyEvent(e);
		checkToolShortcuts(e);
	}

	public void checkToolShortcuts(KeyEvent e)
	{
		if (e.getID() != KeyEvent.KEY_PRESSED)
			return;
		ToolDockItem activeItem = null;
		for (int i = 0; i < items.size(); i++)
		{
			ToolDockItem tdi = (ToolDockItem) items.get(i);
			Tool t = (Tool) tdi.getTool();
			if (t.getShortcut() != null)
			{
				Shortcut s = t.getShortcut();
				if (s.matchesKeyEvent(e))
				{
					activeItem = tdi;
				}
			}
		}
		if (activeItem != null)
		{
			activeItem.setState(MenuItem.DOWN);
			activeItem.performAction();
			currentlyHovered = null;
			for (int i = 0; i < items.size(); i++)
			{
				ToolDockItem tdi = (ToolDockItem) items.get(i);
				if (tdi != activeItem)
					tdi.setState(MenuItem.UP);
			}
		}
	}

	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		super.mouseEvent(e, screen, model);

	}

	class ToolManager implements UIObject
	{
		Tool curTool;

		public void switchTool(Tool switchMe)
		{
			if (curTool != null)
				curTool.exit();
			curTool = switchMe;
			curTool.setCamera(EventManager.instance.toolCamera);
			curTool.enter();
			UIUtils.setBaseCursor(curTool.getCursor());
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
		}

		public void mouseEvent(MouseEvent e, Point screen, Point model)
		{
			if (curTool != null)
				curTool.mouseEvent(e, screen, model);
		}
	}
}