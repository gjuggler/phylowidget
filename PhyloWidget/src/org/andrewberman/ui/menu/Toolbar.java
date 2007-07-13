package org.andrewberman.ui.menu;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.Positionable;
import org.andrewberman.ui.ProcessingUtils;
import org.andrewberman.ui.Sizable;

import processing.core.PApplet;
import processing.core.PFont;

public class Toolbar extends Menu
{
	float width,height;
	boolean isActive;

	RoundRectangle2D.Float roundRect = new RoundRectangle2D.Float(0,0,0,0,0,0);

	/**
	 * If set to true, the toolbar will take up the entire width of the PApplet. 
	 */
	public boolean fullWidth = false;
	/**
	 * If true, this option will cause the menu to grab modal focus on opening.
	 */
	boolean isModal = true;
	
	public Toolbar(PApplet app)
	{
		super(app);
		clickToggles = true;
		hoverNavigable = false;
		clickAwayBehavior = Menu.CLICKAWAY_COLLAPSES;
		actionOnMouseDown = true;
		useCameraCoordinates = false;
		useHandCursor = false;
		layout();
		show();
		if (fullWidth)
		{
			x = style.strokeWidth;
			y = style.strokeWidth;
		} else
		{
			x = style.strokeWidth + style.margin/2;
			y = style.strokeWidth + style.margin/2;
		}
	}
	
	public void drawBefore()
	{
		if (fullWidth)
			setFullWidth();
		roundRect.setRoundRect(x, y, width, height, height*style.roundOff, height*style.roundOff);
		/*
		 * Now draw the rectangle using extra-spiffy Java2D gradients.
		 */
		/*
		 * Draw the first gradient: a full-height gradient.
		 */
		g2.setPaint(style.getGradient(y,y+height));
		g2.fill(roundRect);
		/*
		 * Draw a translucent gradient on top of the first, starting halfway
		 * and going to the bottom.
		 */
		Composite oldC = g2.getComposite();
		AlphaComposite c = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f);
		g2.setComposite(c);
		g2.setPaint(style.getGradient(y+height, y+height/3));
		g2.fillRect((int)x, (int)(y+height/2), (int)width, (int)height/2);
		g2.setComposite(oldC);
		/*
		 * Finally, draw the stroke on top of everything.
		 */
		RenderingHints rh = menu.g2.getRenderingHints();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); 
		g2.setPaint(style.strokeColor);
		g2.setStroke(style.stroke);
		g2.draw(roundRect);
		g2.setRenderingHints(rh);
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
		return new ToolbarMenuItem(label);
	}
	
	public MenuItem add(MenuItem item)
	{
		super.add(item);
		/*
		 * We need to modify this method a little bit, so that when
		 * an item is added to this Toolbar, we automatically call
		 * show() so that the MenuItem that was just added is set to
		 * become visible.
		 */
		show();
		return item;
	}
	
	public void hide()
	{
		/*
		 * A toolbar should never be hidden, so we're going to override
		 * this method to only hide the ToolBarMenuItems' children.
		 */
		for (int i=0; i < items.size(); i++)
		{
			((MenuItem)items.get(i)).hideAllChildren();
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
		float descent = ProcessingUtils.getTextDescent(g,font,fontSize,true);
		float ascent = ProcessingUtils.getTextAscent(g,font,fontSize,true);
		float textHeight = descent+ascent;
		float itemHeight = textHeight + style.padY*2;
		float fontOffsetY = itemHeight/2 + textHeight/2 - descent;
		/*
		 * Set the width, height and position for the top-level items.
		 */
		float xOffset = 0;
		float yOffset = style.margin;
		for (int i=0; i < items.size(); i++)
		{
			xOffset += style.margin;
			MenuItem item = (MenuItem)items.get(i);
			float itemWidth = item.getWidth();
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
			if (item instanceof ToolbarMenuItem)
			{
				ToolbarMenuItem tbi = (ToolbarMenuItem) item;
				tbi.textOffsetX = style.padX;
				tbi.textOffsetY = fontOffsetY;
			}
			xOffset += itemWidth;
		}
		/*
		 * Set this Toolbar's width and height.
		 */
		if (fullWidth)
			setFullWidth();
		else
			width = xOffset + style.margin;
		height = itemHeight + style.padY*2;
		/*
		 * Trigger the recursive layout.
		 */
		super.layout();
	}
	
	void setFullWidth()
	{
		width = canvas.width - style.strokeWidth*2;
	}
	
	protected void getRect(Rectangle2D.Float rect, Rectangle2D.Float buff)
	{
		if (isVisible())
		{
			buff.setRect(x,y,width,height);
			Rectangle2D.union(rect, buff, rect);
		}
		super.getRect(rect, buff);
	}
	
}
