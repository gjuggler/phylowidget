/*******************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * PhyloWidget. If not, see <http://www.gnu.org/licenses/>.
 */
package org.phylowidget.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.TextArea;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.ShortcutManager;
import org.andrewberman.ui.UIGlobals;
import org.andrewberman.ui.menu.Menu;
import org.andrewberman.ui.menu.MenuIO;
import org.andrewberman.ui.menu.MenuItem;
import org.andrewberman.ui.menu.ToolDock;
import org.andrewberman.ui.menu.Toolbar;
import org.andrewberman.ui.unsorted.MethodAndFieldSetter;
import org.phylowidget.PhyloWidget;
import org.phylowidget.net.NodeInfoUpdater;
import org.phylowidget.net.SecurityChecker;
import org.phylowidget.render.NodeRange;
import org.phylowidget.render.RenderOutput;
import org.phylowidget.render.TreeRenderer;
import org.phylowidget.tree.CachedRootedTree;
import org.phylowidget.tree.DefaultVertex;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeClipboard;
import org.phylowidget.tree.TreeIO;
import org.phylowidget.tree.TreeManager;

import processing.core.PApplet;

public class PhyloUI implements Runnable
{
	PhyloWidget p;

	public FocusManager focus;
	public EventManager event;
	public ShortcutManager keys;
	public TreeClipboard clipboard = TreeClipboard.instance();

	public NearestNodeFinder nearest;
	public NodeTraverser traverser;

	public PhyloTextField text;
	public PhyloContextMenu context;
	public Toolbar toolbar;
	public SearchBox search;

	NodeInfoUpdater nodeUpdater;

	public PhyloUI(PhyloWidget p)
	{
		this.p = p;
	}

	public Thread thread;

	public void setup()
	{
		focus = UIGlobals.g.focus();
		event = UIGlobals.g.event();
		keys = UIGlobals.g.shortcuts();

		traverser = new NodeTraverser(p);
		text = new PhyloTextField(p);
		nodeUpdater = new NodeInfoUpdater();

		thread = new Thread(this);
		thread.start();
	}

	ArrayList<MenuItem> menus;

	public void run()
	{
		/*
		 * Then, load properties from the applet.
		 */
		loadFromApplet(p);

		setMenus();

		checkToolbarPermissions();
		thread = null;
	}

	public void setMenus()
	{
		/*
		 * Menu file should be loaded first.
		 */
		if (menus != null)
		{
			for (MenuItem i : menus)
			{
				i.dispose();
			}
		}
		String menuFile = "menus/" + PhyloWidget.cfg.menuFile;
		menus = MenuIO
				.loadFromXML(p, menuFile, PhyloWidget.ui, PhyloWidget.cfg);

		configureMenus(menus);
	}

	public void loadFromApplet(PApplet p)
	{
		HashMap<String, String> map = new HashMap<String, String>();
		Field[] fields = PhyloConfig.class.getDeclaredFields();
		for (Field f : fields)
		{
			try
			{
				String param = p.getParameter(f.getName());
				if (param != null)
				{
					map.put(f.getName(), param);
				}
			} catch (Exception e)
			{
				//				e.printStackTrace();
			}
		}
		//		System.out.println(map);
		MethodAndFieldSetter.setMethodsAndFields(PhyloWidget.cfg, map);
	}

	protected void configureMenus(ArrayList menus)
	{
		/*
		 * Some special handling of specific menus.
		 */
		for (int i = 0; i < menus.size(); i++)
		{
			Menu menu = (Menu) menus.get(i);
			if (menu.getClass() == PhyloContextMenu.class)
			{
				context = (PhyloContextMenu) menu;
				continue;
			}

			if (menu.getClass() == Toolbar.class)
			{
				toolbar = (Toolbar) menu;
			}

			if (menu.getClass() == ToolDock.class)
			{
				ToolDock td = (ToolDock) menu;
			}
		}
	}

	void checkToolbarPermissions()
	{
		// Create a SecurityChecker object.
		SecurityChecker sc = new SecurityChecker(UIGlobals.g.getP());

		if (toolbar != null)
		{
			MenuItem s = toolbar.get("Search:");
			if (s != null)
			{
				search = (SearchBox) s;
				search.setText(PhyloWidget.cfg.search);
			}
			s = toolbar.get("From File...");
			if (s != null)
				s.setEnabled(sc.canReadFiles());
			s = toolbar.get("Save Tree...");
			if (s != null)
				s.setEnabled(sc.canWriteFiles());
			s = toolbar.get("Export Image");
			if (s != null)
				s.setEnabled(sc.canWriteFiles());
		}
	}

