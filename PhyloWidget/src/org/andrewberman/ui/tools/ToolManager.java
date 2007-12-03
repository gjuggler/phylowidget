package org.andrewberman.ui.tools;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.ifaces.UIObject;

public class ToolManager implements UIObject
{
	ToolShortcuts parent;
	Tool curTool;
	
	public ToolManager(ToolShortcuts parent)
	{
		this.parent = parent;
	}

	public void switchTool(Tool switchMe)
	{
		if (curTool != null)
			curTool.exit();
		curTool = switchMe;
		curTool.setCamera(EventManager.instance.toolCamera);
		curTool.enter();
		UIUtils.setBaseCursor(curTool.getCursor());
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
		parent.checkToolShortcuts(e);
		if (curTool != null)
			curTool.keyEvent(e);
	}

	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		if (curTool != null)
			curTool.mouseEvent(e, screen, model);
	}
	
	public interface ToolShortcuts
	{
		public void checkToolShortcuts(KeyEvent e);
	}
}