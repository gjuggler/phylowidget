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
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.andrewberman.sortedlist.SortedXYRangeList;
import org.andrewberman.ui.FontLoader;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.TextField;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.unsorted.BulgeUtil;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.phylowidget.PWContext;
import org.phylowidget.PWPlatform;
import org.phylowidget.PhyloTree;
import org.phylowidget.UsefulConstants;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.ui.NodeUncollapser;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;

/**
 * The abstract tree renderer class.
 * 
 * @author Greg Jordan
 */
@SuppressWarnings("unchecked")
public class BasicTreeRenderer extends DoubleBuffer implements GraphListener, UsefulConstants
{
	float baseStroke;

	protected FontLoader fonts;
	
	protected LayoutBase treeLayout = new LayoutUnrooted();

	protected OverlapDetector overlap = new OverlapDetector();

	protected PhyloNode widestNode;

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

	// protected ArrayList<NodeRange> ranges = new ArrayList<NodeRange>();

	/**
	 * Width of the node label gutter.
	 */
	protected float biggestAspectRatio = 0;

	public static NodeRenderer decorator;

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

	//	protected HashMap<PhyloNode, NodeRange> nodesToRanges = new HashMap<PhyloNode, NodeRange>();

	/**
	 * These variables are set in the calculateSizes() method during every round
	 * of rendering. Very important!
	 */
	//	protected float numCols;
	/**
	 * These variables are set in the calculateSizes() method during every round
	 * of rendering. Very important!
	 */
	//	protected float numRows;
	RenderingHints oldRH;

	protected Point ptemp = new Point(0, 0);

	protected Point ptemp2 = new Point(0, 0);

	/**
	 * Radius of the node ellipses.
	 */
	//	protected float rad;
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

	protected float textSize;

	protected int threshold;

	Rectangle2D.Float tRect = new Rectangle2D.Float();

	/**
	 * The tree that will be rendered.
	 */
	protected RootedTree tree;

	private boolean fforwardMe;

	private float tsf;

	PWContext context;
	
	public BasicTreeRenderer(PWContext context)
	{
		rect = new Rectangle2D.Float(0, 0, 0, 0);
		this.context = context;
		fonts = new FontLoader(context.getPW());
		if (decorator == null)
			decorator = new NodeRenderer();

		setOptions();
	}

	float calcRealX(PhyloNode n)
	{
		return (float) (n.getLayoutX() * scaleX + dx);
	}

	float calcRealY(PhyloNode n)
	{
		return (float) (n.getLayoutY() * scaleY + dy);
	}

	protected void constrainAspectRatio()
	{

	}

	ArrayList<PhyloNode> foundItems = new ArrayList<PhyloNode>();

	private Area a;

	protected void draw()
	{
		float minSize = Math.min(rowSize, colSize);
		baseStroke = getNormalLineWidth() * context.config().lineWidth;
		canvas.noStroke();
		canvas.fill(0);

		canvas.textFont(fonts.getPFont());
		canvas.textAlign(PConstants.LEFT, PConstants.CENTER);

		hint();
		screenRect = new Rectangle2D.Float(0, 0, canvas.width, canvas.height);
		UIUtils.screenToModel(screenRect);

		treeLayout.drawScaleX = (float) scaleX;
		treeLayout.drawScaleY = (float) scaleY;

		/*
		 * FIRST LOOP: Updating nodes Update all nodes, regardless of
		 * "threshold" status.
		 * Also set each node's drawMe flag to FALSE.
		 */
		a = new Area();
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
			updateNode(n); // GJ 2009-02-15 commented out. Node updates will happen in recalc() method now.
			n.drawMe = false;
			n.labelWasDrawn = false;
			n.drawLineAndNode = false;
			n.drawLabel = false;
			//			n.occluded = false;
			n.isWithinScreen = isNodeWithinScreen(n);
			n.bulgeFactor = 1;
			if (n.found && n.isWithinScreen)
				foundItems.add(n);
			// GJ 2008-09-03: Add ALWAYS_SHOW nodes to the foundItems list.
			if (n.getAnnotation(UsefulConstants.LABEL_ALWAYSSHOW) != null)
				foundItems.add(n);
			else if (n.getAnnotation(UsefulConstants.LABEL_ALWAYSSHOW_ALT) != null)
				foundItems.add(n);
			if (nodesDrawn >= context.config().renderThreshold && !context.config().showAllLabels)
				continue;
			if (!n.isWithinScreen)
				continue;
			n.drawMe = true;
			nodesToDraw[nodesDrawn] = n;
			nodesDrawn++;
		}
		fforwardMe = false;

