package org.phylowidget.net;

import org.andrewberman.unsorted.DelayedAction;
import org.andrewberman.unsorted.JSObjectCrap;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;
import org.phylowidget.ui.PhyloUI;

public class JSClipUpdater extends DelayedAction
{
	String jsCall;
	String newClip;

	public void triggerUpdate(String s)
	{
		trigger(200);
		jsCall = PhyloWidget.ui.clipJavascript;
		newClip = s;
	}

	public void run()
	{
		String cmd = jsCall;
		JSObjectCrap.reflectJS(cmd, newClip);
	}
}
