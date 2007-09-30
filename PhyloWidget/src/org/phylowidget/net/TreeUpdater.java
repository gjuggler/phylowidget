package org.phylowidget.net;

import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.TreeIO;
import org.phylowidget.ui.PhyloTree;

public class TreeUpdater extends DelayedAction
{

	String parseMe;
	public void triggerUpdate(String s)
	{
		parseMe = s;
		trigger(200);
	}
	
	public void run()
	{
		PhyloTree t = new PhyloTree();
		t.loading = true;
		PhyloWidget.trees.setTree(TreeIO.parseNewickString(t,parseMe));
		t.loading = false;
	}
}
