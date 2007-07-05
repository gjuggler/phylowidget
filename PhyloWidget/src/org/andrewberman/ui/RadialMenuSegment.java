/**
 * 
 */
package org.andrewberman.ui;

import java.awt.geom.Area;
import java.lang.reflect.Method;

import org.andrewberman.tween.Tween;
import org.andrewberman.tween.TweenListener;
import org.andrewberman.tween.TweenQuad;

class RadialMenuSegment implements TweenListener
{

// Model.
	public String name;
	public char shortcut;

// View.
	public Area area;
	
	/** Area of the segment, transformed and scaled to screen-space. */
	public Area modelArea;
	/** Area of the segment, transformed and scaled for drawing to the off-screen buffer. */
	public Area bufferArea;
	public float thetaLo;
	public float thetaHi;
	public float thetaMid;
	public int alpha = 255;
	/** Tween for changing the alpha of this segment. */
	public Tween tween;
	
	/** Offset value for this segment's text to be drawn. */
	public float textX;
	/** Offset value for this segment's text to be drawn. */
	public float textY;
	/** Width of this segment's text. */
	public float textWidth;
	/** Offset value for this segment's hint character. */
	public float hintX;
	/** Offset value for this segment's hint character. */
	public float hintY;
	
// Controller.
	public int state = RadialMenu.UP;
	public boolean clickedInside = false; // True if the button has been clicked within its boundary.
	
// Menu action call.
	private Object object;
	private String function;
	
	public RadialMenuSegment(Object p, String function, String name, char shortcut)
	{
		this.object = p;
		this.function = function;
		this.name = name;
		this.shortcut = shortcut;
		
		tween = new Tween(this,new TweenQuad(),"out",255,255,30,false);
	}
	
	public void performAction(String name)
	{
		try
		{
			Class functionArgs[] = new Class[1];
			functionArgs[0] = String.class;
			Method m = object.getClass().getDeclaredMethod(function, functionArgs);
			Object args[] = new Object[1];
			args[0] = name;
			m.invoke(object, args);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void fadeOut()
	{
		tween.continueTo(0, 15);
	}
	
	public void hide()
	{
		tween.continueTo(0,5);
		tween.fforward();
	}
	
	public void show()
	{
		tween.continueTo(255,5);
		tween.fforward();
	}

	public void tweenEvent(Tween source, int eventType)
	{
		this.alpha = Math.round(source.position);
	}
}