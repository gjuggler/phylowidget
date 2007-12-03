package org.phylowidget.net;

import org.andrewberman.unsorted.DelayedAction;
import org.phylowidget.tree.TreeClipboard;

public class PWClipUpdater extends DelayedAction
{
	String parseMe;
	public void triggerUpdate(String s)
	{
		parseMe = s;
		trigger(200);
	}
	
	public void run()
	{
		TreeClipboard.instance().setClip(parseMe);
	}
}
