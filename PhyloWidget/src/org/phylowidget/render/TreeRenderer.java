package org.phylowidget.render;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;

import org.andrewberman.sortedlist.SortedXYRangeList;
import org.andrewberman.ui.FontLoader;
import org.andrewberman.ui.TextField;
import org.andrewberman.ui.UIUtils;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.event.VertexSetListener;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.ui.HoverHalo;
import org.phylowidget.ui.PhyloNode;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;

public interface TreeRenderer
{
	public static final int NODE = 0;
	public static final int LABEL = 1;
	
	public void render(PGraphics canvas, float x, float y, float w, float h);

	public void layout();

	public void setTree(RootedTree t);

	public RootedTree getTree();

	public void nodesInRange(ArrayList list, Rectangle2D.Float rect);

	public float getNodeRadius();

	public void positionText(PhyloNode node, TextField text);

	
}
