package org.andrewberman.ui;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import org.phylowidget.PhyloWidget;

import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;

/**
 * The <code>ShortcutManager</code> class is used to detect keyboard shortcut
 * key events. Although its functionality is similar to the
 * <code>EventManager</code> class, in order to allow keyboard shortcuts to be
 * activated on a global level, we need to "shortcut" the
 * EventManager/FocusManager system... get it?
 * <p>
 * 
 * @author Greg
 * @see org.andrewberman.ui.Shortcut
 * @see org.andrewberman.ui.EventManager
 */
public class ShortcutManager implements KeyListener
{
	PApplet p;
	public static ShortcutManager instance;

	public ArrayList keys;

	int meta = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

	private ShortcutManager(PApplet app)
	{
		p = app;
		keys = new ArrayList();
		setup();
	}

	public static void lazyLoad(PApplet app)
	{
		// if (instance == null)
		instance = new ShortcutManager(app);
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

	public Shortcut createShortcut(String s)
	{
		Shortcut sh = new Shortcut(s);
		add(sh);
		return sh;
	}

	public void add(Shortcut key)
	{
		keys.add(key);
	}

	public void remove(Shortcut key)
	{
		keys.remove(key);
	}

	public void keyEvent(KeyEvent e)
	{
		if (e.getID() != KeyEvent.KEY_PRESSED)
			return;
		for (int i = 0; i < keys.size(); i++)
		{
			Shortcut key = (Shortcut) keys.get(i);
			if (key.matchesKeyEvent(e))
				key.performAction();
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
