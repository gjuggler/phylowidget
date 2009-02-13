package org.phylowidget.render;

import java.util.List;

import org.andrewberman.ui.UIUtils;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.PhyloNode;

import processing.core.PGraphics;

public class LayoutCladogram extends LayoutBase
{
	int numLeaves;
	float depthLeafRatio;
	
	public void layoutImpl()
	{
		numLeaves = leaves.length;
		float maxDepth = tree.getMaxDepthToLeaf(tree.getRoot());
		depthLeafRatio = maxDepth / numLeaves;
		
		depthLeafRatio *= context.config().branchScaling;
		
		int index = 0;
		for (PhyloNode leaf : leaves)
		{
			leaf.setTextAlign(PhyloNode.ALIGN_LEFT);
			leafPosition(leaf, index);
			index++;
		}

		branchPosition((PhyloNode) tree.getRoot());
	}

	@Override
	public void drawLine(PGraphics canvas, PhyloNode p, PhyloNode c)
	{
		if (UIUtils.isJava2D(canvas))
		{
		canvas.strokeCap(canvas.ROUND);
		canvas.strokeJoin(canvas.ROUND);
		}
		canvas.noFill();
		canvas.beginShape();
		canvas.vertex(p.getX(), p.getY());
		canvas.vertex(p.getX(), c.getY());
		canvas.vertex(c.getX(), c.getY());
		canvas.endShape();
		if (UIUtils.isJava2D(canvas))
		{
			canvas.strokeCap(canvas.ROUND);
		}
	}

	private float branchPosition(PhyloNode n)
	{
		setAngle(n,0);
		
		if (tree.isLeaf(n))
		{
			// If N is a leaf, then it's already been laid out.
			return n.getTargetY();
		} else
		{
			// If not:
			// Y coordinate should be the average of its children's heights
			List children = tree.getChildrenOf(n);
			float sum = 0;
			float count = 0;
			for (int i = 0; i < children.size(); i++)
			{
				PhyloNode child = (PhyloNode) children.get(i);
				sum += branchPosition(child);
				count++;
			}
			float yPos = (float) sum / (float) count;
			float xPos = 1;
			xPos = calcXPosition(n);
			setPosition(n,xPos, yPos);
			return yPos;
		}
	}

	private void leafPosition(PhyloNode n, int index)
	{
		/**
		 * Set the leaf position.
		 */
		float yPos = ((float) (index + .5f) / (float) (numLeaves));
		float xPos = calcXPosition(n);
		setPosition(n, xPos, yPos);
	}

	private float calcXPosition(PhyloNode n)
	{
		if (context.config().useBranchLengths)
		{
			if (tree.isRoot(n))
				return 0;
			float asdf = 0;
			if (tree.getMaxHeightToLeaf(tree.getRoot()) == 0)
			{
				System.out.println("Tree height is zero!");				
			}
			asdf = (float) tree.getHeightToRoot(n) / (float) tree.getMaxHeightToLeaf(tree.getRoot());
			return asdf * depthLeafRatio;
		} else
		{
			if (tree.isRoot(n))
				return 0;
			float md = 1f - (float) tree.getMaxDepthToLeaf(n) / (float) tree.getMaxDepthToLeaf(tree.getRoot());
			return md * depthLeafRatio;
		}
	}

}
