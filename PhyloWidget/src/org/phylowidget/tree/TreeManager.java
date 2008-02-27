/*******************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * PhyloWidget. If not, see <http://www.gnu.org/licenses/>.
 */
package org.phylowidget.tree;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.UIRectangle;
import org.andrewberman.ui.camera.RectMover;
import org.andrewberman.ui.camera.SettableRect;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.BasicTreeRenderer;
import org.phylowidget.render.Circlegram;
import org.phylowidget.render.DiagonalCladogram;
import org.phylowidget.render.TreeRenderer;
import org.phylowidget.ui.PhyloNode;
import org.phylowidget.ui.PhyloTree;

import processing.core.PApplet;

public class TreeManager
{
	protected PApplet p;

	public static RectMover camera;
	public static UIRectangle cameraRect;
	protected ArrayList trees;
	protected ArrayList renderers;

	// public Navigator nav;
	private RandomTreeMutator mutator;
	private boolean mutateMe;
	private Runnable runMe;

	private boolean fforwardMe;

	public TreeManager(PApplet p)
	{
		this.p = p;
	}

	public void setup()
	{
		trees = new ArrayList();
		renderers = new ArrayList();
		cameraRect = new UIRectangle(0, 0, 0, 0);
		camera = new RectMover(p);
		camera.fillScreen();
		camera.fforward();
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
		cameraRect.setRect(camera.getRect());
		cameraRect.translate(p.width / 2, p.height / 2);

		for (int i = 0; i < renderers.size(); i++)
		{
			TreeRenderer r = (TreeRenderer) renderers.get(i);

			// float oldThresh = PhyloWidget.ui.renderThreshold;
			// PhyloWidget.ui.renderThreshold = 50;
			// r.render(p.g, p.width*.75f, p.height*.75f, p.width*.25f,
			// p.height*.25f, false);
			// PhyloWidget.ui.renderThreshold = oldThresh;
			//			
			r.render(p.g, cameraRect.x, cameraRect.y, cameraRect.width,
					cameraRect.height, true);
		}

		//		if (fforwardMe)
		//		{
		//			fforward(true, true);
		//			fforwardMe = false;
		//		}

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

	// public void nodesTouchingPoint(ArrayList list, Point2D.Float pt)
	// {
	// Rectangle2D.Float rect = new Rectangle2D.Float();
	// rect.setFrame(pt.x, pt.y, 0, 0);
	// nodesInRange(list, rect);
	// }

	public void mutateTree()
	{
		// mutator.randomlyMutateTree();
		mutateMe = true;
	}

	public void startMutatingTree(int delay)
	{
		mutator.stop();
		mutator = new RandomTreeMutator((RootedTree) trees.get(0));
		mutator.setDelay(delay);
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
		synchronized (renderers)
		{
			return (TreeRenderer) renderers.get(0);
		}
	}

	//
	//	public void fforward(boolean upX, boolean upY)
	//	{
	//		// update();
	//		for (int i = 0; i < trees.size(); i++)
	//		{
	//			RootedTree tree = (RootedTree) trees.get(i);
	//			ArrayList nodes = new ArrayList();
	//			tree.getAll(tree.getRoot(), null, nodes);
	//			for (int j = 0; j < nodes.size(); j++)
	//			{
	//				PhyloNode n = (PhyloNode) nodes.get(j);
	//				n.fforward();
	//			}
	//		}
	//
	//	}

	public void setTree(final RootedTree tree)
	{
		trees.clear();
		trees.add(tree);
		getRenderer().setTree(tree);
		if (tree instanceof PhyloTree)
		{
			PhyloTree pt = (PhyloTree) tree;
			pt.setSynchronizedWithJS(true);
		}
		fforwardMe = true;
		mutator = new RandomTreeMutator(tree);
	}

	public void diagonalRender()
	{
		setRenderer(new DiagonalCladogram());
	}

	public void rectangleRender()
	{
		setRenderer(new BasicTreeRenderer());
	}

	public void circleRender()
	{
		setRenderer(new Circlegram());
	}

	void setRenderer(TreeRenderer r)
	{
		synchronized (renderers)
		{
			renderers.clear();
			r.setTree(getTree());
			renderers.add(r);
		}
	}

	public void triggerMutation()
	{
		mutateMe = true;
	}

	public UIRectangle getVisibleRect()
	{
		UIRectangle fl = getRenderer().getVisibleRect();
		fl.translate(-p.width / 2, -p.height / 2);
		return fl;
	}
}
