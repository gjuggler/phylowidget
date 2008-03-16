package org.phylowidget.tree;

import org.jgrapht.Graph;

public class SmartSubtree extends RootedTree
{
	private RootedTree fullTree;
	
	Object fakeRoot;
	
	public SmartSubtree(RootedTree t, Object newRootNode)
	{
		fakeRoot = newRootNode;
	}
}
