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
package org.phylowidget;

import java.util.ArrayList;
import java.util.List;

import org.andrewberman.ui.unsorted.SearchIndex;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.phylowidget.tree.CachedRootedTree;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.ui.NodeUncollapser;

public class PhyloTree extends CachedRootedTree<PhyloNode, DefaultWeightedEdge>
{
	private static final long serialVersionUID = 1L;
	private SearchIndex<PhyloNode> index = new SearchIndex<PhyloNode>();

	public PhyloTree()
	{
		super(DefaultWeightedEdge.class);
		if (PWPlatform.getInstance().getThisAppContext() != null)
			setEnforceUniqueLabels(PWPlatform.getInstance().getThisAppContext().config().enforceUniqueLabels);
	}

	@Override
	public void uncollapseNode(PhyloNode v)
	{
		v.clearAnnotation("collapse");
		
		// Set all subtree nodes to current position.
		List<PhyloNode> nodes = getAllNodes(v);
		for (PhyloNode n : nodes)
		{
			n.setLayoutX(v.getLayoutX());
			n.setLayoutY(v.getLayoutY());
			n.setX(v.getX());
			n.setY(v.getY());
			n.fforward();
		}
		modPlus();
	}
	
	private static String baseURL;
	
	public void setBaseURL(String baseURL)
	{
		this.baseURL = baseURL;
	}
	
	public String getBaseURL()
	{
		if (baseURL == null)
			return "";
		return this.baseURL;
	}
	
	@Override
	public void collapseNode(PhyloNode v)
	{
		v.setAnnotation("collapse", "yes");
		new NodeUncollapser(PWPlatform.getInstance().getThisAppContext().getPW(),v);
		modPlus();
	}
	
	@Override
	public boolean isCollapsed(PhyloNode v)
	{
		String annot = v.getAnnotation("collapse");
		if (annot != null)
		{
			if (PhyloNode.parseTruth(annot))
				return true;
		}
		return false;
	}

	public PhyloNode hoveredNode;

	public void setHoveredNode(PhyloNode n)
	{
		hoveredNode = n;
	}

	public PhyloNode createVertex()
	{
		return new PhyloNode();
	}

	public void flipChildren(PhyloNode parent)
	{
		super.flipChildren(parent);
	}

	public PhyloNode getHoveredNode()
	{
		return hoveredNode;
	}
	
	int modCount = 0;
	@Override
	public int getModCount()
	{
		return modCount;
	}
	
	public void reverseSubtree(PhyloNode vertex)
	{
		super.reverseSubtree(vertex);
	}

	@Override
	public void setLabel(Object vertex, String label)
	{
		index.remove((PhyloNode) vertex);
		super.setLabel(vertex, label);
		index.add((PhyloNode) vertex);
	}

	void removeFound()
	{
		ArrayList nodes = new ArrayList();
		getAll(getRoot(), null, nodes);
		for (int i = 0; i < nodes.size(); i++)
		{
			PhyloNode n = (PhyloNode) nodes.get(i);
			n.found = false;
		}
	}

	public void markNodesAsFound(List<PhyloNode> matches)
	{
		removeFound();
		for (PhyloNode n : matches)
		{
			PhyloNode cur = n;
			while (cur != null)
			{
				cur.found = true;
				cur = (PhyloNode) getParentOf(cur);
			}
		}
	}

	public void searchAndMarkFound(String s)
	{
		List<PhyloNode> matches = search(s);
		markNodesAsFound(matches);
	}

	public List<PhyloNode> search(String s)
	{
		String[] searches = s.split(";");

		ArrayList<PhyloNode> matches = new ArrayList<PhyloNode>();
		for (String s2 : searches)
		{
			matches.addAll(index.search(s2));
		}
		return matches;
	}

	@Override
	public boolean removeVertex(PhyloNode o)
	{
		boolean b = super.removeVertex(o);
		if (b)
			index.remove((PhyloNode) o);
		return b;
	}

	@Override
	public boolean addVertex(PhyloNode o)
	{
		boolean b = super.addVertex(o);
		if (b)
			index.add((PhyloNode) o);
		return b;
	}

	@Override
	public void modPlus()
	{
		super.modPlus();
		modCount++;
		if (modCount > 1000)
			modCount = 0;
	}

	//	class NewickUpdater implements GraphListener
	//	{
	//		public void edgeAdded(GraphEdgeChangeEvent e)
	//		{
	//			if (e.getType() == GraphEdgeChangeEvent.EDGE_ADDED)
	//			{
	//				updateNewick();
	//			}
	//		}
	//
	//		public void edgeRemoved(GraphEdgeChangeEvent e)
	//		{
	//			if (e.getType() == GraphEdgeChangeEvent.EDGE_REMOVED)
	//			{
	//				updateNewick();
	//			}
	//		}
	//
	//		public void vertexAdded(GraphVertexChangeEvent e)
	//		{
	//			if (e.getType() == GraphVertexChangeEvent.VERTEX_ADDED)
	//			{
	//				updateNewick();
	//			}
	//		}
	//
	//		public void vertexRemoved(GraphVertexChangeEvent e)
	//		{
	//			if (e.getType() == GraphVertexChangeEvent.VERTEX_REMOVED)
	//			{
	//				//				index.remove((PhyloNode) e.getVertex());
	//				updateNewick();
	//			}
	//		}
	//	}


	//	public static void main(String... args)
	//	{
	//		SimpleDirectedGraph<String, String> g = new SimpleDirectedGraph<String, String>(
	//				String.class);
	//		g.addVertex("Hello");
	//		g.addVertex("World!");
	//		g.addEdge("World!", "Hello");
	//
	//		SimpleDirectedGraph<String, String> g2 = new SimpleDirectedGraph<String, String>(
	//				String.class)
	//		{
	//			public boolean addVertex(String s)
	//			{
	//				System.out.println(s);
	//				return super.addVertex(s);
	//			}
	//		};
	//		System.out.println(g2);
	//		Graphs.addGraph(g2, g);
	//		System.out.println(g2);
	//	}
}
