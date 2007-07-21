package org.andrewberman.ui.menu;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.andrewberman.tween.PropertyTween;
import org.andrewberman.tween.Tween;
import org.andrewberman.tween.TweenQuad;
import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.ifaces.Positionable;
import org.andrewberman.ui.ifaces.Sizable;

import processing.core.PApplet;
import processing.core.PFont;

/**
 * A <code>Toolbar</code> is a menu that acts more or less like a
 * Windows-style toolbar. The two main options for the Toolbar class are:
 * <code>fullWidth</code>, which if set to true will draw itself across the
 * entire width of the screen, and <code>isModal</code>, which if set to true
 * will make the toolbar grab modal focus from the FocusManager when one of its
 * menus is opened.
 * 
 * @author Greg
 */
public class Toolbar extends Menu
{
	float width, height;
	boolean isActive;
	RoundRectangle2D.Float roundRect = new RoundRectangle2D.Float(0, 0, 0, 0,
			0, 0);
	Rectangle2D.Float buffRect = new Rectangle2D.Float(0, 0, 0, 0);

	/**
	 * If set to true, the toolbar will take up the entire width of the PApplet.
	 */
	public boolean fullWidth = false;
	/**
	 * If true, this option will cause the menu to grab modal focus from the
	 * FocusManager upon opening.
	 */
	public boolean isModal = true;

	public Toolbar(PApplet app)
	{
		super(app);
		layout();
		show();
	}

	protected void setOptions()
	{
		/*
		 * Override some default options from the Menu class.
		 */
		useCameraCoordinates = false;
		clickToggles = true;
		hoverNavigable = false;
		clickAwayBehavior = Menu.CLICKAWAY_COLLAPSES;
		actionOnMouseDown = true;
		useHandCursor = true;
		autoDim = true;
		/*
		 * Do some automatic positioning.
		 */
		if (fullWidth)
		{
			x = style.strokeWidth;
			y = style.strokeWidth;
		} else if (!useCameraCoordinates)
		{
			x = style.strokeWidth + style.margin;
			y = style.strokeWidth + style.margin;
		}
	}

	public void drawBefore()
	{
		if (fullWidth)
			setFullWidth();
		roundRect.setRoundRect(x, y, width, height, height * style.roundOff,
				height * style.roundOff);
		/*
		 * Now draw the rectangle using extra-spiffy Java2D gradients.
		 */
		/*
		 * Draw the first gradient: a full-height gradient.
		 */
		buff.g2.setPaint(style.getGradient(y, y + height));
		buff.g2.fill(roundRect);
		/*
		 * Draw a translucent gradient on top of the first, starting halfway and
		 * going to the bottom.
		 */
		Composite oldC = buff.g2.getComposite();
		AlphaComposite c = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				0.5f * menu.alpha);
		buff.g2.setComposite(c);
		buff.g2.setPaint(style.getGradient(y + height, y + height / 3));
		buff.g2.fillRect((int) x, (int) (y + height / 2), (int) width,
				(int) height / 2);
		buff.g2.setComposite(oldC);
		/*
		 * Finally, draw the stroke on top of everything.
		 */
		RenderingHints rh = menu.buff.g2.getRenderingHints();
		buff.g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		buff.g2.setPaint(style.strokeColor);
		buff.g2.setStroke(style.stroke);
		buff.g2.draw(roundRect);
		buff.g2.setRenderingHints(rh);

		super.drawBefore();
	}

	protected void setOpenItem(MenuItem item)
	{
		super.setOpenItem(item);
		if (item == null)
		{
			isActive = false;
			if (isModal && isRootMenu())
				FocusManager.instance.removeFromFocus(this);
		} else
		{
			isActive = true;
			if (isModal && isRootMenu())
				FocusManager.instance.setModalFocus(this);
		}
	}

	public MenuItem create(String label)
	{
		return new ToolbarItem(label);
	}

	public MenuItem add(MenuItem item)
	{
		super.add(item);
		/*
		 * We need to modify this method a little bit, so that when an item is
		 * added to this Toolbar, we automatically call show() so that the
		 * MenuItem that was just added is set to become visible.
		 */
		show();
		return item;
	}
	
	public void hide()
	{
		/*
		 * A toolbar should never be hidden, so we're going to override this
		 * method to only hide the ToolBarMenuItems' children.
		 */
		for (int i = 0; i < items.size(); i++)
		{
			((MenuItem) items.get(i)).hideAllChildren();
		}
		/*
		 * Now I need to turn off my isActive flag.
		 */
		this.isActive = false;
		if (isModal && isRootMenu())
			FocusManager.instance.removeFromFocus(this);
	}

	public void show()
	{
		super.show();
	}

	public void layout()
	{
		/*
		 * Calculate the height of each row in this menu.
		 */
		PFont font = style.font;
		float fontSize = style.fontSize;
		float descent = UIUtils.getTextDescent(buff, font, fontSize, true);
		float ascent = UIUtils.getTextAscent(buff, font, fontSize, true);
		float textHeight = descent + ascent;
		float itemHeight = textHeight + style.padY * 2;
		float fontOffsetY = itemHeight / 2 + textHeight / 2 - descent;
		/*
		 * Set the width, height and position for the top-level items.
		 */
		float xOffset = style.margin;
		float yOffset = style.margin;
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem) items.get(i);
			float itemWidth = item.getTextWidth();
			if (item instanceof Sizable)
			{
				Sizable size = (Sizable) item;
				size.setSize(itemWidth, itemHeight);
			}
			if (item instanceof Positionable)
			{
				Positionable pos = (Positionable) item;
				pos.setPosition(x + xOffset, y + yOffset);
			}
			if (item instanceof ToolbarItem)
			{
				ToolbarItem tbi = (ToolbarItem) item;
				tbi.textOffsetX = style.padX;
				tbi.textOffsetY = fontOffsetY;
			}
			xOffset += itemWidth;
			/*
			 * I had been using the following line for adding padding between toolbar items,
			 * but it looks better without any space, so we're adding zero.
			 */
			if (i < items.size() - 1)
				xOffset += 0;
		}
		/*
		 * Set this Toolbar's width and height.
		 */
		if (fullWidth)
			setFullWidth();
		else
			width = xOffset + style.margin;
		height = itemHeight + style.padY * 2;
		/*
		 * Trigger the recursive layout.
		 */
		super.layout();
	}

	void setFullWidth()
	{
		width = canvas.width - style.strokeWidth * 2;
	}

	protected void getRect(Rectangle2D.Float rect, Rectangle2D.Float buff)
	{
		if (isVisible())
		{
			buff.setRect(x, y, width, height);
			Rectangle2D.union(rect, buff, rect);
		}
		super.getRect(rect, buff);
	}

	protected void itemMouseEvent(MouseEvent e, Point pt)
	{
		super.itemMouseEvent(e, pt);
		if (isActive)
			aTween.continueTo(fullAlpha);
	}

	protected boolean containsPoint(Point pt)
	{
		buffRect.setRect(0, 0, width, height);

		return buffRect.contains(pt);
	}
}
