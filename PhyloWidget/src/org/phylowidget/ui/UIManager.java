package org.phylowidget.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.ShortcutManager;
import org.andrewberman.ui.TextField;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.ifaces.UIObject;
import org.andrewberman.ui.menu.Dock;
import org.andrewberman.ui.menu.DockItem;
import org.andrewberman.ui.menu.RadialMenu;
import org.andrewberman.ui.menu.RadialMenuItem;
import org.andrewberman.ui.menu.Toolbar;
import org.phylowidget.PhyloWidget;
import org.phylowidget.oldtree.Tree;
import org.phylowidget.oldtree.TreeNode;
import org.phylowidget.render.NodeRange;
import org.phylowidget.temp.RootedTreeGraph;

import processing.core.PApplet;

public final class UIManager
{
	PApplet p;
	
	public FocusManager focus;
	public EventManager event;
	public ShortcutManager keys; 
	
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
		t.get("File").add("New Tree").setAction(this,"newTree").setShortcut("control-n");
		t.get("File").add("Quit").setAction(this,"quit").setShortcut("alt-f4");
		t.add("View").add("Cladogram").setAction(PhyloWidget.trees,"cladogramRender");
		t.get("View").add("Diagonal").setAction(PhyloWidget.trees,"diagonalRender");
		t.add("Tree");
		t.get("Tree").add("Auto-Mutate");
		t.get("Tree").add("Mutate Once").setAction(this,"mutate").setShortcut("control-m");
		t.get("Auto-Mutate").add("Mutate Slow").setAction(this,"mutateSlow").setShortcut("control-shift-m");
		t.get("Auto-Mutate").add("Mutate Fast").setAction(this,"mutateFast").setShortcut("control-alt-m");
		t.get("Auto-Mutate").add("Stop Mutating").setAction(this,"mutateStop");
		
//		t.add("File").add("Save").setAction(this, "doSomething");
//		t.get("Save").setShortcut("control-s");
//		t.get("File").add("Save...");
//		t.get("Save...").add("Where?");
//		t.add("Edit").add("Undo").setShortcut("control-z");
//		t.get("Edit").add("Redo").setShortcut("control-shift-z");
		t.layout();
		
		text = new PhyloTextField(p);
		text.text.insert(0, "Hello, world! How are you today?");
//		focus.setModalFocus(text);
		
		context = new PhyloContextMenu(p);
		context.thetaLo = p.THIRD_PI/2;
		context.thetaHi = p.TWO_PI+p.THIRD_PI/2;
		context.add(context.create("Rename",'r')).setAction(this, "renameNode");
//		context.add(context.create("Edit",'e'));
//		context.get("Edit").add(context.create("Cut",'x'));
//		context.get("Edit").add(context.create("Copy",'c'));
//		context.get("Edit").add(context.create("Paste",'v'));
		context.add(context.create("Delete",'d'));
		context.get("Delete").add(context.create("Subtree",'s')).setAction(this, "deleteSubtree");
		context.get("Delete").add(context.create("This Node",'t')).setAction(this, "deleteNode");
		context.add(context.create("Add",'a'));
		context.get("Add").add(context.create("Sister Node",'s')).setAction(this, "addSisterNode");
		context.get("Add").add(context.create("Child Node",'c')).setAction(this, "addChildNode");
		
		dock = new PhyloToolDock(p);
	}
	
	public void doSomething()
	{
		PhyloWidget.trees.mutator.randomlyMutateTree();
	}
	
	public void update()
	{
//		context.setArc(context.thetaLo+.01f,context.thetaHi+.01f);
	}
	
	//*******************************************************
	// ACTIONS
	//*******************************************************
	
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
		PhyloWidget.trees.clearTrees();
		PhyloWidget.trees.createTree("PhyloWidget");
	}
	
	public void quit()
	{
		p.exit();
	}
	
	public void renameNode()
	{
		NodeRange r = context.curNodeRange;
		text.startEditing(r);
	}
	
	public void addSisterNode()
	{
		NodeRange r = context.curNodeRange;
//		Tree t = r.render.getTree();
//		t.addSisterNode(r.node, t.createNode("[New Sister]"));
	}
	
	public void addChildNode()
	{
		NodeRange r = context.curNodeRange;
//		Tree t = r.render.getTree();
//		t.addChildNode(r.node, t.createNode("[New Child]"));
	}
	
	public void deleteNode()
	{
		NodeRange r = context.curNodeRange;
//		Tree t = r.render.getTree();
//		t.deleteNode(r.node);
	}

	public void deleteSubtree()
	{
		NodeRange r = context.curNodeRange;
		RootedTreeGraph g = r.render.getTree();
		g.reroot(r.node);
//		Tree t = r.render.getTree();
//		t.deleteSubtree(r.node);
	}
	
}
