package org.andrewberman.ui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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

/**
 * The <code>EventManager</code> class, along with <code>FocusManager</code>,
 * is a fundamental part of this package. <code>EventManager's</code> job is
 * to keep track of all the UI objects currently on the screen and dispatch
 * <code>Event</code> objects accordingly.
 * <p>
 * The <code>UIObject</code> interface is used in order to ensure that all
 * objects wishing to register with the <code>EventManager</code> are capable
 * of receiving mouse, keyboard, focus, and draw events.
 * <p>
 * Every UI object wishing to be controlled by the EventManager object should do
 * the following in its constructor:
 * <ul>
 * <li>Call the static method <code>UIUtils.lazyLoad</code>. This causes all
 * "singlet" objects to load up their instances if they haven't done so already.</li>
 * <li>Call <code>EventManager.instance.add(this)</code>. This causes the
 * EventManager to begin managing this UIObject. This consists of: calling the
 * draw() method every frame, and if the FocusManager allows it, calling the
 * mouseEvent, keyEvent, and focusEvent methods accordingly.</li>
 * </ul>
 * 
 * @author Greg
 * @see		org.andrewberman.ui.ifaces.UIObject
 * @see		org.andrewberman.ui.FocusManager
 * @see		org.andrewberman.ui.ShortcutManager
 * @see		org.andrewberman.ui.UIUtils
 */
public final class EventManager implements MouseListener, MouseMotionListener,
		MouseWheelListener, KeyListener
{
	private PApplet p;
	private ArrayList delegates = new ArrayList(5);

	public static EventManager instance;

	Point screen = new Point(0, 0);
	Point model = new Point(0, 0);

	public static void lazyLoad(PApplet p)
	{
		if (instance == null)
			instance = new EventManager(p);
	}

	private EventManager(PApplet p)
	{
		this.p = p;
		setup();
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
		/*
		 * Register ourselves with the PApplet to be drawn every frame.
		 */
		p.registerDraw(this);
	}

	public void add(UIObject o)
	{
		delegates.add(o);
	}

	public void remove(UIObject o)
	{
		delegates.remove(o);
	}

	public void draw()
	{
		UIUtils.setMatrix(p);
//		System.out.println(p.frameCount);
//		System.out.println(delegates.size());
		
		for (int i=0; i < delegates.size(); i++)
		{
			((UIObject)delegates.get(i)).draw();
		}
	}
	
	public void mouseEvent(MouseEvent e)
	{
		screen.setLocation(e.getX(), e.getY());
		model.setLocation(screen.x, screen.y);
		UIUtils.screenToModel(model);

		/*
		 * First, send the event directly to the focus object.
		 */
		if (FocusManager.instance.getFocusedObject() instanceof UIObject)
		{
//			System.out.println(FocusManager.instance.getFocusedObject());
			((UIObject) FocusManager.instance.getFocusedObject())
				.mouseEvent(e, screen, model);
		}
		/*
		 * Then, if the focus isn't modal and the object wasn't consumed, continue
		 * sending the mouse event to the other uiobjects.
		 */
		if (!FocusManager.instance.isModal() && !e.isConsumed())
		{
			for (int i = delegates.size()-1; i >= 0; i--)
			{
				UIObject ui = (UIObject) delegates.get(i);
				if (ui == FocusManager.instance.getFocusedObject())
					continue;
				ui.mouseEvent(e, screen, model);
				if (e.isConsumed()) break;
			}
		}
	}

	public void keyEvent(KeyEvent e)
	{
		// Only send key events to focused object no matter what!
		if (FocusManager.instance.getFocusedObject() instanceof UIObject)
			((UIObject) FocusManager.instance.getFocusedObject()).keyEvent(e);
	}

	public void mouseClicked(MouseEvent e)
	{
		mouseEvent(e);
	}

	public void mouseEntered(MouseEvent e)
	{
		mouseEvent(e);
	}

	public void mouseExited(MouseEvent e)
	{
		mouseEvent(e);
	}

	public void mousePressed(MouseEvent e)
	{
		mouseEvent(e);
	}

	public void mouseReleased(MouseEvent e)
	{
		mouseEvent(e);
	}

	public void mouseDragged(MouseEvent e)
	{
		mouseEvent(e);
	}

	public void mouseMoved(MouseEvent e)
	{
		mouseEvent(e);
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
		mouseEvent(e);
	}

	public void keyPressed(KeyEvent e)
	{
		keyEvent(e);
	}

	public void keyReleased(KeyEvent e)
	{
		keyEvent(e);
	}

	public void keyTyped(KeyEvent e)
	{
		keyEvent(e);
	}

}
