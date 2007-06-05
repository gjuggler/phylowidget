package org.andrewberman.phyloinfo;

import org.andrewberman.phyloinfo.tree.TreeNode;

import processing.core.PApplet;
import processing.core.PFont;

public class PhyloWidget extends PApplet {

public static PhyloWidget instance;
public PFont font;

public PhyloWidget() {
	super();
	instance = this;
}
	
public void setup() {
	size(400,300);
	background(0,0,0);
	
	test();
}

public void draw() {
	
}

public void test() {
	TreeNode a = new TreeNode();
	TreeNode b = new TreeNode(a);
	b.setName("Tree B");
	TreeNode c = new TreeNode(a);
	TreeNode d = new TreeNode(c);
	d.setName("Tree D");
	TreeNode e = new TreeNode(c);
	e.setName("Tree E");
	TreeNode f = new TreeNode(c);
	PhyloRenderer r = new PhyloRenderer();
	r.setTree(a);
	r.render();
}

}