package org.andrewberman.evogame;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.menu.Menu;
import org.andrewberman.ui.menu.MenuItem;

import processing.core.PApplet;

public class DragDropImage extends Menu
{
	private float downMouseX;
	private float downMouseY;
	private float downPosX;
	private float downPosY;
	private Image img;
	private boolean dragging;

	public DragDropImage(PApplet app)
	{
		super(app);
	}

	@Override
	public void setOptions()
	{
		super.setOptions();
		useHandCursor = true;
		modalFocus = true;
	}

	@Override
	public synchronized void draw()
	{
		if (img == null)
		{
			canvas.fill(255, 0, 0);
			canvas.rect(x, y, width, height);
		}
		super.draw();
	}

	@Override
	public synchronized void layout()
	{
		super.layout();
		setSize(64, 64);
	}

	private Rectangle2D.Float rectF = new Rectangle2D.Float();

	@Override
	protected boolean containsPoint(Point pt)
	{
		rectF.setRect(x, y, width, height);
		return rectF.contains(pt);
	}

	boolean isOpen = false;

	@Override
	protected void visibleMouseEvent(MouseEvent e, Point tempPt)
	{
		super.visibleMouseEvent(e, tempPt);
		//		if (mouseInside || dragging)
		//			setCursor(Cursor.HAND_CURSOR);
		//		
		//		if (!mouseInside && !dragging)
		//			return;

		if (e.getID() == MouseEvent.MOUSE_RELEASED)
		{
			if (isOpen)
			{
				close();
				isOpen = false;
			}
		}
		if (e.getID() == MouseEvent.MOUSE_PRESSED)
		{
			downMouseX = tempPt.x;
			downMouseY = tempPt.y;
			downPosX = this.x;
			downPosY = this.y;
			if (mouseInside)
			{
				open();
				isOpen = true;
			}
		}
		if (!mouseInside && !dragging)
			return;
		if (e.getID() == MouseEvent.MOUSE_DRAGGED)
		{
			dragging = true;
			float dX = tempPt.x - downMouseX;
			float dY = tempPt.y - downMouseY;
			setPosition(downPosX + dX, downPosY + dY);
		} else
			dragging = false;
	}

	@Override
	public boolean isOpen()
	{
		return true;
	}

	@Override
	public MenuItem create(String label)
	{
		return null;
	}

}
