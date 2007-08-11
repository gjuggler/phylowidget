package org.phylowidget.oldtree;

public class RenderNodeFactory extends NodeFactory
{
	private static RenderNodeFactory instance;
	
	public static RenderNodeFactory instance()
	{
		if (instance == null)
			instance = new RenderNodeFactory();
		return instance;
	}
	
	public TreeNode createNode()
	{
//		return new NewRenderNode();
		return null;
	}
	
}
