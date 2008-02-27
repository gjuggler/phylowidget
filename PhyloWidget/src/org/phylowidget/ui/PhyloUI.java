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
package org.phylowidget.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.andrewberman.ui.menu.MenuItem;
import org.andrewberman.unsorted.FieldSetter;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;
import org.phylowidget.render.RenderOutput;
import org.phylowidget.render.RenderStyleSet;
import org.phylowidget.render.TreeRenderer;
import org.phylowidget.tree.CachedRootedTree;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;
import org.phylowidget.tree.TreeManager;

import processing.core.PApplet;

public class PhyloUI extends PhyloUISetup
{
	public PhyloUI(PApplet p)
	{
		super(p);
	}

	public String menuFile = "full-menus.xml";
	public String clipJavascript = "updateClip";
	public String treeJavascript = "updateTree";
	public String nodeJavascript = "updateNode";
	public String foreground = "(0,0,0)";
	public String background = "(255,255,255)";
	public String tree = "Homo Sapiens";
	public String search = "";

	public float textRotation = 0;
	// public float textSize = 1;
	public float lineSize = 0.1f;
	public float nodeSize = 0.3f;
	public float renderThreshold = 300f;
	public float minTextSize = 10;

	public boolean showBranchLengths = false;
	public boolean showCladeLabels = false;
	public boolean outputAllInnerNodes = false;
	public boolean enforceUniqueLabels = false;
	public boolean fitTreeToWindow = false;
	public boolean antialias = false;
	public boolean useBranchLengths = false;

	@Override
	public void setup()
	{
		HashMap<String,String> map = new HashMap<String,String>();
		
		/*
		 * PRIORITY 2: APPLET TAGS
		 */
		Field[] fields = this.getClass().getDeclaredFields();
		try
		{
			for (Field f : fields)
			{
				String param = p.getParameter(f.getName());
				if (param != null)
				{
					map.put(f.getName(), param);
				}
			}
		} catch (Exception e)
		{
//			 e.printStackTrace();
		}

		/*
		 * PRIORITY 1: PHYLOWIDGET.PROPERTIES
		 */
		Properties props = new Properties();
		try
		{
			props.load(p.openStream("phylowidget.properties"));
		} catch (IOException e)
		{
//			e.printStackTrace();
		}
		for (Object k : props.keySet())
		{
			map.put((String)k, props.getProperty((String)k));
		}
		
		FieldSetter.setFields(this, map);
		
		/*
		 * Run the PhyloUISetup's setup() method. Note that this method
		 * loads the menus and their associated default values.
		 */
		super.setup();
		
		/*
		 * Initialize the tree and search field.
		 */
		PhyloWidget.trees.setTree(TreeIO.parseNewickString(new PhyloTree(),
				tree));
		
		if (super.search != null)
			super.search.tf.replaceText(PhyloWidget.ui.search);
	}

	

	public void nodeEditBranchLength()
	{
		text.startEditing(curRange(), PhyloTextField.BRANCH_LENGTH);
	}

	public void nodeEditName()
	{
		text.startEditing(curRange(), PhyloTextField.LABEL);
	}

	public void nodeReroot()
	{
		NodeRange r = curRange();
		r.render.getTree().reroot(curNode());
	}

	public void nodeSwitchChildren()
	{
		NodeRange r = curRange();
		r.render.getTree().flipChildren(curNode());
		r.render.layoutTrigger();
	}

	public void nodeFlipSubtree()
	{
		NodeRange r = curRange();
		r.render.getTree().reverseSubtree(curNode());
		getCurTree().modPlus();
		r.render.layoutTrigger();
	}

	public void nodeAddSister()
	{
		NodeRange r = curRange();
		RootedTree tree = r.render.getTree();
		PhyloNode sis = (PhyloNode) tree.createAndAddVertex("");
		tree.addSisterNode(curNode(), sis);
	}

	public void nodeAddChild()
	{
		NodeRange r = curRange();
		RootedTree tree = r.render.getTree();
		tree.addChildNode(curNode());
	}

	public void nodeCut()
	{
		NodeRange r = curRange();
		clipboard.cut(r.render.getTree(), r.node);
	}

	public void nodeCopy()
	{
		NodeRange r = curRange();
		clipboard.copy(r.render.getTree(), r.node);
	}

