package org.andrewberman.applets;

import processing.core.PApplet;

public class SmartApplet extends PApplet
{
	Globals g;
	
	public void destroy() {
		g.destroyGlobals();
	};
}
