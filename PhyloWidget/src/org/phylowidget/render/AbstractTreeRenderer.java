package org.phylowidget.render;

import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.andrewberman.sortedlist.SortedXYRangeList;
import org.andrewberman.sortedlist.XYRange;
import org.andrewberman.ui.FontLoader;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.Tree;
import org.phylowidget.tree.TreeNode;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphicsJava2D;

/**
 * The abstract tree renderer class.
 * @author Greg Jordan
 */
public abstract class AbstractTreeRenderer implements TreeRenderer
{
	protected PApplet p = PhyloWidget.p;
	
	/**
	 * The rectangle that defines the area in which this renderer will
	 * draw itself.
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
	 * All nodes in the associated tree.
	 */
	protected ArrayList nodes = new ArrayList();
	
	/**
	 * Leaf nodes in the associated tree.
	 */
	protected ArrayList leaves = new ArrayList();
	
	/**
	 * Stores positions for all nodes (internal and leaf nodes).
	 * 
	 * Key = TreeNode
	 * Value = org.andrewberman.util.Point
	 * 
	 * Positions should range from 0 to 1 in the x and y
	 * directions. During rendering, these values will be multiplied
	 * accordingly to fill the enclosing rectangle.
	 */
	protected HashMap positions = new HashMap();
	
	/**
	 * Transformations required to go from the stored position to the
	 * actual position. Should be set at the beginning of each draw.
	 */
	protected float scaleX, scaleY = 0;
	protected float dx, dy = 0;
	
	/**
	 * Tracks the last modification count at which this renderer
	 * was synchronized with its tree. This allows us to only recalculate
	 * positions when the tree structure actually changes.
	 */
	protected int lastModCount = 0;



	public AbstractTreeRenderer()
	{
		rect = new Rectangle2D.Float(0,0,0,0);
		font = FontLoader.f64;
	}
	
	public void render()
	{
		if (tree == null)
			return;
		synchronized (tree)
		{	
			if (tree.modCount != lastModCount)
			{
				update();
				lastModCount = tree.modCount;
			}
			draw();
		}
	}
	
	/**
	 * Updates this renderer's internal representation
	 * of the tree.
	 * 
	 * This should only be called when the tree is changed.
	 *
	 */
	public void update()
	{
		leaves.clear();
		nodes.clear();
		tree.getAll(leaves, nodes);
		
		layout();
	}
	
	/**
	 * Calculate the layout of the nodes within this renderer.
	 * Only called when the tree structure is changed, so it's okay
	 * for this to be a relatively expensive operation.
	 * 
	 * This should populate the positions HashMap with the positions
	 * of all nodes.
	 */
	public void layout(){}
	
	/**
	 * Draws this renderer's view to the canvas.
	 *
	 */
	public void draw()
	{
	}
	
	public void setTree(Tree t)
	{
		tree = t;
		this.lastModCount = t.modCount - 1;
	}


	public void setPosition(TreeNode n, float x, float y)
	{
		if (positions.get(n) == null)
		{
			positions.put(n, new Point(x,y));
		}
		Point pt = (Point)positions.get(n);
		pt.setLocation(x,y);
	}

	public Point getPosition(TreeNode n)
	{
		return (Point) positions.get(n);
	}
	
	public Point getTranslatedPosition(TreeNode n, Point pt)
	{
		Point orig = getPosition(n);
		pt.setLocation(orig.x*scaleX + dx, orig.y*scaleY + dy);
		return pt;
	}
	
	public void setRect(float cx, float cy, float w, float h)
	{
		rect.setFrameFromCenter(cx,cy,cx-w/2,cy-h/2);
	}
}
