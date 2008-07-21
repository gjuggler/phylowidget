package org.phylowidget.tree;

public interface TreeFilter<V>
{

	public boolean shouldKeep(V vertex);
	
}
