package org.phylowidget.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.Graphs;
import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

public class RootedTreeGraph extends ListenableDirectedWeightedGraph
{
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
	Object root;

	ArrayList tempList = new ArrayList(3);

	float maxDepth;
	float maxHeight;

	/*
	 * ****** OPTIONS ******
	 */
	boolean useNeighborIndex = true;

	/**
	 * A static "factory" for creating node objects. Currently it just returns
	 * the string given as input, but it could be extended by someone to create
	 * a node object that holds more detailed information. These objects will be
	 * the "vertex" objects inserted into the JGraphT structure.
	 * <p>
	 * It's probably useful to note that inserting larger objects into the
	 * JGraphT won't slow it down or make it take anymore memory; it simply
	 * stores a hashtable of references back to these vertex objects, and does
	 * all the internal stuff using its own internal data structures.
	 * 
	 * @param label
	 * @return
	 */
	public static Object createNode(String label)
	{
		return label;
	}

	public RootedTreeGraph(String rootLabel)
	{
		this();
		PhyloNode n = new PhyloNode(rootLabel);
		addVertex(n);
		root = n;
	}

	public RootedTreeGraph()
	{
		super(DefaultWeightedEdge.class);
		setOptions();
		if (useNeighborIndex)
		{
			neighbors = new DirectedNeighborIndex(this);
			addGraphListener(neighbors);
		}
	}

	/**
	 * Subclasses should override this method to se any of the "option-like"
	 * boolean variables provided, such as "useNeighborIndex".
	 */
	protected void setOptions()
	{

	}

	synchronized void assignParents()
	{
		System.out.println("Assigning parents...");

		BreadthFirstIterator bfi = new BreadthFirstIterator(Graphs
				.undirectedGraph(this), root);
		bfi.setReuseEvents(true);
		LinkedList l = new LinkedList();
		while (bfi.hasNext())
		{
			l.addLast(bfi.next());
			// stack.push(bfi.next());
			// System.out.println(stack.peek());
		}
		while (!l.isEmpty())
		{
			// PhyloNode n = (PhyloNode) stack.pop();
			PhyloNode n = (PhyloNode) l.removeFirst();
			System.out.println(n);
			List s = Graphs.successorListOf(this, n);
			tempList.clear();
			tempList.addAll(s);
			for (int i = 0; i < tempList.size(); i++)
			{
				PhyloNode child = (PhyloNode) tempList.get(i);
				child.parent = n;
				/*
				 * Grab the edge weight.
				 */
				double edgeWeight = 1;
				if (this.containsEdge(n, child))
				{
					edgeWeight = getEdgeWeight(getEdge(n, child));
				} else if (this.containsEdge(child, n))
				{
					edgeWeight = getEdgeWeight(getEdge(child, n));
				}
				/*
				 * Remove existing edges, add our new directed weighted edge.
				 */
				removeEdge(n, child);
				removeEdge(child, n);
				addEdge(n, child);
				Object edge = getEdge(n, child);
				setEdgeWeight(edge, edgeWeight);
			}
		}
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
//		calcDepthAndHeight();
		calcNumLeaves();
	}

//	public void calcDepthAndHeight()
//	{
//		maxDepth = 0;
//		maxHeight = 0;
//
//		BreadthFirstIterator bfi = new BreadthFirstIterator(this, root);
//		bfi.setReuseEvents(true);
//		while (bfi.hasNext())
//		{
//			Object n = bfi.next();
//			// System.out.println(n);
//			Set s = childrenSetOf(n);
//			Iterator i = s.iterator();
//			while (i.hasNext())
//			{
//				PhyloNode child = (PhyloNode) i.next();
//				child.depthToRoot = n.depthToRoot + 1;
//				child.heightToRoot = n.heightToRoot
//						+ (float) getEdgeWeight(getEdge(n, child));
//				if (child.depthToRoot > maxDepth)
//					maxDepth = child.depthToRoot;
//				if (child.heightToRoot > maxHeight)
//					maxHeight = child.heightToRoot;
//			}
//		}
//	}

	public void calcNumLeaves()
	{
		BreadthFirstIterator bfi = new BreadthFirstIterator(this, root);
		bfi.setReuseEvents(true);
		while (bfi.hasNext())
		{
			PhyloNode n = (PhyloNode) bfi.next();
			if (isLeaf(n))
			{
				PhyloNode curNode = n;
				curNode.numEnclosedLeaves = 0;
				while (curNode != null)
				{
					curNode.numEnclosedLeaves++;
					curNode = curNode.parent;
				}
			}
		}
	}

	public boolean isLeaf(Object vertex)
	{
		return (outDegreeOf(vertex) == 0);
	}

	public int getNumLeaves(PhyloNode n)
	{
		return n.numEnclosedLeaves;
	}

	public List childrenOf(Object vertex)
	{
		ArrayList list = new ArrayList();
		list.addAll(childrenSetOf(vertex));
		return list;
	}

	Set childrenSetOf(Object vertex)
	{
		Set s = neighbors.successorsOf(vertex);
		return s;
	}

	public Object parentOf(Object child)
	{
		Set parentSet = neighbors.predecessorsOf(child);
		if (parentSet.size() == 0)
			return null;
		return parentSet.iterator().next();
	}

	/**
	 * A convenience method for retrieving all nodes below a given vertex in a
	 * tree. The "leaves" and "nodes" Arraylists will be filled with all the
	 * appropriate nodes.
	 * 
	 * @param vertex
	 * @param leaves
	 * @param nodes
	 */
	public void getAll(Object vertex, List leaves, List nodes)
	{
		BreadthFirstIterator bfi = new BreadthFirstIterator(this, vertex);
		while (bfi.hasNext())
		{
			Object n = bfi.next();
			if (isLeaf(n) && leaves != null)
				leaves.add(n);
			if (nodes != null)
				nodes.add(n);
		}
	}

	public float getMaxDepth()
	{
		return maxDepth;
	}

	public float getMaxHeight()
	{
		return maxHeight;
	}

	public PhyloNode getRoot()
	{
		return (PhyloNode) root;
	}

	public void setRoot(PhyloNode newRoot)
	{
		root = newRoot;
		assignParents();
		calculateStuff();
	}

	public void reroot(PhyloNode pivot)
	{
		System.out.println(this);
		// Delete the current root vertex.
		List l = childrenOf(root);
		addEdge(l.get(0), l.get(1));
		addEdge(l.get(1), l.get(0));
		removeVertex(root);
		// Create a new "fake" root on the edge above newRoot.
		PhyloNode newRoot = new PhyloNode("newroot");
		addVertex(newRoot);
		// Remove the current pivot's incoming edge.
		removeEdge(pivot, pivot.parent);
		removeEdge(pivot.parent, pivot);
		// Attach the newRoot as a parent to pivot and its old parent.
		addEdge(newRoot, pivot);
		addEdge(newRoot, pivot.parent);
		root = newRoot;
		assignParents();
		calculateStuff();
		System.out.println(this);
	}
}
