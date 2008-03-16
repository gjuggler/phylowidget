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
import org.andrewberman.ui.UIUtils;
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
	//	protected ArrayList trees;
	//	protected ArrayList renderers;

	TreeRenderer r;
	RootedTree t;

	// public Navigator nav;
	private RandomTreeMutator mutator;
	private boolean mutateMe;
	private Runnable runMe;

	private boolean fforwardMe;

	public TreeManager(PApplet p)
	{
		this.p = p;
		UIUtils.loadUISinglets(p);
	}

	public void setup()
	{
		cameraRect = new UIRectangle(0, 0, 0, 0);
		camera = new RectMover(p);
		camera.fillScreen(.8f);
		camera.fforward();
		/*
		 * We need to let the ToolManager know our current Camera object.
		 */
		EventManager.instance.setCamera(camera);

		setTree(TreeIO.parseNewickString(new PhyloTree(), PhyloWidget.cfg.tree));
		rectangleRender();
		
	}

	public void update()
	{
		camera.update();
		cameraRect.setRect(camera.getRect());
		cameraRect.translate(p.width / 2, p.height / 2);
		r.render(p.g, cameraRect.x, cameraRect.y, cameraRect.width,
				cameraRect.height, true);

		if (mutateMe)
		{
			mutator.randomlyMutateTree();
			mutateMe = false;
		}

//		if (runMe != null)
//		{
//			Runnable r = runMe;
//			runMe = null;
//			r.run();
//		}
	}

	public void nodesInRange(ArrayList list, Rectangle2D.Float rect)
	{
			r.nodesInRange(list, rect);
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
		mutator = new RandomTreeMutator(t);
		mutator.setDelay(delay);
		mutator.start();
	}

	public void stopMutatingTree()
	{
		mutator.stop();
	}

	public RootedTree getTree()
	{
		return t;
	}

	public TreeRenderer getRenderer()
	{
		return r;
	}

	public void setTree(String s)
	{
		setTree(TreeIO.parseNewickString(new PhyloTree(), s));
	}
	
	public void setTree(final RootedTree tree)
	{
		this.t = tree;
		if (getRenderer() != null)
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
		this.r = r;
		if (getTree() != null)
			r.setTree(getTree());
		PhyloWidget.ui.search();
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
