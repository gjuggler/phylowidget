package org.phylowidget.oldtree;


public class NodeFactory
{

	public TreeNode createNode()
	{
		return new TreeNode();
	}
	
	public static final NodeFactory defaultFactory()
	{
		return new NodeFactory();
	}
	
}
