package org.andrewberman.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import org.andrewberman.ui.ifaces.UIObject;

import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;

public final class EventDispatcher implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{
	private PApplet p;
	private FocusManager f;
	private ArrayList delegates = new ArrayList(5);
	
	public EventDispatcher(PApplet p)
	{
		this.p = p;
		f = FocusManager.instance;
	}
	
	public void setup()
	{
		if (p.g.getClass().getName().equals(PApplet.OPENGL))
		{
			PGraphicsOpenGL gl = (PGraphicsOpenGL) p.g;
			gl.canvas.addMouseListener(this);
			gl.canvas.addMouseMotionListener(this);
			gl.canvas.addKeyListener(this);
			gl.canvas.addMouseWheelListener(this);
		} else
		{
			p.addMouseListener(this);
			p.addMouseMotionListener(this);
			p.addMouseWheelListener(this);
			p.addKeyListener(this);			
		}
	}
	
	public void addListener(UIObject o)
	{
		delegates.add(o);
	}
	
	public void removeListener(UIObject o)
	{
		delegates.remove(o);
	}

	public void mouseEvent(MouseEvent e)
	{
		if (f.isModal()) // If modal, only send mouse events to focused object.
		{
			if (f.getFocusedObject() instanceof UIObject)
				((UIObject)f.getFocusedObject()).mouseEvent(e);
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
		if (f.getFocusedObject() instanceof UIObject)
			((UIObject)f.getFocusedObject()).keyEvent(e);
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
