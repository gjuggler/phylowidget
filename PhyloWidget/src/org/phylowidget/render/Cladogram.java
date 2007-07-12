package org.phylowidget.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.andrewberman.camera.SettableRect;
import org.andrewberman.sortedlist.SortedXYRangeList;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.ProcessingUtils;
import org.andrewberman.ui.menu.Positionable;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.TreeNode;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphicsJava2D;
import processing.core.PImage;

public final class Cladogram extends AbstractTreeRenderer implements SettableRect
{
	protected PApplet p = PhyloWidget.p;

	/**
	 * A data structure to store the rectangular regions of all nodes.
	 * Instead of drawing all nodes, we retrieve the nodes whose regions
	 * intersect with the visible rectangle, and then draw. This can
	 * significantly improve performance when viewing only a portion of
	 * a large tree.
	 */
	protected SortedXYRangeList list = new SortedXYRangeList();
	protected ArrayList ranges = new ArrayList();
	public static final int NODE = 0;
	public static final int LABEL = 1;
	
	/**
	 * Another optimization method -- if we're zoomed out so far that rows
	 * become unreadable (i.e. if rowsize is <= SKIP_THRESH), then start
	 * skipping drawing nodes to relieve the strain on the text functions.
	 */
	HashMap skipMe = new HashMap();
	public static final int SKIP_THRESH = 3;
	
	/**
	 * An offscreen PGraphicsJava2D buffer, created during the layout()
	 * process to cache the text for each node. This should decrease the
	 * number of calls to Pgraphics.image() during the draw phase, increasing
	 * rendering speed.
	 * 
	 * Note that if we're using native fonts, this should NOT be created.
	 */
	protected PImage labels;
	protected PGraphicsJava2D temp;
	
	/**
	 * If true, this tree will maintain its "proper" aspect ratio, meaning it
	 * won't stretch to completely fill its enclosing rectangle.
	 */
	public boolean keepAspectRatio = true;

	/**
	 * These variables are set in the calculateSizes() method during every round
	 * of rendering. Very important!
	 */
	protected float rowSize, colSize, textSize = 0;

	protected float dFont = 0;

	protected int maxDepth = 0;
	/**
	 * Radius of the node ellipses.
	 */
	protected float dotWidth = 0;
	/**
	 * Width of the node label gutter.
	 */
	protected float gutterWidth = 0;

	protected Point ptemp = new Point(0, 0);
	protected Point ptemp2 = new Point(0, 0);
	
	public Cladogram()
	{
		super();
	}

	public void layout()
	{
		/**
		 * ASSUMPTION: the leaves ArrayList contains a "sorted" view of the
		 * tree's leaves, i.e. in the correct ordering from top to bottom.
		 */
		maxDepth = tree.getRoot().getMaxDepth();
		gutterWidth = 0;
//		float spaceWidth = font.width(' ') * 3;
		for (int i = 0; i < leaves.size(); i++)
		{
			TreeNode n = (TreeNode) leaves.get(i);
			/**
			 * Set the leaf position.
			 */
			float yPos = (float) ((i + .5) / (leaves.size()));
			float xPos = 1 - (n.getMaxDepth() - .5f) / maxDepth;
			setPosition(n, xPos, yPos);
			/**
			 * Find the width of this node's label.
			 */
			char[] chars = n.getName().toCharArray();
			float width = 0;//spaceWidth;
			for (int j = 0; j < chars.length; j++)
			{
				width += font.width(chars[j]);
			}
			if (width > gutterWidth)
				gutterWidth = width;
		}
		/**
		 * Now, set the branch positions.
		 */
		branchPositions(tree.getRoot());
		/**
		 * Create an empty XYRange object for each TreeNode.
		 */
		list.clear();
		ranges.clear();
		for (int i = 0; i < nodes.size(); i++)
		{
			TreeNode n = (TreeNode) nodes.get(i);
			Point p = getInternalPosition(n);
			NodeRange r = new NodeRange();
			r.loX = r.hiX = p.x;
			r.loY = r.hiY = p.y;
			r.type = NodeRange.NODE;
			r.node = n;
			r.render = this;
			ranges.add(r);
			list.insert(r, false);
			if (n.isLeaf())
			{
				NodeRange r2 = new NodeRange();
				r2.loX = r2.hiX = p.x;
				r2.loY = r2.hiY = p.y + .001f;
				r2.type = NodeRange.LABEL;
				r2.node = n;
				r2.render = this;
				ranges.add(r2);
				list.insert(r2,false);
			}
		}
		list.sortFull();
		
	}

	/**
	 * A recursive function to set the positions for all the branches. Only
	 * works if the leaf positions have already been set.
	 * 
	 * @param n
	 * @return
	 */
	public float branchPositions(TreeNode n)
	{
		if (n.isLeaf())
		{
			// If N is a leaf, then it's already been laid out.
			return getInternalPosition(n).y;
		} else
		{
			// If not:
			// Y coordinate should be the average of its children's heights
			ArrayList children = n.getChildren();
			float sum = 0;
			for (int i = 0; i < children.size(); i++)
			{
				TreeNode child = (TreeNode) children.get(i);
				sum += branchPositions(child);
			}
			float y = (float) sum / (float) children.size();
			float x = 1 - (n.getMaxDepth() - .5f) / maxDepth;
			setPosition(n, x, y);
			return y;
		}
	}
	
