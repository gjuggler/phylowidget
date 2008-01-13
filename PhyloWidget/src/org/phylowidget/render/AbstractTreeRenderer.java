package org.phylowidget.render;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.andrewberman.sortedlist.SortedXYRangeList;
import org.andrewberman.ui.Color;
import org.andrewberman.ui.FontLoader;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.menu.MenuUtils;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.UniqueLabeler;
import org.phylowidget.ui.HoverHalo;
import org.phylowidget.ui.PhyloNode;
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
public abstract class AbstractTreeRenderer implements TreeRenderer,
		GraphListener
{
	protected PGraphics canvas;

	/**
	 * The rectangle that defines the area in which this renderer will draw
	 * itself.
	 */
	public Rectangle2D.Float rect, screenRect;

	boolean mainRender;

	/**
	 * The tree that will be rendered.
	 */
	protected RootedTree tree;

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
	 * A data structure to store the rectangular regions of all nodes. Instead
	 * of drawing all nodes, we retrieve the nodes whose regions intersect with
	 * the visible rectangle, and then draw. This can significantly improve
	 * performance when viewing only a portion of a large tree.
	 */
	protected SortedXYRangeList list = new SortedXYRangeList();

	// protected ArrayList<NodeRange> ranges = new ArrayList<NodeRange>();

	protected ArrayList inRange = new ArrayList();

	protected HashMap<PhyloNode, NodeRange> nodesToRanges = new HashMap<PhyloNode, NodeRange>();

	protected boolean needsLayout;

	protected float lineThicknessMult = 0.2f;

	protected int threshold;

	public AbstractTreeRenderer()
	{
		rect = new Rectangle2D.Float(0, 0, 0, 0);
		font = FontLoader.instance.vera;
		style = RenderStyleSet.defaultStyle();
		setOptions();
	}

	/**
	 * This method should be overridden by subclasses that want to change some
	 * of the renderer's boolean options (none available in this abstract class,
	 * but subclasses could define some).
	 */
	protected void setOptions()
	{
	}

	public void render(PGraphics canvas, float x, float y, float w, float h,
			boolean mainRender)
	{
		this.canvas = canvas;
		this.mainRender = mainRender;
		rect.setRect(x, y, w, h);
		if (tree == null)
			return;
		synchronized (tree)
		{
			if (needsLayout)
			{
				updateLayout();
				// System.out.println("update " + System.currentTimeMillis());
				needsLayout = false;
			}
			counter = 0;
			drawRecalc();
			draw();
		}
	}

	/**
	 * Updates this renderer's internal representation of the tree. This should
	 * only be called when the tree is changed.
	 */
	private void updateLayout()
	{
		leaves.clear();
		nodes.clear();
		nodesToRanges.clear();
		tree.getAll(tree.getRoot(), leaves, nodes);
		Collections.sort(nodes, tree.sorter);
		layoutImpl();
//		if (mainRender)
			initNodeRanges();
	}

	final public void layout()
	{
		needsLayout = true;
	}

	/**
	 * Calculate the layout of the nodes within this renderer. Only called when
	 * the tree structure is changed, so it's okay for this to be a relatively
	 * expensive operation.
	 */
	protected void layoutImpl()
	{
	}

	/**
	 * This method creates an empty NodeRange object for each node in the tree,
	 * and an empty NodeRange object of type LABEL for each leaf in the tree.
	 */
	protected void initNodeRanges()
	{
		list.clear();
		nodesToRanges.clear();
		for (int i = 0; i < nodes.size(); i++)
		{
			PhyloNode n = (PhyloNode) nodes.get(i);
			NodeRange r = new NodeRange();
			nodesToRanges.put(n, r);
			// r.loX = n.getTargetX();
			// r.hiX = n.getTargetX() + .001f;
			// r.loY = n.getTargetY();
			// r.hiY = n.getTargetY() + .001f;
			r.type = NodeRange.NODE;
			r.node = n;
			r.render = this;
			initNodeRange(r);
			list.insert(r, false);
		}
		list.sortFull();
	}

	protected void initNodeRange(NodeRange r)
	{

	}

	/**
	 * Draws this renderer's view to the canvas.
	 */
	int counter;

	protected boolean isNodeWithinScreen(PhyloNode n)
	{
		return screenRect.contains(n.x, n.y);
	}

	protected void updateNode(PhyloNode n)
	{
		n.update();
	}

	float baseStroke;

	float strokeForNode(PhyloNode n)
	{
		float stroke = baseStroke;
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

	int colorForNode(PhyloNode n)
	{
		switch (n.getState())
		{
			case (PhyloNode.CUT):
				return style.dimColor.getRGB();
			case (PhyloNode.COPY):
				return style.copyColor.getRGB();
			case (PhyloNode.NONE):
			default:
				return style.foregroundColor.getRGB();
		}
	}

	RenderingHints oldRH;

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
		}
		canvas.noSmooth();
	}

	void unhint()
	{
		if (textSize > 100 && UIUtils.isJava2D(canvas))
		{
			Graphics2D g2 = ((PGraphicsJava2D) canvas).g2;
			g2.setRenderingHints(oldRH);
		}
	}

	protected void draw()
	{
		/*
		 * Call to subclasses that allow them to do some calculations that need
		 * to be performed on a frame-by-frame basis.
		 */
		/*
		 * Now, let's set up the canvas.
		 */
		baseStroke = getRowHeight() * PhyloWidget.ui.lineSize;
		canvas.noStroke();
		canvas.fill(0);

		canvas.textFont(FontLoader.instance.vera);
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
		for (int i = 0; i < nodes.size(); i++)
		{
			PhyloNode n = (PhyloNode) nodes.get(i);
			updateNode(n);
			n.drawMe = false;
			n.isWithinScreen = isNodeWithinScreen(n);
		}
		/*
		 * SECOND LOOP: Flagging drawn nodes
		 *  -- Now, we go through all nodes
		 * (remember this is a list sorted by significance, i.e. enclosed number
		 * of leaves). At each node, we decide whether or not to draw it based
		 * on whether it is within the screen area. We exit the loop once the
		 * threshold number of nodes has been drawn.
		 */
		int nodesDrawn = 0;
		for (int i = 0; i < nodes.size(); i++)
		{
			PhyloNode n = (PhyloNode) nodes.get(i);
			// n.isWithinScreen = isNodeWithinScreen(n);
			if (nodesDrawn >= PhyloWidget.ui.renderThreshold)
				break;
			if (!n.isWithinScreen)
				continue;
			/*
			 * Ok, let's flag this thing to be drawn.
			 */
			n.drawMe = true;
			nodesDrawn++;
		}
		/*
		 * THIRD LOOP: Drawing nodes
		 *   - This loop actually does the drawing.
		 */
		for (int i = 0; i < nodes.size(); i++)
		{
			PhyloNode n = (PhyloNode) nodes.get(i);
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
		unhint();
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
			drawLabel(n);
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
			drawNodeMarker(n);
			drawLine((PhyloNode) n.getParent(), n);
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
					drawLabel(leaf);
				}
			}
			/*
			 * Now, we want to draw this inner node's label if it's significant
			 * and we are set to show clade labels.
			 */
			if (UniqueLabeler.isLabelSignificant(n.getLabel())
					&& PhyloWidget.ui.showCladeLabels)
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
			if (h != null)
			{
				canvas.stroke(style.hoverColor.getRGB());
				float weight = baseStroke * style.hoverStroke;
				weight *= PhyloWidget.ui.traverser.glowTween.getPosition();
				canvas.strokeWeight(weight);
				canvas.fill(style.hoverColor.getRGB());
				drawLine((PhyloNode) h.getParent(), h);
				canvas.noStroke();
				canvas.fill(style.hoverColor.getRGB());
				drawNodeMarker(h);
			}
		}
	}

	protected void drawLabel(PhyloNode n)
	{
		drawLabelImpl(n);
	}

	protected void drawNodeMarker(PhyloNode n)
	{
		canvas.noStroke();
		canvas.fill(colorForNode(n));
		drawNodeMarkerImpl(n);
	}

	protected void drawLine(PhyloNode p, PhyloNode c)
	{
		if (p == null)
			return;
		/*
		 * Keep in mind that p may be null (in the case of root node).
		 */
		float weight = strokeForNode(c);
		canvas.strokeWeight(weight);
		canvas.stroke(colorForNode(c));
		drawLineImpl(p, c);
	}

	abstract void drawLineImpl(PhyloNode p, PhyloNode c);

	abstract void drawNodeMarkerImpl(PhyloNode n);

	abstract void drawLabelImpl(PhyloNode n);

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

	public void setTree(RootedTree t)
	{
		if (tree != null)
			tree.removeGraphListener(this);
		tree = t;
		tree.addGraphListener(this);
		needsLayout = true;
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

	public void edgeAdded(GraphEdgeChangeEvent e)
	{
		needsLayout = true;
	}

	public void edgeRemoved(GraphEdgeChangeEvent e)
	{
		needsLayout = true;
	}

	public RootedTree getTree()
	{
		return tree;
	}

	public void nodesInRange(ArrayList arr, Rectangle2D.Float rect)
	{
		synchronized (list)
		{
			list.getInRange(arr, rect);
		}
	}

	public Object rangeForNode(Object n)
	{
		return nodesToRanges.get(n);
	}

}
