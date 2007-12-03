package org.phylowidget.net;

import org.andrewberman.unsorted.DelayedAction;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.TreeIO;
import org.phylowidget.ui.PhyloTree;

/**
 * A utility class to update PhyloWidget's internal tree when triggered via
 * JavaScript. For the Java->JavaScript communication, see the JSTreeUpdater.
 * 
 * @author Greg
 * 
 */
public class PWTreeUpdater extends DelayedAction
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
		TreeIO.setOldTree(PhyloWidget.trees.getTree());
		PhyloWidget.trees.setTree(TreeIO.parseNewickString(t, parseMe));
	}
}
