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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.WeakHashMap;

import org.jgrapht.Graphs;
import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;

public class RootedTree<V extends DefaultVertex, E extends DefaultWeightedEdge> extends
		ListenableDirectedWeightedGraph<V, E>
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
	DirectedNeighborIndex<V, E> neighbors;

	/**
	 * The current root of this rooted tree.
	 */
	V root;

	/**
	 * This Hashtable keeps track of the sort orders for each node, if they have
	 * been set to be different from the defalut sort order.
	 */
	public WeakHashMap<V, Integer> sorting;

	public static final Integer REVERSE = new Integer(1);
	public static final Integer FORWARD = new Integer(-1);
	public static final int REVERSE_I = 1;
	public static final int FORWARD_I = -1;

	public Comparator<V> sorter = new EnclosedLeavesComparator(-1);
	public Comparator<V> leafSorter = new DepthToRootComparator(1);

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

	private boolean isValid = true;

	public boolean isValid()
	{
		return isValid;
	}

	Class<? extends E> edgeClass;

	public RootedTree(Class<? extends E> edgeClass)
	{
		// super(new SimpleDirectedWeightedGraph(DefaultWeightedEdge.class));
		super(edgeClass);
		this.edgeClass = edgeClass;
		setOptions();
		if (useNeighborIndex)
			createNeighborIndex();
		sorting = new WeakHashMap<V, Integer>();
		isValid = true;
	}

	void createNeighborIndex()
	{
		if (neighbors != null)
			removeGraphListener(neighbors);
		neighbors = new DirectedNeighborIndex<V, E>(this);
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
		} else
		{
			uniqueLabeler.removeDuplicateTags(this);
		}
	}

	public boolean isLabelSignificant(String s)
	{
		if (enforceUniqueLabels)
		{
			return uniqueLabeler.isLabelSignificant(s);
		} else
			return s.length() > 0;
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

	public RootedTree<V,E> extractSubtree(V... vertices)
	{
		// TODO: Implement me!
		return null;
	}
	
	public int getNumLineagesAtHeight(double height)
	{
		List<V> nodes = getAllNodes(getRoot());
		int numLineages = 0;
		for (V v : nodes)
		{
			// Look for nodes whose parental branch covers the depth we're looking for.
			double curHeight = getHeightToRoot(v);
			double heightToParent = getBranchLength(v);
			if (curHeight > height && heightToParent < height)
				numLineages++;
		}
		return numLineages;
	}
	
	public List<V> getNodesAtHeight(double height)
	{
		List<V> nodes = getAllNodes(getRoot());
		ArrayList<V> keepers = new ArrayList<V>(nodes.size());
		for (V v : nodes)
		{
			// Look for nodes whose parental branch covers the depth we're looking for.
			double curHeight = getHeightToRoot(v);
			double heightToParent = getBranchLength(v);
			if (curHeight > height && heightToParent < height)
				keepers.add(v);
		}
		return keepers;
	}
	
	public List<V> getVerticesForLabels(Collection<String> labels)
	{
		ArrayList<V> verts = new ArrayList<V>(labels.size());
		for (String label : labels)
		{
			V v = getVertexForLabel(label);
			if (v != null)
				verts.add(v);
		}
		return verts;
	}
	
	public List<V> getAllLeaves(V vertex)
	{
		List<V> leaves = new ArrayList<V>();
		getAll(vertex,leaves,null);
		return leaves;
	}
	
	public List<V> getAllNodes(V vertex)
	{
		List<V> nodes = new ArrayList<V>();
		getAll(vertex,nodes,null);
		return nodes;
	}
	
	public List<String> getLabelsForVertices(Collection<V> verts)
	{
		ArrayList<String> labels = new ArrayList<String>(verts.size());
		for (V vert : verts)
		{
			labels.add(getLabel(vert));
		}
		return labels;
	}
	
	public V getVertexForLabel(String label)
	{
		if (enforceUniqueLabels)
		{
			V o = (V) uniqueLabeler.getNodeForLabel(label);
			return o;
		} else
		{
			DepthFirstIterator<V, E> it = new DepthFirstIterator<V, E>(this, getRoot());
			while (it.hasNext())
			{
				V vertex = it.next();
				if (getLabel(vertex).equals(label))
				{
					return vertex;
				}
			}
			return null;
		}
	}

	public double getBranchLength(V vertex)
	{
		V parent = getParentOf(vertex);
		return getEdgeWeight(getEdge(parent, vertex));
	}

	public void setBranchLength(V vertex, double length)
	{
		V parent = getParentOf(vertex);
		if (parent == null)
			return;
		E edge = getEdge(parent, vertex);
		setEdgeWeight(edge, length);
	}

	public void resetVertexLabels()
	{
		uniqueLabeler.resetVertexLabels(this);
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
	public V createVertex()
	{
		DefaultVertex v = new DefaultVertex();
		return (V) v;
	}

	public boolean addVertex(V o)
	{
		if (super.addVertex(o))
		{
			if (enforceUniqueLabels)
				uniqueLabeler.addLabel(o);
			return true;
		}
		return false;
	}

	public boolean removeVertex(V o)
	{
		if (super.removeVertex(o))
		{
			if (enforceUniqueLabels)
				uniqueLabeler.removeLabel(o);
			return true;
		}
		return false;
	}

	public V createAndAddVertex()
	{
		V newV = createVertex();
		addVertex(newV);
		return newV;
	}

	public boolean isLeaf(V vertex)
	{
		return (outDegreeOf(vertex) == 0);
	}

	public List<V> getChildrenOf(V vertex)
	{
		List<V> l;
		if (useNeighborIndex)
		{
			l = new ArrayList<V>();
			l.addAll(neighbors.successorsOf(vertex));
		} else
			l = Graphs.successorListOf(this, vertex);
		return sortChildrenList(vertex, l, sorter);
	}

	public void modPlus()
	{

	}

	List<V> sortChildrenList(V vertex, List<V> l, Comparator<V> sorter)
	{
		// Sort the resulting list.
		Collections.sort(l, sorter);
		//		if (sorting.containsKey(vertex))
		//		{
		//			if (sorting.get(vertex) == REVERSE)
		//				Collections.reverse(l);
		//		}
		if (getSorting(vertex) == REVERSE_I)
		{
			Collections.reverse(l);
		}
		return l;
	}

	public V getFirstLeaf(V vertex)
	{
		V cur = vertex;
		while (!isLeaf(cur))
		{
			cur = getFirstChild(cur);
		}
		return cur;
	}

	public V getFirstChild(V vertex)
	{
		return getChildrenOf(vertex).get(0);
	}

	public V getLastChild(V vertex)
	{
		List<V> l = getChildrenOf(vertex);
		return l.get(l.size());
	}

	public V getLastLeaf(V vertex)
	{
		V cur = vertex;
		while (!isLeaf(cur))
		{
			cur = getLastChild(cur);
		}
		return cur;
	}

	public V getParentOf(V child)
	{
		// Special case: this vertex has no parents, i.e. it is the root.
		if (inDegreeOf(child) == 0)
			return null;
		if (!useNeighborIndex)
		{
			Set<E> edgeSet = incomingEdgesOf(child);
			Iterator<E> i = edgeSet.iterator();
			E e = i.next();
			return getEdgeSource(e);
		} else
		{
			Set<V> parentSet = neighbors.predecessorsOf(child);
			return parentSet.iterator().next();
		}
	}

	public boolean isRoot(V vertex)
	{
		return getParentOf(vertex) == null;
	}

	public boolean isParentChild(V parent, V child)
	{
		V v = child;
		while (v != null)
		{
			if (v == parent)
				return true;
			v = getParentOf(v);
		}
		return false;
	}
	
	public int getMaxDepthToLeaf(V vertex)
	{
		int maxDepth = 0;
		BreadthFirstIterator<V, E> bfi = new BreadthFirstIterator<V, E>(this, vertex);
		while (bfi.hasNext())
		{
			V o = bfi.next();
			if (isLeaf(o))
			{
				int curDepth = getDepthToVertex(o, vertex);
				if (curDepth > maxDepth)
					maxDepth = curDepth;
			}
		}
		return maxDepth;
	}

	public int getDepthToRoot(V vertex)
	{
		if (vertex == getRoot())
			return 0;
		return getDepthToVertex(vertex, getRoot());
	}

	int getDepthToVertex(V vertex, V target)
	{
		return Math.abs(getDepthToRoot(vertex)-getDepthToRoot(target));
//		int depth = 0;
//		while (vertex != target)
//		{
//			depth += 1;
//			vertex = getParentOf(vertex);
//			if (vertex == getRoot() || vertex == null)
//				return depth;
//		}
//		if (depth > 0)
//			return depth;
//		return -depth;
	}

	// Note that this method doesn't check whether target is a descendant of vertex.
	public double getHeightToVertex(V vertex, V target)
	{
		double vHeight = getHeightToRoot(vertex);
		double tHeight = getHeightToRoot(target);

		return tHeight - vHeight;
	}

	public double getMaxHeightToLeaf(V vertex)
	{
		double vertexHeight = getHeightToRoot(vertex);
		double maxHeight = 0;
		BreadthFirstIterator<V, E> bfi = new BreadthFirstIterator<V, E>(this, vertex);
		while (bfi.hasNext())
		{
			V o = bfi.next();
			if (isLeaf(o))
			{
				double curHeight = getHeightToRoot(o);
				if (curHeight > maxHeight)
					maxHeight = curHeight;
			}
		}
		return maxHeight - vertexHeight;
	}

	public double getHeightToRoot(V vertex)
	{
		double height = 0;
		while (vertex != root)
		{
			V parent = getParentOf(vertex);
			E edge = getEdge(parent, vertex);
			height += getEdgeWeight(edge);
			// System.out.println("v:"+vertex+" p:"+parent+"
			// w:"+getEdgeWeight(edge));
			vertex = parent;
		}
		return height;
	}

	public synchronized int getNumEnclosedLeaves(V vertex)
	{
		return getEnclosedLeaves(vertex).size();
	}

	public int getMaxChildEnclosed(V vertex)
	{
		if (isLeaf(vertex))
			return 0;
		List<V> children = getChildrenOf(vertex);
		int max = 0;
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
	public synchronized void getAll(V vertex, List<V> leaves, List<V> nodes)
	{
		if (vertex == null)
			return;
		Stack<V> s = new Stack();
		s.push(vertex);
		while (!s.isEmpty())
		{
			V v = s.pop();
//			if (!shouldKeep(v))
//				continue;
			if (isLeaf(v))
			{
				if (leaves != null)
					leaves.add(v);
			} else
			{
				List<V> children = getChildrenOf(v);
				for (int i = children.size() - 1; i >= 0; i--)
				{
					s.add(children.get(i));
				}
			}
			if (nodes != null)
				nodes.add(v);
		}
	}

	private List<V> getEnclosedVertices(V vertex)
	{
		ArrayList<V> l = new ArrayList<V>();
		BreadthFirstIterator<V, E> bfi = new BreadthFirstIterator<V, E>(this, vertex);
		while (bfi.hasNext())
		{
			l.add(bfi.next());
		}
		return l;
	}

	private List<V> getEnclosedLeaves(V vertex)
	{
		ArrayList<V> l = new ArrayList<V>();
		BreadthFirstIterator<V, E> bfi = new BreadthFirstIterator<V, E>(this, vertex);
		while (bfi.hasNext())
		{
			V o = bfi.next();
			if (isLeaf(o))
				l.add(o);
		}
		return l;
	}

	public synchronized void deleteSubtree(V vertex)
	{
		if (vertex == getRoot())
		{
			V newRoot = createAndAddVertex();
			setRoot(newRoot);
		}
		if (isLeaf(vertex))
		{
			removeVertex(vertex);
		} else
		{
			List<V> nodes = getEnclosedVertices(vertex);
			synchronized (this)
			{
				for (int i = 0; i < nodes.size(); i++)
				{
					removeVertex(nodes.get(i));
				}
			}
		}
	}

	public void deleteLeafLineage(V vertex)
	{
		V parent = getParentOf(vertex);
		removeVertex(vertex);
		if (parent != null)
		{
			if (isLeaf(parent))
			{
				deleteLeafLineage(parent);
			}
		}
	}

	public synchronized void deleteNode(V vertex)
	{
		if (getParentOf(vertex) != null)
		{
			V parent = getParentOf(vertex);
			double weightToParent = getEdgeWeight(getEdge(parent, vertex));
			List<V> children = getChildrenOf(vertex);
			for (int i = 0; i < children.size(); i++)
			{
				V child = children.get(i);
				double weight = getEdgeWeight(getEdge(vertex, child));
				E edge = addEdge(parent, child);
				setEdgeWeight(edge, weightToParent + weight);
			}
			removeVertex(vertex);
		} else
		{
			V newRoot = createAndAddVertex();
			setRoot(newRoot);
			removeVertex(vertex);
		}
	}

	public RootedTree<V, E> cloneSubtree(V vertex)
	{
		RootedTree<V, E> newTree;
		try
		{
			Constructor<? extends RootedTree> c = this.getClass().getConstructor();
			newTree = c.newInstance();
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
		Stack<V> myStack = new Stack(); // One for me...
		Stack<V> newStack = new Stack(); // One for you.
		// Initialize the traversal with the designated start vertex.
		myStack.push(vertex);
		V newRoot = newTree.createAndAddVertex();
		newRoot.setLabel(vertex.getLabel());
		newTree.setRoot(newRoot);
		newStack.push(newRoot);
		while (!myStack.isEmpty())
		{
			V parent = myStack.pop();
			V newParent = newStack.pop();
			List<V> list = getChildrenOf(parent);
			for (int i = 0; i < list.size(); i++)
			{
				V thisChild = list.get(i);
				myStack.push(thisChild);
				double thisWeight = getEdgeWeight(getEdge(parent, thisChild));
				// Now do the same (while creating stuff) for the cloned tree.
				//				V newChild = newTree.createAndAddVertex();
				V newChild = (V) thisChild.clone();
				newTree.addVertex(newChild);
				newStack.push(newChild);
				E e = newTree.addEdge(newParent, newChild);
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
	public void addSisterNode(V v, V newSister)
	{
		V curParent = getParentOf(v);
		V newParent = createAndAddVertex();
		if (v == getRoot())
		{
			setRoot(newParent);
			addEdge(newParent, newSister);
			addEdge(newParent, v);
		} else
		{
			insertNodeBetween(curParent, v, newParent);
		}
		addEdge(newParent, newSister);
		double ew = getEdgeWeight(getEdge(newParent, v));
		setEdgeWeight(getEdge(newParent, newSister), ew);
		modPlus();
	}

	/**
	 * Adds an "anonymous" child node below the given vertex.
	 * 
	 * @param v
	 */
	public void addChildNode(V v)
	{
		V newNode = createAndAddVertex();
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
	public void insertNodeBetween(V a, V b, V insertMe)
	{
		E e = getEdge(a, b);
		double weight = getEdgeWeight(e);
		E aToNew = addEdge(a, insertMe); //
		setEdgeWeight(aToNew, weight / 2);
		E newToB = addEdge(insertMe, b);
		setEdgeWeight(newToB, weight / 2);
		removeEdge(a, b);
		modPlus();
	}

	/**
	 * Reroots this RootedTree using the midpoint method. The pivot edge is
	 * taken to be the edge between the given vertex (called "pivot") and its
	 * parent. See the inline comments for more information on the algorithm and
	 * steps involved.
	 * 
	 * @param pivot
	 */
	public void reroot(V pivot)
	{
		isValid = false;
		// System.out.println("Rerooting tree...");
		// System.out.println("Step 1...");
		// System.out.println(this);

		if (pivot == root || getParentOf(pivot) == root)
			return;
		/**
		 * Ok, let's do this thang.
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
		E e = null;
		V v = null;
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
		List<V> l = getChildrenOf(root);
		for (int i = 0; i < l.size(); i++)
		{
			V child = l.get(i);
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
		V newRoot = createAndAddVertex();
		// Capture the length of the edge above the pivot vertex.
		insertNodeBetween(getParentOf(pivot), pivot, newRoot);
		root = newRoot;

		// System.out.println("Step 3...");
		// System.out.println(this);
		// STEP 3: Re-orient all edges, branching out from the root edge.
		//
		// Iterate over an undirected version of this graph, so we can "go
		// against the grain" when we need to.
		BreadthFirstIterator<V, E> bfi = new BreadthFirstIterator<V, E>(Graphs.undirectedGraph(this), root);
		// Toss all the nodes into a linked list.
		LinkedList<V> linked = new LinkedList();
		while (bfi.hasNext())
		{
			linked.addLast(bfi.next());
		}
		// Now, go through the list of nodes, re-orienting edges as needed.
		// Set the root node as the first parent in our algorithm.
		HashMap<V, Integer> seen = new HashMap();
		Integer stupidInt = new Integer(1);
		while (!linked.isEmpty())
		{
			V curNode = linked.removeFirst();
			/*
			 * Here we grab all the edges touching curNode and iterate through
			 * them. We can't just use the Set's iterator because we'll be
			 * modifying the graph structure as we go -- this causes bad stuff
			 * to happen in the graph-backed iterator.
			 */
			List<V> list = Graphs.neighborListOf(this, curNode);
			for (int i = 0; i < list.size(); i++)
			{
				V child = list.get(i);
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
		isValid = true;
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
	public void flipChildren(V parent)
	{
		// If we've already stored a sorting value, then toggle it.
		Integer o = sorting.get(parent);
		if (o == null)
			setSorting(parent, REVERSE);
		else if (o == REVERSE)
			setSorting(parent, FORWARD);
		else
			setSorting(parent, REVERSE);
	}

	/**
	 * Reverses the entire subtree below a given vertex.
	 * 
	 * @param vertex
	 */
	public void reverseSubtree(V vertex)
	{
		ArrayList<V> nodes = new ArrayList<V>();
		getAll(vertex, null, nodes);
		for (V node : nodes)
		{
			flipChildren(node);
		}
		modPlus();
	}

	/**
	 * Ladderizes the tree below the given vertex. Ladderizing is equivalent to
	 * "re-sorting" the tree, where the vertices below a given vertex are sorted
	 * by the their number of total enclosed leaf vertices.
	 * 
	 * @param vertex
	 */
	public void ladderizeSubtree(V vertex)
	{
		BreadthFirstIterator<V, E> bfi = new BreadthFirstIterator<V, E>(this, vertex);
		while (bfi.hasNext())
		{
			V o = bfi.next();
			setSorting(o, FORWARD);
		}
	}

	int fInt = FORWARD.intValue();

	public void setSorting(V vertex, int direction)
	{
		if (fInt == direction)
			sorting.put(vertex, FORWARD);
		else
			sorting.put(vertex, REVERSE);
	}

	public int getSorting(V v)
	{
		Integer i = sorting.get(v);
		if (i == null || i == FORWARD)
			return FORWARD_I;
		else
			return REVERSE_I;
	}

	/**
	 * Removes "elbowed" nodes from the subtree below the given vertex. An
	 * elbowed node is defined as a node that has a single parent and a single
	 * child.
	 * 
	 * @param vertex
	 *            the node at which to begin culling
	 */
	public void removeElbowsBelow(V vertex)
	{
		ArrayList<V> list = new ArrayList<V>();
		getAll(vertex, null, list);
		for (int i = 0; i < list.size(); i++)
		{
			V o = list.get(i);
			if (getParentOf(o) != null && getChildrenOf(o).size() == 1)
			{
				V parent = getParentOf(o);
				V child = getChildrenOf(o).get(0);
				double edgeWeight = getEdgeWeight(getEdge(parent, o));
				edgeWeight += getEdgeWeight(getEdge(o, child));
				removeVertex(o);
				addEdge(parent, child);
				setEdgeWeight(getEdge(parent, child), edgeWeight);
			} else if (getParentOf(o) == null && getChildrenOf(o).size() == 1)
			{
				V child = getChildrenOf(o).get(0);
				setBranchLength(child, 0);
				setRoot(child);
				removeVertex(o);
			}
		}
	}

	/**
	 * @return the current root of this RootedTree.
	 */
	public V getRoot()
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
	public void setRoot(V newRoot)
	{
		if (!containsVertex(newRoot))
		{
			addVertex(newRoot);
		}
		root = newRoot;
	}

	/**
	 * Aligns the leaves of the tree, adjusting branch lengths accordingly.
	 * 
	 * General algorithm is this: - Start at root. - For each node: - Find the
	 * mean height-to-leaf among children nodes. - Scale each child node's
	 * branch length accordingly, then iterate.
	 */
	public void alignLeaves()
	{
		DepthFirstIterator<V, E> it = new DepthFirstIterator<V, E>(this, getRoot());
		LinkedList<V> list = new LinkedList<V>();
		while (it.hasNext())
		{
			list.add(it.next());
		}

		while (!list.isEmpty())
		{
			V v = list.removeLast();
			if (isLeaf(v))
				continue;
			List<V> children = getChildrenOf(v);
			double totalHeight = 0;
			for (V child : children)
			{
				double below = getMaxHeightToLeaf(child);
				double above = getBranchLength(child);
				totalHeight += below + above;
			}
			totalHeight /= children.size();

			for (V child : children)
			{
				double below = getMaxHeightToLeaf(child);
				double above = getBranchLength(child);
				if (below + above == 0)
					below = 0.00001;
				double childScale = totalHeight / (below + above);
				scaleSubtree(child, childScale);
			}
		}
	}

	public V getCommonAncestorOf(String... nodeLabels)
	{
		ArrayList<String> labels = new ArrayList<String>();
		Collections.addAll(labels, nodeLabels);
		List<V> nodes = getVerticesForLabels(labels);
		if (nodes.size() == 0)
			return null;
		return getCommonAncestorOf(nodes.toArray(emptyArray(nodes.get(0))));
	}

	private V[] emptyArray(V ... array)
	{
		V[] a = (V[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), 0);
		return a;
	}

	public V getCommonAncestorOf(V... nodes)
	{
		V a = nodes[0];
		V b = null;

		for (int i = 1; i < nodes.length; i++)
		{
			b = nodes[i];
			a = getCommonAncestorOf(a, b);
		}
		return a;
	}

	public V getCommonAncestorOf(String labelA, String labelB)
	{
		return getCommonAncestorOf(getVertexForLabel(labelA), getVertexForLabel(labelB));
	}

	public V getCommonAncestorOf(V a, V b)
	{
		HashSet<V> seenAncestors = new HashSet<V>();

		V ancA = a;
		V ancB = b;
		V root = getRoot();
		while (true) // Maybe we should have some fail-safe cutoff here.
		{
			if (ancA != root && ancA != null)
			{
				if (seenAncestors.contains(ancA))
					return ancA;
				seenAncestors.add(ancA);
				ancA = getParentOf(ancA);
			}
			if (ancB != root && ancB != null)
			{
				if (seenAncestors.contains(ancB))
					return ancB;
				seenAncestors.add(ancB);
				ancB = getParentOf(ancB);
			}
			if (ancA == root && ancB == root)
				return ancA;
			if (ancA == null)
				return null;
		}
	}

	public void scaleSubtree(V v, double scale)
	{
		DepthFirstIterator<V, E> it = new DepthFirstIterator<V, E>(this, v);
		it.next(); // Should I be doing this?
		while (it.hasNext())
		{
			V curV = it.next();
			setBranchLength(curV, getBranchLength(curV) * scale);
		}
	}

	public void evenlySpaceLineage(V start, V end)
	{
		double totalHeight = getHeightToVertex(start, end);
		double depth = getDepthToVertex(start, end);
		double evenStep = totalHeight / depth;
		
		V v = end;
		while (v != start)
		{
			setBranchLength(v, evenStep);
			v = getParentOf(v);
		}
	}
	
	public void makeSubtreeUltrametric(V v)
	{
		makeSubtreeUltrametric(v,1,false);
	}
	
	public void makeSubtreeUltrametric(V v, double totalHeight, boolean changeCurrentLength)
	{
//		double subtreeHeight = getMaxHeightToLeaf(v);
		double subtreeDepth = getMaxDepthToLeaf(v);
		if (changeCurrentLength)
			subtreeDepth = getMaxDepthToLeaf(getParentOf(v));
		double step = totalHeight / (subtreeDepth);

		//		System.out.println("sHeight: "+subtreeHeight+"  sDepth:"+subtreeDepth+" step:"+step);

		HashMap<V, Double> branchLengths = new HashMap<V,Double>();
		
		DepthFirstIterator<V, E> it = new DepthFirstIterator<V, E>(this, v);
		while (it.hasNext())
		{
			V curV = it.next();
			if (curV == v && !changeCurrentLength)
				continue;
			if (isLeaf(curV))
			{
				double curDepth = getDepthToVertex(curV, v);
				double curHeight = curDepth * step;
				double desiredHeight = totalHeight - curHeight;
				branchLengths.put(curV,desiredHeight);
			} else
			{
				branchLengths.put(curV, step);
			}
		}
		
		setBranchLengths(branchLengths);
	}

	public void setBranchLengths(Map<V,Double> branchLengths)
	{
		Set<V> set = branchLengths.keySet();
		for (V v : set)
		{
			setBranchLength(v, branchLengths.get(v));
		}
	}
	
	public void pruneNodes(List<V> vertices)
	{
		int i = 0;
		for (V v : vertices)
		{
			i++;
			if (isLeaf(v))
				deleteLeafLineage(v);
			else
				deleteNode(v);
		}
	}

	public void spliceOutInternalNode(V vertex)
	{
		List<V> children = getChildrenOf(vertex);
		
	}
	
	public void translateLabels(V v, Map<String, String> oldToNew)
	{
		DepthFirstIterator<V, E> it = new DepthFirstIterator<V, E>(this, v);
		while (it.hasNext())
		{
			V vertex = it.next();
			String oldS = getLabel(vertex);
			String newS = oldToNew.get(oldS);
			if (newS == null)
			{
				continue;
			}
			setLabel(vertex, newS);
		}
	}

	public void pruneNodesByLabel(List<String> vertexLabels)
	{
		List<V> verts = getVerticesForLabels(vertexLabels);
		for (V v : verts)
		{
			deleteSubtree(v);
		}
	}

	public void dispose()
	{
		sorting = null;
		neighbors = null;
		root = null;
		uniqueLabeler = null;
	}

	class VertexAndDouble
	{
		public V v;
		public double d;

		public VertexAndDouble(V v, double d)
		{
			this.v = v;
			this.d = d;
		}
	}

	public class DepthToRootComparator implements Comparator
	{
		int dir;

		public DepthToRootComparator(int dir)
		{
			this.dir = dir;
		}

		public int compare(Object o1, Object o2)
		{
			int a = getDepthToRoot((V) o1);
			int b = getDepthToRoot((V) o2);

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

	public class EnclosedLeavesComparator implements Comparator
	{
		int dir;

		public EnclosedLeavesComparator(int dir)
		{
			this.dir = dir;
		}

		public int compare(Object o1, Object o2)
		{
			int a = getNumEnclosedLeaves((V) o1);
			int b = getNumEnclosedLeaves((V) o2);

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
