package org.phylowidget.ui;

import java.io.File;
import java.util.ArrayList;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.ShortcutManager;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.menu.Menu;
import org.andrewberman.ui.menu.MenuIO;
import org.andrewberman.ui.menu.MenuItem;
import org.andrewberman.ui.menu.RadialMenuItem;
import org.andrewberman.ui.menu.ToolDock;
import org.andrewberman.ui.menu.ToolDockItem;
import org.andrewberman.ui.menu.Toolbar;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;
import org.phylowidget.render.RenderOutput;
import org.phylowidget.render.RenderStyleSet;
import org.phylowidget.render.TreeRenderer;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeClipboard;
import org.phylowidget.tree.TreeIO;
import org.phylowidget.tree.TreeManager;

import processing.core.PApplet;

public final class UIManager implements TreeActions, NodeActions
{
	PApplet p;

	public FocusManager focus;
	public EventManager event;
	public ShortcutManager keys;
	public TreeClipboard clipboard = TreeClipboard.instance();

	public NearestNodeFinder nearest;
	public NodeTraverser traverser;

	public float textRotation = 0;
	public float textSize = 1;
	public float lineSize = RenderStyleSet.defaultStyle().lineThicknessMultiplier;
	public float nodeSize = RenderStyleSet.defaultStyle().nodeSizeMultiplier;
	public float renderThreshold = 500f;
	
	
	public boolean showBranchLengths = false;
	public boolean showCladeLabels = false;
	
	PhyloTextField text;
	PhyloContextMenu context;

	public UIManager(PApplet p)
	{
		this.p = p;
		UIUtils.loadUISinglets(p);
		focus = FocusManager.instance;
		event = EventManager.instance;
		keys = ShortcutManager.instance;
	}

	public void setup()
	{
		traverser = new NodeTraverser(p);
		nearest = new NearestNodeFinder(p);
		text = new PhyloTextField(p);
		String menuFile = (String) PhyloWidget.props.get("menuFile");
		ArrayList menus = MenuIO.loadFromXML(p, menuFile, this);

		for (int i = 0; i < menus.size(); i++)
		{
			Menu menu = (Menu) menus.get(i);
			if (menu.getClass() == PhyloContextMenu.class)
			{
				context = (PhyloContextMenu) menu;
			}
		}
	}

	public void update()
	{
	}

	public RootedTree getCurTree()
	{
		return PhyloWidget.trees.getTree();
	}

	public void layout()
	{
		PhyloWidget.trees.getRenderer().layout();
	}

	public Object curNode()
	{
		return curRange().node;
	}

	public NodeRange curRange()
	{
		return context.curNodeRange;
	}

	/*
	 * Node actions.
	 */

	public void editBranchLength()
	{
		text.startEditing(curRange(), PhyloTextField.BRANCH_LENGTH);
	}

	public void editName()
	{
		text.startEditing(curRange(), PhyloTextField.LABEL);
	}

	public void reroot()
	{
		NodeRange r = curRange();
		r.render.getTree().reroot(curNode());
	}

	public void switchChildren()
	{
		NodeRange r = curRange();
		r.render.getTree().flipChildren(curNode());
		r.render.layout();
	}

	public void flipSubtree()
	{
		NodeRange r = curRange();
		r.render.getTree().reverseSubtree(curNode());
		r.render.layout();
	}

	public void addSister()
	{
		NodeRange r = curRange();
		RootedTree tree = r.render.getTree();
		PhyloNode sis = (PhyloNode) tree.createAndAddVertex("");
		tree.addSisterNode(curNode(), sis);
	}

	public void addChild()
	{
		NodeRange r = curRange();
		RootedTree tree = r.render.getTree();
		tree.addChildNode(curNode());
	}

	public void cutNode()
	{
		NodeRange r = curRange();
		clipboard.cut(r.render.getTree(), r.node);
	}

	public void copyNode()
	{
		NodeRange r = curRange();
		clipboard.copy(r.render.getTree(), r.node);
	}

	public void pasteNode()
	{
		NodeRange r = curRange();
		clipboard.paste(r.render.getTree(), r.node);
	}

	public void clearClipboard()
	{
		clipboard.clearClipboard();
	}

	public void deleteNode()
	{
		NodeRange r = curRange();
		RootedTree g = r.render.getTree();
		g.deleteNode(curNode());
	}

	public void deleteSubtree()
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

	public void zoomToFull()
	{
		TreeManager.camera.zoomCenterTo(0, 0, p.width, p.height);
	}

	public void mutateOnce()
	{
		PhyloWidget.trees.mutateTree();
	}

	public void mutateSlow()
	{
		PhyloWidget.trees.startMutatingTree(1000);
	}

	public void mutateFast()
	{
		PhyloWidget.trees.startMutatingTree(100);
	}

	public void stopMutating()
	{
		PhyloWidget.trees.stopMutatingTree();
	}

	/*
	 * Tree Actions.
	 */

	public void newTree()
	{
		PhyloWidget.trees.setTree(TreeIO.parseNewickString(new PhyloTree(),
		// "(((dog:22.90000,(((bear:13.00000,raccoon:13.00000):5.75000,(seal:12.00000,sea_lion:12.00000):6.75000):1.00000,weasel:19.75000):3.15000):22.01667,cat:44.91667):27.22619,monkey:72.14286);"));
				// "(Bovine,(Hylobates:0.36079,(Pongo:0.33636,(G._Gorilla:0.17147,
				// (P._paniscus:0.19268,H._sapiens:0.11927):0.08386):0.06124):0.15057),
				// Rodent);"));
				"Homo Sapiens"));
		layout();
	}

	public void flipTree()
	{
		RootedTree t = getCurTree();
		t.reverseSubtree(t.getRoot());
		layout();
	}

	public void sortTree()
	{
		RootedTree tree = getCurTree();
		tree.ladderizeSubtree(tree.getRoot());
		layout();
	}

	public void removeElbows()
	{
		RootedTree tree = getCurTree();
		tree.cullElbowsBelow(tree.getRoot());
		layout();
	}

	public void phyloView()
	{
		PhyloWidget.trees.phylogramRender();
	}

	public void cladoView()
	{
		PhyloWidget.trees.cladogramRender();
	}

	public void diagonalView()
	{
		PhyloWidget.trees.diagonalRender();
	}

	public void openFile()
	{
		File f = p.inputFile("Select Newick-format text file...");
		p.noLoop();
		RootedTree t = TreeIO.parseFile(new PhyloTree(),f);
		PhyloWidget.trees.setTree(t);
		p.loop();
	}
	
	public void outputBig()
	{
		TreeRenderer tr = PhyloWidget.trees.getRenderer();
		RenderOutput.save(p, getCurTree(), tr, 1600, 1200);
	}

	public void outputPDF()
	{
		TreeRenderer tr = PhyloWidget.trees.getRenderer();
		RenderOutput.savePDF(p, getCurTree(), tr);
	}

	public void outputSmall()
	{
		TreeRenderer tr = PhyloWidget.trees.getRenderer();
		RenderOutput.save(p, getCurTree(), tr, 640, 480);
	}
}
