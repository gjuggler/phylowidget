package org.andrewberman.ui;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import org.andrewberman.ui.ifaces.UIListener;
import org.andrewberman.ui.ifaces.UIObject;

public abstract class AbstractUIObject implements UIObject
{
	protected ArrayList listeners = new ArrayList(1);
	
	public void addListener(UIListener o)
	{
		listeners.add(o);
	}
	
	public void removeListener(UIListener o)
	{
		listeners.remove(o);
	}
	
	public void fireEvent(int id)
	{
		UIEvent e = new UIEvent(this,id);
		for (int i=0; i < listeners.size(); i++)
		{
			((UIListener)listeners.get(i)).uiEvent(e);
		}
	}
	
	public void draw()
	{
	}

	public void focusEvent(FocusEvent e)
	{
	}

	public void keyEvent(KeyEvent e)
	{
	}

	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
	}
	
	
}
