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
package org.phylowidget.render;

import org.andrewberman.sortedlist.ItemI;
import org.phylowidget.tree.PhyloNode;

/**
 * NodeRange is an implementation of ItemI, to be used by PhyloWidget
 * renderers for storing ranges within a SortedXYRangeList structure.
 * @author Greg
 *
 */
public final class NodeRange implements ItemI
{
	public BasicTreeRenderer render;
	public PhyloNode node;
	
	public int type=0;
	public static final int NODE = 0;
	public static final int LABEL = 1;

	public float loX, hiX, loY, hiY = 0;
	
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
	
	@Override
	public String toString()
	{
//		return super.toString();
		return node.getLabel()+" "+loX+" "+hiX+" "+loY+" "+hiY;
	}
}
