package org.andrewberman.ui;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.andrewberman.ui.ifaces.UIObject;
import org.andrewberman.ui.tools.Tool;

import processing.core.PApplet;

public class ToolManager implements UIObject
{
	PApplet p;

	public static ToolManager instance;
	ArrayList tools;
	public Tool curTool;
	Object listener;

	public static void lazyLoad(PApplet p)
	{
		if (instance == null)
			instance = new ToolManager(p);
	}

	private ToolManager(PApplet p)
	{
		this.p = p;
		tools = new ArrayList();
	}

	public void registerTool(Tool registerMe)
	{
		tools.add(registerMe);
	}

	public void switchTool(Tool switchMe)
	{
		if (curTool != null)
			curTool.exit();
		curTool = switchMe;
		curTool.enter();
		UIUtils.setBaseCursor(curTool.getCursor());
	}

	public void draw()
	{
		if (curTool == null)
			return;
		curTool.draw();
	}

	public void focusEvent(FocusEvent e)
	{
		if (curTool == null)
			return;
		curTool.focusEvent(e);
	}

	public void keyEvent(KeyEvent e)
	{
		if (e.getID() != KeyEvent.KEY_PRESSED) return;
		for (int i = 0; i < tools.size(); i++)
		{
			Tool t = (Tool) tools.get(i);
			if (t.getShortcut() != null)
			{
				Shortcut s = t.getShortcut();
				if (s.matchesKeyEvent(e))
				{
					e.consume();
					switchTool(t);
					notifyListener();
				}
			}
		}
		if (curTool != null)
			curTool.keyEvent(e);
	}

	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		if (curTool == null)
			return;
		curTool.mouseEvent(e, screen, model);
	}
	
	public void setListener(Object o)
	{
		listener = o;
	}
	
	void notifyListener()
	{
		if (listener != null)
		{
			// Try to use reflection to call the "toolChanged" method.
			try
			{
				Method m = listener.getClass().getMethod("toolChanged", new Class[]{});
				m.invoke(listener, new Object[]{});
			} catch (Exception e)
			{	e.printStackTrace();
			}
		}
	}
}
