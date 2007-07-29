package org.phylowidget.render;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.andrewberman.sortedlist.SortedXYRangeList;
import org.andrewberman.ui.TextField;
import org.andrewberman.ui.UIUtils;
import org.phylowidget.tree.RenderNode;
import org.phylowidget.tree.Tree;
import org.phylowidget.ui.FontLoader;
import org.phylowidget.ui.HoverHalo;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;

public interface TreeRenderer
{
	public void render(PGraphics canvas, float x, float y, float w, float h);
	
	public void layout();
	
	public void setTree(Tree t);
	
	public Tree getTree();
	
	public void nodesInRange(ArrayList list, Rectangle2D.Float rect);
	
	public float getNodeRadius();
	
	public void positionText(RenderNode node, TextField text);

	/**
	 * The abstract tree renderer class.
	 * 
	 * @author Greg Jordan
	 */
	abstract class Abstract implements TreeRenderer
	{
		public static final int NODE = 0;

		public static final int LABEL = 1;

		protected PApplet p;
		protected PGraphics canvas;

		/**
		 * The rectangle that defines the area in which this renderer will draw
		 * itself.
		 */
		public Rectangle2D.Float rect;

		/**
		 * The tree that will be rendered.
		 */
		protected Tree tree;

		/**
		 * Font to be used to draw the nodes.
		 */
		protected PFont font;
		protected float textSize;

		/**
		 * Styles for rendering the tree.
		 */
		RenderStyleSet style;
		
		/**
		 * All nodes in the associated tree.
		 */
		protected ArrayList nodes = new ArrayList();

		/**
		 * Leaf nodes in the associated tree.
		 */
		protected ArrayList leaves = new ArrayList();

		/**
		 * Stores positions for all nodes (internal and leaf nodes). Key = TreeNode
		 * Value = org.andrewberman.util.Point Positions should range from 0 to 1 in
		 * the x and y directions. During rendering, these values will be multiplied
		 * accordingly to fill the enclosing rectangle.
		 */
		protected HashMap positions = new HashMap();

		/**
		 * Transformations required to go from the stored position to the actual
		 * position. Should be set at the beginning of each draw.
		 */
		protected float scaleX, scaleY = 0;

		/**
		 * A data structure to store the rectangular regions of all nodes. Instead
		 * of drawing all nodes, we retrieve the nodes whose regions intersect with
		 * the visible rectangle, and then draw. This can significantly improve
		 * performance when viewing only a portion of a large tree.
		 */
		protected SortedXYRangeList list = new SortedXYRangeList();

		protected ArrayList ranges = new ArrayList();
		protected float dx, dy = 0;

		/**
		 * Tracks the last modification count at which this renderer was
		 * synchronized with its tree. This allows us to only recalculate positions
		 * when the tree structure actually changes.
		 */
		protected int lastModCount = 0;

		protected ArrayList inRange = new ArrayList();

		protected boolean sorted = false;

		public Abstract()
		{
			rect = new Rectangle2D.Float(0, 0, 0, 0);
			font = FontLoader.vera;
			style = RenderStyleSet.defaultStyle();
		}

		public void render(PGraphics canvas, float x, float y, float w, float h)
		{
			this.canvas = canvas;
			rect.setRect(x, y, w, h);
			if (tree == null)
				return;
			synchronized (tree)
			{
				if (tree.modCount != lastModCount)
				{
					tree.recalculateStuff();
					update();
					lastModCount = tree.modCount;
				}
				draw();
			}
		}

		/**
		 * Updates this renderer's internal representation of the tree. This should
		 * only be called when the tree is changed.
		 */
		protected void update()
		{
			leaves.clear();
			nodes.clear();
			tree.getAll(leaves, nodes);

			layout();
			createEmptyNodeRanges();
		}

		/**
		 * Calculate the layout of the nodes within this renderer. Only called when
		 * the tree structure is changed, so it's okay for this to be a relatively
		 * expensive operation. This should populate the positions HashMap with the
		 * positions of all nodes.
		 */
		public void layout()
		{
		}

