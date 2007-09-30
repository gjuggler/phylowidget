package org.phylowidget.render;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.sortedlist.SortedXYRangeList;
import org.andrewberman.ui.FontLoader;
import org.andrewberman.ui.UIUtils;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.ui.HoverHalo;
import org.phylowidget.ui.PhyloNode;

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

	protected ArrayList ranges = new ArrayList();

	protected ArrayList inRange = new ArrayList();

	protected boolean sorted = false;

	protected boolean needsLayout;

	public AbstractTreeRenderer(PApplet p)
	{
		this.p = p;
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

	public void render(PGraphics canvas, float x, float y, float w, float h)
	{
		this.canvas = canvas;
		rect.setRect(x, y, w, h);
		if (tree == null)
			return;
		synchronized (tree)
		{
			if (needsLayout)
			{
				update();
				needsLayout = false;
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
		tree.getAll(tree.getRoot(), leaves, nodes);

		doTheLayout();
		createEmptyNodeRanges();
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
	protected void doTheLayout()
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
			PhyloNode n = (PhyloNode) nodes.get(i);
			NodeRange r = new NodeRange();
			r.loX = n.getTargetX();
			r.hiX = n.getTargetX() + .001f;
			r.loY = n.getTargetY();
			r.hiY = n.getTargetY() + .001f;
			r.type = NodeRange.NODE;
			r.node = n;
			r.render = this;
			ranges.add(r);
			list.insert(r, false);
			if (tree.isLeaf(n))
			{
				NodeRange r2 = new NodeRange();
				r2.loX = r2.hiX = n.getTargetX();
				r2.loY = r2.hiY = n.getTargetY() + .001f;
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
		try
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
		Rectangle2D.Float screenRect = new Rectangle2D.Float(0, 0,
				canvas.width, canvas.height);
		UIUtils.screenToModel(screenRect);
		list.getInRange(inRange, screenRect);
		/*
		 * Set up some rendering constants so we don't recalculate within the
		 * loop.
		 */
		baseStroke = getNodeRadius() / 10f;
		int size = inRange.size();
		for (int i = 0; i < size; i++)
		{
			NodeRange r = (NodeRange) inRange.get(i);
			PhyloNode n = r.node;
			switch (r.type)
			{
				case (TreeRenderer.LABEL):
					canvas.fill(colorForNode(n));
					drawLabel(n);
					break;
				case (TreeRenderer.NODE):
					canvas.strokeWeight(strokeForNode(n));
					canvas.stroke(colorForNode(n));
					drawLine(n);
					canvas.noStroke();
					canvas.fill(colorForNode(n));
					// canvas.fill(style.regColor.getRGB());
					drawNode(n);
					break;
			}
		}
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		} finally
		{
			unhint();
		}
	}

	float baseStroke;

	float strokeForNode(PhyloNode n)
	{
		float stroke = baseStroke;
		if (n.hovered)
			stroke *= style.hoverStroke * HoverHalo.hoverMult;
		else
		{
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
		}
		return stroke;
	}

	int colorForNode(PhyloNode n)
	{
		if (n.hovered)
			return style.hoverColor.getRGB();
		else
		{
			switch (n.getState())
			{
				case (PhyloNode.CUT):
					return style.dimColor.getRGB();
				case (PhyloNode.COPY):
					return style.copyColor.getRGB();
				case (PhyloNode.NONE):
				default:
					return style.regColor.getRGB();
			}
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
		}
	}

	void unhint()
	{
		if (textSize > 100 && UIUtils.isJava2D(canvas))
		{
			Graphics2D g2 = ((PGraphicsJava2D) canvas).g2;
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

	protected void drawLabel(PhyloNode n)
	{
	}

	protected void drawNode(PhyloNode n)
	{
	}

	protected void drawLine(PhyloNode n)
	{
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
		// if (e.getType() == GraphVertexChangeEvent.VERTEX_ADDED)
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
		// if (e.getType() == GraphVertexChangeEvent.VERTEX_REMOVED)
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
}
