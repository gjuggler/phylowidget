package org.andrewberman.ui;

import org.andrewberman.ui.ifaces.Malleable;
import org.andrewberman.ui.ifaces.Positionable;

public class LayoutUtils
{

	public static void alignRight (Malleable moveMe, Malleable alignToMe)
	{
		alignRight(moveMe, new UIRectangle(alignToMe));
	}
	
	public static void alignRight(Malleable moveMe, UIRectangle r)
	{
		float hiX = (float) (r.getX()+r.getWidth());
		moveMe.setX(hiX-moveMe.getWidth());
	}

	public static void centerHorizontal(Malleable m, UIRectangle r)
	{
		centerHorizontal(m, r.x, r.x+r.width);
	}
	
	public static void centerHorizontal(Malleable m, float lo, float hi)
	{
		float offsetX = ((hi-lo)-(m.getWidth()))/2;
		m.setPosition(lo+offsetX, m.getY());
	}
	
	public static void centerVertical(Malleable m, float lo, float hi)
	{
		float offsetY = ((hi-lo)-(m.getHeight()))/2;
		m.setPosition(m.getX(), lo+offsetY);
	}
	
	public static void center2D(Malleable m, float x, float y, float w, float h)
	{
		
	}
	
}
