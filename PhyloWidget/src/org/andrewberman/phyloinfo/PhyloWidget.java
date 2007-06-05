package org.andrewberman.phyloinfo;

import javax.swing.SwingUtilities;

import org.andrewberman.PCamera.PCamera;
import org.andrewberman.phyloinfo.render.CladoRenderer;
import org.andrewberman.phyloinfo.tree.RandomTreeMutator;
import org.andrewberman.phyloinfo.tree.Tree;
import org.andrewberman.phyloinfo.tree.TreeNode;

import processing.core.PApplet;
import processing.core.PConstants;

public class PhyloWidget extends PApplet {
	
private static final long serialVersionUID = -7096870051293017660L;
public static PhyloWidget instance;
public PCamera camera;
public CladoRenderer render;
public RandomTreeMutator mutator;
public static int WIDTH = 500;
public static int HEIGHT = 400;

public PhyloWidget() {
	super();
	instance = this;
}
	
public void setup() {
	size(WIDTH,HEIGHT);
	background(0,0,0);
	frameRate(60f);
	camera = new PCamera();
	render = new CladoRenderer();
	
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
			test();
		}
	});
}

public void draw() {
	background(0);
	
	// Pre-translation drawing.
	fill(255);
	textSize(12);
	text(str(round(frameRate)),10,15);
	
	// Translate and scale the stage according to our camera position.
	camera.updatePosition();
	
	// Finally, render the tree.
	render.render();

	if (this.mousePressed) {
		if (this.mouseButton == PConstants.LEFT)
		{
		} else if (this.mouseButton == PConstants.RIGHT)
		{
		}
	}
	
}

public void test() {
	Tree tree = new Tree(new TreeNode("PhyloWidget"));
	render.setTree(tree);
	render.render();
	camera.zoomCenterTo(render.getRect());
	mutator = new RandomTreeMutator(tree);
}

}