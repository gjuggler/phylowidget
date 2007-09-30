package org.phylowidget.net;

import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;

public class TextBoxUpdater extends DelayedAction
{
	RootedTree tree;
	
	public void triggerUpdate(RootedTree t)
	{
		tree = t;
		trigger(200);
	}
	
	public void run()
	{
		System.out.println("Updating!");
		if (JSObjectCrap.reflectionWorking)
		{
			String s = TreeIO.createNewickString(tree);
			String cmd = "replaceNewickText";
			JSObjectCrap.reflectJS(cmd, s);
		}
	}
	
}
