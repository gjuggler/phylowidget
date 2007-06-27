package org.andrewberman.phyloinfo;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import org.andrewberman.camera.Camera;
import org.andrewberman.ui.ProcessingUtils;
import org.andrewberman.util.Position;

import processing.core.PApplet;

public class PhyloCamera extends Camera implements MouseWheelListener,
		KeyListener, MouseMotionListener, MouseListener
{

	private PApplet p;

	Point2D.Float pt = new Point2D.Float(0,0);
	private static int NUDGE_DISTANCE = 100;
	private static float NUDGE_SCALE = 20f / (float)NUDGE_DISTANCE;
	
	private boolean mouseInside = false;
	
	public PhyloCamera(PApplet app)
	{
		p = app;
		p.addKeyListener(this);
		p.addMouseWheelListener(this);
		p.addMouseMotionListener(this);
		p.addMouseListener(this);
	}

	public float getStageHeight()
	{
		// TODO Auto-generated method stub
		return (float) p.getHeight();
	}

	public float getStageWidth()
	{
		// TODO Auto-generated method stub
		return (float) p.getWidth();
	}

	public float getTranslationX()
	{
		return (float) getStageWidth() / 2.0f - getX();
	}

	public float getTranslationY()
	{
		return (float) getStageHeight() / 2.0f - getY();
	}

	public void update()
	{
		super.update();
		
		/*
		 * Handle the edge scrolling.
		 */
		if (mouseInside)
		{
			pt.setLocation(p.mouseX,p.mouseY);
			float zoomMultiplier = NUDGE_SCALE * 1.0f / getZ();
			float dy = 0;
			float dx = 0;
			if (pt.y > p.height - NUDGE_DISTANCE)
			{
				dy = NUDGE_DISTANCE - (p.height - pt.y);
			} else if(pt.y < NUDGE_DISTANCE)
			{
				dy = - (NUDGE_DISTANCE - pt.y);
			}
			if (pt.x > p.width - NUDGE_DISTANCE)
			{
				dx = NUDGE_DISTANCE - (p.width - pt.x);
			} else if (pt.x < NUDGE_DISTANCE)
			{
				dx = - (NUDGE_DISTANCE - pt.x);
			}
			if (dy != 0 || dx != 0)
			{
				this.nudge(dx * zoomMultiplier, dy * zoomMultiplier);
			}
		}
		
		/*
		 * Translate by half the stage width and height to re-center the stage
		 * at (0,0).
		 */
		p.translate(getStageWidth()/2.0f,getStageHeight()/2.0f);
		/*
		 * Now scale.
		 */
		p.scale(getZ());
		/*
		 * Then translate.
		 */
		p.translate(-getX(),-getY());
	}

	public void kill()
	{
		p.removeKeyListener(this);
		p.removeMouseWheelListener(this);
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
		this.zTween.stop();
		
		int rot = -e.getWheelRotation();
		this.zoomBy(1 + (float) (Math.signum(rot) * .25 * Math.sqrt(Math
				.abs(rot))));
		
		float endZoom = this.zTween.getFinish();
		float curZoom = this.zTween.getPosition();
		
		pt.setLocation(p.mouseX,p.mouseY);
		ProcessingUtils.mouseToModel(p, pt);
		System.out.println(pt);
		
		float dx = pt.x - this.xTween.position;
		float dy = pt.y - this.yTween.position;
		
		// if endzoom is bigger, we want to make dx smaller.
		if (curZoom > endZoom)
		{
			dx *= 1.25;
			dy *= 1.25;
		} else
		{
			dx *= .75;
			dy *= .75;
		}
		
		
		this.nudgeTo(pt.x - dx, pt.y - dy);
	}

	public void keyEvent(KeyEvent e)
	{
		// Only want keypresses here.
		if (e.getID() != KeyEvent.KEY_PRESSED) return;
//		System.out.println(e);
		
		int code = e.getKeyCode();
		switch (code)
		{
			case (81):
				this.zoomBy(2);
				break;
			case (87):
				this.zoomBy(.5f);
				break;
		}

	}

	public void mouseEvent(MouseEvent e)
	{
		int type = e.getID();
		
		switch(type)
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

	public void mouseDragged(MouseEvent e){mouseEvent(e);}

	public void mouseMoved(MouseEvent e){mouseEvent(e);}

	public void mouseClicked(MouseEvent e){mouseEvent(e);}

	public void mouseEntered(MouseEvent e){mouseEvent(e);}

	public void mouseExited(MouseEvent e){mouseEvent(e);}

	public void mousePressed(MouseEvent e){mouseEvent(e);}

	public void mouseReleased(MouseEvent e){mouseEvent(e);}

}
