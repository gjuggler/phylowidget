package org.andrewberman.ui.menu;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.ProcessingUtils;
import org.andrewberman.ui.UIObject;
import org.phylowidget.PhyloWidget;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphicsJava2D;

public abstract class Menu extends MenuItem implements UIObject
{
	public static final int START_SIZE = 100;
	public static final int PAD = 10;
	static Rectangle2D.Float rect = new Rectangle2D.Float(0,0,0,0);
	static Rectangle2D.Float buff = new Rectangle2D.Float(0,0,0,0);
	
	protected PApplet p = PhyloWidget.p;
	public PGraphicsJava2D pg;
	protected Graphics2D g2;
	protected float x,y;
	protected boolean justShown = false;
	
	public Menu()
	{
		super();
		this.menu = this;
		setSize(START_SIZE,START_SIZE);
	}
	
	protected void setSize(int w, int h)
	{
		pg = (PGraphicsJava2D) p.createGraphics(w, h, PApplet.JAVA2D);
		pg.smooth();
		g2 = pg.g2;
		pg.hint(PConstants.ENABLE_NATIVE_FONTS); // Native fonts are nice!
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
//		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
//		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	}

	public void setLocation(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public void show()
	{
		super.show();
		showChildren();
		justShown = true;
	}
	
	public void hideMenu()
	{
		hideAllChildren();
	}
	
	public void draw()
	{
		resizeBuffer();
		if (!isVisible()) return;
		pg.beginDraw();
		pg.background(255,0);
		drawBelow();
		super.draw(); // draws all of the sub segments.
		drawAbove();
		pg.modified = true;
		pg.endDraw();
		drawToCanvas();
	}
	
	protected void drawBelow()
	{
		// Subclasses can use this to do some of their own drawing.
	}
	
	protected void drawAbove()
	{
		// Subclasses can use this to do some of their own drawing.
	}
	
	protected void drawToCanvas()
	{
		int w = PApplet.round(rect.width)+PAD;
		int h = PApplet.round(rect.height)+PAD;
		p.g.image(pg, PApplet.round(x-PAD), PApplet.round(y-PAD), w, h, // destination.
				0, 0, w, h); // source.
//		p.image(pg, x, y);
	}
	
	protected float[] getOverhang()
	{
		rect.setFrame(0,0,0,0);
		buff.setFrame(0,0,0,0);
		for (int i=0; i < items.size(); i++)
		{
			MenuItem seg = (MenuItem)items.get(i);
			seg.getRect(rect,buff);
		}
		float dx=0;
		float dy=0;
		final int PAD = 50;
		if (rect.x < 0)
			dx += -rect.x + PAD;
		if (rect.x + rect.width > pg.width)
			dx += rect.x + rect.width - pg.width + PAD;
		if (rect.y < 0)
			dy += -rect.y + PAD;
		if (rect.y + rect.height > pg.height)
			dy += rect.y + rect.height - pg.height + PAD;

		float[] overhang = {dx,dy};
		return overhang;
	}
	
	protected void resizeBuffer()
	{
		float[] overhang = getOverhang();
		if (overhang[0] > 0 || overhang[1] > 0)
			setSize(pg.width + (int)overhang[0]+1, pg.height + (int)overhang[1]+1);
	}
	
	protected boolean containsPoint(Point pt)
	{
		return false;
	}
	protected void getRect(Rectangle2D.Float rect, Rectangle2D.Float buff)
	{
		// Do nothing.
	}
	
	public void keyEvent(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case (KeyEvent.VK_ESCAPE):
				hide();
				break;
		}
		
		super.keyEvent(e);
	}
	
	public void mouseEvent(MouseEvent e)
	{
		if (!isVisible()) return;
		
		Point pt = new Point(e.getX(),e.getY());
		ProcessingUtils.screenToModel(pt);
		pt.translate(-x+PAD, -y+PAD);

		// Recurse through sub-menus with this mouse event.
		mouseEvent(e,pt);

		switch (e.getID())
		{
			case (MouseEvent.MOUSE_MOVED):
				if (mouseInside)
				{
					p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else
				{
					ProcessingUtils.releaseCursor(p,Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				break;
			case (MouseEvent.MOUSE_PRESSED):
				if (!mouseInside && isVisible())
				{
					if (justShown)
						justShown = false;
					else
						hide();
				}
				break;
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
