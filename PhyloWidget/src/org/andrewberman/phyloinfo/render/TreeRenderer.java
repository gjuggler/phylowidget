package org.andrewberman.phyloinfo.render;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.andrewberman.phyloinfo.tree.Tree;
import org.andrewberman.phyloinfo.tree.TreeNode;


public interface TreeRenderer
{
	public void render();
	
	public void setTree(Tree t);
	public Tree getTree();
	
	public void setRect(float x, float y, float w, float h);
	public Rectangle2D.Float getRect();
	
	public Point2D.Float getNearestPoint();
	public TreeNode getNearestNode();
}