	public void updateNodeInfo(RootedTree t, PhyloNode n)
	{
		nodeUpdater.triggerUpdate(t, n);
	}

	public void updateJS()
	{
		PhyloTree t = (PhyloTree) PhyloWidget.trees.getTree();
		if (t != null)
			t.updateNewick();
	}

	public RootedTree getCurTree()
	{
		return PhyloWidget.trees.getTree();
	}

	public void layout()
	{
		if (PhyloWidget.trees.getRenderer() != null)
			PhyloWidget.trees.getRenderer().layoutTrigger();
		updateJS();
	}

	public DefaultVertex curNode()
	{
		return curRange().node;
	}

	public void search()
	{
		String s = PhyloWidget.cfg.search;
		PhyloTree tree = (PhyloTree) PhyloWidget.trees.getTree();
		//		if (search != null)
		//			search.setText(s);
		if (tree != null)
			tree.search(s);
	}

	public NodeRange curRange()
	{
		return context.curNodeRange;
	}

	public void nodeEditBranchLength()
	{
		text.startEditing(curRange(), PhyloTextField.BRANCH_LENGTH);
	}

	public void nodeEditName()
	{
		text.startEditing(curRange(), PhyloTextField.LABEL);
	}

	public void nodeReroot()
	{
		NodeRange r = curRange();
		synchronized (r.render.getTree())
		{
			r.render.getTree().reroot(curNode());
		}
	}

	public void nodeSwitchChildren()
	{
		NodeRange r = curRange();
		r.render.getTree().flipChildren(curNode());
		r.render.layoutTrigger();
	}

	public void nodeFlipSubtree()
	{
		NodeRange r = curRange();
		r.render.getTree().reverseSubtree(curNode());
		getCurTree().modPlus();
		r.render.layoutTrigger();
	}

	public void nodeAddSister()
	{
		NodeRange r = curRange();
		RootedTree tree = r.render.getTree();
		PhyloNode sis = (PhyloNode) tree.createAndAddVertex();
		tree.addSisterNode(curNode(), sis);
	}

	public void nodeAddChild()
	{
		NodeRange r = curRange();
		RootedTree tree = r.render.getTree();
		tree.addChildNode(curNode());
	}

	public void nodeCut()
	{
		NodeRange r = curRange();
		clipboard.cut(r.render.getTree(), r.node);
	}

	public void nodeCopy()
	{
		NodeRange r = curRange();
		clipboard.copy(r.render.getTree(), r.node);
	}

	void setMessage(String s)
	{
		PhyloWidget.setMessage(s);
	}

	public void nodeSwap()
	{
		final NodeRange r = curRange();
		setMessage("Swapping clipboard...");
		new Thread()
		{
			public void run()
			{
				try
				{
					synchronized (r.render.getTree())
					{
						clipboard.swap(r.render.getTree(), r.node);
					}
					setMessage("");
				} catch (Exception e)
				{
					e.printStackTrace();
					setMessage("Swap failed! Make sure the clipboard is not empty.");
				}
			}
		}.start();
	}

	public void nodePaste()
	{
		final NodeRange r = curRange();
		setMessage("Pasting clipboard...");
		new Thread()
		{
			public void run()
			{
				try
				{
					clipboard.paste((CachedRootedTree) r.render.getTree(),
							r.node);
					setMessage("");
				} catch (Exception e)
				{
					e.printStackTrace();
					setMessage("Paste failed! Make sure the clipboard is not empty.");
				}
			}
		}.start();
	}

	public void nodeClearClipboard()
	{
		clipboard.clearClipboard();
	}

	public void nodeDelete()
	{
		NodeRange r = curRange();
		RootedTree g = r.render.getTree();
		synchronized (g)
		{
			g.deleteNode(curNode());
		}
	}

	public void nodeDeleteSubtree()
	{
		NodeRange r = curRange();
		RootedTree g = r.render.getTree();
		synchronized (g)
		{
			g.deleteSubtree(curNode());
		}
	}

	/*
	 * View actions.
	 */

	public void viewRectangular()
	{
		PhyloWidget.trees.rectangleRender();
	}

	public void viewDiagonal()
	{
		PhyloWidget.trees.diagonalRender();
	}

