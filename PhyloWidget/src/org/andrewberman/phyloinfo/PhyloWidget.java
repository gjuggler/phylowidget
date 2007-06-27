package org.andrewberman.phyloinfo;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.andrewberman.phyloinfo.render.Cladogram;
import org.andrewberman.phyloinfo.render.TreeRenderer;
import org.andrewberman.phyloinfo.tree.RandomTreeMutator;
import org.andrewberman.phyloinfo.tree.Tree;
import org.andrewberman.phyloinfo.tree.TreeNode;
import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.PFontLoader;
import org.andrewberman.ui.PRadialMenu;
import org.andrewberman.ui.ProcessingUtils;

import processing.core.PApplet;
import processing.core.PFont;

public class PhyloWidget extends PApplet
{
	private static final long serialVersionUID = -7096870051293017660L;

	private Graphics2D g2;
	public static PApplet p;

	public PhyloCamera camera;
	public TreeRenderer render;
	public PRadialMenu menu;
	public RandomTreeMutator mutator;
	public static int WIDTH = 500;
	public static int HEIGHT = 400;

	private PFont debugFont;
	
	public PhyloWidget()
	{
		super();
		p = this;
	}

	public void setup()
	{
		size(WIDTH, HEIGHT, P3D);
		
		background(0, 0, 0);
		frameRate(30f);
		
		camera = new PhyloCamera(this);
		render = new Cladogram(this,new Tree(new TreeNode("PhyloWidget")));
//		menu = new PRadialMenu(this);
//		menu.addMenuItem("Hello, world!", 'h', "");
		
		debugFont = PFontLoader.f16;
		
		menu = new PRadialMenu(this, 200, 200, 50);
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
		
		phyloInit();
	}

	public void draw()
	{
		background(255);
		textMode(PApplet.SCREEN);
		textAlign(PApplet.LEFT);
		textFont(debugFont,16);
		text(String.valueOf(round(frameRate*10)/10.0), 10, 15);
		if (mutator != null)
			text(mutator.mutations,10,35);
		textMode(PApplet.MODEL);
		
		camera.update();
		
		render.render();
		menu.draw();
		ProcessingUtils.setMatrix(this);
		
//		p.stroke(255,0,0);
//		p.strokeWeight(2.0f);
		Point2D.Float pt = render.getNearestPoint();
//		p.ellipse(pt.x, pt.y, 10, 10);
		if (!FocusManager.isModal())
		{
			menu.setPosition(pt.x, pt.y);
		}
		
		
		Rectangle2D.Float rect = render.getRect();
		noFill();
		stroke(255,0,0);
		p.rect(rect.x, rect.y, rect.width, rect.height);
	}

	public void phyloInit()
	{
		Tree tree = new Tree(new TreeNode("PhyloWidget"));
		render.setTree(tree);
//		camera.zoomCenterTo(render.getRect());
		if (mutator != null)
			mutator.stop();
//		mutator = new RandomTreeMutator(this, render.getTree());
//		mutator.delay = 2000;
	}

	
	public void doSomething(String s)
	{
		// Do nothing.
	}
	
	// Input handling starts here.
	
	public void mouseClicked()
	{
//		phyloInit();
	}
	
}