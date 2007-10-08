package org.phylowidget.net;

import java.util.Properties;

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
		jsCall = PhyloWidget.props.getProperty("treeJavascript", "updateTree");
	}

	public void run()
	{
		String s = TreeIO.createNewickString(tree);
		String cmd = jsCall;
		JSObjectCrap.reflectJS(cmd, s);
	}

}
