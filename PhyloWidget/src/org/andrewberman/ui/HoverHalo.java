package org.andrewberman.ui;

import java.awt.geom.Rectangle2D;

import org.andrewberman.phyloinfo.PhyloWidget;
import org.andrewberman.tween.Tween;
import org.andrewberman.tween.TweenListener;
import org.andrewberman.tween.TweenQuad;

import processing.core.PApplet;
import processing.core.PConstants;

public class HoverHalo implements TweenListener
{
	protected PApplet p = PhyloWidget.p;
	
	protected static final int FRAMES = 30;
	public static final int RECTANGLE = 0;
	public static final int ELLIPSE = 1;
	public int type = RECTANGLE;
	
	protected Rectangle2D.Float rect = new Rectangle2D.Float(0,0,0,0);
	
	protected Tween aTween;
	protected Tween wTween;
	protected Tween hTween;
	
	protected int color = p.color(0,100,220);
	public boolean hidden = true;
	
	public HoverHalo()
	{
		aTween = new Tween(this, TweenQuad.instance, "inout", .75f,  .25f, FRAMES, false);
		wTween = new Tween(this, TweenQuad.instance, "inout", 1.25f, 1.05f, FRAMES, false);
		hTween = new Tween(this, TweenQuad.instance, "inout", 1.25f, 1.05f, FRAMES, false);
	}

	public void draw()
	{
		aTween.update();
		wTween.update();
		hTween.update();
		
		if (hidden)return;
		
		p.stroke(color,255*aTween.position);
		p.strokeWeight(Math.max(rect.width/20,4));
		p.noFill();
		float w = wTween.position * rect.width;
		float h = hTween.position * rect.height;
		float x = (float) rect.getCenterX();
		float y = (float) rect.getCenterY();
//		p.ellipse(x,y,5,5);
		switch (type)
		{
			case (RECTANGLE):
				p.rect(x - w/2, y - h/2, w, h);
//				p.rect(rect.x,rect.y,rect.width,rect.height);
				break;
			case (ELLIPSE):
				p.ellipse(x, y, w, h);
				break;
		}
		
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public void hide()
	{
		hidden = true;
	}
	
	public void show()
	{
		hidden = false;
	}
	
	public void tweenEvent(Tween source, int eventType)
	{
		if (eventType == Tween.FINISHED)
			source.yoyo();
	}
	
	public void setRect(float cx, float cy, float w, float h)
	{
		rect.setFrameFromCenter(cx, cy, cx - w/2, cy - h/2);
	}
	
}
