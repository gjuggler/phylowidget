package org.phylowidget.ui;

import org.jgrapht.traverse.BreadthFirstIterator;
import org.phylowidget.tree.RootedTree;

public class PhyloTree extends RootedTree
{
	private static final long serialVersionUID = 1L;

	PhyloNode clipboardNode;
	
	public Object createNode(String label)
	{
		return new PhyloNode(label);
	}
	
	public void cut(PhyloNode cutMe)
	{
		clipboardNode = cutMe;
		setStateRecursive(cutMe,PhyloNode.CUT);
	}
	
	public void copy(PhyloNode copyMe)
	{
		clipboardNode = copyMe;
		setStateRecursive(copyMe,PhyloNode.COPY);
	}
	
	void setStateRecursive(PhyloNode base, int state)
	{
		BreadthFirstIterator bfi = new BreadthFirstIterator(this,base);
		while (bfi.hasNext())
		{
			PhyloNode n = (PhyloNode) bfi.next();
			n.setState(state);
		}
	}
}
