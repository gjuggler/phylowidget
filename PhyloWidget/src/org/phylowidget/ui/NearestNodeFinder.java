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
package org.phylowidget.ui;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.ui.AbstractUIObject;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIGlobals;
import org.andrewberman.ui.UIRectangle;
import org.andrewberman.ui.UIUtils;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.TreeManager;

import processing.core.PApplet;

public class NearestNodeFinder extends AbstractUIObject
{
//	private PApplet p;
//
//	public static final float RADIUS = 50f;
//
//	Point2D.Float pt = new Point2D.Float(0, 0);
//	Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 0, 0);
//	ArrayList hits = new ArrayList(50);
//
//	NodeRange nearest;
//	float nearestDist;
//
//	private Point updatePt;
//
//	public NearestNodeFinder(PApplet p)
//	{
//		this.p = p;
//		//		
//		UIGlobals.g.event().add(this);
//	}
//
//	public void draw()
//	{
//		update();
//	}
//
//	public void mouseEvent(MouseEvent e, Point screen, Point model)
//	{
//		synchronized (this)
//		{
//			needsUpdate = true;
//		}
//		updatePt = screen;
//	}
//
//	boolean needsUpdate;
//
//	synchronized void update()
//	{
//		if (PhyloWidget.trees == null)
//			return;
////		if (!needsUpdate)
////			return;
//
//		// float ratio = TreeManager.getVisibleRect().width /
//		// PhyloWidget.p.width;
//		float ratio = TreeManager.camera.getZ();
//		float rad = RADIUS * ratio;
//
//		pt = updatePt;
//		rect.x = pt.x - rad;
//		rect.y = pt.y - rad;
//		rect.width = rad * 2;
//		rect.height = rad * 2;
//		UIUtils.screenToModel(pt);
//		UIUtils.screenToModel(rect);
//		hits.clear();
//		PhyloWidget.trees.nodesInRange(hits, rect);
//
//		nearestDist = Float.MAX_VALUE;
//		NodeRange temp = null;
//		for (int i = 0; i < hits.size(); i++)
//		{
//			NodeRange r = (NodeRange) hits.get(i);
//			PhyloNode n = r.node;
////			switch (r.type)
////			{
////				case (NodeRange.NODE):
//					float dist = (float) pt.distance(n.getX(), n.getY());
//					//					float dist = UIRectangle.distToPoint(r.node.rect,pt);
//					System.out.println("Dist: "+dist);
//					if (dist < nearestDist)
//					{
//						temp = r;
//						nearestDist = dist;
//					}
//					break;
////			}
//		}
//		if (temp != null)
//			nearest = temp;
//		needsUpdate = false;
//	}
//
//	public void focusEvent(FocusEvent e)
//	{
//	}
//
//	public void keyEvent(KeyEvent e)
//	{
//	}
}
