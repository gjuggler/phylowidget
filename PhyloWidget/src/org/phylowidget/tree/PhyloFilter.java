package org.phylowidget.tree;

import java.util.HashSet;

public class PhyloFilter implements TreeFilter<PhyloNode>
{
	
	HashSet<PhyloNode> nodesToFilter = new HashSet<PhyloNode>();
	
	public boolean shouldKeep(PhyloNode vertex)
	{
		return true;
	}
	
	public void filterNode(PhyloNode n)
	{
		nodesToFilter.add(n);
	}
	
	public void replaceNode(PhyloNode n)
	{
		nodesToFilter.remove(n);
	}

}
