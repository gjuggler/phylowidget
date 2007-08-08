package org.phylowidget;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.camera.RectMover;
import org.andrewberman.camera.SettableRect;
import org.phylowidget.render.Cladogram;
import org.phylowidget.render.DiagonalCladogram;
import org.phylowidget.render.TreeRenderer;
import org.phylowidget.tree.RandomTreeMutator;
import org.phylowidget.tree.RenderNodeFactory;
import org.phylowidget.tree.Tree;

import processing.core.PApplet;

public class TreeManager implements SettableRect
{
	protected PApplet p;
	
	public static RectMover camera;
	protected static Rectangle2D.Float cameraRect;
	protected ArrayList trees;
	protected ArrayList renderers;
	
//	public Navigator nav;
	public RandomTreeMutator mutator;
	
	public TreeManager(PApplet p)
	{
		this.p = p;
	}
	
	public void setup()
	{
		trees = new ArrayList();
		renderers = new ArrayList();
		cameraRect = new Rectangle2D.Float(0,0,0,0);
		camera = new RectMover(p,this);
		camera.fillScreen();
		
//		nav = new Navigator(p);
	}
	
	public void update()
	{
		camera.update();
		
		for (int i=0; i < renderers.size(); i++)
		{
			TreeRenderer r = (TreeRenderer)renderers.get(i);
			r.render(p.g, cameraRect.x,cameraRect.y,cameraRect.width,cameraRect.height);
		}
	}
	
	public void nodesInRange(ArrayList list, Rectangle2D.Float rect)
	{
		for (int i=0; i < renderers.size(); i++)
		{
			TreeRenderer r = (TreeRenderer)renderers.get(i);
			r.nodesInRange(list, rect);
		}
	}
	
	public void nodesInPoint(ArrayList list, Point2D.Float pt)
	{
		Rectangle2D.Float rect = new Rectangle2D.Float();
		rect.setFrame(pt.x,pt.y,0,0);
		nodesInRange(list,rect);
	}
	
//	public Point getPosition(NodeRange r)
//	{
//		TreeRenderer render = r.render;
//		return render.getPosition(r.node);
//	}
	
	public void clearTrees()
	{
		mutator.stop();
		renderers.clear();
		trees.clear();
	}
	
	public void mutateTree()
	{
		mutator.randomlyMutateTree();
	}
	
	public void startMutatingTree(int delay)
	{
		mutator.stop();
		mutator = new RandomTreeMutator((Tree) trees.get(0));
		mutator.delay = delay;
		mutator.start();
	}
	
	public void stopMutatingTree()
	{
		mutator.stop();
	}
	
	public void createTree(String s)
	{	
		Tree t = new Tree(RenderNodeFactory.instance(),s);
		trees.add(t);
		
		TreeRenderer c = new DiagonalCladogram(p);
		c.setTree(t);
		renderers.add(c);
		
//		nav.setRenderer(c);
		
		mutator = new RandomTreeMutator(t);
	}
	
	/**
	 * Method to respond to our rectangle camera mover thingy.
	 */
	public void setRect(float x, float y, float w, float h)
	{
		if (cameraRect != null)
			cameraRect.setFrame(x,y,w,h);
	}
	
	public static Rectangle2D.Float getVisibleRect()
	{
		return cameraRect;
	}
}
