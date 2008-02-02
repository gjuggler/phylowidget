/**************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PhyloWidget.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import org.phylowidget.net.NodeInfoUpdater;
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
	
	NodeInfoUpdater nodeUpdater;
	
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
//		nearest = new NearestNodeFinder(p);
		text = new PhyloTextField(p);
		
		nodeUpdater = new NodeInfoUpdater();
	
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
	
	public void updateNodeInfo(RootedTree t, PhyloNode n)
	{
		nodeUpdater.triggerUpdate(t, n);
	}
	
	public CachedRootedTree getCurTree()
	{
		return PhyloWidget.trees.getTree();
	}

	public void layout()
	{
		PhyloWidget.trees.getRenderer().layoutTrigger();
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
