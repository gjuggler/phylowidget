package org.phylowidget.tree;

import org.andrewberman.unsorted.DelayedAction;

public class CacheCalculator extends DelayedAction
{
	CachedRootedTree tree;

	public CacheCalculator(CachedRootedTree t)
	{
		super();
		tree = t;
	}

	protected void run()
	{
		new Thread()
		{
			public void run()
			{
				tree.calculateStuff();
				tree.inSync = true;
			}
		}.start();
	}

}
