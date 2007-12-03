package org.andrewberman.ui;

import java.awt.AWTEvent;
import java.awt.Event;

public class UIEvent extends AWTEvent
{
	private static final long serialVersionUID = 1L;

	public static final int TEXT_VALUE = 0;
	public static final int TEXT_SELECTION = 1;
	public static final int TEXT_CARET = 2;
	
	public static final int MENU_OPENED = 3;
	public static final int MENU_CLOSED = 4;
	public static final int MENU_ACTIONPERFORMED = 7;
	
	public static final int DOCK_ITEM_SELECTED = 8;
	public static final int DOCK_ACTIVATED = 9;
	public static final int DOCK_DEACTIVATED = 10;
	
	public UIEvent(Event event)
	{
		super(event);
	}
	
	public UIEvent(Object o, int id)
	{
		super(o,id);
	}
}