	public void viewCircular()
	{
		PhyloWidget.trees.circleRender();
	}

	public void viewZoomToFull()
	{
		//		TreeManager.camera.zoomCenterTo(0, 0, p.width, p.height);
		TreeManager.camera.fillScreen(.8f);
	}

	/*
	 * Tree Actions.
	 */

	public void treeNew()
	{
		synchronized (PhyloWidget.trees.getTree())
		{
			System.out.println("Hey!");
			PhyloWidget.trees.setTree(TreeIO.parseNewickString(new PhyloTree(),
					"PhyloWidget"));
		}
		layout();
	}

	public void treeFlip()
	{
		PhyloTree t = (PhyloTree) getCurTree();
		t.reverseSubtree(t.getRoot());
		t.modPlus();
		layout();
	}

	public void treeAutoSort()
	{
		RootedTree tree = getCurTree();
		tree.ladderizeSubtree(tree.getRoot());
		layout();
	}

	public void treeRemoveElbows()
	{
		RootedTree tree = getCurTree();
		synchronized (tree)
		{
			tree.cullElbowsBelow(tree.getRoot());
		}
		layout();
	}

	/*
	 * Aligns the leaves of the tree, changing branch lengths accordingly.
	 */
	public void treeAlignLeaves()
	{
		new Thread()
		{
			@Override
			public void run()
			{
				setMessage("Aligning leaves...");
				RootedTree tree = getCurTree();
				tree.alignLeaves();
				layout();
				setMessage("");
			}
		}.start();
	}

	public void treeMutateOnce()
	{
		PhyloWidget.trees.mutateTree();
	}

	public void treeMutateSlow()
	{
		PhyloWidget.trees.startMutatingTree(1000);
	}

	public void treeMutateFast()
	{
		PhyloWidget.trees.startMutatingTree(50);
	}

	public void treeStopMutating()
	{
		PhyloWidget.trees.stopMutatingTree();
	}

	public void treeSave()
	{
		final File f = p.outputFile("Save tree as...");
		if (f == null)
			return;
		setMessage("Saving tree...");
		new Thread()
		{
			public void run()
			{
				p.noLoop();
				String s = TreeIO.createNewickString(PhyloWidget.trees
						.getTree(), false);
				try
				{
					f.createNewFile();
					BufferedWriter r = new BufferedWriter(new FileWriter(f));
					r.append(s);
					r.close();
					p.loop();
					setMessage("");
				} catch (IOException e)
				{
					e.printStackTrace();
					p.loop();
					setMessage("Error writing file. Whoops!");
					try
					{
						Thread.sleep(2000);
					} catch (InterruptedException e1)
					{
						e1.printStackTrace();
					}
					setMessage("");
					layout();
					return;
				}
			}
		}.start();
	}

	public void treeLoad()
	{
		final File f = p.inputFile("Select a Newick or Nexus text file...");
		if (f == null)
			return;
		setMessage("Loading tree...");
		new Thread()
		{
			public void run()
			{
				PhyloTree t = (PhyloTree) TreeIO.parseFile(new PhyloTree(), f);
				p.noLoop();
				PhyloWidget.trees.setTree(t);
				p.loop();
				setMessage("");
				layout();
			}
		}.start();
	}

	public Frame getFrame()
	{
		Frame parentFrame = null;
		Component comp = p.getParent();
		while (comp != null)
		{
			if (comp instanceof Frame)
			{
				parentFrame = (Frame) comp;
				break;
			}
			comp = comp.getParent();
		}
		if (parentFrame == null)
			parentFrame = new Frame();
		return parentFrame;
	}

	public void treeInput()
	{
		Frame parentFrame = getFrame();

		InputDialog d = new InputDialog(parentFrame,
				"Enter your Newick-formatted tree here.");
		d.setVisible(true);

		final String treeString = d.text.getText();
		if (treeString == null || treeString.length() == 0)
			return;

		new Thread()
		{
			public void run()
			{
				setMessage("Loading tree...");
				PhyloTree t = (PhyloTree) TreeIO.parseNewickString(
						new PhyloTree(), treeString);
				p.noLoop();
				PhyloWidget.trees.setTree(t);
				p.loop();
				setMessage("");
				layout();
			}
		}.start();
	}

	/*
	 * File actions.
	 */

	public void fileOutput()
	{
		ImageExportDialog ied = new ImageExportDialog(getFrame());
	}
}
