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
		Labelable v = (Labelable) o;
		String oldLabel = v.getLabel();
		vertexLabels.remove(oldLabel);
		v.setLabel(label);
		makeLabelUnique(v);
		vertexLabels.put(v.getLabel(), v);
	}

	protected void addLabel(Object o)
	{
		if (!(o instanceof Labelable))
			return;
		Labelable v = (Labelable) o;
		makeLabelUnique(v);
		vertexLabels.put(v.getLabel(), v);
	}

	protected void removeLabel(Object o)
	{
		if (!(o instanceof Labelable))
			return;
		Labelable v = (Labelable) o;
		vertexLabels.remove(v.getLabel());
	}

	protected void resetVertexLabels(RootedTree t)
	{
		vertexLabels.clear();
		ArrayList nodes = new ArrayList();
		t.getAll(t.getRoot(), null, nodes);
		for (int i = 0; i < nodes.size(); i++)
		{
			Object o = nodes.get(i);
			makeLabelUnique(o);
			if (o instanceof Labelable)
				vertexLabels.put(((Labelable) o).getLabel(), o);
		}
	}

	protected void removeDuplicateTags(RootedTree t)
	{
		vertexLabels.clear();
		ArrayList nodes = new ArrayList();
		t.getAll(t.getRoot(), null, nodes);
		for (int i = 0; i < nodes.size(); i++)
		{
			Object o = nodes.get(i);
			if (o instanceof Labelable)
			{
				Labelable l = (Labelable) o;
				String s = l.getLabel();
				int index = s.lastIndexOf(sep);
				if (index == -1)
				{
					continue;
				} else
				{
					l.setLabel(s.substring(0, index));
				}
			}
		}
	}

	public Object getNodeForLabel(String s)
	{
		return vertexLabels.get(s);
	}

	public boolean isLabelSignificant(String s)
	{
		int index = s.lastIndexOf(UniqueLabeler.sep);
		if (index != 0)
		{
			return true;
		}

		if (index == 0)
		{
			return false;
		} else if (s.length() == 0)
			return false;
		else if (s.startsWith("inode"))
			return false;
		else
			return true;
	}
}
