package org.phylowidget.tree;

import org.jgrapht.Graph;
import org.jgrapht.traverse.BreadthFirstIterator;

public class FilteredBreadthIterator<V,E> extends BreadthFirstIterator<V,E>
{
	RootedTree tree;
	
	public FilteredBreadthIterator(Graph<V,E> g, V startVertex)
	{
		super(g, startVertex);
		this.tree = (RootedTree) g;
	}
	
	@Override
	protected V provideNextVertex()
	{
		V vertex = super.provideNextVertex();
		if (!tree.shouldKeep((DefaultVertex) vertex))
		{
			return null;
		} else
			return vertex;
	}

}
