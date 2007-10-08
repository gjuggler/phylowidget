package org.phylowidget.tree;

public class DefaultVertex implements Labelable
{
	public String label;

	public DefaultVertex(Object o)
	{
		label = o.toString();
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public void setLabel(String s)
	{
		label = s;
	}
	
	public String toString()
	{
		return label;
	}
}
