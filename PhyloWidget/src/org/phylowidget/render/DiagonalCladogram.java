package org.phylowidget.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.phylowidget.oldtree.TreeNode;
import org.phylowidget.tree.PhyloNode;

import processing.core.PApplet;

public class DiagonalCladogram extends Cladogram
{

	public DiagonalCladogram(PApplet p)
	{
		super(p);

		this.keepAspectRatio = true;
		this.useWeightedEdges = false;
		this.dotMult = 0.25f;
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
		PhyloNode loChild = (PhyloNode) children.get(0);
		PhyloNode hiChild = (PhyloNode) children
				.get(children.size() - 1);
		/*
		 * Find the max depth of each child, and project where the "lower" child
		 * would be in the y axis if it were at that higher depth.
		 */
		float stepSize = 1f / (leaves.size());
		float loLeaves = tree.getNumLeaves(loChild);
		float hiLeaves = tree.getNumLeaves(hiChild);
		float mLeaves = Math.max(loLeaves, hiLeaves);
//		System.out.println("md:" + mLeaves);
		float loChildNewY = loChild.unscaledY + (mLeaves - loLeaves) * stepSize
				/ 2;
		float hiChildNewY = hiChild.unscaledY - (mLeaves - hiLeaves) * stepSize
				/ 2;
		n.unscaledY = (loChildNewY + hiChildNewY) / 2;
		n.unscaledX = 1 - (float)(tree.getNumLeaves(n) - 1)
				/ (float)(leaves.size());
		// n.unscaledX = (float)(n.numEnclosedLeaves / (float)leaves.size();
		// n.unscaledX = 1 - (float)n.getMaxDepth() / (float)maxDepth / 2f;
		return 0;
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
			Collections.sort(list);
			int index = list.indexOf(n);
			if (list.size() > 2 && index != 0
					&& index != list.size() - 1)
			{
				/*
				 * If we get here, then this node is in the middle of a
				 * polytomy. Let's do something interesting, a la
				 * http://www.slipperorchids.info/taxonomy/cladogram.jpg
				 */
				// First, find the max number of leaves for any of the child
				// nodes.
				 PhyloNode withMostLeaves = n;
				for (int i = 0; i < list.size(); i++)
				{
					PhyloNode child = (PhyloNode) list.get(i);
					if (child.numEnclosedLeaves > withMostLeaves.numEnclosedLeaves)
						withMostLeaves = child;
				}
				canvas.line(n.x,n.y, withMostLeaves.x, parent.y);
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
