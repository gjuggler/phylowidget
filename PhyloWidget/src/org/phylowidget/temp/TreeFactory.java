package org.phylowidget.temp;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

public class TreeFactory
{

	public static PhyloTreeGraph createGraph()
	{
		return new PhyloTreeGraph();
	}
}
