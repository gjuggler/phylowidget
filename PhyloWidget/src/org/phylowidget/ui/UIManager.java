package org.phylowidget.ui;

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
import org.phylowidget.TreeManager;
import org.phylowidget.render.NodeRange;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;

import processing.core.PApplet;

public final class UIManager implements TreeActions, NodeActions
{
	PApplet p;

	public FocusManager focus;
	public EventManager event;
	public ShortcutManager keys;
	public TreeClipboard clipboard = TreeClipboard.instance();

	public NearestNodeFinder nearest;

	PhyloTextField text;
	PhyloContextMenu context;
	ToolDock dock;

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
		nearest = new NearestNodeFinder(p);

		ArrayList menus = MenuIO.loadFromXML(p,"menus.xml",this);
		
		for (int i=0; i < menus.size(); i++)
		{
			Menu menu = (Menu) menus.get(i);
			if (menu.getClass() == PhyloContextMenu.class)
			{
				context = (PhyloContextMenu)menu;
			}
		}
		
		text = new PhyloTextField(p);
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
		//TODO.
	}
	
	public void editName()
	{
		text.startEditing(curRange());
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
		r.render.getTree().reverseAllChildren(curNode());
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
		g.deleteSubtree(curNode());
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
		PhyloWidget.trees.setTree(new PhyloTree("PhyloWidget"));
		PhyloWidget.trees
				.setTree(TreeIO
						.parseNewickString(
								new PhyloTree(),
								// "(((dog:22.90000,(((bear:13.00000,raccoon:13.00000):5.75000,(seal:12.00000,sea_lion:12.00000):6.75000):1.00000,weasel:19.75000):3.15000):22.01667,cat:44.91667):27.22619,monkey:72.14286);"));
								"(Bovine,(Hylobates:0.36079,(Pongo:0.33636,(G._Gorilla:0.17147, (P._paniscus:0.19268,H._sapiens:0.11927):0.08386):0.06124):0.15057), Rodent);"));
	}

	public void outputTree()
	{
		String s = TreeIO.createNewickString(PhyloWidget.trees.getTree());
		System.out.println(s);
	}

	public void flipTree()
	{
		RootedTree t = PhyloWidget.trees.getTree();
		t.reverseAllChildren(t.getRoot());
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
}
