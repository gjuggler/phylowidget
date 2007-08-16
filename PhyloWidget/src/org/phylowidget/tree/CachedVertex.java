package org.phylowidget.tree;

public class CachedVertex extends DefaultVertex
{

	private Object parent;
	
	private int numLeaves;
	private int numDescendants;
	
	private int depthToRoot;
	private int maxDepthToLeaf;
	private float heightToRoot;
	
	public CachedVertex(Object o)
	{
		super(o);
	}

}
