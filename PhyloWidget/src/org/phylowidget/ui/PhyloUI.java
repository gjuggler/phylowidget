package org.phylowidget.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

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

	public String menuFile = "menus/full-menus.xml";
	public String clipJavascript = "updateClip";
	public String treeJavascript = "updateTree";
	public String foregroundColor = "(0,0,0)";
	public String backgroundColor = "(255,255,255)";
	public String tree = "Homo Sapiens";

	public float textRotation = 0;
	public float textSize = 1;
	public float lineSize = 0.1f;
	public float nodeSize = 0.3f;
	public float renderThreshold = 300f;

	public boolean showBranchLengths = false;
	public boolean showCladeLabels = false;
	public boolean outputAllInnerNodes = false;
	public boolean enforceUniqueLabels = false;

	/*
	 * Node actions.
	 */

	@Override
	public void setup()
	{
		/*
		 * Run the PhyloUISetup's setup() method. Note that this method
		 * loads the menus and their associated default values.
		 */
		super.setup();
		/*
		 * Load the preferences.
		 * Note that preferences should be loaded from the following settings sources
		 * with the specified priority order:
		 * 5. hard-coded default (set below)
		 * 4. Menu XML file (loaded in the above setup() method call)
		 * 3. URL parsing (get / post variables) (done via PHP, put into applet tags)
		 * 2. applet tag
		 * 1. phylowidget.properties
		 */

		Field[] fArray = PhyloUI.class.getDeclaredFields();
		List<Field> fields = Arrays.asList(fArray);

		/*
		 * PRIORITY 2: APPLET TAGS
		 */
		for (Field f : fields)
		{
			String param = p.getParameter(f.getName());
			if (param != null)
			{
				setField(f, param);
			}
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
			e.printStackTrace();
		}
		for (Field f : fields)
		{
			if (props.containsKey(f.getName()))
			{
				setField(f, props.getProperty(f.getName()));
			}
		}

		/*
		 * Initialize the tree.
		 */
		PhyloWidget.trees.setTree(TreeIO.parseNewickString(new PhyloTree(), tree));
	}

	private void setField(Field f, String param)
	{
		try
		{
			Class<?> c = f.getType();
			if (c == String.class)
				f.set(this, param);
			else if (c == Boolean.TYPE)
				f.setBoolean(this, Boolean.parseBoolean(param));
			else if (c == Float.TYPE)
				f.setFloat(this, Float.parseFloat(param));
			else if (c == Integer.TYPE)
				f.setInt(this, Integer.parseInt(param));
			else if (c == Double.TYPE)
				f.setDouble(this, Double.parseDouble(param));
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
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
		r.render.layout();
	}

	public void nodeFlipSubtree()
	{
		NodeRange r = curRange();
		r.render.getTree().reverseSubtree(curNode());
		getCurTree().modPlus();
		r.render.layout();
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
		g.deleteNode(curNode());
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

	// *******************************************************
	// ACTIONS
	// *******************************************************

	public void viewPhylogram()
	{
		PhyloWidget.trees.phylogramRender();
	}

	public void viewCladogram()
	{
		PhyloWidget.trees.cladogramRender();
	}

	public void viewDiagonal()
	{
		PhyloWidget.trees.diagonalRender();
	}

	public void viewCircle()
	{
		PhyloWidget.trees.circleRender();
	}
	
	public void viewZoomToFull()
	{
		TreeManager.camera.zoomCenterTo(0, 0, p.width, p.height);
	}

	/*
	 * Tree Actions.
	 */

	public void treeNew()
	{
		PhyloWidget.trees.setTree(TreeIO.parseNewickString(new PhyloTree(), "Homo Sapiens"));
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
		tree.cullElbowsBelow(tree.getRoot());
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
		PhyloWidget.trees.startMutatingTree(100);
	}

	public void treeStopMutating()
	{
		PhyloWidget.trees.stopMutatingTree();
	}

	/*
	 * File actions.
	 */

	public void fileOpen()
	{
		final File f = p.inputFile("Select Newick-format text file...");
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
		setMessage("Outputting PDF...");
		new Thread()
		{
			public void run()
			{
				RenderOutput.savePDF(p, getCurTree(), tr);
				setMessage("");
			}
		}.start();

	}
}
