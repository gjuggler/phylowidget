package org.andrewberman.ui.camera;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import org.andrewberman.ui.FocusManager;

import processing.core.PApplet;

public class MovableCamera extends Camera implements MouseWheelListener,
		KeyListener, MouseMotionListener, MouseListener
{
	protected PApplet p;

	Point2D.Float pt = new Point2D.Float(0, 0);
	protected int NUDGE_DISTANCE;
	protected float NUDGE_SCALE;

	protected boolean mouseInside = false;
	/**
	 * If set to true, then this camera will start scrolling when the mouse is
	 * close to the edge of the screen.
	 */
	public boolean enableSideScrolling = false;

	public MovableCamera(PApplet p)
	{
		super();
		this.p = p;
		makeResponsive();

		NUDGE_DISTANCE = p.width / 5;
		NUDGE_SCALE = 10f / (float) NUDGE_DISTANCE;
	}

	public void makeResponsive()
	{
		p.addMouseListener(this);
		p.addMouseMotionListener(this);
		p.addMouseWheelListener(this);
		p.addKeyListener(this);
	}

	public void makeUnresponsive()
	{
		p.removeMouseListener(this);
		p.removeMouseMotionListener(this);
		p.removeMouseWheelListener(this);
		p.removeKeyListener(this);
	}

	public void update()
	{
		super.update();
		scroll();
		applyTransformations();
	}

	protected void applyTransformations()
	{
		/*
		 * Translate by half the stage width and height to re-center the stage
		 * at (0,0).
		 */
		p.translate(getStageWidth() / 2.0f, getStageHeight() / 2.0f);
		/*
		 * Now scale.
		 */
		p.scale(getZ());
		/*
		 * Then translate.
		 */
		p.translate(-getX(), -getY());
	}

	public void scroll()
	{
		/*
		 * Handle the edge scrolling.
		 */
		if (mouseInside && enableSideScrolling
				&& !FocusManager.instance.isModal())
		{
			pt.setLocation(p.mouseX, p.mouseY);
			float zoomMultiplier = NUDGE_SCALE / getZ();
			float dy = 0;
			float dx = 0;
			if (pt.y > p.height - NUDGE_DISTANCE)
			{
				dy = NUDGE_DISTANCE - (p.height - pt.y);
			} else if (pt.y < NUDGE_DISTANCE)
			{
				dy = -(NUDGE_DISTANCE - pt.y);
			}
			if (pt.x > p.width - NUDGE_DISTANCE)
			{
				dx = NUDGE_DISTANCE - (p.width - pt.x);
			} else if (pt.x < NUDGE_DISTANCE)
			{
				dx = -(NUDGE_DISTANCE - pt.x);
			}
			if (dy != 0 || dx != 0)
			{
				this.nudge(dx * zoomMultiplier, dy * zoomMultiplier);
			}
		}
	}

	public float getStageWidth()
	{
		return p.width;
	}

	public float getStageHeight()
	{
		return p.height;
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
		this.zTween.stop();

		float rotVal = (float) Math.abs(e.getWheelRotation());
		rotVal = Math.min(rotVal, 1);
		// System.out.println(rotVal);
		// int rotDir = (int) Math.signum(e.getWheelRotation());
		int rotDir = (e.getWheelRotation() > 0 ? 1 : -1);
		float mult = (float) Math.pow(rotVal * .75, rotDir);
		this.zoomBy(mult);

		pt.setLocation(e.getX(), e.getY());
		float dx = p.width / 2 - pt.x;
		float dy = p.height / 2 - pt.y;

		// if endzoom is bigger, we want to make dx smaller.
		dx *= rotDir * .75;
		dy *= rotDir * .75;

		this.nudge(dx / getZ(), dy / getZ());
	}

	public void keyEvent(KeyEvent e)
	{
		// Only want keypresses here.
		if (e.getID() != KeyEvent.KEY_PRESSED)
			return;
		// System.out.println(e);

		int code = e.getKeyCode();
		switch (code)
		{
			case (81): // Q
			// this.zoomBy(2);
				break;
			case (87): // W
			// this.zoomBy(.5f);
				break;
			case (37): // Left
			// this.nudge(-10, 0);
				break;
			case (39): // Right
			// this.nudge(10, 0);
				break;
		}

	}

	public void mouseEvent(MouseEvent e)
	{
		int type = e.getID();
		switch (type)
		{
			case (MouseEvent.MOUSE_MOVED):
			case (MouseEvent.MOUSE_DRAGGED):
				break;
			case (MouseEvent.MOUSE_EXITED):
				mouseInside = false;
				break;
			case (MouseEvent.MOUSE_ENTERED):
				mouseInside = true;
				break;
			case (MouseEvent.MOUSE_PRESSED):
				// Point2D.Float pt = new Point2D.Float(e.getX(), e.getY());
				// ProcessingUtils.screenToModel(p, pt);
				// System.out.println(pt);
				break;
		}
	}

	public void keyPressed(KeyEvent e)
	{
		keyEvent(e);
	}

	public void keyReleased(KeyEvent e)
	{
		keyEvent(e);
	}

	public void keyTyped(KeyEvent e)
	{
		keyEvent(e);
	}

	public void mouseDragged(MouseEvent e)
	{
		mouseEvent(e);
	}

	public void mouseMoved(MouseEvent e)
	{
		mouseEvent(e);
	}

	public void mouseClicked(MouseEvent e)
	{
		mouseEvent(e);
	}

	public void mouseEntered(MouseEvent e)
	{
		mouseEvent(e);
	}

	public void mouseExited(MouseEvent e)
	{
		mouseEvent(e);
	}

	public void mousePressed(MouseEvent e)
	{
		mouseEvent(e);
	}

	public void mouseReleased(MouseEvent e)
	{
		mouseEvent(e);
	}

}
