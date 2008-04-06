/*******************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * PhyloWidget. If not, see <http://www.gnu.org/licenses/>.
 */
package org.phylowidget.render;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.andrewberman.sortedlist.SortedXYRangeList;
import org.andrewberman.ui.Color;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.TextField;
import org.andrewberman.ui.UIGlobals;
import org.andrewberman.ui.UIRectangle;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.unsorted.BulgeUtil;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.NHXNode;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.ui.PhyloTree;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;

/**
 * The abstract tree renderer class.
 * 
 * @author Greg Jordan
 */
public class BasicTreeRenderer extends DoubleBuffer implements TreeRenderer,
		GraphListener
{
	float baseStroke;

	protected OverlapDetector overlap = new OverlapDetector();

	protected String biggestString;

	protected PGraphics canvas;

	/**
	 * These variables are set in the calculateSizes() method during every round
	 * of rendering. Very important!
	 */
	protected float colSize;

	/**
	 * Size of the text, as a multiplier relative to normal size.
	 */
	protected float dFont;
	/**
	 * Radius of the node ellipses.
	 */
	protected float dotWidth;

	protected double dx;

	protected double dy;

	/**
	 * Font to be used to draw the nodes.
	 */
	protected PFont font;

	// protected ArrayList<NodeRange> ranges = new ArrayList<NodeRange>();

	/**
	 * Width of the node label gutter.
	 */
	protected float biggestStringWidth = 0;

	/**
	 * Leaf nodes in the associated tree.
	 */
	//	protected ArrayList<PhyloNode> leaves = new ArrayList<PhyloNode>();
	//	protected ArrayList<PhyloNode> sigLeaves = new ArrayList<PhyloNode>();
	protected PhyloNode[] leaves = new PhyloNode[1];
	protected PhyloNode[] sigLeaves = new PhyloNode[1];

	/**
	 * A data structure to store the rectangular regions of all nodes. Instead
	 * of drawing all nodes, we retrieve the nodes whose regions intersect with
	 * the visible rectangle, and then draw. This can significantly improve
	 * performance when viewing only a portion of a large tree.
	 */
	protected SortedXYRangeList list = new SortedXYRangeList();

	boolean mainRender;

	protected boolean needsLayout;

	/**
	 * All nodes in the associated tree.
	 */
	protected PhyloNode[] nodes = new PhyloNode[1];

	protected HashMap<PhyloNode, NodeRange> nodesToRanges = new HashMap<PhyloNode, NodeRange>();

	/**
	 * These variables are set in the calculateSizes() method during every round
	 * of rendering. Very important!
	 */
	protected float numCols;

	/**
	 * These variables are set in the calculateSizes() method during every round
	 * of rendering. Very important!
	 */
	protected float numRows;

	RenderingHints oldRH;

	protected Point ptemp = new Point(0, 0);

	protected Point ptemp2 = new Point(0, 0);

	/**
	 * Radius of the node ellipses.
	 */
	protected float rad;

	/**
	 * The rectangle that defines the area in which this renderer will draw
	 * itself.
	 */
	public Rectangle2D.Float rect, screenRect;

	/**
	 * These variables are set in the calculateSizes() method during every round
	 * of rendering. Very important!
	 */
	protected float rowSize;

	protected double scaleX;

	protected double scaleY;

	/**
	 * Styles for rendering the tree.
	 */
	RenderStyleSet style;

	protected float textSize;

	protected int threshold;

	Rectangle2D.Float tRect = new Rectangle2D.Float();

	/**
	 * The tree that will be rendered.
	 */
	protected RootedTree tree;

	private boolean fforwardMe;

	private float tsf;

	public BasicTreeRenderer()
	{
		rect = new Rectangle2D.Float(0, 0, 0, 0);
		font = UIGlobals.g.getFont();
		style = RenderStyleSet.defaultStyle();
		setOptions();
	}

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
			n.setPosition(xPos, yPos);
			n.setRealX(calcRealX(n));
			n.setRealY(calcRealY(n));
			return yPos;
		}
	}

	float calcRealX(PhyloNode n)
	{
		return (float) (n.getX() * scaleX + dx);
	}

	float calcRealY(PhyloNode n)
	{
		return (float) (n.getY() * scaleY + dy);
	}

	int lineColor(PhyloNode n)
	{
		if (n.found)
		{
			return style.foundColor.getRGB();
		}
		switch (n.getState())
		{
			case (PhyloNode.CUT):
				return style.dimColor.getRGB();
			case (PhyloNode.COPY):
				return style.copyColor.getRGB();

			case (PhyloNode.NONE):
			default:
				return PhyloWidget.cfg.getBranchColor().getRGB();
		}
	}

	int nodeColor(PhyloNode n)
	{
		if (n.found)
		{
			return style.foundColor.getRGB();
		}
		switch (n.getState())
		{
			case (PhyloNode.CUT):
				return style.dimColor.getRGB();
			case (PhyloNode.COPY):
				return style.copyColor.getRGB();

			case (PhyloNode.NONE):
			default:
				return PhyloWidget.cfg.getNodeColor().getRGB();
		}
	}

	protected void constrainAspectRatio()
	{

	}

	ArrayList<PhyloNode> foundItems = new ArrayList<PhyloNode>();

	protected void draw()
	{
		float minSize = Math.min(rowSize, colSize);
		baseStroke = getNormalLineWidth() * PhyloWidget.cfg.lineSize;
		canvas.noStroke();
		canvas.fill(0);

		canvas.textFont(UIGlobals.g.getFont());
		canvas.textAlign(PConstants.LEFT, PConstants.CENTER);
		canvas.textSize(textSize);
		hint();
		screenRect = new Rectangle2D.Float(0, 0, canvas.width, canvas.height);
		UIUtils.screenToModel(screenRect);

		/*
		 * FIRST LOOP: Updating nodes Update all nodes, regardless of
		 * "threshold" status.
		 * Also set each node's drawMe flag to FALSE.
		 */
		foundItems.clear();
		int nodesDrawn = 0;
		PhyloNode[] nodesToDraw = new PhyloNode[nodes.length];
		Thread.yield();
		for (int i = 0; i < nodes.length; i++)
		{
			Thread.yield();
			PhyloNode n = nodes[i];
			if (fforwardMe)
				n.fforward();
			updateNode(n);
			n.drawMe = false;
			n.labelWasDrawn = false;
			n.isWithinScreen = isNodeWithinScreen(n);
			//			n.isWithinScreen = true;
			n.zoomTextSize = 1;
			if (n.found && n.isWithinScreen)
				foundItems.add(n);

			if (nodesDrawn >= PhyloWidget.cfg.renderThreshold)
				continue;
			if (!n.isWithinScreen)
				continue;
			n.drawMe = true;
			nodesToDraw[nodesDrawn] = n;
			nodesDrawn++;
		}
		fforwardMe = false;
		//		list.sort();
		/*
		 * SECOND LOOP: Flagging drawn nodes
		 *  -- Now, we go through all nodes
		 * (remember this is a list sorted by significance, i.e. enclosed number
		 * of leaves). At each node, we decide whether or not to draw it based
		 * on whether it is within the screen area. We exit the loop once the
		 * threshold number of nodes has been drawn.
		 */
		//		int nodesDrawn = 0;
		//		ArrayList<PhyloNode> nodesToDraw = new ArrayList<PhyloNode>(nodes
		//				.size());
		//		for (int i = 0; i < nodes.size(); i++)
		//		{
		//			PhyloNode n = nodes.get(i);
		//			// n.isWithinScreen = isNodeWithinScreen(n);
		//			if (nodesDrawn >= PhyloWidget.cfg.renderThreshold)
		//				break;
		//			if (!n.isWithinScreen)
		//				continue;
		//			/*
		//			 * Ok, let's flag this thing to be drawn.
		//			 */
		//			n.drawMe = true;
		//			nodesDrawn++;
		//			nodesToDraw.add(n);
		//		}
		/*
		 * THIRD LOOP: Drawing nodes
		 *   - This loop actually does the drawing.
		 */
		Thread.yield();
		for (int i = nodesDrawn - 1; i >= 0; i--)
		{
			Thread.yield();
			PhyloNode n = nodesToDraw[i];
			if (!n.drawMe)
			{
				if (n.isWithinScreen)
				{
					if (isAnyParentDrawn(n))
						continue;
				} else
					continue;
			}
			/*
			 * Ok, we've skipped all the non-drawn nodes,
			 * let's do the actual drawing.
			 */
			handleNode(n);
		}

		/*
		 * FOURTH LOOP: Drawing labels
		 *   - This loop uses the crazy overlap logic.
		 * 
		 * 
		 */
		overlap.clear();

		/*
		 * If we have a hovered node, always draw it.
		 */
		if (tree instanceof PhyloTree)
		{
			PhyloTree pt = (PhyloTree) tree;
			PhyloNode h = pt.hoveredNode;
			if (h != null)
			{
				Point point = new Point(getX(h), getY(h));
				float dist = (float) point.distance(mousePt);
				float bulgedSize = BulgeUtil.bulge(dist, .7f, 30);
				if (tree.isLeaf(h))
				{
					if (textSize <= 14)
						h.zoomTextSize = bulgedSize;
					else
						h.zoomTextSize = 1f;
				}
				updateNode(h);

				drawLabel(h);
				h.labelWasDrawn = true;
				NodeRange r = nodesToRanges.get(h);
				if (r != null && tree.isLeaf(r.node))
				{
					//					if (UniqueLabeler.isLabelSignificant(r.node.getLabel()))
					overlap.insert(r.loY, r.hiY);
				}
			}
		}

		/*
		 * Also always try to draw nodes that are "found".
		 */
		Thread.yield();
		for (PhyloNode n : foundItems)
		{
			Thread.yield();
			NodeRange r = nodesToRanges.get(n);
			if (!overlap.overlaps(r.loY, r.hiY) || !useOverlapDetector())
			{
				if (tree.isLeaf(n))
				{
					overlap.insert(r.loY, r.hiY);
				}
				if (tree.isLabelSignificant(n.getLabel()))
				{
					drawLabel(n);
					n.labelWasDrawn = true;
				}
			}
		}

		int asdf = 0;
		Thread.yield();
		for (int i = 0; i < sigLeaves.length; i++)
		{
			Thread.yield();
			PhyloNode n = sigLeaves[i];
			if (!n.isWithinScreen || n.labelWasDrawn)
				continue;
			NodeRange r = nodesToRanges.get(n);
			if (!overlap.overlaps(r.loY, r.hiY) || !useOverlapDetector())
			{
				overlap.insert(r.loY, r.hiY);
				drawLabel(n);
				n.labelWasDrawn = true;
				asdf++;
			}
		}

		/*
		 * Finally, unhint the canvas.
		 */
		unhint();
	}

	protected void drawBootstrap(PhyloNode n)
	{
		if (!PhyloWidget.cfg.showBootstrapValues)
			return;
		if (n instanceof NHXNode)
		{
			NHXNode nhx = (NHXNode) n;
			String boot = nhx.getAnnotation(NHXNode.BOOTSTRAP);
			if (boot != null)
			{
				canvas.pushMatrix();
				canvas.translate(getX(n) + dotWidth / 2 + getNormalLineWidth()
						* 2, getY(n));
				Double value = Double.parseDouble(boot);
				float curTextSize = textSize * 0.5f;
				canvas.textFont(font);
				canvas.textSize(curTextSize);
				canvas.fill(PhyloWidget.cfg.getTextColor().brighter(100)
						.getRGB());
				canvas.textAlign(canvas.RIGHT, canvas.TOP);
				float s = strokeForNode(n);
				canvas.text(boot, -dotWidth * 2 - s, +s);
				canvas.popMatrix();
			}
		} else
		{
			return;
		}

	}

	protected boolean useOverlapDetector()
	{
		return true;
	}

	protected void drawLabel(PhyloNode n)
	{
		n.labelWasDrawn = true;

		if (!tree.isLeaf(n) && !PhyloWidget.cfg.showCladeLabels)
			return;

		canvas.strokeWeight(strokeForNode(n));
		canvas.fill(textColor(n));

		drawLabelImpl(n);
	}

	protected void drawLabelImpl(PhyloNode n)
	{
		float curTextSize = textSize * n.zoomTextSize;

		/*
		 * Early exit strategy if text is too small.
		 */
		if (curTextSize < .5f)
		{
			return;
		}

		canvas.pushMatrix();
		canvas.translate(getX(n) + dotWidth / 2 + getNormalLineWidth() * 2,
				getY(n));
		if (PhyloWidget.cfg.textRotation != 0)
			canvas.rotate(PApplet.radians(PhyloWidget.cfg.textRotation));

		if (tree.isLeaf(n) && n.found)
		{
			/*
			 * Draw a background rect.
			 */
			canvas.noStroke();
			canvas.fill(style.foundBackground.getRGB());
			canvas.rect(0, -curTextSize / 2,
					(float) (n.unitTextWidth * curTextSize), curTextSize);
		}

		/*
		 * THIS IS THE MAIN LABEL DRAWING CODE. SO SLEEK, SO SIMPLE!!!
		 */
		canvas.textFont(font);
		canvas.textSize(curTextSize);
		if (n.found)
		{
			canvas.fill(style.foundForeground.getRGB());
		}

		if (!tree.isLeaf(n))
		{
			curTextSize *= 0.5f;
			canvas.textSize(curTextSize);
			canvas.fill(PhyloWidget.cfg.getTextColor().brighter(100).getRGB());
			//			curTextSize = rowSize;
			//			canvas.textSize(rowSize);
			canvas.textAlign(canvas.RIGHT, canvas.BOTTOM);
			float s = strokeForNode(n);
			canvas.text(n.getLabel(), -dotWidth - curTextSize / 3 - s, -s
					- curTextSize / 5);
		} else
		{
			canvas.textAlign(canvas.LEFT, canvas.BASELINE);
			canvas.text(n.getLabel(), 0, 0 + dFont * curTextSize / textSize);
		}
		canvas.popMatrix();
	}

	protected void drawLine(PhyloNode p, PhyloNode c)
	{
		if (p == null)
			return;
		/*
		 * Keep in mind that p may be null (in the case of root node).
		 */
		float weight = strokeForNode(c);
		if (weight == 0)
			return;
		weight = Math.max(0.5f, weight);
		canvas.strokeWeight(weight);
		canvas.stroke(lineColor(c));

		if (c instanceof NHXNode)
		{
			NHXNode nhx = (NHXNode) c;
			String boot = nhx.getAnnotation(NHXNode.BOOTSTRAP);
			if (boot != null)
			{
				Double d = Double.parseDouble(boot);
				d = (100 - d) * 200f / 100f;
				canvas.stroke(PhyloWidget.cfg.getBranchColor().brighter(d)
						.getRGB());
			}
		}

		drawLineImpl(p, c);
	}

	protected void drawLineImpl(PhyloNode p, PhyloNode c)
	{
		canvas.line(getX(p), getY(p), getX(p), getY(c));
		canvas.line(getX(p), getY(c), getX(c), getY(c));
		// canvas.line(p.getX(), p.getY(), p.getX(), c.getY());
		// canvas.line(p.getX(), c.getY(), c.getX(), c.getY());
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
		// // g2.drawString(s, parent.x, n.getY() - strokeForNode(n));
		// }
	}

	protected float getNormalLineWidth()
	{
		float min = Math.min(colSize, rowSize);
		return min / 10f;
	}

	protected void drawNodeMarker(PhyloNode n)
	{
		canvas.noStroke();
		canvas.fill(nodeColor(n));
		drawNodeMarkerImpl(n);
	}

	protected void drawNodeMarkerImpl(PhyloNode n)
	{
		if (dotWidth == 0)
			return;
		if (n instanceof NHXNode && !tree.isLeaf(n))
		{
			NHXNode nhx = (NHXNode) n;
			String s = nhx.getAnnotation(NHXNode.DUPLICATION);
			if (s != null)
			{
				if (s.toLowerCase().equals("t") || s.toLowerCase().equals("y"))
				{
					canvas.fill(style.copyColor.getRGB());
				} else
				{
					canvas.fill(new Color(0, 0, 255).getRGB());
				}
			}
		}
		if (PhyloWidget.cfg.nodeShape.equals("square"))
		{
			canvas.rect(getX(n) - dotWidth / 2, getY(n) - dotWidth / 2,
					dotWidth, dotWidth);
		} else
		{
			canvas.ellipse(getX(n), getY(n), dotWidth, dotWidth);
		}

	}

	public void edgeAdded(GraphEdgeChangeEvent e)
	{
		needsLayout = true;
	}

	public void edgeRemoved(GraphEdgeChangeEvent e)
	{
		needsLayout = true;
	}

	protected float getNodeRadius()
	{
		return dotWidth / 2f;
	}

	public float getRowHeight()
	{
		return rowSize;
	}

	public float getTextSize()
	{
		return textSize;
	}

	public RootedTree getTree()
	{
		return tree;
	}

	public void fforward()
	{
		ArrayList ffMe = new ArrayList();
		tree.getAll(tree.getRoot(), null, ffMe);
		for (int i = 0; i < ffMe.size(); i++)
		{
			PhyloNode n = (PhyloNode) ffMe.get(i);
			n.fforward();
		}
	}

	public float getX(PhyloNode n)
	{
		return n.getRealX();
		// return (float) (n.getX() * scaleX + dx);
	}

	public float getY(PhyloNode n)
	{
		return n.getRealY();
		// return (float) (n.getY() * scaleY + dy);
	}

	protected void handleNode(PhyloNode n)
	{
		/*
		 * Set up the strokes and weights.
		 */
		if (tree.isLeaf(n))
		{
			/*
			 * CASE 1: LEAF NODE
			 * - Draw line to parent
			 * - Draw node marker
			 * - Draw label
			 */
			drawLine((PhyloNode) n.getParent(), n);
			drawNodeMarker(n);
			// drawLabel(n);
		} else
		{
			/*
			 * CASE 3: INTERNAL NODE
			 * - Draw node marker
			 * - Draw line to parent
			 * - Go through children:
			 *    - if child is flagged to be drawn, continue
			 *    - if child is *not* drawn, draw a line to its early / latest
			 */
			drawLine((PhyloNode) n.getParent(), n);
			drawNodeMarker(n);

			/*
			 * If we're a NHX node, then draw the bootstrap (if the config says so).
			 */
			drawBootstrap(n);

			/*
			 * Do some extra stuff to clean up the thresholding artifacts.
			 */
			List l = tree.getChildrenOf(n);
			for (int i = 0; i < l.size(); i++)
			{
				PhyloNode child = (PhyloNode) l.get(i);
				NodeRange r = nodesToRanges.get(child);
				/*
				 * If this child is thresholded out, then draw a placemark line to its
				 * earliest or latest leaf node.
				 */
				if (!child.drawMe && child.isWithinScreen)
				{
					PhyloNode leaf = null;
					if (i == 0)
						leaf = (PhyloNode) tree.getFirstLeaf(child);
					else if (i == l.size() - 1)
						leaf = (PhyloNode) tree.getLastLeaf(child);
					else
						/*
						 * If this child is a "middle child", just do nothing.
						 */
						continue;
					drawLine(n, leaf);
					// drawLabel(leaf);
				}
			}
			/*
			 * Now, we want to draw this inner node's label if it's significant
			 * and we are set to show clade labels.
			 */
			if (tree.isLabelSignificant(n.getLabel()))
			{
				drawLabel(n);
			}
		}
		/*
		 * If we've got a PhyloTree on our hands,
		 * let's take care of the hovered node.
		 */
		if (tree instanceof PhyloTree)
		{
			PhyloTree pt = (PhyloTree) tree;
			PhyloNode h = pt.hoveredNode;
			if (h != null && h.getParent() != null)
			{
				canvas.stroke(style.hoverColor.getRGB());
				float weight = baseStroke * style.hoverStroke;
				weight *= PhyloWidget.ui.traverser.glowTween.getPosition();
				canvas.strokeWeight(weight);
				canvas.fill(style.hoverColor.getRGB());
				drawLineImpl((PhyloNode) h.getParent(), h);
				canvas.noStroke();
				canvas.fill(style.hoverColor.getRGB());
				drawNodeMarkerImpl(h);
			}
		}
	}

	void hint()
	{
		if (textSize > 100 && UIUtils.isJava2D(canvas))
		{
			Graphics2D g2 = ((PGraphicsJava2D) canvas).g2;
			oldRH = g2.getRenderingHints();
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
			canvas.noHint(PApplet.ENABLE_NATIVE_FONTS);
			canvas.smooth();
		}

	}

	boolean isAnyParentDrawn(PhyloNode n)
	{
		PhyloNode cur = (PhyloNode) n.getParent();
		while (cur != null)
		{
			if (cur.drawMe)
				return true;
			cur = (PhyloNode) cur.getParent();
		}
		return false;
	}

	protected boolean isNodeWithinScreen(PhyloNode n)
	{
		/*
		 * Get this node range and set the rect.
		 */
		NodeRange r = nodesToRanges.get(n);
		Rectangle rect1 = new Rectangle();
		rect1.setFrameFromDiagonal(r.loX, r.loY, r.hiX, r.hiY);

		/*
		 * Try to get the parental noderange and set it.
		 */
		PhyloNode p = (PhyloNode) tree.getParentOf(n);
		NodeRange r2 = nodesToRanges.get(p);
		if (p == null || r2 == null)
		{
			/*
			 * If we're the root node, just do our intersection.
			 */
			return rect1.intersects(screenRect);
		}

		Rectangle rect2 = new Rectangle();
		rect2.setFrameFromDiagonal(r2.loX, r2.loY, r2.hiX, r2.hiY);
		Rectangle comb = new Rectangle();
		Rectangle.union(rect1, rect2, comb);
		return screenRect.intersects(comb);
	}

	/**
	 * Updates this renderer's internal representation of the tree. This should
	 * only be called when the tree is changed.
	 */
	protected void layout()
	{
		if (!needsLayout)
			return;
		needsLayout = false;

		/*
		 * Grab our list of nodes again.
		 */
		ArrayList<PhyloNode> ls = new ArrayList<PhyloNode>();
		ArrayList<PhyloNode> ns = new ArrayList<PhyloNode>();
		synchronized (this)
		{
			tree.getAll(tree.getRoot(), ls, ns);
			Thread.yield();

			leaves = new PhyloNode[ls.size()];
			nodes = new PhyloNode[ns.size()];
			leaves = ls.toArray(leaves);
			nodes = ns.toArray(nodes);
			/*
			 * Sort these nodes by significance (i.e. num of enclosed nodes).
			 */
			Arrays.sort(nodes, 0, nodes.length, tree.sorter);
			Thread.yield();
			/*
			 * Sort the leaves by "leaf" significance (first leaf = least depth to root)
			 */
			sigLeaves = Arrays.copyOf(leaves, leaves.length);
			Arrays.sort(sigLeaves, 0, sigLeaves.length, tree.leafSorter);
			Thread.yield();
			
		}

		/*
		 * Crate new nodeRange objects for this layout.
		 */
		synchronized (list)
		{
			list.clear();
			nodesToRanges.clear();
			for (int i = 0; i < nodes.length; i++)
			{
				PhyloNode n = (PhyloNode) nodes[i];
				NodeRange r = new NodeRange();
				r.node = n;
				r.render = this;
				nodesToRanges.put(n, r);
				list.insert(r, false);
			}
			list.sortFull();
		}
		Thread.yield();

		/*
		 * ASSUMPTION: the leaves ArrayList contains a "sorted" view of the
		 * tree's leaves, i.e. in the correct ordering from top to bottom.
		 */
		biggestStringWidth = 0;
		biggestString = "";
		//		if (UIUtils.isJava2D(canvas))
		//			fm = UIUtils.getMetrics(canvas, font.font, 100);
		/*
		 * Set the leaf positions.
		 */
		for (int i = 0; i < leaves.length; i++)
		{
			PhyloNode n = (PhyloNode) leaves[i];
			/*
			 * Set the leaf position of this node.
			 */
			leafPosition(n, i);
		}

		Thread.yield();

		taxonColorMap = new HashMap<String, Integer>();
		for (int i = 0; i < nodes.length; i++)
		{
			PhyloNode n = (PhyloNode) nodes[i];
			/**
			 * Find the width of this node's label.
			 */
			float width = 0;// spaceWidth;
			if (UIUtils.isJava2D(canvas))
			{
				// width = fm.stringWidth(n.getName());
				Graphics2D g2 = ((PGraphicsJava2D) canvas).g2;
				//				width = (float) fm.getStringBounds(n.getLabel(), g2).getWidth() / 100f;
				width = UIUtils.getTextWidth(canvas, font, 100, n.getLabel(),
						true) / 100f;
			} else
			{
				char[] chars = n.getLabel().toCharArray();

				for (int j = 0; j < chars.length; j++)
				{
					width += font.width(chars[j]);
				}
			}
			n.unitTextWidth = width;
			if (width > biggestStringWidth)
			{
				biggestStringWidth = width;
				biggestString = n.getLabel();
			}

			if (n.getClass() == NHXNode.class)
			{
				NHXNode nhx = (NHXNode) n;
				String tax = nhx.getAnnotation(NHXNode.TAXON_ID);
				if (tax != null)
				{
					taxonColorMap.put(tax, null);
				} else
				{
					String spec = nhx.getAnnotation(NHXNode.SPECIES_NAME);
					if (spec != null)
						taxonColorMap.put(spec, null);
				}
			}
		}

		Thread.yield();

		if (PhyloWidget.cfg.colorBySpecies)
		{
			getColorsForSpeciesMap();
		}

		/*
		 * Special case: if biggestString is 0 length, we'll fudge it.
		 */
		if (biggestString.length() == 0)
		{
			biggestString = "P";
			//			biggestStringWidth = fm.stringWidth(biggestString) / 100f;
			biggestStringWidth = UIUtils.getTextWidth(canvas, font, 100,
					biggestString, true) / 100f;
		}
		/*
		 * Now, set the branch positions.
		 */
		branchPositions((PhyloNode) tree.getRoot());
		/*
		 * Set the numRows and numCols variables.
		 */
		numRows = leaves.length;
		numCols = tree.getMaxDepthToLeaf(tree.getRoot());
	}

	void getColorsForSpeciesMap()
	{
		int n = taxonColorMap.size();
		Set<String> keys = taxonColorMap.keySet();
		float step = 1f / (n + 1f);
		float pos = 0;
		for (String key : keys)
		{
			pos += step;
			int color = Color.HSBtoRGB(pos, .5f, 1f);
			taxonColorMap.put(key, color);
		}
	}

	int textColor(PhyloNode n)
	{
		if (n.getClass() == NHXNode.class)
		{
			int c = Color.black.getRGB();
			NHXNode nhx = (NHXNode) n;
			String tax = nhx.getAnnotation(NHXNode.TAXON_ID);
			if (tax != null)
			{
				c = taxonColorMap.get(tax);
			} else
			{
				String spec = nhx.getAnnotation(NHXNode.SPECIES_NAME);
				if (spec != null)
					c = taxonColorMap.get(spec);
			}
			return c;
		} else
		{
			return PhyloWidget.cfg.getTextColor().getRGB();
		}
	}

	public void layoutTrigger()
	{
		needsLayout = true;
	}

	protected void leafPosition(PhyloNode n, int index)
	{
		/**
		 * Set the leaf position.
		 */
		float yPos = ((float) (index + .5f) / (float) (leaves.length));
		float xPos = 1;
		if (PhyloWidget.cfg.useBranchLengths)
			xPos = nodeXPosition(n);
		n.setPosition(xPos, yPos);
	}

	public void nodesInRange(ArrayList arr, Rectangle2D.Float rect)
	{
		synchronized (list)
		{
			list.getInRange(arr, rect);
		}
	}

	protected float nodeXPosition(PhyloNode n)
	{
		if (PhyloWidget.cfg.useBranchLengths)
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

	public void positionText(PhyloNode n, TextField tf)
	{
		float textSize = this.textSize * n.zoomTextSize;
		tf.setTextSize(textSize);
		float tfWidth = UIUtils.getTextWidth(canvas, font, textSize, tf
				.getText(), true);
		float textWidth = (float) Math.max(n.unitTextWidth * textSize + 5,
				tfWidth);
		tf.setWidth(textWidth);
		if (tree.isLeaf(n))
		{
			tf.setPositionByBaseline(getX(n) + dotWidth / 2
					+ getNormalLineWidth() * 2, getY(n) + dFont);
		} else
		{
			float s = strokeForNode(n);
			tf.setPositionByBaseline(getX(n) - (float) n.unitTextWidth
					* textSize - s, getY(n) - s);
		}
	}

	public Object rangeForNode(Object n)
	{
		return nodesToRanges.get(n);
	}

	protected void recalc()
	{
		/*
		 * Some calculations that are valid for stretched and non-stretched views.
		 */
		overhang = biggestStringWidth
				* PApplet.sin(PApplet.radians(PhyloWidget.cfg.textRotation));
		float absOverhang = Math.abs(overhang);

		float origWidth = rect.width;
		if (PhyloWidget.cfg.stretchToFit)
		{
			rect.width = canvas.width;
			rect.x = 0;
			rowSize = rect.height / (numRows + absOverhang);
			textSize = rowSize;
			float maxSize = rect.width / (2 * biggestStringWidth);
			if (!PhyloWidget.cfg.showAllLabels)
			{
				textSize = PApplet.constrain(textSize, Math.min(14,
						PhyloWidget.cfg.minTextSize), maxSize);
			} else
			{
				textSize = PApplet.constrain(textSize, 0, maxSize);
			}
			colSize = rect.width / (numCols + 1 + biggestStringWidth);
			scaleX = rect.width - biggestStringWidth * textSize;
			scaleX *= 0.9f;
			scaleY = rect.height - absOverhang * textSize;
			tsf = 1;
		} else
		{
			rowSize = rect.height / (numRows + absOverhang);
			textSize = Math.min(rect.width / biggestStringWidth * .5f, rowSize);
			tsf = PhyloWidget.cfg.textSize;
			if (!PhyloWidget.cfg.showAllLabels)
				tsf *= Math.max(1, PhyloWidget.cfg.minTextSize / textSize);
			colSize = rect.width / (numCols + 1 + biggestStringWidth);
			rowSize = colSize = Math.min(rowSize, colSize) * .9f;

			colSize *= getBranchLengthScaling();

			scaleX = colSize * numCols;
			scaleY = rowSize * numRows;
			textSize = Math.min(rowSize, textSize);
		}
		dotWidth = getNormalLineWidth() * PhyloWidget.cfg.nodeSize;
		rad = dotWidth / 2;
		if (numRows == 1)
			scaleX = 0;
		dx = (rect.width - scaleX - biggestStringWidth * textSize - textSize / 2) / 2;
		dy = (rect.height - scaleY - overhang * textSize) / 2;
		dx += rect.getX();
		dy += rect.getY();
		/*
		 * Multiply the textSize by the user-specified scaling factor.
		 */
		textSize *= tsf;
		dFont = (font.ascent() - font.descent()) * textSize / 2;
	}

	public float getBranchLengthScaling()
	{
		return PhyloWidget.cfg.branchLengthScaling;
	}

	public void render(PGraphics canvas, float x, float y, float w, float h,
			boolean mainRender)
	{
		this.mainRender = mainRender;
		rect.setRect(x, y, w, h);
		if (tree == null)
			return;
		if (PhyloWidget.cfg.useDoubleBuffering)
		{
			drawToCanvas(canvas);
		} else
		{
			synchronized (this)
			{
				this.canvas = canvas;
				layout();
				recalc();
				draw();
			}
		}
	}

	//		this.rect.setFrame(x, y, w, h);
	//		this.canvas = canvas;
	////		canvas.background(0,0);
	//		if (tree == null)
	//			return;
	////		synchronized (this)
	////		{
	//			layout();
	//			recalc();
	//			draw();
	////		}
	//	}

	public void drawToBuffer(PGraphics g)
	{
		this.canvas = g;
		g.background(0, 0);
		synchronized (this)
		{
			layout();
			recalc();
			draw();
		}
	}

	public UIRectangle getVisibleRect()
	{
		float rx = (float) dx;
		float ry = (float) (dy + (overhang < 0 ? overhang * textSize : 0));
		float sx = (float) (scaleX + biggestStringWidth * textSize + dotWidth * 2);
		float sy = (float) (scaleY + Math.abs(overhang * textSize));
		return new UIRectangle(rx, ry, sx, sy);
	}

	Point mousePt = new Point();

	private float overhang;

	protected HashMap<String, Integer> taxonColorMap;

	public void setMouseLocation(Point pt)
	{
		mousePt.setLocation(pt);
	}

	protected void setOptions()
	{
	}

	public void setTree(RootedTree t)
	{
		if (t == null)
			return;
		if (tree != null)
			tree.removeGraphListener(this);
		tree = t;
		tree.addGraphListener(this);
		needsLayout = true;
		fforwardMe = true;
	}

	float strokeForNode(PhyloNode n)
	{
		float stroke = baseStroke;
		if (n.found)
		{
			stroke *= style.foundStroke;
			return stroke;
		}
		switch (n.getState())
		{
			case (PhyloNode.CUT):
				stroke *= style.dimStroke;
				break;
			case (PhyloNode.COPY):
				stroke *= style.copyStroke;
				break;
			case (PhyloNode.NONE):
			default:
				stroke *= style.regStroke;
				break;
		}
		return stroke;
	}

	void unhint()
	{
		if (textSize > 100 && UIUtils.isJava2D(canvas))
		{
			Graphics2D g2 = ((PGraphicsJava2D) canvas).g2;
			g2.setRenderingHints(oldRH);
		}
	}

	/*
	 * This is called once per render.
	 */
	protected void updateNode(PhyloNode n)
	{
		/*
		 * Update the node's Tween.
		 */
		if (mainRender)
			n.update();
		/*
		 * Set the node's cached "real" x and y values.
		 */
		n.setRealX(calcRealX(n));
		n.setRealY(calcRealY(n));

		/*
		 * If this is the hovered node, think about doing a little bulging.
		 */

		/*
		 * Update the nodeRange.
		 */
		NodeRange r = nodesToRanges.get(n);
		if (r == null)
			return;
		float realTextSize = textSize * n.zoomTextSize;
		r.loX = getX(n) - dotWidth / 2;
		float textHeight = (font.ascent() + font.descent()) * realTextSize;
		r.loY = getY(n) - textHeight / 2;
		r.hiY = getY(n) + textHeight / 2;
		float textWidth = (float) n.unitTextWidth * realTextSize;
		r.hiX = getX(n) + dotWidth / 2 + textWidth;
	}

	/**
	 * Notifies that a vertex has been added to the tree.
	 * 
	 * @param e
	 *            the vertex event.
	 */
	public void vertexAdded(GraphVertexChangeEvent e)
	{
		needsLayout = true;
	}

	/**
	 * Notifies that a vertex has been removed from the tree.
	 * 
	 * @param e
	 *            the vertex event.
	 */
	public void vertexRemoved(GraphVertexChangeEvent e)
	{
		needsLayout = true;
	}

}
