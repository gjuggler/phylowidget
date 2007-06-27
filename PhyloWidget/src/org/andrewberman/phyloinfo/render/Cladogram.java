package org.andrewberman.phyloinfo.render;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.andrewberman.phyloinfo.tree.Tree;
import org.andrewberman.phyloinfo.tree.TreeNode;
import org.andrewberman.ui.ProcessingUtils;

import processing.core.PApplet;
import processing.core.PFont;

public class Cladogram extends AbstractTreeRenderer
{
	protected PApplet p;

	/**
	 * A hashmap containing: key is the TreeNode; value is a Point2D
	 * object with the treeNode's position. A position should be an x,y pair
	 * where x and y are between 0 and 1, respectively. These position values
	 * are appropriately scaled and translated in the drawing steps. This way,
	 * we can calculate the positions only when the tree changes, and the
	 * redrawing only requires stepping through all leaves/nodes and multiplying
	 * by the width and height of the current rectangle.
	 */
	protected HashMap positions = new HashMap();

	/**
	 * A hashmap containing the "actual" node positions, i.e. the center of the drawn
	 * circle for each node.
	 */
	protected HashMap realPositions = new HashMap();
	
	protected ArrayList leaves = new ArrayList(200); // all leaves.
	protected ArrayList nodes = new ArrayList(200); // all nodes.
	
	/**
	 * This is the tree's "Mover" object. The mover is updated in the update()
	 * method, and it sets the bounds for this tree's bounding rectangle. This
	 * gives us an easy "camera-like" object to use to smoothly move the tree
	 * around the stage. Keep in mind that the mover can either force an aspect
	 * ratio or let the tree choose the best one.
	 */
	protected TreeMover mover;

	/**
	 * Maximum label width (in pixels, at a size 1 font).
	 * This is scaled up by fontSize in order to get the actual max label width.
	 */
	protected float maxLabelWidth = 0;
	protected PFont font;
	protected float textSize = 12;

	/**
	 * These variables are set in the calculateSizes() method during every
	 * round of rendering. Very important!
	 */
	protected float rowSize = 0;
	protected float colSize = 0;
	protected float minSize = 0;
	protected int numRows = 0;
	protected int numCols = 0;
	
	public Cladogram(PApplet applet)
	{
		this(applet, null);
	}

	public Cladogram(PApplet applet, Tree tree)
	{
		p = applet;
		this.tree = tree;

		mover = new TreeMover(p, this);
		mover.fillScreen();
		mover.forceAspectRatio = false;

		// Load the font from the data directory and set it to be used for text
		// drawing.
		font = p.loadFont("TimesNewRoman-32.vlw");
	}

	float pos = 0;

	public void render()
	{
		if (tree == null)
			return;

		p.pushMatrix();
		mover.updatePosition();
		synchronized (tree)
		{
			if (tree.needsUpdating)
			{
				leaves.clear();
				tree.getAllLeaves(leaves);
				nodes.clear();
				tree.getAllNodes(nodes);
				calcMaxNameWidth();
			}
			calculateSizes(); // this requires knowing the max name width.
			if (tree.needsUpdating)
			{
				leafPositions();
				branchPositions();
				tree.needsUpdating = false;
			}
			translateStage();
			drawLines();
			drawNodes();
		}
		p.popMatrix();
	}

	public void translateStage()
	{
		p.translate(getTranslationX(),getTranslationY());
	}
	
	public float getTranslationX()
	{
		if (mover.forceAspectRatio)
		{
			return rect.x + colSize / 2;
		} else
		{
			float gutterWidth = maxLabelWidth*textSize;
			float realWidth =
				minSize / colSize * (rect.width - gutterWidth) + gutterWidth;
//			float realHeight =
//				minSize / rowSize * (rect.height);
//			colSize = minSize;
			return rect.x + minSize / 2 + (rect.width - realWidth)/2;
		}
	}
	
	public float getTranslationY()
	{
		if (mover.forceAspectRatio)
		{
			return rect.y + rowSize / 2;
		} else
		{
//			float gutterWidth = maxLabelWidth*textSize;
//			float realWidth =
//				minSize / colSize * (rect.width - gutterWidth) + gutterWidth;
			float realHeight =
				minSize / rowSize * (rect.height);
//			rowSize = minSize;
			return rect.y + minSize / 2 + (rect.height - realHeight)/2;
		}
	}
	
	public void calculateSizes()
	{
		// Calculate the "standard" row height.
//		numRows = tree.getRoot().getNumLeaves();
		numRows = leaves.size();
//		System.out.println(numRows);
		rowSize = rect.height / (float) numRows;

		// at what font size would the labelas take up half the width?
		float biggestSize = rect.width / 2 / maxLabelWidth;
		textSize = Math.min(biggestSize, rowSize);

		// Use font size and max height to get width per unit height.
		numCols = tree.getRoot().getMaxDepth();
		colSize = (rect.width - maxLabelWidth * textSize) / (float) numCols;
		
		minSize = Math.min(rowSize,colSize);
	}

