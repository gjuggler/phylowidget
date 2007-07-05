package org.phylowidget;

import java.awt.RenderingHints;

import org.andrewberman.ui.ProcessingUtils;
import org.phylowidget.ui.FontLoader;
import org.phylowidget.ui.UIManager;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphicsJava2D;

public class PhyloWidget extends PApplet
{
	private static final long serialVersionUID = -7096870051293017660L;
	
	public static PhyloWidget p;

	public static TreeManager trees;
	public static UIManager ui;
	
	public static int WIDTH = 400;
	public static int HEIGHT = 400;

	public PhyloWidget()
	{
		super();
		p = this;
		
		// Creates, manages, and renders trees.
		trees = new TreeManager();
		// Creates and manages UI elements.
		ui = new UIManager();
	}

	public void setup()
	{
		frameRate(30f);
		
		trees.setup();
		ui.setup();
		trees.createTree("PhyloWidget");
	}

	public void draw()
	{
		background(255);
		drawFrameRate();
		translate(width/2,height/2);
		
		ProcessingUtils.setMatrix(this);
		
		trees.update();
		ui.update();
	}

	public void drawFrameRate()
	{
		textAlign(PApplet.LEFT);
		textFont(FontLoader.f32);
		fill(0);
		text(String.valueOf(round(frameRate*10)/10.0), 10, 25);	
	}
	
	
	
	public void size(int w, int h)
	{
		System.out.println("Size("+w+","+h+")");
		synchronized(this)
		{
			size(w,h,JAVA2D);
			if (g.getClass() == PGraphicsJava2D.class)
			{
				PGraphicsJava2D pg = (PGraphicsJava2D) p.g;
				p.hint(PConstants.ENABLE_NATIVE_FONTS); // Native fonts are nice!
				pg.g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
				pg.g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
//				p.smooth();
			}	
		}
	}
	
	static public void main(String args[]) {   PApplet.main(new String[] { "PhyloWidget" });}
}