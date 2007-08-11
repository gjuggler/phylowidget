package org.phylowidget.temp;

import java.util.ArrayList;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

public class NewRenderNode implements Comparable
{
	NewRenderNode parent;
	public String label;
	public float unscaledX,unscaledY;
	public float x,y;
	public float unitTextWidth;
	public boolean hovered;
	
	public int childIndex;
	
	public float depthToRoot;
	public float heightToRoot;
	public int numEnclosedLeaves;
	
	public NewRenderNode(String label)
	{
		this.label = label;
	}
	
	public void resetCalculations()
	{
		depthToRoot = 0;
		heightToRoot = 0;
		numEnclosedLeaves = 0;
	}
	
	public String toString()
	{
		return label;
	}
	
	public String getName()
	{
		return label;
	}
	
	public void setName(String s)
	{
		label = s;
	}
	
	public NewRenderNode getParent()
	{
		return parent;
	}
	
	public int compareTo(Object o)
	{
		if (o instanceof NewRenderNode)
		{
			NewRenderNode that = (NewRenderNode) o;
			float a = this.unscaledY;
			float b = that.unscaledY;
			if (a < b)
				return -1;
			else if (a > b)
				return 1;
		}
		return 0;
	}
	
}
