package org.andrewberman.phyloinfo.render;

import java.util.ArrayList;

import org.andrewberman.phyloinfo.tree.TreeNode;
import org.andrewberman.util.Position;

import processing.core.PApplet;

public class DiagonalCladogram extends Cladogram
{

	public DiagonalCladogram(PApplet p)
	{
		super(p);
	}
	
	
	
	public void connectToParent(TreeNode n)
	{
//		if (n.getParent() == TreeNode.NULL_PARENT) return;
//		Position ptA = (Position) positions.get(n.getSerial());
//		Position ptB = (Position) positions.get(n.getParent().getSerial());
//		p.stroke(255);
//		p.line(ptA.x,ptA.y,ptB.x,ptB.y);
//		p.line(ptA.x,ptA.y,ptB.x,ptA.y);
//		p.line(ptB.x,ptA.y,ptB.x,ptB.y);
	}
	
}
