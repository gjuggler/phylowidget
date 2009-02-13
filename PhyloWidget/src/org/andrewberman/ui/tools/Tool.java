/*******************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * PhyloWidget. If not, see <http://www.gnu.org/licenses/>.
 */
package org.andrewberman.ui.tools;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.AbstractUIObject;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.Shortcut;
import org.andrewberman.ui.UIContext;
import org.andrewberman.ui.UIPlatform;
import org.andrewberman.ui.camera.Camera;

import processing.core.PApplet;

public abstract class Tool extends AbstractUIObject
{
	protected PApplet p;
	protected UIContext context;

	Camera camera;

	Shortcut shortcut;
	Point downPoint, curPoint;
	boolean mousePressed, mouseDragging, controlPressed;

	public Tool(PApplet p)
	{
		this.p = p;
		this.context = UIPlatform.getInstance().getThisAppContext();
		downPoint = new Point(0, 0);
		curPoint = new Point(0, 0);
	}

	public void setToggleKey(String s)
	{
		setShortcut(s);
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
	public boolean respondToOtherEvents()
	{
		return false;
	}

	public boolean modalFocusWhileDragging()
	{
		return true;
	}

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
				if (modalFocusWhileDragging())
					context.focus().removeFromFocus(this);
				mousePressed = false;
				break;
			case (MouseEvent.MOUSE_DRAGGED):
				if (modalFocusWhileDragging())
					context.focus().setModalFocus(this);
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
