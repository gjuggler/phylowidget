package org.phylowidget.net;

import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;

public class JSClipUpdater extends DelayedAction
{
	String jsCall;
	String newClip;

	public void triggerUpdate(String s)
	{
		trigger(200);
		jsCall = PhyloWidget.props.getProperty("clipJavascript", "updateClip");
		newClip = s;
	}

	public void run()
	{
		String cmd = jsCall;
		JSObjectCrap.reflectJS(cmd, newClip);
	}
}
