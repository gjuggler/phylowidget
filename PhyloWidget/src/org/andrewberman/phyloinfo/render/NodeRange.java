package org.andrewberman.phyloinfo.render;

import org.andrewberman.phyloinfo.tree.TreeNode;
import org.andrewberman.util.ItemI;

/**
 * NodeRange is an implementation of ItemI, to be used by PhyloWidget
 * renderers for storing ranges within a SortedXYRangeList structure.
 * @author Greg
 *
 */
public final class NodeRange implements ItemI
{
	public TreeRenderer render;	
	public TreeNode node;
	
	public int type=0;
	public static final int NODE = 0;
	public static final int LABEL = 1;
//	public static final int LINE_UP = 2;
//	public static final int LINE_ACROSS = 3;

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
