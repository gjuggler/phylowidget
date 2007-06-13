package org.andrewberman.phyloinfo;

import java.awt.Graphics2D;

import org.andrewberman.phyloinfo.render.Cladogram;
import org.andrewberman.phyloinfo.render.DiagonalCladogram;
import org.andrewberman.phyloinfo.tree.RandomTreeMutator;
import org.andrewberman.phyloinfo.tree.Tree;
import org.andrewberman.phyloinfo.tree.TreeNode;
import org.andrewberman.util.Position;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

public class PhyloWidget extends PApplet
{

	private static final long serialVersionUID = -7096870051293017660L;

	private Graphics2D g2;

	public PWCamera camera;
	public Cladogram render;
	public RandomTreeMutator mutator;
	public static int WIDTH = 500;
	public static int HEIGHT = 400;

	private PFont debugFont;
	
	public PhyloWidget()
	{
		super();
	}

	public void setup()
	{
		size(WIDTH, HEIGHT);
		registerPre(this);
		
		background(0, 0, 0);
		frameRate(60f);
		
		camera = new PWCamera(this);
		render = new Cladogram(this);
		
		debugFont = loadFont("TimesNewRoman-16.vlw");
		
		phyloInit();
	}

	public void pre()
	{
		background(0);

		fill(255);
		
		// Output debug info to the upper-right corner.
		textMode(PApplet.SCREEN);
		textAlign(PApplet.LEFT);
		textFont(debugFont);
		text(str(round(frameRate*10)/10.0), 10, 15);
		textMode(PApplet.MODEL);
		// Camera stuff.
//		Position p = new Position(mouseX, mouseY);
//		camera.screenToStage(p);
//		text(p.y, 10, 25);
	}
	
	public void draw()
	{
		camera.updateStage();
	}

	public void phyloInit()
	{
		Tree tree = new Tree(new TreeNode("PhyloWidget"));
		render.setTree(tree);
		camera.zoomCenterTo(render.getRect());
		if (mutator != null)
			mutator.stop();
		mutator = new RandomTreeMutator(this, tree);
	}

	
	// Input handling starts here.
	
	public void mouseClicked()
	{
		phyloInit();
	}
	
}