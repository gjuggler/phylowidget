package org.phylowidget.temp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.ListenableGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphDelegator;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;

public class PhyloTreeGraph extends ListenableDirectedWeightedGraph
{
	private static final long serialVersionUID = 1L;

	DirectedNeighborIndex neighbors;
	NewRenderNode root;

	int modCount;
	float maxDepth;
	float maxHeight;
	
	public PhyloTreeGraph(String rootLabel)
	{
		this();
		NewRenderNode n = new NewRenderNode(rootLabel);
		addVertex(n);
		root = n;
	}

	public PhyloTreeGraph()
	{
		super(DefaultWeightedEdge.class);
		neighbors = new DirectedNeighborIndex(this);
		addGraphListener(neighbors);
	}
	
	ArrayList temp = new ArrayList(3);
	void assignParents()
	{
		System.out.println("Assigning parents...");

		BreadthFirstIterator bfi = new BreadthFirstIterator(this, root);
		bfi.setReuseEvents(true);
		Stack stack = new Stack();
		while (bfi.hasNext())
		{
			stack.push(bfi.next());
		}
		while (!stack.isEmpty())
		{
			NewRenderNode n = (NewRenderNode) stack.pop();
//			System.out.println(n);
			Set s = neighbors.successorsOf(n);
			temp.clear();
			temp.addAll(s);
			for (int i=0; i < temp.size(); i++)
			{
				NewRenderNode child = (NewRenderNode) temp.get(i);
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
				Object edge = getEdge(n,child);
				setEdgeWeight(edge, edgeWeight);
			}
		}
	}

	void calculateStuff()
	{
		if (root == null) return;
		System.out.println("Calculating stuff...");
		/*
		 * Reset the root vertex calcs and the whole-tree calcs.
		 */
		root.resetCalculations();
		
		calcDepthAndHeight();
		calcNumLeaves();
	}

	public void calcDepthAndHeight()
	{
		maxDepth = 0;
		maxHeight = 0;

		BreadthFirstIterator bfi = new BreadthFirstIterator(this, root);
		bfi.setReuseEvents(true);
		while (bfi.hasNext())
		{
			NewRenderNode n = (NewRenderNode) bfi.next();
//			System.out.println(n);
			Set s = neighbors.successorsOf(n);
			Iterator i = s.iterator();
			while (i.hasNext())
			{
				NewRenderNode child = (NewRenderNode) i.next();
				child.depthToRoot = n.depthToRoot + 1;
				child.heightToRoot = n.heightToRoot
						+ (float)getEdgeWeight(getEdge(n, child));
				if (child.depthToRoot > maxDepth)
					maxDepth = child.depthToRoot;
				if (child.heightToRoot > maxHeight)
					maxHeight = child.heightToRoot;
			}
		}

	}
	
	public void calcNumLeaves()
	{	
		BreadthFirstIterator bfi = new BreadthFirstIterator(this, root);
		bfi.setReuseEvents(true);
		while (bfi.hasNext())
		{
			NewRenderNode n = (NewRenderNode) bfi.next();
			if (isLeaf(n))
			{
				NewRenderNode curNode = n;
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

	public int getNumLeaves(NewRenderNode n)
	{
		return n.numEnclosedLeaves;
	}

	public List childrenOf(Object vertex)
	{
		List l = Graphs.successorListOf(this, vertex);
		return l;
//		return neighbors.successorListOf(vertex);
	}

	public Object parentOf(Object vertex)
	{
		List l = Graphs.predecessorListOf(this, vertex);
		if (l.size() == 0)
			return null;
		return l.get(0);
	}

	public void getAll(Object vertex, ArrayList leaves, ArrayList nodes)
	{
		BreadthFirstIterator bfi = new BreadthFirstIterator(this, vertex);
		while (bfi.hasNext())
		{
			NewRenderNode n = (NewRenderNode) bfi.next();
			if (isLeaf(n) && leaves != null)
				leaves.add(n);
			if (nodes != null)
				nodes.add(n);
		}
	}

	public int getModCount()
	{
		return modCount;
	}

	public float getMaxDepth()
	{
		return maxDepth;
	}
	
	public float getMaxHeight()
	{
		return maxHeight;
	}
	
	public NewRenderNode getRoot()
	{
		return (NewRenderNode) root;
	}

	public void setRoot(NewRenderNode newRoot)
	{
		root = newRoot;
		assignParents();
		calculateStuff();
	}


	protected void fireVertexRemoved(Object o)
	{
		super.fireVertexRemoved(o);
		
		calculateStuff();
	}
	
	protected void fireVertexAdded(Object o)
	{
		super.fireVertexAdded(o);
		calculateStuff();
	}
	
}
