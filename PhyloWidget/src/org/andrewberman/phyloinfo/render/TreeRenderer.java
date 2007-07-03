package org.andrewberman.phyloinfo.render;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.phyloinfo.tree.Tree;

public interface TreeRenderer
{
	public void render();
	
	public void setTree(Tree t);
	
	public void nodesInRange(ArrayList list, Rectangle2D.Float rect);
	
	public void setRect(float cx, float cy, float w, float h);
}
