package org.andrewberman.phyloinfo;

import java.awt.RenderingHints;

import org.andrewberman.ui.PFontLoader;
import org.andrewberman.ui.ProcessingUtils;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphicsJava2D;

public class PhyloWidget extends PApplet
{
	private static final long serialVersionUID = -7096870051293017660L;
	
	public static PhyloWidget p;

	public PhyloCamera camera;
	public TreeManager manager;
	
	public static int WIDTH = 400;
	public static int HEIGHT = 400;

	private PFont debugFont;
	
	public PhyloWidget()
	{
		super();
		p = this;
	}

	public void setup()
	{
		registerSize(this);
		size(WIDTH, HEIGHT);
		frameRate(30f);
		
		camera = new PhyloCamera();
		manager = new TreeManager();
		manager.createTree("PhyloWidget");
		
		debugFont = PFontLoader.f32;
	}

	public void draw()
	{
		background(255);
		drawFrameRate();
		
		camera.update();
		ProcessingUtils.setMatrix(this);
		
//		manager.setRect(0, 0, mouseX, mouseY);
		manager.update();
	}

	public void drawFrameRate()
	{
//		textMode(PApplet.SCREEN);
		textAlign(PApplet.LEFT);
		textFont(debugFont);
		fill(0);
		text(String.valueOf(round(frameRate*10)/10.0), 10, 25);
//		textMode(PApplet.MODEL);	
	}
	
	public void size(int w, int h)
	{
		synchronized(this)
		{
			size(w,h,JAVA2D);
			if (g.getClass() == PGraphicsJava2D.class)
			{
				PGraphicsJava2D pg = (PGraphicsJava2D) p.g;
				
				// I like the native fonts with Java2D.
				p.hint(PConstants.ENABLE_NATIVE_FONTS);
				pg.g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
				
//				p.smooth();
			}	
		}
	}
}