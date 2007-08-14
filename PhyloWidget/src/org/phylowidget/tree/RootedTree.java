package org.phylowidget.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.Graphs;
import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.phylowidget.PhyloWidget;

public class RootedTree extends ListenableDirectedWeightedGraph
{
	private static final long	serialVersionUID	= 1L;

	/**
	 * This object helps cache the sets of predecessor and successor neighbors
	 * by listening to changes on this JGraphT object and updating as necessary.
	 * If you expect to be changing your graph lots and not calling the
	 * childrenOf() method very often, then you're best setting useNeighborIndex
	 * to "false" by overriding the setOptions() method and setting it false in
	 * there.
	 */
	DirectedNeighborIndex		neighbors;
	Object						root;

	ArrayList					tempList			= new ArrayList(3);

	/*
	 * ****** OPTIONS ******
	 */
	boolean						useNeighborIndex;

	public RootedTree(String rootLabel)
	{
		this();
		/*
		 * When we're given a label in the constructor, make a new root node out
		 * of it.
		 */
		Object o = addNode(rootLabel);
		root = o;
	}

	public RootedTree()
	{
//		super(new SimpleDirectedWeightedGraph(DefaultWeightedEdge.class));
		super(DefaultWeightedEdge.class);
		setOptions();
		if (useNeighborIndex)
			createNeighborIndex();
	}

	void createNeighborIndex()
	{
		if (neighbors != null)
			removeGraphListener(neighbors);
		neighbors = new DirectedNeighborIndex(this);
		addGraphListener(neighbors);
	}

	/**
	 * Subclasses should override this method to se any of the "option-like"
	 * boolean variables provided, such as "useNeighborIndex".
	 */
	protected void setOptions()
	{
		useNeighborIndex = true;
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
	public Object createNode(String label)
	{
		return label;
	}

	public Object addNode(String label)
	{
		Object o = createNode(label);
		addVertex(o);
		return o;
	}

	public boolean isLeaf(Object vertex)
	{
		return (outDegreeOf(vertex) == 0);
	}

	public List childrenOf(Object vertex)
	{
		if (useNeighborIndex)
		{
			ArrayList l = new ArrayList();
			l.addAll(neighbors.successorsOf(vertex));
			return l;
		} else
			return Graphs.successorListOf(this, vertex);
	}

	public Object parentOf(Object child)
	{
		// System.out.println(child+" contains:"+this.containsVertex(child));
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

	public int numEnclosedLeaves(Object vertex)
	{
		return enclosedLeaves(vertex).size();
	}

	public int depthToRoot(Object vertex)
	{
		int depth = 0;
		while (vertex != root)
		{
			depth += 1;
			vertex = parentOf(vertex);
		}
		return depth;
	}

	public double heightToRoot(Object vertex)
	{
		// System.out.println("htr!");
		double height = 0;
		while (vertex != root)
		{
			Object parent = parentOf(vertex);
			Object edge = getEdge(parent, vertex);
			height += getEdgeWeight(edge);
			// System.out.println("v:"+vertex+" p:"+parent+"
			// w:"+getEdgeWeight(edge));
			vertex = parent;
		}
		return height;
	}

	// Set childrenSetOf(Object vertex)
	// {
	// Set s;
	// if (useNeighborIndex)
	// s = neighbors.successorsOf(vertex);
	// else
	// {
	// Graphs.get
	// }
	// return s;
	// }

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
				List children = childrenOf(v);
				s.addAll(children);
			}
			if (nodes != null)
				nodes.add(v);
		}

		// BreadthFirstIterator bfi = new BreadthFirstIterator(this, vertex);
		// while (bfi.hasNext())
		// {
		// Object n = bfi.next();
		// if (isLeaf(n) && leaves != null)
		// leaves.add(n);
		// if (nodes != null)
		// nodes.add(n);
		// }
	}

	/**
	 * 
	 * @param vertex
	 * @return
	 */
	List enclosedNodes(Object vertex)
	{
		ArrayList l = new ArrayList();
		BreadthFirstIterator bfi = new BreadthFirstIterator(this, vertex);
		while (bfi.hasNext())
		{
			l.add(bfi.next());
		}
		return l;
	}

	List enclosedLeaves(Object vertex)
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

	public int getMaxDepth()
	{
		int maxDepth = 0;
		BreadthFirstIterator bfi = new BreadthFirstIterator(this, root);
		while (bfi.hasNext())
		{
			Object o = bfi.next();
			if (isLeaf(o))
			{
				int curDepth = depthToRoot(o);
				if (curDepth > maxDepth)
					maxDepth = curDepth;
			}
		}
		return maxDepth;
	}

	public double getMaxHeight()
	{
		double maxHeight = 0;
		BreadthFirstIterator bfi = new BreadthFirstIterator(this, root);
		while (bfi.hasNext())
		{
			Object o = bfi.next();
			if (isLeaf(o))
			{
				double curHeight = heightToRoot(o);
				if (curHeight > maxHeight)
					maxHeight = curHeight;
			}
		}
		return maxHeight;
	}

	public synchronized void reroot(Object pivot)
	{	
//		System.out.println("Rerooting tree...");
//		System.out.println("Step 1...");
//		System.out.println(this);
		if (pivot == root || parentOf(pivot) == root)
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
		while (parentOf(v) != null)
		{
			if (parentOf(v) == root)
				break;
			v = parentOf(v);
		}
		// V should now be the child that leads from root to newRoot.
		double lengthB = getEdgeWeight(getEdge(root, v));
		List l = childrenOf(root);
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
//		System.out.println("Step 2...");
//		System.out.println(this);
		// STEP 2: Create the new root.
		//
		// Add the new root vertex.
		Object newRoot = addNode("newroot");
		// Capture the length of the edge above the pivot vertex.
		insertNodeBetween(parentOf(pivot), pivot, newRoot);
		root = newRoot;

//		System.out.println("Step 3...");
//		System.out.println(this);
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
		// Finally, recreate the neighbor index if necessary.
//		if (useNeighborIndex)
//			createNeighborIndex();
//		System.out.println("Done!");
//		System.out.println(this);
	}

	/**
	 * Adds a new sister node to this vertex. In order to do this and maintain a
	 * relatively bifurcated tree, this method creates a new node <em>above</em>
	 * the given vertex, and adds a new sister node to the newly created
	 * "parent" node.
	 * 
	 * @param v
	 */
	public void addSisterNode(Object v)
	{
		Object curParent = parentOf(v);
		Object newParent = addNode("[new parent]");
		insertNodeBetween(curParent, v, newParent);
		Object newSister = addNode("[new sister]");
		addEdge(newParent,newSister);
		addEdge(newParent,v);
	}

	/**
	 * Inserts vertex insertMe at the midpoint of the edge between a and b.
	 * 
	 * @param a
	 *            the parent node (edge source)
	 * @param b
	 *            the child node (edge target)
	 * @param insertMe
	 */
	void insertNodeBetween(Object a, Object b, Object insertMe)
	{
		Object e = getEdge(a, b);
		double weight = getEdgeWeight(e);
		removeEdge(a,b);
		removeEdge(a,b);
		Object newToB = addEdge(insertMe, b);
		setEdgeWeight(newToB, weight / 2);
		Object aToNew = addEdge(a, insertMe);
		setEdgeWeight(aToNew, weight / 2);
	}

	public Object getRoot()
	{
		return root;
	}

	public void setRoot(Object newRoot)
	{
		root = newRoot;
	}
}
