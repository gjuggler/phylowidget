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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.ShortcutManager;
import org.andrewberman.ui.UIGlobals;
import org.andrewberman.ui.menu.MenuItem;
import org.andrewberman.ui.menu.ToolDock;
import org.andrewberman.ui.menu.Toolbar;
import org.phylowidget.PhyloTree;
import org.phylowidget.PhyloWidget;
import org.phylowidget.net.NodeInfoUpdater;
import org.phylowidget.net.SecurityChecker;
import org.phylowidget.render.BasicTreeRenderer;
import org.phylowidget.render.NodeRange;
import org.phylowidget.tree.CachedRootedTree;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;

import processing.core.PApplet;

public class PhyloUI implements Runnable
{
	PhyloWidget p;

	public FocusManager focus;
	public EventManager event;
	public ShortcutManager keys;
	public TreeClipboard clipboard;

	//	public NearestNodeFinder nearest;
	//	public NodeTraverser traverser;

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

		text = new PhyloTextField(p);
		nodeUpdater = new NodeInfoUpdater();

		clipboard = new TreeClipboard(p);

		thread = new Thread(this);
		thread.start();
	}

	public ArrayList<MenuItem> menus;

	public void run()
	{
		/*
		 * Then, load properties from the applet.
		 */
		try
		{
			loadFromApplet(p);
			p.callMethod("setMenusIfNull");
		} catch (Exception e)
		{
			// Do nothing. Continue on...
		}
		checkPermissions();
	}

	public void setMenusIfNull()
	{
		if (menus == null)
			setMenus();
	}

	boolean runningInBrowser()
	{
		String appViewer = p.getAppletContext().getClass().getCanonicalName();
		System.out.println(appViewer);
		if (appViewer.toLowerCase().contains("appletviewer"))
			return false;
		else
			return true;
	}

	void disposeMenus()
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
	}

	public synchronized void setMenus()
	{
		disposeMenus();
		String[] menuFiles = PhyloWidget.cfg.menus.split(";");
		ArrayList<MenuItem> allMenus = new ArrayList<MenuItem>();
		for (String menuFile : menuFiles)
		{
			if (menuFile.trim().length() == 0)
				continue;
			// GJ 27-8-08: remove quotes from menu specification.
			menuFile.replaceAll("'", "");
			menuFile.replaceAll("\"", "");
			PhyloMenuIO io = new PhyloMenuIO();
			Reader r = null;
			InputStream in = null;
			if (menuFile.toLowerCase().startsWith("http"))
			{
				try
				{
					URL url = new URL(menuFile);
					in = url.openStream();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			} else
			{
				in = p.createInput("menus/" + menuFile);
				//				in = p.openStream("menus/" + menuFile);
				if (in == null)
				{
					//				in = p.openStream(menuFile);
					//					in = p.openStream(menuFile);
					in = p.createInput(menuFile);
				}
				if (in == null)
				{
					String path = p.getDocumentBase().toString();
					int ind = path.lastIndexOf("/");
					if (ind != -1)
						path = path.substring(0,ind);
//					System.out.println(path);
					in = p.createInput(path+"/"+menuFile);
				}
			}
			/*
			 * If "in" is STILL null, then let's try just loading the string itself.
			 */
			if (in == null)
			{
				r = new StringReader(menuFile);
			} else
			{
				r = new InputStreamReader(in);
				//				r = new StringReader(fileS);
			}
			ArrayList<MenuItem> theseMenus = io.loadFromXML(r, p, PhyloWidget.ui, p, PhyloWidget.cfg);
			configureMenus(theseMenus);
			allMenus.addAll(theseMenus);
		}
		this.menus = allMenus;
	}

	String streamToString(InputStream in)
	{
		int c = 0;
		InputStreamReader r = new InputStreamReader(in);
		StringBuffer sb = new StringBuffer();
		try
		{
			while ((c = r.read()) != -1)
			{
				sb.append((char) c);
			}
			return sb.toString();
		} catch (Exception e)
		{
			return "";
		}
	}

	public void loadFromApplet(PApplet app) throws Exception
	{
		/*
		 * Let's first load the URL query parameters.
		 */
		//		System.out.println(app.getDocumentBase());
		/*
		 * The Javascript-defined parameters should take precedence, so now we'll load those up:
		 */
		HashMap<String, String> map = new HashMap<String, String>();
		Field[] fields = PhyloConfig.class.getDeclaredFields();
		for (Field f : fields)
		{
			try
			{
				String param = app.getParameter(f.getName());
				if (param != null)
				{
					//					map.put(f.getName(), param);
					p.changeSetting(f.getName(), param);
				}
			} catch (Exception e)
			{
				//				e.printStackTrace();
			}
		}
		//		System.out.println(map);
		//		MethodAndFieldSetter.setMethodsAndFields(PhyloWidget.cfg, map);
	}

	protected synchronized void configureMenus(ArrayList menus)
	{
		/*
		 * Some special handling of specific menus.
		 */
		for (int i = 0; i < menus.size(); i++)
		{
			MenuItem menu = (MenuItem) menus.get(i);
			if (menu instanceof PhyloContextMenu)
			{
				this.context = (PhyloContextMenu) menu;
				continue;
			}

			if (menu.getClass() == Toolbar.class)
			{
				toolbar = (Toolbar) menu;
				MenuItem item = toolbar.get("Search:");
				if (item != null)
				{
					search = (SearchBox) item;
					search.setText(PhyloWidget.cfg.search);
				}
			}

			if (menu.getClass() == ToolDock.class)
			{
				ToolDock td = (ToolDock) menu;
			}
		}
	}

	void checkPermissions()
	{
		SecurityChecker sc = new SecurityChecker(UIGlobals.g.getP());
		canWriteFiles = sc.canWriteFiles();
		canReadFiles = sc.canReadFiles();
		canAccessInternet = sc.canAccessInternet();
	}

	/**
	 * Some utility methods and fields for UI items dependent on security
	 * permissions.
	 */
	boolean canWriteFiles;
	boolean canReadFiles;
	boolean canAccessInternet;

	public boolean canWriteFiles()
	{
		return canWriteFiles;
	}

	public boolean canReadFiles()
	{
		return canReadFiles;
	}

	public boolean canAccessInternet()
	{
		return canAccessInternet;
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

	public void forceLayout()
	{
		if (PhyloWidget.trees.getRenderer() != null)
		{
			BasicTreeRenderer render = (BasicTreeRenderer) PhyloWidget.trees.getRenderer();
			render.forceLayout();
		}
		updateJS();
	}

	public PhyloNode getCurNode()
	{
		if (curRange() == null)
			return null;
		else
			return curRange().node;
	}

	public void search()
	{
		if (search != null)
		{
			search.setText(PhyloWidget.cfg.search);
		} else
		{
			PhyloTree t = (PhyloTree) getCurTree();
			if (t != null)
				t.searchAndMarkFound(PhyloWidget.cfg.search);
		}
	}

	public NodeRange curRange()
	{
		if (context == null)
			return null;
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

	AnnotationEditorDialog annotation;

	public void nodeEditAnnotation()
	{
		SwingUtilities.invokeLater(new Runnable()
		{

			public void run()
			{
				if (annotation == null)
					annotation = new AnnotationEditorDialog(getFrame(), p);
				annotation.setNode(getCurNode());
				annotation.setVisible(true);
			}
		});
	}

	public void nodeReroot()
	{
		NodeRange r = curRange();
		synchronized (r.render.getTree())
		{
			r.render.getTree().reroot(getCurNode());
		}
	}
	
	public void reroot()
	{
		nodeReroot();
	}

	public void nodeSwitchChildren()
	{
		NodeRange r = curRange();
		r.render.getTree().flipChildren(getCurNode());
		r.render.layoutTrigger();
	}

	public void nodeFlipSubtree()
	{
		NodeRange r = curRange();
		r.render.getTree().reverseSubtree(getCurNode());
		getCurTree().modPlus();
		r.render.layoutTrigger();
	}

	public void nodeAddSister()
	{
		NodeRange r = curRange();
		RootedTree tree = r.render.getTree();
		PhyloNode sis = (PhyloNode) tree.createAndAddVertex();
		tree.addSisterNode(getCurNode(), sis);
	}

	public void nodeAddChild()
	{
		NodeRange r = curRange();
		RootedTree tree = r.render.getTree();
		tree.addChildNode(getCurNode());
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

	public void selectNode(String s)
	{
		PhyloTree tree = (PhyloTree) getCurTree();
		List<PhyloNode> nodes = tree.search(s);
		if (nodes.size() == 0)
		{
			System.err.println("Node " + s + " not found!");
		}
		context.curNodeRange = nodes.get(0).range;
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
					clipboard.paste((CachedRootedTree) r.render.getTree(), r.node);
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
			g.deleteNode(getCurNode());
		}
	}

	public void nodeDeleteSubtree()
	{
		NodeRange r = curRange();
		RootedTree g = r.render.getTree();
		synchronized (g)
		{
			g.deleteSubtree(getCurNode());
		}
	}

	/*
	 * View actions.
	 */

	public void viewUnrooted()
	{
		PhyloWidget.trees.unrootedRender();
	}

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

	public void zoomToFull()
	{
		//		TreeManager.camera.zoomCenterTo(0, 0, p.width, p.height);
		PhyloWidget.trees.fillScreen();
	}

	/*
	 * Tree Actions.
	 */

	public void treeNew()
	{
		synchronized (PhyloWidget.trees.getTree())
		{
			PhyloWidget.trees.setTree(PhyloConfig.DEFAULT_TREE);
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
			tree.removeElbowsBelow(tree.getRoot());
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
				//				tree.alignLeaves();
				tree.makeSubtreeUltrametric(tree.getRoot());
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

	public void nodeLoadImage()
	{
		new Thread()
		{
			public void run()
			{
				getCurNode().loadThumbImage();
			}
		}.start();
	}

	public void treeLoadImages()
	{
		new Thread()
		{
			public void run()
			{
				RootedTree tree = getCurTree();
				ArrayList<PhyloNode> leaves = new ArrayList<PhyloNode>();
				tree.getAll(tree.getRoot(), leaves, null);
				for (PhyloNode n : leaves)
				{
					n.loadThumbImage();
				}
			}
		}.start();
	}

	public void treeSave()
	{
		FileDialog fd =
				new FileDialog(PhyloWidget.ui.getFrame(),
						"Choose your desination file. Tree will be in Newick / NHX format.", FileDialog.SAVE);
		fd.pack();
		fd.setVisible(true);
		String directory = fd.getDirectory();
		String filename = fd.getFile();
		if (filename == null)
		{
			PhyloWidget.setMessage("Tree save cancelled.");
			return;
		}
		final File f = new File(directory, filename);
		setMessage("Saving tree...");
		new Thread()
		{
			public void run()
			{
				p.noLoop();
				File dir = f.getParentFile();
				TreeIO.outputTreeImages(PhyloWidget.trees.getTree(), dir);
				String s = TreeIO.createNewickString(PhyloWidget.trees.getTree(), false);
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
		FileDialog fd =
				new FileDialog(PhyloWidget.ui.getFrame(), "Locate a Newick/NHX/Nexus format file.", FileDialog.LOAD);
		fd.pack();
		fd.setVisible(true);
		String directory = fd.getDirectory();
		String filename = fd.getFile();
		if (filename == null)
		{
			PhyloWidget.setMessage("Tree load cancelled.");
			return;
		}
		final File f = new File(directory, filename);
		setMessage("Loading tree...");
		new Thread()
		{
			public void run()
			{
				PhyloTree t = (PhyloTree) TreeIO.parseFile(new PhyloTree(), f);
				p.noLoop();
				if (t != null)
				{
					PhyloWidget.trees.setTree(t);
					setMessage("");
				} else
				{
					setMessage("Error loading tree!");
				}
				p.loop();
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

	public PhyloNode getHoveredNode()
	{
		PhyloNode nearest = context.getNearestNode();
		if (nearest != null)
		{
			// Test whether it's hovered or not.
			boolean contains = context.traverser.containsPoint(nearest.range, context.traverser.pt);
			if (contains)
				return nearest;
		}
		return null;
	}

	public void treeInput()
	{
		Frame parentFrame = getFrame();

		final InputDialog d = new InputDialog(parentFrame, "Enter your Newick-formatted tree here.");
		SecurityChecker sc = new SecurityChecker(p);
		if (sc.canAccessInternet())
		{
			Label l = new Label("A URL pointing to a Newick/NHX/Nexus file is also valid input.");
			d.add(l, BorderLayout.NORTH);
		}
		d.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				super.windowClosed(e);
				final String treeString = d.text.getText();
				if (treeString == null || treeString.length() == 0)
					return;

				new Thread()
				{
					public void run()
					{
						setMessage("Loading tree...");
						//						p.changeSetting("tree", treeString);
						PhyloTree t = (PhyloTree) TreeIO.parseNewickString(new PhyloTree(), treeString);
						p.noLoop();
						if (t != null)
						{
							PhyloWidget.trees.setTree(t);
							setMessage("");
						} else
						{
							setMessage("Error loading tree!");
						}
						p.loop();

						layout();
					}
				}.start();
			}
		});
		d.setVisible(true);
	}

	/*
	 * File actions.
	 */

	public void fileOutput()
	{
		ImageExportDialog ied = new ImageExportDialog(getFrame());
	}

	/*
	 * Conditional calls.
	 */
	public boolean hasClipboard()
	{
		return !clipboard.isEmpty();
	}

	public boolean isNotRoot()
	{
		if (getCurTree() == null || getCurNode() == null)
			return true;
		return (getCurTree().getRoot() != getCurNode());
	}
	
	public void destroy()
	{
		if (annotation != null)
			annotation.dispose();
		annotation = null;
		disposeMenus();
		p = null;
		focus = null;
		event = null;
		keys = null;
		clipboard = null;
		//		nearest = null;
		//		traverser = null;
		text = null;
		context = null;
		toolbar = null;
		search = null;
		nodeUpdater = null;
		thread = null;
		menus = null;
	}
}
