/**************************************************************************
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
package org.phylowidget.ui;

import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIRectangle;
import org.andrewberman.ui.menu.RadialMenu;
import org.phylowidget.render.NodeRange;
import org.phylowidget.tree.PhyloNode;

import processing.core.PApplet;

public class PhyloContextMenu extends RadialMenu
{
	//	HoverHalo hover;
	public NodeTraverser traverser;

	NodeRange curNodeRange;
	Point nodePt = new Point(0, 0);

	public PhyloContextMenu(PApplet p)
	{
		super(p);
		traverser = new NodeTraverser(p);
	}

	public void setOptions()
	{
		super.setOptions();
		modalFocus = true;
		this.setRadii(10, 28);
	}

	public void draw()
	{
		if (curNodeRange != null)
		{
			// Update our position based on the current menu node.
			setPosition(curNodeRange.node.getX(), curNodeRange.node.getY());
		}
		super.draw();
	}

	public void open(NodeRange r)
	{
		super.open();
		setNodeRange(r);
		aTween.continueTo(1f);
		aTween.fforward();
	}

	public void close()
	{
		super.close();
		if (traverser != null)
			traverser.getCurRange();
	}

	private boolean shouldGlow = true;

	public void setGlow(boolean glow)
	{
		traverser.setGlow(glow);
	}

	public PhyloNode getNearestNode()
	{
		PhyloNode n = traverser.getCurrentNode();
		if (n != null)
			return n;
		else
			return null;
	}

	private void setNodeRange(NodeRange r)
	{
		curNodeRange = r;
	}

	Rectangle2D.Float nodeRect = new Rectangle2D.Float();

	public void itemMouseEvent(MouseEvent e, Point pt)
	{
		super.itemMouseEvent(e, pt);
		NodeRange r = curNodeRange;
		if (r == null)
			return;
		//		if (!isOpen())
		//			return;

		if (mouseInside)
			return;

		nodeRect.setFrameFromDiagonal(r.loX, r.loY, r.hiX, r.hiY);
		float dist = UIRectangle.distToPoint(nodeRect, pt);

		float fadeDist = Math.max(myRect.width, myRect.height) * FADE_DIST_MULTIPLIER;
		fadeDist = Math.max(fadeDist, super.radius);
		if (dist < fadeDist)
		{
			if (autoDim)
			{
				float normalized = 1f - (dist / fadeDist);
				aTween.continueTo(normalized);
				aTween.fforward();
			}
		} else
		{
			close();
		}
	}
}
