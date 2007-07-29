package org.phylowidget.ui;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.TextField;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.ifaces.UIObject;
import org.phylowidget.render.TreeRenderer;
import org.phylowidget.tree.RenderNode;

import processing.core.PApplet;
import processing.core.PGraphics;

public class Navigator implements UIObject
{
	PApplet p;
	TreeRenderer render;
	PGraphics buff;
	
	float x,y;
	
	int lastTreeMod;
	
	public Navigator(PApplet p)
	{
		UIUtils.loadUISinglets(p);
		EventManager.instance.add(this);
		
		this.p = p;
		setSize(p.width/5,p.height/5);
	}

	public void setSize(int w, int h)
	{
		buff = p.createGraphics(w, h, PApplet.JAVA2D);
		/*
		 * Align our new buffer to the lower-right corner.
		 */
		buff.smooth();
		x = p.width - buff.width;
		y = p.height - buff.height;
	}
	
	public void setRenderer(TreeRenderer r)
	{
		this.render = r;
	}
	
	public void draw()
	{
		if (render == null) return;
		if (lastTreeMod != render.getTree().modCount)
		{
			lastTreeMod = render.getTree().modCount;
			buff.beginDraw();
			buff.background(255,0);
			render.render(buff, 0, 0, buff.width, buff.height);
			buff.endDraw();
		}
		/*
		 * Draw the bufer on to the main PGraphics.
		 */
		p.pushMatrix();
		UIUtils.resetMatrix(p);
		p.image(buff, x, y);
		p.popMatrix();
	}
	
	public void focusEvent(FocusEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	public void keyEvent(KeyEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		// TODO Auto-generated method stub
		
	}

}
