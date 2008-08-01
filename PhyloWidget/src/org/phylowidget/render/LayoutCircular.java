package org.phylowidget.render;

import java.util.HashMap;
import java.util.List;

import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.PhyloNode;

import processing.core.PGraphics;

public class LayoutCircular extends TreeLayout
{
	final static float STARTING_ANGLE = .75f * (float)Math.PI*2f;
	
	int numLeaves;
	float depthToLeaves;

	PhyloNode root;
	float rootX;
	float rootY;

	HashMap<PhyloNode, AngleRadius> nodeToAngleRadius = new HashMap<PhyloNode, AngleRadius>();

	public synchronized void layoutImpl()
	{
			numLeaves = leaves.length;
			curAngle = STARTING_ANGLE + PhyloWidget.cfg.layoutAngle / 360f * (float)Math.PI*2f;
			int index = 0;
			for (PhyloNode leaf : leaves)
			{
				leaf.setTextAlign(PhyloNode.ALIGN_LEFT);
				leafPosition(leaf, index);
				index++;
			}

			branchPosition((PhyloNode) tree.getRoot());
			root = (PhyloNode) tree.getRoot();
			rootX = ((PhyloNode) tree.getRoot()).getRealX();
			rootY = ((PhyloNode) tree.getRoot()).getRealY();
	}

	@Override
	public synchronized void drawLine(PGraphics canvas, PhyloNode p, PhyloNode c)
	{
		// Find the radius
		AngleRadius cAr = nodeToAngleRadius.get(c);
		AngleRadius pAr = nodeToAngleRadius.get(p);

		if (root == null)
			return;
		
		float rootX = root.getRealX();
		float rootY = root.getRealY();

		float cRad = getDistance(root, c);
		float pRad = getDistance(root, p);

		//		System.out.println(rootX+"  "+rootY);
		//		canvas.line(c.getRealX(),c.getRealY(),rootX+(float)Math.cos(cAr.angle)*pAr.radius*scaleX,rootY+(float)Math.sin(cAr.angle)*pAr.radius*scaleY);
		canvas.ellipseMode(PGraphics.RADIUS);
		canvas.noFill();
		//		System.out.println(pAr.radius*scaleX);
		
		float loAngle = loAngle(cAr.angle,pAr.angle);
		float hiAngle = hiAngle(cAr.angle,pAr.angle);
		canvas.arc(rootX, rootY, pRad, pRad, loAngle, hiAngle);

		if (!PhyloWidget.cfg.useBranchLengths || !tree.isLeaf(c))
		{
			canvas.strokeCap(canvas.ROUND);
//			canvas.strokeJoin(canvas.ROUND);
			canvas.line(c.getRealX(), c.getRealY(), rootX + (float) Math.cos(cAr.angle) * pRad, rootY
					+ (float) Math.sin(cAr.angle) * pRad);
		} else
		{
			// Draw the same line, but only up until the "true" leaf distance.
			float ratio = cAr.leafRadius / cAr.radius;
			canvas.strokeCap(canvas.ROUND);
//			canvas.strokeJoin(canvas.ROUND);
			canvas.line(rootX + (float) Math.cos(cAr.angle) * cAr.leafRadius * scaleX * drawScaleX, rootY
					+ (float) Math.sin(cAr.angle) * cAr.leafRadius * scaleY * drawScaleY, rootX
					+ (float) Math.cos(cAr.angle) * pRad, rootY + (float) Math.sin(cAr.angle) * pRad);
			
			// Draw a grayer line.
			canvas.strokeCap(canvas.SQUARE);
//			canvas.strokeJoin(canvas.SQUARE);
			canvas.stroke(200);
			canvas.line(rootX + (float) Math.cos(cAr.angle) * cAr.leafRadius * scaleX * drawScaleX, rootY
				+ (float) Math.sin(cAr.angle) * cAr.leafRadius * scaleY * drawScaleY, c.getRealX(),c.getRealY());
		}
	}

	private float getDistance(PhyloNode a, PhyloNode b)
	{
		float aX = a.getRealX();
		float aY = a.getRealY();
		float bX = b.getRealX();
		float bY = b.getRealY();

		return (float) Math.sqrt((bX - aX) * (bX - aX) + (bY - aY) * (bY - aY));
	}