	protected ArrayList inRange = new ArrayList();
	protected boolean sorted = false;
	public void draw()
	{
		TreeNode n;
		NodeRange r;
		
		float numRows = leaves.size();
		float idealRowSize = rect.height / numRows;
		textSize = Math.min(rect.width / 2 / gutterWidth, idealRowSize);
		float effectiveWidth = rect.width - gutterWidth * textSize;
		float numCols = maxDepth;
		float idealColSize = effectiveWidth / numCols;
		float rowSize = idealRowSize;
		float colSize = idealColSize;
		if (keepAspectRatio)
			rowSize = colSize = Math.min(idealRowSize, idealColSize);
		dotWidth = Math.min(rowSize / 2, textSize / 2);
		scaleX = colSize * numCols;
		scaleY = rowSize * numRows;
		dx = (rect.width - scaleX - gutterWidth * textSize - dotWidth) / 2;
		dy = (rect.height - scaleY) / 2;
		dx += rect.getCenterX() - rect.width / 2;
		dy += rect.getCenterY() - rect.height / 2;
		dFont = (font.ascent() - font.descent()) * textSize / 2;
		/*
		 * Update all the XYRange objects.
		 */
		for (int i = 0; i < ranges.size(); i++)
		{
			r = (NodeRange) ranges.get(i);
			n = r.node;
			getPosition(n, ptemp);
			switch (r.type)
			{
				case (Cladogram.NODE):
					r.loX = ptemp.x - dotWidth/2;
					r.hiX = ptemp.x + dotWidth/2;
					r.loY = ptemp.y - dotWidth/2;
					r.hiY = ptemp.y + dotWidth/2;	
					break;
				case (Cladogram.LABEL):
					r.loX = ptemp.x + dotWidth;
				
					float textHeight = (font.ascent() + font.descent()*2)*textSize;
					r.loY = ptemp.y - textHeight/2;
					r.hiY = ptemp.y + textHeight/2;
					float width = ProcessingUtils.getTextWidth(p.g,font,textSize,n.getName(),PhyloWidget.usingNativeFonts);
					r.hiX = r.loX + width;
					break;
			}
		}
		/*
		 * Only sort the list once -- these things aren't moving!
		 */
		if (!sorted)
		{
			list.sort();
			sorted = true;
		}
		/*
		 * Skip nodes if the rows are small enough to allow it.
		 */
		skipMe.clear();
		if (rowSize < SKIP_THRESH)
		{
			for (int i=0; i < leaves.size(); i++)
			{
				n = (TreeNode)leaves.get(i);
				int asdf = 1;
				if (rowSize < SKIP_THRESH)
					asdf = 2;
				if (rowSize < SKIP_THRESH*.5)
					asdf = 3;
				if (rowSize < SKIP_THRESH*.25)
					asdf = 4;
				if (rowSize < SKIP_THRESH*.1)
					asdf = 10;
				if (i % asdf != 0)
					skipMe.put(n, null);
			}
		}
		/*
		 * Draw the nodes that are in range.
		 */
		p.fill(0);
		p.stroke(0);
		p.strokeWeight(1);
		drawAllLines();
		p.noStroke();
		p.textFont(font);
		p.textSize(textSize);
		p.textAlign(PConstants.LEFT);
		inRange.clear();
		list.getInRange(inRange, -p.width / 2, p.width / 2,
				-p.height / 2, p.height / 2);
		synchronized (tree) {
			int size = inRange.size();
			for (int i = 0; i < size; i++)
			{
				r = (NodeRange) inRange.get(i);
				n = r.node;
				switch (r.type)
				{
					case (Cladogram.LABEL):
						if (!skipMe.containsKey(n))
							drawLabel(n);
						break;
					case (Cladogram.NODE):
							drawNode(n);
						break;
				}
			}
		}
	}

	public void drawNode(TreeNode n)
	{
		getPosition(n, ptemp);
		p.ellipse(ptemp.x, ptemp.y, dotWidth, dotWidth);
	}

	public void drawLabel(TreeNode n)
	{
		getPosition(n,ptemp);
		if (PhyloWidget.usingNativeFonts)
		{
			PGraphicsJava2D pgj = (PGraphicsJava2D) p.g;
			Graphics2D g2 = pgj.g2;
			g2.setFont(font.font.deriveFont(textSize));
			g2.setPaint(Color.black);
			g2.drawString(n.getName(), ptemp.x+dotWidth, ptemp.y+dFont);
		} else
			p.text(n.getName(), ptemp.x+dotWidth, ptemp.y + dFont);
	}
	
	public void drawAllLines()
	{
		for (int i = 0; i < nodes.size(); i++)
		{
			TreeNode n = (TreeNode) nodes.get(i);
			drawLine(n);
		}
	}

	public void drawLine(TreeNode n)
	{
		getPosition(n, ptemp);
		if (n.getParent() != TreeNode.NULL_PARENT)
		{
			getPosition(n.getParent(), ptemp2);
			p.line(ptemp.x, ptemp.y, ptemp2.x, ptemp.y);
		}

		if (!n.isLeaf())
		{
			ArrayList children = n.getChildren();
			TreeNode first = (TreeNode) children.get(0);
			TreeNode last = (TreeNode) children.get(children.size() - 1);
			float minY = getPosition(first, ptemp2).y;
			float maxY = getPosition(last, ptemp2).y;
			p.line(ptemp.x, minY, ptemp.x, maxY);
		}
	}
	
	public void nodesInRange(ArrayList arr, Rectangle2D.Float rect)
	{
		list.getInRange(arr, rect);
	}
}
