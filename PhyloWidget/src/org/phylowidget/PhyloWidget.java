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

import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.andrewberman.ui.UIGlobals;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.unsorted.MethodAndFieldSetter;
import org.andrewberman.ui.unsorted.StringPair;
import org.phylowidget.net.PWClipUpdater;
import org.phylowidget.net.PWTreeUpdater;
import org.phylowidget.render.DoubleBuffer;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.ui.PhyloConfig;
import org.phylowidget.ui.PhyloUI;

import processing.core.PApplet;
import processing.core.PGraphicsJava2D;

public class PhyloWidget extends PApplet
{
	private static final long serialVersionUID = -7096870051293017660L;

	public static TreeManager trees;
	public static PhyloConfig cfg = new PhyloConfig();
	public static PhyloUI ui;

	public static PhyloWidget p;

	public static float FRAMERATE = 60;

	public static PWTreeUpdater treeUpdater;
	public static PWClipUpdater clipUpdater;

	private static String messageString = new String();

	long time = 0;

	public PhyloWidget()
	{
		super();
		time = System.currentTimeMillis();
		PhyloWidget.p = this;
	}

	public void setup()
	{
		if (frame != null)
		{
			/*
			 * We're in standalone / application mode. FREEDOM!!!
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
			size(getWidth(), getHeight(), JAVA2D);
		}
		frameRate(FRAMERATE);

		new UIGlobals(this);
		cfg = new PhyloConfig();
		ui = new PhyloUI(this);
		trees = new TreeManager(this);

		treeUpdater = new PWTreeUpdater();
		clipUpdater = new PWClipUpdater();
		
		new Thread()
		{
			public void run()
			{
				ui.setup();
				trees.setup();
			}
		}.start();

		unregisterDraw(UIGlobals.g.event());
		
		clearQueues();
	}

	DoubleBuffer dbr;

	@Override
	public void resize(int width, int height)
	{
		super.resize(width, height);
		//		PGraphicsJava2D pg = (PGraphicsJava2D) g;
		//		if (pg == null)
		//			return;
		UIUtils.setRenderingHints(g);

	}

	boolean drawnOnce = false;

	public synchronized void draw()
	{
		background(PhyloWidget.cfg.getBackgroundColor().getRGB(), 1.0f);

		// If we have setting changes or method calls on the queue, run them now.
		if (drawnOnce)
			clearQueues();

		drawnOnce = true;

		UIGlobals.g.event().draw();

		if (!cfg.suppressMessages)
		{
			if (frameCount - messageFrame > (frameRateTarget * messageDecay))
				messageString = "";
			if (messageString.length() != 0)
			{
				drawMessage();
			}

			if (cfg.debug)
			{
				drawNumLeaves();
				drawFrameRate();
			}
		}
	}

	Pattern parens = Pattern.compile("(.*?)\\((.*)\\)");

	ArrayList<StringPair> settingsAndMethods = new ArrayList<StringPair>();
	static final String METHOD_FLAG = "!!method!!";

	protected void clearQueues()
	{
		while (!settingsAndMethods.isEmpty())
		{
			StringPair sp = settingsAndMethods.remove(0); // Remove first.
			System.out.println(sp);
			if (sp.a == METHOD_FLAG)
			{
				try
				{
					String s = sp.b;
					Method m = null;
					Object[] args = new Object[] {};
					Matcher match = parens.matcher(s);
					boolean matched = false;
					while (match.find())
					{
						matched = true;
						s = match.group(1);
						args = new Object[] { match.group(2) };
						System.out.println(s + "  " + args);
						m = PhyloUI.class.getMethod(s, String.class);
						m.invoke(ui, args);
					}
					if (!matched)
					{
						m = PhyloUI.class.getMethod(s);
						m.invoke(ui);
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			} else
			{
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(sp.a, sp.b);
				MethodAndFieldSetter.setMethodsAndFields(PhyloWidget.cfg, map);
			}
		}
	}

	public void stop()
	{
		super.stop();
	}

	public synchronized void destroy()
	{
		//		System.out.println("Destroying.");
		noLoop();
		super.destroy();
		if (trees != null)
			trees.destroy();
		trees = null;
		cfg.destroy();
		cfg = null;
		ui.destroy();
		ui = null;
		treeUpdater = null;
		clipUpdater = null;
		/*
		 * Call these guys last, because other classes (such as the UI classes) may depend on the globals still being here
		 * in order to destroy themselves.
		 */
		UIGlobals.g.destroyGlobals();
	}

	protected void drawFrameRate()
	{
		textAlign(PApplet.LEFT);
		textFont(UIGlobals.g.getPFont());
		textSize(10);
		fill(255, 0, 0);
		text(String.valueOf(round(frameRate * 10) / 10.0), width - 40, height - 10);
		// Uncomment to print out the number of leaves.
		// RootedTree t = trees.getTree();
		// int numLeaves = t.getNumEnclosedLeaves(t.getRoot());
		// text(numLeaves,5,height-10);
	}

	protected void drawNumLeaves()
	{
		RootedTree tree = trees.getTree();
		if (tree == null)
			return;
		int leaves = tree.getNumEnclosedLeaves(tree.getRoot());
		String nleaves = String.valueOf(leaves);
		textAlign(PApplet.LEFT);
		textFont(UIGlobals.g.getPFont());
		textSize(10);
		fill(255, 0, 0);
		text(nleaves, width - 100, height - 10);
	}

	protected void drawMessage()
	{
		textAlign(PApplet.LEFT);
		textFont(UIGlobals.g.getPFont());
		textSize(10);
		float w = textWidth(messageString);
		fill(255, 255, 255);
		stroke(255, 255, 255);
		strokeWeight(3);
		rect(5, height - 20, w, 12);
		fill(255, 0, 0);
		text(messageString, 5, height - 10);
	}

	static int messageFrame;
	static float messageDecay = 15;

	public static void setMessage(String s)
	{
		messageString = s;
		if (p != null)
			messageFrame = p.frameCount;
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

	public void close()
	{
		if (frame != null) // We're in standalone mode
		{
			frame.setVisible(false);
			destroy();
		}
	}

	public boolean updateTree(String s)
	{
		PhyloWidget.p.treeUpdater.triggerUpdate(s);
		return true;
	}

	public boolean updateClip(String s)
	{
		PhyloWidget.p.clipUpdater.triggerUpdate(s);
		return true;
	}
	
	public synchronized void changeSetting(String setting, String newValue)
	{
		if (cfg.debug)
		{
			System.out.println(setting + "\t" + newValue);
		}
		synchronized (settingsAndMethods)
		{
			settingsAndMethods.add(new StringPair(setting, newValue));
		}
	}

	public synchronized void callMethod(String method)
	{
		settingsAndMethods.add(new StringPair(METHOD_FLAG, method));
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
		return "PhyloWidget";
	}

	static public void main(String args[])
	{
		PApplet.main(new String[] { "org.phylowidget.PhyloWidget" });
	}
}