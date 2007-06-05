package org.andrewberman.phyloinfo.render;

import org.andrewberman.util.Locatable;
import org.andrewberman.util.Position;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.andrewberman.phyloinfo.PhyloWidget;
import org.andrewberman.phyloinfo.tree.Tree;
import org.andrewberman.phyloinfo.tree.TreeNode;

import processing.core.PConstants;
import processing.core.PFont;

public class CladoRenderer implements Locatable
{

	private PhyloWidget p;

	private Tree tree;

	private PFont font;
	private static int FONT_SIZE = 64;
	
	private HashMap positions;

	public CladoRenderer()
	{
		// Get our singlet instance.
		p = PhyloWidget.instance;

		// Load the font from the data directory and set it to be used for text
		// drawing.
		font = p.loadFont("Verdana-64.vlw");
		p.textFont(font);
		p.textAlign(PConstants.LEFT);
		
		positions = new HashMap();
	}

	public void render()
	{
		if (tree == null) return;
		synchronized (tree)
		{
			leafPositions();
			branchPositions();
			drawLines();
		}
//		getRect(); // for testing purpose.
	}
	
	private void leafPositions() {
		ArrayList leaves = tree.getRoot().getAllLeaves();
		for (int i = 0; i < leaves.size(); i++)
		{
			TreeNode leaf = (TreeNode) leaves.get(i);
			Position pt = new Position(xPosForNode(leaf), i * RenderingConstants.LEAF_SPACING + (float) font.ascent / 2.0f);
			positions.put(leaf.getSerial(), pt);
		}
		
	}
	private void branchPositions() {
		branchPositions(tree.getRoot());
	}
	private Position branchPositions(TreeNode n) {
		if (n.isLeaf()) {
			// If N is a leaf, then it's already been laid out.
			return (Position) positions.get(n.getSerial());
		} else {
			// If not:
			// 	Y coordinate should be the average of its children's heights
			ArrayList children = n.getChildren();
			float sum = 0;
			for (int i=0; i < children.size(); i++)
			{
				TreeNode child = (TreeNode) children.get(i);
				sum += branchPositions(child).y;
			}
			float y = (float) sum / (float) children.size();
			float x = xPosForNode(n);
			Position pt = new Position(x,y);
			positions.put(n.getSerial(),pt);
			return pt;
		}
	}
	private float xPosForNode(TreeNode n) {
		return (float) RenderingConstants.BRANCH_LENGTH * (tree.getRoot().getMaxHeight() - n.getMaxHeight()) + 3; 
	}
	private void drawLines() {
		ArrayList nodes = tree.getAllNodes();
		for (int i=0; i < nodes.size(); i++) {
			TreeNode n = (TreeNode) nodes.get(i);
			Position pt = (Position) positions.get(n.getSerial());
			p.stroke(255);
			p.ellipse(pt.x,pt.y,5,5);
			connectToParent(n);
			if (n.isLeaf()) {
				p.fill(255);
				p.textSize(FONT_SIZE);
				p.text(n.getName(),pt.x + RenderingConstants.NAMES_MARGIN,pt.y + font.ascent/2);
			}
		}
	}
	
	private void connectToParent(TreeNode n) {
		if (n.getParent() == TreeNode.NULL_PARENT) return;
		Position ptA = (Position) positions.get(n.getSerial());
		Position ptB = (Position) positions.get(n.getParent().getSerial());
		p.stroke(255);
		p.line(ptA.x,ptA.y,ptB.x,ptA.y);
		p.line(ptB.x,ptA.y,ptB.x,ptB.y);
	}
	
	public Rectangle2D.Float getRect() {
		float top = 0.0f;
		float left = 0.0f;
		float right = RenderingConstants.BRANCH_LENGTH * (tree.getRoot().getMaxHeight()) + RenderingConstants.NAMES_MARGIN + getMaxNameWidth();
		float bottom = RenderingConstants.LEAF_SPACING * (tree.getRoot().getAllLeaves().size() - 1) + font.ascent + font.descent;
		return new Rectangle2D.Float(left,top,right-left,bottom-top);
	}
	
	public float getMaxNameWidth() {
		ArrayList leaves;
		leaves = tree.getRoot().getAllLeaves();
		float maxWidth = 0;
		for (int i=0; i < leaves.size(); i++)
		{
			TreeNode n = (TreeNode) leaves.get(i);
			p.textSize(FONT_SIZE);
			float width = p.textWidth(n.getName());
			if (width > maxWidth) maxWidth = width;
		}
		System.out.println(maxWidth);
		return maxWidth;
	}
	
	public void setTree(Tree newTree)
	{
		tree = newTree;
	}
}
