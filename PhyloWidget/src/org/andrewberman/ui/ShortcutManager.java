package org.andrewberman.ui;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;

public class ShortcutManager implements KeyListener
{
	private PApplet p;
	public static ShortcutManager instance;
	
	public ArrayList keys;
	
	int meta = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	
	public ShortcutManager(PApplet app)
	{
		p = app;
		keys = new ArrayList();
		instance = this;
	}
	
	public static ShortcutManager loadLazy(PApplet app)
	{
		if (instance == null)
			return new ShortcutManager(app);
		else
			return instance;
	}
	
	public void setup()
	{
		if (p.g.getClass().getName().equals(PApplet.OPENGL))
		{
			PGraphicsOpenGL gl = (PGraphicsOpenGL) p.g;
			gl.canvas.addKeyListener(this);
		} else
		{
			p.addKeyListener(this);
		}
	}

	public void add(Shortcut key)
	{
		keys.add(key);
	}

	public void keyEvent(KeyEvent e)
	{
//		System.out.println(e);
		if (e.getID() != KeyEvent.KEY_PRESSED) return;
		for (int i=0; i < keys.size(); i++)
		{
			Shortcut key = (Shortcut) keys.get(i);
			boolean modMatch = (e.getModifiers() & key.keyMask) != 0;
			if (modMatch &&
					e.getKeyCode() == key.keyCode)
			{
				key.performAction();
			}
		}
	}
	
	public void keyTyped(KeyEvent e)
	{
		keyEvent(e);
	}

	
	public void keyPressed(KeyEvent e)
	{
		keyEvent(e);
	}

	public void keyReleased(KeyEvent e)
	{
		keyEvent(e);
	}
}
