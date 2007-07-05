package org.phylowidget.ui;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import org.andrewberman.tween.Tween;
import org.andrewberman.tween.TweenListener;
import org.andrewberman.tween.TweenQuad;
import org.andrewberman.ui.ProcessingUtils;
import org.andrewberman.ui.RadialMenu;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;
import org.phylowidget.render.Point;

import processing.core.PApplet;

public final class HoverHalo implements TweenListener, UIObject
{
	private PhyloWidget p = PhyloWidget.p;
	final int type = ELLIPSE;
	static final int FRAMES = 30;
	final int color = p.color(40,120,240);
	Rectangle2D.Float rect = new Rectangle2D.Float(0,0,0,0);
	NodeRange prevNearest;
	NodeRange curNode;
	Tween aTween;
	Tween wTween;
	Tween hTween;
	
	public static final int RECTANGLE = 0;
	public static final int ELLIPSE = 1;
	
	public boolean hidden = true;
	
	public HoverHalo()
	{
		aTween = new Tween(this, TweenQuad.instance, "inout", .75f,  .25f, FRAMES, false);
		wTween = new Tween(this, TweenQuad.instance, "inout", 1.5f, 1.1f, FRAMES, false);
		hTween = new Tween(this, TweenQuad.instance, "inout", 1.5f, 1.1f, FRAMES, false);
	}

	public synchronized void draw()
	{
		if (hidden) return;

		NodeRange r = curNode;
		if (r == null)
			r = PhyloWidget.ui.nearest;
		if (r != prevNearest)
		{
			prevNearest = r;
			aTween.restart(.75f, .25f, FRAMES);
			hTween.restart(1.5f, 1.1f, FRAMES);
			wTween.restart(1.5f, 1.1f, FRAMES);
		}
		if (r == null)
			return;
		
		float maxD = 100;
		Point pt = r.render.getPosition(r.node);
		ProcessingUtils.modelToScreen(pt);
		float dist = (float) pt.distance(p.mouseX,p.mouseY);
		dist = (dist > maxD ? maxD : dist);
		float alpha = (maxD-dist)/maxD * 255;
		
		rect.x = r.loX;
		rect.y = r.loY;
		rect.width = r.hiX - r.loX;
		rect.height = r.hiY - r.loY;
		
		aTween.update();
		wTween.update();
		hTween.update();
		
//		p.stroke(color,255*aTween.position);
		p.stroke(color,alpha);
		p.strokeWeight(Math.max(rect.width/20,3));
		p.noFill();
		float w = wTween.position * rect.width;
		float h = hTween.position * rect.height;
		float x = (float) rect.getCenterX();
		float y = (float) rect.getCenterY();
		switch (type)
		{
			case (RECTANGLE):
				p.rect(x - w/2, y - h/2, w, h);
//				p.rect(rect.x,rect.y,rect.width,rect.height);
				break;
			case (ELLIPSE):
				p.ellipse(x, y, w, h);
				break;
		}
	}
	
	public void hide()
	{
		hidden = true;
	}
	
	public void show()
	{
		hidden = false;
	}
	
	public void stopTweening()
	{
		aTween.stop();
		hTween.stop();
		wTween.stop();
	}
	
	public void tweenEvent(Tween source, int eventType)
	{
		if (eventType == Tween.FINISHED)
			source.yoyo();
	}

	public void keyEvent(KeyEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	Point mPt = new Point(0,0);
	public void mouseEvent(MouseEvent e)
	{
		if (hidden) return;
		
		switch (e.getID())
		{
			case (MouseEvent.MOUSE_MOVED):
				mPt.setLocation(e.getX(),e.getY());
				ProcessingUtils.screenToModel(mPt);
				// Node radius is equal to half-width of NodeRange object.
				NodeRange r = PhyloWidget.ui.nearest;
				
				if (containsPoint(r,mPt))
				{
					p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else
				{
					p.setCursor(Cursor.getDefaultCursor());
				}		
				break;
			case (MouseEvent.MOUSE_PRESSED):
				mPt.setLocation(e.getX(),e.getY());
				ProcessingUtils.screenToModel(mPt);
				NodeRange r2 = PhyloWidget.ui.nearest;
				if (containsPoint(r2,mPt))
				{
					PhyloWidget.ui.showMenu(r2);
				}
				
				break;
		}
		
	}

	public boolean containsPoint(NodeRange r, Point pt)
	{
		if (r == null) return false;
		Point nPt = r.render.getPosition(r.node);
		float radius = (r.hiX - r.loX) / 2;
		float x = pt.x - nPt.x;
		float y = pt.y - nPt.y;
		return (x*x + y*y < radius*radius);
	}
	
	public synchronized void setNodeRange(NodeRange r)
	{
		this.curNode = r;
	}
//	public void setRect(float cx, float cy, float w, float h)
//	{
//		rect.setFrameFromCenter(cx, cy, cx - w/2, cy - h/2);
//	}
	
}
