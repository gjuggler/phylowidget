package org.andrewberman.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import org.phylowidget.PhyloWidget;

import processing.core.PApplet;

public class FocusManager implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{
	private PApplet p;
	
	private Object focusedObject = null;
	private boolean isModal = false;
	
	private ArrayList delegates = new ArrayList(5);
	
	public FocusManager(PApplet p)
	{
		this.p = p;
		p.addMouseListener(this);
		p.addMouseMotionListener(this);
		p.addMouseWheelListener(this);
		p.addKeyListener(this);
	}
	
	public void addListener(UIObject o)
	{
		delegates.add(o);
	}
	
	public void removeListener(UIObject o)
	{
		delegates.remove(o);
	}
	
	public boolean setFocus(Object o)
	{
		if (isModal && !isFocused(o))
		{
			return false;
		} else
		{
			focusedObject = o;
			return true;
		}
		
	}
	
	public boolean setModalFocus(Object o)
	{
		focusedObject = o;
		isModal = true;
		return true;
	}
	
	/*
	 * Removes the object from focus. Returns true if the object WAS in focus and was removed,
	 * false if the object WAS NOT in focus to begin with.
	 */
	public boolean removeFromFocus(Object o)
	{
		if (focusedObject == o)
		{
//			System.out.println("Remove!");
			focusedObject = null;
			isModal = false;
			return true;
		} else
		{
			return false;
		}
	}
	
	public boolean isFocused(Object o)
	{
		if (o == null) return false;
		return (o == focusedObject);
	}
	
	public Object getFocusedObject()
	{
		return focusedObject;
	}
	
	public boolean isModal()
	{
		return isModal;
	}

	public void mouseEvent(MouseEvent e)
	{
		if (isModal) // If modal, only send mouse events to focused object.
		{
			if (focusedObject instanceof UIObject)
				((UIObject)focusedObject).mouseEvent(e);
		} else
		{
			for (int i=0; i < delegates.size(); i++)
			{
				((UIObject)delegates.get(i)).mouseEvent(e);
			}
		}
	}
	
	public void keyEvent (KeyEvent e)
	{
		// Only send key events to focused object no matter what!
		if (focusedObject instanceof UIObject)
			((UIObject)focusedObject).keyEvent(e);
	}
	
	public void mouseClicked(MouseEvent e){mouseEvent(e);}
	public void mouseEntered(MouseEvent e){mouseEvent(e);}
	public void mouseExited(MouseEvent e){mouseEvent(e);}
	public void mousePressed(MouseEvent e){mouseEvent(e);}
	public void mouseReleased(MouseEvent e){mouseEvent(e);}
	public void mouseDragged(MouseEvent e){mouseEvent(e);}
	public void mouseMoved(MouseEvent e){mouseEvent(e);}
	public void mouseWheelMoved(MouseWheelEvent e){mouseEvent(e);}
	public void keyPressed(KeyEvent e){keyEvent(e);}
	public void keyReleased(KeyEvent e){keyEvent(e);}
	public void keyTyped(KeyEvent e){keyEvent(e);}
}
