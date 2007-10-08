package org.phylowidget;

import java.awt.RenderingHints;
import java.io.IOException;
import java.util.Properties;

import org.andrewberman.ui.FontLoader;
import org.phylowidget.net.PWClipUpdater;
import org.phylowidget.net.PWTreeUpdater;
import org.phylowidget.tree.TreeIO;
import org.phylowidget.tree.TreeManager;
import org.phylowidget.ui.PhyloTree;
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
	public static Properties props;
	
	public static int WIDTH = 400;
	public static int HEIGHT = 400;
	
	public static float FRAMERATE = 40;
	public static float TWEEN_FACTOR = 30f / FRAMERATE; 

	public static boolean usingNativeFonts;
	public static boolean openGL;
	
	public boolean stopCreatedThreads = false;
	
	private PWTreeUpdater updater;
	private PWClipUpdater clipUpdater;
	
	public PhyloWidget()
	{
		super();
		p = this;
	}

	public void setup()
	{
		this.size(500,500);
		frameRate(FRAMERATE);
		
		p = this;
		
		// Load the properties file.
		props = new Properties();
		try
		{
			props.load(this.openStream("phylowidget.properties"));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		// Creates, manages, and renders trees.
		trees = new TreeManager(this);
		// Creates and manages UI elements.
		ui = new UIManager(this);
		updater = new PWTreeUpdater();
		clipUpdater = new PWClipUpdater();
		
		trees.setup();
		ui.setup();
		trees.setTree(PhyloTree.createDefault());
		
	}

	float theta = 0;
	public void draw()
	{	
		trees.update();
		ui.update();
	}

	public void stop()
	{
		super.stop();
		stopCreatedThreads = true;
	}
	
	public void drawFrameRate()
	{
		textAlign(PApplet.LEFT);
		textFont(FontLoader.instance.vera);
		fill(0);
		text(String.valueOf(round(frameRate*10)/10.0), 5, height-10);	
	}
	
	public void size(int w, int h)
	{
		if (width != w || h != h)
			size(w,h,JAVA2D);
//			size(w,h,P3D);
//			size(w,h,OPENGL);
		if (g.getClass() == PGraphicsJava2D.class)
		{
			PGraphicsJava2D pg = (PGraphicsJava2D) p.g;
			p.hint(PConstants.ENABLE_NATIVE_FONTS); // Native fonts are nice!
			usingNativeFonts = true;
			pg.g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//			pg.g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
			pg.g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//				pg.g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
//				p.smooth();
		} else if (g.getClass().getName().equals("OPENGL"))
		{
			openGL = true;
		}
	}
	
	public void updateTree(String s)
	{
		updater.triggerUpdate(s);
	}
	
	public void updateClip(String s)
	{
		clipUpdater.triggerUpdate(s);
	}
	
	static public void main(String args[]) {   PApplet.main(new String[] { "PhyloWidget" });}
}