package org.phylowidget.render;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.phylowidget.tree.Tree;
import org.phylowidget.tree.TreeNode;

public interface TreeRenderer
{
	public void render();
	
	public void setTree(Tree t);
	
	public Tree getTree();
	
	public void nodesInRange(ArrayList list, Rectangle2D.Float rect);
	
	public Point getPosition(TreeNode node);
	
	public Point getPosition(TreeNode node, Point pt);
	
	public void setRect(float cx, float cy, float w, float h);
}
