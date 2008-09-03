/*******************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * PhyloWidget. If not, see <http://www.gnu.org/licenses/>.
 */
package org.phylowidget.tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

public class CachedRootedTree<V extends CachedVertex,E extends DefaultWeightedEdge> extends RootedTree<V,E>
{
	private static final long serialVersionUID = 1L;
	protected boolean inSync;

	public Comparator enclSorter = new CachedEnclosedLeavesComparator(-1);

	public CachedRootedTree(Class<? extends E> edgeClass)
	{
		super(edgeClass);
	}

	public V createVertex()
	{
		CachedVertex cv = new CachedVertex();
		return (V) cv;
	}

	private boolean inSync()
	{
		return inSync;
	}

	/**
	 * Synchronizes each vertex's cached values with the current structure of
	 * the tree. If the tree is already updated (i.e. modID == calcID), then
	 * nothing happens.
	 */
	public void sync()
	{
		if (holdCalculations)
		{
			inSync = false;
			return;
		}
		if (inSync() || root == null)
			return;
		calculateStuff();
		inSync = true;
	}

	boolean holdCalculations;

	public void setHoldCalculations(boolean holdMe)
	{
		holdCalculations = holdMe;
	}

	private synchronized List<V> getChildrenOfNoSort(V vertex)
	{
		List<V> l;
		if (useNeighborIndex)
		{
			l = new ArrayList<V>();
			l.addAll(neighbors.successorsOf(vertex));
		} else
			l = Graphs.successorListOf(this, vertex);
		return l;
	}

