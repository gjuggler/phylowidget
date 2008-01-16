package org.phylowidget.net;

import org.andrewberman.unsorted.DelayedAction;
import org.andrewberman.unsorted.JSObjectCrap;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;

public class JSTreeUpdater extends DelayedAction
{
	RootedTree tree;
	String jsCall;

	public void triggerUpdate(RootedTree t)
	{
		tree = t;
		trigger(200);
		jsCall = PhyloWidget.ui.treeJavascript;
	}

	public void run()
	{
		String s = TreeIO.createNewickString(tree);
		String cmd = jsCall;
		JSObjectCrap.reflectJS(cmd, s);
	}

}
