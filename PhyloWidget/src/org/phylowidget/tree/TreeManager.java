package org.phylowidget.tree;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.Rectangle;
import org.andrewberman.ui.camera.RectMover;
import org.andrewberman.ui.camera.SettableRect;
import org.phylowidget.render.Cladogram;
import org.phylowidget.render.DiagonalCladogram;
import org.phylowidget.render.Phylogram;
import org.phylowidget.render.TreeRenderer;
import org.phylowidget.ui.PhyloNode;
import org.phylowidget.ui.PhyloTree;

import processing.core.PApplet;

public class TreeManager implements SettableRect
{
	protected PApplet p;

	public static RectMover camera;
	protected static Rectangle cameraRect;
	protected ArrayList trees;
	protected ArrayList renderers;

	// public Navigator nav;
	private RandomTreeMutator mutator;
	private boolean mutateMe;
	
	public TreeManager(PApplet p)
	{
		this.p = p;
	}

	public void setup()
	{
		trees = new ArrayList();
		renderers = new ArrayList();
		cameraRect = new Rectangle(0, 0, 0, 0);
		camera = new RectMover(p, this);
		camera.fillScreen();
		/*
		 * We need to let the ToolManager know our current Camera object.
		 */
//		EventManager.lazyLoad(p);
		EventManager.instance.setCamera(camera);
		cladogramRender();
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
		
		if (mutateMe)
		{
			mutator.randomlyMutateTree();
			mutateMe = false;
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

	public void nodesTouchingPoint(ArrayList list, Point2D.Float pt)
	{
		Rectangle2D.Float rect = new Rectangle2D.Float();
		rect.setFrame(pt.x, pt.y, 0, 0);
		nodesInRange(list, rect);
	}

	public void mutateTree()
	{
//		mutator.randomlyMutateTree();
		mutateMe = true;
	}

	public void startMutatingTree(int delay)
	{
		mutator.stop();
		mutator = new RandomTreeMutator((RootedTree) trees.get(0));
		mutator.delay = delay;
		mutator.start();
	}

	public void stopMutatingTree()
	{
		mutator.stop();
	}

	public RootedTree getTree()
	{
		if (trees.size() == 0)
			return null;
		return (RootedTree) trees.get(0);
	}

	public TreeRenderer getRenderer()
	{
		return (TreeRenderer) renderers.get(0);
	}

	public void fforward(boolean upX, boolean upY)
	{
		update();
		for (int i=0; i < trees.size(); i++)
		{
			RootedTree tree = (RootedTree) trees.get(i);
			ArrayList nodes = new ArrayList();
			tree.getAll(tree.getRoot(), null, nodes);
			for (int j=0; j < nodes.size(); j++)
			{
				PhyloNode n = (PhyloNode) nodes.get(j);
				if (upX)
					n.xTween.fforward();
				if (upY)
					n.yTween.fforward();
			}	
		}
		
	}
	
	public void setTree(RootedTree tree)
	{
		trees.clear();
		trees.add(tree);
		getRenderer().setTree(tree);
		if (tree instanceof PhyloTree)
		{
			PhyloTree pt = (PhyloTree) tree;
			pt.setSynchronizedWithJS(true);
		}
		fforward(false,true);
		mutator = new RandomTreeMutator(tree);
	}

	public void diagonalRender()
	{
		setRenderer(new DiagonalCladogram());
	}

	public void cladogramRender()
	{
		setRenderer(new Cladogram());
	}

	public void phylogramRender()
	{
		setRenderer(new Phylogram());
	}

	void setRenderer(TreeRenderer r)
	{
		renderers.clear();
		if (getTree() != null)
			r.setTree(getTree());
		renderers.add(r);
	}

	/**
	 * Method to respond to our rectangle camera mover thingy.
	 */
	public void setRect(float x, float y, float w, float h)
	{
		if (cameraRect != null)
			cameraRect.setFrame(x, y, w, h);
		/*
		 * The cameraRect is using coordinates where (0,0) is the center of the rectangle to be rendered.
		 * We want this rectangle to be centered in our PApplet, so we translate the cameraRect accordingly.
		 */
		cameraRect.translate(p.width/2, p.height/2);
	}

	public static Rectangle2D.Float getVisibleRect()
	{
		return cameraRect;
	}
}
