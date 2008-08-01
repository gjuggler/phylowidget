package org.phylowidget.render;

import java.util.Collections;
import java.util.List;

import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;

import processing.core.PGraphics;

public class LayoutDiagonal extends TreeLayout
{
	int numLeaves;
	
	public void layoutImpl()
	{
		numLeaves = leaves.length;
		
		int index = 0;
		for (PhyloNode leaf : leaves)
		{
			leafPosition(leaf,index);
			index++;
		}
		
		branchPosition((PhyloNode)tree.getRoot());
	}
	
	@Override
	public void drawLine(PGraphics canvas, PhyloNode p, PhyloNode c)
	{
		canvas.line(c.getRealX(), c.getRealY(), p.getRealX(),p.getRealY());
	}
	
	protected float branchPosition(PhyloNode n)
	{
		setAngle(n,0);
		
		if (tree.isLeaf(n))
			// If N is a leaf, then it's already been laid out.
			return 0;
		/*
		 * Do the children first.
		 */
		List<PhyloNode> children = tree.getChildrenOf(n);
		for (int i=0; i < children.size(); i++)
		{
			PhyloNode child = children.get(i);
			if (!tree.shouldKeep(child))
			{
				children.remove(i);
				i--;
			}
		}
		for (int i = 0; i < children.size(); i++)
		{
			PhyloNode child = (PhyloNode) children.get(i);
			branchPosition(child);
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
		float stepSize = 1f / (numLeaves);
		float loLeaves = tree.getNumEnclosedLeaves(loChild);
		float hiLeaves = tree.getNumEnclosedLeaves(hiChild);
//		float loLeaves = getRealEnclosedLeaves(loChild);
//		float hiLeaves = getRealEnclosedLeaves(hiChild);
		float mLeaves = Math.max(loLeaves, hiLeaves);
		// System.out.println("md:" + mLeaves);
		float loChildNewY = loChild.getTargetY() + (mLeaves - loLeaves)
				* stepSize / 2;
		float hiChildNewY = hiChild.getTargetY() - (mLeaves - hiLeaves)
				* stepSize / 2;
		float unscaledY = (loChildNewY + hiChildNewY) / 2;
		float unscaledX = calcXPosition(n);
		setPosition(n,unscaledX, unscaledY);
		return 0;
	}
	
	protected int getRealEnclosedLeaves(PhyloNode n)
	{
		int numEnclosed = tree.getNumEnclosedLeaves(n);
		int orig = numEnclosed;
		List<PhyloNode> children = tree.getChildrenOf(n);
		for (PhyloNode child : children)
		{
			if (!tree.shouldKeep(child))
				numEnclosed -= tree.getNumEnclosedLeaves(child);
		}
//		System.out.println(n.getLabel()+"  "+numEnclosed);
		return numEnclosed;
	}
	
	private void leafPosition(PhyloNode n, int index)
	{
		/**
		 * Set the leaf position.
		 */
		float yPos = ((float) (index + .5f) / (float) (numLeaves));
		float xPos = 1;
//		if (PhyloWidget.cfg.useBranchLengths)
			xPos = calcXPosition(n);
		setPosition(n,xPos, yPos);
	}
	
	protected float calcXPosition(PhyloNode n)
	{
//		System.out.println(n.getLabel()+"  "+getRealEnclosedLeaves(n));
		float a = xPosForNumEnclosedLeaves(getRealEnclosedLeaves(n));
//		float b = (float) (tree.getBranchLength(n) / tree
//				.getMaxHeightToLeaf(tree.getRoot()));
		return a / 2;
	}

	float xPosForNumEnclosedLeaves(int numLeaves)
	{
		float asdf =  1 - (float) (numLeaves - 1) / (float) (leaves.length);
		return asdf;
	}
}
