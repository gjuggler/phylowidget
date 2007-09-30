package org.andrewberman.ui.menu;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIEvent;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.tween.Tween;
import org.andrewberman.ui.tween.TweenFriction;
import org.andrewberman.ui.tween.TweenListener;

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
	
	String iconFile;

	public DockItem()
	{
		super();
		tween = new Tween(this, TweenFriction.tween, Tween.OUT, 0, 0, 6);
	}

	public void setIcon(String file)
	{
		iconFile = file;
		if (menu != null && menu.canvas != null)
			icon = menu.canvas.loadImage(iconFile);
	}

	public void setMenu(Menu menu)
	{
		super.setMenu(menu);
		if (icon == null)
			setIcon(iconFile);
	}
	
	public void draw()
	{
		tween.update();

		if (icon != null)
		{
			/*
			 * Figure out the correctly scaled size of the icon.
			 */
			float pad = menu.style.padX;
			float effectiveW = width - pad*2;
			float effectiveH = height - pad*2;
			float xOffset = pad;
			float yOffset = pad;
			float w = 0;
			float h = 0;
			if (icon.width < icon.height)
			{
				h = effectiveH;
				w = icon.width * h / icon.height;
				xOffset = (effectiveW-w) / 2 + pad;
			} else
			{
				w = effectiveW;
				h = icon.height * w / icon.width;
				yOffset = (effectiveH-h) / 2 + pad;
			}
//			menu.canvas.rect(x, y, width, height);
			if (UIUtils.isJava2D(menu.canvas))
			{
				/*
				 * If we're running on a Java2D canvas, then the Composite is already
				 * set. Just draw the image, and the alpha transparency will be there.
				 * 
				 * NOTE: the tint() function is VERY slow with Java2D, so don't use it!
				 */
				menu.canvas.smooth();
				menu.canvas.image(icon, x+xOffset, y+yOffset, w, h);
				menu.canvas.noSmooth();
			} else
			{
				/*
				 * If we're running on anything else, we need to use tint() to get
				 * the alpha correct.
				 */
				int alf = (int) (menu.alpha * 255);
				menu.canvas.tint(255, alf);
//				float pad = menu.style.padX;
				menu.canvas.image(icon, x+pad, y+pad, width-2*pad, height-2*pad);
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

	public void tweenEvent(Tween source, int eventType)
	{
		if (source == tween)
		{
			width = height = tween.getPosition();
		}
	}

}
