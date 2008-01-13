package org.andrewberman.ui;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import processing.core.PApplet;

public class LabelHyperlink extends Label
{

	String url;
	boolean hovered;
	boolean pressed;
	
	public LabelHyperlink(PApplet p)
	{
		this(p,"");
	}

	public LabelHyperlink(PApplet p, String s)
	{
		super(p, s);
		url = s;
		setColor(60, 60, 220);
	}

	public void setURL(String s)
	{
		url = s;
	}
	
	public String getURL()
	{
		return url;
	}
	
	@Override
	public void draw()
	{
//		super.draw();
		cache();
		Color c = color;
		if (pressed)
			c = color.darker(80);
		
		app.fill(c.getRGB());
		app.textFont(FontLoader.instance.vera);
		app.textSize(fontSize);
		app.smooth();
		app.textAlign(PApplet.LEFT);
		app.text(label, x, y);
		
		if (hovered)
		{
			app.strokeWeight(fontSize/10);
			app.stroke(c.getRGB());
			app.line(x, y+fontSize/4, x+cacheW, y+fontSize/10);
		}
	}
	
	UIRectangle tmpR = new UIRectangle();

	boolean containsPoint(Point pt)
	{
		cache();
		tmpR.setRect(x, y - cacheA, cacheW, cacheH);
		return tmpR.contains(pt);
	}

	void openURL()
	{
		app.link(getURL(),"_new");
//		app.getAppletContext().
	}
	
	@Override
	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		super.mouseEvent(e, screen, model);
		
		if (containsPoint(screen))
		{
			if (UIUtils.getCursorOwner() == null)
			{
				UIUtils.setCursor(this, app, Cursor.HAND_CURSOR);
				hovered = true;
			}
			
			if (e.getID() == MouseEvent.MOUSE_CLICKED)
			{
				openURL();
			} else if (e.getID() == MouseEvent.MOUSE_PRESSED)
				pressed = true;
		} else
		{
			hovered = false;
			UIUtils.releaseCursor(this, app);
		}
		if (e.getID() == MouseEvent.MOUSE_RELEASED)
			pressed = false;
	}
}
