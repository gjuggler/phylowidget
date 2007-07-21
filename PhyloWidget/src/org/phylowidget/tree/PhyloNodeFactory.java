package org.phylowidget.tree;

public class PhyloNodeFactory extends NodeFactory
{
	private static PhyloNodeFactory instance;
	
	public static PhyloNodeFactory instance()
	{
		if (instance == null)
			instance = new PhyloNodeFactory();
		return instance;
	}
	
	public TreeNode createNode()
	{
		System.out.println("New phylonode!");
		return new PhyloNode();
	}
	
}
