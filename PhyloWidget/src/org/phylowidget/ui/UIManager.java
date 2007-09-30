package org.phylowidget.ui;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.ShortcutManager;
import org.andrewberman.ui.UIUtils;
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

public final class UIManager
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

		MenuIO.loadFromXML(p,"menus.xml");
		
		Toolbar t = new Toolbar(p);
		t.add("File");
		t.get("File").add("New Tree").setAction(this, "newTree").setShortcut(
				"control-n");
		t.get("File").add("Output Tree").setAction(this, "saveTree")
				.setShortcut("Control-s");
		t.add("View").add("Phylogram").setAction(PhyloWidget.trees,
				"phylogramRender");
		t.get("View").add("Cladogram").setAction(PhyloWidget.trees,
				"cladogramRender");
		t.get("View").add("Diagonalogram").setAction(PhyloWidget.trees,
				"diagonalRender");
		t.get("View").add("Zoom to full").setAction(this, "zoomToFull")
				.setShortcut("control-1");
		t.add("Tree");
		MenuItem auto = t.get("Tree").add("Mutator (for fun!)");
		auto.add("Mutate Once").setAction(this, "mutate").setShortcut(
				"control-m");
		auto.add("Mutate Slow").setAction(this, "mutateSlow");
		auto.add("Mutate Fast").setAction(this, "mutateFast");
		auto.add("Stop Mutating").setAction(this, "mutateStop").setShortcut(
				"control-shift-m");
		t.get("Tree").add("Ladderize All").setAction(this, "ladderizeTree")
				.setShortcut("control-l");
		t.get("Tree").add("Remove \"Elbows\"").setAction(this, "cullElbows")
				.setShortcut("control-e");
		t.layout();

		text = new PhyloTextField(p);

		context = new PhyloContextMenu(p);
		context.add(context.create("Add", 'a'));
		context.add(context.create("Delete", 'd'));
		context.add(context.create("Edit", 'e'));
		context.add(context.create("Vertex", 'v'));
		// Tree
		context.get("Vertex").add(context.create("Reroot", 'r')).setAction(
				this, "rerootNode");
		context.get("Vertex").add(context.create("Flip children", 'f'))
				.setAction(this, "flipChildren");
		context.get("Vertex").add(context.create("Reverse subtree", 'e'))
				.setAction(this, "reverseChildren");
		context.get("Vertex").add(context.create("Rename", 'n')).setAction(
				this, "renameNode");
		// Edit
		context.get("Edit").add(context.create("Cut", 'x')).setAction(this,
				"cutNode");
		context.get("Edit").add(context.create("Copy", 'c')).setAction(this,
				"copyNode");
		RadialMenuItem rmi = new RadialMenuItem()
		{
			public boolean isEnabled()
			{
				if (clipboard.isEmpty())
					return false;
				// if (context.curNodeRange.node.getState() != PhyloNode.NONE)
				// return false;
				return true;
			}
		};
		rmi.setName("Paste");
		rmi.setHint('v');
		context.get("Edit").add(rmi).setAction(this, "pasteNode");
		
		context.get("Edit").add(context.create("Cancel", 'n')).setAction(this,
				"clearClipboard");
		// Delete
		context.get("Delete").add(context.create("Subtree", 's')).setAction(
				this, "deleteSubtree");
		context.get("Delete").add(context.create("This node", 't')).setAction(
				this, "deleteNode");
		// Add
		context.get("Add").add(context.create("Sister node", 's')).setAction(
				this, "addSisterNode");
		context.get("Add").add(context.create("Child node", 'c')).setAction(
				this, "addChildNode");

		dock = new ToolDock(p);
		dock.add(dock.create("Arrow","Arrow","dock/arrow.png")).setShortcut("a");
		dock.add(dock.create("Scroll","Scroll","dock/scroll.png")).setShortcut("s");
		dock.add(dock.create("Zoom","Zoom","dock/zoom.png")).setShortcut("z");
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

	// *******************************************************
	// ACTIONS
	// *******************************************************

	public void zoomToFull()
	{
		TreeManager.camera.zoomCenterTo(0, 0, p.width, p.height);
	}

	public void mutate()
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

	public void mutateStop()
	{
		PhyloWidget.trees.stopMutatingTree();
	}

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

	public void saveTree()
	{
		String s = TreeIO.createNewickString(PhyloWidget.trees.getTree());
		System.out.println(s);
	}

	public void renameNode()
	{
		NodeRange r = context.curNodeRange;
		text.startEditing(r);
	}

	public void rerootNode()
	{
		NodeRange r = context.curNodeRange;
		r.render.getTree().reroot(r.node);
	}

	public void flipChildren()
	{
		NodeRange r = context.curNodeRange;
		r.render.getTree().flipChildren(r.node);
		r.render.layout();
	}

	public void reverseTree()
	{
		RootedTree t = PhyloWidget.trees.getTree();
		t.reverseAllChildren(t.getRoot());
	}

	public void reverseChildren()
	{
		NodeRange r = context.curNodeRange;
		r.render.getTree().reverseAllChildren(r.node);
		r.render.layout();
	}

	public void addSisterNode()
	{
		NodeRange r = context.curNodeRange;
		RootedTree tree = r.render.getTree();
		PhyloNode sis = (PhyloNode) tree.createAndAddVertex("");
		tree.addSisterNode(r.node, sis);
	}

	public void addChildNode()
	{
		NodeRange r = context.curNodeRange;
		RootedTree tree = r.render.getTree();
		tree.addChildNode(r.node);
	}

	public void cutNode()
	{
		NodeRange r = context.curNodeRange;
		clipboard.cut(r.render.getTree(), r.node);
	}

	public void copyNode()
	{
		NodeRange r = context.curNodeRange;
		clipboard.copy(r.render.getTree(), r.node);
	}

	public void pasteNode()
	{
		NodeRange r = context.curNodeRange;
		clipboard.paste(r.render.getTree(), r.node);
	}

	public void clearClipboard()
	{
		clipboard.clearClipboard();
	}

	public void deleteNode()
	{
		NodeRange r = context.curNodeRange;
		RootedTree g = r.render.getTree();
		g.deleteNode(r.node);
	}

	public void deleteSubtree()
	{
		NodeRange r = context.curNodeRange;
		RootedTree g = r.render.getTree();
		g.deleteSubtree(r.node);
	}

	public void ladderizeTree()
	{
		RootedTree tree = getCurTree();
		tree.ladderizeSubtree(tree.getRoot());
		layout();
	}

	public void cullElbows()
	{
		RootedTree tree = getCurTree();
		tree.cullElbowsBelow(tree.getRoot());
	}

	
	public void phylogramRender()
	{
		PhyloWidget.trees.phylogramRender();
	}
	
	public void cladogramRender()
	{
		PhyloWidget.trees.cladogramRender();
	}
	
	public void diagonalRender()
	{
		PhyloWidget.trees.diagonalRender();
	}
}
