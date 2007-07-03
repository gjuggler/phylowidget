package org.andrewberman.phyloinfo;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.phyloinfo.render.NodeRange;
import org.andrewberman.phyloinfo.tree.TreeNode;
import org.andrewberman.ui.HoverHalo;
import org.andrewberman.ui.ProcessingUtils;
import org.andrewberman.util.XYRange;

import processing.core.PApplet;

public final class UIManager implements MouseMotionListener, MouseListener
{
	protected PApplet p = PhyloWidget.p;
	public HoverHalo h = new HoverHalo();
	
	protected Rectangle2D.Float mRect = new Rectangle2D.Float(0,0,0,0); 
	
	public UIManager()
	{
		p.addMouseMotionListener(this);
		p.addMouseListener(this);
		
		h.setType(HoverHalo.ELLIPSE);
		h.show();
	}
	
	public void draw()
	{
		updateHover();
		h.draw();
	}

	Point2D.Float mpt = new Point2D.Float();
	float minX,minY,minDist=0;
	NodeRange minRange;
	ArrayList hits = new ArrayList();
	
	public void updateHover()
	{
		if (minRange == null) return;
		float cx = (minRange.loX + minRange.hiX) / 2;
		float cy = (minRange.loY + minRange.hiY) / 2;
		h.setRect(cx, cy, minRange.hiX-minRange.loX, minRange.hiY-minRange.loY);
	}
	
	public void mouseEvent(MouseEvent e)
	{
		switch (e.getID())
		{
			case (MouseEvent.MOUSE_MOVED):
				mpt.setLocation(p.mouseX,p.mouseY);
				ProcessingUtils.screenToModel(p,mpt);
				mRect.setFrameFromCenter(mpt.x, mpt.y, mpt.x-p.width/2, mpt.y-p.height/2);
				hits.clear();
				TreeManager.instance.nodesInRange(hits,mRect);
				
				minDist = Float.MAX_VALUE;
				for (int i=0; i < hits.size(); i++)
				{
					NodeRange r = (NodeRange)hits.get(i);
					float cx = (r.loX + r.hiX) / 2;
					float cy = (r.loY + r.hiY) / 2;
					switch (r.type)
					{
						case (NodeRange.NODE):
							float dist = (float) mpt.distanceSq(cx,cy);
							if (dist < minDist)
							{
								minRange = r;
								minDist = dist;
							}
							break;
					}
				}
				if (minDist > 100*100)
					h.hide();
				else
					h.show();
				break;
			default:
				break;
		}
	}
	
	public void mouseDragged(MouseEvent e){mouseEvent(e);}
	public void mouseMoved(MouseEvent e){mouseEvent(e);}
	public void mouseClicked(MouseEvent e){mouseEvent(e);}
	public void mouseEntered(MouseEvent e){mouseEvent(e);}
	public void mouseExited(MouseEvent e){mouseEvent(e);}
	public void mousePressed(MouseEvent e){mouseEvent(e);}
	public void mouseReleased(MouseEvent e){mouseEvent(e);}
}
