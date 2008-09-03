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
package org.phylowidget;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.ui.AbstractUIObject;
import org.andrewberman.ui.UIGlobals;
import org.andrewberman.ui.UIRectangle;
import org.andrewberman.ui.camera.RectMover;
import org.phylowidget.render.BasicTreeRenderer;
import org.phylowidget.render.LayoutCircular;
import org.phylowidget.render.LayoutCladogram;
import org.phylowidget.render.LayoutDiagonal;
import org.phylowidget.render.LayoutUnrooted;
import org.phylowidget.render.TreeRenderer;
import org.phylowidget.render.images.ImageLoader;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;

import processing.core.PApplet;

public class TreeManager extends AbstractUIObject
{
	protected PApplet p;

	public static RectMover camera;
	public static UIRectangle cameraRect;
	//	protected ArrayList trees;
	//	protected ArrayList renderers;

	public static ImageLoader imageLoader;
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
		UIGlobals.g.event().add(this);
	}

	public void setup()
	{
		imageLoader = new ImageLoader();

		cameraRect = new UIRectangle(0, 0, 0, 0);
		camera = new RectMover(p);
		fillScreen();
		camera.nudgeTo(-PhyloWidget.cfg.viewportX, -PhyloWidget.cfg.viewportY);
		camera.zoomTo(PhyloWidget.cfg.viewportZoom);
		camera.fforward();
		/*
		 * We need to let the ToolManager know our current Camera object.
		 */
		UIGlobals.g.event().setCamera(camera);

		setTree(TreeIO.parseNewickString(new PhyloTree(), PhyloWidget.cfg.tree));

		setRenderer(new BasicTreeRenderer());
		PhyloWidget.cfg.setLayout(PhyloWidget.cfg.layout);
		try
		{
			PhyloTree pt = (PhyloTree) getTree();
			pt.updateNewick();
		} catch (Exception e)
		{
			// Do nothing.
		}
	}

	public void draw()
	{
		update();
	}

	protected void updateCameraRect()
	{
		cameraRect.setRect(camera.getRect());
		cameraRect.translate(p.width / 2, p.height / 2);
	}

	public void update()
	{
		if (camera != null && r != null)
		{
			camera.update();
			updateCameraRect();
			r.render(p.g, cameraRect.x, cameraRect.y, cameraRect.width, cameraRect.height, true);
			PhyloWidget.cfg.viewportX = -camera.getX();
			PhyloWidget.cfg.viewportY = -camera.getY();
			PhyloWidget.cfg.viewportZoom = camera.getZ();
		}
		if (mutateMe)
		{
			mutator.randomlyMutateTree();
			mutateMe = false;
		}

		// Synchronize with the PhyloConfig values.
		

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

	public synchronized RootedTree getTree()
	{
		return t;
	}

	public TreeRenderer getRenderer()
	{
		return r;
	}

	public synchronized void setTree(String s)
	{
		setTree(TreeIO.parseNewickString(new PhyloTree(), s));
	}

	public void setTree(final RootedTree tree)
	{
		if (t != null)
		{
			/*
			 * Whenever doing something to the tree (such as DISPOSING it!) we need to lock
			 * on it, because the renderer (which is on a different thread) could be using it at the moment.
			 */
			synchronized (t)
			{
				t.dispose();
				t = null;
			}
		}
		this.t = tree;
		if (getRenderer() != null)
		{
			getRenderer().setTree(tree);
		}
		if (tree instanceof PhyloTree)
		{
			PhyloTree pt = (PhyloTree) tree;
			pt.setSynchronizedWithJS(true);
		}
		fforwardMe = true;
		mutator = new RandomTreeMutator(tree);
	}

	public synchronized void diagonalRender()
	{
		//		setRenderer(new DiagonalCladogram());
		getRenderer().setLayout(new LayoutDiagonal());
	}

	public synchronized void rectangleRender()
	{
		//		setRenderer(new BasicTreeRenderer());
		getRenderer().setLayout(new LayoutCladogram());
	}

	public synchronized void circleRender()
	{
		//		setRenderer(new Circlegram());
		getRenderer().setLayout(new LayoutCircular());
	}

	public synchronized void unrootedRender()
	{
		getRenderer().setLayout(new LayoutUnrooted());
	}

	synchronized void setRenderer(BasicTreeRenderer r)
	{
		if (getRenderer() != null)
		{
			synchronized (this.r)
			{
				getRenderer().dispose();
			}
		}
		this.r = r;
		if (getTree() != null)
			r.setTree(getTree());
		PhyloWidget.ui.search();
	}

	public void triggerMutation()
	{
		mutateMe = true;
	}

	//	public UIRectangle getVisibleRect()
	//	{
	//		UIRectangle fl = getRenderer().getVisibleRect();
	//		fl.translate(-p.width / 2, -p.height / 2);
	//		return fl;
	//	}

	public void destroy()
	{
		if (r != null)
			r.dispose();
		r = null;
		if (t != null)
			t.dispose();
		t = null;
		p = null;
		camera = null;
		cameraRect = null;
		if (imageLoader != null)
			imageLoader.dispose();
		imageLoader = null;
	}

	public void fillScreen()
	{
		camera.fillScreen(0.7f);
	}
}
