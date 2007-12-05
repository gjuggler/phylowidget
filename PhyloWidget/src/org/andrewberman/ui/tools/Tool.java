package org.andrewberman.ui.tools;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.AbstractUIObject;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.Shortcut;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.camera.Camera;

import processing.core.PApplet;
import processing.core.PImage;

public abstract class Tool extends AbstractUIObject
{
	PApplet p;

	Camera camera;

	Shortcut shortcut, toggle;
	Point downPoint, curPoint;
	boolean mousePressed, mouseDragging, controlPressed;

	public Tool(PApplet p)
	{
		this.p = p;
		downPoint = new Point(0, 0);
		curPoint = new Point(0, 0);
	}

	public void setToggleKey(String s)
	{
		toggle = new Shortcut(s);
	}

	public void setShortcut(String s)
	{
		shortcut = new Shortcut(s);
	}

	public Shortcut getShortcut()
	{
		return shortcut;
	}

	/**
	 * Subclasses should return "false" if they want their tool to "ignore"
	 * other events while in use.
	 * 
	 * @return
	 */
	public abstract boolean respondToOtherEvents();

	public Cursor getCursor()
	{
		return Cursor.getDefaultCursor();
	}

	public Camera getCamera()
	{
		return camera;
	}

	public void setCamera(Camera c)
	{
		camera = c;
	}

	public Cursor createCursor(String filename, int offsetX, int offsetY)
	{
		PImage img = p.loadImage(filename);
		Dimension d = Toolkit.getDefaultToolkit().getBestCursorSize(img.width,
				img.height);
		PImage resized = p.createImage(d.width, d.height, PImage.ARGB);
		resized.copy(img, 0, 0, img.width, img.height, 0, 0, img.width,
				img.height);
		Image image = UIUtils.PImageToImage(resized);
		return Toolkit.getDefaultToolkit().createCustomCursor(image,
				new java.awt.Point(offsetX, offsetY), "asdf");
	}

	public void enter()
	{

	}

	public void exit()
	{

	}

	void pressReset(MouseEvent e, Point screen, Point model)
	{
	}

	void reset()
	{

	}

	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		int type = e.getID();
		switch (type)
		{
			case (MouseEvent.MOUSE_PRESSED):
				mousePressed = true;
				downPoint = (Point) screen.clone();
				pressReset(e, screen, model);
				break;
			case (MouseEvent.MOUSE_RELEASED):
				mousePressed = false;
				break;
			case (MouseEvent.MOUSE_DRAGGED):
			case (MouseEvent.MOUSE_MOVED):
				curPoint = (Point) screen.clone();
				break;
		}

		if (type == MouseEvent.MOUSE_DRAGGED)
		{
			if (!mousePressed)
			{
				pressReset(e, screen, model);
				mousePressed = true;
			}
			mouseDragging = true;
		} else
			mouseDragging = false;
	}

	@Override
	public void keyEvent(KeyEvent e)
	{
		super.keyEvent(e);
		if (e.isControlDown())
		{
			if (!controlPressed)
				reset();
			controlPressed = true;

		} else
		{
			if (controlPressed)
				reset();
			controlPressed = false;
		}
	}

}
