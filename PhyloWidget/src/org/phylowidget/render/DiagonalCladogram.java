package org.phylowidget.render;

import java.util.ArrayList;

import org.phylowidget.tree.RenderNode;
import org.phylowidget.tree.TreeNode;

import processing.core.PApplet;

public class DiagonalCladogram extends Cladogram
{

	public DiagonalCladogram(PApplet p)
	{
		super(p);
		
		this.keepAspectRatio = true;
	}
	
	protected float branchPositions(RenderNode n)
	{
		if (n.isLeaf())
			// If N is a leaf, then it's already been laid out.
			return 0;
		/*
		 * Do the children first.
		 */
		ArrayList children = n.getChildren();
		for (int i = 0; i < children.size(); i++)
		{
			RenderNode child = (RenderNode) children.get(i);
			branchPositions(child);
		}
		/*
		 * Now, let's put on our thinking caps and try to lay ourselves out
		 * correctly.
		 */
		RenderNode loChild = (RenderNode) children.get(0);
		RenderNode hiChild = (RenderNode) children.get(children.size() - 1);
		/*
		 * Find the max depth of each child, and project where the "lower" child
		 * would be in the y axis if it were at that higher depth.
		 */
//		System.out.println(n.getName() + " " + loChild.getMaxDepth() + " " +hiChild.getMaxDepth());
		int mDepth = Math.max(loChild.getMaxDepth(),hiChild.getMaxDepth());
//		System.out.println("depth:"+n.getMaxDepth()+"  desc:"+n.getNumDescendants()+"  leaves:"+n.getNumLeaves());
		float loChildNewY = loChild.unscaledY + (mDepth - loChild.getMaxDepth())/maxDepth*leaves.size();
		float hiChildNewY = hiChild.unscaledY - (mDepth - hiChild.getMaxDepth())/maxDepth*leaves.size();
		n.unscaledY = (loChildNewY + hiChildNewY)/2;
//		n.unscaledX = 1 - (float)n.getNumLeaves() / ((float)leaves.size());
		n.unscaledX = 1 - (float)n.getMaxDepth() / (float)maxDepth;
		return 0;
	}

	protected void drawLine(RenderNode n)
	{
		if (n.getParent() != TreeNode.NULL_PARENT)
		{
			RenderNode parent = (RenderNode) n.getParent();
			canvas.line(n.x, n.y, parent.x, parent.y);
//			canvas.line(n.x - rad, n.y, parent.x, n.y);
//			float retreat = 0;
//			if (n.y < parent.y)
//				retreat = -rad;
//			else
//				retreat = rad;
//			canvas.line(parent.x, n.y, parent.x, parent.y + retreat);
		}
	}
	
}
