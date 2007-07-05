package org.phylowidget.ui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.ui.ProcessingUtils;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;

public class NearestNodeFinder
{
	public static final float RADIUS = 100f;
	
	static Point2D.Float pt = new Point2D.Float(0,0);
	static Rectangle2D.Float rect = new Rectangle2D.Float(0,0,0,0);
	static ArrayList hits = new ArrayList(50);
	public static NodeRange nearestNode(float x, float y)
	{
		pt.setLocation(x,y);
		rect.x = pt.x - RADIUS;
		rect.y = pt.y - RADIUS;
		rect.width = RADIUS * 2;
		rect.height = RADIUS * 2;
		ProcessingUtils.screenToModel(pt);
		ProcessingUtils.screenToModel(rect);
//		System.out.println(rect);
		hits.clear();
		PhyloWidget.trees.nodesInRange(hits, rect);
//		System.out.println(hits.size());
		float minDist = Float.MAX_VALUE;
		NodeRange minRange = null;
		for (int i=0; i < hits.size(); i++)
		{
			NodeRange r = (NodeRange)hits.get(i);
			float cx = (r.loX + r.hiX) / 2;
			float cy = (r.loY + r.hiY) / 2;
			switch (r.type)
			{
				case (NodeRange.NODE):
					float dist = (float) pt.distanceSq(cx,cy);
					if (dist < minDist)
					{
						minRange = r;
						minDist = dist;
					}
					break;
			}
		}
		return minRange;
	}
}
