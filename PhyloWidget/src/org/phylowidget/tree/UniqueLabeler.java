package org.phylowidget.tree;

import java.util.ArrayList;
import java.util.HashMap;

public class UniqueLabeler
{
	public static final char sep = '#';

	private HashMap vertexLabels = new HashMap();

	protected void makeLabelUnique(Object object)
	{
		if (!(object instanceof Labelable))
			return;
		Labelable vertex = (Labelable) object;
		if (vertex.getLabel().length() == 0)
		{
			vertex.setLabel(sep + "1");
		}
		while (vertexLabels.containsKey(vertex.getLabel()))
		{
			String cur = vertex.getLabel();
			/*
			 * Take the current label and increment the suffixed number.
			 */
			int i = cur.lastIndexOf(sep);
			if (i != -1)
			{
				String num = cur.substring(i + 1);
				int curNum = Integer.parseInt(num) + 1;
				vertex.setLabel(cur.substring(0, i + 1) + curNum);
			} else
			{
				vertex.setLabel(cur + sep + 1);
			}
		}
	}

	protected void changeLabel(Object o, String label)
	{
		if (!(o instanceof Labelable))
			return;
		Labelable v = (Labelable)o;
		String oldLabel = v.getLabel();
		vertexLabels.remove(oldLabel);
		v.setLabel(label);
		makeLabelUnique(v);
		vertexLabels.put(v.getLabel(),v);
	}
	
	protected void addLabel(Object o)
	{
		if (!(o instanceof Labelable))
			return;
		Labelable v = (Labelable)o;
		makeLabelUnique(v);
		vertexLabels.put(v.getLabel(),v);
	}
	
	protected void removeLabel(Object o)
	{
		if (!(o instanceof Labelable))
			return;
		Labelable v = (Labelable)o;
		vertexLabels.remove(v.getLabel());
	}
	
	protected void resetVertexLabels(RootedTree t)
	{
		ArrayList nodes = new ArrayList();
		t.getAll(t.getRoot(), null, nodes);
		for (int i = 0; i < nodes.size(); i++)
		{
			Object o = nodes.get(i);
			makeLabelUnique(o);
			if (o instanceof Labelable)
				vertexLabels.put(((Labelable)o).getLabel(),o);
		}
	}
	
	public Object getNodeForLabel(String s)
	{
		return vertexLabels.get(s);
	}
	
	public static boolean isLabelSignificant(String s)
	{
		if (s.lastIndexOf(UniqueLabeler.sep) == 0)
			return false;
		else if (s.length() == 0)
			return false;
		else
			return true;
	}
}
