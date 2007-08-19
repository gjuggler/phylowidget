package org.phylowidget.ui;

import java.awt.Cursor;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.ifaces.UIObject;
import org.andrewberman.ui.tween.Tween;
import org.andrewberman.ui.tween.TweenListener;
import org.andrewberman.ui.tween.TweenQuad;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;
import org.phylowidget.render.RenderStyleSet;

import processing.core.PApplet;

public final class HoverHalo implements TweenListener, UIObject
{
	private PApplet p;
	final int type = ELLIPSE;
	static final int FRAMES = 30;
	Rectangle2D.Float rect;
	Point tempPt;
	NodeRange prevNearest;
	NodeRange curNode;
	Tween aTween;
	Tween wTween;
	Tween hTween;

	public static final int RECTANGLE = 0;
	public static final int ELLIPSE = 1;

	public boolean hidden = true;
	public boolean solid = false;

	public static float hoverMult;

	public HoverHalo(PApplet p)
	{
		UIUtils.loadUISinglets(p);
		this.p = p;
		EventManager.instance.add(this);

		rect = new Rectangle2D.Float(0, 0, 0, 0);
		tempPt = new Point(0, 0);

		aTween = new Tween(this, TweenQuad.tween, Tween.INOUT, 1f, .25f, FRAMES);
		wTween = new Tween(this, TweenQuad.tween, Tween.INOUT, 1.5f, 1.1f,
				FRAMES);
		hTween = new Tween(this, TweenQuad.tween, Tween.INOUT, 1.5f, 1.1f,
				FRAMES);
	}

	public void draw()
	{
		if (hidden)
			return;
		if (FocusManager.instance.getFocusedObject() != null)
			return;
		
		NodeRange r = curNode;
		if (r == null)
			r = PhyloWidget.ui.nearest.nearest;
		if (r != prevNearest)
		{
			prevNearest = r;
			restart();
		}
		if (r == null)
			return;

		float maxD = 100;
		tempPt.setLocation(r.node.x,r.node.y);
//		r.render.getNodePosition(r.node, tempPt);
		UIUtils.modelToScreen(tempPt);
		float dist = (float) tempPt.distance(p.mouseX, p.mouseY);
		dist = (dist > maxD ? maxD : dist);
		float alpha = (maxD - dist) / maxD * 255;

		float rad = r.render.getNodeRadius();
		rect.setFrameFromCenter(r.node.x, r.node.y, r.node.x - rad, r.node.y
				- rad);
		// rect.x = r.node.x;
		// rect.y = r.node.y;
		// rect.width = r.render.getNodeRadius();
		// rect.height = r.render.getNodeRadius();

		aTween.update();
		wTween.update();
		hTween.update();

		hoverMult = wTween.position;
		int color = RenderStyleSet.defaultStyle().hoverColor.getRGB();
		p.stroke(color * aTween.position);

		if (solid)
		{

			// p.noStroke();
			// p.fill(color,255);
			p.stroke(40, 120, 240, 255);
			p.strokeWeight(Math.max(rect.width / 20, 3));
		} else
		{
			p.noFill();
			p.stroke(40, 120, 240, 255);
			p.strokeWeight(Math.max(rect.width / 20, 3));
		}
		float w = wTween.position * rect.width;
		float h = hTween.position * rect.height;
		float x = (float) rect.getCenterX();
		float y = (float) rect.getCenterY();
		switch (type)
		{
			case (RECTANGLE):
				p.rect(x - w / 2, y - h / 2, w, h);
				// p.rect(rect.x,rect.y,rect.width,rect.height);
				break;
			case (ELLIPSE):
				p.ellipse(x, y, w, h);
				break;
		}
	}

	public void hide()
	{
		hidden = true;
		UIUtils.releaseCursor(this, p);
	}

	public void show()
	{
		hidden = false;
		restart();
	}

	public void restart()
	{
		solid = false;
		aTween.restart(.75f, .25f, FRAMES);
		hTween.restart(1.5f, 1.1f, FRAMES);
		wTween.restart(1.5f, 1.1f, FRAMES);
	}

	public void becomeSolid()
	{
		hTween.continueTo(1.1f);
		hTween.fforward();
		hTween.stop();
		wTween.continueTo(1.1f);
		wTween.fforward();
		wTween.stop();
		solid = true;
	}

	public void tweenEvent(Tween source, int eventType)
	{
		if (eventType == Tween.FINISHED)
			source.yoyo();
	}

	public void keyEvent(KeyEvent e)
	{
	}

	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		if (hidden)
			return;
		if (FocusManager.instance.getFocusedObject() != null)
			return;

		// System.out.println("Hover event!");
		Point pt = model;

		NodeRange r = PhyloWidget.ui.nearest.nearest;
		if (r == null)
			return;

		switch (e.getID())
		{
			case (MouseEvent.MOUSE_MOVED):
				if (containsPoint(r, pt))
				{
					UIUtils.setCursor(this, p, Cursor.HAND_CURSOR);
					r.node.hovered = true;
				} else
				{
					UIUtils.releaseCursor(this, p);
					r.node.hovered = false;
				}
				break;
			case (MouseEvent.MOUSE_PRESSED):
				if (containsPoint(r, pt))
				{
					PhyloWidget.ui.context.show(r);
				}
				break;
		}
	}

	public void focusEvent(FocusEvent e)
	{

	}

	public boolean containsPoint(NodeRange r, Point pt)
	{
		if (r == null)
			return false;
//		r.render.getNodePosition(r.node, tempPt);
		tempPt.setLocation(r.node.x,r.node.y);
		// float radius = r.render.getNodeRadius();
		float radius = r.render.getNodeRadius() * wTween.position;
		float x = pt.x - tempPt.x;
		float y = pt.y - tempPt.y;
		return (x * x + y * y < radius * radius);
	}
}
