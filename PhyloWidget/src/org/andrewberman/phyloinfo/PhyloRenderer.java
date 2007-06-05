package org.andrewberman.phyloinfo;

import java.util.ArrayList;
import java.util.Stack;

import org.andrewberman.phyloinfo.tree.TreeNode;

import processing.core.PApplet;
import processing.core.PFont;

public class PhyloRenderer
{

	private PhyloWidget p;
	private TreeNode root;
	private PFont font;

	public PhyloRenderer()
	{
		// Get our singlet instance.
		p = PhyloWidget.instance;
		
		// Load the font from the data directory and set it to be used for text drawing.
		font = p.loadFont("Verdana-64.vlw");
		p.textFont(font);
		p.textAlign(PApplet.LEFT);
	}

	public void setTree(TreeNode newRoot)
	{
		root = newRoot;
	}

	public void render()
	{
		// Step 1: Draw left-aligned text of leaf nodes in an appropriate order.
		ArrayList leaves = new ArrayList();
		Stack stack = new Stack();
		stack.push(root);
		while (!stack.isEmpty())
		{
			TreeNode curNode = (TreeNode) stack.pop();
			if (curNode.isLeaf())
			{
				leaves.add(curNode);
				p.text(curNode.getName(),0,50 + leaves.size()*font.ascent*2);
			} else
			{
				stack.addAll(curNode.children);
			}
		}
		
		
	}

}
