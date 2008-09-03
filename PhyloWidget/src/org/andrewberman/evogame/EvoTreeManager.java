package org.andrewberman.evogame;

import java.awt.Rectangle;

import org.phylowidget.TreeManager;

import processing.core.PApplet;
import processing.core.PGraphicsJava2D;

public class EvoTreeManager extends TreeManager
{
	Rectangle clipRect;
	public EvoTreeManager(PApplet p)
	{
		super(p);
		clipRect = new Rectangle(0,0,100,100);
	}

	@Override
	public void draw()
	{
		PGraphicsJava2D pg = (PGraphicsJava2D) p.g;
		super.draw();
		
		p.noFill();
		p.strokeWeight(2);
		p.stroke(0);
		p.rect(0, 0, p.width/2,p.height);
		p.fill(255);
		p.noStroke();
		p.rect(p.width/2,0,p.width/2,p.height);
	}
	
	@Override
	public void fillScreen()
	{
		camera.fillScreen(0.5f);
		camera.nudgeTo(p.width*.6f, 0);
	}
	
}
