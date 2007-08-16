package org.phylowidget.ui;

import org.jgrapht.traverse.BreadthFirstIterator;
import org.phylowidget.tree.RootedTree;

public class PhyloTree extends RootedTree
{
	private static final long serialVersionUID = 1L;
	
	public PhyloTree(Object o)
	{
		super(o);
	}
	
	public PhyloTree()
	{
		super();
	}

	public Object createVertex(Object o)
	{
		return new PhyloNode(o);
	}
	
	
}
