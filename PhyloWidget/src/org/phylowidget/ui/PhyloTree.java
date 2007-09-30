package org.phylowidget.ui;

import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.phylowidget.PhyloWidget;
import org.phylowidget.net.JSObjectCrap;
import org.phylowidget.net.TextBoxUpdater;
import org.phylowidget.tree.CachedRootedTree;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;

public class PhyloTree extends CachedRootedTree
{
	private static final long serialVersionUID = 1L;

	public boolean loading;
	private TextBoxUpdater updater = new TextBoxUpdater();

	public PhyloTree(Object o)
	{
		super(o);
		addGraphListener(new NewickUpdater());
	}

	public PhyloTree()
	{
		super();
		addGraphListener(new NewickUpdater());
	}

	public Object createVertex(Object o)
	{
		return new PhyloNode(o);
	}

	public void flipChildren(Object parent)
	{
		super.flipChildren(parent);
		updateNewick();
	}

	public void reverseAllChildren(Object vertex)
	{
		super.reverseAllChildren(vertex);
		updateNewick();
	}

	public void updateNewick()
	{
		if (loading)
			return;
		updater.triggerUpdate(this);
	}

	class NewickUpdater implements GraphListener
	{

		public void edgeAdded(GraphEdgeChangeEvent e)
		{
			if (e.getType() == GraphEdgeChangeEvent.EDGE_ADDED)
				updateNewick();
		}

		public void edgeRemoved(GraphEdgeChangeEvent e)
		{
			if (e.getType() == GraphEdgeChangeEvent.EDGE_REMOVED)
				updateNewick();
		}

		public void vertexAdded(GraphVertexChangeEvent e)
		{
			if (e.getType() == GraphVertexChangeEvent.VERTEX_ADDED)
				updateNewick();
		}

		public void vertexRemoved(GraphVertexChangeEvent e)
		{
			if (e.getType() == GraphVertexChangeEvent.VERTEX_REMOVED)
				updateNewick();
		}

	}
}
