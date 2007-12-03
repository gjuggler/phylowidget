package org.phylowidget.ui;

import java.io.File;

import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;
import org.phylowidget.render.RenderOutput;
import org.phylowidget.render.RenderStyleSet;
import org.phylowidget.render.TreeRenderer;
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

	/*
	 * Preferences. Note that preferences should be loaded from the following settings sources
	 * with the specified priority order:
	 * 5. hard-coded default (set below)
	 * 4. URL parsing (get / post variables)
	 * 3. applet tag
	 * 2. phylowidget.properties
	 * 1. menu XML file
	 * 
	 */

	public String menuFile = "menus/full-menus.xml";
	public String clipJavascript = "updateClip";
	public String treeJavascript = "updateTree";

	public float textRotation = 0;
	public float textSize = 1;
	public float lineSize = RenderStyleSet.defaultStyle().lineThicknessMultiplier;
	public float nodeSize = RenderStyleSet.defaultStyle().nodeSizeMultiplier;
	public float renderThreshold = 500f;
	public boolean showBranchLengths = false;
	public boolean showCladeLabels = false;

	/*
	 * Node actions.
	 */

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

	public void viewZoomToFull()
	{
		TreeManager.camera.zoomCenterTo(0, 0, p.width, p.height);
	}

	/*
	 * Tree Actions.
	 */

	public void treeNew()
	{
		PhyloWidget.trees.setTree(TreeIO.parseNewickString(new PhyloTree(),
		// "(((dog:22.90000,(((bear:13.00000,raccoon:13.00000):5.75000,(seal:12.00000,sea_lion:12.00000):6.75000):1.00000,weasel:19.75000):3.15000):22.01667,cat:44.91667):27.22619,monkey:72.14286);"));
				// "(Bovine,(Hylobates:0.36079,(Pongo:0.33636,(G._Gorilla:0.17147,
				// (P._paniscus:0.19268,H._sapiens:0.11927):0.08386):0.06124):0.15057),
				// Rodent);"));
				"Homo Sapiens"));
		layout();
	}

	public void treeFlip()
	{
		RootedTree t = getCurTree();
		t.reverseSubtree(t.getRoot());
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
		new Thread() {
			public void run()
			{
				RenderOutput.savePDF(p, getCurTree(), tr);
				setMessage("");
			}
		}.start();
		
	}
}
