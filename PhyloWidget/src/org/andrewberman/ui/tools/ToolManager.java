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

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIGlobals;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.ifaces.UIObject;

import processing.core.PApplet;

public class ToolManager
{
	PApplet p;
	Tool curTool;
	ArrayList<UIObject> listeners;

	public ToolManager(PApplet p)
	{
		this.p = p;
		listeners = new ArrayList<UIObject>();
		UIGlobals.g.event().setToolManager(this);
	}

	public void addToolListener(UIObject o)
	{
		listeners.add(o);
	}
	
	public void switchTool(Tool switchMe)
	{
		if (curTool != null)
			curTool.exit();
		curTool = switchMe;
		curTool.setCamera(UIGlobals.g.event().toolCamera);
		curTool.enter();
		UIUtils.setBaseCursor(p,curTool.getCursor());
	}

	public Tool getCurrentTool()
	{
		return curTool;
	}

	public void draw()
	{
		if (curTool != null)
			curTool.draw();
	}

	public void focusEvent(FocusEvent e)
	{
		if (curTool != null)
			curTool.focusEvent(e);
	}

	public void keyEvent(KeyEvent e)
	{
		if (curTool != null)
			curTool.keyEvent(e);
		if (UIGlobals.g.focus().getFocusedObject() != null)
		{
			return;
		}
		for (UIObject o : listeners)
		{
			o.keyEvent(e);
		}
	}

	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		if (curTool != null)
			curTool.mouseEvent(e, screen, model);
	}

	public interface ToolShortcuts
	{
		public void checkToolShortcuts(KeyEvent e);
	}
}