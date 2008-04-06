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

import java.util.Collections;
import java.util.List;

import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;

import processing.core.PApplet;

public class DiagonalCladogram extends BasicTreeRenderer
{

	public DiagonalCladogram()
	{
		super();
	}

	protected void setOptions()
	{
	}

	@Override
	public float getBranchLengthScaling()
	{
		return 1;
	}
	
	protected float branchPositions(PhyloNode n)
	{
		if (tree.isLeaf(n))
			// If N is a leaf, then it's already been laid out.
			return 0;
		/*
		 * Do the children first.
		 */
		List children = tree.getChildrenOf(n);
		for (int i = 0; i < children.size(); i++)
		{
			PhyloNode child = (PhyloNode) children.get(i);
			branchPositions(child);
		}
		Collections.sort(children);
		/*
		 * Now, let's put on our thinking caps and try to lay ourselves out
		 * correctly.
		 */
		PhyloNode loChild = (PhyloNode) Collections.min(children);
		PhyloNode hiChild = (PhyloNode) Collections.max(children);
		/*
		 * Find the max depth of each child, and project where the "lower" child
		 * would be in the y axis if it were at that higher depth.
		 */
		float stepSize = 1f / (leaves.length);
		float loLeaves = tree.getNumEnclosedLeaves(loChild);
		float hiLeaves = tree.getNumEnclosedLeaves(hiChild);
		float mLeaves = Math.max(loLeaves, hiLeaves);
		// System.out.println("md:" + mLeaves);
		float loChildNewY = loChild.getTargetY() + (mLeaves - loLeaves)
				* stepSize / 2;
		float hiChildNewY = hiChild.getTargetY() - (mLeaves - hiLeaves)
				* stepSize / 2;
		float unscaledY = (loChildNewY + hiChildNewY) / 2;
		float unscaledX = nodeXPosition(n);
		n.setPosition(unscaledX, unscaledY);
		return 0;
	}

	protected float nodeXPosition(PhyloNode n)
	{
		float a = xPosForNumEnclosedLeaves(tree.getNumEnclosedLeaves(n));
		float b = (float) (tree.getBranchLength(n) / tree.getMaxHeightToLeaf(tree.getRoot()));
		return a;
	}

	float xPosForNumEnclosedLeaves(int numLeaves)
	{
		return 1 - (float) (numLeaves - 1) / (float) (leaves.length);
	}

	protected void layout()
	{
		super.layout();
		numCols = numRows / 2;
	}

	protected void drawLineImpl(PhyloNode p, PhyloNode n)
	{
		List list = tree.getChildrenOf(p);
		int index = list.indexOf(n);
		if (index != 0 && index != list.size() - 1)
		{
			/*
			 * This block is only seen by nodes that are "stuck in the
			 * middle" of a polytomy.Maybe we should we do something a la:
			 * 
			 * http://www.slipperorchids.info/taxonomy/cladogram.jpg
			 * 
			 * I tried this already, but such solutions don't tend to scale
			 * up well with large polytomies.
			 */
		}
		float retreatX = getNodeRadius() / 2f;
		float retreatY = getNodeRadius() / 2f;
		if (getY(p) > getY(n))
			retreatY = -retreatY;
		canvas.line(getX(n), getY(n),  getX(p) + retreatX, getY(p) + retreatY);
	}
}
