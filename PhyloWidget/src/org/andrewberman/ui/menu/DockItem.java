package org.andrewberman.ui.menu;

import java.awt.Graphics2D;

import org.andrewberman.tween.Tween;
import org.andrewberman.tween.TweenFriction;
import org.andrewberman.tween.TweenListener;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIUtils;

import processing.core.PGraphicsJava2D;
import processing.core.PImage;

/**
 * The <code>DockItem</code> class represents an item within a Dock menu. As
 * with most other <code>XXXItem</code> classes, the average user won't be
 * interacting too heavily with this class, as the base menu class (in this case
 * <code>Dock</code>) should provide a convenient method for the creation and
 * addition of its preferred type of <code>MenuItem</code>.
 * <p>
 * TODO: Allow DockItems to track their own active/inactive states, and have different
 * icons for each state.
 * 
 * @author Greg
 */
public class DockItem extends MenuItem implements TweenListener
{
	Tween tween;
	PImage icon;

	// float subX,subY;
	// float textX,textY;

	public DockItem(String label)
	{
		super(label);
		tween = new Tween(this, TweenFriction.tween, Tween.OUT, 0, 0, 6);
	}

	public void setFile(String file)
	{
		if (menu != null && menu.canvas != null)
			icon = menu.canvas.loadImage(file);
	}

	public void draw()
	{
		tween.update();

		if (icon != null)
		{
			if (UIUtils.isJava2D(menu.canvas))
			{
				/*
				 * If we're running on a Java2D canvas, then the Composite is already
				 * set. Just draw the image, and the alpha transparency will be there.
				 * 
				 * NOTE: the tint() function is VERY slow with Java2D, so don't use it!
				 */
				menu.canvas.image(icon, x, y, width, height);
			} else
			{
				/*
				 * If we're running on anything else, we need to use tint() to get
				 * the alpha correct.
				 */
				int alf = (int) (menu.alpha * 255);
				menu.canvas.tint(255, alf);
				menu.canvas.image(icon, x, y, width, height);
				menu.canvas.noTint();
			}
		}
		super.draw();
	}

	protected boolean containsPoint(Point p)
	{
		if (p.x < x || p.x > x + width || p.y < y || p.y > y + height)
			return false;
		return true;
	}

	public void setSize(float w, float h)
	{
		tween.continueTo(w);
	}

	// public void itemMouseEvent(MouseEvent e, Point pt)
	// {
	// if (containsPoint(pt))
	// mouseInside = true;
	// else
	// mouseInside = false;
	// super.itemMouseEvent(e,pt);
	// }

	protected void performAction()
	{
		super.performAction();
		// isSelected = true;
	}

	public void tweenEvent(Tween source, int eventType)
	{
		if (source == tween)
		{
			width = height = tween.getPosition();
		}
	}

}
