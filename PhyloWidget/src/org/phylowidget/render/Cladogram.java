package org.phylowidget.render;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.andrewberman.ui.FontLoader;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.TextField;
import org.andrewberman.ui.UIUtils;
import org.phylowidget.PhyloWidget;
import org.phylowidget.ui.NodeTraverser;
import org.phylowidget.ui.PhyloNode;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;

public class Cladogram extends AbstractTreeRenderer
{
	protected double scaleX, scaleY, dx, dy;
	/**
	 * Fontmetrics for calculating text widths.
	 */
	FontMetrics fm;
	/**
	 * These variables are set in the calculateSizes() method during every round
	 * of rendering. Very important!
	 */
	protected float rowSize, colSize, numRows, numCols;

	/**
	 * Size of the text, as a multiplier relative to normal size.
	 */
	// public float textSize = 1f;
	protected float dFont;
	/**
	 * Radius of the node ellipses.
	 */
	protected float dotWidth, rad;
	/**
	 * Width of the node label gutter.
	 */
	protected float gutterWidth = 0;
	protected String biggestString;

	protected Point ptemp = new Point(0, 0);
	protected Point ptemp2 = new Point(0, 0);

	/**
	 * Transformations required to go from the stored position to the actual
	 * position. Should be set at the beginning of each draw.
	 */
	// protected float scaleX, scaleY = 0;
	// protected float dx, dy = 0;
	/**
	 * If true, this tree will maintain its "proper" aspect ratio, meaning it
	 * won't stretch to completely fill its enclosing rectangle.
	 */
//	public boolean keepAspectRatio;

	public Cladogram()
	{
		super();
	}

	protected void setOptions()
	{
//		keepAspectRatio = true;
	}

	protected void layoutImpl()
	{
		/*
		 * ASSUMPTION: the leaves ArrayList contains a "sorted" view of the
		 * tree's leaves, i.e. in the correct ordering from top to bottom.
		 */
		gutterWidth = 0;
		biggestString = "";
		if (UIUtils.isJava2D(canvas))
			fm = UIUtils.getMetrics(canvas, font.font, 100);
		/*
		 * Set the leaf positions.
		 */
		for (int i = 0; i < leaves.size(); i++)
		{
			PhyloNode n = (PhyloNode) leaves.get(i);
			/*
			 * Set the leaf position of this node.
			 */
			leafPosition(n, i);
			/**
			 * Find the width of this node's label.
			 */
			float width = 0;// spaceWidth;
			if (UIUtils.isJava2D(canvas))
			{
				// width = fm.stringWidth(n.getName());
				Graphics2D g2 = ((PGraphicsJava2D) canvas).g2;
				width = (float) fm.getStringBounds(n.getLabel(), g2).getWidth() / 100f;
			} else
			{
				char[] chars = n.getLabel().toCharArray();

				for (int j = 0; j < chars.length; j++)
				{
					width += font.width(chars[j]);
				}
			}
			n.unitTextWidth = width;
			if (width > gutterWidth)
			{
				gutterWidth = width;
				biggestString = n.getLabel();
			}
		}
		/*
		 * Special case: if biggestString is 0 length, we'll fudge it.
		 */
		if (biggestString.length() == 0)
		{
			biggestString = "P";
			gutterWidth = fm.stringWidth(biggestString) / 100f;
		}
		/*
		 * Now, set the branch positions.
		 */
		branchPositions((PhyloNode) tree.getRoot());
		/*
		 * Set the numRows and numCols variables.
		 */
		numRows = leaves.size();
		numCols = tree.getMaxDepthToLeaf(tree.getRoot());
	}

	protected void leafPosition(PhyloNode n, int index)
	{
		/**
		 * Set the leaf position.
		 */
		float yPos = ((float) (index + .5f) / (float) (leaves.size()));
		float xPos = 1;
		if (PhyloWidget.ui.useBranchLengths)
			xPos = nodeXPosition(n);
		n.setUnscaledPosition(xPos, yPos);
	}