	protected void calculateStuff()
	{
		if (holdCalculations)
			return;
		/*
		 * Everything should be able to be cached by first sweeping from root to
		 * leaves, then from leaves to root.
		 */

		/*
		 * STEP 1: ROOT TO LEAVES.
		 */
		// Roll our own breadth-first iteration.
		Stack<V> s = new Stack<V>();
		s.add(root);
		while (!s.isEmpty())
		{
			V v = s.pop();
			if (getParentOf(v) == null)
			{
				// Set the root-level cached values.
				v.setDepthToRoot(0);
				v.setHeightToRoot(0);
				v.setParent(null);
			} else
			{
				// Normal iteration.
				V p = getParentOf(v);
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
		LinkedList<V> traversal = new LinkedList<V>();
		/*
		 * Destination linkedlist for the vertices. Root is first, leaves are
		 * last.
		 */
		LinkedList<V> dest = new LinkedList<V>();
		traversal.add(getRoot());
		while (!traversal.isEmpty())
		{
			V o = traversal.removeFirst();
			dest.addLast(o);
			List<V> children = getChildrenOfNoSort(o);
			for (int i = 0; i < children.size(); i++)
			{
				traversal.addLast(children.get(i));
			}
		}

		while (!dest.isEmpty())
		{
			V cv = dest.removeLast();
			if (super.isLeaf(cv))
			{
				// If this vertex is a leaf, set the base cached values.
				cv.setNumEnclosed(0);
				cv.setNumLeaves(1);
				cv.setMaxDepthToLeaf(0);
				cv.setMaxHeightToLeaf(0);
			} else
			{
				// Regular iteration, building up from the children's cached
				// values.
				int numEnc = 0;
				int numLeaves = 0;
				int maxDepth = 0;
				int minChildEnc = Integer.MAX_VALUE;
				int maxChildEnc = 0;
				double maxHeight = 0;
				List<V> children = getChildrenOfNoSort(cv);
				sortChildrenList(cv, children, enclSorter);
				for (int i = 0; i < children.size(); i++)
				{
					V child = children.get(i);
					if (child.getNumEnclosed() >= maxChildEnc)
					{
						maxChildEnc = child.getNumEnclosed();
						// lastChild = child;
					}
					if (child.getNumEnclosed() <= minChildEnc)
					{
						minChildEnc = child.getNumEnclosed();
						// firstChild = child;
					}
					numEnc += child.getNumEnclosed() + 1;
					numLeaves += child.getNumLeaves();
					double ew = getEdgeWeight(getEdge(cv, child));
					if (child.getMaxHeightToLeaf() + ew > maxHeight)
						maxHeight = ew + child.getMaxHeightToLeaf();
					if (child.getMaxDepthToLeaf() + 1 > maxDepth)
						maxDepth = child.getMaxDepthToLeaf() + 1;
				}
				cv.setMaxChildEnclosed(maxChildEnc);
				cv.setNumEnclosed(numEnc);
				cv.setNumLeaves(numLeaves);
				cv.setMaxDepthToLeaf(maxDepth);
				cv.setMaxHeightToLeaf(maxHeight);
				/*
				 * Cache the first and last child.
				 */
				cv.setFirstChild(children.get(0));
				cv.setLastChild(children.get(children.size() - 1));
			}
		}
	}

	@Override
	public int getMaxChildEnclosed(V vertex)
	{
		CachedVertex c = (CachedVertex) vertex;
		return c.getMaxChildEnclosed();
	}

	@Override
	public V getFirstChild(V vertex)
	{
		sync();
		V c = vertex;
		return (V)c.getFirstChild();
	}

	@Override
	public V getLastChild(V vertex)
	{
		sync();
		CachedVertex c = (CachedVertex) vertex;
		return (V)c.getLastChild();
	}

	@Override
	public int getDepthToRoot(V vertex)
	{
		sync();
		if (inSync())
		{
			CachedVertex cv = (CachedVertex) vertex;
			return cv.getDepthToRoot();
		} else
			return super.getDepthToRoot(vertex);
	}

	public double getHeightToRoot(V vertex)
	{
		sync();
		if (inSync())
		{
			CachedVertex cv = (CachedVertex) vertex;
			return cv.getHeightToRoot();
		} else
			return super.getHeightToRoot(vertex);
	}

	public int getMaxDepthToLeaf(V vertex)
	{
		sync();
		if (inSync())
		{
			CachedVertex cv = (CachedVertex) vertex;
			return cv.getMaxDepthToLeaf();
		} else
			return super.getMaxDepthToLeaf(vertex);
	}

	public double getMaxHeightToLeaf(V vertex)
	{
		sync();
		if (inSync())
		{
			CachedVertex cv = (CachedVertex) vertex;
			return cv.getMaxHeightToLeaf();
		} else
			return super.getMaxHeightToLeaf(vertex);
	}

	public synchronized int getNumEnclosedLeaves(V vertex)
	{
		sync();
		if (inSync())
		{
			CachedVertex cv = (CachedVertex) vertex;
			return cv.getNumLeaves();
		} else
			return super.getNumEnclosedLeaves(vertex);
	}

	@Override
	public synchronized boolean isLeaf(V vertex)
	{
		sync();
		if (inSync())
		{
			CachedVertex cv = (CachedVertex) vertex;
			return cv.getNumEnclosed() == 0;
		} else
			return super.isLeaf(vertex);
	}

	protected void fireEdgeAdded(E arg0)
	{
		modPlus();
		super.fireEdgeAdded(arg0);
	}

	protected void fireEdgeRemoved(E arg0)
	{
		modPlus();
		super.fireEdgeRemoved(arg0);
	}

	protected void fireVertexAdded(V arg0)
	{
		modPlus();
		super.fireVertexAdded(arg0);
	}

	protected void fireVertexRemoved(V arg0)
	{
		modPlus();
		super.fireVertexRemoved(arg0);
	}

	public void setBranchLength(V vertex, double weight)
	{
		modPlus();
		super.setBranchLength(vertex, weight);
	}

//	@Override
//	public void reverseSubtree(V vertex)
//	{
////		setHoldCalculations(true);
//		super.reverseSubtree(vertex);
////		setHoldCalculations(false);
//	}
	
	public void modPlus()
	{
		super.modPlus();
		inSync = false;
	}

	@Override
	public void alignLeaves()
	{
		super.alignLeaves();
	}
	
	@Override
	public void setBranchLengths(Map<V, Double> branchLengths)
	{
		setHoldCalculations(true);
		super.setBranchLengths(branchLengths);
		setHoldCalculations(false);
	}
	
	class CachedEnclosedLeavesComparator implements Comparator
	{
		int dir;

		public CachedEnclosedLeavesComparator(int dir)
		{
			this.dir = dir;
		}

		public int compare(Object o1, Object o2)
		{
			V ca = (V) o1;
			V cb = (V) o2;
			int a = ca.getNumEnclosed();
			int b = cb.getNumEnclosed();

			if (dir == 1)
			{
				if (a > b)
					return 1;
				else if (a < b)
					return -1;
				else
					return 0;
			} else
			{
				if (a > b)
					return -1;
				else if (a < b)
					return 1;
				else
					return 0;
			}
		}

		public boolean equals(Object o1, Object o2)
		{
			return (compare(o1, o1) == 0);
		}

	}
}
