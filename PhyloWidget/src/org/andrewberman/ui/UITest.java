package org.andrewberman.ui;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphicsJava2D;

public class UITest extends PApplet
{

	private static final long serialVersionUID = 1L;

	public void setup()
	{
		size(400, 400, P3D);
		
		// Do something to set up the test.
//		PTextInput input = new PTextInput(this, 64, 25, 96, 350);
//		input.insert("Editing text is fun!", 0);
		
		PFont f = loadFont("TimesNewRoman-64.vlw");
		
		PRadialMenu menu = new PRadialMenu(this, 200, 200, 40);
		
		try
		{
			menu.addMenuItem("New",'+',"doSomething");
			menu.addMenuItem("Delete",'x',"doSomething");
			menu.addMenuItem("Move",'»',"doSomething");
			menu.addMenuItem("Something",'u',"doSomething");
		} catch (RuntimeException e)
		{
			e.printStackTrace();
		}
		
		textFont(f);
		this.textSize(16);
		if (this.g.getClass() == PGraphicsJava2D.class)
		{
			smooth();
		}
		frameRate(30);
		printMatrix();
	}

	public void doSomething(String s)
	{
		System.out.println(s);
	}
	
	public void draw()
	{
		background(255, 255, 255);
		fill(0);
		String fr = String.valueOf(Math.round(frameRate*10)/10);
		this.text(fr,5,20);
		
//		stroke(0,255);
//		ellipse(200,300,5,5);
		
//		scale(.25f);
//		rotate(PI/8);
	}

}
