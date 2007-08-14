package org.phylowidget.render;

import java.util.Collections;
import java.util.List;

import org.phylowidget.ui.PhyloNode;

import processing.core.PApplet;

public class DiagonalCladogram extends Cladogram
{

	public DiagonalCladogram(PApplet p)
	{
		super(p);
		DiagonalCladogram.dotMult = 0.25f;
	}

	protected void setOptions()
	{
		keepAspectRatio = true;
		useBranchLengths = false;
	}
	
	protected float branchPositions(PhyloNode n)
	{
		if (tree.isLeaf(n))
			// If N is a leaf, then it's already been laid out.
			return 0;
		/*
		 * Do the children first.
		 */
		List children = tree.childrenOf(n);
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
		PhyloNode loChild = (PhyloNode) getMinNode(children);
		PhyloNode hiChild = (PhyloNode) getMaxNode(children);
		/*
		 * Find the max depth of each child, and project where the "lower" child
		 * would be in the y axis if it were at that higher depth.
		 */
		float stepSize = 1f / (leaves.size());
		float loLeaves = tree.numEnclosedLeaves(loChild);
		float hiLeaves = tree.numEnclosedLeaves(hiChild);
		float mLeaves = Math.max(loLeaves, hiLeaves);
		// System.out.println("md:" + mLeaves);
		float loChildNewY = loChild.getTargetY() + (mLeaves - loLeaves)
				* stepSize / 2;
		float hiChildNewY = hiChild.getTargetY() - (mLeaves - hiLeaves)
				* stepSize / 2;
		float unscaledY = (loChildNewY + hiChildNewY) / 2;
		float unscaledX = nodeXPosition(n);
		n.setUnscaledPosition(unscaledX, unscaledY);
		// n.unscaledX = (float)(n.numEnclosedLeaves / (float)leaves.size();
		// n.unscaledX = 1 - (float)n.getMaxDepth() / (float)maxDepth / 2f;
		return 0;
	}

	protected float nodeXPosition(PhyloNode n)
	{
		return 1 - (float) (tree.numEnclosedLeaves(n) - 1)
		/ (float) (leaves.size());
	}
	
	Object getMinNode(List l)
	{
		float minY = Integer.MAX_VALUE;
		int minIndex = 0;
		for (int i = 0; i < l.size(); i++)
		{
			PhyloNode child = (PhyloNode) l.get(i);
			if (child.getTargetY() < minY)
			{
				minY = child.getTargetY();
				minIndex = i;
			}
		}
		return l.get(minIndex);
	}
	
	Object getMaxNode(List l)
	{
		float maxY = Integer.MIN_VALUE;
		int maxIndex = 0;
		for (int i = 0; i < l.size(); i++)
		{
			PhyloNode child = (PhyloNode) l.get(i);
			if (child.getTargetY() > maxY)
			{
				maxY = child.getTargetY();
				maxIndex = i;
			}
		}
		return l.get(maxIndex);
	}
	
	protected void doTheLayout()
	{
		super.doTheLayout();
		numCols = numRows / 2;
	}

	protected void drawLine(PhyloNode n)
	{
		if (tree.parentOf(n) != null)
		{
			PhyloNode parent = (PhyloNode) tree.parentOf(n);
			List list = tree.childrenOf(parent);
			// Collections.sort(list);
			// int index = list.indexOf(n);
			float minY = n.getTargetY(), maxY = n.getTargetY();
			for (int i = 0; i < list.size(); i++)
			{
				PhyloNode child = (PhyloNode) list.get(i);
				if (child.getTargetY() > maxY)
					maxY = child.getTargetY();
				else if (child.getTargetY() < minY)
					minY = child.getTargetY();
			}
			if (n.getTargetY() != maxY && n.getTargetY() != minY)
			{
				/*
				 * If we get here, then this node is in the middle of a
				 * polytomy. Let's do something interesting, a la
				 * http://www.slipperorchids.info/taxonomy/cladogram.jpg
				 */
				// First, find the max number of leaves for any of the child
				// nodes.
//				System.out.println(n);
				PhyloNode withMostLeaves = n;
				for (int i = 0; i < list.size(); i++)
				{
					PhyloNode child = (PhyloNode) list.get(i);
					if (tree.numEnclosedLeaves(child) > tree
							.numEnclosedLeaves(n))
						withMostLeaves = child;
				}
//				System.out.println("   ml:" + withMostLeaves);
				canvas.line(n.x, n.y, withMostLeaves.x, parent.y);
				canvas.line(withMostLeaves.x, parent.y, parent.x, parent.y);
			} else
			{
				canvas.line(n.x, n.y, parent.x, parent.y);
			}
			// canvas.line(n.x - rad, n.y, parent.x, n.y);
			// float retreat = 0;
			// if (n.y < parent.y)
			// retreat = -rad;
			// else
			// retreat = rad;
			// canvas.line(parent.x, n.y, parent.x, parent.y + retreat);
		}
	}
}