	private float branchPosition(PhyloNode n)
	{
		if (tree.isLeaf(n))
		{
			// If N is a leaf, then it's already been laid out.
			float theta = nodeToAngleRadius.get(n).angle;
			//			setAngle(n,theta);
			return theta;
		} else
		{
			// If not:
			// theta should be the average of its children's thetas
			List children = tree.getChildrenOf(n);
			float sum = 0;
			float count = 0;
			for (int i = 0; i < children.size(); i++)
			{
				PhyloNode child = (PhyloNode) children.get(i);
				sum += branchPosition(child);
				count++;
			}
			// Find the radius.
			float radius = calcRadius(n);
			float theta = (float) sum / (float) count;

			// Convert radius and theta into x,y.
			float x = (float) Math.cos(theta) * radius;
			float y = (float) Math.sin(theta) * radius;

			setPosition(n, x, y);
			setAngle(n, theta);
			nodeToAngleRadius.put(n, new AngleRadius(theta, radius));
			return theta;
		}
	}

	/*
	 * Find the smallest angle between two angles.
	 */
	private float angleBetween(float a, float b)
	{
		a = a % TWOPI;
		b = b % TWOPI;
		
		if (b - a > PI)
			a += TWOPI;
		if (a - b > PI)
			b += TWOPI;
		
		return (a+b)/2f;
	}
	
	/*
	 * Find the lowest (i.e. most counter-clockwise) angle of two.
	 */
	private float loAngle(float a, float b)
	{
		a = a % TWOPI;
		b = b % TWOPI;
		
		if (b - a > PI)
			a += TWOPI;
		if (a - b > PI)
			b += TWOPI;
		
		return (float)Math.min(a,b);
	}
	/*
	 * Find the lowest (i.e. most counter-clockwise) angle of two.
	 */
	private float hiAngle(float a, float b)
	{
		a = a % TWOPI;
		b = b % TWOPI;
		
		if (b - a > PI)
			a += TWOPI;
		if (a - b > PI)
			b += TWOPI;
		
		return (float)Math.max(a,b);
	}
	
	static float PI = (float)Math.PI;
	static float TWOPI = (float)Math.PI*2;
	float curAngle;
	private void leafPosition(PhyloNode n, int index)
	{
		/**
		 * Set the leaf position.
		 */
		float theta = curAngle;
		curAngle += 1f / (float)numLeaves * (float)Math.PI*2f;
//		float theta = (float) index / (float) numLeaves * (float) Math.PI * 2f;
//		theta += startingAngle;
//		theta = theta % (float)Math.PI * 2f;
		float radius = 1;
		float leafRadius = 1;
		if (PhyloWidget.cfg.useBranchLengths)
			leafRadius = calcRadius(n);

		float yPos = (float) Math.sin(theta) * radius;
		float xPos = (float) Math.cos(theta) * radius;
		//		if (PhyloWidget.cfg.useBranchLengths)
		//		xPos = calcXPosition(n);
		setPosition(n, xPos, yPos);
		setAngle(n, theta);
		nodeToAngleRadius.put(n, new AngleRadius(theta, radius, leafRadius));
	}

	private float calcRadius(PhyloNode n)
	{
		if (PhyloWidget.cfg.useBranchLengths)
		{
			if (tree.isRoot(n))
				return 0;
			float asdf = (float) tree.getHeightToRoot(n) / (float) tree.getMaxHeightToLeaf(tree.getRoot());
			return asdf;
		} else
		{
			if (tree.isRoot(n))
				return 0;
			float md = 1f - (float) tree.getMaxDepthToLeaf(n) / (float) tree.getMaxDepthToLeaf(tree.getRoot());
			return md;
		}
	}

	class AngleRadius
	{
		public float angle;
		public float radius;
		public float leafRadius;

		public AngleRadius(float angle, float radius)
		{
			this.angle = angle;
			this.radius = radius;
		}

		public AngleRadius(float angle, float radius, float leafRadius)
		{
			this(angle, radius);
			this.leafRadius = leafRadius;
		}
	}

}
