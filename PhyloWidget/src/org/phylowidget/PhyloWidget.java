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

import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.andrewberman.ui.Color;
import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.FontLoader;
import org.andrewberman.ui.UIGlobals;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.unsorted.MethodAndFieldSetter;
import org.phylowidget.net.PWClipUpdater;
import org.phylowidget.net.PWTreeUpdater;
import org.phylowidget.render.DoubleBuffer;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;
import org.phylowidget.tree.TreeManager;
import org.phylowidget.ui.PhyloConfig;
import org.phylowidget.ui.PhyloTree;
import org.phylowidget.ui.PhyloUI;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;

public class PhyloWidget extends PApplet
{
	private static final long serialVersionUID = -7096870051293017660L;

	public static TreeManager trees;
	public static PhyloConfig cfg;
	public static PhyloUI ui;

	public static float FRAMERATE = 60;
	public static float TWEEN_FACTOR = 30f / FRAMERATE;

	public static boolean isOutputting;

	private static PWTreeUpdater treeUpdater;
	private static PWClipUpdater clipUpdater;

	private static String messageString = new String();

	boolean DEBUG =  true;

	public PhyloWidget()
	{
		super();
	}

	public void setup()
	{
		if (frame != null)
		{
			/*
			 * We're in standalone / application mode. Freedom!!
			 */
			frame.setResizable(true);
			frame.setTitle("PhyloWidget Standalone");
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					size(500, 500);
				}
			});
		} else
		{
			/*
			 * We're locked into an applet. Don't fight it.
			 */
			size(getWidth(), getHeight());
		}
		frameRate(FRAMERATE);
		
		PGraphicsJava2D pg = (PGraphicsJava2D) g;

		new UIGlobals(this);
		trees = new TreeManager(this);
		cfg = new PhyloConfig();
		ui = new PhyloUI(this);

		treeUpdater = new PWTreeUpdater();
		clipUpdater = new PWClipUpdater();

		ui.setup();
		trees.setup();
		
	}
DoubleBuffer dbr;

	@Override
	public void resize(int width, int height)
	{
		super.resize(width, height);
		PGraphicsJava2D pg = (PGraphicsJava2D) g;
		if (pg == null)
			return;
		UIUtils.setRenderingHints(pg);
		
	}
	
	public synchronized void draw()
	{
		background(PhyloWidget.cfg.getBackgroundColor().getRGB(), 1.0f);

		if (frameCount - messageFrame > (frameRateTarget * messageDecay))
			messageString = "";
		if (messageString.length() != 0)
			drawMessage();

		if (DEBUG)
		{
			drawNumLeaves();
			drawFrameRate();
		}
	}

	public void stop()
	{
		super.stop();
	}

	public void destroy()
	{
		super.destroy();
		UIGlobals.g.destroyGlobals();
		trees = null;
		cfg = null;
		ui = null;
		treeUpdater = null;
		clipUpdater = null;
	}
	
	void drawFrameRate()
	{
		textAlign(PApplet.LEFT);
		textFont(UIGlobals.g.getFont());
		textSize(10);
		fill(255, 0, 0);
		text(String.valueOf(round(frameRate * 10) / 10.0), width - 40,
				height - 10);
		// Uncomment to print out the number of leaves.
		// RootedTree t = trees.getTree();
		// int numLeaves = t.getNumEnclosedLeaves(t.getRoot());
		// text(numLeaves,5,height-10);
	}

	void drawNumLeaves()
	{
		int leaves = trees.getRenderer().getTree().getNumEnclosedLeaves(
				trees.getRenderer().getTree().getRoot());
		String nleaves = String.valueOf(leaves);
		textAlign(PApplet.LEFT);
		textFont(UIGlobals.g.getFont());
		textSize(10);
		fill(255, 0, 0);
		text(nleaves, width - 100, height - 10);
	}

	void drawMessage()
	{
		textAlign(PApplet.LEFT);
		textFont(UIGlobals.g.getFont());
		textSize(10);
		fill(255, 0, 0);
		text(messageString, 5, height - 10);
	}

	static int messageFrame;
	static float messageDecay = 15;

	public static void setMessage(String s)
	{
		messageString = s;
		messageFrame = UIGlobals.g.getP().frameCount;
	}

	//	public void size(int w, int h)
	//	{
	//		super.size(w,h);
	////		if (width != w || h != h)
	////			size(w, h, JAVA2D);
	//		// size(w,h,P3D);
	//		// size(w,h,OPENGL);
	//			// pg.g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
	//			// RenderingHints.VALUE_STROKE_PURE);
	//			// p.smooth();
	//	}

	//	@Override
	//	public void resize(int w, int h)
	//	{
	////		super.resize(width, height);
	//		if (g != null && (getWidth()!=w || getHeight()!=h))
	//		{
	//			size(w,h);
	//		}
	////		setup();
	//		System.out.println("resize!"+width);
	////		size(width,height);
	//	}

	public boolean jsTest()
	{
		return true;
	}

	public boolean updateTree(String s)
	{
		treeUpdater.triggerUpdate(s);
		return true;
	}

	public boolean updateClip(String s)
	{
		clipUpdater.triggerUpdate(s);
		return true;
	}
	
	public void changeSetting(String setting, String newValue)
	{
		HashMap m = new HashMap();
		m.put(setting, newValue);
		MethodAndFieldSetter.setMethodsAndFields(PhyloWidget.cfg, m);
	}

	@Override
	public void keyPressed()
	{
		super.keyPressed();
		if (key == KeyEvent.VK_ESCAPE)
			key = 0;
	}

	@Override
	public String getAppletInfo()
	{
		return "PhyloWidget!";
	}

	
	static public void main(String args[])
	{
		PApplet.main(new String[] { "org.phylowidget.PhyloWidget" });
	}
}