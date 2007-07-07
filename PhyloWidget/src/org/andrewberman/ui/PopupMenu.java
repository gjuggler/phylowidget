package org.andrewberman.ui;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.tween.Tween;
import org.andrewberman.tween.TweenListener;
import org.andrewberman.tween.TweenQuad;
import org.phylowidget.PhyloWidget;
import org.phylowidget.ui.FontLoader;
import org.phylowidget.ui.UIObject;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphicsJava2D;

public abstract class PopupMenu implements TweenListener, UIObject, MouseListener, MouseMotionListener
{
	protected PApplet p = PhyloWidget.p;
	protected PGraphicsJava2D pg;
	protected Graphics2D g2;

	public static final int START_SIZE = 100;
	
	protected ArrayList segments;
	protected boolean changeCursor = false;
	protected boolean withinButtons = false;
	
	protected Tween aTween;
	protected int alpha;
	
	public boolean hidden;
	
	public PopupMenu()
	{
		setSize(START_SIZE,START_SIZE);
		segments = new ArrayList();
		hidden = true;
		aTween = new Tween(this, new TweenQuad(), Tween.INOUT, 0, 255, 30);
		aTween.stop();
	}
	
	public void setSize(int w, int h)
	{
		pg = (PGraphicsJava2D) p.createGraphics(w, h, PApplet.JAVA2D);
		g2 = pg.g2;
		pg.hint(PConstants.ENABLE_NATIVE_FONTS); // Native fonts are nice!
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
//		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		pg.smooth();
	}

	Rectangle2D.Float rect;
	public void draw()
	{
		resizeToFit();
//		setSize(w+5,h+5);
		preDraw();
		aTween.update();
		if (alpha == 0) return;
		pg.beginDraw();
		pg.background(255,0);
		pg.translate(pg.width/2, pg.height/2);
		drawSegments();
		
		pg.modified = true;
		pg.endDraw();
		bufferToCanvas();
	}
	
	public void drawSegments()
	{
		for (int i=0; i < segments.size(); i++)
		{
			MenuSegment seg = (MenuSegment)segments.get(i);
			seg.drawUnder(); // for the background rectangles... ugly, I know.
		}
		for (int i=0; i < segments.size(); i++)
		{
			MenuSegment seg = (MenuSegment)segments.get(i);
			seg.draw();
		}
	}
	
	public void resizeToFit()
	{
		Rectangle2D.Float rect = new Rectangle2D.Float(0,0,0,0);
		Rectangle2D.Float buff = new Rectangle2D.Float(0,0,0,0);
//		rect.setFrame(0,0,pg.width,pg.height);
		for (int i=0; i < segments.size(); i++)
		{
			MenuSegment seg = (MenuSegment)segments.get(i);
			seg.getRect(rect,buff);
		}
		p.noFill();
//		p.rect(rect.x, rect.y, rect.width, rect.height);
		float dx=0;
		float dy=0;
		final int PAD = 25;
		if (rect.x < 0)
			dx = -rect.x + PAD;
		if (rect.x + rect.width > pg.width)
			dx = rect.x + rect.width - pg.width + PAD;
		if (rect.y < 0)
			dy = -rect.y + PAD;
		if (rect.y + rect.height > pg.height)
			dy = rect.y + rect.height - pg.height + PAD;

		if (dx != 0 || dy != 0)
			setSize(pg.width+(int)dx*2,pg.height+(int)dy*2);
	}
	
	public void layoutSegments()
	{
		for (int i=0; i < segments.size(); i++)
		{
			MenuSegment seg = (MenuSegment)segments.get(i);
			seg.layout();
		}
	}
	
	public abstract void preDraw();
	public abstract void bufferToCanvas();
	
	public void hide()
	{
		if (hidden)return;
		hidden = true;
		aTween.continueTo(0, 10);
//		System.out.println("Hide!");
	}
	
	public void show()
	{
		if (!hidden)return;
		hidden = false;
		aTween.continueTo(255,10);
//		System.out.println("Show!");
	}
	
	public void addSegment(MenuSegment addMe)
	{
		segments.add(addMe);
		layoutSegments();
	}
	
	public void tweenEvent(Tween source, int eventType)
	{
		if (source == aTween)
		{
			alpha = (int) aTween.position;
		}
	}
	
	public void keyEvent(KeyEvent e)
	{
		for (int i=0; i < segments.size(); i++)
		{
			MenuSegment seg = (MenuSegment)segments.get(i);
			seg.keyEvent(e);
		}
	}
	public void mouseEvent(MouseEvent e)
	{
		changeCursor = false;
		withinButtons = false;
		
		for (int i=0; i < segments.size(); i++)
		{
			MenuSegment seg = (MenuSegment)segments.get(i);
			seg.mouseEvent(e);
		}
//		System.out.println(alpha);
		if (alpha > 100 && e.getID() == MouseEvent.MOUSE_PRESSED && !withinButtons)
		{
//			System.out.println("hey dude");
			hide();
		}
		
		if (changeCursor)
		{
			p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else
		{
			ProcessingUtils.releaseCursor(p,Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}
	public void mouseClicked(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	public void mouseEntered(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	public void mouseExited(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	public void mousePressed(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	public void mouseReleased(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	public void mouseDragged(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	public void mouseMoved(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	
	
}
