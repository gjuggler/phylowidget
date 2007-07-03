package org.andrewberman.phyloinfo;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.camera.PRectMover;
import org.andrewberman.camera.PSettableRect;
import org.andrewberman.phyloinfo.render.Cladogram;
import org.andrewberman.phyloinfo.render.TreeRenderer;
import org.andrewberman.phyloinfo.tree.RandomTreeMutator;
import org.andrewberman.phyloinfo.tree.Tree;
import org.andrewberman.phyloinfo.tree.TreeNode;

import processing.core.PApplet;

public class TreeManager implements PSettableRect
{
	protected PApplet p = PhyloWidget.p;
	public static TreeManager instance;
	
	protected static UIManager ui;
	protected static PRectMover camera;
	protected static Rectangle2D.Float cameraRect;
	protected ArrayList trees;
	protected ArrayList renderers;

	public TreeManager()
	{
		instance = this;
		
		ui = new UIManager();
		camera = new PRectMover(PhyloWidget.p,this);
		camera.fillScreen();
		cameraRect = new Rectangle2D.Float(0,0,0,0);
		trees = new ArrayList();
		renderers = new ArrayList();
	}
	
	public void update()
	{
		camera.update();
//		p.stroke(0);
//		p.noFill();
//		p.rect(cameraRect.x, cameraRect.y, cameraRect.width,cameraRect.height);
		
		for (int i=0; i < renderers.size(); i++)
		{
			TreeRenderer r = (TreeRenderer)renderers.get(i);
			r.setRect(cameraRect.x+cameraRect.width/2,cameraRect.y+cameraRect.height/2,cameraRect.width,cameraRect.height);
			r.render();
		}
		ui.draw();
	}
	
	public void nodesInRange(ArrayList list, Rectangle2D.Float rect)
	{
		for (int i=0; i < renderers.size(); i++)
		{
			TreeRenderer r = (TreeRenderer)renderers.get(i);
			r.nodesInRange(list, rect);
		}
	}
	
	public void createTree(String s)
	{
		Tree t = new Tree(s);
		trees.add(t);
		
		Cladogram c = new Cladogram();
		c.setTree(t);
		renderers.add(c);
		
		RandomTreeMutator mutator = new RandomTreeMutator(t);
		for (int i=0; i < 1000; i++)
		{
			mutator.randomlyMutateTree();
		}
		
//		mutator.delay = 50;
//		mutator.start();
	}
	
	/**
	 * Method to respond to our rectangle camera mover thingy.
	 */
	public void setRect(float cx, float cy, float w, float h)
	{
		if (cameraRect != null)
			cameraRect.setFrameFromCenter(cx, cy, cx - w/2, cy - h/2);
	}
	
	public static Rectangle2D.Float getVisibleRect()
	{
		return cameraRect;
	}
}
