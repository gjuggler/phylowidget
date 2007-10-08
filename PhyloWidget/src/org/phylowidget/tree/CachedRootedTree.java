package org.phylowidget.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import org.jgrapht.Graphs;
import org.jgrapht.traverse.BreadthFirstIterator;

import sun.misc.Queue;

public class CachedRootedTree extends RootedTree
{
	private static final long serialVersionUID = 1L;

	/**
	 * A simple counter that is ticked every time the structure of the tree is
	 * altered.
	 */
	private int modID = 1;
	/**
	 * Another simple counter that is set to modID whenever the tree is
	 * re-calculated.
	 */
	private int calcID;

	public CachedRootedTree()
	{
		super();
	}

	public Object createVertex(Object o)
	{
		return new CachedVertex(o);
	}

	/**
	 * Synchronizes each vertex's cached values with the current structure of
	 * the tree. If the tree is already updated (i.e. modID == calcID), then
	 * nothing happens.
	 */
	synchronized private void sync()
	{
		if (modID == calcID || root == null)
			return;
//		System.out.println("Calculating stuff...");

		calcID = modID; // Do this before calculating to avoid stack overflow.
		calculateStuff();
	}

	private List getChildrenOfNoSort(Object vertex)
	{
		List l;
		if (useNeighborIndex)
		{
			l = new ArrayList();
			l.addAll(neighbors.successorsOf(vertex));
		} else
			l = Graphs.successorListOf(this, vertex);
		return l;
	}
	
	private void calculateStuff()
	{
		/*
		 * Everything should be able to be cached by first sweeping from root to
		 * leaves, then from leaves to root.
		 */

		/*
		 * STEP 1: ROOT TO LEAVES.
		 */
		// Roll our own breadth-first iteration.
		Stack s = new Stack();
		s.add(root);
		while (!s.isEmpty())
		{
			CachedVertex v = (CachedVertex) s.pop();
			if (getParentOf(v) == null)
			{
				// Set the root-level cached values.
				v.setDepthToRoot(0);
				v.setHeightToRoot(0);
				v.setParent(null);
			} else
			{
				// Normal recursion.
				CachedVertex p = (CachedVertex) getParentOf(v);
				v.setDepthToRoot(p.getDepthToRoot() + 1);
				double ew = getEdgeWeight(getEdge(p, v));
				v.setBranchLength(ew);
				v.setHeightToRoot(p.getHeightToRoot() + ew);
				v.setParent(p);
			}
			// Add this vertex's children to the stack.
			s.addAll(getChildrenOfNoSort(v));
		}

		/*
		 * STEP 2: LEAVES TO ROOT.
		 */
		// Load the vertices breadth-first into a linked list.
		LinkedList traversal = new LinkedList();
		/*
		 * Destination linkedlist for the vertices. Root is first, leaves are last.
		 */
		LinkedList dest = new LinkedList();
		traversal.add(getRoot());
		while (!traversal.isEmpty())
		{
			Object o = traversal.removeFirst();
			dest.addLast(o);
			List children = getChildrenOfNoSort(o);
			for (int i=0; i < children.size(); i++)
			{
				traversal.addLast(children.get(i));
			}
		}
		
		// A placeholder object for our hashtable.
		while (!dest.isEmpty())
		{
			CachedVertex cv = (CachedVertex) dest.removeLast();
			if (isLeaf(cv))
			{
				// If this vertex is a leaf, set the base cached values.
				cv.setNumEnclosed(1);
				cv.setNumLeaves(1);
				cv.setMaxDepthToLeaf(0);
				cv.setMaxHeightToLeaf(0);
			} else
			{
				// Regular recursion, building up from the children's cached
				// values.
				int numEnc = 0;
				int numLeaves = 0;
				int maxDepth = 0;
				double maxHeight = 0;
				List children = getChildrenOfNoSort(cv); // Gather our children.
				for (int i = 0; i < children.size(); i++)
				{
					CachedVertex child = (CachedVertex) children.get(i);
					numEnc += child.getNumEnclosed() + 1;
					numLeaves += child.getNumLeaves();
					double ew = getEdgeWeight(getEdge(cv, child));
					if (child.getMaxHeightToLeaf() + ew > maxHeight)
						maxHeight = ew + child.getMaxHeightToLeaf();
					if (child.getMaxDepthToLeaf() + 1 > maxDepth)
						maxDepth = child.getMaxDepthToLeaf() + 1;
				}
				cv.setNumEnclosed(numEnc);
				cv.setNumLeaves(numLeaves);
				cv.setMaxDepthToLeaf(maxDepth);
				cv.setMaxHeightToLeaf(maxHeight);
			}
		}
	}
	
	public double getHeightToRoot(Object vertex)
	{
		sync();
		CachedVertex cv = (CachedVertex) vertex;
		return cv.getHeightToRoot();
	}

	public int getMaxDepthToLeaf(Object vertex)
	{
		sync();
		CachedVertex cv = (CachedVertex) vertex;
		return cv.getMaxDepthToLeaf();
	}

	public double getMaxHeightToLeaf(Object vertex)
	{
		sync();
		CachedVertex cv = (CachedVertex) vertex;
		return cv.getMaxHeightToLeaf();
	}

	public int getNumEnclosedLeaves(Object vertex)
	{
		sync();
		CachedVertex cv = (CachedVertex) vertex;
		return cv.getNumLeaves();
	}

	protected void fireEdgeAdded(Object arg0)
	{
		modPlus();
		super.fireEdgeAdded(arg0);
	}

	protected void fireEdgeRemoved(Object arg0)
	{
		modPlus();
		super.fireEdgeRemoved(arg0);
	}

	protected void fireVertexAdded(Object arg0)
	{
		modPlus();
		super.fireVertexAdded(arg0);
	}

	protected void fireVertexRemoved(Object arg0)
	{
		modPlus();
		super.fireVertexRemoved(arg0);
	}
	
	public void setBranchLength(Object vertex, double weight)
	{
		modPlus();
		super.setBranchLength(vertex, weight);
	}
	
	private void modPlus()
	{
		modID++;
	}

}
