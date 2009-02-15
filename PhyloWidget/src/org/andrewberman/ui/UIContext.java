package org.andrewberman.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.andrewberman.ui.menu.MenuTimer;
import org.andrewberman.ui.tools.ToolManager;

import processing.core.PApplet;
import processing.core.PFont;

public class UIContext
{
	private final PApplet applet;
	
	private EventManager eventManager;
	private FocusManager focusManager;
	private FontLoader fontLoader;
	private ShortcutManager shortcutManager;
	private MenuTimer menuTimer;
	private ToolManager toolManager;

	private ThreadGroup threadGroup = null;
	private static int nextContextID = 0;

	private boolean inited;
	
	public UIContext(PApplet p)
	{
		this.applet = p;
	}

	public void init()
	{
		System.out.println("Initting "+this);
		eventManager = new EventManager(this);
		focusManager = new FocusManager(this);
		fontLoader = new FontLoader(this);
		shortcutManager = new ShortcutManager(this);
		menuTimer = new MenuTimer();
		menuTimer.start();
		toolManager = new ToolManager(this);
		inited = true;
	}
	
	public ThreadGroup getThreadGroup()
	{
		if (threadGroup == null || threadGroup.isDestroyed())
		{
			threadGroup = new ThreadGroup("PulpCore-App" + nextContextID);
			nextContextID++;
		}
		return threadGroup;
	}
	
    public PApplet getApplet()
    {
    	return applet;
    }
    
	public PApplet p()
	{
		return applet;
	}

	public MenuTimer getMenuTimer()
	{
		return menuTimer;
	}

	public EventManager getEventManager()
	{
		return eventManager;
	}

	public FocusManager getFocusManager()
	{
		return focusManager;
	}

	public ShortcutManager getShortcutManager()
	{
		return shortcutManager;
	}

	public ToolManager getToolManager()
	{
		return toolManager;
	}

	public ShortcutManager shortcuts()
	{
		return getShortcutManager();
	}

	public ToolManager tools()
	{
		return getToolManager();
	}

	public EventManager event()
	{
		return getEventManager();
	}

	public FocusManager focus()
	{
		return getFocusManager();
	}

	public PFont getPFont()
	{
		return fontLoader.vera;
	}
	
	public void destroy()
	{
		this.eventManager = null;
		this.fontLoader = null;
		this.focusManager = null;
		this.menuTimer.stop();
		this.toolManager = null;
		this.shortcutManager = null;
	}
	
	public boolean isInited()
	{
		return inited;
	}
}