	public void nodePaste()
	{
		final NodeRange r = curRange();
		setMessage("Pasting tree...");
		new Thread()
		{
			public void run()
			{
				synchronized (r.render.getTree())
				{
					clipboard.paste(r.render.getTree(), r.node);
				}
				setMessage("");
			}
		}.start();

	}

	public void nodeClearClipboard()
	{
		clipboard.clearClipboard();
	}

	public void nodeDelete()
	{
		NodeRange r = curRange();
		RootedTree g = r.render.getTree();
		synchronized (g)
		{
			g.deleteNode(curNode());
		}
	}

	public void nodeDeleteSubtree()
	{
		NodeRange r = curRange();
		RootedTree g = r.render.getTree();
		synchronized (g)
		{
			g.deleteSubtree(curNode());
		}
	}

	/*
	 * View actions.
	 */

	public void viewRectangular()
	{
		PhyloWidget.trees.rectangleRender();
	}

	public void viewDiagonal()
	{
		PhyloWidget.trees.diagonalRender();
	}

	public void viewCircular()
	{
		PhyloWidget.trees.circleRender();
	}

	public void viewZoomToFull()
	{
		//		TreeManager.camera.zoomCenterTo(0, 0, p.width, p.height);
		TreeManager.camera.fillScreen();
	}

	/*
	 * Tree Actions.
	 */

	public void treeNew()
	{
		PhyloWidget.trees.setTree(TreeIO.parseNewickString(new PhyloTree(),
				"Homo Sapiens"));
		layout();
	}

	public void treeFlip()
	{
		PhyloTree t = (PhyloTree) getCurTree();
		t.reverseSubtree(t.getRoot());
		t.modPlus();
		layout();
	}

	public void treeAutoSort()
	{
		RootedTree tree = getCurTree();
		tree.ladderizeSubtree(tree.getRoot());
		layout();
	}

	public void treeRemoveElbows()
	{
		RootedTree tree = getCurTree();
		synchronized (tree)
		{
			tree.cullElbowsBelow(tree.getRoot());
		}
		layout();
	}

	public void treeMutateOnce()
	{
		PhyloWidget.trees.mutateTree();
	}

	public void treeMutateSlow()
	{
		PhyloWidget.trees.startMutatingTree(1000);
	}

	public void treeMutateFast()
	{
		PhyloWidget.trees.startMutatingTree(50);
	}

	public void treeStopMutating()
	{
		PhyloWidget.trees.stopMutatingTree();
	}

	public void treeSave()
	{
		final File f = p.outputFile("Save tree as...");
		if (f == null)
			return;
		setMessage("Saving tree...");
		new Thread()
		{
			public void run()
			{
				p.noLoop();
				String s = TreeIO.createNewickString(PhyloWidget.trees
						.getTree());
				try
				{
					f.createNewFile();
					BufferedWriter r = new BufferedWriter(new FileWriter(f));
					r.append(s);
					r.close();
					p.loop();
					setMessage("");
				} catch (IOException e)
				{
					e.printStackTrace();
					p.loop();
					setMessage("Error writing file. Whoops!");
					try
					{
						Thread.sleep(2000);
					} catch (InterruptedException e1)
					{
						e1.printStackTrace();
					}
					setMessage("");
					return;
				}
			}
		}.start();
	}

	public void treeLoad()
	{
		final File f = p.inputFile("Select Newick-format text file...");
		if (f == null)
			return;
		setMessage("Loading tree...");
		new Thread()
		{
			public void run()
			{
				PhyloTree t = (PhyloTree) TreeIO.parseFile(new PhyloTree(), f);
				p.noLoop();
				PhyloWidget.trees.setTree(t);
				p.loop();
				setMessage("");
			}
		}.start();
	}

	/*
	 * File actions.
	 */

	public void fileOutputSmall()
	{
		TreeRenderer tr = PhyloWidget.trees.getRenderer();
		RenderOutput.save(p, getCurTree(), tr, 640, 480);
	}

	public void fileOutputBig()
	{
		TreeRenderer tr = PhyloWidget.trees.getRenderer();
		RenderOutput.save(p, getCurTree(), tr, 1600, 1200);
	}

	public void fileOutputPDF()
	{
		final TreeRenderer tr = PhyloWidget.trees.getRenderer();
		RenderOutput.savePDF(p, getCurTree(), tr);
	}
}
