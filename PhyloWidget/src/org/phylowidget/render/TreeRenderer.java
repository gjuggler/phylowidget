package org.phylowidget.render;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.TextField;
import org.phylowidget.tree.RenderNode;
import org.phylowidget.tree.Tree;
import org.phylowidget.tree.TreeNode;

public interface TreeRenderer
{
	public void render();
	
	public void layout();
	
	public void setTree(Tree t);
	
	public Tree getTree();
	
	public void nodesInRange(ArrayList list, Rectangle2D.Float rect);
	
	public float getNodeRadius();
	
	public void positionText(RenderNode node, TextField text);
	
	public void setRect(float cx, float cy, float w, float h);
}
