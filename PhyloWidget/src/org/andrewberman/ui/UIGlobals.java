//package org.andrewberman.ui;
//
//import java.awt.Font;
//
//import org.andrewberman.applets.Globals;
//import org.andrewberman.ui.menu.MenuTimer;
//import org.andrewberman.ui.tools.ToolManager;
//
//import processing.core.PApplet;
//import processing.core.PFont;
//
//public class UIGlobals extends Globals
//{
//	
//	
//	public UIGlobals(PApplet p)
//	{
//		UIGlobals.g = this;
//		this.p = p;
//		setFontLoader(new FontLoader(p));
//		setEventManager(new EventManager(p));
//		setFocusManager(new FocusManager(p));
//		setToolManager(new ToolManager(p));
//		setShortcutManager(new ShortcutManager(p));
//		menuTimer = new MenuTimer();
//		menuTimer.start();
//	}
//
//	public MenuTimer getMenuTimer()
//	{
//		return menuTimer;
//	}
//	
//	public  EventManager event()
//	{
//		return getEventManager();
//	}
//
//	public  FocusManager focus()
//	{
//		return getFocusManager();
//	}
//	
//	public  EventManager getEventManager()
//	{
//		return eventManager;
//	}
//	
//	public  FocusManager getFocusManager()
//	{
//		return focusManager;
//	}
//	
//	public  PFont getPFont()
//	{
//		return fontLoader.vera;
//	}
//	
//	public  PApplet getP()
//	{
//		return p;
//	}
//
//	public  ShortcutManager getShortcutManager()
//	{
//		return shortcutManager;
//	}
//
//	public  ToolManager getToolManager()
//	{
//		return toolManager;
//	}
//
//	public  void setEventManager(EventManager eventManager)
//	{
//		this.eventManager = eventManager;
//	}
//
//	public  void setFocusManager(FocusManager focusManager)
//	{
//		this.focusManager = focusManager;
//	}
//
//	public void setFontLoader(FontLoader l)
//	{
//		this.fontLoader = l;
//	}
//	
//	public  void setP(PApplet p)
//	{
//		this.p = p;
//	}
//
//	public  void setShortcutManager(ShortcutManager shortcutManager)
//	{
//		this.shortcutManager = shortcutManager;
//	}
//
//	public  void setToolManager(ToolManager toolManager)
//	{
//		this.toolManager = toolManager;
//	}
//
//	public  ShortcutManager shortcuts()
//	{
//		return getShortcutManager();
//	}
//
//	public  ToolManager tools()
//	{
//		return getToolManager();
//	}
//	
//	@Override
//	public synchronized void destroyGlobals()
//	{
//		super.destroyGlobals();
//		menuTimer.item = null;
//		menuTimer.parent = null;
//		menuTimer.lastSet = null;
//		menuTimer.stop();
//		menuTimer = null;
//		eventManager = null;
//		focusManager = null;
//		fontLoader = null;
//		shortcutManager = null;
//		toolManager = null;
//		p = null;
//		UIGlobals.g = null;
//	}
//	
//}
