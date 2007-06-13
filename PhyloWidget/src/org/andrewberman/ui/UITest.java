package org.andrewberman.ui;

import processing.core.PApplet;

public class UITest extends PApplet
{

	private static final long serialVersionUID = 1L;

	public void setup()
	{
		size(400, 100, P3D);
		
		// Do something to set up the test.
		PTextInput input = new PTextInput(this, 64, 25, 70, 350);
		input.insert("Text is fun!", 0);
	}

	public void draw()
	{
		background(255, 255, 255);
//		rect(0,0,50,50);
		// this.smooth();
		 rotate(PI/8);
		// scale(.5f);
	}

}
