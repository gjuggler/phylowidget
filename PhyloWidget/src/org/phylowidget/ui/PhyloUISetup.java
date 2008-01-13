package org.phylowidget.ui;

import java.io.File;
import java.util.ArrayList;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.ShortcutManager;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.menu.CheckBox;
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
import org.phylowidget.tree.CachedRootedTree;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeClipboard;
import org.phylowidget.tree.TreeIO;
import org.phylowidget.tree.TreeManager;

import processing.core.PApplet;

public class PhyloUISetup
{
	PApplet p;

	public FocusManager focus;
	public EventManager event;
	public ShortcutManager keys;
	public TreeClipboard clipboard = TreeClipboard.instance();

	public NearestNodeFinder nearest;
	public NodeTraverser traverser;
	
	PhyloTextField text;
	PhyloContextMenu context;
	
	public PhyloUISetup(PApplet p)
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
	
		/*
		 * Load the menu file.
		 */
		String menuFile = (String) "menus/"+PhyloWidget.ui.menuFile;
		ArrayList menus = MenuIO.loadFromXML(p, menuFile, this);

		for (int i = 0; i < menus.size(); i++)
		{
			Menu menu = (Menu) menus.get(i);
			if (menu.getClass() == PhyloContextMenu.class)
			{
				context = (PhyloContextMenu) menu;
			}
			/*
			 * Set a callback for the "use branch lengths" property.
			 */
			MenuItem item = menu.get("Use Branch Lengths");
			if (item != null)
			{
				System.out.println("GOT IT");
				CheckBox cb = (CheckBox) item;
				cb.setAction(this,"layout");
			}
		}
	}

	void setPropString(String property, String value)
	{
		
	}
	
	void setPropFloat(String property, float value)
	{
		
	}
	
	public CachedRootedTree getCurTree()
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
	
	void setMessage(String s)
	{
		PhyloWidget.p.setMessage(s);
	}
	
}
