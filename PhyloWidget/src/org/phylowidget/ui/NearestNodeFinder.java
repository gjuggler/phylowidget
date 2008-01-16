package org.phylowidget.ui;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.ifaces.UIObject;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;
import org.phylowidget.tree.TreeManager;

import processing.core.PApplet;

public class NearestNodeFinder implements UIObject
{
	private PApplet p;

	public static final float RADIUS = 50f;

	Point2D.Float pt = new Point2D.Float(0, 0);
	Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 0, 0);
	ArrayList hits = new ArrayList(50);

	NodeRange nearest;
	float nearestDist;

	private Point updatePt;

	public NearestNodeFinder(PApplet p)
	{
		this.p = p;
		UIUtils.loadUISinglets(p);
		//		
		EventManager.instance.add(this);
	}

	public void draw()
	{
		update();
	}

	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		// Let's send screen coordintaes to the update() function.
		// update(screen.x,screen.y);
		synchronized (this)
		{
			needsUpdate = true;
		}
		updatePt = screen;
	}

	boolean needsUpdate;

	synchronized void update()
	{
		if (PhyloWidget.trees == null)
			return;
		if (!needsUpdate)
			return;

		System.out.println("Hey");

		// float ratio = TreeManager.getVisibleRect().width /
		// PhyloWidget.p.width;
		float ratio = TreeManager.camera.getZ();
		float rad = RADIUS * ratio;

		pt = updatePt;
		rect.x = pt.x - rad;
		rect.y = pt.y - rad;
		rect.width = rad * 2;
		rect.height = rad * 2;
		UIUtils.screenToModel(pt);
		UIUtils.screenToModel(rect);
		hits.clear();
		PhyloWidget.trees.nodesInRange(hits, rect);

		nearestDist = Float.MAX_VALUE;
		NodeRange temp = null;
		for (int i = 0; i < hits.size(); i++)
		{
			NodeRange r = (NodeRange) hits.get(i);
			PhyloNode n = r.node;
			switch (r.type)
			{
				case (NodeRange.NODE):
					float dist = (float) pt.distance(n.getX(), n.getY());
					if (dist < nearestDist)
					{
						temp = r;
						nearestDist = dist;
					}
					break;
			}
		}
		if (temp != null)
			nearest = temp;
		needsUpdate = false;
	}

	public void focusEvent(FocusEvent e)
	{
	}

	public void keyEvent(KeyEvent e)
	{
	}
}