	public void calcMaxNameWidth()
	{
		maxLabelWidth = 0;
		for (int i = 0; i < leaves.size(); i++)
		{
			TreeNode n = (TreeNode) leaves.get(i);

			char[] chars = n.getName().toCharArray();
			// Include a leading space to separate from the node ellipse.
			float width = font.width(' ')*2;
			for (int j = 0; j < chars.length; j++)
			{
				width += font.width(chars[j]);
			}
			if (width > maxLabelWidth)
				maxLabelWidth = width;
		}
	}

	public void leafPositions()
	{
		System.out.println(leaves.size());
		for (int i = 0; i < leaves.size(); i++)
		{
			TreeNode leaf = (TreeNode) leaves.get(i);
			setPosition(leaf, xPosForNode(leaf), i);
		}
	}

	public void branchPositions()
	{
		branchPositions(tree.getRoot());
	}

	public float branchPositions(TreeNode n)
	{
		if (n.isLeaf())
		{
			// If N is a leaf, then it's already been laid out.
			return getPosition(n).y;
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
			float x = xPosForNode(n);

			setPosition(n, x, y);
			return y;
		}
	}

	public float xPosForNode(TreeNode n)
	{
		int max = tree.getRoot().getMaxDepth();
		int cur = n.getMaxDepth();
		return (float) (max - cur);
	}

	public void setPosition(TreeNode n, float x, float y)
	{
		Point2D.Float pt = new Point2D.Float(x, y);
		positions.put(n, pt);
	}

	public Point2D.Float getPosition(TreeNode n)
	{
		return (Point2D.Float) positions.get(n);
	}

	public void drawLines()
	{
		Point2D.Float mouse = new Point2D.Float(p.mouseX,p.mouseY);
		ProcessingUtils.mouseToModel(p, mouse);
		float nearestDistance = Float.MAX_VALUE;
		float currentDistance = 0;
		
		Point2D.Float pt = new Point2D.Float(0,0);
		for (int i = 0; i < nodes.size(); i++)
		{
			TreeNode n = (TreeNode) nodes.get(i);
			pt.setLocation(getPosition(n));
			p.fill(0);
			p.noStroke();
			float radius = Math.min(textSize / 2, colSize / 2);
			p.ellipse(pt.x * colSize, pt.y * rowSize, radius, radius);
		
			pt.setLocation(pt.x * colSize + getTranslationX(),
					pt.y * rowSize + getTranslationY());
			currentDistance = (float) pt.distanceSq(mouse);
			if (currentDistance < nearestDistance)
			{
				nearestDistance = currentDistance;
				nearestNode = n;
				nearestNodePoint.setLocation(pt);
			}
			
			connectToParent(n);
		}
	}

	public Point2D.Float nearestNodePoint = new Point2D.Float(0,0);
	public TreeNode nearestNode = null;
	public Point2D.Float getNearestPoint()
	{
		return nearestNodePoint;
	}
	public TreeNode getNearestNode()
	{
		return nearestNode;
	}
	
	public void drawNodes()
	{
		p.fill(0);
		p.textFont(font);
		p.textAlign(PApplet.LEFT, PApplet.CENTER);
		p.textSize(textSize);
		float space = font.width(' ')*textSize;
		
		Point2D.Float pt = new Point2D.Float(0,0);
		for (int i = 0; i < leaves.size(); i++)
		{
			TreeNode n = (TreeNode)leaves.get(i);
			pt.setLocation(getPosition(n));
			p.text("  "+n.getName(), pt.x * colSize, pt.y
					* rowSize);
		}
	}

	public void connectToParent(TreeNode n)
	{
		if (n.getParent() == TreeNode.NULL_PARENT)
			return;
		Point2D.Float ptA = getPosition(n);
		Point2D.Float ptB = getPosition(n.getParent());
		p.stroke(0);
		p.strokeWeight(1.0f);
		p.line(ptA.x * colSize, ptA.y * rowSize, ptB.x * colSize, ptA.y
				* rowSize);
		p.line(ptB.x * colSize, ptA.y * rowSize, ptB.x * colSize, ptB.y
				* rowSize);
	}

	// public Rectangle2D.Float getRect()
	// {
	// float top = 0.0f;
	// float left = 0.0f;
	// float right = RenderingConstants.BRANCH_LENGTH
	// * (tree.getRoot().getMaxHeight())
	// + RenderingConstants.NAMES_MARGIN + getMaxNameWidth();
	// float bottom = RenderingConstants.LEAF_SPACING
	// * (tree.getRoot().getAllLeaves().size() - 1) + font.ascent
	// + font.descent;
	// // add 10% border.
	// float wBorder = (right - left) / 10.0f;
	// float hBorder = (bottom - top) / 10.0f;
	// return new Rectangle2D.Float(left - wBorder, top - hBorder, right
	// - left + 2 * wBorder, bottom - top + 2 * hBorder);
	// }

}
