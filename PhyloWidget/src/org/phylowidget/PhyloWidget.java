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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.unsorted.JSCaller;
import org.andrewberman.ui.unsorted.JavaUtils;
import org.andrewberman.ui.unsorted.MethodAndFieldSetter;
import org.andrewberman.ui.unsorted.StringPair;
import org.phylowidget.render.DoubleBuffer;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.ui.PhyloConfig;
import org.phylowidget.ui.PhyloUI;

import processing.core.PApplet;

public class PhyloWidget extends PWPublicMethods
{
	private static final long serialVersionUID = -7096870051293017660L;

	public PWContext pwc;

	public static float FRAMERATE = 40;

	private static String messageString = new String();

	long time = 0;

	public PhyloWidget()
	{
		super();
		time = System.currentTimeMillis();
	}

	@Override
	public void start()
	{
		pwc = (PWContext) PWPlatform.getInstance().registerApp(this);
		super.start();
		// When running inside a browser, start() will be called when someone
		// returns to a page containing this applet.
		// http://dev.processing.org/bugs/show_bug.cgi?id=581
		finished = false;
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
			final PhyloWidget pw = this;
			Runnable callHome = new Runnable()
			{
				public void run()
				{
					JSCaller call = new JSCaller(pw);
					try
					{
						// Make this Javascript call to satisfy the PulpCore javascript loader that we're using.
						call.call("pulpcore_appletLoaded", null);
					} catch (Exception e)
					{
						// If we're not in a browser, we'll get an exception here.
					}
				}
			};
			pwc.createThread(callHome).start();
		}
		frameRate(FRAMERATE);
		unregisterDraw(pwc.event());

		final PhyloWidget pw = this;
		Runnable setup = new Runnable()
		{
			public void run()
			{
				pwc.ui().setup();
				pwc.trees().setup();
				clearQueues();
			}
		};
		//		setup.run();
		pwc.createThread(setup).start();
	}

	DoubleBuffer dbr;

	@Override
	public void resize(int width, int height)
	{
		super.resize(width, height);
		UIUtils.setRenderingHints(g);
	}

	boolean drawnOnce = false;

	public synchronized void draw()
	{
		background(pwc.config().getBackgroundColor().getRGB(), 1.0f);

		// If we have setting changes or method calls on the queue, run them now.
		if (drawnOnce)
			clearQueues();

		pwc.event().draw();

		if (!pwc.config().suppressMessages)
		{
			if (frameCount - messageFrame > (frameRateTarget * messageDecay))
				messageString = "";
			if (messageString.length() != 0)
			{
				drawMessage();
			}

			if (pwc.config().debug)
			{
				drawNumLeaves();
				drawFrameRate();
			}
		}

		drawnOnce = true;
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
						m.invoke(pwc.ui(), args);
					}
					if (!matched)
					{
						m = PhyloUI.class.getMethod(s);
						m.invoke(pwc.ui());
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			} else
			{
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(sp.a, sp.b);
				MethodAndFieldSetter.setMethodsAndFields(pwc.config(), map);
			}
		}
	}

	public void stop()
	{
		super.stop();
	}

	public synchronized void destroy()
	{
		noLoop();
		super.destroy();
		pwc.destroy();
	}

	protected void drawFrameRate()
	{
		textAlign(PApplet.LEFT);
		textFont(pwc.getPFont());
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
		RootedTree tree = pwc.trees().getTree();
		if (tree == null)
			return;
		int leaves = tree.getNumEnclosedLeaves(tree.getRoot());
		String nleaves = String.valueOf(leaves);
		textAlign(PApplet.LEFT);
		textFont(pwc.getPFont());
		textSize(10);
		fill(255, 0, 0);
		text(nleaves, width - 100, height - 10);
	}

	protected void drawMessage()
	{
		textAlign(PApplet.LEFT);
		textFont(pwc.getPFont());
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

	public void setMessage(String s)
	{
		messageString = s;
		//		if (p != null)
		messageFrame = frameCount;
	}

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

	public void setTree(String s)
	{
		changeSetting("tree", s);
		setMessage("Tree updated.");
	}

	public void setClipboard(String s)
	{
		changeSetting("clipboard", s);
		setMessage("Clipboard updated.");
	}

	public synchronized void changeSetting(String setting, String newValue)
	{
		if (pwc.config().debug)
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

	@Override
	public String getClipboardString()
	{
		return pwc.ui().clipboard.getClipboardText();
	}

	@Override
	public String getUrlParameters()
	{
		Map<String, String> changedFields = PhyloConfig.getConfigSnapshot(pwc.config());
		PhyloTree tree = (PhyloTree) pwc.trees().getTree();
		// Replace the &'s with *'s. TreeIO was modified to accept this alternative NHX signifier.
		String nhx = tree.getNHX();
		nhx = nhx.replaceAll("&", "*");
		nhx = nhx.replaceAll("'", "`");
		changedFields.put("tree", nhx);
		ArrayList<String> keyvals = new ArrayList<String>();
		for (String key : changedFields.keySet())
		{
			String s = "";
			s += key + "=" + "'" + changedFields.get(key) + "'";
			keyvals.add(s);
		}
		return JavaUtils.join("&", keyvals);
	}

	@Override
	public void setAnnotations(String nodeLabel, String annotationJson)
	{
		PhyloTree tree = (PhyloTree) pwc.trees().getTree();
		if (tree == null)
			return;
		List<PhyloNode> nodes = tree.search(nodeLabel);
		PhyloNode n = nodes.get(0);
		if (n == null)
			return;
		n.setAnnotationsFromJson(annotationJson);
	}
	
	public void setAnnotation(String nodeLabel, String key, String value)
	{
		PhyloTree tree = (PhyloTree) pwc.trees().getTree();
		if (tree == null)
			return;
		List<PhyloNode> nodes = tree.search(nodeLabel);
		PhyloNode n = nodes.get(0);
		if (n == null)
			return;
		n.setAnnotation(key, value);
	}
}