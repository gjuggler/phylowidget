package org.phylowidget.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.Menu;
import org.andrewberman.ui.RadialPopupMenu;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;
import org.phylowidget.render.Point;
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
		menu.thetaLo = 0;
		menu.thetaHi = PConstants.TWO_PI;
		menu.radLo = 15;
		menu.radHi = 40;
		menu.addMenuItem("Add", 'a', this, "addSisterNode");
		menu.addMenuItem("Delete", 'x', this, "deleteNode");
		menu.addMenuItem("Rename", 'r', this, null);
		
		halo = new HoverHalo();
		
		// Keep in mind that the first added is the first drawn.
		addObject(halo);
		addObject(menu);
		
		halo.show();
	}
	
	public void update()
	{
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
		if (!menu.hidden)
			menu.hide();
	}
	
	public void addSisterNode()
	{
		NodeRange r = menu.curNode;
		Tree t = r.render.getTree();
		t.addSisterNode(r.node, new TreeNode("[Unnamed]"));
		hideMenu();
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