		/*
		 * THIRD LOOP: Drawing nodes
		 *   - This loop actually does the drawing.
		 */
		Thread.yield();
		for (int i = nodesDrawn - 1; i >= 0; i--)
		{
			Thread.yield();
			PhyloNode n = nodesToDraw[i];
			//			canvas.fill(100,100);
			NodeRenderer.r = this;
			n.drawLineAndNode = true;
			n.drawLabel = false;
			handleNode(n);
			//			canvas.rect(n.range.loX,n.range.loY,n.range.hiX-n.range.loX,n.range.hiY-n.range.loY);
		}

		/*
		 * If we have a hovered node, always draw it.
		 */
		if (tree instanceof PhyloTree)
		{
			PhyloTree pt = (PhyloTree) tree;
			PhyloNode h = pt.hoveredNode;
			if (h != null && pt.containsVertex(h))
			{
				Point point = new Point(getX(h), getY(h));
				float dist = (float) point.distance(mousePt);
				float bulgedSize = BulgeUtil.bulge(dist, .7f, 30);
				if (tree.isLeaf(h))
				{
					if (textSize <= 14)
						h.bulgeFactor = bulgedSize;
					else
						h.bulgeFactor = 1f;
				}
				insertAndReturnOverlap(h);
				h.drawLabel = true;
				decorator.render(this, h);
				h.labelWasDrawn = true;
			}
		}

		/*
		 * Sort the found items.
		 */
		//		Collections.sort(foundItems, new ZOrderComparator());
		/*
		 * Also always try to draw nodes that are "found".
		 */
		Thread.yield();
		Collections.reverse(foundItems);
		for (PhyloNode n : foundItems)
		{
			Thread.yield();
			NodeRange r = n.range;
			// GJ 19-09-2008 change: Found nodes will ALWAYS be drawn, regardless of whether they're overlapping something else.
			insertAndReturnOverlap(n);
			// GJ 22-09-2008 change: Don't render the found nodes quite yet; we want them to show up over the nodes!
		}

		/*
		 * Now, go through the significance-sorted list of leaves, drawing and occluding as we go.
		 */
		Thread.yield();
		for (int i = 0; i < sigLeaves.length; i++)
		{
			Thread.yield();
			PhyloNode n = sigLeaves[i];
			if (!n.isWithinScreen || n.labelWasDrawn)
				continue;
			NodeRange r = n.range;
			if (insertAndReturnOverlap(n))
				continue;
			n.drawLabel = true;
			decorator.render(this, n);
		}

		/*
		 * Now, we can draw the found nodes on top of everything else.
		 */
		Thread.yield();
		for (PhyloNode n : foundItems)
		{
			Thread.yield();
			NodeRange r = n.range;
			n.drawLabel = true;
			decorator.render(this, n);
			n.labelWasDrawn = true;
		}

