package org.andrewberman.ui.menu;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

import org.andrewberman.ui.Color;
import org.phylowidget.PhyloWidget;

import processing.core.PApplet;
import processing.core.PGraphicsJava2D;

public class Toolbar extends ToolbarMenu
{
	PApplet p;
	
	public Toolbar(PApplet p)
	{
		super();
		this.p = p;
		this.x = style.pad;
		this.y = style.pad;
		useCameraCoordinates = false;
		useHandCursor = false;
		show();
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
	}
	
	public void draw()
	{
		p.pushMatrix();
		/*
		 * Clear the matrix so we're drawing to screen coordinates.
		 * P3D and openGL do this differently from java2d.
		 */
		if (PhyloWidget.java2D)
			p.resetMatrix();
		else
			p.camera();
		/*
		 * Now draw the rectangle. If we're using Java2D, do some extra-spiffy
		 * gradient stuff.
		 */
		float menuHeight = itemHeight + style.pad*2;
		if (PhyloWidget.java2D)
		{
			PGraphicsJava2D pgj = (PGraphicsJava2D) p.g;
			Graphics2D g2 = pgj.g2;
			g2.setPaint(style.stateColors[MenuItem.UP]);
			g2.setPaint(style.getGradient(0,menuHeight));
			g2.fillRect((int)0, (int)0, p.width,(int)menuHeight);
			Composite oldC = g2.getComposite();
			AlphaComposite c = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f);
			g2.setComposite(c);
			g2.setPaint(style.getGradient(menuHeight, menuHeight/3));
			g2.fillRect((int)0, (int)menuHeight/2, p.width,(int)menuHeight/2);
			g2.setComposite(oldC);
		} else
		{
			p.fill(menu.style.stateColors[MenuItem.UP].getRGB());
			p.rect(0,0,p.width,menuHeight);
		}
		super.draw();
		p.popMatrix();
	}
}
