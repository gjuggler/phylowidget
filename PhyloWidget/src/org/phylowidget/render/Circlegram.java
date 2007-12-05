package org.phylowidget.render;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.phylowidget.PhyloWidget;
import org.phylowidget.ui.PhyloNode;

import processing.core.PApplet;

public class Circlegram extends Cladogram
{

	HashMap<PhyloNode, Float> sines = new HashMap<PhyloNode, Float>();
	HashMap<PhyloNode, Float> cosines = new HashMap<PhyloNode, Float>();

	@Override
	protected void setOptions()
	{
		super.setOptions();
		useBranchLengths = true;
	}
	
	@Override
	protected void layoutImpl()
	{
		super.layoutImpl();
	}

	@Override
	protected void leafPosition(PhyloNode n, int index)
	{
		float theta = (float) index / (float) (leaves.size()) * PApplet.TWO_PI;
		float sin = PApplet.sin(theta);
		float cos = PApplet.cos(theta);
		n.setUnscaledPosition(nodeXPosition(n), theta);
		sines.put(n, sin);
		cosines.put(n, cos);
	}

	@Override
	protected float branchPositions(PhyloNode n)
	{
		if (tree.isLeaf(n))
		{
			// If N is a leaf, then it's already been laid out.
			return n.getTargetY();
		} else
		{
			// If not:
			// theta should be the average of its children's thetas.
			List children = tree.getChildrenOf(n);
			float sum = 0;
			for (int i = 0; i < children.size(); i++)
			{
				PhyloNode child = (PhyloNode) children.get(i);
				sum += branchPositions(child);
			}
			float theta = (float) sum / (float) children.size();
			float r = 0;
			r = nodeXPosition(n);
			n.setUnscaledPosition(r, theta);
			sines.put(n, PApplet.sin(theta));
			cosines.put(n, PApplet.cos(theta));
			return theta;
		}
	}

	@Override
	protected void drawRecalc()
	{
		// super.drawRecalc();
		float circum = PApplet.PI;
		rowSize = rect.height / (circum * numRows);
//		textSize = Math.min(rect.width / gutterWidth * .5f, rowSize);
		textSize = rowSize * circum;
		dotWidth = textSize * PhyloWidget.ui.nodeSize;
		rad = dotWidth / 2;
		float gutter = gutterWidth * textSize;
		float minSide = Math.min(rect.width - 2 * gutter, rect.height - 2
				* gutter);
		scaleX = rowSize * numRows;
		scaleY = rowSize * numRows;
		dx = rect.width/2.0f;
		dy = rect.width/2.0f;
//		dx = 0;
//		dy = 0;
		dx += rect.getX();
		dy += rect.getY();
		textSize *= PhyloWidget.ui.textSize;
		dFont = (font.ascent() - font.descent()) * textSize / 2;
	}

	@Override
	protected void updateNode(PhyloNode n)
	{
		n.update();
		double r = n.getTargetX();
		double theta = n.getTargetY();
		float y = (float) (r * sines.get(n));
		float x = (float) (r * cosines.get(n));
		// System.out.println(n.getLabel() + " " +r + " "+theta);
		n.x = (float) (x * scaleX + dx);
		n.y = (float) (y * scaleY + dy);
	}

	protected void drawLineImpl(PhyloNode p, PhyloNode n)
	{
		canvas.noFill();
		canvas.ellipseMode(PApplet.RADIUS);
		float pr = (float) p.getTargetX();
		double loTheta = Math.min(p.getTargetY(), n.getTargetY());
		double hiTheta = Math.max(p.getTargetY(), n.getTargetY());
		canvas.arc((float) (dx), (float) (dy), (float) (scaleX * pr), (float) (scaleY * pr),
				(float) loTheta, (float) hiTheta);
//		canvas.ellipseMode(PApplet.CORNER);
		double r = p.getTargetX();
		double x = r * cosines.get(n);
		double y = r * sines.get(n);
		double x1 = dx + scaleX*x;
		double y1 = dy + scaleY*y;
		canvas.line((float)x1,(float)y1,n.x,n.y);
	}

	@Override
	protected void drawLabelImpl(PhyloNode n)
	{
		float theta = (float) n.getUnscaledY();
//		float theta = 0;
		float oldTextRot = PhyloWidget.ui.textRotation;
		PhyloWidget.ui.textRotation = theta / PApplet.TWO_PI * 360;
		PhyloWidget.usingNativeFonts = false;
		float oldR = n.getTargetX();
		super.drawLabelImpl(n);
		PhyloWidget.usingNativeFonts = true;
		PhyloWidget.ui.textRotation = oldTextRot;
	}
	
}
