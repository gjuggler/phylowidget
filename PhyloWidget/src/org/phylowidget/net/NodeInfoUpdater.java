package org.phylowidget.net;

import java.util.ArrayList;

import org.andrewberman.unsorted.DelayedAction;
import org.andrewberman.unsorted.JSObjectCrap;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.ui.PhyloNode;

public class NodeInfoUpdater extends DelayedAction
{

	RootedTree tree;
	PhyloNode node;
	String jsCall;

	public NodeInfoUpdater()
	{
		jsCall = PhyloWidget.ui.nodeJavascript;
	}

	public void triggerUpdate(RootedTree t, PhyloNode n)
	{
		tree = t;
		node = n;
		trigger(50);
	}

	public void run()
	{
//		System.out.println("Update " + System.currentTimeMillis());
		String s = createNodeHTML();
		String cmd = jsCall;
		JSObjectCrap.reflectJS(cmd, s);
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
