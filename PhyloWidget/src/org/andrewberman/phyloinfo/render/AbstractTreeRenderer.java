package org.andrewberman.phyloinfo.render;

import java.awt.geom.Rectangle2D;

import org.andrewberman.phyloinfo.tree.Tree;

import processing.core.PApplet;

/*
 * The abstract tree renderer class.
 * @author Greg Jordan
 */
public abstract class AbstractTreeRenderer implements TreeRenderer
{
	/*
	 * The rectangle that defines the area in which this renderer will
	 * draw itself.
	 */
	public Rectangle2D.Float rect;
	
	/*
	 * The tree that will be rendered.
	 */
	protected Tree tree;
	
	public AbstractTreeRenderer()
	{
		rect = new Rectangle2D.Float(0,0,100,100);
	}
	
	public void setTree(Tree t)
	{
		tree = t;
	}
	
	public Tree getTree()
	{
		return tree;
	}
	
	public void setRect(float x, float y, float w, float h)
	{
		rect.setFrame(x,y,w,h);
	}
	public Rectangle2D.Float getRect()
	{
		return rect;
	}
}
