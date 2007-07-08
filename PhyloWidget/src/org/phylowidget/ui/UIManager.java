package org.phylowidget.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIObject;
import org.andrewberman.ui.menu.Menu;
import org.andrewberman.ui.menu.MenuItem;
import org.andrewberman.ui.menu.RadialMenu;
import org.andrewberman.ui.menu.RectMenuItem;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;
import org.phylowidget.tree.Tree;
import org.phylowidget.tree.TreeNode;

import processing.core.PConstants;

public final class UIManager implements MouseMotionListener, MouseListener, MouseWheelListener
{
	PhyloWidget p = PhyloWidget.p;
	
	public FocusManager focus = new FocusManager();
	public EventDispatcher event = new EventDispatcher();
	
	public ArrayList uiObjects = new ArrayList(5);
	
	public NodeRange nearest;
	public Point nearestP;
	public PhyloMenu menu;
	public HoverHalo halo;
	
	public UIManager()
	{
		focus = new FocusManager();
		event = new EventDispatcher();
	}
	
	public void setup()
	{
		focus.setup();
		event.setup();
		
		menu = new PhyloMenu();
		RectMenuItem hello = new RectMenuItem("Hello!");
		RectMenuItem whatever = new RectMenuItem("Whatev");
		hello.add(whatever);
		whatever.add(new RectMenuItem("Heya!"));
		RectMenuItem goodbye = new RectMenuItem("Goodbye!");
		RectMenuItem asdf = new RectMenuItem("Heya");
		goodbye.add(asdf);
		menu.add(hello);
		menu.add(goodbye);
		
		halo = new HoverHalo();
		
		// Keep in mind that the first added is the first drawn.
		addObject(halo);
		addObject(menu);
		
		halo.show();
	}
	
	public void update()
	{
//		menu.setArc(menu.thetaLo+.05f,menu.thetaHi+.05f);
		
		updateNearest();
		
		for (int i=0; i < uiObjects.size(); i++)
		{
			((UIObject)uiObjects.get(i)).draw();
		}
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
		menu.setNodeRange(r);
		menu.show();
	}
	
	public void hideMenu()
	{
		halo.setNodeRange(null);
		halo.restart();
	}
	
	public void addSisterNode()
	{
		NodeRange r = menu.curNode;
		Tree t = r.render.getTree();
		t.addSisterNode(r.node, new TreeNode("[Unnamed]"));
		hideMenu();
	}
	
	public void addChildNode()
	{
	}
	
	public void deleteNode()
	{
		NodeRange r = menu.curNode;
		Tree t = r.render.getTree();
		t.deleteNode(r.node);
		hideMenu();
	}
	
	//*******************************************************
	// UTILITY / LISTENER FUNCTIONS
	//*******************************************************
	
	public void addObject(UIObject o)
	{
		uiObjects.add(o);
		event.addListener(o);
	}
	
	public void removeObject(UIObject o)
	{
		uiObjects.remove(o);
		event.removeListener(o);
	}
	
	public void mouseEvent(MouseEvent e)
	{
	}
	
	public void mouseDragged(MouseEvent e){mouseEvent(e);}
	public void mouseMoved(MouseEvent e){mouseEvent(e);}
	public void mouseClicked(MouseEvent e){mouseEvent(e);}
	public void mouseEntered(MouseEvent e){mouseEvent(e);}
	public void mouseExited(MouseEvent e){mouseEvent(e);}
	public void mousePressed(MouseEvent e){mouseEvent(e);}
	public void mouseReleased(MouseEvent e){mouseEvent(e);}

	public void mouseWheelMoved(MouseWheelEvent e){mouseEvent(e);}
}
