package org.phylowidget.tree;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.UIRectangle;
import org.andrewberman.ui.camera.RectMover;
import org.andrewberman.ui.camera.SettableRect;
import org.phylowidget.render.Circlegram;
import org.phylowidget.render.Cladogram;
import org.phylowidget.render.DiagonalCladogram;
import org.phylowidget.render.TreeRenderer;
import org.phylowidget.ui.PhyloNode;
import org.phylowidget.ui.PhyloTree;

import processing.core.PApplet;

public class TreeManager implements SettableRect
{
	protected PApplet p;

	public static RectMover camera;
	protected static UIRectangle cameraRect;
	protected ArrayList trees;
	protected ArrayList renderers;

	// public Navigator nav;
	private RandomTreeMutator mutator;
	private boolean mutateMe;
	private Runnable runMe;

	public TreeManager(PApplet p)
	{
		this.p = p;
	}

	public void setup()
	{
		trees = new ArrayList();
		renderers = new ArrayList();
		cameraRect = new UIRectangle(0, 0, 0, 0);
		camera = new RectMover(p, this);
		camera.fillScreen();
		/*
		 * We need to let the ToolManager know our current Camera object.
		 */
		// EventManager.lazyLoad(p);
		EventManager.instance.setCamera(camera);
		rectangleRender();
	}

	public void update()
	{
		camera.update();

		for (int i = 0; i < renderers.size(); i++)
		{
			TreeRenderer r = (TreeRenderer) renderers.get(i);
			
//			float oldThresh = PhyloWidget.ui.renderThreshold;
//			PhyloWidget.ui.renderThreshold = 50;
//			r.render(p.g, p.width*.75f, p.height*.75f, p.width*.25f, p.height*.25f, false);
//			PhyloWidget.ui.renderThreshold = oldThresh;
//			
			r.render(p.g, cameraRect.x, cameraRect.y, cameraRect.width,
					cameraRect.height, true);
			
			
		}

		if (mutateMe)
		{
			mutator.randomlyMutateTree();
			mutateMe = false;
		}

		if (runMe != null)
		{
			Runnable r = runMe;
			runMe = null;
			r.run();
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
		// mutator.randomlyMutateTree();
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

	public CachedRootedTree getTree()
	{
		if (trees.size() == 0)
			return null;
		return (CachedRootedTree) trees.get(0);
	}

	public TreeRenderer getRenderer()
	{
		return (TreeRenderer) renderers.get(0);
	}

	public void fforward(boolean upX, boolean upY)
	{
		update();
		for (int i = 0; i < trees.size(); i++)
		{
			RootedTree tree = (RootedTree) trees.get(i);
			ArrayList nodes = new ArrayList();
			tree.getAll(tree.getRoot(), null, nodes);
			for (int j = 0; j < nodes.size(); j++)
			{
				PhyloNode n = (PhyloNode) nodes.get(j);
				if (upX)
					n.xTween.fforward();
				if (upY)
					n.yTween.fforward();
			}
		}

	}

	public void setTree(final RootedTree tree)
	{
		runMe = new Runnable()
		{
			public void run()
			{
				trees.clear();
				trees.add(tree);
				getRenderer().setTree(tree);
				if (tree instanceof PhyloTree)
				{
					PhyloTree pt = (PhyloTree) tree;
					pt.setSynchronizedWithJS(true);
				}
				fforward(false, true);
				mutator = new RandomTreeMutator(tree);

			}
		};

	}

	public void diagonalRender()
	{
		setRenderer(new DiagonalCladogram());
	}

	public void rectangleRender()
	{
		setRenderer(new Cladogram());
	}

	public void circleRender()
	{
		setRenderer(new Circlegram());
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
		cameraRect.translate(p.width / 2, p.height / 2);
	}

	public static Rectangle2D.Float getVisibleRect()
	{
		return cameraRect;
	}
}