		/**
		 * This method creates an empty NodeRange object for each node in the tree,
		 * and an empty NodeRange object of type LABEL for each leaf in the tree.
		 */
		protected void createEmptyNodeRanges()
		{

			list.clear();
			ranges.clear();
			for (int i = 0; i < nodes.size(); i++)
			{
				RenderNode n = (RenderNode) nodes.get(i);
				NodeRange r = new NodeRange();
				r.loX = n.unscaledX;
				r.hiX = n.unscaledX + .001f;
				r.loY = n.unscaledY;
				r.hiY = n.unscaledY + .001f;
				r.type = NodeRange.NODE;
				r.node = n;
				r.render = this;
				ranges.add(r);
				list.insert(r, false);
				if (n.isLeaf())
				{
					NodeRange r2 = new NodeRange();
					r2.loX = r2.hiX = n.unscaledX;
					r2.loY = r2.hiY = n.unscaledY + .001f;
					r2.type = NodeRange.LABEL;
					r2.node = n;
					r2.render = this;
					ranges.add(r2);
					list.insert(r2, false);
				}
			}
			list.sortFull();
		}
		
		/**
		 * Draws this renderer's view to the canvas.
		 */
		protected void draw()
		{
			drawRecalc();
			if (!sorted)
			{
				list.sort();
				sorted = true;
			}
			/*
			 * Draw the nodes that are in range.
			 */
			canvas.noStroke();
			canvas.fill(0);
			canvas.textFont(font);
			canvas.textSize(textSize);
			canvas.textAlign(PConstants.LEFT);
			hint();
			inRange.clear();
			Rectangle2D.Float screenRect = new Rectangle2D.Float(0, 0, canvas.width,
					canvas.height);
			UIUtils.screenToModel(screenRect);
			list.getInRange(inRange, screenRect);
			/*
			 * Set up some rendering constants so we don't recalculate within the loop.
			 */
			float regWidth = style.regStroke * getNodeRadius()/10;
			float hoverWidth = style.hoverStroke * getNodeRadius()/10;
			hoverWidth = Math.max(hoverWidth, 3);
			hoverWidth *= HoverHalo.hoverMult;
//			float regWidth = 1f;
//			float hoverWidth = 3f;
			int size = inRange.size();
			for (int i = 0; i < size; i++)
			{
				NodeRange r = (NodeRange) inRange.get(i);
				RenderNode n = r.node;
				switch (r.type)
				{
					case (Abstract.LABEL):
						drawLabel(n);
						break;
					case (Abstract.NODE):
						canvas.strokeWeight(1f);
						if (n.hovered)
						{					
							canvas.stroke(style.hoverColor.getRGB());
							canvas.strokeWeight(hoverWidth);
							drawLine(n);
						} else
						{
							canvas.stroke(style.regColor.getRGB());
							canvas.strokeWeight(regWidth);
							drawLine(n);
						}
						canvas.noStroke();
						canvas.fill(style.regColor.getRGB());
						drawNode(n);
						break;
				}
			}
			unhint();
		}

		RenderingHints oldRH;
		void hint()
		{
			if (textSize > 100 && UIUtils.isJava2D(canvas))
			{
				Graphics2D g2 = ((PGraphicsJava2D)canvas).g2;
				oldRH = g2.getRenderingHints();
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			}
		}
		
		void unhint()
		{
			if (textSize > 100 && UIUtils.isJava2D(canvas))
			{
				Graphics2D g2 = ((PGraphicsJava2D)canvas).g2;
				g2.setRenderingHints(oldRH);
			}
		}
		
		/**
		 * This method is where subclasses should perform any calculations that
		 * should be performed during each draw() cycle prior to the actual drawing.
		 * <p>
		 * Things such as: updating the node ranges, recalculating offsets and
		 * sizes, etc. etc.
		 */
		protected void drawRecalc()
		{
		}

		protected void drawLabel(RenderNode n)
		{
		}

		protected void drawNode(RenderNode n)
		{
		}

		protected void drawLine(RenderNode n)
		{
		}
		
		public void setTree(Tree t)
		{
			tree = t;
			this.lastModCount = t.modCount - 1;
		}

		public Tree getTree()
		{
			return tree;
		}
		
		public void nodesInRange(ArrayList arr, Rectangle2D.Float rect)
		{
			synchronized (tree)
			{
				list.getInRange(arr, rect);
			}
		}
		
	}


}
