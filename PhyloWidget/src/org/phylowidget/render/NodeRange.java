package org.phylowidget.render;

import org.andrewberman.sortedlist.ItemI;
import org.phylowidget.ui.PhyloNode;

/**
 * NodeRange is an implementation of ItemI, to be used by PhyloWidget
 * renderers for storing ranges within a SortedXYRangeList structure.
 * @author Greg
 *
 */
public final class NodeRange implements ItemI
{
	public TreeRenderer render;	
	public PhyloNode node;
	
	public int type=0;
	public static final int NODE = 0;
	public static final int LABEL = 1;

	public float loX, hiX, loY, hiY = 0;
	public static final int LO_X = 0;
	public static final int HI_X = 1;
	public static final int LO_Y = 2;
	public static final int HI_Y = 3;
	
	public float get(int what)
	{
		switch (what)
		{
			case LO_X:
				return loX;
			case HI_X:
				return hiX;
			case LO_Y:
				return loY;
			case HI_Y:
				return hiY;
			default:
				return -1;
		}
	}
}
