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

import java.awt.Graphics2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.phylowidget.PhyloWidget;
import org.phylowidget.ui.PhyloNode;

import processing.core.PApplet;
import processing.core.PGraphicsJava2D;

public class Circlegram extends BasicTreeRenderer
{

	HashMap<PhyloNode, Float> sines = new HashMap<PhyloNode, Float>();
	HashMap<PhyloNode, Float> cosines = new HashMap<PhyloNode, Float>();

	@Override
	protected void setOptions()
	{
		super.setOptions();
	}

	@Override
	protected void leafPosition(PhyloNode n, int index)
	{
		float theta = (float) index / (float) (leaves.size()) * PApplet.TWO_PI;
		float sin = PApplet.sin(theta);
		float cos = PApplet.cos(theta);
		setRadius(n, nodeXPosition(n));
		setTheta(n, theta);
		sines.put(n, sin);
		cosines.put(n, cos);
	}

	float getTheta(PhyloNode n)
	{
		return n.getTargetY();
	}

	void setTheta(PhyloNode n, float theta)
	{
		n.setY(theta);
	}

	float getRadius(PhyloNode n)
	{
		return n.getTargetX();
	}

	void setRadius(PhyloNode n, float rad)
	{
		n.setX(rad);
	}

	@Override
	protected float branchPositions(PhyloNode n)
	{
		if (tree.isLeaf(n))
		{
			// If N is a leaf, then it's already been laid out.
			return getTheta(n);
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
			// n.setPosition(r, theta);
			setRadius(n, r);
			setTheta(n, theta);
			sines.put(n, PApplet.sin(theta));
			cosines.put(n, PApplet.cos(theta));
			return theta;
		}
	}

	@Override
	protected void recalc()
	{
		// super.drawRecalc();
		float circum = PApplet.PI;
		rowSize = rect.height / (circum * numRows);
//		rowSize = Math.min(5, rowSize);
		// textSize = Math.min(rect.width / gutterWidth * .5f, rowSize);
		textSize = rowSize * circum;
		dotWidth = textSize * PhyloWidget.ui.nodeSize;
		rad = dotWidth / 2;
		float gutter = biggestStringWidth * textSize;
		float minSide = Math.min(rect.width - 2 * gutter, rect.height - 2
				* gutter);
		scaleX = rowSize * numRows;
		scaleY = rowSize * numRows;
		dx = rect.width / 2.0f;
		dy = rect.width / 2.0f;
		// dx = 0;
		// dy = 0;
		dx += rect.getX();
		dy += rect.getY();
//		textSize *= PhyloWidget.ui.textSize;
		dFont = (font.ascent() - font.descent()) * textSize / 2;
	}

	@Override
	protected void layout()
	{
		super.layout();
		if (!needsLayout)
			return;
		needsLayout = false;
		/*
		 * Create a second set of noderanges if necessary.
		 */
		if (PhyloWidget.ui.useBranchLengths)
		{
			synchronized (list)
			{
//				list.clear();
//				nodesToRanges.clear();
				for (int i = 0; i < nodes.size(); i++)
				{
					PhyloNode n = (PhyloNode) nodes.get(i);
					NodeRange r = new NodeRange();
					r.node = n;
					r.render = this;
					float oldTheta = getTheta(n);
					setTheta(n,1);
					updateNode(n);
					r.loX = getX(n) - dotWidth / 2;
					float textHeight = (font.ascent() + font.descent()) * textSize;
					r.loY = getY(n) - textHeight / 2;
					r.hiY = getY(n) + textHeight / 2;
					float textWidth = (float) n.unitTextWidth * textSize;
					r.hiX = getX(n) + dotWidth / 2 + textWidth;
					setTheta(n,oldTheta);
					updateNode(n);
					list.insert(r, false);
				}
				list.sortFull();
			}
		}
	}

	@Override
	protected void updateNode(PhyloNode n)
	{
		if (mainRender)
			n.update();
		/*
		 * Store the real, scaled x and y values.
		 */
		n.setRealX(calcRealX(n));
		n.setRealY(calcRealY(n));

		/*
		 * Update the nodeRange.
		 */
		setRange(n,nodesToRanges.get(n));
	}
	
