package org.andrewberman.ui.tools;

import processing.core.PApplet;

public class Arrow extends Tool
{

	public Arrow(PApplet p)
	{
		super(p);
	}

	public boolean respondToOtherEvents()
	{
		return true;
	}

}
