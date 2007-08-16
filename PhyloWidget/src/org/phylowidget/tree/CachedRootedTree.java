package org.phylowidget.tree;

import java.util.Iterator;
import java.util.Set;

import org.jgrapht.traverse.BreadthFirstIterator;

public class CachedRootedTree extends RootedTree
{
	private static final long serialVersionUID = 1L;

	private int modCount;
	
	private int maxDepth;
	private float maxHeight;

	public Object createVertex(Object o)
	{
		return new CachedVertex(o);
	}
	
	synchronized void calculateStuff()
	{
		if (root == null)
			return;
		System.out.println("Calculating stuff...");
		/*
		 * Reset the root vertex calcs and the whole-tree calcs.
		 */
		// root.resetCalculations();
		// calcDepthAndHeight();
//		calcNumLeaves();
	}

//	 public void calcDepthAndHeight()
//	 {
//	 maxDepth = 0;
//	 maxHeight = 0;
//	
//	 BreadthFirstIterator bfi = new BreadthFirstIterator(this, root);
//	 bfi.setReuseEvents(true);
//	 while (bfi.hasNext())
//	 {
//	 PhyloNode n = (PhyloNode) bfi.next();
//	 // System.out.println(n);
//	 Set s = childrenSetOf(n);
//	 Iterator i = s.iterator();
//	 while (i.hasNext())
//	 {
//	 PhyloNode child = (PhyloNode) i.next();
//	 child.depthToRoot = n.depthToRoot + 1;
//	 child.heightToRoot = n.heightToRoot
//	 + (float) getEdgeWeight(getEdge(n, child));
//	 if (child.depthToRoot > maxDepth)
//	 maxDepth = child.depthToRoot;
//	 if (child.heightToRoot > maxHeight)
//	 maxHeight = child.heightToRoot;
//	 }
//	 }
//	 }

//	public void calcNumLeaves()
//	{
//		BreadthFirstIterator bfi = new BreadthFirstIterator(this, root);
//		bfi.setReuseEvents(true);
//		while (bfi.hasNext())
//		{
//			PhyloNode n = (PhyloNode) bfi.next();
//			if (isLeaf(n))
//			{
//				PhyloNode curNode = n;
//				curNode.numEnclosedLeaves = 0;
//				while (curNode != null)
//				{
//					curNode.numEnclosedLeaves++;
//					curNode = curNode.parent;
//				}
//			}
//		}
//	}

	
}
