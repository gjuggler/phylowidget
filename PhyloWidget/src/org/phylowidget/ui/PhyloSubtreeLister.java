package org.phylowidget.ui;

import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIEvent;
import org.andrewberman.ui.UIRectangle;
import org.andrewberman.ui.ifaces.UIListener;
import org.andrewberman.ui.unsorted.JSCaller;
import org.phylowidget.PhyloTree;
import org.phylowidget.render.NodeRange;
import org.phylowidget.tree.PhyloNode;

import processing.core.PApplet;

public class PhyloSubtreeLister extends PhyloContextMenu implements UIListener
{

	String delimiter = ";";
	String callback = "alert";
	String highlightColor = "blue";
	boolean highlight = true;
	JSCaller jsCaller;

	public PhyloSubtreeLister(PApplet p)
	{
		super(p);
		addListener(this);

		jsCaller = new JSCaller(p);
	}

	public void setHighlightColor(String s)
	{
		this.highlightColor = s;
	}
	
	public void setHighlight(boolean b)
	{
		this.highlight = b;
	}
	
	public void setDelimiter(String s)
	{
		this.delimiter = s;
	}

	public void setCallback(String s)
	{
		this.callback = s;
	}

	public void open(NodeRange r)
	{
		super.open(r);
		if (curNodeRange == null)
			return;
		PhyloNode n = curNodeRange.node;

		List<PhyloNode> leaves = n.getTree().getAllLeaves(n);
		String s = join(delimiter, leaves);
		System.out.println(s);

		try
		{
			jsCaller.call(callback, s);
		} catch (Exception e)
		{
			System.out.println(e.getLocalizedMessage());
		}

		if (highlight)
		{
			PhyloTree t = n.getTree();
			
			List<PhyloNode> nodes = t.getAllNodes(t.getRoot());
			// Clear colors for the whole tree.
			for (PhyloNode n2 : nodes)
			{
				n2.setAnnotation("ncol", null);
				n2.setAnnotation("bcol", null);
			}
			// Set the color for the whole tree.
			nodes = t.getAllNodes(n);
			for (PhyloNode n2 : nodes)
			{
				if (n2 != n)
				{
					n2.setAnnotation("bcol", highlightColor);
				}
				n2.setAnnotation("ncol", highlightColor);
			}
		}
		
		close();
	}

	public void uiEvent(UIEvent e)
	{

	}

	public static String join(String delimiter, String... array)
	{
		StringBuffer buffer = new StringBuffer();
		int len = array.length;
		for (int i = 0; i < len; i++)
		{
			buffer.append(array[i]);
			if (i < len - 1)
				buffer.append(delimiter);
		}
		return buffer.toString();
	}

	public static String join(String delimiter, Collection s)
	{
		StringBuffer buffer = new StringBuffer();
		Iterator iter = s.iterator();
		while (iter.hasNext())
		{
			buffer.append(iter.next().toString());
			if (iter.hasNext())
			{
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}

}
