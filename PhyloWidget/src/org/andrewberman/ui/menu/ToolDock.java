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
import org.andrewberman.ui.tools.ToolManager;
import org.andrewberman.ui.tools.ToolManager.ToolShortcuts;

import processing.core.PApplet;

public class ToolDock extends Dock implements ToolManager.ToolShortcuts
{
	protected ToolManager toolManager;

	public ToolDock(PApplet app)
	{
		super(app);
		toolManager = new ToolManager(this);
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
		ToolDockItem tdi = (ToolDockItem) create(name);
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
			setState(activeItem,MenuItem.DOWN);
			activeItem.performAction();
			hovered = null;
			for (int i = 0; i < items.size(); i++)
			{
				ToolDockItem tdi = (ToolDockItem) items.get(i);
				if (tdi != activeItem)
					setState(tdi,MenuItem.UP);
			}
		}
	}

	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		super.mouseEvent(e, screen, model);

	}


}