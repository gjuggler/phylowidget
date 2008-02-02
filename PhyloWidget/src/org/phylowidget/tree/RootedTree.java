/**************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PhyloWidget.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.phylowidget.tree;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.Graphs;
import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

public class RootedTree extends ListenableDirectedWeightedGraph
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This object helps cache the sets of predecessor and successor neighbors
	 * by listening to changes on this JGraphT object and updating as necessary.
	 * If you expect to be changing your graph lots and not calling the
	 * childrenOf() method very often, then you're best setting useNeighborIndex
	 * to "false" by overriding the setOptions() method and setting it false in
	 * there.
	 */
	DirectedNeighborIndex neighbors;

	/**
	 * The current root of this rooted tree.
	 */
	Object root;

	/**
	 * This Hashtable keeps track of the sort orders for each node, if they have
	 * been set to be different from the defalut sort order.
	 */
	public HashMap sorting;
	public static final Integer REVERSE = new Integer(1);
	public static final Integer FORWARD = new Integer(-1);
	public Comparator sorter = new EnclosedLeavesComparator(-1);
	public Comparator leafSorter = new DepthToRootComparator(1);
	
	public static final String INTERNAL_NODE_LABEL = "";

	/*
	 * ****** OPTIONS ******
	 */
	/**
	 * (only relevant for subclassers) If true, then this tree will keep an
	 * indexed structure of the neighbors of each node's neighbors.
	 */
	protected boolean useNeighborIndex;
	/**
	 * If true, then this RootedTree will ensure that no two vertices in the
	 * tree have the same string representation (i.e. the same String resulting
	 * from calling toString()).
	 */
	protected boolean enforceUniqueLabels;
	/**
	 * An object which will handle keeping our labels unique.
	 */
	private UniqueLabeler uniqueLabeler;

	public RootedTree()
	{
		// super(new SimpleDirectedWeightedGraph(DefaultWeightedEdge.class));
		super(DefaultWeightedEdge.class);
		setOptions();
		if (useNeighborIndex)
			createNeighborIndex();
		sorting = new HashMap();
	}

	void createNeighborIndex()
	{
		if (neighbors != null)
			removeGraphListener(neighbors);
		neighbors = new DirectedNeighborIndex(this);
		addGraphListener(neighbors);
	}

	/**
	 * Subclasses should override this method to set any of the "option-like"
	 * boolean variables provided, such as "useNeighborIndex".
	 */
	protected void setOptions()
	{
		useNeighborIndex = true;
		setEnforceUniqueLabels(true);
	}

	/**
	 * Returns whether this RootedTree enforces unique node labels or not.
	 */
	public boolean getEnforceUniqueLabels()
	{
		return enforceUniqueLabels;
	}

	/**
	 * 
	 * Set this RootedTree to either enforce or not enforce unique node labels.
	 * 
	 * @param enforceUniqueLabels
	 *            If true, then this RootedTree will ensure that node labels are
	 *            unique.
	 */
	public void setEnforceUniqueLabels(boolean enforceUniqueLabels)
	{
		this.enforceUniqueLabels = enforceUniqueLabels;
		if (enforceUniqueLabels)
		{
			uniqueLabeler = new UniqueLabeler();
			uniqueLabeler.resetVertexLabels(this);
		}
	}

	/**
	 * Checks the current Rooted Tree for existence of the label attached to the
	 * vertex, and if it already exists in the tree, returns a new unique label.
	 */

	public String getLabel(Labelable vertex)
	{
		return vertex.getLabel();
	}

	public void setLabel(Object vertex, String label)
	{
		if (enforceUniqueLabels)
		{
			uniqueLabeler.changeLabel(vertex, label);
		} else if (vertex instanceof Labelable)
		{
			Labelable v = (Labelable) vertex;
			v.setLabel(label);
		}
	}

	public Object getVertexForLabel(String label)
	{
		if (enforceUniqueLabels)
		{
			return uniqueLabeler.getNodeForLabel(label);
		} else
			return null;
	}

	public double getBranchLength(Object vertex)
	{
		Object parent = getParentOf(vertex);
		return getEdgeWeight(getEdge(parent, vertex));
	}

	public void setBranchLength(Object vertex, double weight)
	{
		Object parent = getParentOf(vertex);
		if (parent == null)
			return;
		Object edge = getEdge(parent, vertex);
		setEdgeWeight(edge, weight);
	}

	/**
	 * A "factory" method for creating node objects. Currently it just returns
	 * the string given as input, but it could be extended by a subclass to
	 * create a node object that holds more detailed information. These objects
	 * will be the "vertex" objects inserted into the JGraphT structure.
	 * <p>
	 * It's probably useful to note that inserting larger objects into the
	 * JGraphT won't slow it down or make it take anymore memory; it simply
	 * stores a hashtable of references back to these vertex objects, and does
	 * all the internal stuff using its own internal data structures.
	 * 
	 * @param label
	 * @return
	 */
	public Object createVertex(Object o)
	{
		DefaultVertex v = new DefaultVertex(o);
		return v;
	}

	public boolean addVertex(Object o)
	{
		if (enforceUniqueLabels)
			uniqueLabeler.addLabel(o);
		return super.addVertex(o);
	}

	public boolean removeVertex(Object o)
	{
		if (enforceUniqueLabels)
			uniqueLabeler.removeLabel(o);
		return super.removeVertex(o);
	}

	public Object createAndAddVertex(Object o)
	{
		Object newV = createVertex(o);
		addVertex(newV);
		return newV;
	}

	public boolean isLeaf(Object vertex)
	{
		return (outDegreeOf(vertex) == 0);
	}

	public List getChildrenOf(Object vertex)
	{
		List l;
		if (useNeighborIndex)
		{
			l = new ArrayList();
			l.addAll(neighbors.successorsOf(vertex));
		} else
			l = Graphs.successorListOf(this, vertex);
		return sortChildrenList(vertex,l,sorter);
	}

	List sortChildrenList(Object vertex, List l, Comparator sorter)
	{
		// Sort the resulting list.
		Collections.sort(l, sorter);
		if (sorting.containsKey(vertex))
		{
			Integer i = (Integer) sorting.get(vertex);
			if (i == REVERSE)
				Collections.reverse(l);
		}
		return l;
	}
	
	public Object getFirstLeaf(Object vertex)
	{
		Object cur = vertex;
		while (!isLeaf(cur))
		{
			cur = getFirstChild(cur);
		}
		return cur;
	}
	
	public Object getFirstChild(Object vertex)
	{
		return getChildrenOf(vertex).get(0);
	}

	public Object getLastChild(Object vertex)
	{
		List l = getChildrenOf(vertex);
		return l.get(l.size());
	}

	public Object getLastLeaf(Object vertex)
	{
		Object cur = vertex;
		while (!isLeaf(cur))
		{
			cur = getLastChild(cur);
		}
		return cur;
	}
	
	public Object getParentOf(Object child)
	{
		// Special case: this vertex has no parents, i.e. it is the root.
		if (inDegreeOf(child) == 0)
			return null;
		if (!useNeighborIndex)
		{
			Set edgeSet = incomingEdgesOf(child);
			Iterator i = edgeSet.iterator();
			Object e = i.next();
			return getEdgeSource(e);
		} else
		{
			Set parentSet = neighbors.predecessorsOf(child);
			return parentSet.iterator().next();
		}
	}

	public int getMaxDepthToLeaf(Object vertex)
	{
		int maxDepth = 0;
		BreadthFirstIterator bfi = new BreadthFirstIterator(this, vertex);
		while (bfi.hasNext())
		{
			Object o = bfi.next();
			if (isLeaf(o))
			{
				int curDepth = getDepthToVertex(o, vertex);
				if (curDepth > maxDepth)
					maxDepth = curDepth;
			}
		}
		return maxDepth;
	}

	public int getDepthToRoot(Object vertex)
	{
		return getDepthToVertex(vertex,getRoot());
	}
	
	int getDepthToVertex(Object vertex, Object target)
	{
		int depth = 0;
		while (vertex != target)
		{
			depth += 1;
			vertex = getParentOf(vertex);
			if (vertex == root)
				return depth;
		}
		return depth;
	}

	public double getMaxHeightToLeaf(Object vertex)
	{
		double maxHeight = 0;
		BreadthFirstIterator bfi = new BreadthFirstIterator(this, vertex);
		while (bfi.hasNext())
		{
			Object o = bfi.next();
			if (isLeaf(o))
			{
				double curHeight = getHeightToRoot(o);
				if (curHeight > maxHeight)
					maxHeight = curHeight;
			}
		}
		return maxHeight;
	}

	public double getHeightToRoot(Object vertex)
	{
		// System.out.println("htr!");
		double height = 0;
		while (vertex != root)
		{
			Object parent = getParentOf(vertex);
			Object edge = getEdge(parent, vertex);
			height += getEdgeWeight(edge);
			// System.out.println("v:"+vertex+" p:"+parent+"
			// w:"+getEdgeWeight(edge));
			vertex = parent;
		}
		return height;
	}

	public int getNumEnclosedLeaves(Object vertex)
	{
		return getEnclosedLeaves(vertex).size();
	}

	public int getMaxChildEnclosed(Object vertex)
	{
		if (isLeaf(vertex))
			return 0;
		List children = getChildrenOf(vertex);
		int max = 0;
		Object maxChild = null;
		for (int i = 0; i < children.size(); i++)
		{
			int cur = getNumEnclosedLeaves(children.get(i));
			if (cur > max)
			{
				max = cur;
			}
		}
		return max;
	}

	/**
	 * A method for retrieving all nodes below a given vertex in a tree. The
	 * "leaves" and "nodes" List objects (which must have already been created
	 * by the caller) will be filled with all the appropriate nodes.
	 * 
	 * @param vertex
	 * @param leaves
	 * @param nodes
	 */
	public void getAll(Object vertex, List leaves, List nodes)
	{
		if (vertex == null)
			return;
		Stack s = new Stack();
		s.push(vertex);
		while (!s.isEmpty())
		{
			Object v = s.pop();
			if (isLeaf(v))
			{
				if (leaves != null)
					leaves.add(v);
			} else
			{
				List children = getChildrenOf(v);
				s.addAll(children);
			}
			if (nodes != null)
				nodes.add(v);
		}
	}

	private List getEnclosedVertices(Object vertex)
	{
		ArrayList l = new ArrayList();
		BreadthFirstIterator bfi = new BreadthFirstIterator(this, vertex);
		while (bfi.hasNext())
		{
			l.add(bfi.next());
		}
		return l;
	}

	private List getEnclosedLeaves(Object vertex)
	{
		ArrayList l = new ArrayList();
		BreadthFirstIterator bfi = new BreadthFirstIterator(this, vertex);
		while (bfi.hasNext())
		{
			Object o = bfi.next();
			if (isLeaf(o))
				l.add(o);
		}
		return l;
	}

	public synchronized void deleteSubtree(Object vertex)
	{
		if (vertex == getRoot())
		{
			Object newRoot = createAndAddVertex("[new root]");
			setRoot(newRoot);
		}
		List nodes = getEnclosedVertices(vertex);
		for (int i = 0; i < nodes.size(); i++)
		{
//			deleteNode(nodes.get(i));
			removeVertex(nodes.get(i));
		}
	}

	public synchronized void deleteNode(Object vertex)
	{
		if (getParentOf(vertex) != null)
		{
			Object parent = getParentOf(vertex);
			double weightToParent = getEdgeWeight(getEdge(parent, vertex));
			List children = getChildrenOf(vertex);
			for (int i = 0; i < children.size(); i++)
			{
				Object child = children.get(i);
				double weight = getEdgeWeight(getEdge(vertex, child));
				Object edge = addEdge(parent, child);
				setEdgeWeight(edge, weightToParent + weight);
			}
			removeVertex(vertex);
		} else
		{
			// Do nothing -- how would I delete the root node?
		}
	}

	public RootedTree cloneSubtree(Object vertex)
	{
		RootedTree newTree;
		try
		{
			Constructor c = this.getClass().getConstructor(new Class[] {});
			newTree = (RootedTree) c.newInstance(new Object[] {});
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		/*
		 * The basic idea here is to go through the current subtree, and
		 * basically mirror the same actions in the new tree, creating vertices
		 * and edges as needed.
		 */
		Stack myStack = new Stack(); // One for me...
		Stack newStack = new Stack(); // One for you.
		// Initialize the traversal with the designated start vertex.
		myStack.push(vertex);
		Object newRoot = newTree.createAndAddVertex(vertex);
		newTree.setRoot(newRoot);
		newStack.push(newRoot);
		while (!myStack.isEmpty())
		{
			Object parent = myStack.pop();
			Object newParent = newStack.pop();
			List list = getChildrenOf(parent);
			for (int i = 0; i < list.size(); i++)
			{
				Object thisChild = list.get(i);
				myStack.push(thisChild);
				double thisWeight = getEdgeWeight(getEdge(parent, thisChild));
				// Now do the same (while creating stuff) for the cloned tree.
				Object newChild = newTree.createAndAddVertex(thisChild);
				newStack.push(newChild);
				Object e = newTree.addEdge(newParent, newChild);
				newTree.setEdgeWeight(e, thisWeight);
			}
		}
		return newTree;
	}

	/**
	 * Adds a new sister node to this vertex. In order to do this and maintain a
	 * relatively bifurcated tree, this method creates a new node <em>above</em>
	 * the given vertex, and adds a new sister node to the newly created
	 * "parent" node.
	 * 
	 * @param v
	 */
	public void addSisterNode(Object v, Object newSister)
	{
		Object curParent = getParentOf(v);
		Object newParent = createAndAddVertex(INTERNAL_NODE_LABEL);
		// Object newSister = createAndAddVertex("");
		if (v == getRoot())
		{
			setRoot(newParent);
		} else
		{
			insertNodeBetween(curParent, v, newParent);
		}
		addEdge(newParent, newSister);
		addEdge(newParent, v);
		// return newSister;
	}

	/**
	 * Adds an "anonymous" child node below the given vertex.
	 * 
	 * @param v
	 */
	public void addChildNode(Object v)
	{
		Object newNode = createAndAddVertex("");
		addEdge(v, newNode);
	}

	/**
	 * Inserts vertex insertMe at the midpoint of the edge between a and b. Note
	 * that insertMe must have already been created using the
	 * createVertex(Object) method of this RootedTree.
	 * 
	 * @param a
	 *            the parent node (edge source)
	 * @param b
	 *            the child node (edge target)
	 * @param insertMe
	 */
	protected void insertNodeBetween(Object a, Object b, Object insertMe)
	{
		Object e = getEdge(a, b);
		double weight = getEdgeWeight(e);
		Object newToB = addEdge(insertMe, b);
		setEdgeWeight(newToB, weight / 2);
		Object aToNew = addEdge(a, insertMe); //
		setEdgeWeight(aToNew, weight / 2);
		removeEdge(a, b);
	}

	/**
	 * Reroots this RootedTree using the midpoint method. The pivot edge is
	 * taken to be the edge between the given vertex (called "pivot") and its
	 * parent. See the inline comments for more information on the algorithm and
	 * steps involved.
	 * 
	 * @param pivot
	 */
	public synchronized void reroot(Object pivot)
	{
		// System.out.println("Rerooting tree...");
		// System.out.println("Step 1...");
		// System.out.println(this);

		if (pivot == root || getParentOf(pivot) == root)
			return;
		/**
		 * Ok, let's do this.
		 * 
		 * Here's the plan:
		 * <p>
		 * 1. Create the new root using the midpoint method. Take the edge
		 * between pivot and parentOf(pivot) and create a new vertex in its
		 * midpoint.
		 * <p>
		 * 2. Get rid of the current root, by removing it and re-attaching its
		 * children to the child in the direction of the new root. Sum the edge
		 * lengths to make it all stay correct.
		 * <p>
		 * 3. Re-orient all the edges in the tree to point away from the root.
		 */
		Object e = null, v = null;
		// STEP 1: Remove the current root and reconnect the top-level vertices.
		//
		// Find the child that "points" to the new root from the current root.
		v = pivot;
		while (getParentOf(v) != null)
		{
			if (getParentOf(v) == root)
				break;
			v = getParentOf(v);
		}
		// V should now be the child that leads from root to newRoot.
		double lengthB = getEdgeWeight(getEdge(root, v));
		List l = getChildrenOf(root);
		for (int i = 0; i < l.size(); i++)
		{
			Object child = l.get(i);
			if (child == v) // If this is the child that "points", continue.
				continue;
			// Get the edge length between root and this child.
			e = getEdge(root, child);
			double lengthA = getEdgeWeight(e);
			// Create the new edge between v and child.
			removeEdge(v, child);
			e = addEdge(v, child);
			setEdgeWeight(e, lengthA + lengthB);
		}
		removeVertex(root);
		// Remove the root vertex and all its touching edges.
		// System.out.println("Step 2...");
		// System.out.println(this);
		// STEP 2: Create the new root.
		//
		// Add the new root vertex.
		Object newRoot = createAndAddVertex("");
		// Capture the length of the edge above the pivot vertex.
		insertNodeBetween(getParentOf(pivot), pivot, newRoot);
		root = newRoot;

		// System.out.println("Step 3...");
		// System.out.println(this);
		// STEP 3: Re-orient all edges, branching out from the root edge.
		//
		// Iterate over an undirected version of this graph, so we can "go
		// against the grain" when we need to.
		BreadthFirstIterator bfi = new BreadthFirstIterator(Graphs
				.undirectedGraph(this), root);
		// Toss all the nodes into a linked list.
		LinkedList linked = new LinkedList();
		while (bfi.hasNext())
		{
			linked.addLast(bfi.next());
		}
		// Now, go through the list of nodes, re-orienting edges as needed.
		// Set the root node as the first parent in our algorithm.
		HashMap seen = new HashMap();
		Integer stupidInt = new Integer(1);
		while (!linked.isEmpty())
		{
			Object curNode = linked.removeFirst();
			/*
			 * Here we grab all the edges touching curNode and iterate through
			 * them. We can't just use the Set's iterator because we'll be
			 * modifying the graph structure as we go -- this causes bad stuff
			 * to happen in the graph-backed iterator.
			 */
			List list = Graphs.neighborListOf(this, curNode);
			for (int i = 0; i < list.size(); i++)
			{
				Object child = list.get(i);
				if (seen.containsKey(child)) // Skip if node already seen.
					continue;
				// Capture the edge length.
				double edgeWeight = 1;
				if (containsEdge(curNode, child))
					edgeWeight = getEdgeWeight(getEdge(curNode, child));
				else if (containsEdge(child, curNode))
					edgeWeight = getEdgeWeight(getEdge(child, curNode));
				// Delete the edges between these nodes.
				removeEdge(curNode, child);
				removeEdge(child, curNode);
				// Insert a correctly-oriented edge.
				e = addEdge(curNode, child);
				setEdgeWeight(e, edgeWeight);
			}
			seen.put(curNode, stupidInt);
		}
	}

	/**
	 * Flips the children directly below the given vertex.
	 * 
	 * A note on implementation: the RootedTree object contains a HashMap that
	 * keeps track of a sort value for each node. This can be either FORWARD or
	 * REVERSE, where FORWARD is the default sort. Note that this means we
	 * cannot arbitrarily shuffle the children of a vertex if there are more
	 * than 3. However, for most phylogenetic purposes, this is a rare
	 * occurrence and is not worth the extra effort.
	 * 
	 * @param parent
	 *            the vertex whose direct children to flip.
	 */
	public void flipChildren(Object parent)
	{
		// If we've already stored a sorting value, then toggle it.
		if (sorting.containsKey(parent))
		{
			Object o = sorting.get(parent);
			if (o == REVERSE)
				sorting.put(parent, FORWARD);
			else
				sorting.put(parent, REVERSE);
		} else
		{
			// Otherwise, this vertex is already implicitly forward sorted.
			// Switch it to reverse.
			sorting.put(parent, REVERSE);
		}
	}

	/**
	 * Reverses the entire subtree below a given vertex.
	 * 
	 * @param vertex
	 */
	public void reverseSubtree(Object vertex)
	{
		BreadthFirstIterator bfi = new BreadthFirstIterator(this, vertex);
		while (bfi.hasNext())
		{
			Object o = bfi.next();
			flipChildren(o);
		}
	}

	/**
	 * Ladderizes the tree below the given vertex. Ladderizing is equivalent to
	 * "re-sorting" the tree, where the vertices below a given vertex are sorted
	 * by the their number of total enclosed leaf vertices.
	 * 
	 * @param vertex
	 */
	public void ladderizeSubtree(Object vertex)
	{
		BreadthFirstIterator bfi = new BreadthFirstIterator(this, vertex);
		while (bfi.hasNext())
		{
			Object o = bfi.next();
			sorting.put(o, FORWARD);
		}
	}

	/**
	 * Removes "elbowed" nodes from the subtree below the given vertex. An
	 * elbowed node is defined as a node that has a single parent and a single
	 * child.
	 * 
	 * @param vertex
	 *            the node at which to begin culling
	 */
	public void cullElbowsBelow(Object vertex)
	{
		ArrayList list = new ArrayList();
		getAll(vertex, null, list);
		for (int i = 0; i < list.size(); i++)
		{
			Object o = list.get(i);
			if (getParentOf(o) != null && getChildrenOf(o).size() == 1)
			{
				Object parent = getParentOf(o);
				Object child = getChildrenOf(o).get(0);
				double edgeWeight = getEdgeWeight(getEdge(parent, o));
				edgeWeight += getEdgeWeight(getEdge(o, child));
				removeVertex(o);
				addEdge(parent, child);
				setEdgeWeight(getEdge(parent, child), edgeWeight);
			}
		}
	}

	/**
	 * @return the current root of this RootedTree.
	 */
	public Object getRoot()
	{
		return root;
	}

	/**
	 * Sets the root vertex of this tree. Note that this DOES NOT reroot the
	 * tree using the midpoint algorithm. For that operation, see
	 * reroot(object).
	 * 
	 * @param newRoot
	 */
	public void setRoot(Object newRoot)
	{
		if (!containsVertex(newRoot))
		{
			addVertex(newRoot);
		}
		root = newRoot;
	}

	class DepthToRootComparator implements Comparator
	{
		int dir;
		
		public DepthToRootComparator(int dir)
		{
			this.dir = dir;
		}
		
		public int compare(Object o1, Object o2)
		{
			int a = getDepthToRoot(o1);
			int b = getDepthToRoot(o2);
			
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
	}
	
	class EnclosedLeavesComparator implements Comparator
	{
		int dir;

		public EnclosedLeavesComparator(int dir)
		{
			this.dir = dir;
		}

		public int compare(Object o1, Object o2)
		{
			int a = getNumEnclosedLeaves(o1);
			int b = getNumEnclosedLeaves(o2);

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