	protected float nodeXPosition(PhyloNode n)
	{
		if (PhyloWidget.ui.useBranchLengths)
		{
			if (tree.getParentOf(n) == null)
				return 0;
			else
			{
				float asdf = (float) tree.getHeightToRoot(n)
						/ (float) tree.getMaxHeightToLeaf(tree.getRoot());
				return asdf;
			}
		} else
		{
			float md = 1 - (float) tree.getMaxDepthToLeaf(n)
					/ (float) tree.getMaxDepthToLeaf(tree.getRoot());
			return md;
		}
	}

//	public float branchLengthPerPixel()
//	{
//		return (float) (tree.getMaxHeightToLeaf(tree.getRoot()) / scaleX);
//	}
//
//	public float branchLengthForXPos(Object vertex, float xPos)
//	{
//		PhyloNode parent = (PhyloNode) tree.getParentOf(vertex);
//		float parentX = parent.x;
//		float dx = xPos - parentX;
//		return dx * branchLengthPerPixel();
//	}

	/**
	 * A recursive function to set the positions for all the branches. Only
	 * works if the leaf positions have already been set.
	 * 
	 * @param n
	 * @return
	 */
	protected float branchPositions(PhyloNode n)
	{
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
			for (int i = 0; i < children.size(); i++)
			{
				PhyloNode child = (PhyloNode) children.get(i);
				sum += branchPositions(child);
			}
			float yPos = (float) sum / (float) children.size();
			float xPos = 0;
			xPos = nodeXPosition(n);
			n.setUnscaledPosition(xPos, yPos);
			return yPos;
		}
	}
	
	@Override
	public void render(PGraphics canvas, float x, float y, float w, float h,
			boolean mainRender)
	{
//		if (!mainRender)
//		{
//			double[] oldVals = {scaleX,scaleY,dx,dy};
//			super.render(canvas, x, y, w, h, mainRender);
//			scaleX = oldVals[0];
//			scaleY = oldVals[1];
//			dx = oldVals[2];
//			dy = oldVals[3];
//		} else
			super.render(canvas, x, y, w, h, mainRender);
	}
	
	protected void drawRecalc()
	{
		/*
		 * Figure out the ideal row size.
		 */
		float overhang = gutterWidth
				* (float) Math
						.sin(PApplet.radians(PhyloWidget.ui.textRotation));
		float absOverhang = Math.abs(overhang);
		rowSize = rect.height / (numRows + absOverhang);
		textSize = Math.min(rect.width / gutterWidth * .5f, rowSize);
		colSize = rect.width / (numCols + 1 + gutterWidth);
//		System.out.println("height:"+rect.width);
		if (!PhyloWidget.ui.fitTreeToWindow)
		{
			rowSize = colSize = Math.min(rowSize, colSize);
			scaleX = colSize * numCols;
			scaleY = rowSize * numRows;
		}
		textSize = Math.min(rowSize, textSize);
		if (PhyloWidget.ui.fitTreeToWindow)
		{
			scaleX = rect.width - gutterWidth*textSize - 10;
			scaleY = rect.height - absOverhang*textSize;
		}
//		System.out.println(scaleX);
		dotWidth = textSize * PhyloWidget.ui.nodeSize;
		rad = dotWidth / 2;
		if (numRows == 1)
			scaleX = 0;
		dx = (rect.width - scaleX - gutterWidth * textSize - textSize / 2) / 2;
		dy = (rect.height - scaleY - overhang * textSize) / 2;
		dx += rect.getX();
		dy += rect.getY();
		/*
		 * Multiply the textSize by the user-specified scaling factor.
		 */
		textSize *= PhyloWidget.ui.textSize;
		dFont = (font.ascent() - font.descent()) * textSize / 2;
	}

	protected void constrainAspectRatio()
	{
		
	}

	@Override
	protected void updateNode(PhyloNode n)
	{
		n.update();
		n.x = (float) (n.getUnscaledX() * scaleX + dx);
		n.y = (float) (n.getUnscaledY() * scaleY + dy);
	}

	// protected void updateNodeRanges()
	// {
	// /*
	// * Update all the XYRange objects.
	// */
	// int threshold = (int) PhyloWidget.ui.renderThreshold;
	// for (int i = 0; i < nodes.size(); i++)
	// {
	// PhyloNode n = (PhyloNode) nodes.get(i);
	// n.update();
	// if (i > threshold)
	// continue;
	// n.x = n.unscaledX * scaleX + dx;
	// n.y = n.unscaledY * scaleY + dy;
	// // PhyloNode n = (PhyloNode) nodes.get(i);
	//			
	// // n.update();
	// // NodeRange r = nodesToRanges.get(n);
	// }
	// // NodeRange r = (NodeRange) ranges.get(i);
	// // n.x = n.unscaledX * scaleX + dx;
	// // n.y = n.unscaledY * scaleY + dy;
	// // PhyloNode parent;
	// // if (tree.getParentOf(n) != null)
	// // parent = (PhyloNode) tree.getParentOf(n);
	// // else
	// // {
	// // parent = n;
	// // }
	// // switch (r.type)
	// // {
	// // case (TreeRenderer.NODE):
	// // n.update();
	// // r.loX = Math.min(n.x - rad, parent.x - rad);
	// // r.hiX = Math.max(n.x + rad, parent.x + rad);
	// // r.loY = Math.min(n.y - rad, parent.y - rad);
	// // r.hiY = Math.max(n.y + rad, parent.y + rad);
	// // break;
	// // case (TreeRenderer.LABEL):
	// // r.loX = n.x + dotWidth;
	// // float textHeight = (font.ascent() + font.descent())
	// // * textSize;
	// // r.loY = n.y - textHeight / 2;
	// // r.hiY = n.y + textHeight / 2;
	// // float width = 0;
	// // width = n.unitTextWidth * textSize;
	// // r.hiX = r.loX + width;
	// // break;
	// // }
	// // }
	// // list.sort();
	// }

	@Override
	protected void initNodeRange(NodeRange r)
	{
		PhyloNode n = r.node;
		PhyloNode p = (PhyloNode) tree.getParentOf(n);
		if (p == null)
			p = n;
		r.loX = (float) Math.min(n.unscaledX - rad / rect.width, p.unscaledX
				- rad / rect.width);
		r.hiX = (float) Math.max(n.unscaledX + rad / rect.width, p.unscaledX
				+ rad / rect.width);
		r.loY = (float) Math.min(n.unscaledY - rad / rect.width, p.unscaledY
				- rad / rect.width);
		r.hiY = (float) Math.max(n.unscaledY + rad / rect.width, p.unscaledY
				+ rad / rect.width);
	}

	Rectangle2D.Float tRect = new Rectangle2D.Float();

	@Override
	protected boolean isNodeWithinScreen(PhyloNode n)
	{
		PhyloNode p = (PhyloNode) tree.getParentOf(n);
		if (p == null)
			return screenRect.contains(n.x, n.y);
		
		float textWidth = (float)n.unitTextWidth * textSize;
		float loX = n.x - dotWidth/2;
		float textHeight = (font.ascent() + font.descent() ) * textSize;
		float loY = n.y - textHeight / 2;
		float hiY = n.y + textHeight / 2;
		float width = (float) (n.unitTextWidth * textSize);
		float hiX = n.x + dotWidth/2 + width;
		
		tRect.setFrameFromDiagonal(n.x, n.y, p.x, p.y);
		// return screenRect.contains(n.x, n.y);
		return screenRect.intersects(tRect);
	}

	protected void drawNodeMarkerImpl(PhyloNode n)
	{
		canvas.ellipse(n.x, n.y, dotWidth, dotWidth);
	}

	protected void drawLineImpl(PhyloNode p, PhyloNode c)
	{
		canvas.line(p.x, p.y, p.x, c.y);
		canvas.line(p.x, c.y, c.x, c.y);
		// if (PhyloWidget.ui.showBranchLengths)
		// {
		// PGraphicsJava2D pgj = (PGraphicsJava2D) canvas;
		// Graphics2D g2 = pgj.g2;
		// g2.setFont(font.font.deriveFont(textSize * .5f));
		// g2.setPaint(Color.black);
		// double temp = tree.getBranchLength(n);
		// temp *= 100;
		// temp = (int) temp;
		// temp /= 100;
		// String s = String.valueOf(temp);
		// // g2.drawString(s, parent.x, n.y - strokeForNode(n));
		// }
	}

	protected void drawLabelImpl(PhyloNode n)
	{
		canvas.pushMatrix();
		canvas.translate(n.x + dotWidth / 2 + textSize / 3, n.y);
		canvas.rotate(PApplet.radians(PhyloWidget.ui.textRotation));
		if (RenderOutput.isOutputting)
		{
			canvas.hint(PApplet.ENABLE_NATIVE_FONTS);
			canvas.textFont(FontLoader.instance.vera);
			canvas.textAlign(PConstants.LEFT, PConstants.BOTTOM);
			canvas.textSize(textSize);
			canvas.text(n.getLabel(), 0, 0 + dFont);
		} else if (canvas.getClass() == PGraphicsJava2D.class
				&& PhyloWidget.usingNativeFonts)
		{
			PGraphicsJava2D pgj = (PGraphicsJava2D) canvas;
			Graphics2D g2 = pgj.g2;
			/*
			 * If it's not a leaf, then draw the white background.
			 */
			if (!tree.isLeaf(n))
			{
				/*
				 * This is simply for drawing non-leaf nodes.
				 */
				float ratio = (float) tree.getNumEnclosedLeaves(n)
						/ (float) tree.getNumEnclosedLeaves(tree.getRoot());
				ratio = (float) Math.sqrt(ratio);
				g2.setFont(font.font.deriveFont(textSize * ratio));
				float tw = UIUtils.getTextWidth(canvas, font, textSize * ratio,
						n.getLabel(), true);
				g2.setPaint(Color.black);
				float ascent = UIUtils.getTextAscent(canvas, font, textSize
						* ratio, true);
				float descent = UIUtils.getTextDescent(canvas, font, textSize
						* ratio, true);
				float df = (ascent - descent) / 2f;
				g2.drawString(n.getLabel(), 0, 0 + df);
			} else
			{
				g2.setFont(font.font.deriveFont(textSize));
				g2.setPaint(style.foregroundColor);
				g2.drawString(n.getLabel(), 0, 0 + dFont);
			}
		} else
		{
			canvas.noHint(PApplet.ENABLE_NATIVE_FONTS);
			canvas.textFont(FontLoader.instance.veraNonNative);
			canvas.textAlign(PConstants.LEFT, PConstants.BOTTOM);
			canvas.textSize(textSize);
			canvas.text(n.getLabel(), 0, 0 + dFont);
			canvas.hint(PApplet.ENABLE_NATIVE_FONTS);
		}
		canvas.popMatrix();
	}

	public float getRowHeight()
	{
		return rowSize;
	}

	public float getTextSize()
	{
		return textSize;
	}

	protected float getNodeRadius()
	{
		return dotWidth / 2f;
	}

	@Override
	public synchronized void nodesInRange(ArrayList arr, Rectangle2D.Float rect)
	{
		AffineTransform sc = AffineTransform.getScaleInstance(1f / scaleX,
				1f / scaleY);
		AffineTransform tr = AffineTransform.getTranslateInstance(-dx, -dy);

		Point2D p1 = new Point2D.Double(rect.getMinX(), rect.getMinY());
		Point2D p2 = new Point2D.Double(rect.getMaxX(), rect.getMaxY());
		
		p1 = tr.transform(p1, null);
		p2 = tr.transform(p2, null);
		p1 = sc.transform(p1, null);
		p2 = sc.transform(p2, null);

		rect.setFrameFromDiagonal(p1, p2);
		// Rectangle2D.Float tempRect = new Rectangle2D.Float();
		// tempRect.setFrameFromDiagonal(p1, p2);
		super.nodesInRange(arr, rect);
	}

	public void positionText(PhyloNode n, TextField tf)
	{
		tf.setTextSize(textSize);
		float tfWidth = UIUtils.getTextWidth(canvas, font, textSize, tf
				.getText(), true);
		float textWidth = (float) Math.max(n.unitTextWidth * textSize + 5,
				tfWidth);
		tf.setWidth(textWidth);
		tf
				.setPositionByBaseline(n.x + dotWidth / 2 + textSize / 3, n.y
						+ dFont);
	}
}
