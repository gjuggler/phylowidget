package org.andrewberman.ui.ifaces;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.Point;

/**
 * The <code>UIObject</code> interface guarantees that an object has the
 * minimum methods necessary to be managed by the <code>EventManager</code>
 * and <code>FocusManager</code> managers. In other words, it can respond to
 * mouse, keyboard, and focus events, and can be drawn to the canvas.
 * 
 * @author Greg
 */
public interface UIObject
{
	public void mouseEvent(MouseEvent e, Point screen, Point model);

	public void keyEvent(KeyEvent e);

	public void focusEvent(FocusEvent e);

	public void draw();
}
