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
package org.phylowidget.ui;

import java.util.ArrayList;

import org.andrewberman.ui.unsorted.SearchIndex;
import org.jgrapht.Graphs;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.phylowidget.PhyloWidget;
import org.phylowidget.net.JSTreeUpdater;
import org.phylowidget.tree.CachedRootedTree;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;

public class PhyloTree extends CachedRootedTree<PhyloNode,DefaultWeightedEdge>
{
	private static final long serialVersionUID = 1L;

	private JSTreeUpdater updater;

	private SearchIndex<PhyloNode> index = new SearchIndex<PhyloNode>();

	private boolean synchronizedWithJS;

	public PhyloTree()
	{
		super(DefaultWeightedEdge.class);
		boolean unique = PhyloWidget.cfg.enforceUniqueLabels;
//		boolean unique = false;
		setEnforceUniqueLabels(unique);
	}

	public static PhyloTree createDefault()
	{
		PhyloTree t = new PhyloTree();
		PhyloNode v = t.createAndAddVertex();
		t.setRoot(v);
		return t;
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
		updateNewick();
	}

	public void reverseSubtree(PhyloNode vertex)
	{
		super.reverseSubtree(vertex);
		updateNewick();
	}

	@Override
	public void setLabel(Object vertex, String label)
	{
		index.remove((PhyloNode) vertex);
		super.setLabel(vertex, label);
		index.add((PhyloNode) vertex);
	}

	public void updateNewick()
	{
		if (isValid())
		{
			PhyloWidget.ui.search();
			if (synchronizedWithJS)
				updater.triggerUpdate(this);
		}
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

	public void search(String s)
	{
		removeFound();
		
		String[] searches = s.split(";");

		ArrayList<PhyloNode> matches = new ArrayList<PhyloNode>();
		for (String s2 : searches)
		{
			matches.addAll(index.search(s2));
		}
		for (PhyloNode n : matches)
		{
			PhyloNode cur = n;
			while (cur != null)
			{
				cur.found = true;
				cur = (PhyloNode) getParentOf(cur);
			}

			//			if (n.getState() == PhyloNode.NONE)
			//			{
			//				n.setState(PhyloNode.FOUND);
			//			}
		}
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

	class NewickUpdater implements GraphListener
	{
		public void edgeAdded(GraphEdgeChangeEvent e)
		{
			if (e.getType() == GraphEdgeChangeEvent.EDGE_ADDED)
			{
				updateNewick();
			}
		}

		public void edgeRemoved(GraphEdgeChangeEvent e)
		{
			if (e.getType() == GraphEdgeChangeEvent.EDGE_REMOVED)
			{
				updateNewick();
			}
		}

		public void vertexAdded(GraphVertexChangeEvent e)
		{
			if (e.getType() == GraphVertexChangeEvent.VERTEX_ADDED)
			{
				updateNewick();
			}
		}

		public void vertexRemoved(GraphVertexChangeEvent e)
		{
			if (e.getType() == GraphVertexChangeEvent.VERTEX_REMOVED)
			{
				//				index.remove((PhyloNode) e.getVertex());
				updateNewick();
			}
		}
	}

	public boolean isSynchronizedWithJS()
	{
		return synchronizedWithJS;
	}

	public void setSynchronizedWithJS(boolean synchronizedWithJS)
	{
		this.synchronizedWithJS = synchronizedWithJS;
		if (synchronizedWithJS && updater == null)
		{
			updater = new JSTreeUpdater();
			addGraphListener(new NewickUpdater());
		}
	}

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
