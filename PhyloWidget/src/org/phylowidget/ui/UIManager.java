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
import org.phylowidget.render.NodeRange;
import org.phylowidget.tree.Tree;
import org.phylowidget.tree.TreeNode;

public final class UIManager
{
	PhyloWidget p = PhyloWidget.p;
	
	public FocusManager focus;
	public EventManager event;
	public ShortcutManager keys; 
	
	public NodeRange nearest;
	public Point nearestP;
	public HoverHalo halo;
	
	PhyloMenu radial;
	
	public UIManager()
	{
		UIUtils.loadUISinglets(p);
		focus = FocusManager.instance;
		event = EventManager.instance;
		keys = ShortcutManager.instance;
	}
	
	public void setup()
	{
		halo = new HoverHalo(p);
		halo.show();
		
		Toolbar t = new Toolbar(p);
		t.add("File").add("Save").setAction(this, "doSomething");
		t.get("Save").setShortcut("control-s");
		t.get("File").add("Save...");
		t.get("Save...").add("Where?");
		t.add("Edit").add("Undo").setShortcut("control-z");
		t.get("Edit").add("Redo").setShortcut("control-shift-z");
		t.layout();
		
//		TextField text = new TextField(p);
//		text.text.insert(0, "Hello, world! How are you today?");
//		focus.setModalFocus(text);
		
		Dock dock = new Dock(p);
		dock.add("Pencil","pencil2.png");
		dock.add("Line","line.png");
		dock.add("Magnifier","magnifier.png");
		dock.add("Points","points.png");
		dock.add("Connected Lines","connectedlines.png");
		
		radial = new PhyloMenu(p);
//		radial.setPosition(50,50);
		radial.add("Delete");
		radial.add("Edit");
		radial.get("Edit").add(radial.create("Cut",'x'));
		radial.get("Edit").add(radial.create("Copy",'c'));
		radial.get("Edit").add(radial.create("Paste",'v'));
		radial.add("Add");
		radial.get("Add").add(radial.create("Sister Node",'s'));
		radial.get("Add").add(radial.create("Child Node",'c'));
		radial.get("Sister Node").add(radial.create("Whatever node",'w'));
//		radial.show();
		
	}
	
	public void doSomething()
	{
//		PhyloWidget.trees.mutator.randomlyMutateTree();
	}
	
	public void update()
	{
		radial.setArc(radial.thetaLo+.01f,radial.thetaHi+.01f);
		updateNearest();
	}
	
	public void updateNearest()
	{
		nearest = NearestNodeFinder.nearestNode(p.mouseX, p.mouseY);
		if (nearest == null)
		{
			nearestP = null;
			return;
		}
		nearestP = nearest.render.getPosition(nearest.node);
	}
	
	//*******************************************************
	// ACTIONS
	//*******************************************************
	
	public void showMenu(NodeRange r)
	{
		halo.setNodeRange(r);
		halo.becomeSolid();
		radial.setNodeRange(r);
		radial.show();
	}
	
	public void hideMenu()
	{
		halo.setNodeRange(null);
		halo.restart();
	}
	
	public void addSisterNode()
	{
		NodeRange r = radial.curNode;
		Tree t = r.render.getTree();
		t.addSisterNode(r.node, new TreeNode("[Unnamed]"));
		hideMenu();
	}
	
	public void addChildNode()
	{
	}
	
	public void deleteNode()
	{
//		NodeRange r = menu.curNode;
//		Tree t = r.render.getTree();
//		t.deleteNode(r.node);
		hideMenu();
	}

}
