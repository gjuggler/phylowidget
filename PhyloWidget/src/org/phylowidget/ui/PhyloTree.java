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
package org.phylowidget.ui;

import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.phylowidget.PhyloWidget;
import org.phylowidget.net.JSTreeUpdater;
import org.phylowidget.tree.CachedRootedTree;
import org.phylowidget.tree.Labelable;

public class PhyloTree extends CachedRootedTree
{
	private static final long serialVersionUID = 1L;

	private JSTreeUpdater updater;
	
	private boolean synchronizedWithJS;

	public PhyloTree()
	{
		super();
		boolean unique = PhyloWidget.ui.enforceUniqueLabels;
		setEnforceUniqueLabels(unique);
	}
	
	public static PhyloTree createDefault()
	{
		PhyloTree t = new PhyloTree();
		Object v = t.createAndAddVertex("PhyloWidget");
		t.setRoot(v);
		return t;
	}
	
	public PhyloNode hoveredNode;
	
	public void setHoveredNode(PhyloNode n)
	{
		hoveredNode = n;
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

	public void reverseSubtree(Object vertex)
	{
		super.reverseSubtree(vertex);
		updateNewick();
	}

	public void updateNewick()
	{
		if (synchronizedWithJS)
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
}