		/*
		 * Finally, unhint the canvas.
		 */
		unhint();
	}

	private Polygon tempP = new Polygon();

	private final boolean insertAndReturnOverlap(PhyloNode n)
	{
		//		if (!tree.isLeaf(n)) // Do nothing and pretend no overlap for branch nodes.
		//			return false;
		if (context.config().showAllLabels)
			return false;
		float angle = n.getAngle();
		if (angle == 0 || angle % Math.PI / 2 == 0)
		{
			if (intersectsRect(n, a))
				return true;
			a.add(new Area(r2d));
		} else
		{
			fillPolygon(n, tempP);
			if (intersectsPoly(n, a, tempP))
				return true;
			a.add(new Area(tempP));
		}
		return false;
	}

	Rectangle2D.Float r2d = new Rectangle2D.Float();
	static final float POLYMULT = 1000;

	private final boolean intersectsPoly(PhyloNode n, Area a, Polygon scratch)
	{
		//		fillPolygon(n, scratch);
		int[] xpoints = scratch.xpoints;
		int[] ypoints = scratch.ypoints;
		for (int i = 0; i < xpoints.length; i++)
		{
			float x = (float) xpoints[i] / POLYMULT;
			float y = (float) ypoints[i] / POLYMULT;
			if (a.contains(x, y))
				return true;
		}
		return false;
	}

	private final boolean intersectsRect(PhyloNode n, Area a)
	{
		//		r2d.setFrame((float)n.getRealX(),(float)n.getRealY(),0,0);
		//		for (Point2D pt : n.corners)
		//		{
		//			r2d.add(pt);
		//		}
		r2d.setFrame(n.range.loX, n.range.loY, n.range.hiX - n.range.loX, n.range.hiY - n.range.loY);
		return a.intersects(r2d);
	}

	private void fillPolygon(PhyloNode n, Polygon p)
	{
		p.reset();
//		Point2D[] points = n.corners;
//		for (Point2D pt : points)
//		{
//			p.addPoint((int) (pt.getX() * POLYMULT), (int) (pt.getY() * POLYMULT));
//		}
	}

	protected void drawBootstrap(PhyloNode n)
	{
		if (n.isNHX() && context.config().showBootstrapValues)
		{
			String boot = n.getAnnotation(BOOTSTRAP);
			if (boot != null)
			{
				canvas.pushMatrix();
				canvas.translate(getX(n), getY(n));
				Double value = Double.parseDouble(boot);
				float curTextSize = textSize * 0.5f;
				canvas.textFont(fonts.getPFont());
				canvas.textSize(curTextSize);
				canvas.fill(context.config().getTextColor().brighter(100).getRGB());
				canvas.textAlign(canvas.RIGHT, canvas.BOTTOM);
				//				float s = strokeForNode(n) / 2 + rowSize * RenderConstants.labelSpacing;
				float s = 0;
				canvas.text(boot, -getNodeRadius(), -s);
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

	double clamp(double a, double lo, double hi)
	{
		if (a <= lo)
			return lo;
		else if (a >= hi)
			return hi;
		else
			return a;
	}

	protected float getNormalLineWidth()
	{
		float min = rowSize * 0.1f;
		return min;
		//		return min * RenderConstants.labelSpacing;
	}

	public void edgeAdded(GraphEdgeChangeEvent e)
	{
		needsLayout = true;
	}

	public void edgeRemoved(GraphEdgeChangeEvent e)
	{
		needsLayout = true;
	}

	public float getNodeRadius()
	{
		return dotWidth / 2f;
	}

	public float getNodeOffset(PhyloNode n)
	{
		float w = decorator.nr.render(canvas, n, false, true)[1];
		return w;
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

	float getX(PhyloNode n)
	{
		return n.getX();
		// return (float) (n.getX() * scaleX + dx);
	}

	float getY(PhyloNode n)
	{
		return n.getY();
		// return (float) (n.getY() * scaleY + dy);
	}

	protected void handleNode(PhyloNode n)
	{
		if (tree.isLeaf(n))
		{
			decorator.render(this, n);
			//			decorator.lineRender.render(canvas, n, true,false);
			//			decorator.nr.render(canvas, n,true,false);
		} else
		{
			n.drawLabel = true;
			if (insertAndReturnOverlap(n))
				n.drawLabel = false;
			decorator.render(this, n);
			//			decorator.lineRender.render(canvas, n, true,false);
			//			decorator.nr.render(canvas, n,true,false);
			/*
			 * If we're a NHX node, then draw the bootstrap (if the config says so).
			 */
			drawBootstrap(n);

			//			drawCladeLabelIfNeeded(n);

			/*
			 * Do some extra stuff to clean up the thresholding artifacts.
			 */
			List l = tree.getChildrenOf(n);
			int sz = l.size();
			for (int i = 0; i < sz; i++)
			{
				PhyloNode child = (PhyloNode) l.get(i);
				NodeRange r = child.range;
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
					// GJ 19-09-08 change: Loop to render from the first / last leaf all the way to the current node.
					while (leaf != n)
					{
						//						decorator.lineRender.render(canvas, leaf, true,false);
						//						decorator.nr.render(canvas, leaf,true,false);
						//						drawCladeLabelIfNeeded(leaf);
						leaf.drawLineAndNode = true;
						leaf.drawLabel = false;
						decorator.render(this, leaf);
						leaf = (PhyloNode) tree.getParentOf(leaf);
					}
				}
			}
		}
	}

	void drawCladeLabelIfNeeded(PhyloNode n)
	{
		if (tree.isLeaf(n))
			return;
		if (context.config().showCladeLabels && tree.isLabelSignificant(tree.getLabel(n)))
		{
			boolean overlap = insertAndReturnOverlap(n);
			if (!overlap)
			{
				//				decorator.lr.render(canvas, n, true,false);
			}
		}
	}

	void hint()
	{
		if (UIUtils.isJava2D(canvas))
		{
			Graphics2D g2 = ((PGraphicsJava2D) canvas).g2;
			oldRH = g2.getRenderingHints();
			if (context.config().antialias)
			{
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			} else
			{
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		} else
		{
			if (context.config().antialias)
			{
				canvas.smooth();
			} else
			{
				canvas.noSmooth();
			}
		}

		//		if (textSize > 100)
		//		{
		//			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		//					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		//			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		//					RenderingHints.VALUE_ANTIALIAS_OFF);
		//		}

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

	Rectangle2D.Float rect1 = new Rectangle2D.Float();
	Rectangle2D.Float rect2 = new Rectangle2D.Float();
	Rectangle2D.Float rect3 = new Rectangle2D.Float();

	protected boolean isNodeWithinScreen(PhyloNode n)
	{
		/*
		 * Get this node range and set the rect.
		 */
		NodeRange r = n.range;
		//		Rectangle rect1 = new Rectangle();
		float EXPAND = 50;
		float EXPAND2 = 100;
		/*
		 * Try to get the parental noderange and set it.
		 */
		PhyloNode p = (PhyloNode) tree.getParentOf(n);
		rect1.x = r.loX - EXPAND;
		rect1.y = r.loY - EXPAND;
		rect1.width = r.hiX - r.loX + EXPAND2;
		rect1.height = r.hiY - r.loY + EXPAND2;

		if (p == null)
		{
			/*
			 * If we're the root node, just intersect this node's rect with the screen.
			 */
			return rect1.intersects(screenRect);
		} else
		{
			NodeRange r2 = p.range;
			/*
			 * Find the union of ourselves and our parent, and then intersect with screen.
			 * (This fixes the problem where a node is off the screen but we want its parent-line drawn.
			 */
			rect2.x = r2.loX - EXPAND;
			rect2.y = r2.loY - EXPAND;
			rect2.width = r2.hiX - r2.loX + EXPAND2;
			rect2.height = r2.hiY - r2.loY + EXPAND2;

			Rectangle.union(rect1, rect2, rect3);
			return screenRect.intersects(rect3);
		}
	}

	/**
	 * Updates this renderer's internal representation of the tree. This should
	 * only be called when the tree is changed.
	 */
	protected void layout()
	{
		if (!needsLayout)
			return;
//		System.out.println("Layout "+System.currentTimeMillis());
		needsLayout = false;

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
			sigLeaves = new PhyloNode[leaves.length];
			for (int i = 0; i < leaves.length; i++)
			{
				sigLeaves[i] = leaves[i];
			}
			int dir = 1;
			if (context.config().prioritizeDistantLabels)
				dir = -1;
			Arrays.sort(sigLeaves, 0, sigLeaves.length, tree.new DepthToRootComparator(dir));
			Thread.yield();
		}

		/*
		 * Crate new nodeRange objects for this layout.
		 */
		synchronized (list)
		{
			list.clear();
			for (int i = 0; i < nodes.length; i++)
			{
				PhyloNode n = (PhyloNode) nodes[i];
				synchronized (n)
				{
					n.range.render = this;
				}
				list.insert(n.range, false);
			}
			list.sortFull();
		}
		Thread.yield();

		/*
		 * ASSUMPTION: the leaves ArrayList contains a "sorted" view of the
		 * tree's leaves, i.e. in the correct ordering from top to bottom.
		 */
		//		FontMetrics fm = canvas.g2.getFontMetrics(font.font);
		//		FontMetrics fm = UIUtils.getMetrics(canvas, font.font, font.size);
		for (int i = 0; i < nodes.length; i++)
		{
			PhyloNode n = nodes[i];

			// GJ 2008-10-15: Add a NodeUncollapser if it doesn't exist.
			if (tree.isCollapsed(n) && !NodeUncollapser.containsNode(n))
			{
				tree.collapseNode(n);
			}

			/*
			 * If we have NHX annotations, put our species into the colors map.
			 * This is done within this loop just to save the effort of looping through all
			 * nodes again during layout.
			 */
			if (n.isNHX() && context.config().colorSpecies)
			{
				String tax = n.getAnnotation(TAXON_ID);
				if (tax != null)
				{
					decorator.taxonColorMap.put(tax, null);
				} else
				{
					String spec = n.getAnnotation(SPECIES_NAME);
					if (spec != null)
						decorator.taxonColorMap.put(spec, null);
				}
			}
			//			Graphics2D g2 = ((PGraphicsJava2D) canvas).g2;
			//				width = (float) fm.getStringBounds(n.getLabel(), g2).getWidth() / 100f;
			float width = UIUtils.getTextWidth(canvas, fonts.getPFont(), 100, n.getLabel(), true) / 100f;
			n.unitTextWidth = width;
		}

		Thread.yield();

		if (context.config().colorSpecies)
		{
			decorator.getColorsForSpeciesMap();
		}
		treeLayout.layout(tree, leaves, nodes);
	}

	public void layoutTrigger()
	{
		needsLayout = true;
	}

	public void nodesInRange(ArrayList arr, Rectangle2D.Float rect)
	{
		synchronized (list)
		{
			list.getInRange(arr, rect);
		}
	}

	public void positionText(PhyloNode n, TextField tf)
	{
		decorator.lr.positionText(this, n, tf);
	}

	protected void recalc()
	{
		
		rowSize = rect.height / leaves.length;
		textSize = rowSize * 0.9f;
		dotWidth = getNormalLineWidth() * context.config().nodeSize;
		scaleX = rect.width;
		scaleY = rect.height;
		float scale = (float) Math.min(scaleX, scaleY);
		scaleX = scaleY = scale;
		// If we have few nodes, don't fill it up so much.
		if (leaves.length <= 10)
		{
			float scaleMult = 0.025f;
			float scaleFactor = scaleMult + (leaves.length) * ((1 - scaleMult) / 10);
			scaleX *= scaleFactor;
			scaleY *= scaleFactor;
			rowSize *= scaleFactor;
			textSize *= scaleFactor;
			dotWidth *= scaleFactor;
		}

		dx = (rect.width - scaleX) / 2;
		dy = (rect.height - scaleY) / 2;
		dx += rect.getX();
		dy += rect.getY();

		PFont font = fonts.getPFont();
		dFont = (font.ascent() - font.descent()) * textSize / 2;
	}

	private void updateNodes()
	{
		int len = nodes.length;
		for (int i=0; i < len; i++)
		{
			PhyloNode n = nodes[i];
			updateNode(n);
		}
	}
	
	private TreeRenderState lastRenderState;
	private long nodePositionHash;
	
	public void render(PGraphics canvas, float x, float y, float w, float h, boolean mainRender)
	{
		this.mainRender = mainRender;
		rect.setRect(x, y, w, h);
		if (tree == null)
			return;
		
		// GJ 2009-02-15
		shouldTriggerRepaint = true;
		nodePositionHash = 0;
		
//		synchronized (this)
//		{
//			this.canvas = canvas;	
//			layout();
//		}
//		synchronized (tree)
//		{
//			recalc();
//			updateNodes();
//		}
		
		// GJ 2009-02-15 : stop re-rendering when the tree ain't changing!!
//		TreeRenderState trs = new TreeRenderState(this);
//		if (trs.equals(lastRenderState))
//		{
////			System.out.println("Nothing new!!!");
//			shouldTriggerRepaint = false;
//		} else
//		{
//			lastRenderState = trs;
//		}
		
		if (context.config().useDoubleBuffering)
		{
			drawDoubleBuffered(canvas);
		} else
		{
			synchronized (tree)
			{
				this.canvas = canvas;
				layout();
				recalc();
//				updateNodes();
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
		super.drawToBuffer(g);
		
		this.canvas = g;
		g.background(0, 0);
		/*
		 * All operations requiring integrity of the tree structure should synchronize on the tree object!
		 */
		synchronized (tree)
		{
			layout();
			recalc();
//			updateNodes();
			draw();
		}
		this.canvas = null;
	}

	//	public UIRectangle getVisibleRect()
	//	{
	//		float rx = (float) dx;
	//		float ry = (float) (dy + (overhang < 0 ? overhang * textSize : 0));
	//		float sx = (float) (scaleX + biggestAspectRatio * textSize + dotWidth * 2);
	//		float sy = (float) (scaleY + Math.abs(overhang * textSize));
	//		return new UIRectangle(rx, ry, sx, sy);
	//	}

	Point mousePt = new Point();

	private float overhang;

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
		{
			synchronized (tree)
			{
				tree.removeGraphListener(this);
				tree.dispose();
				tree = null;
			}
		}
		synchronized (t)
		{
			tree = t;
			tree.addGraphListener(this);
			needsLayout = true;
			if (!context.config().animateNewTree)
				fforwardMe = true;
		}
	}

	void unhint()
	{
		//		if (textSize > 100 && UIUtils.isJava2D(canvas))
		//		{
		if (UIUtils.isJava2D(canvas))
		{
			Graphics2D g2 = ((PGraphicsJava2D) canvas).g2;
			g2.setRenderingHints(oldRH);
		}
		//		}
	}

	private Point2D.Float tempPt = new Point2D.Float();

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
		n.setX(calcRealX(n));
		n.setY(calcRealY(n));

		/*
		 * Update the nodeRange.
		 */
		NodeRange r = n.range;

		decorator.setCornerPoints(this, n);

		if (n.rect.getWidth() == 0)
		{
			n.rect.setFrame(n.getX(), n.getY(), 0, 0);
		}
		
		r.loX = n.rect.x;
		r.hiX = n.rect.x + n.rect.width;
		r.loY = n.rect.y;
		r.hiY = n.rect.y + n.rect.height;
		
		nodePositionHash += n.getX();
		nodePositionHash += n.getY();
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

	private LayoutBase oldLayout = null;

	public void setLayout(LayoutBase layout)
	{
		this.oldLayout = this.treeLayout;
		this.treeLayout = layout;
		layoutTrigger();
	}

	public LayoutBase getLayout()
	{
		return treeLayout;
	}

	public void forceLayout()
	{
		needsLayout = true;
		layoutTrigger();
	}

	static class ZOrderComparator implements Comparator<PhyloNode>
	{
		public int compare(PhyloNode o1, PhyloNode o2)
		{
			String z1 = o1.getAnnotation(UsefulConstants.Z_ORDER);
			String z2 = o2.getAnnotation(UsefulConstants.Z_ORDER);
			if (z1 == null)
				return -1;
			else if (z2 == null)
				return 1;
			else
			{
				int z1i = Integer.parseInt(z1);
				int z2i = Integer.parseInt(z2);
				if (z1i > z2i)
					return 1;
				else if (z1i == z2i)
					return 0;
				else
					return -1;
			}
		}

	}
	
	class TreeRenderState
	{
		public int treeModCount;
		public double x;
		public double y;
		public double zoom;
		public long nodePositionHash;
		
		public TreeRenderState(BasicTreeRenderer r)
		{
			this.treeModCount = r.tree.getModCount();
			this.x = r.rect.x;
			this.y = r.rect.y;
			this.zoom = r.rect.getWidth();
			this.nodePositionHash = r.nodePositionHash;
		}
		
		public boolean equals(Object o)
		{
			if (o == null)
				return false;
			TreeRenderState b = (TreeRenderState) o;
			return (b.x == x && b.y == y && b.treeModCount == treeModCount && b.zoom == zoom && b.nodePositionHash == nodePositionHash);
		}
	}

	public FontLoader getFontLoader()
	{
		return fonts;
	}
}
