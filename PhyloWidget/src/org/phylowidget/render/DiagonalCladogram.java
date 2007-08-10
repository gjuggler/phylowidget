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
		this.dotMult = 0.25f;
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
		float stepSize = 1f / (leaves.size());
		float loLeaves = loChild.getNumLeaves()-1;
		float hiLeaves = hiChild.getNumLeaves()-1;
		float mLeaves = Math.max(loLeaves,hiLeaves);
		System.out.println("md:"+mLeaves);
//		System.out.println("LOW usy:"+loChild.unscaledY+"   md:"+loChild.getMaxDepth());
//		System.out.println("HI usy:"+hiChild.unscaledY+"   md:"+hiChild.getMaxDepth());
		float loChildNewY = loChild.unscaledY + (mLeaves - loLeaves)*stepSize/2;
		float hiChildNewY = hiChild.unscaledY - (mLeaves - hiLeaves)*stepSize/2;
		n.unscaledY = (loChildNewY + hiChildNewY)/2;
		n.unscaledX = 1 - ((float)n.getNumLeaves()-1) / ((float)leaves.size());
//		n.unscaledX = 1 - (float)n.getMaxDepth() / (float)maxDepth / 2f;
		return 0;
	}

	protected void doTheLayout()
	{
		super.doTheLayout();
		numCols = numRows / 2;
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
