package org.andrewberman.evogame;

import java.awt.geom.Rectangle2D;

import org.andrewberman.ui.UIGlobals;
import org.andrewberman.ui.UIUtils;
import org.phylowidget.PhyloWidget;
import org.phylowidget.TreeManager;
import org.phylowidget.net.PWClipUpdater;
import org.phylowidget.net.PWTreeUpdater;
import org.phylowidget.ui.PhyloConfig;
import org.phylowidget.ui.PhyloUI;

import processing.core.PGraphicsJava2D;

public class EvoGameApplet extends PhyloWidget
{
	public static EvoGameApplet p;

	public EvoGameApplet()
	{
		p = this;
	}
	
	@Override
	public void setup()
	{
//		super.setup();
		
		frameRate(60);

		PGraphicsJava2D pg = (PGraphicsJava2D) g;

		new UIGlobals(this);
		cfg = new PhyloConfig();
		trees = new EvoTreeManager(this);
		ui = new PhyloUI(this);

		treeUpdater = new PWTreeUpdater();
		clipUpdater = new PWClipUpdater();

		ui.setup();
		trees.setup();
		clearQueues();
		
		trees.camera.nudgeTo(200, 0);
		trees.camera.zoomTo(0.7f);
		
		DragDropImage ddi = new DragDropImage(this);
		ddi.setPosition(300,100);
	}
	
	@Override
	public void resize(int width, int height)
	{
		super.resize(width, height);
		PGraphicsJava2D pg = (PGraphicsJava2D) g;
		if (pg == null)
			return;
		UIUtils.setRenderingHints(pg);
	}
	
	@Override
	public synchronized void draw()
	{
		super.draw();
		
//		noFill();
//		stroke(0);
//		strokeWeight(2);
//		Rectangle2D.Float rect = trees.cameraRect;
//		rect(rect.x,rect.y,rect.width,rect.height);
	}
	
}
