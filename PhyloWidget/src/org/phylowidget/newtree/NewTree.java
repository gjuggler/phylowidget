package org.phylowidget.newtree;

import java.util.ArrayList;
import java.util.Iterator;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

public class NewTree
{
	SimpleDirectedWeightedGraph dwg;
	
	Object fakeRoot;
	
	public ArrayList getAllNodes()
	{
		ArrayList list = new ArrayList(dwg.vertexSet().size());
		dwg.vertexSet().addAll(list);
		return list;
	}
	
	public void rootTree(Object edgeToRoot)
	{
		dwg = new SimpleDirectedWeightedGraph(DefaultWeightedEdge.class);
		
//		Graphs.addGraph(dwg,ug);
	}
	
}
