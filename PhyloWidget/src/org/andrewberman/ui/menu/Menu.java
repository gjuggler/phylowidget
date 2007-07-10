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
import processing.opengl.PGraphicsOpenGL;

public abstract class Menu extends MenuItem implements UIObject, Positionable
{
	public static final int START_SIZE = 100;
	public static final int OFFSET = 10;
	Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 0, 0);
	Rectangle2D.Float buff = new Rectangle2D.Float(0, 0, 0, 0);
	protected static PApplet p = PhyloWidget.p;

	public PGraphicsJava2D pg;
	public Palette style = Palette.defaultSet;
	protected Graphics2D g2;
	protected MenuItem currentlySelected;
	protected float x, y;
	protected boolean justShown = false;

	/**
	 * If true, a mouse hover will expand a menu item.
	 */
	boolean hoverNavigable = true;
	/**
	 * If true, a click will hide a submenu as well as show it.
	 * This option works best when set to the opposite of the above
	 * hoverNavigable option.
	 */
	boolean clickToggles = false;
	/**
	 * If true, only one of this MenuItem's submenus will be allowed to be
	 * shown at once.
	 */
	boolean singletNavigation = true;
	/**
	 * Defines the behavior of a click that happens outside the bounds of the menu 
	 * and its sub-menus).
	 */
	int clickAwayBehavior;
	public static final int CLICKAWAY_HIDES = 0;
	public static final int CLICKAWAY_COLLAPSES = 1;
	public static final int CLICKAWAY_IGNORED = 2;
	
	public Menu()
	{
		super();
		setMenu(this);
		setSize(START_SIZE, START_SIZE);
	}

	protected void setSize(int w, int h)
	{
		pg = (PGraphicsJava2D) p.createGraphics(w, h, PApplet.JAVA2D);
		// pg.smooth();
		g2 = pg.g2;
//		pg.hint(PConstants.ENABLE_NATIVE_FONTS); // Native fonts are nice!
		// g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
		// RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		// g2.setRenderingHint(RenderingHints.KEY_RENDERING,
		// RenderingHints.VALUE_RENDER_SPEED);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_PURE);
		// g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		// RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		// g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);
	}

	public void setPosition(float x, float y)
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

	public void hide()
	{
		super.hide();
		hideChildren();
	}
	
	public void hideMenu()
	{
		hideAllChildren();
	}

	public void draw()
	{
		if (menu != this)
		{
			drawBefore();
			super.draw();
			drawAfter();
			return;
		}
		resizeBuffer();
		if (!isVisible())
			return;
		pg.beginDraw();
		pg.background(255, 0);
		drawBefore();
		super.draw(); // draws all of the sub segments.
		drawAfter();
			pg.modified = true;
		pg.endDraw();
		drawToCanvas();
	}

	protected void drawBefore()
	{
		// Subclasses can use this to do some of their own drawing.
	}

	protected void drawAfter()
	{
		// Subclasses can use this to do some of their own drawing.
	}

	protected void drawToCanvas()
	{
		int w = PApplet.round(rect.width + OFFSET * 2);
		int h = PApplet.round(rect.height + OFFSET * 2);
		p.g.image(pg, PApplet.round(x - OFFSET), PApplet.round(y - OFFSET),
				w,h, 0, 0, w, h);
		if (p.g instanceof PGraphicsOpenGL)
		{
			PGraphicsOpenGL gl = (PGraphicsOpenGL) p.g;
		}
	}

	protected float[] getOverhang()
	{
		rect.setFrame(0, 0, 0, 0);
		buff.setFrame(0, 0, 0, 0);
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem seg = (MenuItem) items.get(i);
			seg.getRect(rect, buff);
		}
		float dx = 0;
		float dy = 0;
		final int PAD = 50;
		if (rect.x < 0)
			dx += -rect.x + PAD;
		if (rect.x + rect.width > pg.width)
			dx += rect.x + rect.width - pg.width + PAD;
		if (rect.y < 0)
			dy += -rect.y + PAD;
		if (rect.y + rect.height > pg.height)
			dy += rect.y + rect.height - pg.height + PAD;

		float[] overhang = { dx, dy };
		return overhang;
	}

	protected void resizeBuffer()
	{
		float[] overhang = getOverhang();
		if (overhang[0] > 0 || overhang[1] > 0)
		{
			setSize(pg.width + (int) overhang[0] + 1, pg.height
					+ (int) overhang[1] + 1);
		}
	}
	
	protected boolean containsPoint(Point pt)
	{
		return false;
	}

	// protected void getRect(Rectangle2D.Float rect, Rectangle2D.Float buff)
	// {
	// super.getRect(rect, buff);
	// }

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

	protected void mouseEvent(MouseEvent e, Point p)
	{
		super.mouseEvent(e,p);
		// case (MouseEvent.MOUSE_CLICKED):
		if (!mouseInside && isVisible() && e.getID() == MouseEvent.MOUSE_PRESSED)
		{
			switch (clickAwayBehavior)
			{
				case (CLICKAWAY_HIDES):
					if (justShown)
						justShown = false;
					else
						hide();
					break;
				case (CLICKAWAY_COLLAPSES):
					System.out.println(label + "  "+mouseInside);
					setOpenItem(null);
					break;
				case (CLICKAWAY_IGNORED):
				default:
					break;
			}
//			if (justShown)
//				justShown = false;
//			else 
		}
	}
	
	public void mouseEvent(MouseEvent e)
	{
		if (!isVisible())
			return;
		// if (e.getID() == MouseEvent.MOUSE_PRESSED ||
		// e.getID() == MouseEvent.MOUSE_CLICKED)
		// {
		// System.out.println(e);
		// System.out.println(x);
		// }

		Point pt = new Point(e.getX(), e.getY());
		ProcessingUtils.screenToModel(pt);
//		System.out.println(pt.x+"   "+pt.y);
		pt.translate(-x + OFFSET, -y + OFFSET);

		// Recurse through sub-menus with this mouse event.
		mouseEvent(e, pt);

		switch (e.getID())
		{
			case (MouseEvent.MOUSE_MOVED):
				if (mouseInside)
				{
					p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else
				{
					ProcessingUtils.releaseCursor(p, Cursor
							.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				break;
			case (MouseEvent.MOUSE_PRESSED):
				
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
