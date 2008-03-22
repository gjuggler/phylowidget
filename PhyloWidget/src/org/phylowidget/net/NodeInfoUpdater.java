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
package org.phylowidget.net;

import java.util.ArrayList;

import org.andrewberman.ui.unsorted.DelayedAction;
import org.andrewberman.ui.unsorted.JSCaller;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.ui.PhyloNode;

public class NodeInfoUpdater extends DelayedAction
{

	JSCaller caller = new JSCaller(PhyloWidget.p);
	RootedTree tree;
	PhyloNode node;
	String jsCall;

	public NodeInfoUpdater()
	{
		jsCall = "updateNode";
	}

	public void triggerUpdate(RootedTree t, PhyloNode n)
	{
		tree = t;
		node = n;
		trigger(50);
	}

	public void run()
	{
		String s = createNodeHTML();
		String cmd = jsCall;
		try {
			Object o = caller.getMember("PhyloWidget");
			caller.callWithObject(o, jsCall, s);
		} catch (Exception e)
		{
//			e.printStackTrace();
		}
	}

	String b(String s)
	{
		return "<b>" + s + "</b>";
	}

	String br()
	{
		return "<br/>";
	}

	ArrayList<String[]>props = new ArrayList<String[]>();
	
	String getUBioLink(PhyloNode n)
	{
		String s = "http://www.ubio.org/browser/search.php?search_all='"+n.getLabel()+"'";
		s = "<a href=\""+s+"\" target='_new'>"+n.getLabel()+"</a>";
		return s;
	}
	
	void put (String a, String b)
	{
		props.add(new String[]{a,b});
	}
	
	String createNodeHTML()
	{
		StringBuffer sb = new StringBuffer();
		props.clear();
		put("Name", node.getLabel());
		put("Branch Length",""+node.getBranchLength());
		put("Enclosed Leaves", ""+node.getNumLeaves());
		put("Depth to Root",node.getDepthToRoot()+"");
		put("UBio Link",getUBioLink(node));
		
		sb.append("<table>");
		
		for (String[] pair: props)
		{
			String key = pair[0];
			String val = pair[1];
			sb.append("<tr>");
			sb.append("<td class='key'>"+b(key+":")+"</td>");
			sb.append("<td class='val'>"+val+"</td>");
			sb.append("</tr>");
		}
		
		sb.append("</table>");
		return sb.toString();
	}

}
