package org.phylowidget.render;

import java.util.List;

import org.andrewberman.ui.UIUtils;
import org.phylowidget.PWPlatform;
import org.phylowidget.PhyloTree;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.PhyloNode;

import processing.core.PConstants;
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

	public void drawSquareLine(PGraphics canvas, PhyloNode p, PhyloNode c)
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

	public void drawBezierLine(PGraphics canvas, PhyloNode p, PhyloNode c)
	{
		if (UIUtils.isJava2D(canvas))
		{
			canvas.strokeCap(canvas.ROUND);
			canvas.strokeJoin(canvas.ROUND);
		}
		canvas.noFill();

		PhyloNode nearestChild = c;
		PhyloTree t = p.getTree();
		List<PhyloNode> children = t.getChildrenOf(p);
		for (PhyloNode child : children)
		{
			if (child.getX() < nearestChild.getX())
			{
				nearestChild = child;
			}
		}

		float w = (nearestChild.getX() - p.getX());
		float curveFraction = 1f;
		float wouldBeDx = w * curveFraction;
		float roundOffX = 0;
		roundOffX = wouldBeDx;

		canvas.noFill();
		canvas.beginShape();
		canvas.vertex(p.getX(), p.getY()); // Parent point.
		canvas.bezierVertex(p.getX() + roundOffX, p.getY(), p.getX(), c.getY(), p.getX() + roundOffX, c.getY());
		canvas.vertex(c.getX(), c.getY());
		canvas.endShape();

		if (UIUtils.isJava2D(canvas))
		{
			canvas.strokeCap(canvas.ROUND);
		}
	}

	public void drawLine(PGraphics canvas, PhyloNode p, PhyloNode c)
	{
		char ch = PWPlatform.getInstance().getThisAppContext().config().lineStyle.charAt(0);
		switch (ch)
		{
			case ('r'): // R is for round.
				drawCurvedLine(canvas, p, c);
				break;
			case ('b'): // B is for bezier.
				drawBezierLine(canvas, p, c);
				break;
			case ('s'): // S is for square.
			default:
				drawSquareLine(canvas, p, c);
				break;
		}
	}

	public void drawCurvedLine(PGraphics canvas, PhyloNode p, PhyloNode c)
	{
		if (UIUtils.isJava2D(canvas))
		{
			canvas.strokeCap(canvas.ROUND);
			canvas.strokeJoin(canvas.ROUND);
		}
		int oldMode = canvas.ellipseMode;

		// Find the left-most child.
		PhyloNode nearestChild = c;

		boolean findNearestChild = true;
		if (findNearestChild)
		{
			PhyloTree t = p.getTree();
			List<PhyloNode> children = t.getChildrenOf(p);
			for (PhyloNode child : children)
			{
				if (child.getX() < nearestChild.getX())
				{
					nearestChild = child;
				}
			}
		}

		// Find the "ideal" curve amount for each direction.
		float w = (nearestChild.getX() - p.getX());
		float h = (c.getY() - p.getY());

		float absH = Math.abs(h);

		float curveFraction = 0.8f;
		float wouldBeDx = w * curveFraction;
		float wouldBeDy = absH * (curveFraction);
		// We want the curve to be "square", so use the minimum curve as the true value.

		float roundOffX = 0;
		float roundOffY = 0;

		//		float max_dist = 10;
		//		if (wouldBeDy > max_dist) wouldBeDy = max_dist;
		//		if (wouldBeDx > max_dist) wouldBeDx = max_dist;
		boolean keepSquare = true;
		if (keepSquare)
		{
			if (wouldBeDx > wouldBeDy)
				wouldBeDx = wouldBeDy;
			if (wouldBeDy > wouldBeDx)
				wouldBeDy = wouldBeDx;
		}

		roundOffX = wouldBeDx;
		roundOffY = wouldBeDy;
		// reverse Y direction if < 0.
		roundOffY = (h > 0 ? roundOffY : -roundOffY);

		canvas.noFill();
		canvas.beginShape();
		canvas.vertex(p.getX(), p.getY()); // Parent point.
		canvas.vertex(p.getX(), p.getY() + (h - roundOffY)); // Partly down the vertical line.
		canvas.endShape();

		canvas.ellipseMode(PConstants.CENTER);
		float pi = PConstants.PI;
		if (h > 0)
		{
			canvas.arc(p.getX() + roundOffX, p.getY() + (h - roundOffY), roundOffX * 2, (roundOffY) * 2, pi / 2, pi);
		} else
		{
			canvas.arc(p.getX() + roundOffX, p.getY() + (h - roundOffY), roundOffX * 2, Math.abs(roundOffY) * 2, pi,
				3 * pi / 2);
		}
		canvas.beginShape();
		canvas.vertex(p.getX() + roundOffX, c.getY()); // Mid-way on horizontal line.
		canvas.vertex(c.getX(), c.getY());
		canvas.endShape();

		canvas.ellipseMode(oldMode);
		if (UIUtils.isJava2D(canvas))
		{
			canvas.strokeCap(canvas.ROUND);
		}
	}

	private float branchPosition(PhyloNode n)
	{
		setAngle(n, 0);

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
			setPosition(n, xPos, yPos);
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
