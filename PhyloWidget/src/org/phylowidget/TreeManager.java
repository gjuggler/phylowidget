package org.phylowidget;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.camera.RectMover;
import org.andrewberman.camera.SettableRect;
import org.phylowidget.render.Cladogram;
import org.phylowidget.render.DiagonalCladogram;
import org.phylowidget.render.Phylogram;
import org.phylowidget.render.TreeRenderer;
import org.phylowidget.tree.RandomTreeMutator;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;
import org.phylowidget.ui.PhyloTree;

import processing.core.PApplet;

public class TreeManager implements SettableRect
{
	protected PApplet p;

	public static RectMover camera;
	protected static Rectangle2D.Float cameraRect;
	protected ArrayList trees;
	protected ArrayList renderers;

	// public Navigator nav;
	public RandomTreeMutator mutator;

	public TreeManager(PApplet p)
	{
		this.p = p;
	}

	public void setup()
	{
		trees = new ArrayList();
		renderers = new ArrayList();
		cameraRect = new Rectangle2D.Float(0, 0, 0, 0);
		camera = new RectMover(p, this);
		camera.fillScreen();



		// Pattern p = Pattern.compile("(duck|buck)");
		// StringBuffer sb = new StringBuffer("My duck is worth a buck.");
		// Matcher m = p.matcher(sb);
		// while (m.find())
		// {
		// System.out.println(m.group());
		// }

		// nav = new Navigator(p);
	}

	public void update()
	{
		camera.update();

		for (int i = 0; i < renderers.size(); i++)
		{
			TreeRenderer r = (TreeRenderer) renderers.get(i);
			r.render(p.g, cameraRect.x, cameraRect.y, cameraRect.width,
					cameraRect.height);
		}
	}

	public void nodesInRange(ArrayList list, Rectangle2D.Float rect)
	{
		for (int i = 0; i < renderers.size(); i++)
		{
			TreeRenderer r = (TreeRenderer) renderers.get(i);
			r.nodesInRange(list, rect);
		}
	}

	public void nodesInPoint(ArrayList list, Point2D.Float pt)
	{
		Rectangle2D.Float rect = new Rectangle2D.Float();
		rect.setFrame(pt.x, pt.y, 0, 0);
		nodesInRange(list, rect);
	}

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
//		mutator = new RandomTreeMutator((Tree) trees.get(0));
		mutator.delay = delay;
		mutator.start();
	}

	public void stopMutatingTree()
	{
		mutator.stop();
	}

	public void createTree(String rootLabel)
	{
//		PhyloTreeGraph ptg = new PhyloTreeGraph("PhyloWidget");
//		trees.add(ptg);
//		 String s = "(,(,,),)";
//		String s = "(((dog:22.90000,(((bear:13.00000,raccoon:13.00000):5.75000,(seal:12.00000,sea_lion:12.00000):6.75000):1.00000,weasel:19.75000):3.15000):22.01667,cat:44.91667):27.22619,monkey:72.14286);";
		String s = "((a,b),c);";
//		String s = "(Alpha,Beta,Gamma,Delta,,Epsilon,,,);";
//		 String s = "(A:3.33,(C:3,B:2):5)";
//		 String s = "(B:6.0,(A:5.0,C:3.0,E:4.0)Ancestor1:5.0,D:11.0);";

//		String s = "(((One:0.2,Two:0.3):0.3,(Three:0.5,Four:0.3):0.2):0.3,Five:0.7):0.0;";
		
		PhyloTree tree = new PhyloTree();
		trees.add(TreeIO.parseNewick(tree, s));
		diagonalRender();
//		mutator = new RandomTreeMutator(t);
	}

	public void diagonalRender()
	{
		setRenderer(new DiagonalCladogram(p));
	}

	public void cladogramRender()
	{
		setRenderer(new Cladogram(p));
	}

	public void phylogramRender()
	{
		setRenderer(new Phylogram(p));
	}
	
	void setRenderer(TreeRenderer r)
	{
		renderers.clear();
		r.setTree((RootedTree) trees.get(0));
		renderers.add(r);
	}

	/**
	 * Method to respond to our rectangle camera mover thingy.
	 */
	public void setRect(float x, float y, float w, float h)
	{
		if (cameraRect != null)
			cameraRect.setFrame(x, y, w, h);
	}

	public static Rectangle2D.Float getVisibleRect()
	{
		return cameraRect;
	}
}
