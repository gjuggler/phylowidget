package org.andrewberman.phyloinfo.render;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.andrewberman.camera.Locatable;
import org.andrewberman.phyloinfo.tree.Tree;
import org.andrewberman.phyloinfo.tree.TreeNode;
import org.andrewberman.util.Position;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

public class Cladogram implements Locatable
{
	protected PApplet p;

	protected Tree tree;

	protected PFont font;
	protected static int FONT_SIZE = 64;
	
	protected HashMap positions;

	public Cladogram(PApplet applet)
	{

		p = applet;
		p.registerDraw(this);
		
		// Load the font from the data directory and set it to be used for text
		// drawing.
		font = p.loadFont("TimesNewRoman-64.vlw");
		
		positions = new HashMap();
	}

	public void draw()
	{
		if (tree == null) return;
		synchronized (tree)
		{
			setFont();
			leafPositions();
			branchPositions();
			drawLines();
		}
//		getRect(); // for testing purpose.
	}
	
	public void setFont()
	{
		p.fill(255);
		p.textFont(font);
		p.textAlign(PConstants.LEFT);
		p.textSize(FONT_SIZE);
	}
	
	public void leafPositions() {
		ArrayList leaves = tree.getRoot().getAllLeaves();
		for (int i = 0; i < leaves.size(); i++)
		{
			TreeNode leaf = (TreeNode) leaves.get(i);
			Position pt = new Position(xPosForNode(leaf), i * RenderingConstants.LEAF_SPACING + (float) font.ascent / 2.0f);
			positions.put(leaf.getSerial(), pt);
		}
		
	}
	public void branchPositions() {
		branchPositions(tree.getRoot());
	}
	public Position branchPositions(TreeNode n) {
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
	public float xPosForNode(TreeNode n) {
		return (float) RenderingConstants.BRANCH_LENGTH * (tree.getRoot().getMaxHeight() - n.getMaxHeight()) + 3; 
	}
	public void drawLines() {
		ArrayList nodes = tree.getAllNodes();
		for (int i=0; i < nodes.size(); i++) {
			TreeNode n = (TreeNode) nodes.get(i);
			Position pt = (Position) positions.get(n.getSerial());
			p.stroke(255);
			p.ellipse(pt.x,pt.y,5,5);
			connectToParent(n);
			if (n.isLeaf()) {
				p.text(n.getName(),pt.x + RenderingConstants.NAMES_MARGIN,pt.y + font.ascent/2);
			}
		}
	}
	
	public void connectToParent(TreeNode n) {
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
			setFont();
			float width = p.textWidth(n.getName());
			if (width > maxWidth) maxWidth = width;
		}
		return maxWidth;
	}
	
	public void setTree(Tree newTree)
	{
		tree = newTree;
	}
}
