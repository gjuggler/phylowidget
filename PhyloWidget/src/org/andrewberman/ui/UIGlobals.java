package org.andrewberman.ui;

import org.andrewberman.applets.Globals;
import org.andrewberman.ui.tools.ToolManager;

import processing.core.PApplet;
import processing.core.PFont;

public class UIGlobals extends Globals
{
	public static UIGlobals g;
	
	private EventManager eventManager;
	private FocusManager focusManager;
	private FontLoader fontLoader;
	private PApplet p;
	private ShortcutManager shortcutManager;
	
	private ToolManager toolManager;
	
	public UIGlobals(PApplet p)
	{
		UIGlobals.g = this;
		this.p = p;
		setEventManager(new EventManager(p));
		setFocusManager(new FocusManager(p));
		setToolManager(new ToolManager(p));
		setShortcutManager(new ShortcutManager(p));
		setFontLoader(new FontLoader(p));
	}

	public  EventManager event()
	{
		return getEventManager();
	}

	public  FocusManager focus()
	{
		return getFocusManager();
	}
	
	public  EventManager getEventManager()
	{
		return eventManager;
	}
	
	public  FocusManager getFocusManager()
	{
		return focusManager;
	}
	
	public PFont font()
	{
		return getFont();
	}
	
	public  PFont getFont()
	{
		return fontLoader.vera;
	}
	
	public  PApplet getP()
	{
		return p;
	}

	public  ShortcutManager getShortcutManager()
	{
		return shortcutManager;
	}

	public  ToolManager getToolManager()
	{
		return toolManager;
	}

	public  void setEventManager(EventManager eventManager)
	{
		this.eventManager = eventManager;
	}

	public  void setFocusManager(FocusManager focusManager)
	{
		this.focusManager = focusManager;
	}

	public void setFontLoader(FontLoader l)
	{
		this.fontLoader = l;
	}
	
	public  void setP(PApplet p)
	{
		this.p = p;
	}

	public  void setShortcutManager(ShortcutManager shortcutManager)
	{
		this.shortcutManager = shortcutManager;
	}

	public  void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}

	public  ShortcutManager shortcuts()
	{
		return getShortcutManager();
	}

	public  ToolManager tools()
	{
		return getToolManager();
	}
	
	@Override
	public void destroyGlobals()
	{
		super.destroyGlobals();
		eventManager = null;
		focusManager = null;
		fontLoader = null;
		p = null;
		shortcutManager = null;
		toolManager = null;
	}
	
}