	void setRange(PhyloNode n, NodeRange r)
	{
//		NodeRange r = nodesToRanges.get(n);
		r.loX = getX(n) - dotWidth / 2;
		float textHeight = (font.ascent() + font.descent()) * textSize;
		r.loY = getY(n) - textHeight / 2;
		r.hiY = getY(n) + textHeight / 2;
		float textWidth = (float) n.unitTextWidth * textSize;
		r.hiX = getX(n) + dotWidth / 2 + textWidth;
	}

	@Override
	float calcRealX(PhyloNode n)
	{
		float r = n.getX();
		// float theta = n.getY();
		float x = r * PApplet.cos(getTheta(n));
		return (float) (x * scaleX + dx);
	}

	@Override
	float calcRealY(PhyloNode n)
	{
		float r = n.getX();
		// float theta = n.getY();
		float y = r * PApplet.sin(getTheta(n));
		return (float) (y * scaleY + dy);
	}

	protected void drawLineImpl(PhyloNode p, PhyloNode n)
	{
		canvas.noFill();
		canvas.ellipseMode(PApplet.RADIUS);
		float pr = (float) getRadius(p);
		double loTheta = Math.min(getTheta(p), getTheta(n));
		double hiTheta = Math.max(getTheta(p), getTheta(n));
		canvas.arc((float) (dx), (float) (dy), (float) (scaleX * pr),
				(float) (scaleY * pr), (float) loTheta, (float) hiTheta);
		// canvas.ellipseMode(PApplet.CORNER);
		double r = getRadius(p);
		double x = r * cosines.get(n);
		double y = r * sines.get(n);
		double x1 = dx + scaleX * x;
		double y1 = dy + scaleY * y;
		canvas.line((float) x1, (float) y1, getX(n), getY(n));
		if (tree.isLeaf(n))
		{
			double x2 = dx + scaleX * cosines.get(n);
			double y2 = dy + scaleY * sines.get(n);
			canvas.stroke(230);
			canvas.line(getX(n), getY(n), (float) x2, (float) y2);
		}
	}

	boolean between(float a, float lo, float hi)
	{
		if (a <= hi && a >= lo)
			return true;
		else
			return false;
	}

	@Override
	protected void drawLabelImpl(PhyloNode n)
	{
		// if (true == true)
		// return;
		float theta = (float) getTheta(n);
		int degrees = (int) (theta / PApplet.TWO_PI * 360);

		int textRotation = 0;
		boolean alignRight = false;
		if (between(degrees, 45, 90))
		{
			textRotation = 45;
		} else if (between(degrees, 90, 135))
		{
			textRotation = -45;
			alignRight = true;
		} else if (between(degrees, 135, 225))
		{
			alignRight = true;
		} else if (between(degrees, 225, 270))
		{
			textRotation = 45;
			alignRight = true;
		} else if (between(degrees, 270, 315))
		{
			textRotation = -45;
		}

		float oldR = getRadius(n);
		n.setPosition(1, getTheta(n));
		// updateNode(n);
		n.setRealX(calcRealX(n));
		n.setRealY(calcRealY(n));

		canvas.pushMatrix();
		canvas.translate(getX(n) + dotWidth / 2 + textSize / 3, getY(n));
		canvas.rotate(PApplet.radians(textRotation));

		PGraphicsJava2D pgj = (PGraphicsJava2D) canvas;
		Graphics2D g2 = pgj.g2;
		g2.setFont(font.font.deriveFont(textSize));
		g2.setPaint(style.foregroundColor);
		if (!alignRight)
			g2.drawString(n.getLabel(), 0, 0 + dFont);
		else
		{
			float width = (float) (n.unitTextWidth * textSize);
			g2.drawString(n.getLabel(), -width, 0 + dFont);
		}

		canvas.popMatrix();

		n.setPosition(oldR, getTheta(n));
		// updateNode(n);
		n.setRealX(calcRealX(n));
		n.setRealY(calcRealY(n));
	}
}
