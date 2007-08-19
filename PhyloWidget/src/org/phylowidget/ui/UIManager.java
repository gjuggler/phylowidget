package org.phylowidget.ui;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.ShortcutManager;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.menu.RadialMenuItem;
import org.andrewberman.ui.menu.Toolbar;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;
import org.phylowidget.tree.RootedTree;

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
	PhyloToolDock dock;

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

		Toolbar t = new Toolbar(p);
		t.add("File");
		t.get("File").add("New Tree").setAction(this, "newTree").setShortcut(
				"control-n");
		t.get("File").add("Output Tree").setAction(this, "saveTree").setShortcut(
				"Control-s");
		t.add("View").add("Phylogram").setAction(PhyloWidget.trees,
				"phylogramRender");
		t.get("View").add("Cladogram").setAction(PhyloWidget.trees,
				"cladogramRender");
		t.get("View").add("Diagonalogram").setAction(PhyloWidget.trees,
				"diagonalRender");
		t.get("View").add("Zoom to full").setAction(this, "zoomToFull")
				.setShortcut("control-1");
		t.add("Tree");
		t.get("Tree").add("Auto-Mutator");
		t.get("Auto-Mutator").add("Mutate Once").setAction(this, "mutate")
				.setShortcut("control-m");
		t.get("Auto-Mutator").add("Mutate Slow").setAction(this, "mutateSlow");
		t.get("Auto-Mutator").add("Mutate Fast").setAction(this, "mutateFast");
		t.get("Auto-Mutator").add("Stop Mutating")
				.setAction(this, "mutateStop").setShortcut("control-shift-m");
		t.get("Tree").add("Ladderize All").setAction(this, "ladderizeTree")
				.setShortcut("control-l");
		t.get("Tree").add("Cull Elbow Vertices").setAction(this, "cullElbows")
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
		RadialMenuItem rmi = new RadialMenuItem("Paste", 'v')
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

		dock = new PhyloToolDock(p);
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
		PhyloWidget.trees.camera.zoomCenterTo(0, 0, p.width, p.height);
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
	}

	public void saveTree()
	{
		
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
		tree.addSisterNode(r.node);
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

}
