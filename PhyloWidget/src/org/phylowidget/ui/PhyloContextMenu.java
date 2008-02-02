/**************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PhyloWidget.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.phylowidget.ui;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.menu.RadialMenu;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;

import processing.core.PApplet;

public final class PhyloContextMenu extends RadialMenu
{
	HoverHalo hover;
	
	NodeRange curNodeRange;
	Point nodePt = new Point(0,0);
	
	public PhyloContextMenu(PApplet p)
	{
		super(p);
	}

	public void setOptions()
	{
		super.setOptions();
		this.setRadii(10, 28);
	}
	
	public void draw()
	{
		if (curNodeRange != null)
		{
			// Update our position based on the current menu node.
			setPosition(curNodeRange.node.getRealX(),curNodeRange.node.getRealY());
		}
		super.draw();
	}

	public void open(NodeRange r)
	{
		menu.open(this);
		setNodeRange(r);
		aTween.continueTo(1f);
		aTween.fforward();
	}
	
	public void close()
	{
		super.close();
		PhyloWidget.ui.traverser.getCurRange();
	}
	
	private void setNodeRange(NodeRange r)
	{
		curNodeRange = r;
	}
}
