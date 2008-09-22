package org.andrewberman.evogame;

import java.util.ArrayList;
import java.util.HashMap;

import org.andrewberman.ui.UIGlobals;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.menu.MenuItem;
import org.andrewberman.ui.menu.Toolbar;
import org.phylowidget.PhyloWidget;
import org.phylowidget.net.PWClipUpdater;
import org.phylowidget.net.PWTreeUpdater;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.ui.PhyloConfig;
import org.phylowidget.ui.PhyloUI;

import processing.core.PGraphicsJava2D;

public class EvoGameApplet extends PhyloWidget
{
	public static EvoGameApplet p;

	public static String nextMenu;

	private Toolbar next;
	
	public EvoGameApplet()
	{
		p = this;
	}
	
	@Override
	public void setup()
	{
//		super.setup();
		
		frameRate(60);

		PGraphicsJava2D pg = (PGraphicsJava2D) g;

		new UIGlobals(this);
		cfg = new PhyloConfig();
		trees = new EvoTreeManager(this);
		ui = new PhyloUI(this);

		treeUpdater = new PWTreeUpdater();
		clipUpdater = new PWClipUpdater();

		ui.setup();
		trees.setup();
		clearQueues();
		
		trees.camera.nudgeTo(200, 0);
		trees.camera.zoomTo(0.7f);
		
		unregisterDraw(UIGlobals.g.event());
		
		DragDropImage.nodeToImage = null; // Set this to null -- applets HATE static variables left over from previous instantiations.
		
		changeSetting("colorHoveredBranch", "false");
		changeSetting("textSize", "0.7");
		changeSetting("respondToMouseWheel","false");
		changeSetting("suppressMessages","true");

		if (ui.context != null)
			ui.context.setGlow(false);
		
//		next = new Toolbar(this);
//		next.add("Next");
//		next.setPosition(400, 400);
//		next.setEnabled(false);
	}
	
	String curMenu = "";
	@Override
	public synchronized void changeSetting(String setting, String newValue)
	{
		if (setting.equals("menus"))
		{
			curMenu = newValue;
			DragDropImage.nodeToImage = null;
		}
		super.changeSetting(setting, newValue);
	}
	
	@Override
	public void resize(int width, int height)
	{
		super.resize(width, height);
		PGraphicsJava2D pg = (PGraphicsJava2D) g;
		if (pg == null)
			return;
		UIUtils.setRenderingHints(pg);
	}
	
	@Override
	public synchronized void draw()
	{
		super.draw();		
		for (MenuItem m : ui.menus)
		{
			m.layout();
		}
	}
	
	public void next()
	{
		if (curMenu.equals("1.xml"))
			changeSetting("menus","2.xml");
		else
			changeSetting("menus","1.xml");
	}
	
	public boolean allNodesAreCorrect()
	{
		if (trees.getTree() == null)
			return false;
		HashMap<PhyloNode,DragDropImage> map = DragDropImage.nodeToImage;
		if (map == null)
		{
			return false;
		}
		ArrayList<PhyloNode> leaves = new ArrayList<PhyloNode>();
		trees.getTree().getAll(trees.getTree().getRoot(), leaves, null);
		int size = leaves.size();
		if (map.size() != leaves.size())
		{
			return false;
		}
		for (PhyloNode n : map.keySet())
		{
			DragDropImage ddi = map.get(n);
			if (!ddi.isAttachedToCorrectNode())
			{
				return false;
			}
		}
		return true;
	}
	
}
